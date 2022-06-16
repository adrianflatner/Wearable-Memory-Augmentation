package no.ntnu.wearablememoryaugmentation.views;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.model.Cue;
import no.ntnu.wearablememoryaugmentation.viewModel.CardViewModel;

public class CardFragment extends Fragment {
    public static final String CHANNEL_ID = "channelid";
    private NotificationManagerCompat notificationManager;

    private CardViewModel cardViewModel;
    private TextView cueText;

    //For å kunne flippe
    private String currentCueInfo = "";
    private String currentCueText = "";

    private ImageView flipButton;
    private ImageView settingsButton;
    private TextView textViewId;
    private int cueNum;
    private boolean isCue;

    private FirebaseAnalytics firebaseAnalytics;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private static final int REPETITION = 3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        createNotificationChannels();

        cueNum = sharedPref.getInt("cueNum", 0);

        cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);

        //newCue();

        notificationManager = NotificationManagerCompat.from(this.getContext());

        cardViewModel.getCueListMutableLiveData().observe(this, new Observer<ArrayList<Cue>>() {
            @Override
            public void onChanged(ArrayList<Cue> cues) {
                Log.e("CUE-cardViewModel_onChanged()", "onChanged()");
                if (cues != null) {
                    List<Integer> currentIndexes = new ArrayList<>();
                    int currentCueListLength = sharedPref.getInt("currentCueListLength", 0);
                    if (currentCueListLength != cues.size()) {
                        List<Integer> index = new ArrayList<>();
                        for (int i = 0; i < cues.size(); i++) {
                            index.add(i);
                        }
                        for (int i = 1; i <= REPETITION; i++) {
                            currentIndexes.addAll(index);
                        }
                        Collections.shuffle(currentIndexes);
                        editor.putString("cueIndexes", String.valueOf(currentIndexes)
                                .replace("[", "")
                                .replace("]", ""));
                        editor.putInt("currentCueListLength", cues.size());
                        editor.commit();
                        Log.e("cueIndexes-randomized", String.valueOf(currentIndexes));
                    } else {
                        String cueIndexes = sharedPref.getString("cueIndexes", "0");
                        Log.e("CUEINDEXES", cueIndexes);
                        currentIndexes = Stream.of(cueIndexes.split(","))
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
                    }

                    cueNum = cueNum < 0 ? -1 : cueNum;
                    cueNum = currentIndexes.size() <= cueNum ? currentIndexes.size() : cueNum;
                    editor.putInt("cueNum", cueNum);
                    if (cueNum >= currentIndexes.size() || cueNum < 0) {
                        cueText.setText("Finished with all cues");
                        //TODO her er det mulig noe må legges til
                    } else {
                        Log.e("CUENUM", String.valueOf(cueNum));
                        Log.e("CURRENTINDEX", String.valueOf(currentIndexes.get(cueNum)));
                        //Flip
                        currentCueText = cues.get(currentIndexes.get(cueNum)).cue;
                        currentCueInfo = cues.get(currentIndexes.get(cueNum)).info;
                        cueText.setText(cues.get(currentIndexes.get(cueNum)).cue);
                        cueText.setTypeface(null, Typeface.BOLD);
                        isCue = true;
                    }
                    try {
                        editor.putString("currentCue", cues.get(currentIndexes.get(cueNum)).cue);
                        Log.v("CURRENTCUE", cues.get(currentIndexes.get(cueNum)).cue);
                    } catch (Exception e) {
                        editor.putString("currentCue", "Finished with all cues");
                    }
                    editor.commit();
                }
            }
        });

        PeriodicWorkRequest nextCueRequest = new PeriodicWorkRequest.Builder(CueWorker.class, 15, TimeUnit.MINUTES)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(getContext())
                .enqueueUniquePeriodicWork("cueWork", ExistingPeriodicWorkPolicy.KEEP, nextCueRequest);

        WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(nextCueRequest.getId()).observe(this, workInfo -> {
            Log.e("WORKMANAGERNEWCUE", String.valueOf(cueNum));
        });
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = this.getContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_fragment, container, false);

        cueText = view.findViewById(R.id.cueText);
        flipButton = view.findViewById(R.id.flipButton);
        settingsButton = view.findViewById(R.id.settingsButton);
        textViewId = view.findViewById(R.id.textViewId);

        textViewId.setText(sharedPref.getString("participantId", "null"));
        cueText.setText(sharedPref.getString("currentCue", "No cues yet"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());

        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCue) {
                    cueText.setText(currentCueInfo);
                    cueText.setTypeface(null, Typeface.NORMAL);
                    cueText.setTextSize(20);
                    isCue = false;
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "watch");
                    params.putString("cue", String.valueOf(cueText.getText()));
                    params.putString("cueLength", String.valueOf(cueText.getText().length()));
                    params.putString("cueInfoLength", String.valueOf(currentCueInfo.length()));
                    params.putString("received", formatter.format(date));
                    firebaseAnalytics.logEvent("flipCardToBack", params);
                } else {
                    //cueText.setText(cueArrayList.get(cueNum).cue);
                    cueText.setText(currentCueText);
                    cueText.setTypeface(null, Typeface.BOLD);
                    cueText.setTextSize(30);
                    isCue = true;
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "watch");
                    params.putString("cue", String.valueOf(cueText.getText()));
                    params.putString("cueLength", String.valueOf(cueText.getText().length()));
                    params.putString("cueInfoLength", String.valueOf(currentCueInfo.length()));
                    params.putString("received", formatter.format(date));
                    firebaseAnalytics.logEvent("flipCardToFront", params);
                }

                /*//TODO Dette er for testing av notifikasjoner og kan fjernes
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

                    Notification notification = new NotificationCompat.Builder(view.getContext(),
                            CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_logo_big)
                            .setContentTitle("Testing notifications")
                            .setContentText("Tap to see cue")
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build();
                    notificationManager.notify(1,notification);*/
            }
        });

        cueText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCue) {
                    //cueText.setText(cueArrayList.get(cueNum).info);
                    cueText.setText(currentCueInfo);
                    cueText.setTypeface(null, Typeface.NORMAL);
                    cueText.setTextSize(20);
                    isCue = false;
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "watch");
                    params.putString("cue", String.valueOf(cueText.getText()));
                    params.putString("cueLength", String.valueOf(cueText.getText().length()));
                    params.putString("cueInfoLength", String.valueOf(currentCueInfo.length()));
                    params.putString("received", formatter.format(date));
                    firebaseAnalytics.logEvent("flipCardToBack", params);
                } else {
                    cueText.setText(currentCueText);
                    cueText.setTypeface(null, Typeface.BOLD);
                    cueText.setTextSize(30);
                    isCue = true;
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "watch");
                    params.putString("cue", String.valueOf(cueText.getText()));
                    params.putString("cueLength", String.valueOf(cueText.getText().length()));
                    params.putString("cueInfoLength", String.valueOf(currentCueInfo.length()));
                    params.putString("received", formatter.format(date));
                    firebaseAnalytics.logEvent("flipCardToFront", params);
                }
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flipText");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                bundle.putString("received", formatter.format(date));
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_click_settings);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        cueNum = sharedPref.getInt("cueNum", 0);
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "appStatus");
        firebaseAnalytics.logEvent("appInForeground", params);
    }

    @Override
    public void onResume() {
        super.onResume();
        cueNum = sharedPref.getInt("cueNum", 0);
    }

    @Override
    public void onStop() {
        super.onStop();
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "appStatus");
        firebaseAnalytics.logEvent("appInBackground", params);
    }

    public static class CueWorker extends Worker {
        private Context context;
        private int notificationId = 0;
        private SharedPreferences sharedPref;
        private SharedPreferences.Editor editor;
        private int cueNum;
        private String nextCueText;
        private String nextCueInfo;
        private FirebaseAnalytics firebaseAnalytics;
        private String participantId;
        private String databaseReference;
        private List<Integer> currentIndexes = new ArrayList<>();

        public CueWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            this.context = context;
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            editor = sharedPref.edit();
            cueNum = sharedPref.getInt("cueNum", 0) + 1;
            participantId = sharedPref.getString("participantId", "Not set");
            databaseReference = sharedPref.getString("cueSet", "Astronomy");
            String cueIndexes = sharedPref.getString("cueIndexes", "0");
            Log.e("WORKER-cueIndexes", cueIndexes);
            currentIndexes = Stream.of(cueIndexes.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        private void fetchCues() {
            final FirebaseDatabase database = FirebaseDatabase.getInstance("https://wearable-memory-augmentation-default-rtdb.europe-west1.firebasedatabase.app");
            final DatabaseReference dbRef = database.getReference(databaseReference);

            if (cueNum >= currentIndexes.size()) {
                nextCueText = "Finished with all cues";
                nextCueInfo = nextCueText;
                //TODO Check if this works
                WorkManager.getInstance(context).cancelAllWork();
                Log.e("WORKER-CANCELLALLWORK()", "cancelAllWork()");
            } else {
                int cueNumFB = currentIndexes.get(cueNum);
                dbRef.child(String.valueOf(cueNumFB)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        } else {
                            Log.v("FIREBASERESULTS", String.valueOf(task.getResult().getValue()));
                            Cue nextCue = task.getResult().getValue(Cue.class);
                            if (nextCue != null) {
                                nextCueText = nextCue.cue;
                                nextCueInfo = nextCue.info;
                                Log.v("CUE", nextCue.cue);
                            }
                        }
                    }
                });
            }
        }

        private NotificationCompat.Builder createNotification() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_logo_big)
                    .setContentTitle(nextCueText)
                    .setContentText("Tap to see cue!")
                    .setAutoCancel(true);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            return builder;
        }

        @NonNull
        @Override
        public Result doWork() {
            Log.v("WORKER-DOWORK()", "doWork()");
            Log.v("WORKER-CUENUM-BEFORE-fetchCues()", String.valueOf(cueNum));
            fetchCues();
            Log.v("WORKER-CUENUM-AFTER-fetchCues()", String.valueOf(cueNum));

            editor.putInt("cueNum", cueNum);
            editor.putString("currentCue", nextCueText);
            editor.putString("currentInfo", nextCueInfo);
            editor.commit();


            notificationId += 1;
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, createNotification().build());


            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());

            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "watch");
            params.putString("cue", nextCueText);
            
//            params.putString("cueLength", String.valueOf(nextCueText.length()));
            //params.putString("cueInfoLength", String.valueOf(nextCueInfo.length()));
//            params.putString("received", formatter.format(date));
            firebaseAnalytics.logEvent("receiveNewCue", params);

            if (nextCueText.equals("Finished with all cues")) {
                WorkManager.getInstance(context).cancelAllWork();
            }

            PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock mWakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "wakeLock");
            mWakeLock.acquire();
            mWakeLock.release();

            return Result.success();
        }
    }

}
