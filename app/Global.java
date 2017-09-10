import play.*;

import models.*;
import utilities.BuiltInArticles;


import java.util.*;

public class Global extends GlobalSettings {
    
	
	@Override
    public void onStart(Application app) {

        // Force the display of datetime using Pacific Standard Time
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Logger.info("Current time " + new Date());

        /*
        //scan the user list and make them all lower case
        List<User> userList = User.find.all();
        userList.stream().forEach(user -> {
            try {
                String email_lower = user.email.toLowerCase();
                user.email = email_lower;
                user.save();
            } catch (Exception e) {
                Logger.warn("Failed to update all user emails to lower case");
            }
        });


        Logger.info("Updated all user emails to lower case");
        */

        boolean systemInited = "true".equals(Setting.GetConfig("system_inited", "false"));
        /*
		if(!systemInited) {
            System.out.println("Adding test data");

            Setting.SetConfig("system_inited", "true");
            // 6216f8a75fd5bb3d5f22b6f9958cdede3fc086c2 is hashed for password "111"
            String commonPwd = play.libs.Crypto.sign("6216f8a75fd5bb3d5f22b6f9958cdede3fc086c2");
            // String email, String name, String password, String role, String tel, String gender, int age, String address
            new User("admin@wechatgogo.com", "admin", commonPwd, "admin","1234","male", 30, "ADDR");

        }
		*/

        /*
        http://localhost:9000/api/v1/addCity2/Vancouver/Canada/US/Pacific
        http://localhost:9000/api/v1/addCity2/Toronto/Canada/Canada/Eastern
         */

        // Add built-in articles if necessary
        BuiltInArticles.GetNotice();
        BuiltInArticles.GetTerms();
        BuiltInArticles.GetNewTicketTemplate();
        BuiltInArticles.GetOrganizerTemplate();
        BuiltInArticles.GetAdminGeneratedUserConfirmTemplate();
        BuiltInArticles.GetUserConfirmTemplate();
        BuiltInArticles.GetResetPasswordTemplate();

    }
    
}