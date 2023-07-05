package com.example.gaodemap;

public class LocationBean {
    private double latitude;
    private double longitude;
    private String province;
    private String coordType;
    private String city;
    private String district;
    private String cityCode;
    private String adCode;
    private String address;
    private String country;
    private String road;
    private String poiName;
    private String street;
    private String streetNum;
    private String aoiName;
    private String poiid;
    private String floor;
    private String errorCode;
    private String errorInfo;
    private String locationDetail;
    private String description;
    private String locationType;
    private String conScenario;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCoordType() {
        return coordType;
    }

    public void setCoordType(String coordType) {
        this.coordType = coordType;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNum() {
        return streetNum;
    }

    public void setStreetNum(String streetNum) {
        this.streetNum = streetNum;
    }

    public String getAoiName() {
        return aoiName;
    }

    public void setAoiName(String aoiName) {
        this.aoiName = aoiName;
    }

    public String getPoiid() {
        return poiid;
    }

    public void setPoiid(String poiid) {
        this.poiid = poiid;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public String getLocationDetail() {
        return locationDetail;
    }

    public void setLocationDetail(String locationDetail) {
        this.locationDetail = locationDetail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getConScenario() {
        return conScenario;
    }

    public void setConScenario(String conScenario) {
        this.conScenario = conScenario;
    }

    @Override
    public String toString() {
        return "LocationBean{" +
                "latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", province='" + province + '\'' +
                ", coordType='" + coordType + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", cityCode='" + cityCode + '\'' +
                ", adCode='" + adCode + '\'' +
                ", address='" + address + '\'' +
                ", country='" + country + '\'' +
                ", road='" + road + '\'' +
                ", poiName='" + poiName + '\'' +
                ", street='" + street + '\'' +
                ", streetNum='" + streetNum + '\'' +
                ", aoiName='" + aoiName + '\'' +
                ", poiid='" + poiid + '\'' +
                ", floor='" + floor + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorInfo='" + errorInfo + '\'' +
                ", locationDetail='" + locationDetail + '\'' +
                ", description='" + description + '\'' +
                ", locationType='" + locationType + '\'' +
                ", conScenario='" + conScenario + '\'' +
                '}';
    }
}
