package no.ntnu.wearablememoryaugmentation.views;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.model.Cue;
import no.ntnu.wearablememoryaugmentation.viewModel.CardViewModel;
import no.ntnu.wearablememoryaugmentation.viewModel.HomeViewModel;

public class HomeFragment extends Fragment {

    private TextView loggedInUserTextView;
    private View settingsButton;
    private Boolean isOn;
    private HomeViewModel homeViewModel;
    private TextView previousButton;
    private TextView nextButton;
    private int cueNum;
    private String repeatInterval;
    private Fragment cardFragment;
    private FragmentTransaction transaction;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private int notificationId = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

        sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        cueNum = sharedPref.getInt("cueNum", 0);
        repeatInterval = sharedPref.getString("timing", "Random");
        isOn = sharedPref.getBoolean("isOn", true);
        Log.e("ISON", isOn.toString());


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

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        loggedInUserTextView = view.findViewById(R.id.fragment_loggedin_loggedInUser);
                loggedInUserTextView.setVisibility(View.GONE);
        settingsButton = view.findViewById(R.id.settingsButton);
        View cardOff = view.findViewById(R.id.card_off_visibility);
        View on_off_button = view.findViewById(R.id.on_off_button);
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
            on_off_button.setBackgroundResource(R.drawable.ic_on_button);
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
                    on_off_button.setBackgroundResource(R.drawable.ic_on_button);
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
                                break;
                            case 2:
                                on_off_button.setBackgroundResource(R.drawable.ic_glasses);
                                break;
                            default:
                                on_off_button.setBackgroundResource(R.drawable.ic_on_button);
                                break;
                        }
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
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cueNum = sharedPref.getInt("cueNum", 0);
                cueNum += 1;
                newCue();
            }
        });

        return view;
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

    public static class CueWorker extends Worker {
        private Context context;
        private int notificationId = 0;
        private SharedPreferences sharedPref;
        private SharedPreferences.Editor editor;
        //private String nextCue;


        public CueWorker(
                @NonNull Context context,
                @NonNull WorkerParameters params) {
            super(context, params);
            this.context = context;
            sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            editor = sharedPref.edit();
        }

       /* private void setNextCue() {
            int cueNum = sharedPref.getInt("cueNum", -1);
            if (cueNum < 0) {
                nextCue = "All cues finished";
            }
            CardViewModel cardViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(CardViewModel.class);
            cardViewModel.getCueListMutableLiveData().observe((LifecycleOwner) this, new Observer<ArrayList<Cue>>() {
                @Override
                public void onChanged(ArrayList<Cue> cues) {
                    if (cues != null) {
                        nextCue = cues.get(sharedPref.getInt("cueNum", 0)).cue;
                    }
                }
            });
        }*/

        private NotificationCompat.Builder createNotification() {
            String newCue = sharedPref.getString("currentCue", "New Cue");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "cueChannel")
                    .setSmallIcon(R.drawable.ic_logo_big)
                    .setContentTitle(newCue)
                    .setContentText("Tap to see cue!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            return builder;
        }

        @Override
        public Result doWork() {

            int cueNum = sharedPref.getInt("cueNum", 0) + 1;
            editor.putInt("cueNum", cueNum);
            editor.commit();

            String notifications = sharedPref.getString("notifications", "On");
            if (notifications.equals("On")) {
                notificationId += 1;
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(notificationId, createNotification().build());

            }
            return Result.success();
        }
    }

}
