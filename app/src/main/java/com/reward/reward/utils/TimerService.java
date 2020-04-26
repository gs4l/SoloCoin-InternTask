package com.reward.reward.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

public class TimerService extends Service {

    private boolean USER_INSIDE_GEOFENCE = true;

    private long timeInMillisecond = 600000; // 10 mins

    private SharedPrefs mPrefs;

    private GeofenceBroadcastReciever mReciever;

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = new SharedPrefs(this);

        mReciever = new GeofenceBroadcastReciever(){
            @Override
            public void onReceive(Context context, Intent intent) {

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
        };

        IntentFilter filter = new IntentFilter();
        this.registerReceiver(mReciever, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        runTimer();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReciever);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void runTimer(){
        CountDownTimer timer = new CountDownTimer(timeInMillisecond, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {


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
