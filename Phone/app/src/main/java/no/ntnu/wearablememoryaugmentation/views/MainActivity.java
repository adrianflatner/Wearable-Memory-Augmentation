package no.ntnu.wearablememoryaugmentation.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.vuzix.connectivity.sdk.Connectivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import no.ntnu.wearablememoryaugmentation.R;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics firebaseAnalytics;
    private static final String ACTION_SEND = "no.ntnu.wearablememoryaugmentation.SEND";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, new IntentFilter(ACTION_SEND));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());

            if (Connectivity.get(context).verify(intent, "no.ntnu.wearablememoryaugmentation")) {
                String name = intent.getStringExtra("name");
                String cue = intent.getStringExtra("extraFirst");
                String cueLength = intent.getStringExtra("extraSecond");
                String cueInfoLength = intent.getStringExtra("extraThird");
                if (name != null && cue != null && cueLength != null && cueInfoLength != null) {
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "glasses");
                    params.putString("cue", cue);
                    params.putString("cueLength", cueLength);
                    params.putString("cueInfoLength", cueInfoLength);
                    params.putString("received", formatter.format(date));
                    firebaseAnalytics.logEvent(name, params);
                }
            }
        }};
}