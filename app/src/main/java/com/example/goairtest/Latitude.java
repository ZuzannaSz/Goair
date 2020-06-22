package com.example.goairtest;

public class Latitude {
    private String Key;
    private String IdBiker;
    private String Latitude;
    private String TimeStamp;
    public Latitude(){}
    public Latitude(String Key, String IdBiker, String Latitude, String TimeStamp) {
        this.IdBiker=IdBiker;
        this.Key=Key;
        this.Latitude = Latitude;
        this.TimeStamp = TimeStamp;
    }
    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getIdBiker() {
        return IdBiker;
    }

    public String getKey() {
        return Key;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setIdBiker(String idBiker) {
        IdBiker = idBiker;
    }

    public void setKey(String key) {
        Key = key;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

}
