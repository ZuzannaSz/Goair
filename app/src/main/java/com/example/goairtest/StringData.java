package com.example.goairtest;

public class StringData {
    private String longitude;
    private String latitude;
    private String pollution;
    private int id;
    private String date;
    private String userId;
    public StringData(){}
    public StringData(String userId, String longitude, String latitude, String pollution, String date) {
        this.userId=userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pollution = pollution;
        this.date = date;
    }
    public StringData(int id, String userId, String longitude, String latitude, String pollution, String date) {
        this.id=id;
        this.userId=userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pollution = pollution;
        this.date = date;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setPollution(String pollution) {
        this.pollution = pollution;
    }

    public int getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getPollution() {
        return pollution;
    }

    public void setId(int id) {
        this.id = id;
    }
}
