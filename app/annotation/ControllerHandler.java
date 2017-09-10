package annotation;


import models.User;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;


public class ControllerHandler extends play.mvc.Action.Simple {

    public F.Promise<Result> call(Http.Context ctx) throws Throwable {

        // This is common entrance for those action without @With

        /*
        String method = (String)ctx.args.get("ROUTE_ACTION_METHOD");
        if(method != null){
            Logger.debug("Calling " + method);
        }*/

        return delegate.call(ctx);
    }
}
