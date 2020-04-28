package com.reward.reward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.reward.reward.utils.GeofenceRecieverService;
import com.reward.reward.utils.SharedPrefs;

public class TimerActivity extends AppCompatActivity {

    private TextView timerText;
    private TextView walletText;
    private long timeLeft;

    private Context mContext;
    private SharedPrefs mPrefs;

    private BroadcastReceiver mReceiver;
    private IntentFilter filter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer);
        mContext = this;
        mPrefs = new SharedPrefs(mContext);

        timerText = findViewById(R.id.timer_text);
        walletText = findViewById(R.id.wallet_text);

        filter = new IntentFilter();
        filter.addAction("service.to.activity.transfer");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent != null){
                    timeLeft = intent.getLongExtra("time.in.millisecond", 0);
                    setTimerText();
                }
            }
        };
        registerReceiver(mReceiver, filter);

        startGeofenceService();

    }
    private void setTimerText(){
        int minutes = (int) timeLeft / 60000;
        int seconds = (int) (timeLeft % 60000)/1000;

        String timeLeftText;
        timeLeftText = ""+minutes+":";
        if (seconds<10){
            timeLeftText += "0";
        }
        timeLeftText += seconds;
        timerText.setText(timeLeftText);
        if(minutes == 9){
            setWalletText();
        }
    }

    private void setWalletText(){
        String text = (new StringBuilder()).append(getResources().getString(R.string.wallet_text))
                .append(mPrefs.getWalletBalance()).toString();
        walletText.setText(text);
    }

    private void startGeofenceService(){
        Intent intent = new Intent(this, GeofenceRecieverService.class);
        intent.putExtra("USER_INSIDE_GEOFENCE", true);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        registerReceiver(mReceiver, filter);
    }
}
