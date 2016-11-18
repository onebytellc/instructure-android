package com.instructure.loginapi.login.api.zendesk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ZendeskComment implements Parcelable{

    private long id;
    private String body;
    @SerializedName("public")
    private boolean isPublic;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public boolean isPublic() {
        return isPublic;
    }
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public ZendeskComment(){}
    public ZendeskComment(long id, String body, boolean isPublic){
        this.id = id;
        this.body = body;
        this.isPublic = isPublic;
    }

    ZendeskComment(Parcel in){
        this.id = in.readLong();
        this.body = in.readString();
        this.isPublic = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(body);
        dest.writeByte(isPublic ? (byte) 1 : (byte) 0);
    }

    public static Creator<ZendeskComment> CREATOR = new Creator<ZendeskComment>() {
        @Override
        public ZendeskComment createFromParcel(Parcel source) {
            return new ZendeskComment(source);
        }

        @Override
        public ZendeskComment[] newArray(int size) {
            return new ZendeskComment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
