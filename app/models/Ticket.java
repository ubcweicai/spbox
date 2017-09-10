package models;

import models.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.*;

import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.db.ebean.*;
import utilities.QRCodeUtil;


@Entity
public class Ticket extends Model {

    @Id
    public String id;
    public Date purchase_date_time;
    public String title;
    public String date_time;
    public String venue;
    public String type_name;
    public double price;
    public String seat;
    
    public boolean checked = false;
    public Date checked_time = null;
    public String checked_by = null;

    // To identify what tickets have been bought together
    public String bundle_id;
    // The ticket is only valid when it is marked as paid
    public boolean paid = false;

    // This property make sure we could know the ticket is correctly return to ticket pool
    public boolean payment_failure_handled = false;

    public boolean deleted = false;
    
    @Column(columnDefinition = "LONGTEXT")
    public String qrcode;

    @ManyToOne
    public User assigned_to;
    public String folder;
    @ManyToOne
    public Activity activity;


    public String GetLocalDateTime(Date date)
    {

        if (date == null) {
            return "";
        }
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss z", Locale.CHINA);
        df.setTimeZone(TimeZone.getTimeZone(City.GetTimeZone(activity.GetVenueCity(), activity.GetVenueCountry(), "US/Pacific")));
        return df.format(date);
    }


    public String HasError()
    {
        if(!paid && !payment_failure_handled)
        {
            return "Payment failure is not handled";
        }
        return null;
    }

    public static Model.Finder<String,Ticket> find = new Model.Finder(String.class, Ticket.class);

    public static List<Ticket> findTodoInvolving(String user) {
       return find.fetch("activity").where()
                .eq("done", false)
                .eq("activity.members.email", user)
           .findList();
    }
    
    public Ticket(String ticketId, User assignedTo, Activity activity, TicketType ticket_type, String bundleId, boolean paid) {
    	this.id = ticketId;
    	this.purchase_date_time=new Date();

    	this.activity = activity;
    	this.title = this.activity.name;
    	this.date_time = this.activity.date_time;
    	this.venue = this.activity.venue;
    	this.assigned_to=assignedTo;
    	this.folder = this.activity.folder;
    	this.type_name=ticket_type.type_name;
    	this.price=ticket_type.price;
    	this.seat=ticket_type.seat;
        this.bundle_id = bundleId;
        this.paid = paid;
        this.qrcode = null;// will create later to reduce transaction lock time

        // Never call save() here, we would handle ticket save in the transaction and should be able to rollback

    }
    
	public void deleteTicket(){
		this.deleted = true;
		
		try{
        	this.update();
        } catch(Exception e) {
        	Logger.error("Create ticket failed: "+ e.getLocalizedMessage());
        }
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

}