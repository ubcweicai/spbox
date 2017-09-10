package models;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.db.ebean.*;
import play.libs.Json;

@Entity
public class TicketType extends Model {
	
	@Id
	public String id;
	@ManyToOne
	public Activity activity;
	public String type_name;
	public int quantity; // in total
	public int booked; // The number of tickets that have been booked
	public double price;
	public String currency; // The currency of the amount (3-letter ISO code). The default is USD.
	public String seat;
	public boolean taxIncluded = false;
	public boolean deleted = false;

	//TODO
	public static Model.Finder<String,TicketType> find = new Model.Finder(String.class, TicketType.class);

	public TicketType(){}

	//TODO
	public TicketType(Activity _activity)
	{
		this.id = UUID.randomUUID().toString();
		this.activity = _activity;
		this.type_name = "";
		this.quantity = 0;
		this.booked = 0;
		this.price = 0;
		this.seat = "";
		this.currency = "CAD";
		this.save();
	}

	public boolean IsOrganizedBy(User user)
	{
		if(user.IsAdministrator()) {
			return true;
		}
		if(user.role != null && user.role.equals("org")){
			return user.email.equals(this.activity.organizer);
		}
		return false;
	}

	public ObjectNode toJsonObject()
	{
		ObjectNode result = Json.newObject();
		result.put("type_name", type_name);
		result.put("quantity", quantity);
		result.put("booked", booked);
		result.put("price", price);
		result.put("seat", seat);
		result.put("currency", currency);
		result.put("taxIncluded", taxIncluded);
		return result;
	}

	//TODO
	public void deleteTicketType(){
		this.deleted = true;

		try{
        	this.update();
        } catch(Exception e) {
        	Logger.error("Delete ticket type failed. ", e);
        }
	}

	//	public TicketType(Activity _activity, String _type_name, int _quantity, int _booked, double _price, String _seat, String currency, boolean taxIncluded)
//	{
//		this.id = UUID.randomUUID().toString();
//		this.activity = _activity;
//		this.type_name = _type_name;
//		this.quantity = _quantity;
//		this.booked = _booked;
//		this.price = _price;
//		this.seat = _seat;
//		this.currency = currency;
//		this.taxIncluded = taxIncluded;
//		this.save();
//	}
//
//	public TicketType(Activity _activity, String _type_name, int _quantity, int _booked, double _price, String currency, boolean taxIncluded)
//	{
//		this.id = UUID.randomUUID().toString();
//		this.activity = _activity;
//		this.type_name = _type_name;
//		this.quantity = _quantity;
//		this.booked = _booked;
//		this.price = _price;
//		this.seat = "";
//		this.currency = currency;
//		this.taxIncluded = taxIncluded;
//
//		try{
//        	this.save();
//        } catch(Exception e) {
//        	Logger.error("Create ticket type failed. ", e);
//			throw e;
//        }
//	}
//
//	public void updateTicketType(String _type_name, int _quantity, double _price, boolean taxIncluded)
//	{
//		this.type_name = _type_name;
//		this.quantity = _quantity;
//		this.price = _price;
//		this.taxIncluded = taxIncluded;
//
//		try{
//        	this.update();
//        } catch(Exception e) {
//        	Logger.error("update ticket type failed: ", e);
//        }
//	}
//
//	public void deleteTicketType(){
//		this.deleted = true;
//
//		try{
//        	this.update();
//        } catch(Exception e) {
//        	Logger.error("Delete ticket type failed. ", e);
//        }
//	}

}