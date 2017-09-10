package annotation;

import models.User;
import play.Logger;
import play.mvc.Http;

public class UserContext {

    protected static String UserContextKey = "CURRENT_AUTHENTICATED_USER";

    public static User GetCurrentUser() {

        User user = (User)Http.Context.current().args.getOrDefault(UserContextKey, null);
        if(user != null){
            return user;
        }
        String email = Http.Context.current().session().get("email");
        if (email != null)
        {
            user = User.find.byId(email);
            if(user == null) {
                Logger.warn("User no longer exists " + email);
                // User no longer exists, we should clear the session
                Http.Context.current().session().clear();
            }
            else
            {
                Http.Context.current().args.put(UserContextKey, user);
            }

        }

        return user;

    }

    public static void SetCurrentUser(User user)
    {
        if(user == null){
            Http.Context.current().session().clear();
            Http.Context.current().args.remove(UserContextKey);
        }
        else {
            Http.Context.current().session().put("email", user.email);

            Http.Context.current().args.put(UserContextKey, user);
        }
    }

}
