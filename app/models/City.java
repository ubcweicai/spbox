package models;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.db.ebean.Model;
import play.libs.Json;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;
//import java.beans.Transient;

@Entity
public class City extends Model {

    @Id
    public String id;
    public String name;
    public String country;
    public String timeZone;

    public static Finder<String,City> find = new Finder<String,City>(
            String.class, City.class
    );


    public City(String name, String country, String timeZone)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.country = country;
        this.timeZone = timeZone;
        try {
            this.save();
            Logger.info("Create city success!");
        } catch (Exception e) {
            Logger.error("Create city failed: " + e.getLocalizedMessage());
        }
    }

    public static boolean SetTimeZone(String city, String country, String timeZone) {
        try {
            new City(city.toUpperCase(), country.toUpperCase(), timeZone);
            return true;
        }catch (Exception e)
        {
            Logger.error("Create city failed: " + e.getLocalizedMessage());
            return false;
        }
    }

    public static String GetTimeZone(String city, String defaultTimeZone) {
        try {
            City theCity = City.find.where().eq("name", city.toUpperCase()).findUnique();
            if(theCity == null)
            {
                Logger.error("Unable to locate the city: "+ city.toUpperCase());
                return defaultTimeZone;
            }
            return theCity.timeZone;
        }catch (Exception e){
            Logger.error("Unable to locate the city: "+ city.toUpperCase(), e);
            return defaultTimeZone;
        }
    }

    public static String GetTimeZone(String city, String country, String defaultTimeZone) {
        try {
            City theCity = City.find.where().eq("name",city.toUpperCase()).eq("country",country.toUpperCase()).findUnique();
            if(theCity == null)
            {
                Logger.error("Unable to locate the city: "+ city.toUpperCase());
                return defaultTimeZone;
            }
            return theCity.timeZone;
        }catch (Exception e){
            Logger.error("Unable to locate the city: "+ city.toUpperCase(), e);
            return defaultTimeZone;
        }
    }

    public ObjectNode toJsonObject()
    {
        ObjectNode result = Json.newObject();
        result.put("id", this.id);
        result.put("name", this.name);
        result.put("country", this.country);
        result.put("timeZone", this.timeZone);
        return result;
    }

}
