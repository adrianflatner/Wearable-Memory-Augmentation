package no.ntnu.wearablememoryaugmentation.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AppRepository {
    private Application application;
    //private FirebaseAuth firebaseAuth;
    //private MutableLiveData<FirebaseUser> userMutableLiveData;
    private MutableLiveData<Boolean> loggedOutMutableLiveData;
    private DatabaseReference dbRef;
    private FirebaseDatabase database;
    private MutableLiveData<ArrayList<Cue>> cueListMutableLiveData;

    public AppRepository(Application application){
        this.application = application;

        firebaseAuth = FirebaseAuth.getInstance();
        userMutableLiveData = new MutableLiveData<>();
        loggedOutMutableLiveData = new MutableLiveData<>();

        if (firebaseAuth.getCurrentUser() != null){
            userMutableLiveData.postValue(firebaseAuth.getCurrentUser());
            loggedOutMutableLiveData.postValue(false);
        }

        database = FirebaseDatabase.getInstance("https://wearable-memory-augmentation-default-rtdb.europe-west1.firebasedatabase.app");
        dbRef = database.getReference("cues");

        cueListMutableLiveData = new MutableLiveData<ArrayList<Cue>>();
    }

    public MutableLiveData<ArrayList<Cue>> getCueListMutableLiveData() {
        ArrayList<Cue> cueList = new ArrayList<>();
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Cue cue = snapshot.getValue(Cue.class);
                    cueList.add(cue);
                }
                cueListMutableLiveData.setValue(cueList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return cueListMutableLiveData;
    }

    public MutableLiveData<FirebaseUser> getUserMutableLiveData() {
        return userMutableLiveData;
    }

    public MutableLiveData<Boolean> getLoggedOutMutableLiveData() {
        return loggedOutMutableLiveData;
    }

}
