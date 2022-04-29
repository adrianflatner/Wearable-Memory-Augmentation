package no.ntnu.wearablememoryaugmentation.views;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.animation.Animator;
import androidx.core.animation.AnimatorInflater;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import no.ntnu.wearablememoryaugmentation.R;
import no.ntnu.wearablememoryaugmentation.model.Cue;
import no.ntnu.wearablememoryaugmentation.viewModel.CardViewModel;

public class CardFragment extends Fragment {

    private FirebaseAnalytics firebaseAnalytics;
    private CardViewModel cardViewModel;
    private TextView cueText;
    private TextView cueInfo;
    private TextView currentInfo;
    private TextView cueTextSmall;
    private FrameLayout front;
    private FrameLayout back;
    private View flipButton;
    private TextView finishButton;
    private ArrayList<Cue> cueArrayList;
    private int cueNum;
    private static final int REPETITION = 3;
    private String device;
    SharedPreferences sharedPref;

    public CardFragment(int cueNum){
        this.cueNum = cueNum;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        sharedPref = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);
        cardViewModel.getCueListMutableLiveData().observe(this, new Observer<ArrayList<Cue>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChanged(ArrayList<Cue> cues) {
                if (cues != null) {
                    List<Integer> currentIndexes = new ArrayList<>();
                    int currentCueListLength = sharedPref.getInt("currentCueListLength", 0);
                    Log.e("CUELISTLENGTH", String.valueOf(cues.size()));
                    Log.e("CUELISTGET", String.valueOf(currentCueListLength));
                    if(currentCueListLength != cues.size()){
                            List<Integer> index = new ArrayList<>();
                            for (int i = 0; i < cues.size(); i++) {
                                index.add(i);
                            }
                            for (int i = 1; i <= REPETITION; i++) {
                                currentIndexes.addAll(index);
                            }
                            Collections.shuffle(currentIndexes);
                            editor.putString("cueIndexes", String.valueOf(currentIndexes).replace("[", "").replace("]", ""));
                            editor.putInt("currentCueListLength", cues.size());
                            editor.commit();
                        }
                    else{
                        String cueIndexes = sharedPref.getString("cueIndexes", "0");
                        currentIndexes = Stream.of(cueIndexes.split(","))
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
                    }

                    for(int i : currentIndexes){
                        Log.e("INDEX", String.valueOf(i));
                    }
                    cueNum = cueNum < 0 ? -1 : cueNum;
                    cueNum = currentIndexes.size() <= cueNum ? currentIndexes.size() : cueNum;
                    editor.putInt("cueNum", cueNum);
                    if(cueNum >= currentIndexes.size() || cueNum < 0){
                        cueText.setText("Finished with all cues");
                        cueInfo.setText("Finished with all cues");
                    }
                    else{
                        Log.e("CUENUM", String.valueOf(cueNum));
                        Log.e("CURRENTINDEX", String.valueOf(currentIndexes.get(cueNum)));
                        cueText.setText(cues.get(currentIndexes.get(cueNum)).cue);
                        cueTextSmall.setText(cues.get(currentIndexes.get(cueNum)).cue);
                        cueInfo.setText(cues.get(currentIndexes.get(cueNum)).info);
                    }

                    try {
                        editor.putString("currentCue", cues.get(currentIndexes.get(cueNum)).cue);
                        editor.putString("currentInfo", cues.get(currentIndexes.get(cueNum)).info);
                    } catch (Exception e){
                        editor.putString("currentCue", "Finished with all cues");
                        editor.putString("currentInfo", "Finished with all cues");
                    }
                    editor.commit();
                }
                if(cueInfo.getText().length() > 70){
                    cueInfo.setTextSize(19);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card, container, false);

        currentInfo = view.findViewById(R.id.current_info);
        cueTextSmall = view.findViewById(R.id.cue_text_small_back);
        cueText = view.findViewById(R.id.cue_text);
        cueInfo = view.findViewById(R.id.cue_info_back);

        front = view.findViewById(R.id.view_front);
        front.setVisibility(View.VISIBLE);
        back = view.findViewById(R.id.view_back);

        flipButton = view.findViewById(R.id.flip_button);
        finishButton = view.findViewById(R.id.finish_button);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());

        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard(inflater.getContext(), back, front);
                flipButton.setVisibility(View.GONE);
                device = sharedPref.getString("cuingMode", "Phone");
                if(!device.equals("Glasses")){
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "flashcard");
                    params.putString("cue", String.valueOf(cueText.getText()));
                    params.putString("cueLength", String.valueOf(cueText.getText().length()));
                    params.putString("cueInfoLength", String.valueOf(cueInfo.getText().length()));
                    params.putString("received", formatter.format(date));
                    firebaseAnalytics.logEvent("flipCardToBack", params);
                }
            }
        });
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard(inflater.getContext(), front, back);
                finishButton.setVisibility(View.GONE);
                device = sharedPref.getString("cuingMode", "Phone");
                if(!device.equals("Glasses")) {
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "flashcard");
                    params.putString("cue", String.valueOf(cueText.getText()));
                    params.putString("cueLength", String.valueOf(cueText.getText().length()));
                    params.putString("cueInfoLength", String.valueOf(cueInfo.getText().length()));
                    params.putString("received", formatter.format(date));
                    firebaseAnalytics.logEvent("flipCardToFront", params);
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void flipCard(Context context, View front, View back) {
        try {
            front.setVisibility(View.VISIBLE);
            Animator flipOutAnimatorSet = AnimatorInflater.loadAnimator(
                    context,
                    R.animator.flip_out
            );
            flipOutAnimatorSet.setTarget(back);
            Animator flipInAnimatorSet =
                    AnimatorInflater.loadAnimator(
                            context,
                            R.animator.flip_in
                    );
            flipInAnimatorSet.setTarget(front);
            flipOutAnimatorSet.start();
            flipInAnimatorSet.start();
            flipOutAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) { }
                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    back.setVisibility(View.GONE);
                    flipButton.setVisibility(View.VISIBLE);
                    finishButton.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationCancel(@NonNull Animator animation) { }
                @Override
                public void onAnimationRepeat(@NonNull Animator animation) { }
            });
        } catch (Exception e) {
        }
    }

}
