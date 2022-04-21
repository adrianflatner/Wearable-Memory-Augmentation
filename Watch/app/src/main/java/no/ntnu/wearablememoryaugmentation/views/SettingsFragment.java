package no.ntnu.wearablememoryaugmentation.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import no.ntnu.wearablememoryaugmentation.R;

public class SettingsFragment extends Fragment {
    private Button resetButton;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        resetButton = view.findViewById(R.id.resetSettingsButton);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO legg til logikk for resetting av settings
                Navigation.findNavController(getView()).navigate(R.id.action_reset_settings);
            }
        });

        return view;
    }
}
