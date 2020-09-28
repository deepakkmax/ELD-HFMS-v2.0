package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 10/10/2017.
 */

public class PermissionBean {
    String label;
    String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
