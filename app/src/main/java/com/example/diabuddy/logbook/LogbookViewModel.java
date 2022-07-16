package com.example.diabuddy.logbook;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.diabuddy.foodtemplate.FoodItem;
import com.example.diabuddy.foodtemplate.Meal;
import com.example.diabuddy.messages.MessagesViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class LogbookViewModel extends ViewModel {
    private final CollectionReference logbookDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("logbook");
    private final DocumentReference settingsDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final CollectionReference messagesDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("messages");
    private final CollectionReference templateDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("template");
    private final DocumentReference challengeDB = FirebaseFirestore.getInstance().collection("users")
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
    private final String SEVENTY_KEY = "challenges-70";
    private final String EIGHTY_KEY = "challenges-80";
    private final String NINETY_KEY = "challenges-90";
    private final String HUNDRED_KEY = "challenges-100";
    private final String NOTES_CHALLENGE_KEY = "challenges-notes";
    private final String WARNED_FREQ_KEY = "warnedFreq";
    private final String MESSAGES_KEY = "messages";
    private final String GROUPS_KEY = "groups";
    private final String GROUP_KEY = "group";
    private final String CONTENT_KEY = "content";
    private final String TYPE_KEY = "type";
    private final String INDEX_KEY = "index";
    private final String ONE_FOOD_KEY = "food";
    private final String VARIABLES_KEY = "variables";

    private MutableLiveData<Double> lowerBound = new MutableLiveData<>();
    private MutableLiveData<Double> upperBound = new MutableLiveData<>();
    private String glucoseUnit = "mmol/L";
    private int logbookSize = -1;
    private SimpleDateFormat gmtFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // set in the constructor
    private SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy");
    private boolean warnedFreq = false;
    private int size = 0;
    private Date curDate = new Date();

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

        logbookDB.document(VARIABLES_KEY).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    warnedFreq = (boolean) task.getResult().get(WARNED_FREQ_KEY);
                    size = (int) task.getResult().get(SIZE_KEY);
                }
            }
        });
        logbookDB.document(VARIABLES_KEY).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null && value != null && value.exists()) {
                    warnedFreq = (boolean) value.get(WARNED_FREQ_KEY);
                    size = (int) value.get(SIZE_KEY);
                }
            }
        });

        logbookDB.whereGreaterThanOrEqualTo(DATE_KEY, startOfDay(new Date()))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<LogbookEntry> arr = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                arr.add(document.toObject(LogbookEntry.class));
                            }
                        }
                        logbookEntries.setValue(arr);
                    }
                });

        templateDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<FoodItem> foodList = new ArrayList<>();
                ArrayList<Meal> mealList = new ArrayList<>();
                if (task.isSuccessful()) {
                    try {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int i = (Integer) document.getData().get(INDEX_KEY);
                            String name = (String) document.getData().get(NAME_KEY);
                            if (document.getId().contains(ONE_FOOD_KEY)) {
                                double carbs = (Double) document.getData().get(CARBS_KEY);
                                foodList.add(new FoodItem(i, name, carbs));
                            } else {
                                List<Integer> arr = (List<Integer>) document.getData().get(LIST_KEY);
                                mealList.add(new Meal(i, name, indexToItem(arr)));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("LogbookViewModel", e.getMessage());
                    }
                }
                Collections.sort(foodList);
                Collections.sort(mealList);
                foodItems.setValue(foodList);
                meals.setValue(mealList);
            }
        });
        templateDB.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                ArrayList<FoodItem> foodList = new ArrayList<>();
                ArrayList<Meal> mealList = new ArrayList<>();
                if (e == null) {
                    try {
                        for (QueryDocumentSnapshot document : value) {
                            int i = (Integer) document.getData().get(INDEX_KEY);
                            String name = (String) document.getData().get(NAME_KEY);
                            if (document.getId().contains(ONE_FOOD_KEY)) {
                                double carbs = (Double) document.getData().get(CARBS_KEY);
                                foodList.add(new FoodItem(i, name, carbs));
                            } else {
                                List<Integer> arr = (List<Integer>) document.getData().get(LIST_KEY);
                                mealList.add(new Meal(i, name, indexToItem(arr)));
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("PageViewModel", ex.getMessage());
                    }
                }
                Collections.sort(foodList);
                Collections.sort(mealList);
                foodItems.setValue(foodList);
                meals.setValue(mealList);
            }
        });

        curDate = startOfDay(new Date());

    }

    public int loadNextPage() {
        if (curDate.before(new Date(1))) return -1;
        Date prvDate = curDate;
        logbookDB.whereLessThan(DATE_KEY, curDate)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                curDate = new Date(0);
                            } else {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    Date date = (Date) doc.getData().get(DATE_KEY);
                                    if (curDate.after(date)) {
                                        curDate = date;
                                    }
                                }
                            }
                        }
                    }
                });
        if (curDate.before(new Date(1))) return -1;


        ArrayList<LogbookEntry> list = new ArrayList<>();

        logbookDB.whereGreaterThanOrEqualTo(DATE_KEY, curDate)
                .whereLessThanOrEqualTo(DATE_KEY, prvDate)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                list.add(doc.toObject(LogbookEntry.class));
                            }
                        }

                    }
                });
        int ret = list.size();
        list.addAll(logbookEntries.getValue());
        Collections.sort(list);
        logbookEntries.setValue(list);
        return ret;
    }

    public String createNewEntry() { // return the key of the new entry
        String key = size + "";
        LogbookEntry cur = new LogbookEntry(key);
        saveEdits(cur);
        Map<String, Object> map = new HashMap<>();
        map.put(SIZE_KEY, size+1);
        map.put(WARNED_FREQ_KEY,false);
        logbookDB.document(VARIABLES_KEY).set(map);
        return key;
    }

    public void saveEdits(LogbookEntry logbookEntry) {
        String key = logbookEntry.getKey();
        logbookDB.document(key).set(logbookEntry);
        updateChallenge(logbookEntry.getDatetime());
    }

    public LogbookEntry getEntry(String key) {
        ArrayList<LogbookEntry> list = new ArrayList<>();
        try {
            DocumentReference docRef = logbookDB.document(key);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    list.add(documentSnapshot.toObject(LogbookEntry.class));
                }
            });
        } catch (Exception e) {
            Log.e("LogbookViewModel", e.getMessage());
        }
        if (list.size() > 0) return list.get(0);
        return null;
    }

    private void updateChallenge(Date date) {
        challengeDB.update(SEVENTY_KEY, FieldValue.arrayRemove(date));
        challengeDB.update(EIGHTY_KEY, FieldValue.arrayRemove(date));
        challengeDB.update(NINETY_KEY, FieldValue.arrayRemove(date));
        challengeDB.update(HUNDRED_KEY, FieldValue.arrayRemove(date));
        challengeDB.update(NOTES_CHALLENGE_KEY, FieldValue.arrayRemove(date));

        logbookDB.whereGreaterThanOrEqualTo(DATE_KEY, startOfDay(date))
                .whereLessThanOrEqualTo(DATE_KEY, endOfDay(date))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int inRange = 0, total = 0, notes = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                double reading = document.getDouble(READING_KEY);
                                if (reading > 0) {
                                    total++;
                                    if (reading >= lowerBound.getValue() && reading <= upperBound.getValue()) inRange++;
                                }
                                if (!((String) document.get(NOTES_KEY)).equals("")) notes++;
                            }
                            double frac = (double) inRange / (double) total;
                            if (frac >= 0.7 && frac < 0.8) challengeDB.update(SEVENTY_KEY, FieldValue.arrayUnion(date));
                            else if (frac >= 0.8 && frac < 0.9) challengeDB.update(EIGHTY_KEY, FieldValue.arrayUnion(date));
                            else if (frac >= 0.9 && frac < 1.0) challengeDB.update(NINETY_KEY, FieldValue.arrayUnion(date));
                            else if (frac == 1.0) challengeDB.update(HUNDRED_KEY, FieldValue.arrayUnion(date));
                            if (notes == task.getResult().size()) challengeDB.update(NOTES_CHALLENGE_KEY, FieldValue.arrayUnion(date));
                        }
                    }
                });
    }

    public void deleteEntry(String key) {
        logbookDB.document(key).delete();
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
        Map<String, Object> map = new HashMap<>();
        map.put(WARNED_FREQ_KEY, true);
        logbookDB.document(WARNED_FREQ_KEY).update(map);

        messagesDB.document(MESSAGES_KEY).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    try {
                        List<DocumentReference> groups = (List<DocumentReference>) task.getResult().get(GROUPS_KEY);
                        int index = groups.size()-1;
                        DocumentReference lastGroup = groups.get(index);
                        lastGroup.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                if (task1.isSuccessful()) {
                                    List<Integer> typeList = (List<Integer>) task1.getResult().get(TYPE_KEY);
                                    if (typeList.size() == MessagesViewModel.GROUP_SIZE) {
                                        DocumentReference ref = addMessageToGroup(code, s, index+1);
                                        messagesDB.document(MESSAGES_KEY).update(GROUPS_KEY, FieldValue.arrayUnion(ref));
                                    } else {
                                        addMessageToGroup(code, s, index);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("MessagesViewModel", e.getMessage());
                    }
                }
            }
        });
    }

    private DocumentReference addMessageToGroup(int type, String msg, int group) {
        messagesDB.document(GROUP_KEY + group).update(CONTENT_KEY, FieldValue.arrayUnion(msg));
        messagesDB.document(GROUP_KEY + group).update(TYPE_KEY, FieldValue.arrayUnion(type));
        return messagesDB.document(GROUP_KEY + group);
    }

    protected ArrayList<FoodItem> indexToItem(List<Integer> list) {
        ArrayList<FoodItem> ans = new ArrayList<>();
        for (Integer i : list) ans.add(foodItems.getValue().get(i));
        return ans;
    }

    protected List<Integer> itemToIndex(ArrayList<FoodItem> list) {
        List<Integer> ans = Arrays.asList();
        for (FoodItem f : list) ans.add(f.getId());
        return ans;
    }

    public boolean isWarnedFreq() {
        return warnedFreq;
    }

    private Date startOfDay(Date date) {
        ZonedDateTime dayInZone = date.toInstant().atZone(ZoneId.systemDefault());
        LocalDateTime startOfDay = dayInZone.toLocalDate().atStartOfDay();
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Date endOfDay(Date date) {
        ZonedDateTime dayInZone = date.toInstant().atZone(ZoneId.systemDefault());
        LocalDateTime endOfDay = dayInZone.toLocalDate().atTime(LocalTime.MAX);
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }
}