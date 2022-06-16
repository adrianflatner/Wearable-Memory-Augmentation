package no.ntnu.wearablememoryaugmentation.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.work.WorkManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import no.ntnu.wearablememoryaugmentation.R;

public class SettingsFragment extends Fragment {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private FirebaseAnalytics firebaseAnalytics;
    private Button resetButton;
    private ImageView settingsBackButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        resetButton = view.findViewById(R.id.resetSettingsButton);
        settingsBackButton = view.findViewById(R.id.settingsBackButton);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WorkManager.getInstance(getContext()).cancelAllWork();
                editor.clear().commit();
                Toast.makeText(getContext(), "Settings reset", Toast.LENGTH_SHORT).show();
                /*Toast toast = Toast.makeText(getContext(), "Settings reset", Toast.LENGTH_SHORT);
                TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
                textView.setTextColor(getResources().getColor(R.color.white));
                textView.setBackgroundResource(R.color.app_green);
                toast.show();*/
                Navigation.findNavController(getView()).navigate(R.id.action_reset_settings);
            }
        });

        settingsBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.action_backButton_settings);
            }
        });

        return view;
    }
}
