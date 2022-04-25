package no.ntnu.wearablememoryaugmentation.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.analytics.FirebaseAnalytics;

import no.ntnu.wearablememoryaugmentation.R;

public class LoginFragment extends Fragment implements AdapterView.OnItemSelectedListener{
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

        Spinner cueSetSpinner = (Spinner) view.findViewById(R.id.cueSelectSpinner);
        cueSetSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> cueSetAdapter = ArrayAdapter.createFromResource(inflater.getContext(),
                R.array.cueSet, android.R.layout.simple_spinner_item);
        cueSetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cueSetSpinner.setAdapter(cueSetAdapter);
        int cueSetPosition = cueSetAdapter.getPosition(sharedPref.getString("cueSet", "Arts"));
        cueSetSpinner.setSelection(cueSetPosition);

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String value = String.valueOf(adapterView.getItemAtPosition(i));
        String name = "cueSet";
        firebaseAnalytics.setUserProperty("CueSet", value);

        editor.putString(name, value);
        editor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
