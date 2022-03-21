package com.e.memoryblade;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vuzix.hud.actionmenu.ActionMenuActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends ActionMenuActivity {
    private MenuItem HelloMenuItem;
    private MenuItem VuzixMenuItem;
    private MenuItem BladeMenuItem;
    private TextView mainText;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
    }

    public boolean onCreateActionMenu(@NotNull Menu menu) {
        super.onCreateActionMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu, menu);
        this.HelloMenuItem = menu.findItem(R.id.item1);
        this.VuzixMenuItem = menu.findItem(R.id.item2);
        this.BladeMenuItem = menu.findItem(R.id.item3);
        //this.mainText = this.findViewById(R.id.mainTextView);
        this.updateMenuItems();
        return true;
    }

    protected boolean alwaysShowActionMenu() {
        return false;
    }

    private void updateMenuItems() {
        if (HelloMenuItem == null) {
            return;
        }
        VuzixMenuItem.setEnabled(false);
        BladeMenuItem.setEnabled(false);
    }

    //Action Menu Click events
    //This events where register via the XML for the menu definitions.
    public void showHello(MenuItem item){

        showToast("Hello World!");
        mainText.setText("Hello World!");
        VuzixMenuItem.setEnabled(true);
        BladeMenuItem.setEnabled(true);
    }

    public void showVuzix(MenuItem item){
        showToast("Vuzix!");
        mainText.setText("Vuzix!");
    }

    public void showBlade(MenuItem item){
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
}
