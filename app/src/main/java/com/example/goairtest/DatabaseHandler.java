package com.example.goairtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dataStorage";
    private static final String TABLE_DATA = "data";
    private static final String TABLE_TH = "tempHum";
    private static final String TABLE_MAP = "map";
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_POLLUTION = "pollution";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_UPDATE = "updated";
    private static final String KEY_IDB = "id_biker";

    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_HUMIDITY = "humidity";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    private static final String CREATE_TABLE_DATA = "CREATE TABLE "
            + TABLE_DATA + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LATITUDE + " DOUBLE,"
            + KEY_LONGITUDE + " DOUBLE," + KEY_ALTITUDE + " INTEGER," + KEY_POLLUTION + " INTEGER,"
            + KEY_DATE + " TEXT," + KEY_UPDATE + " TEXT" + ")";
    private static final String CREATE_TABLE_TH = "CREATE TABLE "
            + TABLE_TH + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TEMPERATURE + " INTEGER,"
            + KEY_HUMIDITY + " INTEGER," + KEY_DATE + " TEXT," + KEY_UPDATE + " TEXT" + ")";
    private static final String CREATE_TABLE_MAP = "CREATE TABLE "
            + TABLE_MAP + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_IDB + " TEXT," + KEY_LATITUDE + " TEXT," + KEY_LONGITUDE +
            " TEXT," + KEY_POLLUTION + " TEXT," + KEY_DATE + " TEXT" + ")";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_DATA);
        db.execSQL(CREATE_TABLE_TH);
        db.execSQL(CREATE_TABLE_MAP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAP);
        onCreate(db);
    }
    void addMap(StringData data) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_IDB, data.getUserId());
            values.put(KEY_LATITUDE,data.getLatitude());
            values.put(KEY_LONGITUDE, "");
            values.put(KEY_POLLUTION, "");
            values.put(KEY_DATE, data.getDate());
            db.insert(TABLE_MAP,null,values);

    }
    public List<Data>getLastMap() {
        List<Data> dataList = new ArrayList<Data>();
        String selectQuery = "SELECT "+ KEY_ID + "," + KEY_IDB + "," + KEY_LATITUDE + "," + KEY_LONGITUDE + "," + KEY_POLLUTION +
                ",MAX(" + KEY_DATE + ")" + " FROM " + TABLE_MAP + " GROUP BY " + KEY_LATITUDE + ","+ KEY_LONGITUDE ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Data data = new Data();
                data.setID(Integer.parseInt(cursor.getString(0)));
                data.setUserId(cursor.getString(1));
                data.setLatitude(Double.parseDouble(cursor.getString(2)));
                data.setLongitude(Double.parseDouble(cursor.getString(3)));
                data.setPollution(Integer.parseInt(cursor.getString(4)));
                data.setDate(cursor.getString(5));
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        db.close();
        return dataList;
    }

    void addData(Data data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, data.getLatitude());
        values.put(KEY_LONGITUDE, data.getLongitude());
        values.put(KEY_ALTITUDE, data.getAltitude());
        values.put(KEY_POLLUTION, data.getPollution());
        values.put(KEY_DATE, data.getDate());
        values.put(KEY_UPDATE, data.getUpdate());
        db.insert(TABLE_DATA, null, values);
        db.close();
}
    void addTH(TempHum tempHum)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TEMPERATURE, tempHum.getTemperature());
        values.put(KEY_HUMIDITY, tempHum.getHumidity());
        values.put(KEY_DATE, tempHum.getDate());
        values.put(KEY_UPDATE, tempHum.getUpdate());
        db.insert(TABLE_TH, null, values);
        db.close();
    }
    Data getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DATA, new String[]{KEY_ID,
                        KEY_LATITUDE, KEY_LONGITUDE, KEY_ALTITUDE, KEY_POLLUTION, KEY_DATE, KEY_UPDATE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        Data data = new Data(Integer.parseInt(cursor.getString(0)),
                Double.parseDouble(cursor.getString(1)),
                Double.parseDouble(cursor.getString(2)),
                Double.parseDouble(cursor.getString(3)),
                Integer.parseInt(cursor.getString(4)),
                cursor.getString(5),
                cursor.getString(6));

        db.close();
        return data;
    }
    TempHum getTH(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TH, new String[]{KEY_ID,
                        KEY_TEMPERATURE, KEY_HUMIDITY, KEY_DATE, KEY_UPDATE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        TempHum th = new TempHum(Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)),
                Integer.parseInt(cursor.getString(2)),
                cursor.getString(3),
                cursor.getString(4));
        db.close();
        return th;
    }
    public int getId(Data data) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Data> dataList = new ArrayList<Data>();
        String selectQuery = "SELECT * FROM " + TABLE_DATA;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                if (data.getLatitude() == Double.parseDouble(cursor.getString(1))
                        && data.getLongitude() == Double.parseDouble(cursor.getString(2))) {
                    db.close();
                    return Integer.parseInt(cursor.getString(0));
                }
            } while (cursor.moveToNext());
        }
        db.close();
        return -1;
    }
    public Data getLast() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_DATA + " ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        Data data =new Data();
        if (cursor != null)
            cursor.moveToFirst();
        if(cursor!=null)
        {
            data = new Data(Integer.parseInt(cursor.getString(0)),
                    Double.parseDouble(cursor.getString(1)),
                    Double.parseDouble(cursor.getString(2)),
                    Double.parseDouble(cursor.getString(3)),
                    Integer.parseInt(cursor.getString(4)),
                    cursor.getString(5),
                    cursor.getString(6));
        }

        db.close();
        return data;
    }
    public TempHum getLastTH() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_TH + " ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        TempHum th = new TempHum(Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)),
                Integer.parseInt(cursor.getString(2)),
                cursor.getString(3),
                cursor.getString(4));
        db.close();
        return th;
    }
    public void deleteData(Data data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATA, KEY_ID + " = ?",
                new String[]{String.valueOf(data.getID())});
        db.close();
    }
    public void deleteMap() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_MAP);
        db.close();
    }
    public List<Data> getAllData() {
        List<Data> dataList = new ArrayList<Data>();
        String selectQuery = "SELECT  * FROM " + TABLE_DATA;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Data data = new Data();
                data.setID(Integer.parseInt(cursor.getString(0)));
                data.setLatitude(Double.parseDouble(cursor.getString(1)));
                data.setLongitude(Double.parseDouble(cursor.getString(2)));
                data.setAltitude(Double.parseDouble(cursor.getString(3)));
                data.setPollution(Integer.parseInt(cursor.getString(4)));
                data.setDate(cursor.getString(5));
                data.setUpdate(cursor.getString(6));
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        db.close();
        return dataList;
    }
    public boolean clearMap() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MAP, KEY_LONGITUDE + "=? OR " + KEY_POLLUTION + "=?", new String[]{"",""}) >0;
    }
    public List<Data> getAllMapData() {
        List<Data> dataList = new ArrayList<Data>();
        String selectQuery = "SELECT  * FROM " + TABLE_MAP;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Data data = new Data();
                data.setID(Integer.parseInt(cursor.getString(0)));
                data.setUserId(cursor.getString(1));
                data.setLatitude(Double.parseDouble(cursor.getString(2)));
                data.setLongitude(Double.parseDouble(cursor.getString(3)));
                data.setPollution(Integer.parseInt(cursor.getString(4)));
                data.setDate(cursor.getString(5));
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        db.close();
        return dataList;
    }
    public StringData getMapValue(String user, String date) {
        StringData data = new StringData();
        String selectQuery = "SELECT * FROM " + TABLE_MAP + " WHERE " + KEY_IDB + " =? AND " + KEY_DATE + " =? ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery,  new String[] {user, date});
            if(cursor.moveToFirst()) {
                data.setId(cursor.getInt(0));
                data.setUserId(cursor.getString(1));
                data.setLatitude(cursor.getString(2));
                data.setLongitude(cursor.getString(3));
                data.setPollution(cursor.getString(4));
                data.setDate(cursor.getString(5));
            }
        }finally {
            if(cursor != null)
                cursor.close();
        }
        return data;
    }
    public int updateMap(StringData data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IDB, data.getUserId());
        values.put(KEY_LATITUDE, data.getLatitude());
        values.put(KEY_LONGITUDE, data.getLongitude());
        values.put(KEY_POLLUTION, data.getPollution());
        values.put(KEY_DATE, data.getDate());
        int i = db.update(TABLE_MAP, values,  KEY_ID + " = " + data.getId(), null);
        return 0;
    }
    public boolean checkDataPopulated() {
        boolean result =false;
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select exists(select 1 from " + TABLE_MAP  + ");";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        if(count==1)
        {
            result= true;
        }
        cursor.close();
        db.close();
        return result;
    }
    public void deleteAll()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + TABLE_DATA);
        db.execSQL("delete from "+ TABLE_TH);
        db.close();
    }
    public List<TempHum> getAllTH(){
        List<TempHum> thList = new ArrayList<TempHum>();
        String selectQuery = "SELECT * FROM " + TABLE_TH;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()) {
            do {
                TempHum th = new TempHum();
                th.setID(Integer.parseInt(cursor.getString(0)));
                th.setTemperature(Integer.parseInt(cursor.getString(1)));
                th.setHumidity(Integer.parseInt(cursor.getString(2)));
                th.setDate(cursor.getString(3));
                th.setUpdate(cursor.getString(4));
            }while (cursor.moveToNext());
        }
        db.close();
        return thList;
    }
     public int updateData(Data data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, Double.toString(data.getLatitude()));
        values.put(KEY_LONGITUDE, Double.toString(data.getLongitude()));
        values.put(KEY_ALTITUDE, Double.toString(data.getAltitude()));
        values.put(KEY_POLLUTION, Integer.toString(data.getPollution()));
        values.put(KEY_DATE, data.getDate());
        values.put(KEY_UPDATE, data.getUpdate());
        int i = db.update(TABLE_DATA, values,  KEY_ID + " = " + data.getID(), null);
        return 0;
    }
    public int updateTH(TempHum tempHum) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TEMPERATURE, Integer.toString(tempHum.getTemperature()));
        values.put(KEY_HUMIDITY, Integer.toString(tempHum.getHumidity()));
        values.put(KEY_DATE, tempHum.getDate());
        values.put(KEY_UPDATE, tempHum.getUpdate());
        int i = db.update(TABLE_DATA, values,  KEY_ID + " = " + tempHum.getID(), null);
        return 0;
    }
}
    /*public boolean checkExistance(Data data) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Data> dataList = new ArrayList<Data>();
        String selectQuery = "SELECT * FROM " + TABLE_DATA;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {                if (data.getLatitude() == Integer.parseInt(cursor.getString(1))
                    && data.getLongitude() == Integer.parseInt(cursor.getString(2))) {
                return true;
            }            } while (cursor.moveToNext());        }
        return false; } */