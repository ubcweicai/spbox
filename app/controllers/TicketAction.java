package controllers;

import annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.*;
import org.joda.time.DateTime;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.*;
import play.twirl.api.Html;
import services.ActivityService;
import services.TicketTypeService;
import views.html.*;

import java.io.*;
import java.util.*;


import static play.data.Form.form;

@With({ ControllerHandler.class, Organizer.class})
public class TicketAction extends Controller {

	// WEBPAGE DISPAY: activity management
	public static Result activityManagerUI() {
		try {
			User user = UserContext.GetCurrentUser();
			List<Activity> activity_list = user.IsAdministrator() ? Activity.find
					.where().eq("deleted", false).orderBy("star desc, create_date_time desc").findList()
					: Activity.find.where().eq("deleted", false)
							.eq("organizer", user.email).findList();
			return ok(activity_manager.render("活动列表", activity_list));
		} catch (Exception e) {
			Logger.error("Read activity list failed: ", e);
			String shtml = "读取活动列表出错！";
			return ok(info.render("读取活动列表出错！", Html.apply(shtml)));
		}
	}

	// WEBPAGE DISPAY: tickets management
	public static Result ticketManagerUI() {
		try {

			//Logger.debug("enter: "+DateTime.now());

			User user = UserContext.GetCurrentUser();
			DynamicForm activityForm = form().bindFromRequest();
			String activityIdInForm = activityForm.get("id");

			final String activity_id = (activityIdInForm == null)? "none" : activityIdInForm;

			//final String activity_id = ((activityIdInForm == null) || (activityIdInForm.equals("all"))) ? "all" : activityIdInForm;

			List<Activity> activity_list = user.IsAdministrator() ? Activity.find
					.where().eq("deleted", false).findList()
					: Activity.find.where().eq("deleted", false)
							.eq("organizer", user.email).findList();

			List<Ticket> ticket_list;

			//Logger.debug("before selection: "+DateTime.now());

			if (activity_id.equals("all")) {
				ticket_list = user.IsAdministrator() ? Ticket.find.where()
						.eq("deleted", false).eq("paid", true)
						.orderBy("assigned_to_email").findList()
						: Ticket.find.fetch("activity").where()
								.eq("deleted", false).eq("paid", true)
								.eq("activity.organizer", user.email)
								.findList();

			} else if (activity_id.equals("none")){
				ticket_list = new ArrayList<Ticket>();
			} else if (activity_list.stream().anyMatch(
					(activity -> activity.id.equals(activity_id)))) {
				ticket_list = Ticket.find.where().eq("deleted", false).eq("paid", true)
						.eq("activity_id", activity_id)
						.orderBy("assigned_to_email").findList();
			} else {
				ticket_list = new LinkedList<>();
			}

			//Logger.debug("before ticket count: "+DateTime.now());

			long numberOfChecktedTicket = ticket_list.stream()
					.filter(t -> t.checked).count();

			//Logger.debug("before rendering: "+DateTime.now());

			String aid = "票据列表";
			return ok(ticket_manager.render(aid, activity_id, ticket_list, numberOfChecktedTicket, activity_list));

		} catch (Exception e) {
			Logger.error("Read ticket list failed: ", e);
			String shtml = "读取票据列表出错！";
			return ok(info.render("读取票据列表出错！", Html.apply(shtml)));
		}
	}

	// WEBPAGE DISPAY: tickets type management
	public static Result ticketTypesUI(String id) {
		User user = UserContext.GetCurrentUser();
	
		if (id == null) {
			String shtml = "活动不存在";
			return ok(info.render("非法操作", Html.apply(shtml)));
		}
	
		Activity activity = Activity.find.byId(id);
	
		if (activity != null
				&& (user.IsAdministrator() || user.email
						.equals(activity.organizer))) {
			return ok(edit_ticket_type.render("编辑票务类型", activity,
					TicketTypeService.findAllActiveTicketTypesByActivityId(activity.id)));
		} else {
			String shtml = "活动不存在";
			return ok(info.render("非法操作", Html.apply(shtml)));
		}
	
	}

	// FUNCTION: to cut a ticket
	public static Result cutTicketUI() {
		try {
			User user = UserContext.GetCurrentUser();
			DynamicForm ticketForm = form().bindFromRequest();
			String ticket_id = ticketForm.get("id");
			Ticket t = user.IsAdministrator() ? Ticket.find.where()
					.eq("id", ticket_id).eq("deleted", false).findUnique()
					: Ticket.find.fetch("activity").where().eq("id", ticket_id)
							.eq("deleted", false)
							.eq("activity.organizer", user.email).findUnique();

			if (t != null) {
				String shtml = "检票成功";
				t.checked = true;
				t.checked_time = new Date();
				t.checked_by = UserContext.GetCurrentUser().email;
				t.update();
				return ok(info.render("检票成功", Html.apply(shtml)));
			} else {
				String shtml = "检票失败，无法读取该票";
				return ok(info.render("检票失败", Html.apply(shtml)));
			}
		} catch (Exception e) {
			Logger.error("Cut ticket failed: ", e);
			String shtml = "剪票出错！";
			return ok(info.render("剪票出错！", Html.apply(shtml)));
		}
	}

	public static Result deleteActivityImage(String activityId){
		
		// CHECK if the activity existing
		if (activityId == "") {
			// System.out.println("create a new activity");
			String shtml = "活动不存在";
			return ok(info.render("活动ID出错", Html.apply(shtml)));
		}
		// delete a activity
		else {
			// System.out.println("update an existing activity");
			Activity activity = Activity.find.byId(activityId);
			User user = UserContext.GetCurrentUser();
			if (activity != null
					&& (user.IsAdministrator() || user.email
							.equals(activity.organizer))) {
				
				activity.image = null;
				activity.update();

				ObjectNode result = Json.newObject();
				result.put("success", true);

				return ok(result);
			} else {
				System.out.println("找不到该活动ID");
				String shtml = "找不到该活动ID";
				return ok(info.render("活动编辑错误", Html.apply(shtml)));
			}
		}
			
		
	}
	
	public static Result deleteActivityDescriptionImage(String activityId){
		
		// CHECK if the activity existing
		if (activityId == "") {
			// System.out.println("create a new activity");
			String shtml = "活动不存在";
			return ok(info.render("活动ID出错", Html.apply(shtml)));
		}
		// delete a activity
		else {
			// System.out.println("update an existing activity");
			Activity activity = Activity.find.byId(activityId);
			User user = UserContext.GetCurrentUser();
			if (activity != null
					&& (user.IsAdministrator() || user.email
							.equals(activity.organizer))) {
				
				activity.description_image = null;
				activity.update();

				ObjectNode result = Json.newObject();
				result.put("success", true);

				return ok(result);
			} else {
				System.out.println("找不到该活动ID");
				String shtml = "找不到该活动ID";
				return ok(info.render("活动编辑错误", Html.apply(shtml)));
			}
		}
			
		
	}
	// FUNCTION: to delete a activity
	public static Result deleteActivity(String id) {
		try {
				// CHECK if the activity existing
				if (id == "") {
					// System.out.println("create a new activity");
					String shtml = "活动不存在";
					return ok(info.render("活动ID出错", Html.apply(shtml)));
				}
				// delete a activity
				else {
					// System.out.println("update an existing activity");
					Activity activity = Activity.find.byId(id);
					User user = UserContext.GetCurrentUser();
					if (activity != null
							&& (user.IsAdministrator() || user.email
									.equals(activity.organizer))) {

						String shtml = "活动 <" + activity.name + "> 删除成功";

						// delete the activity
						ActivityService.doDelete(activity);

						return ok(info.render("活动删除成功", Html.apply(shtml)));
					} else {
						System.out.println("找不到该活动ID");
						String shtml = "找不到该活动ID";
						return ok(info.render("活动编辑错误", Html.apply(shtml)));
					}
				}

		} catch (Exception e) {
			Logger.error("Delete activity failed: ", e);
			String shtml = "删除活动出错！";
			return ok(info.render("删除活动出错！", Html.apply(shtml)));
		}
	}

	public static Result getEmailSettingsUI(String id) {
		try {
			if (id == "") {
				String shtml = "活动不存在";
				return ok(info.render("活动ID出错", Html.apply(shtml)));
			}

			Activity activity = Activity.find.byId(id);
			User user = UserContext.GetCurrentUser();
			if (activity != null
					&& (user.IsAdministrator() || user.email
					.equals(activity.organizer))) {

				return ok(edit_email_settings.render("邮件设置", activity));
			} else {
				System.out.println("找不到该活动ID");
				String shtml = "找不到该活动ID";
				return ok(info.render("活动编辑错误", Html.apply(shtml)));
			}


		} catch (Exception e) {
			Logger.error("Failed to get email settings", e);
			String shtml = "获取邮件配置出错！";
			return ok(info.render("获取邮件配置出错！", Html.apply(shtml)));
		}

	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result saveEmailSettings(String activityId)
	{
		try {
			Activity activity = Activity.find.byId(activityId);

			if (activity == null) {
				return notFound();
			}

			JsonNode node = request().body().asJson();
			Boolean emailToBuyer = node.findPath("emailToBuyer").booleanValue();
			Boolean emailToOrganizer = node.findPath("emailToOrganizer").booleanValue();
			Boolean isEmailCustomized = node.findPath("isEmailCustomized").booleanValue();

			activity.SetEmailToBuyerEnabled(emailToBuyer);
			activity.SetEmailToOrganizerEnabled(emailToOrganizer);
			activity.SetIsEmailCustomized(isEmailCustomized);
			if (isEmailCustomized) {
				String template = node.findPath("content").textValue();
				activity.SetEmailTemplate(template);
			}

			ObjectNode result = Json.newObject();
			result.put("success", true);
			return ok(result);
		}
		catch (Exception e)
		{
			Logger.error("Failed to save email settings", e);
			return badRequest("Failed to save email settings");
		}
	}

	public static Result addImage(){
		try
		{
			Http.MultipartFormData body = request().body().asMultipartFormData();
			Http.MultipartFormData.FilePart file = body.getFile("file");
			String host = request().host();

			if (file != null) {
				File fileObj = file.getFile();

				FileInputStream fin = new FileInputStream(fileObj);
				byte fileContent[] = new byte[(int) fileObj.length()];
				fin.read(fileContent);

				Image newImg = new Image(file.getContentType(), fileContent);
				newImg.save();
				Logger.debug("New image is added " + newImg.id);
				return ok("http://" + host + "/image/" + newImg.id);
			} else {
				return badRequest("Failed to upload image.");
			}


		} catch (Exception e) {
			Logger.error("Failed to add image {}", e);
			return badRequest("Failed to add image");
		}
	}

}
