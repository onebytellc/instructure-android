/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi2.builders;

import android.os.Parcel;
import android.os.Parcelable;

import com.instructure.canvasapi2.models.CanvasContext;


public class RestParams implements Parcelable {

    private CanvasContext canvasContext;
    private String domain;
    private String apiVersion;
    private boolean usePerPageQueryParam;
    private boolean shouldIgnoreToken;
    private boolean forceReadFromCache;
    private boolean forceReadFromNetwork;

    private RestParams(Builder builder) {
        canvasContext = builder.innerCanvasContext;
        domain = builder.innerDomain;
        apiVersion = builder.innerAPIVersion;
        usePerPageQueryParam = builder.innerUsePerPageQueryParam;
        shouldIgnoreToken = builder.innerShouldIgnoreToken;
        forceReadFromCache = builder.innerForceReadFromCache;
        forceReadFromNetwork = builder.innerForceReadFromNetwork;
    }

    public static final class Builder {
        private CanvasContext innerCanvasContext;
        private String innerDomain;
        private String innerAPIVersion = "/api/v1/";
        private boolean innerUsePerPageQueryParam;
        private boolean innerShouldIgnoreToken;
        private boolean innerForceReadFromCache;
        private boolean innerForceReadFromNetwork;

        public Builder() {
        }

        public Builder(RestParams params) {
            innerCanvasContext = params.canvasContext;
            innerDomain = params.domain;
            innerAPIVersion = params.apiVersion;
            innerUsePerPageQueryParam = params.usePerPageQueryParam;
            innerShouldIgnoreToken = params.shouldIgnoreToken;
            innerForceReadFromCache = params.forceReadFromCache;
            innerForceReadFromNetwork = params.forceReadFromNetwork;
        }

        public Builder withCanvasContext(CanvasContext val) {
            innerCanvasContext = val;
            return this;
        }

        public Builder withDomain(String val) {
            innerDomain = val;
            return this;
        }

        public Builder withPerPageQueryParam(boolean val) {
            innerUsePerPageQueryParam = val;
            return this;
        }

        public Builder withShouldIgnoreToken(boolean val) {
            innerShouldIgnoreToken = val;
            return this;
        }

        public Builder withForceReadFromCache(boolean val) {
            innerForceReadFromCache = val;
            return this;
        }

        public Builder withForceReadFromNetwork(boolean val) {
            innerForceReadFromNetwork = val;
            return this;
        }

        public Builder withAPIVersion(String apiVersion) {
            innerAPIVersion = apiVersion;
            return this;
        }
        public RestParams build() {
            return new RestParams(this);
        }
    }


    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    public String getDomain() {
        return domain;
    }

    public String getAPIVersion() {
        if(apiVersion == null) return "";
        return apiVersion; }

    public boolean usePerPageQueryParam() {
        return usePerPageQueryParam;
    }

    public boolean shouldIgnoreToken() {
        return shouldIgnoreToken;
    }

    public boolean isForceReadFromCache() {
        return forceReadFromCache;
    }

    public boolean isForceReadFromNetwork() {
        return forceReadFromNetwork;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.canvasContext, flags);
        dest.writeString(this.domain);
        dest.writeByte(this.usePerPageQueryParam ? (byte) 1 : (byte) 0);
        dest.writeByte(this.shouldIgnoreToken ? (byte) 1 : (byte) 0);
        dest.writeByte(this.forceReadFromCache ? (byte) 1 : (byte) 0);
        dest.writeByte(this.forceReadFromNetwork ? (byte) 1 : (byte) 0);
        dest.writeString(this.apiVersion);
    }

    protected RestParams(Parcel in) {
        this.canvasContext = in.readParcelable(CanvasContext.class.getClassLoader());
        this.domain = in.readString();
        this.usePerPageQueryParam = in.readByte() != 0;
        this.shouldIgnoreToken = in.readByte() != 0;
        this.forceReadFromCache = in.readByte() != 0;
        this.forceReadFromNetwork = in.readByte() != 0;
        this.apiVersion = in.readString();
    }

    public static final Creator<RestParams> CREATOR = new Creator<RestParams>() {
        @Override
        public RestParams createFromParcel(Parcel source) {
            return new RestParams(source);
        }

        @Override
        public RestParams[] newArray(int size) {
            return new RestParams[size];
        }
    };
}
