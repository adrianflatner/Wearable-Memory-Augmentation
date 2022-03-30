package no.ntnu.wearablememoryaugmentation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.vuzix.connectivity.sdk.Connectivity;
import com.vuzix.connectivity.sdk.Device;
import com.vuzix.hud.actionmenu.ActionMenuActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

import no.ntnu.wearablememoryaugmentation.R;

public class MainActivity extends ActionMenuActivity {
    private MenuItem CueMenuItem;
    private MenuItem InfoMenuItem;
    private MenuItem TutorialMenuItem;
    private TextView mainText;
    private View tutorialView;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private FirebaseAnalytics firebaseAnalytics;

    private static final String ACTION_SEND = "no.ntnu.wearablememoryaugmentation.SEND";
    private static final String ACTION_GET = "no.ntnu.wearablememoryaugmentation.GET";

    private static final String CUE_TEXT = "CueText";
    private static final String CUE_INFO = "CueInfo";

    private static final int CUE_SIZE = 30;
    private static final int CUE_INFO_SIZE = 24;

    private String currentCue;
    private String currentInfo;

    private int state = 0;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        sharedPref = getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        // check for Connectivity framework
        if (!Connectivity.get(this).isAvailable()) {
            Toast.makeText(this, "Not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.e("DEVICES", String.valueOf(Connectivity.get(getContext()).getDevices()));
        }
        mainText = findViewById(R.id.mainText);
        tutorialView = findViewById(R.id.tutorial);
        currentCue = sharedPref.getString("currentCue", "Connect with phone");
        currentInfo = sharedPref.getString("currentInfo", "Connect with phone");

        mainText.setText(currentCue);
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

    public boolean onCreateActionMenu(@NotNull Menu menu) {
        super.onCreateActionMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu, menu);
        this.CueMenuItem = menu.findItem(R.id.item1);
        this.InfoMenuItem = menu.findItem(R.id.item2);
        this.TutorialMenuItem = menu.findItem(R.id.item3);
        this.updateMenuItems();
        return true;
    }

    protected boolean alwaysShowActionMenu() {
        return false;
    }

    private void updateMenuItems() {
        if (CueMenuItem == null) {
            return;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                state = 1;
                setMainText();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                state = 0;
                setMainText();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    //Action Menu Click events
    //This events where register via the XML for the menu definitions.
    public void showCue(MenuItem item) {
        state = 0;
        switchView();
        setMainText();
        closeActionMenu(false);
        updateMenuItems();
    }

    public void showInfo(MenuItem item) {
        state = 1;
        switchView();
        setMainText();
        closeActionMenu(true);

    }

    public void showTutorial(MenuItem item) {
        state = 2;
        switchView();
        closeActionMenu(true);
    }

    private void switchView() {
        if (state == 2) {
            mainText.setVisibility(View.GONE);
            tutorialView.setVisibility(View.VISIBLE);
        } else {
            mainText.setVisibility(View.VISIBLE);
            tutorialView.setVisibility(View.GONE);
        }
    }

    private void setMainText() {
        if (state == 0) {
            mainText.setText(currentCue);
            mainText.setTextSize(CUE_SIZE);
        } else {
            mainText.setText(currentInfo);
            mainText.setTextSize(CUE_INFO_SIZE);
        }
    }

    private void showToast(final String text) {

        final Activity activity = this;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendClicked(View view) {
        Intent sendIntent = new Intent(ACTION_SEND);
        sendIntent.setPackage("no.ntnu.wearablememoryaugmentation");
        sendIntent.putExtra(CUE_TEXT, mainText.getText().toString());
        Connectivity.get(this).sendBroadcast(sendIntent);
        mainText.setText(null);
    }

    public void getRemoteDeviceModelClicked(View view) {
        Connectivity connectivity = Connectivity.get(this);
        Device device = connectivity.getDevice();
        if (device != null) {
            Intent getIntent = new Intent(ACTION_GET);
            getIntent.setPackage("no.ntnu.wearablememoryaugmentation");
            connectivity.sendOrderedBroadcast(device, getIntent, new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String model = getResultData();
                    if (model != null) {
                        Toast.makeText(context, model, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Connectivity.get(context).verify(intent, "no.ntnu.wearablememoryaugmentation")) {
                String cueText = intent.getStringExtra(CUE_TEXT);
                if (cueText != null) {
                    editor.putString("currentCue", cueText);
                    editor.commit();
                    currentCue = cueText;
                }

                String cueInfo = intent.getStringExtra(CUE_INFO);
                if (cueInfo != null) {
                    editor.putString("currentInfo", cueInfo);
                    editor.commit();
                    currentInfo = cueInfo;
                }
                setMainText();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                Date date = new Date(System.currentTimeMillis());

                Bundle params = new Bundle();
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "worker");
                params.putString("cueLength", String.valueOf(cueText.length()));
                params.putString("cueInfoLength", String.valueOf(cueInfo.length()));
                params.putString("received", formatter.format(date));
                firebaseAnalytics.logEvent("receiveNewCue", params);

                PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock mWakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "wakeLock");
                mWakeLock.acquire();
                mWakeLock.release();
            }
        }
    };
}
