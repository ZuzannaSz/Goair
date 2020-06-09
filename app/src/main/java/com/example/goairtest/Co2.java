package com.example.goairtest;

public class Co2 {
    private String IdBiker;
    private String Key;
    private String Co2;
    private String TimeStamp;
    public Co2(){}
    public Co2(String IdBiker, String Key, String Co2, String TimeStamp){
        this.IdBiker = IdBiker;
        this.Co2 = Co2;
        this.Key=Key;
        this.TimeStamp = TimeStamp;
    }

    public String getKey() {
        return Key;
    }

    public String getIdBiker() {
        return IdBiker;
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

    public String getCo2() {
        return Co2;
    }

    public void setCo2(String co2) {
        Co2 = co2;
    }
}
