package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 6/8/2017.
 */

public class DocumentDetailBean {
    //_id INTEGER PRIMARY KEY AUTOINCREMENT, DocumentType INTEGER, DocumentPath text, Remarks text, VehicleId INTEGER,DriverId INTEGER,CompanyId INTEGER,CreatedDate text";
    int _id, documentType, driverId;
    String documentPath;
    String documentNo;

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    String createdDate;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getDocumentType() {
        return documentType;
    }

    public void setDocumentType(int documentType) {
        this.documentType = documentType;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }
}
