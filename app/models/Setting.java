package models;

import play.Logger;
import play.cache.Cache;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
//import java.beans.Transient;

@Entity
public class Setting extends Model {
    @Id
    public String name;
    public String value;


    public void setValue(String value){
        this.value = value;
    }

    public static Finder<String,Setting> find = new Finder<String,Setting>(
            String.class, Setting.class
    );

    public static Boolean GetConfig(String name, Boolean defaultValue){
        try {
            return Boolean.parseBoolean(GetConfig(name, defaultValue.toString()));
        }
        catch (Exception e){
            Logger.warn("Failed to get boolean config", e);
            return defaultValue;
        }
    }

    public static void SetConfig(String name, Boolean value){
        SetConfig(name, value.toString());
    }

    public static String GetConfig(String name, String defaultValue) {
        String result = (String)Cache.get("setting." + name);
        if (result == null) {
            Setting stored = find.byId(name);
            if (stored == null) {
                Logger.debug("'setting." + name + "' is not found in store.");
                result = defaultValue;
            } else {
            	Logger.debug("'setting." + name + "' is found: "+stored.value);
                result = stored.value;
            }
            Cache.set("setting." + name, result, 600); // cache for 10 minutes
        }
        return result;
    }

    public static void SetConfig(String name, String value){

        try {
            Setting stored = find.byId(name);
            if (stored == null) {
                Setting setting = new Setting();
                setting.name = name;
                setting.value = value;
                setting.save();
            } else {
                stored.setValue(value);
                stored.save();

            }
            Cache.remove("setting." + name);  // invalidate the cache
        }
        catch(Exception e){
            Logger.error("Failed to set config: " + name);
        }
    }

    public static String GetTimezone(String city, String defaultValue) {
        String timezone = defaultValue;
        if(city.equalsIgnoreCase("vancouver"))
        {
            Logger.info("time zone get: "+city);
        }

        return timezone;
    }

}
