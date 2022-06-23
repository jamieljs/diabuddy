package com.example.diabuddy.foodtemplate;

import android.app.AlertDialog;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageViewModel extends ViewModel {

    private MutableLiveData<ArrayList<FoodItem>> foodItems = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<ArrayList<Meal>> meals = new MutableLiveData<>(new ArrayList<>());
    private final CollectionReference templateDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("template");
    //private final String ARRAYS_KEY = "arrays";
    private final String FOOD_ITEM_KEY = "food-items";
    private final String MEAL_KEY = "meals";
    private final String ONE_FOOD_KEY = "food";
    private final String ONE_MEAL_KEY = "meal";
    private final String INDEX_KEY = "index";
    private final String NAME_KEY = "name";
    private final String CARBS_KEY = "carbs";
    private final String LIST_KEY = "list";

    public PageViewModel() {
        templateDB.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                Log.e("PageViewModel", e.getMessage());
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
    }

    public MutableLiveData<ArrayList<FoodItem>> getFoodItems() { return foodItems; }

    public void setFoodItems(ArrayList<FoodItem> foodItems) { this.foodItems.setValue(foodItems); }

    public MutableLiveData<ArrayList<Meal>> getMeals() { return meals; }

    public void setMeals(ArrayList<Meal> meals) { this.meals.setValue(meals); }

    public boolean addFoodItem(String name, double carbs) {
        ArrayList<FoodItem> cur = foodItems.getValue();
        int maxi=0;
        for (FoodItem f : cur) {
            if (f.getName().equals(name)) {
                return false;
            }
            maxi = Math.max(maxi, f.getId());
        }
        int id = maxi+1;
        FoodItem foodItem = new FoodItem(id, name, carbs);
        cur.add(foodItem);
        foodItems.setValue(cur);

        Map<String, Object> foodMap = new HashMap<>();
        foodMap.put(INDEX_KEY, id);
        foodMap.put(NAME_KEY, name);
        foodMap.put(CARBS_KEY, carbs);
        templateDB.document(ONE_FOOD_KEY + id).set(foodMap);
        return true;
    }

    public boolean modifyFoodItem(int id, String name, double carbs) {
        ArrayList<FoodItem> cur = foodItems.getValue();
        for (FoodItem f : cur) {
            if (f.getId() != id && f.getName().equals(name)) {
                return false;
            }
        }
        cur.get(id).setName(name);
        cur.get(id).setCarbs(carbs);
        foodItems.setValue(cur);

        templateDB.document(ONE_FOOD_KEY + id).update(NAME_KEY, name, CARBS_KEY, carbs);
        return true;
    }

    public boolean addMeal(String name, ArrayList<FoodItem> arr) {
        ArrayList<Meal> cur = meals.getValue();
        int maxi=0;
        for (Meal m : cur) {
            if (m.getName().equals(name)) {
                return false;
            }
            maxi = Math.max(maxi, m.getId());
        }
        int id = maxi+1;
        Meal meal = new Meal(id, name, arr);
        cur.add(meal);
        meals.setValue(cur);

        Map<String, Object> mealMap = new HashMap<>();
        mealMap.put(INDEX_KEY, id);
        mealMap.put(NAME_KEY, name);
        mealMap.put(LIST_KEY, itemToIndex(arr));
        templateDB.document(ONE_MEAL_KEY + id).set(mealMap);
        return true;
    }

    public boolean modifyMeal(int id, String name, ArrayList<FoodItem> arr) {
        ArrayList<Meal> cur = meals.getValue();
        for (Meal m : cur) {
            if (m.getId() != id && m.getName().equals(name)) {
                return false;
            }
        }
        cur.get(id).setName(name);
        cur.get(id).setList(arr);
        meals.setValue(cur);

        templateDB.document(ONE_MEAL_KEY + id).update(NAME_KEY, name, LIST_KEY, itemToIndex(arr));
        return true;
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

}