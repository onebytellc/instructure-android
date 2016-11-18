package com.instructure.loginapi.login;

public interface URLSigninInterface{
    public void handleNightlyBuilds();
    public void refreshWidgets();
    public void deleteCachedFiles();
    public String getUserAgent();
    public int getRootLayout();
    public boolean shouldShowHelpButton();
}