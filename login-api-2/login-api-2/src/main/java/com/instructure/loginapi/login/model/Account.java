package com.instructure.loginapi.login.model;

import android.content.Context;
import android.text.TextUtils;

import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.util.Const;

import java.util.ArrayList;
import java.util.List;

public class Account {

    public String name = "";
    public String domain = "";
    public Locations[] locations = null;
    public android.location.Location currentLocation = null;
    public Locations closestSchoolLocation = null;
    public float distanceInMeters = 0;
    public double distanceInMiles = 0;

    public String getDistanceString(Context context) {

        if(domain != null && domain.equals(Const.URL_CANVAS_NETWORK)) {
            return context.getString(R.string.loginRightBehindYou);
        } else {
            final String distance = String.valueOf(distanceInMiles);
            if(TextUtils.isEmpty(distance)) {
                return "";
            }

            int dec = distance.indexOf('.');
            String subString = distance.substring(0, Math.min(distance.length(), dec + 2));
            return String.format(context.getString(R.string.loginMiles), subString);
        }
    }

    public static ArrayList<AccountDomain> scrubList(List<AccountDomain> accounts) {

        if(accounts == null) {
            return new ArrayList<>();
        }

        final ArrayList<AccountDomain> alteredAccounts = new ArrayList<>();

        for(AccountDomain account : accounts) {

            if(account.getDistance() != null && account.getDistance()* 0.000621371192237334 < 51.0) {
                alteredAccounts.add(account);
            }
        }
        return alteredAccounts;
    }
}
