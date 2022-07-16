package com.example.diabuddy.trends;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diabuddy.logbook.LogbookEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class TrendsViewModel extends ViewModel {

    private final CollectionReference logbookDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("logbook");
    private final DocumentReference settingsDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final String WEIGHT_KEY = "weight";
    private final String HYPO_KEY = "hypo";
    private final String HYPER_KEY = "hyper";
    private final String GLUCOSE_UNIT_KEY = "bgUnit";

    private ArrayList<LogbookEntry> logbookEntries = new ArrayList<>();
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    private MutableLiveData<Double> weight = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> lb = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> ub = new MutableLiveData<>(0.0);
    private MutableLiveData<String> unit = new MutableLiveData<>("mmol/L");

    public TrendsViewModel() {
        settingsDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    weight.setValue((Double) doc.get(WEIGHT_KEY));
                    lb.setValue((Double) doc.get(HYPO_KEY));
                    ub.setValue((Double) doc.get(HYPER_KEY));
                    unit.setValue((String) doc.get(GLUCOSE_UNIT_KEY));
                }
            }
        });
        settingsDB.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {
                if (error == null && doc != null && doc.exists()) {
                    weight.setValue((Double) doc.get(WEIGHT_KEY));
                    lb.setValue((Double) doc.get(HYPO_KEY));
                    ub.setValue((Double) doc.get(HYPER_KEY));
                    unit.setValue((String) doc.get(GLUCOSE_UNIT_KEY));
                }
            }
        });
    }

    public ArrayList<LogbookEntry> getEntries(@NonNull Date startDate, @NonNull Date endDate) {
        // 00:00 of startDate to 23:59 of endDate
        try {
            String dateOnly = dateFormatter.format(startDate);
            startDate = formatter.parse(dateOnly + " 00:00");
            dateOnly = dateFormatter.format(endDate);
            endDate = formatter.parse(dateOnly + " 23:59");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ArrayList<LogbookEntry> ans = new ArrayList<>();

        logbookDB.whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                ans.add(doc.toObject(LogbookEntry.class));
                            }
                        }
                    }
                });
        return ans;
    }

    public MutableLiveData<Double> getWeightInKg() {
        return weight;
    }

    public MutableLiveData<Double> getLowerBound() {
        return lb;
    }

    public MutableLiveData<Double> getUpperBound() {
        return ub;
    }

    public MutableLiveData<String> getUnit() { return unit; }
}