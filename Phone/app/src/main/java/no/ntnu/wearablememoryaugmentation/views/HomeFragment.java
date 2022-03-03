package no.ntnu.wearablememoryaugmentation.views;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.TimeUnit;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.viewModel.HomeViewModel;
import no.ntnu.wearablememoryaugmentation.viewModel.LoginRegisterViewModel;

public class HomeFragment extends Fragment {

    private TextView loggedInUserTextView;
    private View settingsButton;
    private Boolean isOn = true;
    private HomeViewModel homeViewModel;
    private TextView previousButton;
    private TextView nextButton;
    private int cueNum;
    private int repeatInterval;
    private Fragment cardFragment;
    private FragmentTransaction transaction;
    private SharedPreferences sharedPref;
    private int notificationId = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        cueNum = sharedPref.getInt("cueNum", 0);
        repeatInterval = sharedPref.getInt("repeatInterval", 15);


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

        PeriodicWorkRequest nextCueRequest =
                new PeriodicWorkRequest.Builder(CueWorker.class, 15, TimeUnit.MINUTES)
                        // Constraints
                        .build();
        WorkManager
                .getInstance(getContext())
                .enqueue(nextCueRequest);

        WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(nextCueRequest.getId()).observe(this, workInfo -> {
            if(workInfo.getState().isFinished()){
                cueNum += 1;
                newCue();
                notificationId += 1;
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                notificationManager.notify(notificationId, createNotification().build());
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        loggedInUserTextView = view.findViewById(R.id.fragment_loggedin_loggedInUser);

        settingsButton = view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_settingsFragment);
            }
        });

        View cardOff = view.findViewById(R.id.card_off_visibility);

        if (isOn) {
            newCue();
        }
        else{
            cardOff.setVisibility(View.VISIBLE);
        }

        View on_off_button = view.findViewById(R.id.on_off_button);
        on_off_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOn = !isOn;
                if (isOn) {
                    cardOff.setVisibility(View.GONE);


                } else {
                    cardOff.setVisibility(View.VISIBLE);
                }
            }
        });

        previousButton = view.findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cueNum = sharedPref.getInt("cueNum", 0);
                cueNum -= 1;
                newCue();
            }
        });

        nextButton = view.findViewById(R.id.nextButton);
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

    private void newCue(){
        cardFragment = new CardFragment(cueNum);
        transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.card_placeholder, cardFragment).commit();
    }

    private NotificationCompat.Builder createNotification(){
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
        public CueWorker(
                @NonNull Context context,
                @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public Result doWork() {
            return Result.success();
        }
    }

}
