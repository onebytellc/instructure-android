package com.instructure.loginapi.login.api.zendesk.model;


import android.os.Parcel;
import android.os.Parcelable;

public class ZendeskRequester implements Parcelable {

    /**
     * requester":{"name":"The Customer", "email":"thecustomer@domain.com"
     */
    private String name;
    private String email;
    private int locale_id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getLocale_id() {
        return locale_id;
    }

    public void setLocale_id(int locale_id) {
        this.locale_id = locale_id;
    }

    public ZendeskRequester(String name, String email, int locale_id) {
        this.name = name;
        this.email = email;
        this.locale_id = locale_id;
    }

    ZendeskRequester(Parcel in){
        this.name = in.readString();
        this.email = in.readString();
        this.locale_id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeInt(locale_id);
    }

    public static Creator<ZendeskRequester> CREATOR = new Creator<ZendeskRequester>() {
        @Override
        public ZendeskRequester createFromParcel(Parcel source) {
            return new ZendeskRequester(source);
        }

        @Override
        public ZendeskRequester[] newArray(int size) {
            return new ZendeskRequester[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
