package com.hutchsystems.hutchconnect.beans;

/**
 * Created by SAMSUNG on 30-01-2017.
 */

public class FuelDetailBean {

    int _id, VehicleId, DriverId, Duration, cardId, ModifiedBy,FuelUnit;
    String FuelDateTime, Price, DEFFuelPrice, ReferFuelPrice, CashAdvance, Images, Quantity, DEFFuelQuantity, ReferFuelQuantity, CardNo, modifiedDate, Latitude, Longitude, Location, trailerNo;

    public int getFuelUnit() {
        return FuelUnit;
    }

    public void setFuelUnit(int fuelUnit) {
        FuelUnit = fuelUnit;
    }

    public int getVehicleId() {
        return VehicleId;
    }

    public void setVehicleId(int vehicleId) {
        VehicleId = vehicleId;
    }

    public int getDriverId() {
        return DriverId;
    }

    public void setDriverId(int driverId) {
        DriverId = driverId;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getDEFFuelQuantity() {
        return DEFFuelQuantity;
    }

    public void setDEFFuelQuantity(String DEFFuelQuantity) {
        this.DEFFuelQuantity = DEFFuelQuantity;
    }

    public String getReferFuelQuantity() {
        return ReferFuelQuantity;
    }

    public void setReferFuelQuantity(String referFuelQuantity) {
        ReferFuelQuantity = referFuelQuantity;
    }

    public String getFuelDateTime() {
        return FuelDateTime;
    }

    public void setFuelDateTime(String fuelDateTime) {
        FuelDateTime = fuelDateTime;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDEFFuelPrice() {
        return DEFFuelPrice;
    }

    public void setDEFFuelPrice(String DEFFuelPrice) {
        this.DEFFuelPrice = DEFFuelPrice;
    }

    public String getReferFuelPrice() {
        return ReferFuelPrice;
    }

    public void setReferFuelPrice(String referFuelPrice) {
        ReferFuelPrice = referFuelPrice;
    }

    public String getCashAdvance() {
        return CashAdvance;
    }

    public void setCashAdvance(String cashAdvance) {
        CashAdvance = cashAdvance;
    }

    public String getImages() {
        return Images;
    }

    public void setImages(String images) {
        Images = images;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public String getCardNo() {
        return CardNo;
    }

    public void setCardNo(String cardNo) {
        CardNo = cardNo;
    }

    public int getModifiedBy() {
        return ModifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        ModifiedBy = modifiedBy;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getTrailerNo() {
        return trailerNo;
    }

    public void setTrailerNo(String trailerNo) {
        this.trailerNo = trailerNo;
    }
}
