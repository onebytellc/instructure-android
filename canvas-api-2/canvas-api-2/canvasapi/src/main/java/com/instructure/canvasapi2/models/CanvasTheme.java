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

package com.instructure.canvasapi2.models;

import android.os.Parcel;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;


public class CanvasTheme extends CanvasComparable<CanvasTheme> {

    @SerializedName("ic-brand-primary")
    public String icBrandPrimary;

    @SerializedName("ic-brand-button--primary-bgd")
    public String icBrandButtonPrimaryBgd;

    @SerializedName("ic-brand-button--primary-text")
    public String icBrandButtonPrimaryText;

    @SerializedName("ic-brand-button--secondary-bgd")
    public String icBrandButtonSecondaryBgd;

    @SerializedName("ic-brand-button--secondary-text")
    public String icBrandButtonSecondaryText;

    @SerializedName("ic-link-color")
    public String icLinkColor;

    @SerializedName("ic-brand-global-nav-bgd")
    public String icBrandGlobalNavBgd;

    @SerializedName("ic-brand-global-nav-ic-icon-svg-fill")
    public String icBrandGlobalNavIcIconSvgFill;

    @SerializedName("ic-brand-global-nav-ic-icon-svg-fill--active")
    public String icBrandGlobalNavIcIconSvgFillActive;

    @SerializedName("ic-brand-global-nav-menu-item__text-color")
    public String icBrandGlobalNavMenuItemTextColor;

    @SerializedName("ic-brand-global-nav-menu-item__text-color--active")
    public String icBrandGlobalNavMenuItemTextColorActive;

    @SerializedName("ic-brand-global-nav-avatar-border")
    public String icBrandGlobalNavAvatarBorder;

    @SerializedName("ic-brand-global-nav-menu-item__badge-bgd")
    public String icBrandGlobalNavMenuItemBadgeBgd;

    @SerializedName("ic-brand-global-nav-menu-item__badge-text")
    public String icBrandGlobalNavMenuItemBadgeText;

    @SerializedName("ic-brand-global-nav-logo-bgd")
    public String icBrandGlobalNavLogoBgd;

    @SerializedName("ic-brand-header-image")
    public String icBrandHeaderImage;

    @SerializedName("ic-brand-watermark")
    public String icBrandWatermark;

    @SerializedName("ic-brand-watermark-opacity")
    public String icBrandWatermarkOpacity;

    @SerializedName("ic-brand-favicon")
    public String icBrandFavicon;

    @SerializedName("ic-brand-apple-touch-icon")
    public String icBrandAppleTouchIcon;

    @SerializedName("ic-brand-msapplication-tile-color")
    public String icBrandMsapplicationTileColor;

    @SerializedName("ic-brand-msapplication-tile-square")
    public String icBrandMsapplicationTileSquare;

    @SerializedName("ic-brand-msapplication-tile-wide")
    public String icBrandMsapplicationTileWide;

    @SerializedName("ic-brand-right-sidebar-logo")
    public String icBrandRightSidebarLogo;

    @SerializedName("ic-brand-Login-body-bgd-color")
    public String icBrandLoginBodyBgdColor;

    @SerializedName("ic-brand-Login-body-bgd-image")
    public String icBrandLoginBodyBgdImage;

    @SerializedName("ic-brand-Login-body-bgd-shadow-color")
    public String icBrandLoginBodyBgdShadowColor;

    @SerializedName("ic-brand-Login-logo")
    public String icBrandLoginLogo;

    @SerializedName("ic-brand-Login-Content-bgd-color")
    public String icBrandLoginContentBgdColor;

    @SerializedName("ic-brand-Login-Content-border-color")
    public String icBrandLoginContentBorderColor;

    @SerializedName("ic-brand-Login-Content-inner-bgd")
    public String icBrandLoginContentInnerBgd;

    @SerializedName("ic-brand-Login-Content-inner-border")
    public String icBrandLoginContentInnerBorder;

    @SerializedName("ic-brand-Login-Content-inner-body-bgd")
    public String icBrandLoginContentInnerBodyBgd;

    @SerializedName("ic-brand-Login-Content-inner-body-border")
    public String icBrandLoginContentInnerBodyBorder;

    @SerializedName("ic-brand-Login-Content-label-text-color")
    public String icBrandLoginContentLabelTextColor;

    @SerializedName("ic-brand-Login-Content-password-text-color")
    public String icBrandLoginContentPasswordTextColor;
    @SerializedName("ic-brand-Login-Content-button-bgd")
    public String icBrandLoginContentButtonBgd;

    @SerializedName("ic-brand-Login-Content-button-text")
    public String icBrandLoginContentButtonText;

    @SerializedName("ic-brand-Login-footer-link-color")
    public String icBrandLoginFooterLinkColor;

    @SerializedName("ic-brand-Login-footer-link-color-hover")
    public String icBrandLoginFooterLinkColorHover;

    @SerializedName("ic-brand-Login-instructure-logo")
    public String icBrandLoginInstructureLogo;

    //region Theme Editor Conversions to Android Theme UI Language

    public String getPrimary() {
        return icBrandGlobalNavBgd;
    }

    public String getPrimaryText() {
        return icBrandGlobalNavMenuItemTextColor;
    }

    public String getAccent() {
        return icBrandGlobalNavMenuItemTextColorActive;
    }

    public String getButton() {
        return icBrandButtonPrimaryBgd;
    }

    public String getButtonText() {
        return icBrandButtonPrimaryText;
    }

    public String getLogoUrl() {
        return icBrandHeaderImage;
    }

    //endregion


    @Override
    public long getId() {
        return super.getId();
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return null;
    }

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.icBrandPrimary);
        dest.writeString(this.icBrandButtonPrimaryBgd);
        dest.writeString(this.icBrandButtonPrimaryText);
        dest.writeString(this.icBrandButtonSecondaryBgd);
        dest.writeString(this.icBrandButtonSecondaryText);
        dest.writeString(this.icLinkColor);
        dest.writeString(this.icBrandGlobalNavBgd);
        dest.writeString(this.icBrandGlobalNavIcIconSvgFill);
        dest.writeString(this.icBrandGlobalNavIcIconSvgFillActive);
        dest.writeString(this.icBrandGlobalNavMenuItemTextColor);
        dest.writeString(this.icBrandGlobalNavMenuItemTextColorActive);
        dest.writeString(this.icBrandGlobalNavAvatarBorder);
        dest.writeString(this.icBrandGlobalNavMenuItemBadgeBgd);
        dest.writeString(this.icBrandGlobalNavMenuItemBadgeText);
        dest.writeString(this.icBrandGlobalNavLogoBgd);
        dest.writeString(this.icBrandHeaderImage);
        dest.writeString(this.icBrandWatermark);
        dest.writeString(this.icBrandWatermarkOpacity);
        dest.writeString(this.icBrandFavicon);
        dest.writeString(this.icBrandAppleTouchIcon);
        dest.writeString(this.icBrandMsapplicationTileColor);
        dest.writeString(this.icBrandMsapplicationTileSquare);
        dest.writeString(this.icBrandMsapplicationTileWide);
        dest.writeString(this.icBrandRightSidebarLogo);
        dest.writeString(this.icBrandLoginBodyBgdColor);
        dest.writeString(this.icBrandLoginBodyBgdImage);
        dest.writeString(this.icBrandLoginBodyBgdShadowColor);
        dest.writeString(this.icBrandLoginLogo);
        dest.writeString(this.icBrandLoginContentBgdColor);
        dest.writeString(this.icBrandLoginContentBorderColor);
        dest.writeString(this.icBrandLoginContentInnerBgd);
        dest.writeString(this.icBrandLoginContentInnerBorder);
        dest.writeString(this.icBrandLoginContentInnerBodyBgd);
        dest.writeString(this.icBrandLoginContentInnerBodyBorder);
        dest.writeString(this.icBrandLoginContentLabelTextColor);
        dest.writeString(this.icBrandLoginContentPasswordTextColor);
        dest.writeString(this.icBrandLoginContentButtonBgd);
        dest.writeString(this.icBrandLoginContentButtonText);
        dest.writeString(this.icBrandLoginFooterLinkColor);
        dest.writeString(this.icBrandLoginFooterLinkColorHover);
        dest.writeString(this.icBrandLoginInstructureLogo);
    }

    public CanvasTheme() {
    }

    protected CanvasTheme(Parcel in) {
        this.icBrandPrimary = in.readString();
        this.icBrandButtonPrimaryBgd = in.readString();
        this.icBrandButtonPrimaryText = in.readString();
        this.icBrandButtonSecondaryBgd = in.readString();
        this.icBrandButtonSecondaryText = in.readString();
        this.icLinkColor = in.readString();
        this.icBrandGlobalNavBgd = in.readString();
        this.icBrandGlobalNavIcIconSvgFill = in.readString();
        this.icBrandGlobalNavIcIconSvgFillActive = in.readString();
        this.icBrandGlobalNavMenuItemTextColor = in.readString();
        this.icBrandGlobalNavMenuItemTextColorActive = in.readString();
        this.icBrandGlobalNavAvatarBorder = in.readString();
        this.icBrandGlobalNavMenuItemBadgeBgd = in.readString();
        this.icBrandGlobalNavMenuItemBadgeText = in.readString();
        this.icBrandGlobalNavLogoBgd = in.readString();
        this.icBrandHeaderImage = in.readString();
        this.icBrandWatermark = in.readString();
        this.icBrandWatermarkOpacity = in.readString();
        this.icBrandFavicon = in.readString();
        this.icBrandAppleTouchIcon = in.readString();
        this.icBrandMsapplicationTileColor = in.readString();
        this.icBrandMsapplicationTileSquare = in.readString();
        this.icBrandMsapplicationTileWide = in.readString();
        this.icBrandRightSidebarLogo = in.readString();
        this.icBrandLoginBodyBgdColor = in.readString();
        this.icBrandLoginBodyBgdImage = in.readString();
        this.icBrandLoginBodyBgdShadowColor = in.readString();
        this.icBrandLoginLogo = in.readString();
        this.icBrandLoginContentBgdColor = in.readString();
        this.icBrandLoginContentBorderColor = in.readString();
        this.icBrandLoginContentInnerBgd = in.readString();
        this.icBrandLoginContentInnerBorder = in.readString();
        this.icBrandLoginContentInnerBodyBgd = in.readString();
        this.icBrandLoginContentInnerBodyBorder = in.readString();
        this.icBrandLoginContentLabelTextColor = in.readString();
        this.icBrandLoginContentPasswordTextColor = in.readString();
        this.icBrandLoginContentButtonBgd = in.readString();
        this.icBrandLoginContentButtonText = in.readString();
        this.icBrandLoginFooterLinkColor = in.readString();
        this.icBrandLoginFooterLinkColorHover = in.readString();
        this.icBrandLoginInstructureLogo = in.readString();
    }

    public static final Creator<CanvasTheme> CREATOR = new Creator<CanvasTheme>() {
        @Override
        public CanvasTheme createFromParcel(Parcel source) {
            return new CanvasTheme(source);
        }

        @Override
        public CanvasTheme[] newArray(int size) {
            return new CanvasTheme[size];
        }
    };

    //endregion
}
