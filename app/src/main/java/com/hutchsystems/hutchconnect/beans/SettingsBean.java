package com.hutchsystems.hutchconnect.beans;

import java.io.Serializable;

public class SettingsBean implements Serializable {
    private int Unit = 2; //1-Metric, 2-Imperial
    private int id;
    private float timeZone;
    private int defaultRule;
    private int graphLine; //0: current time, 1: end
    private int colorLineUS;
    private int colorLineCanada;
    private int timeFormat = 0; //0: 12 hrs, 1: 24hrs
    private int violationReading = 1; //0: disable, 1: enable (default)
    private int messageReading; //0: disable, 1: enable (default)
    private String startTime = "12"; //always 12AM
    private int orientation = 0; //0: auto, 1: port, 2: landscape
    private int visionMode; //0: day, 1: night, 2: Auto
    //private int brightness;
    private int copyTrailer = 1; //0 disable, 1 enable
    private int showViolation = 0; //0: off, 1: on
    private int syncTime = AppSettings.SYNC5; //5, 10, 20, 30, 60 mins
    private int automaticRuleChange = 0;
    private int violationOnGrid = 0; //0: off, 1: on

    private int fontSize = 1; //0: small, 1: normal, 2: large
    private int driverId;
    private int dutyStatusReading = 1; //0: disable, 1: enable (default)
    private int drivingScreen = 0; // 0:gauge Cluster, 1: navigation
    private int vehicleOdometerUnit = 1;

    private int autoEventInterval = 5;
    private int autoEventAfterStop = 4;

    private int showAlertSplitSlip = 1;//0 disable, 1 enable
    private int enableSplitSlip = 1;//0 disable, 1 enable


    private int setDriverScreen = 2;//0 HosWithGraph ,1 HosWithEngineHours ,2 GauageCluster
    private int supportLanguage = 1;//1 English,2 Punjabi,3 Spanish 4 French

    private int ECUConnectivity = 1; // 1-Bluetooth, 2- USB

    private int violationOnDrivingScreen = 0;

    private int dEditFg = 0;

    private int changeUnitOnRuleChange=0;
    private int newsplitSleepUSA=0;

    public int getNewsplitSleepUSA() {
        return newsplitSleepUSA;
    }

    public void setNewsplitSleepUSA(int newsplitSleepUSA) {
        this.newsplitSleepUSA = newsplitSleepUSA;
    }

    public int getChangeUnitOnRuleChange() {
        return changeUnitOnRuleChange;
    }

    public void setChangeUnitOnRuleChange(int changeUnitOnRuleChange) {
        this.changeUnitOnRuleChange = changeUnitOnRuleChange;
    }

    public int getdEditFg() {
        return dEditFg;
    }

    public void setdEditFg(int dEditFg) {
        this.dEditFg = dEditFg;
    }

    public int getViolationOnDrivingScreen() {
        return violationOnDrivingScreen;
    }

    public void setViolationOnDrivingScreen(int violationOnDrivingScreen) {
        this.violationOnDrivingScreen = violationOnDrivingScreen;
    }

    public int getECUConnectivity() {
        return ECUConnectivity;
    }

    public void setECUConnectivity(int ECUConnectivity) {
        this.ECUConnectivity = ECUConnectivity;
    }

    public int getSupportLanguage() {
        return supportLanguage;
    }

    public void setSupportLanguage(int supportLanguage) {
        this.supportLanguage = supportLanguage;
    }

    public int getDashboardDesign() {
        return setDriverScreen;
    }

    public void setDashboardDesign(int setDashboardDesign) {
        this.setDriverScreen = setDashboardDesign;
    }

    private int locationSource = 1; //0 sygic,1 textFile,2 Google


    public int getVehicleOdometerUnit() {
        return vehicleOdometerUnit;
    }

    public void setVehicleOdometerUnit(int vehicleOdometerUnit) {
        this.vehicleOdometerUnit = vehicleOdometerUnit;
    }

    public int getAutoEventInterval() {
        return autoEventInterval;
    }

    public void setAutoEventInterval(int autoEventInterval) {
        this.autoEventInterval = autoEventInterval;
    }

    public int getAutoEventAfterStop() {
        return autoEventAfterStop;
    }

    public void setAutoEventAfterStop(int autoEventAfterStop) {
        this.autoEventAfterStop = autoEventAfterStop;
    }

    public int getAutoChangeStatus() {
        return autoChangeStatus;
    }

    public void setAutoChangeStatus(int autoChangeStatus) {
        this.autoChangeStatus = autoChangeStatus;
    }

    private int autoChangeStatus = 1;

    public int getDrivingScreen() {
        return drivingScreen;
    }

    public void setDrivingScreen(int drivingScreen) {
        this.drivingScreen = drivingScreen;
    }

    public int getDutyStatusReading() {
        return dutyStatusReading;
    }

    public void setDutyStatusReading(int value) {
        dutyStatusReading = value;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int value) {
        fontSize = value;
    }


    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int value) {
        driverId = value;
    }

    public int getViolationOnGrid() {
        return violationOnGrid;
    }

    public void setViolationOnGrid(int value) {
        violationOnGrid = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int value) {
        id = value;
    }

    public float getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(float value) {
        timeZone = value;
    }

    public int getDefaultRule() {
        return defaultRule;
    }

    public void setDefaultRule(int value) {
        defaultRule = value;
    }

    public int getGraphLine() {
        return graphLine;
    }

    public void setGraphLine(int value) {
        graphLine = value;
    }

    public int getColorLineUS() {
        return colorLineUS;
    }

    public void setColorLineUS(int value) {
        colorLineUS = value;
    }

    public int getColorLineCanada() {
        return colorLineCanada;
    }

    public void setColorLineCanada(int value) {
        colorLineCanada = value;
    }

    public int getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(int value) {
        timeFormat = value;
    }

    public int getViolationReading() {
        return violationReading;
    }

    public void setViolationReading(int value) {
        violationReading = value;
    }

    public int getMessageReading() {
        return messageReading;
    }

    public void setMessageReading(int value) {
        messageReading = value;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int value) {
        orientation = value;
    }

    public int getVisionMode() {
        return visionMode;
    }

    public void setVisionMode(int value) {
        visionMode = value;
    }

//	public int getBrightness() {
//		return brightness;
//	}
//
//	public void setBrightness(int value) {
//		brightness = value;
//	}

    public int getCopyTrailer() {
        return copyTrailer;
    }

    public void setCopyTrailer(int value) {
        copyTrailer = value;
    }

    public int getShowViolation() {
        return showViolation;
    }

    public void setShowViolation(int value) {
        showViolation = value;
    }

    public int getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(int value) {
        syncTime = value;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String value) {
        startTime = value;
    }

    public int getAutomaticRuleChange() {
        return automaticRuleChange;
    }

    public void setAutomaticRuleChange(int value) {
        automaticRuleChange = value;
    }

    public int getUnit() {
        return Unit;
    }

    public void setUnit(int unit) {
        Unit = unit;
    }

    public int getShowAlertSplitSlip() {
        return showAlertSplitSlip;
    }

    public void setShowAlertSplitSlip(int showAlertSplitSlip) {
        this.showAlertSplitSlip = showAlertSplitSlip;
    }

    public int getEnableSplitSlip() {
        return enableSplitSlip;
    }

    public void setEnableSplitSlip(int enableSplitSlip) {
        this.enableSplitSlip = enableSplitSlip;
    }

    public int getLocationSource() {
        return locationSource;
    }

    public void setLocationSource(int locationSource) {
        this.locationSource = locationSource;
    }
}
