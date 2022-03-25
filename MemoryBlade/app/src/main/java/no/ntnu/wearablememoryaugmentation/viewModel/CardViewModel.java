package no.ntnu.wearablememoryaugmentation.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import no.ntnu.wearablememoryaugmentation.model.AppRepository;
import no.ntnu.wearablememoryaugmentation.model.Cue;

import java.util.ArrayList;


public class CardViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private MutableLiveData<ArrayList<Cue>> cueListMutableLiveData;

    public CardViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        cueListMutableLiveData = appRepository.getCueListMutableLiveData();

    }

    public MutableLiveData<ArrayList<Cue>> getCueListMutableLiveData() {
        return cueListMutableLiveData;
    }
}
