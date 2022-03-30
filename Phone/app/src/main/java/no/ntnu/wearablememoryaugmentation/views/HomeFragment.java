package no.ntnu.wearablememoryaugmentation.views;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vuzix.connectivity.sdk.Connectivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.model.Cue;
import no.ntnu.wearablememoryaugmentation.viewModel.HomeViewModel;

public class HomeFragment extends Fragment {

    private TextView loggedInUserTextView;
    private View settingsButton;
    private View on_off_button;
    private Boolean isOn;
    private HomeViewModel homeViewModel;
    private TextView previousButton;
    private TextView nextButton;
    private int cueNum;
    private String device;
    private String repeatInterval;
    private Fragment cardFragment;
    private FragmentTransaction transaction;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private int notificationId = 0;
    private static final String CUE_TEXT = "CueText";
    private static final String CUE_INFO = "CueInfo";
    private static final String ACTION_SEND = "no.ntnu.wearablememoryaugmentation.SEND";

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        createNotificationChannel();

        sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        cueNum = sharedPref.getInt("cueNum", 0);
        repeatInterval = sharedPref.getString("timing", "Random");
        isOn = sharedPref.getBoolean("isOn", true);
        device = sharedPref.getString("cuingMode", "Phone");
        Log.e("ISON", isOn.toString());

        if (!Connectivity.get(getContext()).isAvailable()) {
            Toast.makeText(getContext(), "Glasses not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isOn) {
            homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
            homeViewModel.getUserMutableLiveData().observe(this, new Observer<FirebaseUser>() {
                @Override
                public void onChanged(FirebaseUser firebaseUser) {
                    if (firebaseUser != null) {
                        loggedInUserTextView.setText("Logged in user: " + firebaseUser.getEmail());
                    }
                }
            });

            homeViewModel.getLoggedOutMutableLiveData().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean loggedOut) {
                    if (loggedOut) {
                        Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_loginRegisterFragment);
                    }
                }
            });
        }

        if(isOn) {

            //String timing = sharedPref.getString("timing", "Random");
            PeriodicWorkRequest nextCueRequest =
                    new PeriodicWorkRequest.Builder(CueWorker.class, getRepeatInterval(repeatInterval), TimeUnit.MINUTES)
                            // Constraints
                            .setInitialDelay(getRepeatInterval(repeatInterval), TimeUnit.MINUTES)
                            .build();

        /*WorkRequest nextCueRequest =
                new OneTimeWorkRequest.Builder(CueWorker.class)
                        .build();*/

            WorkManager
                    .getInstance(getContext())
                    .enqueueUniquePeriodicWork("cueWork", ExistingPeriodicWorkPolicy.KEEP, nextCueRequest);


            WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(nextCueRequest.getId()).observe(this, workInfo -> {
                if (workInfo.getState().isFinished()) {
                    Log.e("NEW CUE", String.valueOf(cueNum));
                    newCue();
                }
            });
        }
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "HOME");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        loggedInUserTextView = view.findViewById(R.id.fragment_loggedin_loggedInUser);
                loggedInUserTextView.setVisibility(View.GONE);
        settingsButton = view.findViewById(R.id.settingsButton);
        View cardOff = view.findViewById(R.id.card_off_visibility);
        on_off_button = view.findViewById(R.id.on_off_button);
        previousButton = view.findViewById(R.id.previousButton);
        nextButton = view.findViewById(R.id.nextButton);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_settingsFragment);
            }
        });


        if (isOn) {
            newCue();
            previousButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
            Log.e("DEVICE", device);
            setDeviceIcon();
        } else {
            cardOff.setVisibility(View.VISIBLE);
            previousButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            on_off_button.setBackgroundResource(R.drawable.ic_on_button_red);
        }


        on_off_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOn = !isOn;
                editor.putBoolean("isOn", isOn);
                editor.commit();
                if (isOn) {
                    PeriodicWorkRequest nextCueRequest =
                            new PeriodicWorkRequest.Builder(HomeFragment.CueWorker.class, getRepeatInterval(repeatInterval), TimeUnit.MINUTES)
                                    // Constraints
                                    .setInitialDelay(getRepeatInterval(repeatInterval), TimeUnit.MINUTES)
                                    .build();
                    WorkManager.getInstance(getContext())
                            .enqueueUniquePeriodicWork("cueWork", ExistingPeriodicWorkPolicy.REPLACE, nextCueRequest);
                    newCue();
                    cardOff.setVisibility(View.GONE);
                    setDeviceIcon();
                    previousButton.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    transaction = getChildFragmentManager().beginTransaction();
                    transaction.remove(cardFragment).commit();
                    cardOff.setVisibility(View.VISIBLE);
                    WorkManager
                            .getInstance(getContext())
                            .cancelAllWork();
                    on_off_button.setBackgroundResource(R.drawable.ic_on_button_red);
                    previousButton.setVisibility(View.GONE);
                    nextButton.setVisibility(View.GONE);
                }
            }
        });

        on_off_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String[] colors = {"Phone", "Smartwatch", "Smart-glasses"};

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom));
                builder.setTitle("Pick device");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        switch (which){
                            case 1:
                                on_off_button.setBackgroundResource(R.drawable.ic_watch);
                                device = "Watch";
                                break;
                            case 2:
                                on_off_button.setBackgroundResource(R.drawable.ic_glasses);
                                device = "Glasses";
                                sendCue();
                                break;
                            default:
                                on_off_button.setBackgroundResource(R.drawable.ic_on_button);
                                device = "Phone";
                                break;
                        }
                        editor.putString("cuingMode", device);
                        editor.commit();
                    }
                });
                builder.show();
                return true;
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cueNum = sharedPref.getInt("cueNum", 0);
                cueNum -= 1;
                newCue();
                if(device.equals("Glasses")){
                    sendCue();
                }
                else{
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "previous");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cueNum = sharedPref.getInt("cueNum", 0);
                cueNum += 1;
                newCue();
                if(device.equals("Glasses")){
                    sendCue();
                }
                else {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "next");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                }
            }
        });

        return view;
    }

    private void setDeviceIcon(){
        switch (device){
            case "Watch":
                on_off_button.setBackgroundResource(R.drawable.ic_watch);
                break;
            case "Glasses":
                on_off_button.setBackgroundResource(R.drawable.ic_glasses);
                break;
            default:
                on_off_button.setBackgroundResource(R.drawable.ic_on_button);
        }
    }

    private void newCue() {
        cardFragment = new CardFragment(cueNum);
        transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.card_placeholder, cardFragment).commit();
    }

    public static int getRepeatInterval(String timing) {
        switch (timing) {
            case "Random":
                int randomInt = ThreadLocalRandom.current().nextInt(15, 120 + 1);
                Log.e("RANDOMINT", String.valueOf(randomInt));
                return randomInt;
            case "15 minutes":
                return 15;
            case "30 minutes":
                return 30;
            case "1 hour":
                return 60;
            default:
                return 120;

        }
    }

    private NotificationCompat.Builder createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "cueChannel")
                .setSmallIcon(R.drawable.ic_arrow_right)
                .setContentTitle("New Cue")
                .setContentText("Here is the cue")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Cues";
            String description = "New cue";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("cueChannel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendCue() {
        Log.e("DEVICES", String.valueOf(Connectivity.get(getContext()).isConnected()));
        String cue = sharedPref.getString("currentCue", "No cue");
        String cueInfo = sharedPref.getString("currentInfo", "No cue");
        Intent sendIntent = new Intent(ACTION_SEND);
        sendIntent.setPackage("no.ntnu.wearablememoryaugmentation");
        sendIntent.putExtra(CUE_TEXT, cue);
        sendIntent.putExtra(CUE_INFO, cueInfo);
        Connectivity.get(getContext()).sendBroadcast(sendIntent);
        Log.e("SEND", "SENT");
    }

    public static class CueWorker extends Worker {
        private Context context;
        private int notificationId = 0;
        private SharedPreferences sharedPref;
        private SharedPreferences.Editor editor;
        private int cueNum;
        private String nextCueText;
        private String nextCueInfo;
        private String device;
        private FirebaseAnalytics firebaseAnalytics;
        //private String nextCue;


        public CueWorker(
                @NonNull Context context,
                @NonNull WorkerParameters params) {
            super(context, params);
            this.context = context;
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            editor = sharedPref.edit();
            cueNum = sharedPref.getInt("cueNum", 0) + 1;
            device = sharedPref.getString("cuingMode", "Phone");

        }

        private void fetchCues(){
            final FirebaseDatabase database = FirebaseDatabase.getInstance("https://wearable-memory-augmentation-default-rtdb.europe-west1.firebasedatabase.app");
            final DatabaseReference dbRef = database.getReference("cues");
            int cueNumFB = cueNum + 1;
            dbRef.child(String.valueOf(cueNumFB)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());

                    }
                    else {
                        if(task.getResult().getValue() == null){
                            nextCueText = "Finished with all cues";
                            nextCueInfo = nextCueText;
                        }
                        else{
                            Log.e("FIREBASERESULTS", String.valueOf(task.getResult().getValue()));
                            Cue nextCue = task.getResult().getValue(Cue.class);
                            nextCueText = nextCue.cue;
                            nextCueInfo = nextCue.info;
                            Log.e("CUE", nextCue.cue);
                        }
                    }
                }
            });
            //cueListMutableLiveData = new MutableLiveData<ArrayList<Cue>>();
        }

        private NotificationCompat.Builder createNotification() {
            //String newCue = sharedPref.getString("currentCue", "New Cue");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "cueChannel")
                    .setSmallIcon(R.drawable.ic_logo_big)
                    .setContentTitle(nextCueText)
                    .setContentText("Tap to see cue!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(contentIntent);

            return builder;
        }

        public void sendCue() {
            Intent sendIntent = new Intent(ACTION_SEND);
            sendIntent.setPackage("no.ntnu.wearablememoryaugmentation");
            sendIntent.putExtra(CUE_TEXT, nextCueText);
            sendIntent.putExtra(CUE_INFO, nextCueInfo);
            Connectivity.get(context).sendBroadcast(sendIntent);
            Log.e("SEND", "SENT");
        }

        @Override
        public Result doWork() {
            fetchCues();

            editor.putInt("cueNum", cueNum);
            editor.putString("currentCue", nextCueText);
            editor.putString("currentInfo", nextCueInfo);
            editor.commit();

            if(device.equals("Glasses")){
                sendCue();
            }

            String notifications = sharedPref.getString("notifications", "On");
            if (notifications.equals("On")) {
                notificationId += 1;
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(notificationId, createNotification().build());

            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());

            if(!device.equals("Glasses")) {
                Bundle params = new Bundle();
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "worker");
                params.putString("cueLength", String.valueOf(nextCueText.length()));
                params.putString("cueInfoLength", String.valueOf(nextCueInfo.length()));
                params.putString("received", formatter.format(date));
                firebaseAnalytics.logEvent("receiveNewCue", params);
            }

            return Result.success();
        }
    }

}
