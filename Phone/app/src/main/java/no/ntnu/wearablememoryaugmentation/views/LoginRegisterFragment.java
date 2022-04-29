package no.ntnu.wearablememoryaugmentation.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;


import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.viewModel.LoginRegisterViewModel;

public class LoginRegisterFragment extends Fragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText participantIdEditText;
    private Button loginButton;
    private Button registerButton;
    private Button resetSettings;
    private LoginRegisterViewModel loginRegisterViewModel;
    private FirebaseAnalytics firebaseAnalytics;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        loginRegisterViewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);
        loginRegisterViewModel.getUserMutableLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null){
                    Navigation.findNavController(getView()).navigate(R.id.action_loginRegisterFragment_to_homeFragment);
                }
            }
        });
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loginregister, container, false);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });

        emailEditText = view.findViewById(R.id.fragment_loginregister_email);
        passwordEditText = view.findViewById(R.id.fragment_loginregister_password);
        participantIdEditText = view.findViewById(R.id.login_register_participant);
        loginButton = view.findViewById(R.id.fragment_loginregister_login);
        registerButton = view.findViewById(R.id.fragment_loginregister_register);
        resetSettings = view.findViewById(R.id.resetSettings);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String participantId = participantIdEditText.getText().toString().trim();
                if(participantId.length() <= 0){
                    participantIdEditText.setError("Required");
                }
                if(email.length() <= 0){
                    emailEditText.setError("Required");
                }
                if(password.length() <= 0){
                    passwordEditText.setError("Required");
                }
                if(email.length() > 0 && password.length() > 0 && participantId.length() > 0){
                    loginRegisterViewModel.register(email, password);
                    firebaseAnalytics.setUserId(participantId);
                    editor.putString("participantId", participantId);
                    editor.commit();
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.METHOD, "email");
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, params);
                }

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String participantId = participantIdEditText.getText().toString().trim();
                if(participantId.length() <= 0){
                    participantIdEditText.setError("Required");
                }
                if(email.length() <= 0){
                    emailEditText.setError("Required");
                }
                if(password.length() <= 0){
                    passwordEditText.setError("Required");
                }
                if(email.length() > 0 && password.length() > 0 && participantId.length() > 0){
                    loginRegisterViewModel.login(email, password);
                    firebaseAnalytics.setUserId(participantId);
                    editor.putString("participantId", participantId);
                    editor.commit();
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.METHOD, "email");
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, params);
                }
            }
        });

        resetSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.clear();
                editor.commit();
                Toast.makeText(getContext(), "Settings reset", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


}
