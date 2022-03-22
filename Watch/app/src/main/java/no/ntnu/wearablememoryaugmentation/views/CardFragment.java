package no.ntnu.wearablememoryaugmentation.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.model.Cue;
import no.ntnu.wearablememoryaugmentation.viewModel.CardViewModel;

public class CardFragment extends Fragment {
    private CardViewModel cardViewModel;
    private TextView cueText;
    private ArrayList<Cue> cueArrayList;

    private Button flipButton;
    private Button nextCueButton;
    private Button prevCueButton;
    private int cueCounter = 0;
    private boolean isCue = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("CARDFRAGMENT", "onCreate()");

        cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);
        cardViewModel.getCueListMutableLiveData().observe(this, new Observer<ArrayList<Cue>>() {
            @Override
            public void onChanged(ArrayList<Cue> cues) {
                Log.v("DEBUGCUE", "onChanged()-cardfragment");
                int i = 0;
                if (cues != null) {
                    cueText.setText(cues.get(i).cue);
                    //cueTextSmall.setText(cues.get(i).cue);
                    //cueInfo.setText(cues.get(i).info);
                    cueArrayList = cues;
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_fragment, container, false);
        cueText = view.findViewById(R.id.cueText);
        flipButton = view.findViewById(R.id.flipButton);
        nextCueButton = view.findViewById(R.id.nextCueButton);
        prevCueButton = view.findViewById(R.id.prevCueButton);

        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCue){
                    cueText.setText(cueArrayList.get(cueCounter).info);
                    isCue = false;
                } else {
                    cueText.setText(cueArrayList.get(cueCounter).cue);
                    isCue = true;
                }
            }
        });

        cueText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCue){
                    cueText.setText(cueArrayList.get(cueCounter).info);
                    isCue = false;
                } else {
                    cueText.setText(cueArrayList.get(cueCounter).cue);
                    isCue = true;
                }
            }
        });

        nextCueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cueCounter++;
                cueText.setText(cueArrayList.get(cueCounter).cue);
                isCue = true;
            }
        });

        prevCueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cueCounter > 0){
                    cueCounter--;
                }
                cueText.setText(cueArrayList.get(cueCounter).cue);
                isCue = true;
            }
        });

        return view;
    }
}
