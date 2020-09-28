package com.hutchsystems.hutchconnect.beans;

public class InspectionCriteriaBean {
    String title,subTitle;
    int option;// pass 1,fail 2,na 3
    int pass,fail,na;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public int getPass() {
        return pass;
    }

    public void setPass(int pass) {
        this.pass = pass;
    }

    public int getFail() {
        return fail;
    }

    public void setFail(int fail) {
        this.fail = fail;
    }

    public int getNa() {
        return na;
    }

    public void setNa(int na) {
        this.na = na;
    }
}
