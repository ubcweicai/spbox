package controllers;

import static play.data.Form.form;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Enums.ActivityFolderEnum;
import annotation.Authenticated;
import annotation.ControllerHandler;
import annotation.UserContext;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.*;

import models.Image;

import play.Logger;
import play.data.DynamicForm;
import play.libs.Akka;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.mvc.*;
import play.mvc.Http.RequestBody;
import play.twirl.api.Html;
import scala.concurrent.duration.Duration;
import services.ActivityService;
import services.TicketService;
import services.TicketTypeService;
import utilities.BuiltInArticles;
import utilities.Payment;
import utilities.QRCodeUtil;
import views.html.*;

import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;

import com.fasterxml.jackson.databind.JsonNode;

@With({ControllerHandler.class, Authenticated.class})
public class AuthenticatedAction extends Controller {

	//WEBPAGE DISPLAY: tickets list page
	public static Result ticketsUI() {

		try{
			List<Ticket> ticket_list = TicketService.findPaidTicketsByEmail(UserContext.GetCurrentUser().email);

			// Generate QR code when first use
			String server_url = Setting.GetConfig("server_url", "http://192.168.1.100:9000");

			//Java 8 featured coding style, with collection class, stream its elements with filter and iterator
			ticket_list.stream().filter(ticket -> ticket != null && ticket.qrcode == null).forEach(ticket -> {
				try {
					ticket.qrcode = QRCodeUtil.encodeBase64(server_url + "/verifyTicket?id=" + ticket.id, QRCodeUtil.GetIconImage("public/images/logo_rect.png", true));
					TicketService.doCreate(ticket);
				} catch (Exception e) {
					Logger.warn("Failed to update ticket QRCode: " + ticket.id);
				}
			});
			return ok(tickets.render("我的票夹", ticket_list));
		} catch(Exception e) {
          	Logger.error("Read ticket failed: "+ e.getLocalizedMessage());
          	String shtml = "读取用户票据列表出错！";
          	return ok(info.render("读取用户票据列表出错！", Html.apply(shtml)));
        }
	}



	//to create a ticket
	public static Result createTicket(String activity_id, String type_name, String quantity, String token) {

		User user = UserContext.GetCurrentUser();

		DynamicForm activityForm = form().bindFromRequest();

		if (activityForm.hasErrors()) {
			Logger.warn("ticket creation failed");
			ObjectNode result = Json.newObject();
			result.put("success", false);
			result.put("message", "表单有误");
			return ok(result);
		} else {

			Activity a = ActivityService.findOneById(activity_id);

			TicketType tt = TicketTypeService.findOneByActivityIdAndTypeName(activity_id, type_name);

			if (tt != null && Integer.valueOf(quantity) > 0) {

				String genTicketResult = CreateTickets(user, Integer.valueOf(quantity), a, tt, token);
				if(genTicketResult != null) {
					return badRequest(genTicketResult);

				}
			}

			ObjectNode result = Json.newObject();
			result.put("success", true);

			return ok(result);
		}
	}

	private static String CreateTickets(User user, int quantity, Activity a, TicketType tt, String paymentToken) {

		String bundleId = UUID.randomUUID().toString();

		Logger.debug("Creating {} ticket(s) for {} with bundle {}", quantity, user.email, bundleId);

		boolean isFreeTicket = tt.price == 0;
		if(isFreeTicket)
		{
			Logger.debug("Free ticket does not require payment gateway, will create ticket directly.");

			// verify reCAPTCHA instead
			String request = "secret=" + "6LfYIAkTAAAAANOnqqqme-mJyzU9qDlIoYuSh0gl" + "&response=" + paymentToken;
			F.Promise<JsonNode> jsonPromise = WS.url("https://www.google.com/recaptcha/api/siteverify")
					.setContentType("application/x-www-form-urlencoded; charset=utf-8")
					.post(request).map(response -> {
						Logger.debug("verify reCAPTCHA got return message: {}", response.getBody());
						return response.asJson();
					});
			JsonNode result = jsonPromise.get(1000);

			if(!result.get("success").asBoolean()){
				// reCAPTCHA not verified
				return "验证码错误";
			}
		}


		ArrayList<Ticket> tickets = Activity.createTickets(a.id, tt, user, quantity, bundleId, isFreeTicket);
		if (tickets == null) {
            return "出票失败";
        }

		boolean success = isFreeTicket; // We don't need to process payment if it is free ticket

		if(!success) {
			if (Payment.ProcessPayment(paymentToken, tt, user, quantity)) {
				for (int retry = 0; retry < 10; retry++) {
					// It is important to mark it as paid after payment successful and it is common to see SQL deadlock, let's retry if failed.

					try {
						for (Ticket ticket : tickets) {
							Ebean.createUpdate(Ticket.class, "update ticket set paid = true where id = :id and paid = false")
									.setParameter("id", ticket.id).execute();
						}
						Logger.debug("All {} ticket(s) are marked as paid for {} with bundle {}", quantity, user.email, bundleId);

						success = true;
						break;
					} catch (Exception e) {
						Logger.error("Failed to mark ticket as paid for user {} with bundle {}, retry {}", user.email, bundleId, retry, e);
						try {
							Thread.sleep(500); // Sleep and retry
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			} else {

				Logger.debug("Returning {} ticket(s) back for {} with bundle {}", quantity, user.email, bundleId);
				for (int retry = 0; retry < 10; retry++) { // It is important to mark it as paid, let retry if failed.

					Ebean.beginTransaction();
					try {
						Ebean.createUpdate(TicketType.class, "update ticket_type set booked = booked - :count where id = :id")
								.setParameter("id", tt.id)
								.setParameter("count", quantity).execute();
						Ebean.createUpdate(Ticket.class, "update ticket set payment_failure_handled = true where bundle_id = :id")
								.setParameter("id", bundleId).execute();
						Ebean.commitTransaction();

						return "支付失败";
					} catch (Exception e) {
						Ebean.rollbackTransaction();
						Logger.error("Failed to add ticket remain back for user {} with bundle {}, retry {} ", user.email, bundleId, retry, e);
					} finally {
						Ebean.endTransaction();
					}

				}


				return "支付失败";
			}
		}


		if(success) {
			String host = request().host();

			// Run email notice in background
			Akka.system().scheduler().scheduleOnce(
					Duration.create(0, TimeUnit.SECONDS),
					() -> {
						if (a.IsEmailToBuyerEnabled()) {
							try {
								Logger.info("Sending email notification to user " + user.email);
								SendEmail(tickets, tt, user, host);
							} catch (Exception e) {
								Logger.error("Failed to send email for user {}", user.email, e);
							}
						}
						if (a.IsEmailToOrganizerEnabled()) {
							try {
								Logger.info("Sending email notification to organizer ");
								SendEmailNotificationToOrganizer(tt, tickets.size(), user.email, host);
							} catch (Exception e) {
								Logger.error("Failed to send email for organizer", e);
							}
						}
					},
					Akka.system().dispatcher()
			);

		}

		return success ? null : "购买失败,请联系管理员处理,并告知处理编号：" + bundleId;
	}

	public static void SendEmailNotificationToOrganizer(TicketType type, int quantity, String buyer, String websiteHost){

		if(quantity == 0)
		{
			return;
		}

		String organizerEmail = type.activity.organizer;

		if(organizerEmail == null){
			Logger.warn("Organizer is unknown");
			return;
		}

		if(!organizerEmail.contains("@"))
		{
			// Invalid email name
			Logger.warn("Organizer email does not contain '@' {}", organizerEmail);
			return;
		}

		User organizer = User.find.byId(organizerEmail);
		if(organizer == null){
			Logger.warn("Organizer is unknown");
			return;
		}


		Article template = BuiltInArticles.GetOrganizerTemplate();

		String userName =  organizer.name != null && !organizer.name.equals("") ? organizer.name : organizer.email.split("@")[0];
		final Email email = new Email();
		email.setSubject(template.title.replaceAll("\\{activity_name\\}", type.activity.name));
		email.setFrom("WeTicket <notify@wechatgogo.com>");
		email.addTo(userName + "<" + organizer.email + ">");

		String redirectUrl = "http://" + websiteHost + "/redirect?token=" +
				play.api.libs.Crypto.encryptAES(organizer.email + "|quick-auth|" + new Date())
				+ "&redirectUrl=" + play.utils.UriEncoding.encodePathSegment("/ticket_manager?id=" + type.activity.id, "UTF-8");

		//String content = String.format("<html><body><p>尊敬的%s,</p><p></p><p>恭喜收到以下活动门票的订购:</p><p>    </p><p>活动：%s</p><p>类型：%s ($%s)</p><p>票数：%s</p><p>用户：%s</p>",
		//		userName, type.activity.name, type.type_name, type.price, quantity, buyer);
		//content += "<p>您可以登录 <a href='" + redirectUrl +"'>票务管理</a> 管理所有已购买的门票</p>";
		//content += "</body></html>";
		String content = "<html><body>" + template.content
				.replaceAll("\\{user\\}", userName)
				.replaceAll("\\{activity\\}", type.activity.name)
				.replaceAll("\\{type\\}", type.type_name)
				.replaceAll("\\{quantity\\}", (quantity+""))
				.replaceAll("\\{buyer\\}", buyer)
				.replaceAll("\\{price\\}", ("\\$"+type.price))
				.replaceAll("\\{link\\}", ("<a href='" + redirectUrl +"'>票务管理</a>")) + "</body></html>";

		email.setBodyHtml(content);
		String id = MailerPlugin.send(email);
	}

	public static void SendEmail(List<Ticket> tickets, TicketType ticketType, User user, String websiteHost)
	{
		if(tickets.size() == 0)
		{
			return;
		}

		if(!user.email.contains("@"))
		{
			// Invalid email name
			Logger.debug("User email does not contain '@' {}", user.email);
			return;
		}

		String organizerEmail = ticketType.activity.organizer;


		User organizer = User.find.byId(organizerEmail);
		if(organizer == null){
			Logger.warn("Organizer is unknown");
			return;
		}

		Article template = ticketType.activity.GetEmailTemplate();
		String organizerName =  organizer.name != null && !organizer.name.equals("") ? organizer.name : organizer.email.split("@")[0];
		String organizerPhone = organizer.tel;
		String userName =  user.name != null && !user.name.equals("") ? user.name : user.email.split("@")[0];
		String server_url = "http://" + websiteHost;


		String content = template.content;
		String ticketGroupPattern = "\\{tickets_begin\\}(.+)\\{tickets_end\\}";
		Matcher matcher = Pattern.compile(ticketGroupPattern).matcher(content);

		if (matcher.find())
		{
			Logger.debug("Got tickets template " + matcher.group(1));
			String ticketGroupTemplate = matcher.group(1);
			String ticketGroup = "";

			//play-mailer does not support cid, we cannot insert an image here, so instead, we use image link to our website
			for(Ticket t : tickets)
			{
				if(t != null)
				{
					try {


						Image img = new Image("image/jpg",
								QRCodeUtil.encodeImage(server_url + "/verifyTicket?id=" + t.id, QRCodeUtil.GetIconImage("public/images/logo_rect.png", true)));
						img.save();
						ticketGroup += ticketGroupTemplate
								.replaceAll("\\{ticket_qr_code\\}",server_url + "/image/" + img.id)
								.replaceAll("\\{ticket_number\\}", t.id)
								.replaceAll("\\{ticket_verify_link\\}", server_url + "/verifyTicket?id=" + t.id);


					} catch (Exception e) {
						Logger.warn("Failed to generate ticket QRCode: " + t.id);
					}
				}
			}

			content = content.replaceAll("\\{tickets_begin\\}.+\\{tickets_end\\}", ticketGroup);

		}



		final Email email = new Email();
		email.setSubject(template.title.replaceAll("\\{activity_name\\}", tickets.get(0).activity.name));
		email.setFrom("WeTicket <notify@wechatgogo.com>");
		email.addTo(userName + "<" + user.email + ">");

		content = "<html><body>" + content
				.replaceAll("\\{user\\}", userName)
				.replaceAll("\\{organizer\\}", organizerName)
				.replaceAll("\\{organizer_email\\}", organizerEmail)
				.replaceAll("\\{organizer_tel\\}", organizerPhone)
				.replaceAll("\\{date\\}", tickets.get(0).activity.GetLocalDate(new Date()))
				.replaceAll("\\{activity\\}", tickets.get(0).activity.name)
				.replaceAll("\\{activity_link\\}", server_url + "/activity/" + tickets.get(0).activity.id)
				.replaceAll("\\{type\\}", ticketType.type_name)
				.replaceAll("\\{quantity\\}", tickets.size()+"")
				.replaceAll("\\{price\\}", "\\$" + ticketType.price)
				.replaceAll("\\{link\\}", ("<a href='" + server_url + "/tickets'>我的门票<a>")) + "</body></html>";


		email.setBodyHtml(content);
		String id = MailerPlugin.send(email);

	}

	//purchase ticket success page
	public static Result purchaseSuccessUI() {
		String shtml =   "<center><h1>购票成功</h1></center><br>";
		shtml += "<a class='btn btn-lg btn-info btn-block' href='/tickets' role='button'>查看我的票夹</a><br>";
		//shtml += "<div class='row'><div class='col-sm-offset-4 col-sm-4'><img src='" + routes.Assets.at("images/logo_rect.png") + "' class='img-responsive'></div></div><br>";
		return ok(info.render("购票成功", Html.apply(shtml)));
	}
	

	//to verify a ticket is valid or not
	public static Result verifyTicketUI() {

		User user = UserContext.GetCurrentUser();

		DynamicForm ticketForm = form().bindFromRequest();
		String ticket_id = ticketForm.get("id");
		try{
			Ticket t = Ticket.find.byId(ticket_id);
		
			if (t != null && t.paid) {
				if (t.checked == false) {
					String shtml = "" + t.title + " - 票据有效！<br><br>";
					shtml += "时间： " + t.date_time + "<br>";
					shtml += "类型： " + t.type_name + "($ "+t.price+")<br>";
					shtml += "持有者： " + t.assigned_to.email + "<br><br>";
					shtml += "";

					if(t.IsOrganizedBy(user)) {
						shtml += "<center><a class='btn btn-lg btn-danger' href='/cutTicket?id=" + ticket_id + "' role='button'>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp检票&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</a></center>";
					}
	
					return ok(info.render("查票成功", Html.apply(shtml)));
				} else {
					String shtml = "" + t.title + " - 该票已失效！<br><br>";
					shtml += "类型： " + t.type_name + "($ "+t.price+")<br>";
					shtml += "检票时间： " + t.checked_time + "<br>";
					shtml += "检票员： " + t.checked_by + "<br><br>";
					return ok(info.render("查票成功", Html.apply(shtml)));
				}
	
			} else {
				String shtml = "Ticket verification failed";
				return ok(info.render("查票失败", Html.apply(shtml)));
			}
		} catch (Exception e) {
			Logger.error("Verify ticket failed: {}", e);
			String shtml = "查票出错！";
			return ok(info.render("查票出错！", Html.apply(shtml)));
		}
		
	}
	
	
	
	//WEBPAGE DISPAY: update a user password
	public static Result getChangePasswordUI() {
		try{

			User user = UserContext.GetCurrentUser();
			return ok(update_password.render("修改密码", user));

		} catch(Exception e) {
			Logger.error("Unexpected error occurred when getting change password UI?!.", e);
			String shtml = "未知错误";
			return badRequest(info.render("非法操作", Html.apply(shtml)));
		}
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result changePassword() {
		try {
			RequestBody body = request().body();
			String password = body.asJson().findPath("newPassword").textValue();
			String oldPassword = body.asJson().findPath("oldPassword").textValue();

			password = play.libs.Crypto.sign(password);
			oldPassword = play.libs.Crypto.sign(oldPassword);

			User u = UserContext.GetCurrentUser();
			if(!u.password.equals(oldPassword)){
				return unauthorized("Failed to update password");
			}

			u.password = password;
			u.save();

			ObjectNode result = Json.newObject();
			result.put("success", "true");
			return ok(result);


		} catch (Exception e) {
			Logger.error("update password failed", e);
			ObjectNode result = Json.newObject();
			result.put("error", "update password failed.");
			return badRequest(result);
		}
	}

	// to display a user's information
	public static Result viewUserInfoUI(String user_email){
		boolean editable = false;
		User user = User.find.byId(user_email);
		if (user != null) {
			return ok(register.render("编辑用户", user, editable, false));
		} else {
			String shtml = "用户不存在";
			return ok(info.render("非法操作", Html.apply(shtml)));
		}
	}
				
				
	// to get user information			
	public static Result getUserInfo() {

		try {
			// User could only get info of himself
			User u = UserContext.GetCurrentUser();
			return ok(u.toJsonObject());
		} catch (Exception e) {
			Logger.error("Failed to get user info", e);
			return badRequest("failed");
		}

	}
	
	// to update a user
	public static Result updateUserInfo() {
		try {
			JsonNode update_field = request().body().asJson();
			String name = update_field.findPath("name").textValue();
			String tel = update_field.findPath("tel").textValue();
			String gender = update_field.findPath("gender").textValue();
			String age = update_field.findPath("age").textValue();
			String address = update_field.findPath("address").textValue();
			/*
			DynamicForm loginForm = form().bindFromRequest();
			String name = loginForm.get("name");
			String tel = loginForm.get("tel");
			String gender = loginForm.get("gender");
			String age = loginForm.get("age");
			String address = loginForm.get("address");
*/
			User u = UserContext.GetCurrentUser();
			Logger.info("update email: " + u.email + "name: " + name + "tel: " + tel + " gender: " + gender + " age: " + age + " address: " + address);

			try {
				if (name != null && name != "") {
					u.name = name;
				}
				if (tel != null && tel != "") {
					u.tel = tel;
				}
				if (gender != null && gender != "") {
					u.gender = gender;
				}
				if (age != null && age != "0" & age != "") {
					u.age = Integer.valueOf(age);
				}
				if (address != null && address != "") {
					u.address = address;
				}
				u.update();
				return ok("true");
			} catch (Exception e) {
				// Make sure we always have log to check
				Logger.error("Failed to udpate user {}", u.email, e);
				return badRequest("failed");
			}

		} catch (Exception e) {
			Logger.error("Failed to update user", e);
			return badRequest("failed");
		}
	}

	public static Result publishActivity(String id){
		try {
			User user = UserContext.GetCurrentUser();
			Activity activity = ActivityService.findOneById(id);
			if (activity != null){
				if(user.email.equals(activity.organizer)) {
					activity.status = Activity.STATUS_REQUEST_PUBLISH;
					ActivityService.doUpdate(activity);
				}
				else if(user.IsAdministrator()){
					activity.status = Activity.STATUS_PUBLISH;
					ActivityService.doUpdate(activity);
				}
				else
				{
					return forbidden("No permission to perform the action.");
				}
				ObjectNode result = Json.newObject();
				result.put("success", true);
				return ok(result);
			} else {
				return badRequest("Invalid activity id");
			}

		} catch (Exception e) {
			Logger.error("Unexpected error occured when requesting publish activity", e);
			return badRequest("Unexpected error occured when requesting publish activity");
		}
	}

	// WEBPAGE DISPAY: create activity
	public static Result createUI(String id) {
		try {

			List<City> cities = City.find.all();
			User user = UserContext.GetCurrentUser();

			if (id.equals("new")) {
				System.out.println("creating new activity");
				if(user.IsAdministrator()) {
					return ok(create.render("创建活动", null, null, null, cities, ActivityFolderEnum.values(), user));
				}else{
					System.out.println("welcome general user");
					Article createActivityTerm = BuiltInArticles.GetCreateActivityTerm();
					return ok(create.render("创建活动", null, createActivityTerm, null, cities, ActivityFolderEnum.values(), user));
				}
			} else {
				Activity activity = ActivityService.findOneById(id);
				if (activity != null
						&& (user.IsAdministrator() || user.email
						.equals(activity.organizer))) {

					List<TicketType> ticket_type_list = TicketTypeService.findAllActiveTicketTypesByActivityId(activity.id);

					return ok(create.render("编辑活动", activity, null, ticket_type_list, cities, ActivityFolderEnum.values(), user));
				} else {
					String shtml = "活动不存在";
					return ok(info.render("非法操作", Html.apply(shtml)));
				}
			}
		} catch (Exception e) {
			Logger.error("Create or updated activity failed: ", e);
			String shtml = "创建或更新活动出错！";
			return ok(info.render("创建或更新活动出错！", Html.apply(shtml)));
		}
	}

	// FUNCTION: to create or update a activity
	public static Result createActivityUI() {
		try {
			DynamicForm activityForm = form().bindFromRequest();
			if (activityForm.hasErrors()) {
				System.out.println("活动编辑错误");
				String shtml = "活动编辑错误";
				return badRequest("活动编辑错误");
			} else {

				User user = UserContext.GetCurrentUser();

				String id = activityForm.get("id");
				String name = activityForm.get("name");
				String folder = activityForm.get("folder");
				String brief_description = activityForm
						.get("brief_description");
				String description = activityForm.get("description");
				String status = user.IsAdministrator() ? activityForm.get("status") : Activity.STATUS_UNPUBLISH;
				String date_time = activityForm.get("datetime");
				String city = activityForm.get("venue_city");
				String country = activityForm.get("venue_country");
				String time_zone = City.GetTimeZone(city, country, "US/Pacific");

				Logger.debug(TimeZone.getTimeZone(time_zone).toString());

				String start_datetime_string = activityForm.get("start_datetime");
				String end_datetime_string = activityForm.get("end_datetime");
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				df.setTimeZone(TimeZone.getTimeZone(time_zone));
				Date start_date_time = df.parse(start_datetime_string);
				Date end_date_time = df.parse(end_datetime_string);

				String organizer = activityForm.get("organizer");
				String organizer_phone = activityForm.get("organizer-phone");
				String organizer_name = activityForm.get("organizer-name");

				boolean bname = Boolean.valueOf(activityForm.get("bname"));
				boolean btel = Boolean.valueOf(activityForm.get("btel"));
				boolean bgender = Boolean.valueOf(activityForm.get("bgender"));
				boolean bage = Boolean.valueOf(activityForm.get("bage"));
				boolean baddress = Boolean
						.valueOf(activityForm.get("baddress"));



				String descriptionImage = GetBase64FileFromPost("description_image_compressed");
				String image = GetBase64FileFromPost("activity_image_compressed");
				String venue = GetVenueString(activityForm);

				//
				// create a new activity
				if (id == null || id.equals("")) {

					Activity activity = new Activity();
					activity.name = name;
					activity.folder = folder;
					activity.image = image;
					activity.brief_description = brief_description;
					activity.description = description;
					activity.description_image = descriptionImage;
					activity.status = status;
					activity.date_time = date_time;
					activity.start_date_time = start_date_time;
					activity.end_date_time = end_date_time;
					activity.time_zone = time_zone;
					activity.venue = venue;
					activity.organizer = organizer;
					activity.bname = bname;
					activity.btel = btel;
					activity.bgender = bgender;
					activity.bage = bage;
					activity.baddress = baddress;

					ActivityService.doCreate(activity);

					if(!user.IsOrganizer() && !user.IsAdministrator()) {
						user.role = "org";
						user.save();
					}

					if(organizer_phone != null){
						user.tel = organizer_phone;
						user.save();
					}

					if(organizer_name != null){
						user.name = organizer_name;
						user.save();
					}

					Result r = GetNewTicketTypeFromPost(activity, user, activityForm, "new");
					if(r != null) return r;

					Logger.debug("活动 {} 创建成功", name);
					ObjectNode result = Json.newObject();
					result.put("success", true);
					String location = "/activity/"+activity.id+"?preview=true";
					result.put("redirect", location);
					return ok(result);
				}
				// update a activity
				else {
					Activity activity = ActivityService.findOneById(id);

					if (image == null)
						image = activity.image;

					if (descriptionImage == null)
						descriptionImage = activity.description_image;

					if (activity != null
							&& (user.IsAdministrator() || user.email
							.equals(activity.organizer))) {

						activity.name = name;
						activity.folder = folder;
						activity.image = image;
						activity.brief_description = brief_description;
						activity.description = description;
						activity.description_image = descriptionImage;
						activity.status = status;
						activity.date_time = date_time;
						activity.start_date_time = start_date_time;
						activity.end_date_time = end_date_time;
						activity.time_zone = time_zone;
						activity.venue = venue;
						activity.organizer = organizer;
						activity.bname = bname;
						activity.btel = btel;
						activity.bgender = bgender;
						activity.bage = bage;
						activity.baddress = baddress;

						ActivityService.doUpdate(activity);

						if(organizer_phone != null){
							user.tel = organizer_phone;
							user.save();
						}

						if(organizer_name != null){
							user.name = organizer_name;
							user.save();
						}


						Result r_new = GetNewTicketTypeFromPost(activity, user, activityForm, "new");
						if(r_new != null) return r_new;

						Result r_update = GetNewTicketTypeFromPost(activity, user, activityForm, "update");
						if(r_update != null) return r_update;

						Result r_delete = GetNewTicketTypeFromPost(activity, user, activityForm, "delete");
						if(r_delete != null) return r_delete;

						String shtml = "活动更新成功";
						ObjectNode result = Json.newObject();
						result.put("success", true);
						return ok(result);
					} else {
						System.out.println("找不到该活动ID");
						return badRequest("找不到该活动ID");

					}
				}
			}
		} catch (Exception e) {
			Logger.error("Create activity failed: ", e);
			String shtml = "更新或者创建活动出错！";
			return ok(info.render("更新或者创建活动出错！", Html.apply(shtml)));
		}
	}

	private static String GetVenueString(DynamicForm activityForm){
		String venueName = activityForm.get("venue_name");
		String venueStreetAddress = activityForm.get("venue_street_address");
		String venuePostalCode = activityForm.get("venue_postal_code");
		String venueCity = activityForm.get("venue_city");
		String venueCountry = activityForm.get("venue_country");


		ObjectNode result = Json.newObject();
		if(venueName != null) {
			result.put("venue_name", venueName);
		}
		if(venueStreetAddress != null){
			result.put("venue_street_address", venueStreetAddress);
		}
		if(venuePostalCode != null){
			result.put("venue_postal_code", venuePostalCode);
		}
		if(venueCity != null){
			result.put("venue_city", venueCity);
		}
		if(venueCountry != null){
			result.put("venue_country", venueCountry);
		}

		return result.toString();
	}

	private static Result GetNewTicketTypeFromPost(Activity activity, User user, DynamicForm activityForm, String key)
			throws Exception {

		if(activityForm.get(key + "_count") != null && activityForm.get(key + "_count").trim() != ""){

			Integer key_count = Integer.valueOf(activityForm.get(key + "_count"));

			if(key.trim().equals("new")){
				return addTicektType(key_count, activity, activityForm);
			}

			if(key.trim().equals("update")){
				return editTicektType(user, key_count, activityForm);
			}

			if(key.trim().equals("delete")){
				return deleteTicektType(user, key_count, activityForm);
			}
		}
		return null;
	}

	private static Result addTicektType(Integer count, Activity activity, DynamicForm activityForm){
		for(int i = 0; i< count; i++){
			String ticket_type_name = activityForm.get("new_" + i + "_type_name");
			String ticket_type_price = activityForm.get("new_" + i + "_price");
			String ticket_type_quantity = activityForm.get("new_" + i + "_quantity");
			String ticket_type_include_tax = activityForm.get("new_" + i + "_include_tax");

			if(activity != null) {
				TicketType ticketType = new TicketType();
				ticketType.activity = activity;
				ticketType.type_name = ticket_type_name;
				ticketType.price = Double.valueOf(ticket_type_price);
				ticketType.quantity = Integer.valueOf(ticket_type_quantity);
				ticketType.taxIncluded = Boolean.valueOf(ticket_type_include_tax);
				ticketType.currency = "CAD";

				try {
					TicketTypeService.doCreate(ticketType);
				}catch(Exception e) {
					return badRequest("添加票务类型出错");
				}
			}

		}
		return null;
	}

	private static Result editTicektType(User user, Integer count, DynamicForm activityForm){
		for(int i = 0; i< count; i++){
			String ticket_type_id = activityForm.get("update_" + i + "_id");
			String ticket_type_name = activityForm.get("update_" + i + "_type_name");
			String ticket_type_price = activityForm.get("update_" + i + "_price");
			String ticket_type_quantity = activityForm.get("update_" + i + "_quantity");
			String ticket_type_include_tax = activityForm.get("update_" + i + "_include_tax");

			if(ticket_type_id != null && ticket_type_id != ""){
				TicketType ticketType = TicketTypeService.find.byId(ticket_type_id);
				if(ticketType == null) {
					return badRequest("票务类型不存在");
				}else{
					if (!ticketType.IsOrganizedBy(user)) {
						return unauthorized("无权操作该票务类型");
					}
					ticketType.type_name = ticket_type_name;
					ticketType.quantity = Integer.valueOf(ticket_type_quantity);
					ticketType.price = Double.valueOf(ticket_type_price);
					ticketType.taxIncluded = Boolean.valueOf(ticket_type_include_tax);

					try {
						TicketTypeService.doUpdate(ticketType);
					}catch(Exception e){
						return badRequest("更新票务类型出错");
					}
				}
			}else return badRequest("票务类型不存在, ID 不能为空");
		}
		return null;
	}

	private static Result deleteTicektType(User user, Integer count, DynamicForm activityForm){
		for(int i = 0; i< count; i++){
			String ticket_type_id = activityForm.get("delete_" + i + "_id");

			if(ticket_type_id != null && ticket_type_id != ""){
				TicketType ticketType = TicketTypeService.find.byId(ticket_type_id);
				if(ticketType == null) {
					return badRequest("票务类型不存在");
				}else{
					if (!ticketType.IsOrganizedBy(user)) {
						return unauthorized("无权操作该票务类型");
					}

					try {
						TicketTypeService.doDelete(ticketType);
					}catch(Exception e){
						return badRequest("删除票务类型出错");
					}
				}
			}else return badRequest("票务类型不存在, ID 不能为空");
		}
		return null;
	}

	private static String GetBase64FileFromPost(String keyName)
			throws Exception {

		Http.MultipartFormData body = request().body().asMultipartFormData();
		Http.MultipartFormData.FilePart file = body.getFile(keyName);

		if (file != null) {
			File fileObj = file.getFile();
			// Put some limit on file to avoid saving too big file in DB
			if (fileObj.length() > 2 * 1024 * 1024) {
				throw new Exception("Image too large.");
			}

			FileInputStream fin = new FileInputStream(fileObj);
			byte fileContent[] = new byte[(int) fileObj.length()];
			fin.read(fileContent);
			return "data:" + file.getContentType() + ";base64,"
					+ com.ning.http.util.Base64.encode(fileContent);
		} else {
			return null;
		}
	}
}


