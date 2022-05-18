package no.ntnu.wearablememoryaugmentation.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.vuzix.connectivity.sdk.Connectivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.viewModel.HomeViewModel;
import no.ntnu.wearablememoryaugmentation.viewModel.SettingsViewModel;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private SettingsViewModel settingsViewModel;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private FirebaseAnalytics firebaseAnalytics;
    private Boolean isOn;
    private String device;

    String[] cuingModes;
    String[] timings;
    String[] notifications;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        cuingModes = getResources().getStringArray(R.array.cuingModes);
        timings = getResources().getStringArray(R.array.timings);
        notifications = getResources().getStringArray(R.array.notifications);
        isOn = sharedPref.getBoolean("isOn", true);
        device = sharedPref.getString("cuingMode", "Phone");

        settingsViewModel.getLoggedOutMutableLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedOut) {
                if (loggedOut){
                    Navigation.findNavController(getView()).navigate(R.id.action_settingsFragment_to_loginRegisterFragment);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Spinner cuingSpinner = (Spinner) view.findViewById(R.id.cuing_spinner);
        cuingSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> cuingAdapter = ArrayAdapter.createFromResource(inflater.getContext(),
                R.array.cuingModes, R.layout.spinner);
        cuingAdapter.setDropDownViewResource(R.layout.spinner_item);
        cuingSpinner.setAdapter(cuingAdapter);
        int cuingPosition = cuingAdapter.getPosition(sharedPref.getString("cuingMode", "Phone"));
        cuingSpinner.setSelection(cuingPosition);

        Spinner timingSpinner = (Spinner) view.findViewById(R.id.timing_spinner);
        timingSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> timingAdapter = ArrayAdapter.createFromResource(inflater.getContext(),
                R.array.timings, R.layout.spinner);
        timingAdapter.setDropDownViewResource(R.layout.spinner_item);
        timingSpinner.setAdapter(timingAdapter);
        int timingPosition = timingAdapter.getPosition(sharedPref.getString("timings", "15 minutes"));
        timingSpinner.setSelection(timingPosition);

        Spinner notificationsSpinner = (Spinner) view.findViewById(R.id.notifications_spinner);
        notificationsSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> notificationsAdapter = ArrayAdapter.createFromResource(inflater.getContext(),
                R.array.notifications, R.layout.spinner);
        notificationsAdapter.setDropDownViewResource(R.layout.spinner_item);
        notificationsSpinner.setAdapter(notificationsAdapter);
        int notPosition = notificationsAdapter.getPosition(sharedPref.getString("notifications", "On"));
        notificationsSpinner.setSelection(notPosition);

        Spinner cueSetSpinner = (Spinner) view.findViewById(R.id.cueSet_spinner);
        cueSetSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> cueSetAdapter = ArrayAdapter.createFromResource(inflater.getContext(),
                R.array.cueSet, R.layout.spinner);
        cueSetAdapter.setDropDownViewResource(R.layout.spinner_item);
        cueSetSpinner.setAdapter(cueSetAdapter);
        int cueSetPosition = cueSetAdapter.getPosition(sharedPref.getString("cueSet", "cues"));
        cueSetSpinner.setSelection(cueSetPosition);

        TextView logOutButton = view.findViewById(R.id.log_out_button);

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsViewModel.logOut();
                WorkManager
                        .getInstance(getContext())
                        .cancelAllWork();
                Bundle params = new Bundle();
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "settings");
                firebaseAnalytics.logEvent("logOut", params);
            }
        });

        View backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_settingsFragment_to_homeFragment);
            }
        });

        View homeButton = view.findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_settingsFragment_to_homeFragment);
            }
        });

        TextView participantId = view.findViewById(R.id.participantId);
        participantId.setText(sharedPref.getString("participantId", "Id not set"));

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String name;
        String value = String.valueOf(adapterView.getItemAtPosition(i));
        Log.e("VALUE", value);
        if(ArrayUtils.contains(cuingModes, value)){
            if (!Connectivity.get(getContext()).isAvailable() && value.equals("Glasses")) {
                Toast.makeText(getContext(), "Glasses not available", Toast.LENGTH_SHORT).show();
                return;
            }
            name = "cuingMode";
            firebaseAnalytics.setUserProperty("Device", value);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "ChangeDevice");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
        }
        else if (ArrayUtils.contains(timings, value) && !value.equals(sharedPref.getString("timings", "null"))){
            name = "timings";
            if(isOn) {
                PeriodicWorkRequest nextCueRequest =
                        new PeriodicWorkRequest.Builder(HomeFragment.CueWorker.class, HomeFragment.getRepeatInterval(value), TimeUnit.MINUTES)
                                // Constraints
                                .setInitialDelay(HomeFragment.getRepeatInterval(value), TimeUnit.MINUTES)
                                .build();
                WorkManager.getInstance(getContext())
                        .enqueueUniquePeriodicWork("cueWork", ExistingPeriodicWorkPolicy.REPLACE, nextCueRequest);
            }
            firebaseAnalytics.setUserProperty("Timing", value);
        }
        else if(ArrayUtils.contains(notifications, value)){
            name = "notifications";
            firebaseAnalytics.setUserProperty("Notifications", value);
        }
        else{
            name = "cueSet";
            firebaseAnalytics.setUserProperty("CueSet", value);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "ChangeCueSet");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
        }

        editor.putString(name, value);
        editor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
