package services;

import models.TicketType;
import play.Logger;
import play.db.ebean.Model;

import java.util.List;
import java.util.UUID;

/**
 *
 */
public class TicketTypeService {

    public static Model.Finder<String,TicketType> find = new Model.Finder(String.class, TicketType.class);

    public static List<TicketType> findAllByActivityId(String activityId){
        return find.where().eq("activity_id", activityId).findList();
    }

    public static List<TicketType> findAllActiveTicketTypesByActivityId(String activityId){
        return find.where().eq("activity_id", activityId).eq("deleted", false).orderBy("price desc").findList();
    }

    public static TicketType findOneByActivityIdAndTypeName(String activityId, String typeName){
        return find.where().eq("activity_id", activityId).eq("deleted", false).eq("type_name", typeName).findUnique();
    }

    public static void doCreate(TicketType ticketType) throws Exception{
        ticketType.id = UUID.randomUUID().toString();
        ticketType.booked = 0;
        try {
            ticketType.save();
        }catch(Exception e){
            Logger.error("Create Ticket Type failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    public static void doUpdate(TicketType ticketType) throws Exception{
        try {
            ticketType.update();
        }catch(Exception e){
            Logger.error("Update Ticket Type failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    public static void doDelete(TicketType ticketType) throws Exception{
        ticketType.deleted = true;
        try {
            ticketType.update();
        }catch(Exception e){
            Logger.error("Create Ticket Type failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    public static void doDeleteAll(List<TicketType> ticketTypes) throws Exception{
        if (ticketTypes != null) {
            for (TicketType ticketType : ticketTypes) {
                doDelete(ticketType);
            }
        }
    }
}
