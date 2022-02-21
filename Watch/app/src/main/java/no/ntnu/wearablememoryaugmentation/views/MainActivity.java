package no.ntnu.wearablememoryaugmentation.views;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.textView;
        mTextView.setText(R.string.q0);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCardClick(view);
            }
        });
    }



    protected void handleCardClick(View view){
        mTextView.setText(R.string.a0);
    }
}