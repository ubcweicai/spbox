package annotation;


import models.User;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import views.html.login;

public class Authenticated extends play.mvc.Action.Simple {
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {

        if(UserContext.GetCurrentUser() == null){
            Result unauthorized = Results.ok(login.render("登录"));
            return F.Promise.pure(unauthorized);
        }

        return delegate.call(ctx);
    }
}
