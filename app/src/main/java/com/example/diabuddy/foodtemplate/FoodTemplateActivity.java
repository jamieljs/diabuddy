package com.example.diabuddy.foodtemplate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FoodTemplateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_template);
        setSupportActionBar(findViewById(R.id.template_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getStringExtra("toAdd") != null) {
            double carbs = getIntent().getDoubleExtra("carbs", 0.0);
            openNewFoodItem(getIntent().getStringExtra("toAdd"), carbs);
        }

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.setSelectedTabIndicatorColor(getColor(R.color.white));
        tabs.setTabTextColors(getColor(R.color.teal_dark), getColor(R.color.white));

        ArrayList<FoodItem> arr = new ArrayList<>();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
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
                    TableRow row = new TableRow(FoodTemplateActivity.this);
                    TextView nameTV = new TextView(FoodTemplateActivity.this);
                    nameTV.setText(f.getName());
                    nameTV.setGravity(Gravity.CENTER);
                    nameTV.setMaxWidth(120);
                    TextView carbsTV = new TextView(FoodTemplateActivity.this);
                    carbsTV.setText(f.getCarbs() + "");
                    carbsTV.setGravity(Gravity.CENTER);
                    ImageButton del = new ImageButton(FoodTemplateActivity.this);
                    final int index = i;
                    del.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_white_24dp, FoodTemplateActivity.this.getTheme()));
                    del.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange, FoodTemplateActivity.this.getTheme())));
                    del.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black, FoodTemplateActivity.this.getTheme())));
                    del.setForegroundGravity(Gravity.CENTER);
                    del.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            arr.remove(index);
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
                if (tabs.getSelectedTabPosition() == 0) {
                    openNewFoodItem("", 0.0);
                } else {
                    arr.clear();

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    View dialogView = View.inflate(view.getContext(), R.layout.dialog_meal, null);
                    EditText nameET = dialogView.findViewById(R.id.name_edit_text);
                    nameET.setText("");
                    ScrollView mealTableScroll = dialogView.findViewById(R.id.meal_table_scroll);

                    TableLayout table = dialogView.findViewById(R.id.meal_table);
                    TextView totalTV = dialogView.findViewById(R.id.meal_total);
                    updateTableLayout(table, totalTV);

                    Spinner spinner = dialogView.findViewById(R.id.food_spinner);
                    ArrayList<FoodItem> foodItems = SectionsPagerAdapter.vm.getFoodItems().getValue();
                    String[] foodNames = new String[foodItems.size()];
                    for (int i = 0; i < foodItems.size(); i++)
                        foodNames[i] = foodItems.get(i).getName();
                    ArrayAdapter<String> foodAdapter = new ArrayAdapter<String>(FoodTemplateActivity.this, android.R.layout.simple_spinner_item, foodNames);
                    foodAdapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
                    spinner.setAdapter(foodAdapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                    Button addBtn = dialogView.findViewById(R.id.add_food_button);
                    addBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String rawSelection = String.valueOf(spinner.getAdapter().getItem(spinner.getSelectedItemPosition()));
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
                            } else if (!SectionsPagerAdapter.vm.addMeal(String.valueOf(nameET.getText()), arr)) {
                                errorTV.setVisibility(View.VISIBLE);
                                errorTV.setText(getResources().getString(R.string.meal_error));
                            } else {
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    private void openNewFoodItem(String name, double carbs) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.dialog_food_item, null);
        EditText nameET = dialogView.findViewById(R.id.name_edit_text);
        nameET.setText(name);
        EditText carbsET = dialogView.findViewById(R.id.carbs_edit_text);
        carbsET.setText((carbs == 0.0 ? "" : carbs + ""));
        TextView errorTV = dialogView.findViewById(R.id.food_item_error);

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
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
                    errorTV.setText(getResources().getString(R.string.empty_food_name_error));
                } else if (carbsET.getText().toString().equals("")) {
                    errorTV.setVisibility(View.VISIBLE);
                    errorTV.setText(getResources().getString(R.string.empty_food_carbs_error));
                } else if (!SectionsPagerAdapter.vm.addFoodItem(String.valueOf(nameET.getText()), Double.parseDouble(String.valueOf(carbsET.getText())))) {
                    errorTV.setVisibility(View.VISIBLE);
                    errorTV.setText(getResources().getString(R.string.food_item_error));
                } else {
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}