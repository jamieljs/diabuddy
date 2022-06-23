package com.example.diabuddy.nutrition;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class NutritionViewModel extends ViewModel {

    private final DocumentReference challengeDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

    private static final String ACCESS_POINT = "https://api.edamam.com/api/food-database/v2/parser";
    private static final String APP_ID = "a5aaf84e";
    private static final String APP_KEY = "242015fc501fc80dbfa8ca09dd5db57b";
    private static final String HINTS = "hints";
    private static final String FOOD = "food";
    private static final String LABEL = "label";
    private static final String FOOD_CONTENTS_LABEL = "foodContentsLabel";
    private static final String NUTRIENTS = "nutrients";
    private static final String[] NUTRIENT_LIST = {"CHOCDF", "ENERC_KCAL", "PROCNT", "FAT"};
    private static final String[] NUTRIENT_VIEW = {"Carbohydrates (grams)", "Energy (kcal)", "Protein (grams)", "Fat (grams)"};

    public NutritionViewModel() {

    }

    private JSONArray foods;
    public ArrayList<Pair<String, String>> query(String query) {
        ArrayList<Pair<String, String>> answer = new ArrayList<>();
        HttpsURLConnection connection = null;
        int responseCode = 0;
        try {
            connection = (HttpsURLConnection) new URL(ACCESS_POINT + "?app_id=" + APP_ID + "&app_key=" + APP_KEY + "&ingr=" + query).openConnection();
            connection.setRequestMethod("GET");
            responseCode = connection.getResponseCode();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (responseCode == 200) {
            StringBuilder response = new StringBuilder();
            try {
                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                byte[] contents = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = in.read(contents)) != -1) {
                    response.append(new String(contents, 0, bytesRead));
                }
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                foods = (new JSONObject(response.toString())).getJSONArray(HINTS);
                for (int i = 0; i < foods.length(); i++) {
                    JSONObject food = foods.getJSONObject(i).getJSONObject(FOOD);
                    String description = "-";
                    if (food.has(LABEL)) {
                        description = food.getString(LABEL);
                    }
                    String additional = "-";
                    if (food.has(FOOD_CONTENTS_LABEL)) {
                        additional = food.getString(FOOD_CONTENTS_LABEL);
                    }
                    answer.add(new Pair<>(description, additional));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return answer;
    }

    public ArrayList<String> viewNutrients(int index) {
        ArrayList<String> answer = new ArrayList<>();
        try {
            JSONObject nutrients = foods.getJSONObject(index).getJSONObject(FOOD).getJSONObject(NUTRIENTS);
            for (int i = 0; i < NUTRIENT_LIST.length; i++) {
                if (!nutrients.has(NUTRIENT_LIST[i])) answer.add(String.format("%s: -", NUTRIENT_VIEW[i]));
                else answer.add(String.format("%s: %.2f", NUTRIENT_VIEW[i], nutrients.getDouble(NUTRIENT_LIST[i])));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        challengeDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Document found in the offline cache
                    DocumentSnapshot document = task.getResult();
                    challengeDB.update("challenges-nutrition", (Integer) document.get("challenges-nutrition"));
                }
            }
        });
        return answer;
    }

}