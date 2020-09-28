package com.hutchsystems.hutchconnect.beans;

import java.io.Serializable;

public class CTPATInspectionBean  implements Serializable {
    int id;
    String dateTime;
    int driverId;
    String driverName;
    int companyId;
    String companyName;
    int Type,SyncFg,StatusId;
    String inspectionCriteria;
    String inspectionResult;
    String truckNumber;
    String trailer;

    public int getid() {
        return id;
    }

    public void setId(int _id) {
        this.id = _id;
    }

    public String getSealValue() {
        return sealValue;
    }

    public void setSealValue(String sealValue) {
        this.sealValue = sealValue;
    }

    String comments,sealValue;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getSyncFg() {
        return SyncFg;
    }

    public void setSyncFg(int syncFg) {
        SyncFg = syncFg;
    }

    public int getStatusId() {
        return StatusId;
    }

    public void setStatusId(int statusId) {
        StatusId = statusId;
    }

    public String getInspectionCriteria() {
        return inspectionCriteria;
    }

    public void setInspectionCriteria(String inspectionCriteria) {
        this.inspectionCriteria = inspectionCriteria;
    }

    public String getInspectionResult() {
        return inspectionResult;
    }

    public void setInspectionResult(String inspectionResult) {
        this.inspectionResult = inspectionResult;
    }

    public String getTruckNumber() {
        return truckNumber;
    }

    public void setTruckNumber(String truckNumber) {
        this.truckNumber = truckNumber;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
