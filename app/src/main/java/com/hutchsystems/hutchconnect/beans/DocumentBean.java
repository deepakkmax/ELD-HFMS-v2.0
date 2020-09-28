package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 2/15/2017.
 */

public class DocumentBean {
    private int _id;
    private String type, path;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
