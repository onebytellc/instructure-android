package com.instructure.loginapi.login.api.zendesk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

public class ZendeskTicketData implements Parcelable, Serializable{

    private long id;
    private long submitter_id;
    private String subject;
    private ZendeskComment comment;
    private List<ZendeskCustomField> custom_fields;
    private List<String> tags;
    private ZendeskRequester requester;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getSubmitter_id() {
        return submitter_id;
    }
    public void setSubmitter_id(long submitter_id) {
        this.submitter_id = submitter_id;
    }
    public ZendeskComment getComment() {
        return comment;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public void setComment(ZendeskComment comment) {
        this.comment = comment;
    }
    public List<ZendeskCustomField> getCustom_fields() {
        return custom_fields;
    }
    public void setCustom_fields(List<ZendeskCustomField> custom_fields) {
        this.custom_fields = custom_fields;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public ZendeskRequester getRequester() {
        return requester;
    }

    public void setRequester(ZendeskRequester requester) {
        this.requester = requester;
    }

    public ZendeskTicketData(){}
    ZendeskTicketData(Parcel in){
        this.id = in.readLong();
        this.submitter_id = in.readLong();
        this.subject = in.readString();
        this.comment = in.readParcelable(ZendeskComment.class.getClassLoader());
        in.readList(this.custom_fields, ZendeskCustomField.class.getClassLoader());
        in.readList(this.tags, String.class.getClassLoader());
        this.requester = in.readParcelable(ZendeskRequester.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(submitter_id);
        dest.writeString(subject);
        dest.writeParcelable(comment, flags);
        dest.writeList(custom_fields);
        dest.writeList(tags);
        dest.writeParcelable(requester, flags);
    }
    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new ZendeskTicketData(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new ZendeskTicketData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
