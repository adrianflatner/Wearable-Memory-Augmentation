package no.ntnu.wearablememoryaugmentation.views;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
import java.util.Date;
import java.util.concurrent.TimeUnit;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.model.Cue;
import no.ntnu.wearablememoryaugmentation.viewModel.CardViewModel;

public class CardFragment extends Fragment{
    public static final String CHANNEL_ID = "channelid";
    private NotificationManagerCompat notificationManager;

    private CardViewModel cardViewModel;
    private TextView cueText;
    private ArrayList<Cue> cueArrayList = new ArrayList<>();

    private Button flipButton;
    private Button nextCueButton;
    private Button prevCueButton;
    private int cueNum = 0;
    private boolean isCue;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("CARDFRAGMENT", "onCreate()");

        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);
        cardViewModel.getCueListMutableLiveData().observe(this, new Observer<ArrayList<Cue>>() {
            @Override
            public void onChanged(ArrayList<Cue> cues) {
                if (cues != null) {
                    cueNum = cueNum < 0 ? 0 : cueNum;
                    cueNum = cues.size() <= cueNum ? cues.size()-1 : cueNum;
                    cueText.setText(cues.get(cueNum).cue);

                    cueArrayList = cues;
                }
            }
        });
        createNotificationChannels();
        notificationManager = NotificationManagerCompat.from(this.getContext());

        PeriodicWorkRequest nextCueRequest = new PeriodicWorkRequest.Builder(CueWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(getContext())
                .enqueueUniquePeriodicWork("cueWork", ExistingPeriodicWorkPolicy.KEEP, nextCueRequest);

        WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(nextCueRequest.getId()).observe(this, workInfo -> {
            Log.e("NEW CUE", String.valueOf(cueNum));
            //newCue();
        });

    }

    private void createNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
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
        nextCueButton = view.findViewById(R.id.nextCueButton);
        prevCueButton = view.findViewById(R.id.prevCueButton);

        updateButtonVisibility();

        //prevCueButton.setVisibility(View.GONE);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());

        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCue){
                    cueText.setText(cueArrayList.get(cueNum).info);
                    isCue = false;
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flipText");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                    bundle.putString("received", formatter.format(date));
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                } else {
                    cueText.setText(cueArrayList.get(cueNum).cue);
                    isCue = true;
                }

                Intent intent = new Intent(view.getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

                Notification notification = new NotificationCompat.Builder(view.getContext(),
                        CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_logo_big)
                        .setContentTitle(cueArrayList.get(cueNum).cue)
                        .setContentText("Tap to see cue")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();
                notificationManager.notify(1,notification);
            }
        });

        cueText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCue){
                    cueText.setText(cueArrayList.get(cueNum).info);
                    isCue = false;
                } else {
                    cueText.setText(cueArrayList.get(cueNum).cue);
                    isCue = true;
                }
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "flipText");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                bundle.putString("received", formatter.format(date));
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

        nextCueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cueNum < cueArrayList.size()-1){
                    cueNum++;
                    cueText.setText(cueArrayList.get(cueNum).cue);
                    isCue = true;

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "nextCue");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                    bundle.putString("received", formatter.format(date));
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                }
                updateButtonVisibility();
            }
        });

        prevCueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cueNum > 0){
                    cueNum--;
                    cueText.setText(cueArrayList.get(cueNum).cue);
                    isCue = true;
                }
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "prevCue");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                bundle.putString("received", formatter.format(date));
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                updateButtonVisibility();
            }
        });
        return view;
    }

    protected void updateButtonVisibility(){
        if(cueNum == 0){
            prevCueButton.setVisibility(View.GONE);
        }
        else {
            prevCueButton.setVisibility(View.VISIBLE);
        }
        if(cueNum == cueArrayList.size()-1){
            nextCueButton.setVisibility(View.GONE);
        } else {
            nextCueButton.setVisibility(View.VISIBLE);
        }
    }

    /*protected void newCue(){
        cueNum++;
        cueText.setText(cueArrayList.get(cueNum).cue);
        isCue = true;
    }*/

    public static class CueWorker extends Worker{
        private int cueNum;
        private String nextCueText;
        private String nextCueInfo;
        private int notificationId = 0;
        private Context context;

        public CueWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            cueNum = 0;
            this.context = context;
        }

        private void fetchCues(){
            final FirebaseDatabase database = FirebaseDatabase.getInstance("https://wearable-memory-augmentation-default-rtdb.europe-west1.firebasedatabase.app");
            final DatabaseReference dbRef = database.getReference("cues");
            int cueNumFB = cueNum + 1;
            dbRef.child(String.valueOf(cueNumFB)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(!task.isSuccessful()){
                        Log.e("firebase", "Error geting data", task.getException());
                    }
                    else {
                        if(task.getResult().getValue() == null){
                            nextCueText = "Finished with all cues";
                            nextCueInfo = nextCueText;
                        }
                        else {
                            Log.v("FIREBASERESULTS", String.valueOf(task.getResult().getValue()));
                            Cue nextCue = task.getResult().getValue(Cue.class);
                            nextCueText = nextCue.cue;
                            nextCueInfo = nextCue.info;
                            Log.v("CUE", nextCue.cue);
                            Log.v("CUE", nextCue.info);
                        }
                    }
                }
            });
        }

        private NotificationCompat.Builder createNotification() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "cueChannel")
                    .setSmallIcon(R.drawable.ic_logo_big)
                    .setContentTitle(nextCueText)
                    .setContentText("Tap to see cue!");

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            return builder;
        }

        @NonNull
        @Override
        public Result doWork() {
            fetchCues();

            notificationId++;
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, createNotification().build());

            return Result.success();
        }
    }

}
