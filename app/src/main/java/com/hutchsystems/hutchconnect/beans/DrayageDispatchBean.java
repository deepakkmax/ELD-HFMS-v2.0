package com.hutchsystems.hutchconnect.beans;

import java.util.ArrayList;

/**
 * Created by Deepak Sharma on 7/21/2017.
 */

public class DrayageDispatchBean {
    int dispatchId, driverId, noOfContainer, status;
    String dispatchDate, dispatchNo, bookingNo, customer, customerAddress, customerLatitude, customerLongitude, steamshipLineCompany, sslAddress, sslLatitude, sslLongitude, pickupCompany, pickupAddress, pickupLatitude,
            pickupLongitude, dropCompany, dropAddress, dropLatitude, dropLongitude, emptyReturnCompany, emptyReturnAddress, emptyReturnLatitude, emptyReturnLongitude, notes;

    ArrayList<DispatchDetailBean> dispatchDetailBean;

    public int getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(int dispatchId) {
        this.dispatchId = dispatchId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getNoOfContainer() {
        return noOfContainer;
    }

    public void setNoOfContainer(int noOfContainer) {
        this.noOfContainer = noOfContainer;
    }

    public String getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(String dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public String getDispatchNo() {
        return dispatchNo;
    }

    public void setDispatchNo(String dispatchNo) {
        this.dispatchNo = dispatchNo;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerLatitude() {
        return customerLatitude;
    }

    public void setCustomerLatitude(String customerLatitude) {
        this.customerLatitude = customerLatitude;
    }

    public String getCustomerLongitude() {
        return customerLongitude;
    }

    public void setCustomerLongitude(String customerLongitude) {
        this.customerLongitude = customerLongitude;
    }

    public String getSteamshipLineCompany() {
        return steamshipLineCompany;
    }

    public void setSteamshipLineCompany(String steamshipLineCompany) {
        this.steamshipLineCompany = steamshipLineCompany;
    }

    public String getSslAddress() {
        return sslAddress;
    }

    public void setSslAddress(String sslAddress) {
        this.sslAddress = sslAddress;
    }

    public String getSslLatitude() {
        return sslLatitude;
    }

    public void setSslLatitude(String sslLatitude) {
        this.sslLatitude = sslLatitude;
    }

    public String getSslLongitude() {
        return sslLongitude;
    }

    public void setSslLongitude(String sslLongitude) {
        this.sslLongitude = sslLongitude;
    }

    public String getPickupCompany() {
        return pickupCompany;
    }

    public void setPickupCompany(String pickupCompany) {
        this.pickupCompany = pickupCompany;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(String pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public String getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(String pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public String getDropCompany() {
        return dropCompany;
    }

    public void setDropCompany(String dropCompany) {
        this.dropCompany = dropCompany;
    }

    public String getDropAddress() {
        return dropAddress;
    }

    public void setDropAddress(String dropAddress) {
        this.dropAddress = dropAddress;
    }

    public String getDropLatitude() {
        return dropLatitude;
    }

    public void setDropLatitude(String dropLatitude) {
        this.dropLatitude = dropLatitude;
    }

    public String getDropLongitude() {
        return dropLongitude;
    }

    public void setDropLongitude(String dropLongitude) {
        this.dropLongitude = dropLongitude;
    }

    public String getEmptyReturnCompany() {
        return emptyReturnCompany;
    }

    public void setEmptyReturnCompany(String emptyReturnCompany) {
        this.emptyReturnCompany = emptyReturnCompany;
    }

    public String getEmptyReturnAddress() {
        return emptyReturnAddress;
    }

    public void setEmptyReturnAddress(String emptyReturnAddress) {
        this.emptyReturnAddress = emptyReturnAddress;
    }

    public String getEmptyReturnLatitude() {
        return emptyReturnLatitude;
    }

    public void setEmptyReturnLatitude(String emptyReturnLatitude) {
        this.emptyReturnLatitude = emptyReturnLatitude;
    }

    public String getEmptyReturnLongitude() {
        return emptyReturnLongitude;
    }

    public void setEmptyReturnLongitude(String emptyReturnLongitude) {
        this.emptyReturnLongitude = emptyReturnLongitude;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ArrayList<DispatchDetailBean> getDispatchDetailBean() {
        return dispatchDetailBean;
    }

    public void setDispatchDetailBean(ArrayList<DispatchDetailBean> dispatchDetailBean) {
        this.dispatchDetailBean = dispatchDetailBean;
    }

    public String getBookingNo() {
        return bookingNo;
    }

    public void setBookingNo(String bookingNo) {
        this.bookingNo = bookingNo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
