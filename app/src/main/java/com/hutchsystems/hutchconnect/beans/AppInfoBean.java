package com.hutchsystems.hutchconnect.beans;

import java.util.Date;

public class AppInfoBean {

    long versionCode;
    Date versionDate;
    String versionName;
    boolean mandatoryUpdateFg;

    public boolean isMandatoryUpdateFg() {
        return mandatoryUpdateFg;
    }

    public void setMandatoryUpdateFg(boolean mandatoryUpdateFg) {
        this.mandatoryUpdateFg = mandatoryUpdateFg;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}

