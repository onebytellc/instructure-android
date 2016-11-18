package com.instructure.loginapi.login.api.zendesk.utilities;

import android.content.Context;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.loginapi.login.BuildConfig;
import com.instructure.loginapi.login.api.zendesk.model.ZendeskTicket;
import com.instructure.loginapi.login.api.zendesk.model.ZendeskTicketData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ZendeskAPI {

    interface ZendeskInterface {
        @POST("/tickets.json")
        Call<ZendeskTicketData> postZendeskTicket(@Body ZendeskTicket ticket);
    }

    public static void postZendeskTicket(Context context, ZendeskTicket zendeskTicket, StatusCallback<ZendeskTicketData> callback) {
        //Append user info to ticket body
        String ticketBody = zendeskTicket.getTicket().getComment().getBody();
        zendeskTicket.getTicket().getComment().setBody(ticketBody + getUserDataString(context));
        ZendeskRestAdapter.buildAdapter().create(ZendeskInterface.class).postZendeskTicket(zendeskTicket).enqueue(callback);
    }

    private static String getUserDataString(Context context) {
        String res = "\n\n\n";
        User signedInUser = APIHelper.getCacheUser(context);
        boolean isAnonymousDomain = APIHelper.getDomain(context).endsWith(BuildConfig.ANONYMOUS_SCHOOL_DOMAIN);

        String versionNumber = "";
        try {
            versionNumber = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + " (" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode + ")";
        } catch (Exception ignore) {
        }

        if (!isAnonymousDomain && signedInUser != null) {
            res += "User ID   : " + Long.toString(signedInUser.getId()) + "\n";
            res += "User Name : " + signedInUser.getName() + "\n";
            res += "Email     : " + signedInUser.getPrimaryEmail() + "\n";
            res += "Hostname  : " + APIHelper.getDomain(context) + "\n";
            res += "Version   : " + versionNumber;
        } else {
            res += "Hostname  : " + APIHelper.getDomain(context) + "\n";
            res += "Version   : " + versionNumber;
        }

        return res;
    }
}
