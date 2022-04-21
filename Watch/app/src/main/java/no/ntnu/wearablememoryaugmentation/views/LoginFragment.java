package no.ntnu.wearablememoryaugmentation.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.analytics.FirebaseAnalytics;

import no.ntnu.wearablememoryaugmentation.R;

public class LoginFragment extends Fragment {
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private FirebaseAnalytics firebaseAnalytics;

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

        View view = inflater.inflate(R.layout.login_fragment, container, false);
        View submitButton = view.findViewById(R.id.submitLoginButton);
        EditText participantIdEditText = view.findViewById(R.id.editTextNumber2);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String participantId = participantIdEditText.getText().toString().trim();
                editor.putString("participantId", participantId);
                editor.commit();
                firebaseAnalytics.setUserId(participantId);
                // TODO add firebase event-logging
                Navigation.findNavController(getView()).navigate(R.id.action_login_submit);
            }
        });
        return view;
    }
}
