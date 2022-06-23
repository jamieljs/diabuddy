package com.example.diabuddy.siterotation;

import android.renderscript.Sampler;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SiteViewModel extends ViewModel {
    
    private final DatabaseReference sitesDB = FirebaseDatabase.getInstance().getReferenceFromUrl("https://diabuddy-f65b0-default-rtdb.firebaseio.com/")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("sites");
    private final String NAME_KEY = "name";
    private final String USED_KEY = "used";
    private final String IN_PROGRESS_KEY = "inProgress"; // 1 if its being edited, 0 if editing is done
    private final String SIZE_KEY = "size";

    // name, used, index
    private MutableLiveData<ArrayList<Pair<Pair<String, Boolean>,Integer>>> siteList = new MutableLiveData<>(new ArrayList<>());
    private ValueEventListener listener;
    private int size;

    public SiteViewModel() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(IN_PROGRESS_KEY) && snapshot.child(IN_PROGRESS_KEY).getValue(Integer.class) == 1) {
                    return;
                }
                ArrayList<Pair<Pair<String, Boolean>,Integer>> sites = new ArrayList<>();
                if (!snapshot.hasChild(SIZE_KEY) || !snapshot.hasChild(NAME_KEY) || !snapshot.hasChild(USED_KEY)) {
                    siteList.setValue(sites);
                    return;
                }
                size = snapshot.child(SIZE_KEY).getValue(Integer.class);
                for (int i = 0; i < size; i++) {
                    String index = i + "";
                    if (snapshot.child(NAME_KEY).hasChild(index) && snapshot.child(USED_KEY).hasChild(index)) {
                        String name = snapshot.child(NAME_KEY).child(index).getValue(String.class);
                        boolean used = snapshot.child(USED_KEY).child(index).getValue(Boolean.class);
                        sites.add(new Pair<>(new Pair<>(name, used),i));
                    }
                }
                siteList.setValue(sites);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        sitesDB.addListenerForSingleValueEvent(listener);
        sitesDB.addValueEventListener(listener);
    }

    public MutableLiveData<ArrayList<Pair<Pair<String, Boolean>,Integer>>> getSiteList() {
        return siteList;
    }

    private void enterEditMode() {
        sitesDB.child(IN_PROGRESS_KEY).setValue(1);
    }
    private void exitEditMode() {
        sitesDB.child(IN_PROGRESS_KEY).setValue(0);
    }

    public void addSite(String name) {
        enterEditMode();
        sitesDB.child(NAME_KEY).child(size + "").setValue(name);
        sitesDB.child(USED_KEY).child(size + "").setValue(false);
        size++;
        sitesDB.child(SIZE_KEY).setValue(size);
        exitEditMode();
    }

    public void deleteAll() {
        enterEditMode();
        sitesDB.child(NAME_KEY).setValue(null);
        sitesDB.child(USED_KEY).setValue(null);
        sitesDB.child(SIZE_KEY).setValue(0);
        exitEditMode();
    }

    public void markOne(int index, boolean isChecked) {
        enterEditMode();
        sitesDB.child(USED_KEY).child(index + "").setValue(isChecked);
        exitEditMode();
    }

    public void deleteOne(int index) {
        enterEditMode();
        sitesDB.child(NAME_KEY).child(index + "").setValue(null);
        sitesDB.child(USED_KEY).child(index + "").setValue(null);
        exitEditMode();
    }

    public void reset() {
        enterEditMode();
        for (int i = 0; i < size; i++) {
            sitesDB.child(USED_KEY).child(i + "").setValue(false);
        }
        exitEditMode();
    }
}
