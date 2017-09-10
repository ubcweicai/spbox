package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.annotation.ConcurrencyMode;
import com.avaje.ebean.annotation.EntityConcurrencyMode;
import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.db.ebean.Model;
import play.libs.Json;
import services.ActivityService;
import services.TicketTypeService;
import utilities.BuiltInArticles;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@EntityConcurrencyMode(ConcurrencyMode.NONE)
public class Activity extends Model {

    @Id
    public String id;
    //need to add a "create_data_time" to record the creation time of the event
    @Version
    public Date create_date_time;
    public String name;
    public String folder;
    //need to change image column definition to longtext
    @Column(columnDefinition = "LONGTEXT")
    public String image;
    @Column(columnDefinition = "LONGTEXT")
    public String brief_description;
    @Column(columnDefinition = "LONGTEXT")
    public String description;
    @Column(columnDefinition = "LONGTEXT")
    public String description_image;
    public boolean star = false;
    public String status;
    public static String STATUS_UNPUBLISH = "unpublish";
    public static String STATUS_REQUEST_PUBLISH = "requested";
    public static String STATUS_EXPIRED = "expired";
    public static String STATUS_PUBLISH = "publish";

    public boolean deleted;
    public boolean bname = false;
    public boolean btel = false;
    public boolean bgender = false;
    public boolean bage = false;
    public boolean baddress = false;
    public Date start_date_time;
    public Date end_date_time;
    public String time_zone;
    public String date_time;
    public String venue;
    public String organizer;

    @OneToMany(cascade = CascadeType.REMOVE)
    public ArrayList<TicketType> ticket_type_list;

//    @OneToMany(cascade = CascadeType.REMOVE)
//    public List<Ticket> tickets = new ArrayList<Ticket>();

    public static Model.Finder<String, Activity> find = new Model.Finder(String.class, Activity.class);

    public Activity(){
    }

    public Boolean IsEmailToBuyerEnabled()
    {
        return Setting.GetConfig("email_to_buyer_enabled_" + this.id, true);
    }

    public void SetEmailToBuyerEnabled(Boolean isEnabled)
    {
        Setting.SetConfig("email_to_buyer_enabled_" + this.id, isEnabled);
    }

    public Boolean IsEmailToOrganizerEnabled()
    {
        return Setting.GetConfig("email_to_organizer_enabled_" + this.id, true);
    }

    public void SetEmailToOrganizerEnabled(Boolean isEnabled)
    {
        Setting.SetConfig("email_to_organizer_enabled_" + this.id, isEnabled);
    }

    public Boolean IsEmailCustomized()
    {
        return Setting.GetConfig("email_customized_" + this.id, false);
    }

    public void SetIsEmailCustomized(Boolean isEnabled)
    {
        Setting.SetConfig("email_customized_" + this.id, isEnabled);
    }

    public Article GetEmailTemplate()
    {
        if(IsEmailCustomized()){
            Article article = Article.find.where().eq("name", "EMAIL_CUSTOMIZED_" + this.id.toUpperCase()).findUnique();
            if(article == null){
                Article builtIn = BuiltInArticles.GetNewTicketTemplate();
                return new Article(builtIn.name, builtIn.title, builtIn.content, Article.ArticleType.GENERAL);
            }
            return article;
        }
        return BuiltInArticles.GetNewTicketTemplate();
    }

    public String GetCustomizedEmailTemplateContent()
    {
        Article article = Article.find.where().eq("name", "EMAIL_CUSTOMIZED_" + this.id.toUpperCase()).findUnique();
        if(article == null){
            return BuiltInArticles.GetNewTicketTemplate().content;
        }
        else
        {
            return article.content;
        }
    }

    public void SetEmailTemplate(String content){
        Article article = Article.find.where().eq("name", "EMAIL_CUSTOMIZED_" + this.id.toUpperCase()).findUnique();
        if(article == null){
            Article builtIn = BuiltInArticles.GetNewTicketTemplate();
            new Article("EMAIL_CUSTOMIZED_" + this.id.toUpperCase(), builtIn.title, content, Article.ArticleType.GENERAL);
        }
        else{
            article.content = content;
            article.save();
        }
    }

    public static ArrayList<Ticket> createTickets(String activityId, TicketType ticketType, User user, int quantity, String bundleId, boolean ticketPaid) {

        // Pre-generate uuids and sort to avoid SQL deadlock in MySQL
        // In MySQL, if we insert record with unordered primary key, while there are concurrent insert exists, deadlocks !!!!.

        String[] ticketIds = new String[quantity];
        for (int i = 0; i < quantity; i++) {
            ticketIds[i] = UUID.randomUUID().toString();
        }
        Arrays.sort(ticketIds);

        ArrayList<Ticket> tickets = new ArrayList<Ticket>();
        Activity activity = ActivityService.findOneById(activityId);
        Transaction t = Ebean.beginTransaction();
        try {
            Logger.debug("Updating ticket count ..., bundle {}", bundleId);
            int affected = Ebean.createUpdate(TicketType.class, "update ticket_type set booked = booked + :count where id = :id and (quantity - booked >= :count) and deleted = false")
                    .setParameter("id", ticketType.id)
                    .setParameter("count", quantity)
                    .execute();

            if (affected == 0) {
                Logger.debug("No enough ticket remain or activity is deleted...");
                return null;
            } else {

                Logger.debug("Creating tickets for bundle {}", bundleId);
                for (String ticketId : ticketIds) {
                    Ticket ticket = new Ticket(ticketId, user, activity, ticketType, bundleId, ticketPaid);
                    ticket.save();
                    tickets.add(ticket);
                }

            }
            Ebean.commitTransaction();
            return tickets;
        } catch (Exception e) {
            Logger.error("Something bad happened when creating the tickets for bundle " + bundleId, e);
            Ebean.rollbackTransaction();
        } finally {
            Ebean.endTransaction();
        }
        return null;
    }

    public String GetLocalDateTime(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss z", Locale.CHINA);
        df.setTimeZone(TimeZone.getTimeZone(City.GetTimeZone(GetVenueCity(), GetVenueCountry(), "US/Pacific")));
        return df.format(date);
    }

    public String GetLocalDate(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        df.setTimeZone(TimeZone.getTimeZone(City.GetTimeZone(GetVenueCity(), GetVenueCountry(), "US/Pacific")));
        return df.format(date);
    }

    public String GetLocalDateTimeCanada(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA);
        df.setTimeZone(TimeZone.getTimeZone(City.GetTimeZone(GetVenueCity(), GetVenueCountry(), "US/Pacific")));
        return df.format(date);
    }

    public String GetDisplayTime(String start_datetime, String end_datetime) {
        String result_string = "";

        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            df.setTimeZone(TimeZone.getTimeZone(Setting.GetConfig("INPUT_TIMEZONE", "US/Pacific")));
            Date start_date_time = df.parse(start_datetime);
            Date end_date_time = df.parse(end_datetime);

            Calendar cal = Calendar.getInstance();
            cal.setTime(start_date_time);
            int start_month = cal.get(Calendar.MONTH);
            int start_day = cal.get(Calendar.DATE);
            int start_hour = cal.get(Calendar.HOUR);
            int start_minute = cal.get(Calendar.MINUTE);

            cal.setTime(end_date_time);
            int end_month = cal.get(Calendar.MONTH);
            int end_day = cal.get(Calendar.DATE);
            int end_hour = cal.get(Calendar.HOUR);
            int end_minute = cal.get(Calendar.MINUTE);

            if ((start_month == end_month) && (start_day == end_day)) {
                result_string = start_month + "月" + start_day + "日" + start_hour + "点" + start_minute + "分 至 " + end_hour + "点" + end_minute + "分";
            } else if (start_month == end_month) {
                result_string = start_month + "月" + start_day + "日 至 " + end_day + "日";
            } else {
                result_string = start_month + "月" + start_day + "日 至 " + end_month + "月" + end_day + "日";
            }

        } catch (Exception e) {

        }
        return result_string;
    }

    public String GetVenueName() {
        if (this.venue == null || this.venue.trim() == "" || !this.venue.contains(":")) {
            return "";
        }

        JsonNode node = Json.parse(this.venue);
        if (node.hasNonNull("venue_name")) {
            return node.get("venue_name").asText();
        }
        return "";
    }

    public String GetVenueStreetAddress() {
        if (this.venue == null || this.venue.trim() == "" || !this.venue.contains(":")) {
            return "";
        }

        JsonNode node = Json.parse(this.venue);
        if (node.hasNonNull("venue_street_address")) {
            return node.get("venue_street_address").asText();
        }
        return "";
    }

    public String GetVenuePostalCode() {
        if (this.venue == null || this.venue.trim() == "" || !this.venue.contains(":")) {
            return "";
        }

        JsonNode node = Json.parse(this.venue);
        if (node.hasNonNull("venue_postal_code")) {
            return node.get("venue_postal_code").asText();
        }
        return "";
    }

    public String GetVenueCity() {
        if (this.venue == null || this.venue.trim() == "" || !this.venue.contains(":")) {
            return "";
        }

        JsonNode node = Json.parse(this.venue);
        if (node.hasNonNull("venue_city")) {
            return node.get("venue_city").asText();
        }
        return "";
    }

    public String GetVenueCountry() {
        if (this.venue == null || this.venue.trim() == "" || !this.venue.contains(":")) {
            return "";
        }

        JsonNode node = Json.parse(this.venue);
        if (node.hasNonNull("venue_country")) {
            return node.get("venue_country").asText();
        }
        return "";
    }

    public String GetVenueBrief() {
        if (this.venue == null || this.venue.trim() == "") {
            return "";
        }

        if (!this.venue.contains(":")) {
            return this.venue;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.GetVenueName()).append(", ").append(this.GetVenueCity());

        return stringBuilder.toString();
    }

    public String GetVenueFull() {
        if (this.venue == null || this.venue.trim() == "") {
            return "";
        }

        if (!this.venue.contains(":")) {
            return this.venue;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.GetVenueName()).append(", ");
                if(this.GetVenueStreetAddress() != "") {
                    stringBuilder.append(this.GetVenueStreetAddress()).append(", ");
                }
                if(this.GetVenuePostalCode() != ""){
                    stringBuilder.append(this.GetVenuePostalCode()).append(", ");
                }

                stringBuilder.append(this.GetVenueCity()).append(", ")
                .append(this.GetVenueCountry());

        return stringBuilder.toString();
    }

    public String[] GetTicketTypeListName() {

        List<TicketType> ticketTypeList = TicketTypeService.findAllActiveTicketTypesByActivityId(this.id);

        int size = ticketTypeList.size();

        String ticket_type_array[] = new String[size];

        for(int i = 0; i < size; i++) {
            ticket_type_array[i] = ticketTypeList.get(i).type_name;
        }

        return ticket_type_array;
    }

//    //TODO
//    public Activity(String name, String folder, String image, String brief_description, String description, String description_image, String status, String date_time, Date start_data_time, Date end_date_time, String time_zone, String venue, String organizer, boolean bname, boolean btel, boolean bgender, boolean bage, boolean baddress) {
//        this.id = UUID.randomUUID().toString();
//        this.create_date_time = new Date();
//        this.name = name;
//        this.folder = folder;
//        this.image = image;
//        this.brief_description = brief_description;
//        this.description = description;
//        this.description_image = description_image;
//        //this.closed = closed;
//        this.status = status;
//        this.deleted = false;
//        this.date_time = date_time;
//
//        this.start_date_time = start_data_time;
//        this.end_date_time = end_date_time;
//        this.time_zone = time_zone;
//
//        this.venue = venue;
//        this.organizer = organizer;
//        this.bname = bname;
//        this.btel = btel;
//        this.bage = bage;
//        this.bgender = bgender;
//        this.baddress = baddress;
//        this.ticket_type_list = new ArrayList<TicketType>();
//
//        try {
//            this.save();
//        } catch (Exception e) {
//            Logger.error("Create activity failed: " + e.getLocalizedMessage());
//        }
//    }
//
//    //previous version of activity, creating the activity WITH the ticket type
//    /*
//    public Activity(String name, String folder, String image, String brief_description, String description, String description_image, boolean closed, String date_time, Date start_data_time, Date end_date_time, String time_zone, String venue, String organizer, ArrayList<String> category, ArrayList<String> quantity, ArrayList<String> booked, ArrayList<String> price, boolean bname, boolean btel, boolean bgender, boolean bage, boolean baddress) {
//        this.id = UUID.randomUUID().toString();
//        this.create_date_time = new Date();
//        this.name = name;
//        this.folder = folder;
//        this.image = image;
//        this.brief_description = brief_description;
//        this.description = description;
//        this.description_image = description_image;
//        this.closed = closed;
//        this.deleted = false;
//        this.date_time = date_time;
//
//        this.start_date_time = start_data_time;
//        this.end_date_time = end_date_time;
//        this.time_zone = time_zone;
//
//        this.venue = venue;
//        this.organizer = organizer;
//        this.bname = bname;
//        this.btel = btel;
//        this.bage = bage;
//        this.bgender = bgender;
//        this.baddress = baddress;
//        try {
//            this.save();
//        } catch (Exception e) {
//            Logger.error("Create activity failed: " + e.getLocalizedMessage());
//        }
//
//        try {
//            this.ticket_type_list = new ArrayList<TicketType>();
//            int ticket_type_list_length = quantity.size();
//            for (int i = 0; i < ticket_type_list_length; i++) {
//                TicketType newTicketType = new TicketType(this, category.get(i), Integer.parseInt(quantity.get(i)), Integer.parseInt(booked.get(i)), Double.parseDouble(price.get(i)), "CAD");
//                this.ticket_type_list.add(newTicketType);
//            }
//        } catch (Exception e) {
//            Logger.error("Create associated ticket type failed: " + e.getLocalizedMessage());
//        }
//    }
//    */
//
//    //TODO
//    public void updateActivity(String name, String folder, String image, String brief_description, String description, String description_image, String status, String date_time, Date start_data_time, Date end_date_time, String time_zone, String venue, String organizer, boolean bname, boolean btel, boolean bgender, boolean bage, boolean baddress) {
//        this.name = name;
//        this.folder = folder;
//        this.image = image;
//        this.brief_description = brief_description;
//        this.description = description;
//        this.description_image = description_image;
//        //this.closed = closed;
//        this.status = status;
//        this.deleted = false;
//        this.date_time = date_time;
//
//        this.start_date_time = start_data_time;
//        this.end_date_time = end_date_time;
//        this.time_zone = time_zone;
//
//        this.venue = venue;
//        this.organizer = organizer;
//        this.bname = bname;
//        this.btel = btel;
//        this.bage = bage;
//        this.bgender = bgender;
//        this.baddress = baddress;
//        try {
//            this.update();
//        } catch (Exception e) {
//            Logger.error("Update activity failed. ", e);
//        }
//    }
//
//    //TODO
//    public void deleteImage() {
//        this.image = null;
//        try {
//            this.update();
//        } catch (Exception e) {
//            Logger.error("Delete image failed: " + e.getLocalizedMessage());
//        }
//    }
//
//    //TODO
//    public void deleteDescriptionImage() {
//        this.description_image = null;
//        try {
//            this.update();
//        } catch (Exception e) {
//            Logger.error("Delete description image failed: " + e.getLocalizedMessage());
//        }
//    }
//
//    //TODO
//    public void addActivityTicketType() {
//
//        //add new ticket type
//        try {
//            TicketType newTicketType = new TicketType(this);
//            this.ticket_type_list.add(newTicketType);
//
//        } catch (Exception e) {
//            Logger.error("Create associated ticket type failed: " + e.getLocalizedMessage());
//        }
//    }
//
//    //TODO
//    public void deleteActivity() {
//        //delete associated ticket type
//        deleteAssociatedTicketType();
//        //delete associated tickets
//        deleteAssociatedTicket();
//        //delete the activity
//        this.deleted = true;
//        try {
//            this.update();
//        } catch (Exception e) {
//            Logger.error("Delete activity failed: " + e.getLocalizedMessage());
//        }
//    }
//
//    //TODO
//    public void deleteAssociatedTicketType() {
//        try {
//            List<TicketType> old_ticket_type_list = TicketType.find.where().eq("activity_id", this.id).findList();
//            if (old_ticket_type_list != null) {
//                int old_ticket_type_list_length = old_ticket_type_list.size();
//                for (int i = 0; i < old_ticket_type_list_length; i++) {
//                    old_ticket_type_list.get(i).deleteTicketType();
//                }
//            }
//        } catch (Exception e) {
//            Logger.error("Delete associated ticket type failed: " + e.getLocalizedMessage());
//        }
//    }
//
//    //TODO
//    public void deleteAssociatedTicket() {
//        try {
//            List<Ticket> associate_ticket_list = Ticket.find.where().eq("activity_id", this.id).findList();
//            if (associate_ticket_list != null) {
//                int associate_ticket_list_length = associate_ticket_list.size();
//                for (int i = 0; i < associate_ticket_list_length; i++) {
//                    associate_ticket_list.get(i).deleteTicket();
//                }
//            }
//        } catch (Exception e) {
//            Logger.error("Delete associated ticket type failed: " + e.getLocalizedMessage());
//        }
//    }
//
//    //TODO
//    public List<TicketType> getTicketTypeList() {
//
//        return TicketType.find.where().eq("activity_id", this.id).eq("deleted", false).orderBy("price desc").findList();
//    }
//
//    //TODO
//    public List<Ticket> getTicketList() {
//
//        return Ticket.find.where().eq("activity_id", this.id).eq("deleted", false).findList();
//    }

}