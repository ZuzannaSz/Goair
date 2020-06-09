package com.example.goairtest;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    static String getJson(Context context, String file)
    {
        String myString;
        try{
            InputStream stream= context.getAssets().open(file);
            int size = stream.available();
            byte[] buff = new byte[size];
            stream.read(buff);
            stream.close();
            myString = new String(buff, "UTF-8");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return myString;
    }
}
