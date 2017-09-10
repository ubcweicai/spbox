package controllers;

import static play.data.Form.form;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.Base64;

import annotation.Administrator;
import annotation.Authenticated;
import annotation.ControllerHandler;
import annotation.UserContext;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ning.http.util.*;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;

import models.*;
import play.Logger;
import play.cache.Cache;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.*;
import play.twirl.api.Html;
import services.ActivityService;
import views.html.*;


@With({ControllerHandler.class, Administrator.class})
public class AdminAction extends Controller {


	//WEBPAGE DISPAY: portal of system management
	public static Result systemManagerUI() {
		try {

			return ok(system_manager.render("系统管理"));

		} catch (Exception e) {
			Logger.error("System Managment Error");
			String shtml = "系统管理面板出错！";
			return ok(info.render("系统管理面板出错！", Html.apply(shtml)));
		}
	}

	//WEBPAGE DISPAY: article management
	public static Result articleManagerUI(String atype) {
		try {
			List<Article> article_list = Article.find.all();

			return ok(article_manager.render("文章列表", atype, article_list));

		} catch (Exception e) {
			Logger.error("Read article list failed: " + e.getLocalizedMessage());
			String shtml = "读取文章列表出错！";
			return ok(info.render("读取文章列表出错！", Html.apply(shtml)));
		}
	}

	//WEBPAGE DISPAY: edit an article
	public static Result editArticleUI() {

		try {
			DynamicForm userForm = form().bindFromRequest();
			String article_id = userForm.get("id");
			if (article_id == null) {
				return ok(edit_article.render("创建文章", null));
			} else {
				Article article = Article.find.byId(article_id);
				if (article != null) {
					return ok(edit_article.render("编辑文章", article));
				} else {
					String shtml = "文章不存在";
					return ok(info.render("非法操作", Html.apply(shtml)));
				}
			}
		} catch (Exception e) {
			Logger.error("edit article failed: " + e.getLocalizedMessage());
			String shtml = "创建更新文章出错！";
			return ok(info.render("创建更新文章出错！", Html.apply(shtml)));
		}
	}

	//WEBPAGE DISPAY: article management
	public static Result cityManagerUI(String atype) {
		try {
			List<City> city = City.find.all();

			return ok(city_manager.render("城市列表", atype, city));

		} catch (Exception e) {
			Logger.error("Read city list failed: " + e.getLocalizedMessage());
			String shtml = "读取城市列表出错！";
			return ok(info.render("读取城市列表出错！", Html.apply(shtml)));
		}
	}

	//WEBPAGE DISPAY: edit an article
	public static Result editCityUI() {

		try {
			DynamicForm userForm = form().bindFromRequest();
			String city_id = userForm.get("id");
			if (city_id == null) {
				return ok(edit_city.render("创建城市", null,  TimeZone.getAvailableIDs()));
			} else {
				City city = City.find.byId(city_id);
				if (city != null) {

					return ok(edit_city.render("编辑城市", city, TimeZone.getAvailableIDs()));
				} else {
					String shtml = "城市不存在";
					return ok(info.render("非法操作", Html.apply(shtml)));
				}
			}
		} catch (Exception e) {
			Logger.error("Edit city failed ", e);
			String shtml = "创建更新城市出错！";
			return ok(info.render("创建更新城市出错！", Html.apply(shtml)));
		}
	}

	//WEBPAGE DISPAY: user management
	public static Result userManagerUI(String role) {
		try {
			List<User> user_list;

			if ((role == null) || (role.equals("all"))) {
				user_list = User.find.all();
			} else {
				user_list = User.find.where().eq("role", role).findList();
			}
			return ok(user_manager.render("用户列表",role, user_list));
		} catch (Exception e) {
			Logger.error("Read user list failed: " + e.getLocalizedMessage());
			String shtml = "读取用户列表出错！";
			return ok(info.render("读取用户列表出错！", Html.apply(shtml)));
		}
	}



	public static Result changeStar(String id) {
		try {
			Activity a = ActivityService.findOneById(id);
			a.star = !a.star;

			ActivityService.doUpdate(a);

			Logger.info("change star");

			ObjectNode result = Json.newObject();
			result.put("response", String.valueOf(a.star));

			return ok(result);
		} catch (Exception e) {
			Logger.error("Failed to change star", e);

			ObjectNode result = Json.newObject();
			result.put("error", "change star failed.");
			return badRequest(result);
		}

	}

	
	//FUNCTION: to delete a user
	public static Result deleteUser() {
		try{
			DynamicForm activityForm = form().bindFromRequest();
	
			if (activityForm.hasErrors()) {
				System.out.println("用户编辑错误");
				String shtml = "用户编辑错误";
				return ok(info.render("用户编辑错误", Html.apply(shtml)));
			} else {
				String edit_email = activityForm.get("email");
	
				//CHECK if the activity existing
				if (edit_email == "") {
					//System.out.println("create a new activity");
					String shtml = "用户不存在";
					return ok(info.render("用户邮箱出错", Html.apply(shtml)));
				}
				//delete a activity
				else {
					//System.out.println("update an existing activity");
					User user = User.find.byId(edit_email);
					if (user != null) {
						String shtml = "用户 <" + user.email + "> 删除成功";
	
						//delete the activity
						user.deleteUser();
	
						return ok(info.render("用户删除成功", Html.apply(shtml)));
					} else {
						System.out.println("找不到该用户邮件");
						String shtml = "找不到该用户邮件";
						return ok(info.render("用户编辑错误", Html.apply(shtml)));
					}
				}
			}
		} catch (Exception e) {
			Logger.error("Delete activity failed: " + e.getLocalizedMessage());
          	String shtml = "删除用户出错！";
			return ok(info.render("删除用户出错！", Html.apply(shtml)));
        }
	}


	// to edit an article
	@BodyParser.Of(BodyParser.Json.class)
	public static Result editArticle() {
		try {
			Http.RequestBody body = request().body();
			String id = body.asJson().findPath("id").textValue();
			String name = body.asJson().findPath("name").textValue();
			String title = body.asJson().findPath("title").textValue();
			String content = body.asJson().findPath("content").textValue();

			if (name == null) {
				name = "";
			}
			if (title == null) {
				title = "";
			}
			if (content == null) {
				content = "";
			}


			Article a = Article.find.where().eq("id", id).findUnique();

			// To create new user
			if (a == null) {

				// check if you are admin
				User currentUser = UserContext.GetCurrentUser();
					if (currentUser == null) {
						Logger.warn("请先登录");
						return unauthorized("false");
					} else if (!currentUser.IsAdministrator()) {
						Logger.warn("不是管理员请勿创建文章");
						return unauthorized("false");
					} else {

						Article checkDuplicated = Article.find.where().eq("name", name.toUpperCase()).findUnique();
						if(checkDuplicated != null) {
							Logger.warn("创建重复文章");
							ObjectNode result = Json.newObject();
							result.put("message", "duplicated");
							return forbidden(result);
						}else{
							Article newArticle = new Article(name.toUpperCase(), title, content, Article.ArticleType.GENERAL);
							Logger.info("成功创建文章： " + name);
							return ok(newArticle.toJsonObject());
						}
					}
			}
			// to update existing user
			else {

				User currentUser = UserContext.GetCurrentUser();
				if (currentUser == null) {
					Logger.warn("请先登录");
					return unauthorized("false");
				} else if (!currentUser.IsAdministrator()) {
					Logger.warn("不是管理员请勿更新文章");
					return unauthorized("false");
				} else {


					a.id = id;

					if(a.type != Article.ArticleType.BUILTIN) {
						a.name = name.toUpperCase();
					}
					a.title = title;
					a.content = content;
					a.update();
					Logger.info("Update article {} successful.", name);
					return ok(a.toJsonObject());


				}

			}
		} catch (Exception e) {
			Logger.error("Register / update user failed: ", e);
			return ok("增加或更新文章出错！");
		}
	}

	//FUNCTION: to delete an article
	public static Result deleteArticle() {
		try{
			DynamicForm activityForm = form().bindFromRequest();

			if (activityForm.hasErrors()) {
				System.out.println("文章编辑错误");
				String shtml = "文章编辑错误";
				return ok(info.render("文章编辑错误", Html.apply(shtml)));
			} else {
				String delete_article = activityForm.get("id");

				//CHECK if the activity existing
				if (delete_article == "") {
					//System.out.println("create a new activity");
					String shtml = "文章不存在";
					return ok(info.render("文章ID出错", Html.apply(shtml)));
				}
				//delete a activity
				else {
					//System.out.println("update an existing activity");
					Article article = Article.find.byId(delete_article);
					if (article != null) {
						String shtml = "文章 <" + article.name+ "("+ article.title +")" + "> 删除成功";

						//delete the activity
						article.delete();

						return ok(info.render("文章删除成功", Html.apply(shtml)));
					} else {
						System.out.println("找不到该文章");
						String shtml = "找不到该文章";
						return ok(info.render("文章编辑错误", Html.apply(shtml)));
					}
				}
			}
		} catch(Exception e) {
			Logger.error("Delete activity failed: "+ e.getLocalizedMessage());
			String shtml = "删除文章出错！";
			return ok(info.render("删除文章出错！", Html.apply(shtml)));
		}
	}

	// to edit a city
	@BodyParser.Of(BodyParser.Json.class)
	public static Result editCity() {
		try {
			Http.RequestBody body = request().body();
			String id = body.asJson().findPath("id").textValue();
			String name = body.asJson().findPath("name").textValue();
			String country = body.asJson().findPath("country").textValue();
			String timezone = body.asJson().findPath("timezone").textValue();

			if ((name == null)||(country == null)||(timezone == null)) {
				return badRequest("增加或更新城市出错！");
			}

			City c = City.find.where().eq("id", id).findUnique();

			// To create new user
			if (c == null) {
				// check if you are admin
				User currentUser = UserContext.GetCurrentUser();
				if (currentUser == null) {
					Logger.warn("请先登录");
					return unauthorized("false");
				} else if (!currentUser.role.equals("admin")) {
					Logger.warn("不是管理员请勿创建城市");
					return unauthorized("false");
				} else {

					City checkDuplicated = City.find.where().eq("name", name.toUpperCase()).eq("country", country.toUpperCase()).findUnique();
					if(checkDuplicated != null) {
						Logger.warn("创建重复城市");
						ObjectNode result = Json.newObject();
						result.put("message", "duplicated");
						return forbidden(result);
					}else{
						City newCity = new City(name.toUpperCase(), country.toUpperCase(), timezone);
						Logger.info("成功创建城市： " + name);
						return ok(newCity.toJsonObject());
					}
				}
			}
			// to update existing user
			else {

				User currentUser = UserContext.GetCurrentUser();
				if (currentUser == null) {
					Logger.warn("请先登录");
					return unauthorized("false");
				} else if (!currentUser.IsAdministrator()) {
					Logger.warn("不是管理员请勿更新文章");
					return unauthorized("false");
				} else {


						c.id = id;
						c.name = name.toUpperCase();
						c.country = country.toUpperCase();
						c.timeZone = timezone;
						c.update();
						Logger.info("Update city {} successful.", name);
						return ok(c.toJsonObject());


				}
			}
		} catch (Exception e) {
			Logger.error("Register / update user failed: ", e);
			return ok("增加或更新城市出错！");
		}
	}

	//FUNCTION: to delete a city
	public static Result deleteCity() {
		try{
			DynamicForm activityForm = form().bindFromRequest();

			if (activityForm.hasErrors()) {
				System.out.println("城市编辑错误");
				String shtml = "城市编辑错误";
				return ok(info.render("城市编辑错误", Html.apply(shtml)));
			} else {
				String delete_city = activityForm.get("id");

				//CHECK if the activity existing
				if ("".equals(delete_city)) {
					//System.out.println("create a new activity");
					String shtml = "城市不存在";
					return ok(info.render("城市ID出错", Html.apply(shtml)));
				}
				//delete a activity
				else {
					//System.out.println("update an existing activity");
					City city = City.find.byId(delete_city);
					if (city != null) {
						String shtml = "城市 <" + city.name+ "("+ city.country +")" + "> 删除成功";

						//delete the activity
						city.delete();

						return ok(info.render("城市删除成功", Html.apply(shtml)));
					} else {
						System.out.println("找不到该城市");
						String shtml = "找不到该城市";
						return ok(info.render("城市编辑错误", Html.apply(shtml)));
					}
				}
			}
		} catch(Exception e) {
			Logger.error("Delete city failed: "+ e.getLocalizedMessage());
			String shtml = "删除城市出错！";
			return ok(info.render("删除城市出错！", Html.apply(shtml)));
		}
	}


	public static Result addCity(String name, String country, String timeZone){

		ObjectNode result = Json.newObject();
		try {

			City checkDuplicated = City.find.where().eq("name", name).eq("country", country).findUnique();

			if(checkDuplicated != null) {
				Logger.warn("创建重复城市2");
				result.put("message", "duplicated");
				return badRequest(result);
			}


			City.SetTimeZone(name, country, timeZone);
			result.put("success", true);
		}
		catch (Exception e){
			Logger.error("Failed to set config {}, {}", name, e);
			result.put("success", false);
			result.put("message", "Failed to set config " + e);
		}
		return ok(result);

	}

	public static Result addCity2(String name, String country, String timeZone1, String timeZone2){

		ObjectNode result = Json.newObject();
		try {

			City checkDuplicated = City.find.where().eq("name", name).eq("country",country).findUnique();

			if(checkDuplicated != null) {
				Logger.warn("创建重复城市3");
				result.put("message", "duplicated");
				return badRequest(result);
			}


			City.SetTimeZone(name, country, timeZone1+"/"+timeZone2);
			result.put("success", true);
		}
		catch (Exception e){
			Logger.error("Failed to set config {}, {}", name, e);
			result.put("success", false);
			result.put("message", "Failed to set config " + e);
		}
		return ok(result);

	}


	public static Result GetUnProcessedInfo(){
		Integer publishRequestCount = ActivityService.findActivityCountByStatus(Activity.STATUS_REQUEST_PUBLISH);
		ObjectNode config = Json.newObject();
		config.put("publishRequests", publishRequestCount);
		return ok(config);
	}

	public static Result getConfig(String name){
		ObjectNode result = Json.newObject();
		// We skip cache to see the setting in db
		try {
			Setting stored = Setting.find.byId(name);
			String cached = (String) Cache.get("setting." + name);
			if (stored == null && cached == null) {

				result.put("success", false);
				result.put("message", "setting " + name + " does not exist");
			} else if(stored != null){
				ObjectNode config = Json.newObject();
				config.put("name", stored.name);
				config.put("value", stored.value);
				config.put("cached", cached != null);
				config.put("db", true);
				result.put("success", true);
				result.put("result", config);

			}
			else  {
				ObjectNode config = Json.newObject();
				config.put("name", name);
				config.put("value", cached);
				config.put("cached", true);
				config.put("db", false);
				result.put("success", true);
				result.put("result", config);
			}
		}
		catch (Exception e){
			Logger.error("Failed to get config {} {}", name, e);
			result.put("success", false);
			result.put("message", "Failed to get config " + name);
		}

		return ok(result);

	}

	public static Result setConfig(String name, String value){
		ObjectNode result = Json.newObject();
		try {
			Setting.SetConfig(name, value);
			result.put("success", true);
		}
		catch (Exception e){
			Logger.error("Failed to set config {}, {}", name, e);
			result.put("success", false);
			result.put("message", "Failed to set config " + e);
		}
		return ok(result);
	}

}


