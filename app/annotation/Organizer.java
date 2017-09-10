package annotation;

import models.User;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import views.html.info;
import views.html.login;

public class Organizer extends play.mvc.Action.Simple {
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {

        User user = UserContext.GetCurrentUser();
        if (user == null || user.role.equals("blocked")) {
            Result unauthorized = Results.ok(login.render("登录"));
            return F.Promise.pure(unauthorized);
        }
        if (!user.IsOrganizer() && !user.IsAdministrator()) {
            String shtml = "您不是活动组织者，无法执行该操作";
            Result notAllowed = Results.forbidden(info.render("非法操作", Html.apply(shtml)));
            return F.Promise.pure(notAllowed);
        }
        return delegate.call(ctx);
    }
}
