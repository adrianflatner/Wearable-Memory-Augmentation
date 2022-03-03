package no.ntnu.wearablememoryaugmentation.views;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.animation.Animator;
import androidx.core.animation.AnimatorInflater;
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
    private TextView cueInfo;
    private TextView currentInfo;
    private TextView cueTextSmall;
    private FrameLayout front;
    private FrameLayout back;
    private View flipButton;
    private TextView finishButton;
    private ArrayList<Cue> cueArrayList;

    private int cueNum;

    public CardFragment(int cueNum){
        this.cueNum = cueNum;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        cardViewModel = new ViewModelProvider(this).get(CardViewModel.class);
        cardViewModel.getCueListMutableLiveData().observe(this, new Observer<ArrayList<Cue>>() {
            @Override
            public void onChanged(ArrayList<Cue> cues) {
                if (cues != null) {
                    cueNum = cueNum < 0 ? 0 : cueNum;
                    cueNum = cues.size() <= cueNum ? cues.size()-1 : cueNum;
                    editor.putInt("cueNum", cueNum);
                    editor.commit();
                    cueText.setText(cues.get(cueNum).cue);
                    cueTextSmall.setText(cues.get(cueNum).cue);
                    cueInfo.setText(cues.get(cueNum).info);
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
        cueTextSmall = view.findViewById(R.id.cue_text_small_back);
        cueText = view.findViewById(R.id.cue_text);
        cueInfo = view.findViewById(R.id.cue_info_back);

        front = view.findViewById(R.id.view_front);
        front.setVisibility(View.VISIBLE);
        back = view.findViewById(R.id.view_back);

        flipButton = view.findViewById(R.id.flip_button);
        finishButton = view.findViewById(R.id.finish_button);

        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard(inflater.getContext(), back, front);
            }
        });
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard(inflater.getContext(), front, back);
            }
        });



        return view;
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
