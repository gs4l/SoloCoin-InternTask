package com.reward.reward.utils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceRecieverService extends IntentService {

    private static final String TAG = "GeofenceRecieverService";
    private SharedPrefs mPrefs;

    private boolean USER_INSIDE_GEOFENCE = true;
    private Context mContext;

    private long timeInMillisecond = 600000; // 10 mins

    public GeofenceRecieverService(){
        super(TAG);
    }

    @Override
    public void onCreate() {

        mContext = this;
        mPrefs = new SharedPrefs(mContext);

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        USER_INSIDE_GEOFENCE = intent.getBooleanExtra("USER_INSIDE_GEOFENCE", true);
        runTimer();
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()){
            Log.e("BroadcastRecieve Error", GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode()));
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){

            USER_INSIDE_GEOFENCE = true;
        }
        else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){

            USER_INSIDE_GEOFENCE = false;
        }
    }

    private void runTimer(){
        CountDownTimer timer = new CountDownTimer(timeInMillisecond, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Intent local = new Intent();
                local.setAction("service.to.activity.transfer");
                local.putExtra("time.in.millisecond", timeInMillisecond);
                mContext.sendBroadcast(local);

            }

            @Override
            public void onFinish() {
                timeInMillisecond = 600000;
                updateWalletBalance();
                runTimer();
            }
        }.start();
    }

    private void updateWalletBalance(){
        int currentBalance = mPrefs.getWalletBalance();

        if(USER_INSIDE_GEOFENCE){
            mPrefs.setWalletBalance((currentBalance+10));
        }
        else if(!USER_INSIDE_GEOFENCE){
            mPrefs.setWalletBalance((currentBalance-10));
        }

    }
}
