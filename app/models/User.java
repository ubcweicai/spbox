package models;

import javax.persistence.*;

import annotation.UserContext;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.db.ebean.*;
import play.libs.Json;
import play.twirl.api.Html;
import views.html.info;

import com.avaje.ebean.*;

@Entity
public class User extends Model {

    @Id
    public String email;
    public String name;
    public String password;
    public String role;
    //need to add a "tel, gender, age, address" column to the database table User
    public String tel;
    public String gender;
    public int age;
    public String address;
    public boolean verified;
    
    public User(String email, String password) {
        this.email = email;
        this.name = "";
        this.password = password;
        this.role = "user";
        
        try{
        	this.save();
        } catch(Exception e) {
        	Logger.error("Create user failed: "+ e.getLocalizedMessage());
        }
    }

    public ObjectNode toJsonObject()
    {
        ObjectNode result = Json.newObject();
        result.put("email", email);
        result.put("name", name);
        result.put("role", role);
        result.put("tel", tel);
        result.put("gender", gender);
        result.put("age", age);
        result.put("address", address);
        result.put("verified", verified);
        return result;
    }

    
    public boolean IsAdministrator()
    {
        return role != null && role.equals(ROLE_ADMINISTRATOR);
    }

    public boolean IsOrganizer()
    {
        return role != null && role.equals(ROLE_ORGANIZER);
    }

    public boolean IsBlocked()
    {
        return role != null && role.equals("blocked");
    }

    public static String ROLE_ADMINISTRATOR = "admin";
    public static String ROLE_ORGANIZER = "org";
    
    public User(String email, String name, String password, String tel, String gender, int age, String address) {
      this.email = email;
      this.name = name;
      this.password = password;
      this.role = "user";
      
      this.tel = tel;
      this.gender= gender;
      this.age = age;
      this.address = address;

      try{
      	this.save();
      } catch(Exception e) {
      	Logger.error("Create user failed: "+ e.getLocalizedMessage());
      }
    }

    public User(String email, String name, String password, String role, String tel, String gender, int age, String address) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;

        this.tel = tel;
        this.gender= gender;
        this.age = age;
        this.address = address;

        try{
        	this.save();
        } catch(Exception e) {
        	Logger.error("Create user failed: ", e);
        }
    }
    
    public void deleteUser()
    {
    	this.role = "blocked";
    	
    	try{
        	this.update();
        } catch(Exception e) {
        	Logger.error("Delete user failed: ", e);
        }
    }
    
    public static Finder<String,User> find = new Finder<String,User>(
        String.class, User.class
    ); 
    
    public static User authenticate(String email, String password) {
        return find.where().eq("email", email)
            .eq("password", password).findUnique();
    }
    

    public static User CurrentUser() {
        return UserContext.GetCurrentUser();
    }

    public static String GetCurrentUserRole(){
        return UserContext.GetCurrentUser() != null ? UserContext.GetCurrentUser().role : "";
    }

    public static String GetCurrentUserEmail(){
        return UserContext.GetCurrentUser() != null ? UserContext.GetCurrentUser().email : "";
    }
}