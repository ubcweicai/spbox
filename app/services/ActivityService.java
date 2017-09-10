package services;

import com.avaje.ebean.Expr;
import models.Activity;
import models.Ticket;
import models.TicketType;
import play.Logger;
import play.db.ebean.Model;

import java.util.List;
import java.util.UUID;

/**
 *
 */
public class ActivityService {

    private static Model.Finder<String, Activity> find = new Model.Finder(String.class, Activity.class);

    public static Activity findOneById(String id){
       return find.where().eq("id", id).findUnique();
    }

    public static List<Activity> findAllByStatus(String... status){

//        for(String s :status){
//
//
//        }

        return find.where().or(Expr.eq("status", status[1]),
                Expr.eq("status", "expired")
        ).eq("deleted", false)
                .orderBy("star desc, start_date_time desc").findList();
    }

    public static List<Activity> findAll(){
        return find
                .where().eq("deleted", false).orderBy("star desc, create_date_time desc").findList();
    }

    public static List<Activity> findAllByUserRole(String role, String email){
       return find.where().eq("deleted", false)
               .eq(role, email).findList();
    }

    public static List<Activity> findAllByStatus(String role, String email){
        return find.where().eq("deleted", false)
                .eq(role, email).findList();
    }

    public static Integer findActivityCountByStatus(String status){
        return find.where().eq("status", status).findRowCount();
    }

    public static void doCreate(Activity activity) throws Exception{
        activity.id = UUID.randomUUID().toString();
        try {
            activity.save();
        }catch(Exception e){
            Logger.error("Create activity failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    public static void doUpdate(Activity activity) throws Exception{
        try {
            activity.update();
        }catch(Exception e){
            Logger.error("update activity failed: " + e.getLocalizedMessage());
            throw e;
        }
    }

    public static void doDelete(Activity activity) throws Exception{
        List<TicketType> ticketTypes = TicketTypeService.findAllByActivityId(activity.id);
        TicketTypeService.doDeleteAll(ticketTypes);

        List<Ticket> tickets = TicketService.findAllByActivityId(activity.id);
        TicketService.doDeleteAll(tickets);

        activity.deleted = true;
        try {
            activity.update();
        }catch(Exception e){
            Logger.error("delete activity failed: " + e.getLocalizedMessage());
            throw e;
        }
    }
}
