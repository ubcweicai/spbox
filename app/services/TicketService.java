package services;

import models.Ticket;
import play.Logger;
import play.db.ebean.Model;

import java.util.List;
import java.util.UUID;

/**
 *
 */
public class TicketService {

    private static Model.Finder<String,Ticket> find = new Model.Finder(String.class, Ticket.class);

    public static List<Ticket> findAllByActivityId(String activityId){
        return find.where().eq("activity_id", activityId).findList();
    }

    public static List<Ticket> findPaidTicketsByEmail(String email){
       return find.where().eq("assigned_to_email", email).eq("deleted", false).eq("paid", 1)
                .orderBy("purchase_date_time desc").findList();
    }

    public static List<Ticket> findTodoInvolving(String user) {
        return find.fetch("activity").where()
                .eq("done", false)
                .eq("activity.members.email", user)
                .findList();
    }

    public static void doCreate(Ticket ticket)throws Exception{
        ticket.id = UUID.randomUUID().toString();
        try{
            ticket.save();
        }catch(Exception e){
            Logger.error("Create Ticket failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    public static void doUpdate(Ticket ticket)throws Exception{
        try{
            ticket.update();
        }catch(Exception e){
            Logger.error("update Ticket failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    public static void doDelete(Ticket ticket)throws Exception{
        ticket.deleted = true;
        try{
            ticket.update();
        }catch(Exception e){
            Logger.error("delete Ticket failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    public static void doDeleteAll(List<Ticket> tickets)throws Exception{
        if (tickets != null) {
            for (Ticket ticket : tickets) {
                doDelete(ticket);
            }
        }
    }
}
