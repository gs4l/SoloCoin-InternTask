package com.reward.reward.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {

    private Context mContext;


    public SharedPrefs(Context context){
        this.mContext = context;
    }

    private float getFloatPreference(String key, float value){

        SharedPreferences mPrefs = mContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return mPrefs.getFloat(key, value);
    }

    private void setFloatPreference(String key, float value){
        SharedPreferences.Editor mEditor = mContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
        mEditor.putFloat(key, value);
        mEditor.apply();

    }

    private int getIntPreference(String key, int value){

        SharedPreferences mPrefs = mContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return mPrefs.getInt(key, value);
    }

    private void setIntPreference(String key, int value){
        SharedPreferences.Editor mEditor = mContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
        mEditor.putInt(key, value);
        mEditor.apply();

    }
    public int getWalletBalance(){
        return getIntPreference("Balance", 0);
    }

    public void setWalletBalance(int value){
        if (value <= 0) {
            setIntPreference("Balance", 0);
        }else {
            setIntPreference("Balance", value);
        }
    }

    public double getLat(){
        return getFloatPreference("Lat", 0);
    }

    public void setLat(float value){
        setFloatPreference("Lat", value);
    }

    public double getLong(){
        return getFloatPreference("Long", 0);
    }

    public void setLong(float value){
        setFloatPreference("Long", value);
    }
}