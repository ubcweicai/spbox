package utilities;

import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import models.Setting;
import models.TicketType;
import models.User;
import play.Logger;

import java.util.HashMap;
import java.util.Map;

public class Payment {

    public static boolean ProcessPayment(String paymentToken, TicketType tt, User user, int quantity) {

        String productionPaymentMethod = Setting.GetConfig("production_payment_method", "true");

        if(productionPaymentMethod.equals("false"))
        {
            // Payment simulator
            Logger.debug("Simulates the payment begin: " + paymentToken);
            try {
                Thread.sleep(500); // The payment would take 0.5s to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Logger.debug("Simulates the payment end: " + paymentToken);
            return paymentToken.equals("success_token");
        }

        try {

            Logger.debug("Processing payment with token: " + paymentToken);

            String desc = Setting.GetConfig("charge_heading", "Spark Ticket - ") + user.email;
            int total = (int) Math.floor(tt.price * 100 * quantity);
            if(!tt.taxIncluded)
            {
                total += total * 0.05; // hard coded 5% tax
                //TODO: We should be able to config tax somewhere in the future
            }
            Stripe.apiKey = Setting.GetConfig("stripe_api_key_server", "sk_test_anblhfVFoPQAZbl1gNjSysyu");
            Map<String, Object> chargeParams = new HashMap<String, Object>();
            chargeParams.put("amount", total);
            chargeParams.put("currency", tt.currency);
            chargeParams.put("source", paymentToken);
            chargeParams.put("description", desc);
					/*RequestOptions options = RequestOptions
							.builder()
							.setIdempotencyKey("ChGfknMeYFunZACb")
							.build();*/

            Charge.create(chargeParams);
            Logger.debug("Charge successfully for token: " + paymentToken);
            return true;

        } catch (CardException e) {
            // Since it's a decline, CardException will be caught
            Logger.warn("Status is: " + e.getCode());
            Logger.warn("Message is: " + e.getMessage());
        } catch (InvalidRequestException e) {
            // Invalid parameters were supplied to Stripe's API
            Logger.error("Invalid request occurred: " + e.getLocalizedMessage());
        } catch (AuthenticationException e) {
            // Authentication with Stripe's API failed
            Logger.error("Authentication failed: " + e.getLocalizedMessage());
        } catch (APIConnectionException e) {
            // Network communication with Stripe failed
            Logger.error("Network error: " + e.getLocalizedMessage());
        } catch (StripeException e) {
            // Display a very generic error to the user, and maybe send
            // yourself an email
            Logger.error("General failure: " + e.getLocalizedMessage());
        } catch (Exception e) {
            // Something else happened, completely unrelated to Stripe
            Logger.error("Unknown error occurred: " + e.getLocalizedMessage());
        }

        return false;
    }

}
