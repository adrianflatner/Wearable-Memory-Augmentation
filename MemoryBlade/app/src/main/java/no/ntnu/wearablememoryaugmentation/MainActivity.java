package no.ntnu.wearablememoryaugmentation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import no.ntnu.wearablememoryaugmentation.R;
import com.vuzix.connectivity.sdk.Connectivity;
import com.vuzix.connectivity.sdk.Device;
import com.vuzix.hud.actionmenu.ActionMenuActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainActivity extends ActionMenuActivity {
    private MenuItem CueMenuItem;
    private MenuItem InfoMenuItem;
    private MenuItem TutorialMenuItem;
    private TextView mainText;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private static final String ACTION_SEND = "no.ntnu.wearablememoryaugmentation.SEND";
    private static final String ACTION_GET = "no.ntnu.wearablememoryaugmentation.GET";

    private static final String CUE_TEXT = "CueText";
    private static final String CUE_INFO = "CueInfo";


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        sharedPref = getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        // check for Connectivity framework
        if (!Connectivity.get(this).isAvailable()) {
            Toast.makeText(this, "Not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        else{
            Toast.makeText(this, "AVAILABLE", Toast.LENGTH_SHORT).show();
            Log.e("DEVICES", String.valueOf(Connectivity.get(getContext()).getDevices()));
        }
        mainText = findViewById(R.id.mainText);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, new IntentFilter(ACTION_SEND));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    public boolean onCreateActionMenu(@NotNull Menu menu) {
        super.onCreateActionMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu, menu);
        this.CueMenuItem = menu.findItem(R.id.item1);
        this.InfoMenuItem = menu.findItem(R.id.item2);
        this.TutorialMenuItem = menu.findItem(R.id.item3);
        //this.mainText = this.findViewById(R.id.mainTextView);
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
        InfoMenuItem.setEnabled(false);
        TutorialMenuItem.setEnabled(false);
    }

    //Action Menu Click events
    //This events where register via the XML for the menu definitions.
    public void showCue(MenuItem item){
        String cue = sharedPref.getString("currentCue", "Connect with phone");
        showToast("Hello World!");
        mainText.setText(cue);
        InfoMenuItem.setEnabled(true);
        TutorialMenuItem.setEnabled(true);
    }

    public void showInfo(MenuItem item){
        showToast("Vuzix!");
        mainText.setText("Vuzix!");
    }

    public void showTutorial(MenuItem item){
        showToast("Blade");
        mainText.setText("Blade");
    }

    private void showToast(final String text){

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
                Log.e("CUE", "INCOMING");
                if (cueText != null) {
                    editor.putString("currentCue", cueText);
                    editor.commit();
                    mainText.setText(cueText);
                }

                String cueInfo = intent.getStringExtra(CUE_INFO);
                if (cueInfo != null) {
                    Toast.makeText(context, cueInfo, Toast.LENGTH_SHORT).show();
                    editor.putString("currentInfo", cueInfo);
                    editor.commit();
                    //mainText.setText(cueInfo);
                }
            }
        }
    };
}
