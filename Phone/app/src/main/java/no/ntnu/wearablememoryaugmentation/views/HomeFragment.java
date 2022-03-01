package no.ntnu.wearablememoryaugmentation.views;

import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseUser;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.viewModel.HomeViewModel;
import no.ntnu.wearablememoryaugmentation.viewModel.LoginRegisterViewModel;

public class HomeFragment extends Fragment {

    private TextView loggedInUserTextView;
    private Button logOutButton;
    private Button settingsButton;
    private Boolean isOn = true;
    private HomeViewModel homeViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        loggedInUserTextView = view.findViewById(R.id.fragment_loggedin_loggedInUser);
        /*logOutButton = view.findViewById(R.id.fragment_loggedin_logOut);

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeViewModel.logOut();
            }
        });*/

        settingsButton = view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_settingsFragment);
            }
        });

        View cardOff = view.findViewById(R.id.card_off_visibility);

        if (isOn) {
            Fragment cardFragment = new CardFragment();
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.card_placeholder, cardFragment).commit();
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

        return view;
    }
}
