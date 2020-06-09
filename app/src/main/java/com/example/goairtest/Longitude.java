package com.example.goairtest;

public class Longitude {
    private String IdBiker;
    private String Key;
    private String Longitude;
    private String TimeStamp;
    public Longitude(){}
    public Longitude(String IdBiker, String Key, String Longitude, String TimeStamp)
    {
        this.IdBiker=IdBiker;
        this.Key=Key;
        this.Longitude = Longitude;
        this.TimeStamp = TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public void setKey(String key) {
        Key = key;
    }

    public void setIdBiker(String idBiker) {
        IdBiker = idBiker;
    }
    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getIdBiker() {
        return IdBiker;
    }

    public String getKey() {
        return Key;
    }

    public String getLongitude() {
        return Longitude;
    }
}
