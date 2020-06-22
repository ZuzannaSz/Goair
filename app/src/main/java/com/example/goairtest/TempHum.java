package com.example.goairtest;

import java.sql.Timestamp;

public class TempHum {
    private int temperature;
    private int humidity;
    private int ID;
    private String date;
    private String update;
    public  TempHum(){}
    public TempHum(int id, int temperature, int humidity, String date, String update) {
        this.humidity=humidity;
        this.temperature = temperature;
        this.ID = id;
        this.date = date;
        this.update = update;
    }
    public TempHum( int temperature, int humidity, String date, String update) {
        this.temperature = temperature;
        this.humidity= humidity;
        this.date = date;
        this.update = update;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
}
