package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class CarrierInfoBean {
    private int companyId, statusId, vehicleId, totalAxle, canBusFg, packageId, vehicleOdometerUnit, odometerSource, companyStatusId;
    private String CarrierName;
    private String ELDManufacturer;
    private String USDOT;
    private String UnitNo;
    private String PlateNo;
    private String VIN;
    private String MACAddress;
    private String TimeZoneId;
    private String StartOdometerReading, features, fullAddress, eldConfig, protocol;
    private boolean setupFg, latePaymentFg;
    private String referralCode, rechargeLink, referralLink;

    public int getCompanyStatusId() {
        return companyStatusId;
    }

    public void setCompanyStatusId(int companyStatusId) {
        this.companyStatusId = companyStatusId;
    }

    public boolean isLatePaymentFg() {
        return latePaymentFg;
    }

    public void setLatePaymentFg(boolean latePaymentFg) {
        this.latePaymentFg = latePaymentFg;
    }

    public String getRechargeLink() {
        return rechargeLink;
    }

    public void setRechargeLink(String rechargeLink) {
        this.rechargeLink = rechargeLink;
    }

    public String getReferralLink() {
        return referralLink;
    }

    public void setReferralLink(String referralLink) {
        this.referralLink = referralLink;
    }

    public int getVehicleOdometerUnit() {
        return vehicleOdometerUnit;
    }

    public void setVehicleOdometerUnit(int vehicleOdometerUnit) {
        this.vehicleOdometerUnit = vehicleOdometerUnit;
    }

    public String getEldConfig() {
        return eldConfig;
    }

    public void setEldConfig(String eldConfig) {
        this.eldConfig = eldConfig;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }


    public String getSerailNo() {
        return SerailNo;
    }

    public void setSerailNo(String serailNo) {
        SerailNo = serailNo;
    }

    String SerailNo;

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getCarrierName() {
        return CarrierName;
    }

    public void setCarrierName(String carrierName) {
        CarrierName = carrierName;
    }

    public String getELDManufacturer() {
        return ELDManufacturer;
    }

    public void setELDManufacturer(String ELDManufacturer) {
        this.ELDManufacturer = ELDManufacturer;
    }

    public String getUSDOT() {
        return USDOT;
    }

    public void setUSDOT(String USDOT) {
        this.USDOT = USDOT;
    }

    public String getUnitNo() {
        return UnitNo;
    }

    public void setUnitNo(String unitNo) {
        UnitNo = unitNo;
    }

    public String getVIN() {
        return VIN;
    }

    public void setVIN(String VIN) {
        this.VIN = VIN;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    public String getPlateNo() {
        return PlateNo;
    }

    public void setPlateNo(String plateNo) {
        PlateNo = plateNo;
    }

    public String getTimeZoneId() {
        return TimeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        TimeZoneId = timeZoneId;
    }

    public int getTotalAxle() {
        return totalAxle;
    }

    public void setTotalAxle(int totalAxle) {
        this.totalAxle = totalAxle;
    }

    public int getCanBusFg() {
        return canBusFg;
    }

    public void setCanBusFg(int canBusFg) {
        this.canBusFg = canBusFg;
    }

    public String getStartOdometerReading() {
        return StartOdometerReading;
    }

    public void setStartOdometerReading(String startOdometerReading) {
        StartOdometerReading = startOdometerReading;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public int getOdometerSource() {
        return odometerSource;
    }

    public void setOdometerSource(int odometerSource) {
        this.odometerSource = odometerSource;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isSetupFg() {
        return setupFg;
    }

    public void setSetupFg(boolean setupFg) {
        this.setupFg = setupFg;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
}
