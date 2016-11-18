package com.instructure.loginapi.login.api.zendesk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ZendeskTicket implements Parcelable, Serializable{

    private ZendeskTicketData ticket;

    public ZendeskTicketData getTicket() {
        return ticket;
    }
    public void setTicket(ZendeskTicketData ticket) {
        this.ticket = ticket;
    }

    public ZendeskTicket(){}
    public ZendeskTicket(Parcel in){
        this.ticket = in.readParcelable(ZendeskTicketData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(ticket, flags);
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new ZendeskTicket(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new ZendeskTicket[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
