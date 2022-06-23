package com.example.diabuddy.logbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diabuddy.foodtemplate.FoodItem;
import com.example.diabuddy.foodtemplate.Meal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import java.util.TimeZone;

public class LogbookViewModel extends ViewModel {
    private final CollectionReference logbookDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("logbook");
    private final DocumentReference settingsDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

    private MutableLiveData<ArrayList<LogbookEntry>> logbookEntries = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<ArrayList<FoodItem>> foodItems = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<ArrayList<Meal>> meals = new MutableLiveData<>(new ArrayList<>());

    private final String LB_KEY = "hypo";
    private final String UB_KEY = "hyper";
    private final String UNIT_KEY = "bgUnit";
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
    private final String NOTES_KEY = "notes"; // USED IN MULTIPLE PLACES (logbook and challenges)
    private final String NAME_KEY = "name";
    private final String LIST_KEY = "list";
    private final String SEVENTY_KEY = "70";
    private final String EIGHTY_KEY = "80";
    private final String NINETY_KEY = "90";
    private final String HUNDRED_KEY = "100";
    private final String WARNED_FREQ_KEY = "warnedFreq";

    private MutableLiveData<Double> lowerBound = new MutableLiveData<>();
    private MutableLiveData<Double> upperBound = new MutableLiveData<>();
    private String glucoseUnit = "mmol/L";
    private int logbookSize = -1;
    private SimpleDateFormat gmtFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // set in the constructor
    private SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy");
    private boolean warnedFreq = false;

    public LogbookViewModel() {
        gmtFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        settingsDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    lowerBound.setValue((Double) document.get(LB_KEY));
                    upperBound.setValue((Double) document.get(UB_KEY));
                    glucoseUnit = (String) document.get(UNIT_KEY);
                }
            }
        });
        settingsDB.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null && value != null && value.exists()) {
                    lowerBound.setValue((Double) value.get(LB_KEY));
                    upperBound.setValue((Double) value.get(UB_KEY));
                    glucoseUnit = (String) value.get(UNIT_KEY);
                }
            }
        });


        ValueEventListener logbookListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int size = snapshot.child(SIZE_KEY).getValue(Integer.class);
                if (logbookSize != size) logbookSize = size;
                if (snapshot.hasChild(WARNED_FREQ_KEY)) warnedFreq = snapshot.child(WARNED_FREQ_KEY).getValue(Boolean.class);
                loadLogbook(snapshot, size);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        };
        dbLogbook.addListenerForSingleValueEvent(logbookListener);
        dbLogbook.addValueEventListener(logbookListener);

        ValueEventListener foodListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<FoodItem> list = new ArrayList<>();
                if (snapshot.hasChild((foodItems.getValue().size() - 1) + "") && !snapshot.child((foodItems.getValue().size() - 1) + "").hasChild(CARBS_KEY)) {
                    return;
                }
                int i = 0;
                while (snapshot.hasChild(i + "") && snapshot.child(i + "").hasChild(CARBS_KEY)) {
                    list.add(new FoodItem(i, snapshot.child(i + "").child(NAME_KEY).getValue(String.class), snapshot.child(i + "").child(CARBS_KEY).getValue(Double.class)));
                    i++;
                }
                Collections.sort(list);
                foodItems.setValue(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        dbFoodItems.addListenerForSingleValueEvent(foodListener);
        dbFoodItems.addValueEventListener(foodListener);

        ValueEventListener mealListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Meal> list = new ArrayList<>();
                if (snapshot.hasChild((meals.getValue().size() - 1) + "") && !snapshot.child((meals.getValue().size() - 1) + "").hasChild(LIST_KEY)) {
                    return;
                }
                int i = 0;
                while (snapshot.hasChild(i + "") && snapshot.child(i + "").hasChild(LIST_KEY)) {
                    String raw = snapshot.child(i + "").child(LIST_KEY).getValue(String.class);
                    String[] tokens = raw.split(",");
                    ArrayList<Integer> arr = new ArrayList<>();
                    for (String s : tokens) arr.add(Integer.parseInt(s));
                    list.add(new Meal(i, snapshot.child(i + "").child(NAME_KEY).getValue(String.class), indexToItem(arr)));
                    i++;
                }
                Collections.sort(list);
                meals.setValue(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        dbMeals.addListenerForSingleValueEvent(mealListener);
        dbMeals.addValueEventListener(mealListener);
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
                    String food = cur.child(FOOD_KEY).getValue(String.class);
                    int exercise = cur.child(EXERCISE_KEY).getValue(Integer.class);
                    int intensity = cur.child(INTENSITY_KEY).getValue(Integer.class);
                    String notes = cur.child(NOTES_KEY).getValue(String.class);
                    try {
                        Date localDate = gmtFormatter.parse(date);
                        arr.add(new LogbookEntry(i, localDate, reading, bolus, correction, basal, carbs, food, exercise, intensity, notes));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        logbookEntries.setValue(arr);

        int chalCount[] = {0, 0, 0, 0, 0}; // 70, 80, 90, 100, notes
        int start = 0;
        for (int i = 0; i < arr.size(); i++) {
            if (i < arr.size() - 1 && !dateOnlyFormat.format(arr.get(i).getDatetime()).equals(dateOnlyFormat.format(arr.get(i + 1).getDatetime()))) { // different date
                int inRange = 0, total = 0, notes = 0;
                for (int j = start; j <= i; j++) {
                    double reading = arr.get(j).getReading();
                    if (reading > 0) {
                        total++;
                        if (reading >= lowerBound.getValue() && reading <= upperBound.getValue()) inRange++;
                    }
                    if (!arr.get(j).getNotes().equals("")) notes++;
                }
                double frac = (double) inRange / (double) total;
                if (frac >= 0.7 && frac < 0.8) chalCount[0]++;
                else if (frac >= 0.8 && frac < 0.9) chalCount[1]++;
                else if (frac >= 0.9 && frac < 1.0) chalCount[2]++;
                else if (frac == 1.0) chalCount[3]++;
                if (notes == i - start + 1) chalCount[4]++;
                start = i + 1;
            }
        }
        dbChallenges.child(SEVENTY_KEY).setValue(chalCount[0]);
        dbChallenges.child(EIGHTY_KEY).setValue(chalCount[1]);
        dbChallenges.child(NINETY_KEY).setValue(chalCount[2]);
        dbChallenges.child(HUNDRED_KEY).setValue(chalCount[3]);
        dbChallenges.child(NOTES_KEY).setValue(chalCount[4]);
    }

    public int createNewEntry() { // return the key of the new entry
        int key = logbookSize;
        LogbookEntry cur = new LogbookEntry(key);
        saveEdits(cur);
        dbLogbook.child(SIZE_KEY).setValue(key + 1);
        dbLogbook.child(WARNED_FREQ_KEY).setValue(false);
        return key;
    }

    public void saveEdits(LogbookEntry logbookEntry) {
        int key = logbookEntry.getKey();
        DatabaseReference cur = dbLogbook.child(key + "");
        cur.child(DATE_KEY).setValue(gmtFormatter.format(logbookEntry.getDatetime()));
        cur.child(READING_KEY).setValue(logbookEntry.getReading());
        cur.child(BOLUS_KEY).setValue(logbookEntry.getBolus());
        cur.child(CORRECTION_KEY).setValue(logbookEntry.getCorrection());
        cur.child(BASAL_KEY).setValue(logbookEntry.getBasal());
        cur.child(CARBS_KEY).setValue(logbookEntry.getCarbs());
        cur.child(FOOD_KEY).setValue(logbookEntry.getFood());
        cur.child(EXERCISE_KEY).setValue(logbookEntry.getExercise());
        cur.child(INTENSITY_KEY).setValue(logbookEntry.getIntensity());
        cur.child(NOTES_KEY).setValue(logbookEntry.getNotes());
    }

    public LogbookEntry getEntry(int key) {
        for (LogbookEntry l : logbookEntries.getValue()) {
            if (l.getKey() == key) return l;
        }
        return null;
    }

    public void deleteEntry(int key) {
        dbLogbook.child(key + "").removeValue();
    }

    public MutableLiveData<ArrayList<LogbookEntry>> getLogbookEntries() { return logbookEntries; }

    public MutableLiveData<ArrayList<FoodItem>> getFoodItems() { return foodItems; }

    public MutableLiveData<ArrayList<Meal>> getMeals() { return meals; }

    public String getGlucoseUnit() { return glucoseUnit; }

    public MutableLiveData<Double> getLowerBound() {
        return lowerBound;
    }

    public MutableLiveData<Double> getUpperBound() {
        return upperBound;
    }

    public void warn(int code, String s) {
        dbLogbook.child(WARNED_FREQ_KEY).setValue(true);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int size = snapshot.child("size").getValue(Integer.class);
                dbMessages.child(size + "").child("type").setValue(code);
                dbMessages.child(size + "").child("content").setValue(s);
                dbMessages.child("size").setValue(size + 1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        dbMessages.addListenerForSingleValueEvent(listener);
    }

    protected ArrayList<FoodItem> indexToItem(ArrayList<Integer> list) {
        ArrayList<FoodItem> ans = new ArrayList<>();
        for (Integer i : list) ans.add(foodItems.getValue().get(i));
        return ans;
    }

    public boolean isWarnedFreq() {
        return warnedFreq;
    }
}