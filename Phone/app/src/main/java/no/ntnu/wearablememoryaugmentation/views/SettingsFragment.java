package no.ntnu.wearablememoryaugmentation.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.viewModel.HomeViewModel;
import no.ntnu.wearablememoryaugmentation.viewModel.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Spinner cuingSpinner = (Spinner) view.findViewById(R.id.cuing_spinner);
        ArrayAdapter<CharSequence> cuingAdapter = ArrayAdapter.createFromResource(inflater.getContext(),
                R.array.cuingModes, R.layout.spinner);
        cuingAdapter.setDropDownViewResource(R.layout.spinner_item);
        cuingSpinner.setAdapter(cuingAdapter);

        Spinner timingSpinner = (Spinner) view.findViewById(R.id.timing_spinner);
        ArrayAdapter<CharSequence> timingAdapter = ArrayAdapter.createFromResource(inflater.getContext(),
                R.array.timings, R.layout.spinner);
        timingAdapter.setDropDownViewResource(R.layout.spinner_item);
        timingSpinner.setAdapter(timingAdapter);


        Spinner notificationsSpinner = (Spinner) view.findViewById(R.id.notifications_spinner);
        ArrayAdapter<CharSequence> notificationsAdapter = ArrayAdapter.createFromResource(inflater.getContext(),
                R.array.notifications, R.layout.spinner);
        notificationsAdapter.setDropDownViewResource(R.layout.spinner_item);
        notificationsSpinner.setAdapter(notificationsAdapter);

        TextView logOutButton = view.findViewById(R.id.log_out_button);

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsViewModel.logOut();
                Navigation.findNavController(getView()).navigate(R.id.action_settingsFragment_to_loginRegisterFragment);
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

        return view;
    }

}
