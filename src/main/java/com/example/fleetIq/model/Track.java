package com.example.fleetIq.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "imei", nullable = false, length = 15)
    private String imei;

    @Column(name = "gpstime", nullable = false)
    private Long gpstime;

    @Column(name = "hearttime")
    private Long hearttime;

    @Column(name = "systemtime")
    private Long systemtime;

    @Column(name = "servertime")
    private Long servertime;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "speed")
    private Double speed;

    @Column(name = "course")
    private Double course;

    @Column(name = "acctime")
    private Long acctime;

    @Column(name = "accstatus")
    private Boolean accstatus;

    @Column(name = "doorstatus")
    private Integer doorstatus;

    @Column(name = "chargestatus")
    private Integer chargestatus;

    @Column(name = "oilpowerstatus")
    private Integer oilpowerstatus;

    @Column(name = "defencestatus")
    private Integer defencestatus;

    @Column(name = "datastatus")
    private Integer datastatus;

    @Column(name = "battery")
    private Double battery;

    @Column(name = "mileage")
    private Long mileage;

    @Column(name = "todaymileage")
    private Long todaymileage;

    @Column(name = "externalpower", length = 10)
    private String externalpower;

    @Column(name = "fuel", length = 10)
    private String fuel;

    @Column(name = "fueltime")
    private Long fueltime;

    @Column(name = "temperature", length = 255)
    private String temperature;

    @Column(name = "temperaturetime")
    private Long temperaturetime;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }
    public Long getGpstime() { return gpstime; }
    public void setGpstime(Long gpstime) { this.gpstime = gpstime; }
    public Long getHearttime() { return hearttime; }
    public void setHearttime(Long hearttime) { this.hearttime = hearttime; }
    public Long getSystemtime() { return systemtime; }
    public void setSystemtime(Long systemtime) { this.systemtime = systemtime; }
    public Long getServertime() { return servertime; }
    public void setServertime(Long servertime) { this.servertime = servertime; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    public Double getCourse() { return course; }
    public void setCourse(Double course) { this.course = course; }
    public Long getAcctime() { return acctime; }
    public void setAcctime(Long acctime) { this.acctime = acctime; }
    public Boolean getAccstatus() { return accstatus; }
    public void setAccstatus(Boolean accstatus) { this.accstatus = accstatus; }
    public Integer getDoorstatus() { return doorstatus; }
    public void setDoorstatus(Integer doorstatus) { this.doorstatus = doorstatus; }
    public Integer getChargestatus() { return chargestatus; }
    public void setChargestatus(Integer chargestatus) { this.chargestatus = chargestatus; }
    public Integer getOilpowerstatus() { return oilpowerstatus; }
    public void setOilpowerstatus(Integer oilpowerstatus) { this.oilpowerstatus = oilpowerstatus; }
    public Integer getDefencestatus() { return defencestatus; }
    public void setDefencestatus(Integer defencestatus) { this.defencestatus = defencestatus; }
    public Integer getDatastatus() { return datastatus; }
    public void setDatastatus(Integer datastatus) { this.datastatus = datastatus; }
    public Double getBattery() { return battery; }
    public void setBattery(Double battery) { this.battery = battery; }
    public Long getMileage() { return mileage; }
    public void setMileage(Long mileage) { this.mileage = mileage; }
    public Long getTodaymileage() { return todaymileage; }
    public void setTodaymileage(Long todaymileage) { this.todaymileage = todaymileage; }
    public String getExternalpower() { return externalpower; }
    public void setExternalpower(String externalpower) { this.externalpower = externalpower; }
    public String getFuel() { return fuel; }
    public void setFuel(String fuel) { this.fuel = fuel; }
    public Long getFueltime() { return fueltime; }
    public void setFueltime(Long fueltime) { this.fueltime = fueltime; }
    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }
    public Long getTemperaturetime() { return temperaturetime; }
    public void setTemperaturetime(Long temperaturetime) { this.temperaturetime = temperaturetime; }
}