package controllers;

import static play.data.Form.form;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import annotation.ControllerHandler;
import annotation.UserContext;

import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.*;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Akka;
import play.libs.Json;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.*;
import play.twirl.api.Html;
import scala.concurrent.duration.Duration;
import services.TicketTypeService;
import utilities.BuiltInArticles;
import views.html.*;

@With(ControllerHandler.class)
public class GeneralAction extends Controller {

	// index: activity list page
	public static Result indexUI() {
		try {
			List<Activity> activity_list = Activity.find.where().or(Expr.eq("status", "publish"),
					Expr.eq("status", "expired")
					).eq("deleted", false)
					.orderBy("star desc, start_date_time desc").findList();
			return ok(index.render("微票网", activity_list));
		} catch (Exception e) {
			Logger.error("Index activity list failed: " + e.getLocalizedMessage());
			String shtml = "读取首页活动列表出错！";
			return ok(info.render("读取首页活动列表出错！", Html.apply(shtml)));
		}
	}

	// detail of a activity page
	public static Result getActivityUI(String id, String preview) {
		try {
			Activity theActivity = Activity.find.where().eq("id", id).findUnique();

			if (theActivity != null) {
				if(preview.trim().equals("true")){
					List<TicketType> theTicketType = TicketTypeService.findAllActiveTicketTypesByActivityId(theActivity.id);
					Article terms = BuiltInArticles.GetTerms();
					return ok(activity.render("活动详情", theActivity, terms, theTicketType, "preview"));
				}else {
					if (theActivity.status.equalsIgnoreCase("unpublish")) {
						String shtml = "活动未发布";
						return ok(info.render("活动未发布", Html.apply(shtml)));
					}
					List<TicketType> theTicketType = TicketTypeService.findAllActiveTicketTypesByActivityId(theActivity.id);
					String paymentApiKey = Setting.GetConfig("stripe_api_key_client", "pk_test_fY4ssnR42Qz7gZU9Ehbq6aDO");
					Article terms = BuiltInArticles.GetTerms();
					return ok(activity.render("活动详情", theActivity, terms, theTicketType, paymentApiKey));
				}
			} else {
				String shtml = "活动读取错误";
				return ok(info.render("活动读取错误", Html.apply(shtml)));
			}
		} catch (Exception e) {
			Logger.error("Read activity details failed. ", e);
			String shtml = "读取活动细节出错！";
			return ok(info.render("读取活动细节出错！", Html.apply(shtml)));
		}
	}

	// about page
	public static Result aboutUI() {
		// String shtml = "<div class='row
		// col-sm-12'><center><h1>微票网</h1></center></div><br>";
		Article notice = BuiltInArticles.GetNotice();

		String title = notice.title;
		String content = notice.content;

		return ok(info.render(title, Html.apply(content)));
	}

	// clause page
	public static Result clauseUI() {
		String shtml = "<div class='row col-sm-12'><center><h1>微票网服务条款</h1></center></div><br>";
		shtml += "<div class='row col-sm-12'>灵感科技保留一切活动的解释权。</div><br>";
		return ok(info.render("购票须知", Html.apply(shtml)));
	}

	// to logout
	public static Result logout() {
		UserContext.SetCurrentUser(null);
		ObjectNode result = Json.newObject();
		result.put("success", "true");
		return ok(result);
	}

	public static Result resendUserVerificationEmail(String email){
		User u = User.find.where().eq("email", email).findUnique();
		if(u != null){
			SendUserVerificationEmail(u, null, false);
			String shtml = "邮件已发送";
			return ok(info.render("邮件已发送", Html.apply(shtml)));
		}else{
			String shtml = "该用户不存在";
			return ok(info.render("该用户不存在", Html.apply(shtml)));
		}
	}

	public static void SendUserConfirmEmail(User user, String websiteHost, String initialPwd, Boolean createdByAdmin) {

		if (!user.email.contains("@")) {
			// Invalid email name
			Logger.warn("Email does not contain '@' {}", user.email);
			return;
		}

		Article template = createdByAdmin ? BuiltInArticles.GetAdminGeneratedUserConfirmTemplate()
				: BuiltInArticles.GetUserConfirmTemplate();
		String userName = user.name != null && !user.name.equals("") ? user.name : user.email.split("@")[0];
		final Email email = new Email();
		email.setSubject(template.title);
		email.setFrom("WeTicket <notify@wechatgogo.com>");
		email.addTo(userName + "<" + user.email + ">");

		String linkUrl = "http://" + websiteHost + "/verify_account?token="
				+ play.api.libs.Crypto.encryptAES(user.email + "|account-verify|" + new Date());

		String content = "<html><body>" +
				template.content
						.replaceAll("\\{user\\}", userName)
						.replaceAll("\\{link\\}", ("<a href='" + linkUrl + "'>" + "马上验证"+ "</a>"))
						.replaceAll("\\{link_url\\}",linkUrl)
				+ "</body></html>";

		if (createdByAdmin) {
			content = content.replaceAll("\\{pwdinfo\\}", initialPwd);
		}

		email.setBodyHtml(content);
		String id = MailerPlugin.send(email);
	}

	private static void SendUserVerificationEmail(User user, String initialPwd, Boolean createdByAdmin) {
		String host = request().host();

		// Run email notice in background
		Akka.system().scheduler().scheduleOnce(Duration.create(0, TimeUnit.SECONDS), () -> {
			try {
				Logger.info("Sending email notification to user " + user.email);
				SendUserConfirmEmail(user, host, initialPwd, createdByAdmin);
			} catch (Exception e) {
				Logger.error("Failed to send email for user {}", user.email, e);
			}
		}, Akka.system().dispatcher());
	}

	private static String encryptPassword(String password) {
		String sha1 = "";
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(password.getBytes("UTF-8"));
			sha1 = byteToHex(crypt.digest());
		} catch (NoSuchAlgorithmException e) {
			Logger.error("Failed to generate sha-1 NoSuchAlgorithmException", e);
		} catch (UnsupportedEncodingException e) {
			Logger.error("Failed to generate sha-1 UnsupportedEncodingException", e);
		}
		return sha1;
	}

	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	// to register a user
	@BodyParser.Of(BodyParser.Json.class)
	public static Result register() {
		try {

			Http.RequestBody body = request().body();
			String email = body.asJson().findPath("email").textValue().toLowerCase();
			String name = body.asJson().findPath("name").textValue();
			String role = body.asJson().findPath("role").textValue();
			String tel = body.asJson().findPath("tel").textValue();
			String gender = body.asJson().findPath("gender").textValue();
			String age = body.asJson().findPath("age").textValue();
			String address = body.asJson().findPath("address").textValue();
			int ageInt = 0;

			if (name == null) {
				name = "";
			}
			if (tel == null) {
				tel = "";
			}
			if (gender == null) {
				gender = "";
			}
			if (age == null || age.equals("")) {
				age = "0";
			}
			try {
				ageInt = Integer.parseInt(age);
			} catch (Exception e) {
				Logger.warn("年龄输入有误");
				return badRequest("年龄输入有误");
			}
			if (address == null) {
				address = "";
			}

			User u = User.find.where().eq("email", email).findUnique();
			// To create new user
			if (u == null) {
				// if role="", it shall be from common user registration
				Logger.warn(role);
				if (role == null) {
					String password = play.libs.Crypto.sign(body.asJson().findPath("password").textValue());

					User newUser = new User(email, name, password, "user", tel, gender, Integer.valueOf(age), address);
					SendUserVerificationEmail(newUser, null, false);
					Logger.info("成功注册用户： " + email);
					UserContext.SetCurrentUser(newUser);
					return ok(newUser.toJsonObject());
				}
				// else it is from a request of admin
				else {
					// check if you are admin
					User currentUser = UserContext.GetCurrentUser();
					if (currentUser == null) {
						Logger.warn("请先登录" + email);
						return unauthorized("false");
					} else if (!currentUser.role.equals("admin")) {
						Logger.warn("不是管理员请勿创建用户" + email);
						return unauthorized("false");
					} else {
						Random rand = new Random();
						String initialPwd = "p" + rand.nextInt(Integer.MAX_VALUE);
						Logger.debug("Initial pwd {} for {}", initialPwd, email);
						String password = play.libs.Crypto.sign(encryptPassword(initialPwd));
						User newUser = new User(email, name, password, role, tel, gender, ageInt, address);
						SendUserVerificationEmail(newUser, initialPwd, true);
						Logger.info("User created: {}", email);
						return ok(newUser.toJsonObject());
					}
				}
			}
			// to update existing user
			else {

				User currentUser = UserContext.GetCurrentUser();
				if (currentUser == null) {
					Logger.warn("请先登录" + email);
					return unauthorized("false");
				} else if (!currentUser.IsAdministrator()) {
					Logger.warn("不是管理员请勿更新用户" + email);
					return unauthorized("false");
				} else {
					u.name = name;
					u.role = role;
					u.tel = tel;
					u.gender = gender;
					u.age = Integer.valueOf(age);
					u.address = address;
					// u.password=password;
					u.save();
					Logger.info("Update user {} successful.", name);
					return ok(u.toJsonObject());
				}

			}
		} catch (Exception e) {
			Logger.error("Register / update user failed: ", e);
			return ok("注册或更新用户出错！");
		}
	}

	// to search if an email address is exist
	public static Result searchEmail(String email) {
		try {
			User u = User.find.where().eq("email", email).findUnique();
			if (u == null) {

				return unauthorized("user not found");

			} else if (u.role.equals("blocked")) {

				Logger.info("Blocked Email");
				return forbidden("user not found");

			} else {
				Logger.info("Existing email");

				return ok(u.toJsonObject());
			}
		} catch (Exception e) {
			Logger.error("Search email failed: " + e.getLocalizedMessage());
			String shtml = "搜索邮箱出错！";
			return badRequest(info.render("搜索邮箱出错！", Html.apply(shtml)));
		}
	}

	// to check if a user is an administrator
	public static Result checkRole() {
		try {
			User user = UserContext.GetCurrentUser();
			if (user != null) {
				ObjectNode result = Json.newObject();
				result.put("response", user.role);
				return ok(result);
			} else {
				ObjectNode result = Json.newObject();
				result.put("response", "none");
				return ok(result);
			}
		} catch (Exception e) {
			Logger.error("Check admin failed: {}", e);
			ObjectNode result = Json.newObject();
			result.put("response", "none");
			return ok(result);
		}
	}

	public static Result login(String user, String password) {

		try {
			User authenticatedUser = User.authenticate(user.toLowerCase(), play.libs.Crypto.sign(password));
			if ((authenticatedUser == null) || (authenticatedUser.role.equals("blocked"))) {
				UserContext.SetCurrentUser(null);
				Logger.info("User {} does not exist or blocked.", user);

				ObjectNode result = Json.newObject();
				result.put("error", "user not found or blocked");
				return notFound(result);
			} else {
				UserContext.SetCurrentUser(authenticatedUser);
				Logger.info("User {} login success", user.toLowerCase());
				return ok(authenticatedUser.toJsonObject());
			}
		} catch (Exception e) {
			Logger.error("Failed to authenticate user ", e);

			ObjectNode result = Json.newObject();
			result.put("error", "authentication failed.");
			return badRequest(result);
		}
	}

	private static User getUserByToken(String token, String tokenKeyword, int validHours) {

		String plainToken = "";
		try {
			plainToken = play.api.libs.Crypto.decryptAES(token);
		} catch (Exception e) {
			Logger.debug("Failed to decrypt token {}", token);
			return null;
		}

		Logger.debug("Got token {}", plainToken);
		String[] result = plainToken.split("\\|");

		if (result.length != 3 || !result[1].equals(tokenKeyword)) {
			return null;
		}

		try {
			// token valid for specified time
			DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
			Date tokenDate = df.parse(result[2]);
			Calendar c = Calendar.getInstance();
			c.setTime(tokenDate);
			c.add(Calendar.HOUR, validHours);
			if (c.getTime().before(new Date())) {
				Logger.debug("Token expired");
				return null;
			}
		} catch (Exception e) {
			Logger.debug("Failed to parse date {}", e);
			return null;
		}

		String email = result[0];
		return User.find.byId(email);
	}

	public static Result verifyUserEmail(String token) {
		try {
			User user = getUserByToken(token, "account-verify", 24);

			if (user != null) {
				user.verified = true;
				user.save();
				return ok(info.render("账户验证", Html.apply("账户已验证")));
			}

			return ok(info.render("账户验证", Html.apply("连接无效")));

		} catch (Exception e) {
			Logger.error("Unexpected error occurred in redirectWithAuthenticateToken {}", e);
			return ok(info.render("账户验证", Html.apply("连接无效")));
		}
	}

	public static Result redirectWithAuthenticateToken(String token, String redirectUrl) {
		try {
			User user = getUserByToken(token, "quick-auth", 24);

			if (user != null) {
				UserContext.SetCurrentUser(user);
			}

			return redirect(redirectUrl);

		} catch (Exception e) {
			Logger.error("Unexpected error occurred in redirectWithAuthenticateToken {}", e);
			return redirect(redirectUrl);
		}
	}

	public static Result getPasswordResetUI(String token) {
		try {
			User user = getUserByToken(token, "password-recovery", 24);

			if (user == null) {
				return badRequest("Invalid token");
			}

			return ok(password_reset.render("", user.email, token));

		} catch (Exception e) {
			Logger.error("Failed to get password reset UI {}", e);
			return badRequest(e.getMessage());
		}
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result passwordReset() {

		try {
			Http.RequestBody body = request().body();
			String token = body.asJson().findPath("token").textValue();
			String newPassword = body.asJson().findPath("password").textValue();

			User user = getUserByToken(token, "password-recovery", 24);

			if (user == null) {
				return badRequest("Invalid token");
			}

			user.password = play.libs.Crypto.sign(newPassword);
			user.save();

			ObjectNode result = Json.newObject();
			result.put("success", "true");
			return ok(result);

		} catch (Exception e) {
			Logger.error("Failed to get password reset UI {}", e);
			return badRequest(e.getMessage());
		}
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result sendPasswordResetRequest() {
		try {
			Http.RequestBody body = request().body();
			String email = body.asJson().findPath("email").textValue();

			User user = User.find.byId(email);
			if (user == null) {
				return notFound("无法找到该账号");
			}

			String host = request().host();

			// Run email notice in background
			Akka.system().scheduler().scheduleOnce(Duration.create(0, TimeUnit.SECONDS), () -> {
				try {
					Logger.info("Sending email  to " + user.email);
					String url = "http://" + host + "/passwordReset?token="
							+ play.api.libs.Crypto.encryptAES(user.email + "|password-recovery|" + new Date());
					final Email mail = new Email();
					mail.setSubject("[微票网] - 密码重置");
					mail.setFrom("WeTicket <notify@wechatgogo.com>");
					String userName = user.name != null && !user.name.equals("") ? user.name : user.email.split("@")[0];
					mail.addTo(userName + "<" + user.email + ">");

					Article template = BuiltInArticles.GetResetPasswordTemplate();
					String content = "<html><body>" + template.content.replaceAll("\\{user\\}", userName)
							.replaceAll("\\{link_url\\}", url);
					content += "</body></html>";

					mail.setBodyHtml(content);
					String id = MailerPlugin.send(mail);

				} catch (Exception e) {
					Logger.error("Failed to send email for user {}", user.email, e);
				}

			}, Akka.system().dispatcher());
			ObjectNode result = Json.newObject();
			result.put("success", "true");
			return ok(result);
		} catch (Exception e) {
			Logger.error("Failed to find password ", e);
			return badRequest("Failed");
		}
	}

	public static Result GetImageUI(String id) {

		try {
			Image img = Image.find.byId(id);
			if (img == null) {
				return notFound();
			}

			return ok(img.data).as(img.contentType);

		} catch (Exception e) {
			Logger.error("Failed to get image {}", e);
			return badRequest(e.getMessage());
		}
	}

	//WEBPAGE DISPAY: register a user
	public static Result registerUI() {

		boolean editable = true;

		User currentUser = UserContext.GetCurrentUser();

		try {
			DynamicForm userForm = form().bindFromRequest();
			String user_email = userForm.get("email");
			if (user_email == null) {
				if(currentUser == null){
					return ok(register.render("创建用户", null, editable, true));
				}else {
					return ok(register.render("创建用户", null, editable, false));
				}
			} else {
				User user = User.find.byId(user_email);
				if (user != null) {
					return ok(register.render("编辑用户", user, editable, false));
				} else {
					String shtml = "用户不存在";
					return ok(info.render("非法操作", Html.apply(shtml)));
				}
			}
		} catch (Exception e) {
			Logger.error("Getting register user page failed", e);
			String shtml = "注册用户页面出错！";
			return ok(info.render("注册用户页面出错！", Html.apply(shtml)));
		}
	}
	
}
