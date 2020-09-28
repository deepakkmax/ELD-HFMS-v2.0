package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 7/21/2017.
 */

public class DispatchDetailBean {
    String appointmentNo, appointmentDate, containerNo, containerSize, containerType, containerGrade, manufacturingDate, sealNo1, sealNo2, documentPath;
    int dispatchDetailId, dispatchId, pickDropId, maxPayLoad, tareWeight, maxGrossWeight;

    public String getAppointmentNo() {
        return appointmentNo;
    }

    public void setAppointmentNo(String appointmentNo) {
        this.appointmentNo = appointmentNo;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getContainerNo() {
        return containerNo;
    }

    public void setContainerNo(String containerNo) {
        this.containerNo = containerNo;
    }

    public String getContainerSize() {
        return containerSize;
    }

    public void setContainerSize(String containerSize) {
        this.containerSize = containerSize;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public String getContainerGrade() {
        return containerGrade;
    }

    public void setContainerGrade(String containerGrade) {
        this.containerGrade = containerGrade;
    }

    public int getDispatchDetailId() {
        return dispatchDetailId;
    }

    public void setDispatchDetailId(int dispatchDetailId) {
        this.dispatchDetailId = dispatchDetailId;
    }

    public int getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(int dispatchId) {
        this.dispatchId = dispatchId;
    }

    public int getPickDropId() {
        return pickDropId;
    }

    public void setPickDropId(int pickDropId) {
        this.pickDropId = pickDropId;
    }

    public String getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(String manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

    public String getSealNo1() {
        return sealNo1;
    }

    public void setSealNo1(String sealNo1) {
        this.sealNo1 = sealNo1;
    }

    public String getSealNo2() {
        return sealNo2;
    }

    public void setSealNo2(String sealNo2) {
        this.sealNo2 = sealNo2;
    }

    public int getMaxPayLoad() {
        return maxPayLoad;
    }

    public void setMaxPayLoad(int maxPayLoad) {
        this.maxPayLoad = maxPayLoad;
    }

    public int getTareWeight() {
        return tareWeight;
    }

    public void setTareWeight(int tareWeight) {
        this.tareWeight = tareWeight;
    }

    public int getMaxGrossWeight() {
        return maxGrossWeight;
    }

    public void setMaxGrossWeight(int maxGrossWeight) {
        this.maxGrossWeight = maxGrossWeight;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }
}
