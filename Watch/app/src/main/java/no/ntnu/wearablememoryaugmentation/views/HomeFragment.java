package no.ntnu.wearablememoryaugmentation.views;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.model.Cue;
import no.ntnu.wearablememoryaugmentation.viewModel.HomeViewModel;

public class HomeFragment extends Fragment {
    /*private HomeViewModel homeViewModel;

    private int cueNum;
    private Fragment cardFragment;
    private FragmentTransaction transaction;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

        sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        //cueNum = sharedPref.getInt("cueNum", 0);
        cueNum = 0;

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        PeriodicWorkRequest nextCueRequest = new PeriodicWorkRequest.Builder(CueWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(getContext())
                .enqueueUniquePeriodicWork("cueWork", ExistingPeriodicWorkPolicy.KEEP, nextCueRequest);

        WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(nextCueRequest.getId()).observe(this, workInfo -> {
            Log.e("NEW CUE", String.valueOf(cueNum));
            //newCue();
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false); */

        /*Fragment cardFragment = new CardFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.card_placeholder, cardFragment).commit();
         */ /*
        return view;
    }

    private void createNotificationChannel() {
        CharSequence name = "Cues";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    "cueChannel",
                    "New cue",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = this.getContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }
*/
    /*private NotificationCompat.Builder createNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "cueChannel")
                .setSmallIcon(R.drawable.ic_baseline_access_time_24)
                .setContentTitle("New Cue")
                .setContentText("Tap to see cue");
                return builder;
    }*/

    /*protected void newCue(){
        cardFragment = new CardFragment(cueNum);
        transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.card_placeholder, cardFragment).commit();
    }*/
/*
    public static class CueWorker extends Worker {
        private String nextCueText;
        private String nextCueInfo;
        private int notificationId = 0;
        private Context context;

        private SharedPreferences sharedPref;
        private SharedPreferences.Editor editor;
        private int cueNum;


        public CueWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            this.context = context;
            sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            editor = sharedPref.edit();
            //cueNum = sharedPref.getInt("cueNum", 0) + 1;
            cueNum = 0;
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
                    .setSmallIcon(R.drawable.ic_baseline_access_time_24)
                    .setContentTitle(nextCueText)
                    .setContentText("Tap to see cue!");

            //Intent intent = new Intent(context, MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            return builder;
        }

        @NonNull
        @Override
        public Result doWork() {
            fetchCues();

            editor.putInt("cueNum", cueNum);
            editor.putString("currentCue", nextCueText);
            editor.putString("currentInfo", nextCueInfo);
            editor.commit();

            notificationId++;
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, createNotification().build());

            return Result.success();
        }
    }
*/
}
