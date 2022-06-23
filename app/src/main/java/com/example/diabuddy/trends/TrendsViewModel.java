package com.example.diabuddy.trends;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diabuddy.logbook.LogbookEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private final String SIZE_KEY = "size";
    private final String DATE_KEY = "date";
    private final String READING_KEY = "reading";
    private final String BOLUS_KEY = "bolus";
    private final String CORRECTION_KEY = "correction";
    private final String BASAL_KEY = "basal";
    private final String CARBS_KEY = "carbs";
    private final String FOOD_KEY = "food";
    private final String EXERCISE_KEY = "exercise";
    private final String INTENSITY_KEY = "intensity";
    private final String NOTES_KEY = "notes";
    private final String WEIGHT_KEY = "weight";
    private final String HYPO_KEY = "hypo";
    private final String HYPER_KEY = "hyper";
    private final String GLUCOSE_UNIT_KEY = "bgUnit";

    private ArrayList<LogbookEntry> logbookEntries = new ArrayList<>();
    private int logbookSize = -1;
    private SimpleDateFormat gmtFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    private MutableLiveData<Double> weight = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> lb = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> ub = new MutableLiveData<>(0.0);
    private MutableLiveData<String> unit = new MutableLiveData<>("mmol/L");

    public TrendsViewModel() {
        gmtFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        ValueEventListener logbookListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int size = snapshot.child(SIZE_KEY).getValue(Integer.class);
                if (logbookSize != size) logbookSize = size;
                loadLogbook(snapshot, size);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        dbLogbook.addListenerForSingleValueEvent(logbookListener);
        dbLogbook.addValueEventListener(logbookListener);

        ValueEventListener settingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                weight.setValue(snapshot.child(WEIGHT_KEY).getValue(Double.class));
                lb.setValue(snapshot.child(HYPO_KEY).getValue(Double.class));
                ub.setValue(snapshot.child(HYPER_KEY).getValue(Double.class));
                unit.setValue(snapshot.child(GLUCOSE_UNIT_KEY).getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        dbSettings.addListenerForSingleValueEvent(settingsListener);
        dbSettings.addValueEventListener(settingsListener);
    }

    private void loadLogbook(DataSnapshot snapshot, int size) {
        ArrayList<LogbookEntry> arr = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (snapshot.hasChild(i + "")) {
                DataSnapshot cur = snapshot.child(i + "");
                if (cur.hasChild(NOTES_KEY)) {
                    String date = cur.child(DATE_KEY).getValue(String.class);
                    double reading = cur.child(READING_KEY).getValue(Double.class);
                    double bolus = cur.child(BOLUS_KEY).getValue(Double.class);
                    double correction = cur.child(CORRECTION_KEY).getValue(Double.class);
                    double basal = cur.child(BASAL_KEY).getValue(Double.class);
                    double carbs = cur.child(CARBS_KEY).getValue(Double.class);
                    //String food = cur.child(FOOD_KEY).getValue(String.class);
                    int exercise = cur.child(EXERCISE_KEY).getValue(Integer.class);
                    //int intensity = cur.child(INTENSITY_KEY).getValue(Integer.class);
                    //String notes = cur.child(NOTES_KEY).getValue(String.class);
                    try {
                        arr.add(new LogbookEntry(i, gmtFormatter.parse(date), reading, bolus, correction, basal, carbs, "", exercise, 0, ""));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        logbookEntries.clear();
        logbookEntries.addAll(arr);
    }

    public ArrayList<LogbookEntry> getEntries(Date startDate, Date endDate) {
        // 00:00 of startDate to 23:59 of endDate
        try {
            String dateOnly = dateFormatter.format(startDate);
            startDate = formatter.parse(dateOnly + " 00:00");
            dateOnly = dateFormatter.format(endDate);
            endDate = formatter.parse(dateOnly + " 23:59");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ArrayList<LogbookEntry> arr = new ArrayList<>();
        try {
            String dateOnly = dateFormatter.format(startDate);
            Date start = formatter.parse(dateOnly + " 00:00");
            dateOnly = dateFormatter.format(endDate);
            Date end = formatter.parse(dateOnly + " 00:00");

            for (LogbookEntry l : logbookEntries) {
                dateOnly = dateFormatter.format(l.getDatetime());
                try {
                    Date date = dateFormatter.parse(dateOnly + " 00:00");
                    if (date.compareTo(start) >= 0 && date.compareTo(end) <= 0) {
                        arr.add(l);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return arr;
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