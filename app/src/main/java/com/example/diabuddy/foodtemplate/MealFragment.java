package com.example.diabuddy.foodtemplate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.diabuddy.R;

import java.util.ArrayList;

public class MealFragment extends Fragment {

    public MealFragment() {
        // Required empty public constructor
    }

    public static MealFragment newInstance() {
        MealFragment fragment = new MealFragment();
        return fragment;
    }

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<Meal> meals = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_meal, container, false);

        TextView instructionsTV = root.findViewById(R.id.meal_instructions);
        instructionsTV.setVisibility(View.GONE);

        ConstraintLayout empty = root.findViewById(R.id.meal_empty);
        empty.setVisibility(View.GONE);

        recyclerView = root.findViewById(R.id.meal_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        final Observer<ArrayList<Meal>> observer = new Observer<ArrayList<Meal>>() {
            @Override
            public void onChanged(ArrayList<Meal> arr) {
                meals = arr;
                if (meals.isEmpty()) {
                    empty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    instructionsTV.setVisibility(View.GONE);
                } else {
                    empty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    instructionsTV.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }
        };
        SectionsPagerAdapter.vm.getMeals().observe(getViewLifecycleOwner(), observer);

        return root;
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_meal, parent,false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.curMeal = meals.get(position);
            holder.name.setText(meals.get(position).getName());
            holder.carbs.setText(meals.get(position).getCarbs() + "");
        }

        @Override
        public int getItemCount() {
            return meals.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            Meal curMeal;
            TextView name;
            TextView carbs;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.meal_name);
                carbs = itemView.findViewById(R.id.meal_carb);
                ArrayList<FoodItem> arr = new ArrayList<>();

                itemView.setOnClickListener(new View.OnClickListener(){
                    private double updateSum() {
                        double sum = 0;
                        for (int i = 0; i < arr.size(); i++) {
                            sum += arr.get(i).getCarbs();
                        }
                        return sum;
                    }

                    private void updateTableLayout(TableLayout table, TextView totalTV) {
                        while (table.getChildCount() != 1) table.removeViewAt(table.getChildCount() - 1);
                        for (int i = 0; i < arr.size(); i++) {
                            FoodItem f = arr.get(i);
                            TableRow row = new TableRow(getContext());
                            TextView nameTV = new TextView(getContext());
                            nameTV.setText(f.getName());
                            nameTV.setGravity(Gravity.CENTER);
                            nameTV.setMaxWidth(120);
                            TextView carbsTV = new TextView(getContext());
                            carbsTV.setText(f.getCarbs() + "");
                            carbsTV.setGravity(Gravity.CENTER);
                            ImageButton del = new ImageButton(getContext());
                            final int index = i;
                            del.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_white_24dp, getContext().getTheme()));
                            del.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange, getContext().getTheme())));
                            del.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black, getContext().getTheme())));
                            del.setForegroundGravity(Gravity.CENTER);
                            del.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    arr.remove(index);
                                    // reconstruct arr
                                    table.removeView(row);
                                    totalTV.setText(getString(R.string.total_carbs) + String.format(" %.2f", updateSum()));
                                    updateTableLayout(table, totalTV);
                                }
                            });
                            row.setGravity(Gravity.CENTER);
                            row.addView(nameTV);
                            row.addView(carbsTV);
                            row.addView(del);
                            table.addView(row);
                        }
                        totalTV.setText(getString(R.string.total_carbs) + String.format(" %.2f", updateSum()));
                    }

                    @Override
                    public void onClick(View view) {
                        arr.clear();
                        arr.addAll(curMeal.getList());

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        View dialogView = View.inflate(view.getContext(), R.layout.dialog_meal,null);
                        EditText nameET = dialogView.findViewById(R.id.name_edit_text);
                        nameET.setText(name.getText());
                        ScrollView mealTableScroll = dialogView.findViewById(R.id.meal_table_scroll);

                        TableLayout table = dialogView.findViewById(R.id.meal_table);
                        TextView totalTV = dialogView.findViewById(R.id.meal_total);
                        updateTableLayout(table, totalTV);

                        Spinner spinner = dialogView.findViewById(R.id.food_spinner);
                        ArrayList<FoodItem> foodItems = SectionsPagerAdapter.vm.getFoodItems().getValue();
                        String[] foodNames = new String[foodItems.size()];
                        for (int i = 0; i < foodItems.size(); i++) foodNames[i] = foodItems.get(i).getName();
                        ArrayAdapter<String> foodAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, foodNames);
                        foodAdapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
                        spinner.setAdapter(foodAdapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) { }
                        });

                        Button addBtn = dialogView.findViewById(R.id.add_food_button);
                        addBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String rawSelection = String.valueOf( spinner.getAdapter().getItem(spinner.getSelectedItemPosition()) );
                                for (int i = 0; i < foodNames.length; i++) {
                                    if (foodNames[i].equals(rawSelection)) {
                                        FoodItem f = foodItems.get(i);
                                        arr.add(f);
                                        updateTableLayout(table, totalTV);
                                        break;
                                    }
                                }
                                mealTableScroll.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mealTableScroll.fullScroll(ScrollView.FOCUS_DOWN);
                                    }
                                },50);
                            }
                        });

                        TextView errorTV = dialogView.findViewById(R.id.meal_error);
                        errorTV.setVisibility(View.GONE);

                        builder.setNegativeButton(getString(R.string.discard_edit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.setPositiveButton(getString(R.string.save_edit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.setView(dialogView);
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (nameET.getText().toString().equals("")) {
                                    errorTV.setVisibility(View.VISIBLE);
                                    errorTV.setText(getResources().getString(R.string.empty_meal_name_error));
                                } else if (arr.isEmpty()) {
                                    errorTV.setVisibility(View.VISIBLE);
                                    errorTV.setText(getResources().getString(R.string.empty_meal_list_error));
                                } else if (!SectionsPagerAdapter.vm.modifyMeal(curMeal.getId(), String.valueOf(nameET.getText()), arr)) {
                                    errorTV.setVisibility(View.VISIBLE);
                                    errorTV.setText(getResources().getString(R.string.meal_error));
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                });
            }
        }
    }
}