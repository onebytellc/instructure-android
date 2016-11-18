package com.instructure.loginapi.login.model;

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.canvasapi2.models.User;

import java.util.ArrayList;
import java.util.Date;

public class SignedInUser implements Comparable<SignedInUser>{
    public User user;
    public String domain;
    public String protocol;
    public String token;
    public ArrayList<String> calendarFilterPrefs;

    public Date lastLogoutDate;

    @Override
    public int compareTo(SignedInUser signedInUser) {
        //We want newest first.
        return -1 * CanvasComparable.compare(lastLogoutDate, signedInUser.lastLogoutDate);
    }
}