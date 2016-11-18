package com.instructure.loginapi.login.model;

public class CourseColorDataSync {

    //The Canvas Context ID.
    public long contextId;

    //The HEX VALUE AARRGGBB of a color.
    public String color;

    public CourseColorDataSync(){}

    public CourseColorDataSync(long contextId, String color) {
        this.contextId = contextId;
        this.color = color;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append(" - CANVAS ID: ");
        builder.append(contextId);
        builder.append("\n");
        builder.append(" - CANVAS COLOR: ");
        builder.append(color);

        return builder.toString();
    }
}
