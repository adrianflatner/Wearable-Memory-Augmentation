package no.ntnu.wearablememoryaugmentation.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.model.Cue;
import no.ntnu.wearablememoryaugmentation.viewModel.CardViewModel;
import no.ntnu.wearablememoryaugmentation.viewModel.HomeViewModel;

public class CardFragment extends Fragment {

    private CardViewModel cardViewModel;
    private TextView cueText;
    private TextView cueInfo;
    private TextView currentInfo;
    private TextView cueTextSmall;
    private ArrayList<Cue> cueArrayList;
    private Boolean isFlipped;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFlipped = false;
        cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);
        cardViewModel.getCueListMutableLiveData().observe(this, new Observer<ArrayList<Cue>>() {
            @Override
            public void onChanged(ArrayList<Cue> cues) {
                int i = 0;
                if (cues != null){
                    cueText.setText(cues.get(i).cue);
                    cueInfo.setText(cues.get(i).info);
                    cueArrayList = cues;
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card, container, false);

        currentInfo = view.findViewById(R.id.current_info);
        cueTextSmall = view.findViewById(R.id.cue_text_small);
        cueText = view.findViewById(R.id.cue_text);
        cueInfo = view.findViewById(R.id.cue_info);
        cueInfo.setVisibility(TextView.INVISIBLE);

        ConstraintLayout cardView = view.findViewById(R.id.card_view);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFlipped = !isFlipped;
                cueText.setVisibility(isFlipped ? TextView.INVISIBLE : TextView.VISIBLE);
                cueInfo.setVisibility(!isFlipped ? TextView.INVISIBLE : TextView.VISIBLE);
                currentInfo.setText(isFlipped ? "Information" : "Current cue");
                cueTextSmall.setText(isFlipped ? cueText.getText() : "");
            }
        });

        return view;
    }
}
