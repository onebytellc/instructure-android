/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.candroid.model;

public class DomainVerificationResult {

    // Success      = 0
    // Other        = 1 # generic "you aren't authorized cuz i said so"
    // BadSite      = 2 # ['domain'] isn't authorized for mobile apps
    // BadUserAgent = 3 # the user agent given wasn't recognized
    public enum DomainVerificationCode{Success,GeneralError,DomainNotAuthorized,UnknownUserAgent,UnknownError};

    private boolean authorized;
    private int result;
    private String client_id;
    private String api_key;
    private String client_secret;
    private String base_url;

    //Generated from the base-url
    private String protocol;


    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public DomainVerificationCode getResult() {
        switch (result) {
            case 0:
                //Success
                return DomainVerificationCode.Success;
            case 1:
                // general error
                return DomainVerificationCode.GeneralError;
            case 2:
                // unauthorized domain
                return DomainVerificationCode.DomainNotAuthorized;
            case 3:
                // bad user agent
                return DomainVerificationCode.UnknownUserAgent;
            default:
                // send an unknown error
                return DomainVerificationCode.UnknownError;
        }
    }

    public void setResult(DomainVerificationCode domainVerificationCode) {
        switch (domainVerificationCode){
            case Success:
                result = 0;
                break;
            case GeneralError:
                result = 1;
                break;
            case DomainNotAuthorized:
                result = 2;
                break;
            case UnknownUserAgent:
                result = 3;
                break;
            case UnknownError:
                result = 4;
                break;
        }

    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    private void checkBaseURL() {
        if (base_url.contains("://")) {
            String api_protocol = "https";
            //check if it's http or https
            if (base_url != null) {
                String[] components = base_url.split("://");
                if (components.length == 2) {
                    api_protocol = components[0];
                    base_url = components[1];
                } else {
                    api_protocol = "https";
                }
            }
            protocol = api_protocol;
        }
    }

    public String getBase_url() {
        checkBaseURL();
        return base_url;
    }

    public String getProtocol(){
        checkBaseURL();
        return protocol;
    }

    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }
}
