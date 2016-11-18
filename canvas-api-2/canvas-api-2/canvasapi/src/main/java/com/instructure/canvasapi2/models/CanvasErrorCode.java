package com.instructure.canvasapi2.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

import okhttp3.ResponseBody;

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
public class CanvasErrorCode implements Parcelable {

    private int code;
    private ResponseBody error;

    public CanvasErrorCode(int code, ResponseBody error) {
        this.code = code;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ResponseBody getError() {
        return error;
    }

    public void setError(ResponseBody error) {
        this.error = error;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.code);
        try {
            dest.writeByteArray(this.error.bytes());
        } catch (IOException e) {}
    }

    protected CanvasErrorCode(Parcel in) {
        this.code = in.readInt();
        try {
            in.readByteArray(this.error.bytes());
        } catch (IOException e) {}
    }

    public static final Creator<CanvasErrorCode> CREATOR = new Creator<CanvasErrorCode>() {
        @Override
        public CanvasErrorCode createFromParcel(Parcel source) {
            return new CanvasErrorCode(source);
        }

        @Override
        public CanvasErrorCode[] newArray(int size) {
            return new CanvasErrorCode[size];
        }
    };
}
