package com.instructure.loginapi.login.model;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.instructure.loginapi.login.util.Const;

public class Locations {

    public double latitude;
    public double longitude;

    /**
     * Gets the last known location of a user, starting with GPS, if fails, NETWORK, if fails, OTHER PROVIDER if one exists
     * @param context
     * @return the users last location or null
     */
    public static Location getCurrentLocation(Context context) throws SecurityException {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        if(manager == null) {
            return null;
        }

        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if(location == null && manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location =  manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if(location == null && manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            location =  manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        return location;
    }

    public float calculateDistanceBetweenPoints(Account account) {

        if(account.currentLocation == null) {
            return Const.NO_LOCATION_INDICATOR_INT;
        }

        Location location = new Location("School");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return account.currentLocation.distanceTo(location);
    }
}
