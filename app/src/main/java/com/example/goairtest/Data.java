package com.example.goairtest;

import java.sql.Timestamp;

public class Data {
   private double longitude; //długość
    private double latitude; //szerokość
    private double altitude;
    private int pollution;
    private int ID;
    private String date;
    public Data(){}
    public Data(int ID, double latitude, double longitude, double altitude, int pollution, String date)
    {
        this.ID =ID;
        this.latitude=latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.pollution = pollution;
        this.date= date;
    }
    public Data(double latitude, double longitude, double altitude, int pollution, String date)
    {
        this.latitude=latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.pollution = pollution;
        this.date = date;

    }
    public boolean dataCompare(Data a, Data b)
    {
        if(a.getLatitude()==b.getLatitude() && a.getLongitude() == b.getLongitude() && a.getAltitude()==b.getAltitude())
        {
            return true;
        }
        return false;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public double getAltitude()
    {
        return this.altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public int getPollution() {
        return this.pollution;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPollution(int pollution) {
        this.pollution = pollution;
    }
}
