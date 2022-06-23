package com.example.diabuddy.logbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.diabuddy.LoginActivity;
import com.example.diabuddy.R;
import com.example.diabuddy.foodtemplate.FoodItem;
import com.example.diabuddy.foodtemplate.Meal;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.slider.Slider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class EditActivity extends AppCompatActivity {

    private int index = -1;
    private final int REQ_CODE_SPEECH_READING = 200;
    private final int REQ_CODE_SPEECH_FOOD = 201;
    private final int REQ_CODE_SPEECH_NOTES = 202;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeOnlyFormat = new SimpleDateFormat("HH:mm");
    private ArrayList<FoodItem> foodItemArrayList = new ArrayList<>();
    private ArrayList<Meal> mealArrayList = new ArrayList<>();
    private ArrayList<String> foodNames = new ArrayList<>();
    private ArrayList<String> mealNames = new ArrayList<>();
    private ArrayAdapter<String> foodAdapter;
    private ArrayAdapter<String> mealAdapter;
    private LogbookEntry logbookEntry;
    private EditText dateET, timeET, readingET, bolusET, corrET, basalET, carbsET, foodET, exerciseET, notesET;
    private Slider intensitySlider;
    private ImageButton readingStt, foodStt, notesStt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setSupportActionBar(findViewById(R.id.edit_toolbar));

        if (getIntent() == null) return;
        if (getIntent().getExtras() != null) {
            index = getIntent().getIntExtra("index", -1);
        }
        if (index == -1) {
            Toast.makeText(this, "Logbook entry not found", Toast.LENGTH_LONG).show();
            finish();
        } else {
            LogbookEntry fromVM = LogbookListFragment.vm.getEntry(index);
            if (fromVM == null) fromVM = new LogbookEntry(index); // not yet created when you add a new entry
            logbookEntry = new LogbookEntry(fromVM);
            // datetime
            dateET = findViewById(R.id.date_edit_text);
            dateET.setText(dateOnlyFormat.format(logbookEntry.getDatetime()));
            timeET = findViewById(R.id.time_edit_text);
            timeET.setText(timeOnlyFormat.format(logbookEntry.getDatetime()));
            dateET.setFocusable(false);
            dateET.setClickable(true);
            timeET.setFocusable(false);
            timeET.setClickable(true);
            // datetime pickers
            final Calendar calendarDate = Calendar.getInstance();
            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendarDate.set(Calendar.YEAR,year);
                    calendarDate.set(Calendar.MONTH,month);
                    calendarDate.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                    dateET.setText(dateOnlyFormat.format(calendarDate.getTime()));
                }
            };
            dateET.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(EditActivity.this, dateSetListener,calendarDate.get(Calendar.YEAR),
                            calendarDate.get(Calendar.MONTH),calendarDate.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
            final Calendar calendarTime = Calendar.getInstance();
            TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    calendarTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendarTime.set(Calendar.MINUTE, minute);
                    timeET.setText(timeOnlyFormat.format(calendarTime.getTime()));
                }
            };
            timeET.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new TimePickerDialog(EditActivity.this, timeSetListener, calendarTime.get(Calendar.HOUR_OF_DAY),
                            calendarTime.get(Calendar.MINUTE), true).show();
                }
            });

            // glucose reading
            readingET = findViewById(R.id.reading_edit_text);
            readingET.setText(String.valueOf(logbookEntry.getReading()));
            readingET.setHint(getResources().getString(R.string.blood_glucose_reading) + " (" + LogbookListFragment.vm.getGlucoseUnit() + ")");

            // insulin
            bolusET = findViewById(R.id.bolus_edit_text);
            corrET = findViewById(R.id.corr_edit_text);
            basalET = findViewById(R.id.basal_edit_text);
            bolusET.setText(String.valueOf(logbookEntry.getBolus()));
            corrET.setText(String.valueOf(logbookEntry.getCorrection()));
            basalET.setText(String.valueOf(logbookEntry.getBasal()));

            TextView totalInsulinTV = findViewById(R.id.insulin_total_number_tv);
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    try {

                        double value = (bolusET.getText().toString().isEmpty() ? 0 : Double.parseDouble(bolusET.getText().toString()))
                                + (corrET.getText().toString().isEmpty() ? 0 : Double.parseDouble(corrET.getText().toString()))
                                + (basalET.getText().toString().isEmpty() ? 0 : Double.parseDouble(basalET.getText().toString()));
                        totalInsulinTV.setText(value + "");
                    } catch (NumberFormatException ex) {
                        totalInsulinTV.setText("-");
                    }
                }
            };
            bolusET.addTextChangedListener(watcher);
            corrET.addTextChangedListener(watcher);
            basalET.addTextChangedListener(watcher);
            double value = Double.parseDouble(bolusET.getText().toString()) + Double.parseDouble(corrET.getText().toString()) + Double.parseDouble(basalET.getText().toString());
            totalInsulinTV.setText(value + "");

            // food
            carbsET = findViewById(R.id.carbs_edit_text);
            carbsET.setText(String.valueOf(logbookEntry.getCarbs()));
            foodET = findViewById(R.id.food_edit_text);
            foodET.setText(String.valueOf(logbookEntry.getFood()));
            Button foodTemplateBtn = findViewById(R.id.food_template_button);
            foodAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, foodNames);
            mealAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mealNames);
            final Observer<ArrayList<FoodItem>> foodObserver = new Observer<ArrayList<FoodItem>>() {
                @Override
                public void onChanged(ArrayList<FoodItem> foodItems) {
                    foodItemArrayList.clear();
                    foodItemArrayList.addAll(foodItems);
                    // System.out.println("HELLO " + foodItemArrayList);
                    foodNames.clear();
                    for (FoodItem f : foodItems) {
                        foodNames.add(f.getName());
                    }
                    foodAdapter.notifyDataSetChanged();
                }
            };
            LogbookListFragment.vm.getFoodItems().observe(this, foodObserver);
            final Observer<ArrayList<Meal>> mealObserver = new Observer<ArrayList<Meal>>() {
                @Override
                public void onChanged(ArrayList<Meal> meals) {
                    mealArrayList.clear();
                    mealArrayList.addAll(meals);
                    System.out.println("HELLO " + mealArrayList);
                    mealNames.clear();
                    for (Meal m : meals) {
                        mealNames.add(m.getName());
                    }
                    mealAdapter.notifyDataSetChanged();
                }
            };
            LogbookListFragment.vm.getMeals().observe(this, mealObserver);
            foodTemplateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFoodTemplateDialog(carbsET, foodET);
                }
            });

            // exercise
            exerciseET = findViewById(R.id.exercise_edit_text);
            exerciseET.setText(String.valueOf(logbookEntry.getExercise()));
            intensitySlider = findViewById(R.id.intensity_slider);
            intensitySlider.setValue(logbookEntry.getIntensity());

            // notes
            notesET = findViewById(R.id.notes_edit_text);
            notesET.setText(logbookEntry.getNotes());

            // save/discard/delete
            ExtendedFloatingActionButton saveBtn = findViewById(R.id.logbook_save);
            ExtendedFloatingActionButton discardBtn = findViewById(R.id.logbook_cancel);
            ExtendedFloatingActionButton deleteBtn = findViewById(R.id.logbook_delete);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    save();
                }
            });
            discardBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogbookListFragment.vm.saveEdits(logbookEntry);
                    finish();
                }
            });
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogbookListFragment.vm.deleteEntry(index);
                    finish();
                }
            });

            readingStt = findViewById(R.id.reading_stt);
            readingStt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

                    try {
                        startActivityForResult(i, REQ_CODE_SPEECH_READING);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(EditActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            foodStt = findViewById(R.id.food_stt);
            foodStt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

                    try {
                        startActivityForResult(i, REQ_CODE_SPEECH_FOOD);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(EditActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            notesStt = findViewById(R.id.notes_stt);
            notesStt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

                    try {
                        startActivityForResult(i, REQ_CODE_SPEECH_NOTES);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(EditActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void warning(int code) {
        String[] arr = getResources().getStringArray((code == 5 ? R.array.very_low_warnings : R.array.very_high_warnings));
        LogbookListFragment.vm.warn(code, arr[(int) Math.floor(Math.random() * arr.length)]);

        int notifID = 102;
        String channelID = "com.example.diabuddy2021.warnings";
        Context context = getApplicationContext();
        Intent i = new Intent(context, LoginActivity.class); // redirect to MessagesActivity after login
        i.putExtra("type", "warning"); // can give the same thing
        PendingIntent pending = PendingIntent.getActivity(context, 1, i, FLAG_UPDATE_CURRENT); // have different notifs for the different purposes
        String str = getResources().getString((code == 5 ? R.string.hypo_text : R.string.hyper_text));
        Notification notification = new Notification.Builder(context, channelID)
                .setContentTitle(getResources().getString(R.string.warning_title))
                .setContentText(str)
                .setSmallIcon(R.drawable.ic_notifications_active_white_24dp)
                .setStyle(new Notification.BigTextStyle().bigText(str))
                .setContentIntent(pending)
                .setChannelId(channelID).build();

        NotificationManager nm = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = nm.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelID, getResources().getString(R.string.diabuddy_warnings), importance);
        nm.createNotificationChannel(channel);

        nm.notify(notifID, notification);
    }

    private void openFoodTemplateDialog(EditText carbsET, EditText foodET) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.dialog_food_template,null);

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

        RadioButton foodItemRadio = dialogView.findViewById(R.id.food_item_radio);
        RadioButton mealRadio = dialogView.findViewById(R.id.meal_radio);
        Spinner spinner = dialogView.findViewById(R.id.template_spinner);
        foodAdapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
        mealAdapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
        foodItemRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setAdapter(foodAdapter);
            }
        });
        mealRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setAdapter(mealAdapter);
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        foodItemRadio.performClick();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getAdapter().isEmpty()) {
                    dialog.dismiss();
                    return;
                }
                String rawSelection = String.valueOf( spinner.getAdapter().getItem(spinner.getSelectedItemPosition()) );
                double carbs = Double.parseDouble(carbsET.getText().toString());
                String food = foodET.getText().toString();
                if (foodItemRadio.isChecked()) {
                    System.out.println(rawSelection + " FOOD");
                    for (FoodItem f : foodItemArrayList) {
                        System.out.println(f.getName());
                        if (f.getName().equals(rawSelection)) {
                            carbs += f.getCarbs();
                            carbsET.setText(carbs + "");
                            if (!food.equals("")) {
                                food += ", ";
                            }
                            food += f.toString();
                            foodET.setText(food);
                            dialog.dismiss();
                            return;
                        }
                    }
                } else {
                    System.out.println(rawSelection + " MEAL");
                    for (Meal m : mealArrayList) {
                        System.out.println(m.getName());
                        if (m.getName().equals(rawSelection)) {
                            carbs += m.getCarbs();
                            carbsET.setText(carbs + "");
                            if (!food.equals("")) {
                                food += ", ";
                            }
                            food += m.toString();
                            foodET.setText(food);
                            dialog.dismiss();
                            return;
                        }
                    }
                }

                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.save_logbook_edit_title))
        .setMessage(getResources().getString(R.string.save_logbook_edit_content))
        .setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                save();
            }
        })
        .setNegativeButton(getResources().getString(R.string.discard), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogbookListFragment.vm.saveEdits(logbookEntry);
                finish();
            }
        });
        builder.create().show();
    }

    private void save() {
        try {
            logbookEntry.setDatetime(formatter.parse(dateET.getText().toString() + " " + timeET.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (readingET.getText().toString().isEmpty()) readingET.setText("0.0");
        if (bolusET.getText().toString().isEmpty()) bolusET.setText("0.0");
        if (corrET.getText().toString().isEmpty()) corrET.setText("0.0");
        if (basalET.getText().toString().isEmpty()) basalET.setText("0.0");
        if (carbsET.getText().toString().isEmpty()) carbsET.setText("0.0");
        if (exerciseET.getText().toString().isEmpty()) exerciseET.setText("0.0");

        logbookEntry.setReading(Double.parseDouble(readingET.getText().toString()));
        logbookEntry.setBolus(Double.parseDouble(bolusET.getText().toString()));
        logbookEntry.setCorrection(Double.parseDouble(corrET.getText().toString()));
        logbookEntry.setBasal(Double.parseDouble(basalET.getText().toString()));
        logbookEntry.setCarbs(Double.parseDouble(carbsET.getText().toString()));
        logbookEntry.setFood(foodET.getText().toString());
        logbookEntry.setExercise(Integer.parseInt(exerciseET.getText().toString()));
        logbookEntry.setIntensity((int) intensitySlider.getValue());
        logbookEntry.setNotes(notesET.getText().toString());

        if (LogbookListFragment.vm.getGlucoseUnit().equals(getResources().getString(R.string.mmol_l))) {
            if (logbookEntry.getReading() <= 0.0) {

            } else if (logbookEntry.getReading() < 3.0) {
                warning(5); // hypo warning
            } else if (logbookEntry.getReading() > 11.0) {
                warning(6); // hyper warning
            }
        } else {
            if (logbookEntry.getReading() <= 0.0) {

            } else if (logbookEntry.getReading() < 54) {
                warning(5); // hypo warning
            } else if (logbookEntry.getReading() > 200) {
                warning(6); // hyper warning
            }
        }

        LogbookListFragment.vm.saveEdits(logbookEntry);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_READING:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String speechReceived = result.get(0);
                    try {
                        double possible = Double.parseDouble(speechReceived);
                        readingET.setText(possible + "");
                    } catch (NumberFormatException ex) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getResources().getString(R.string.reading_wrong_format_title));
                        builder.setMessage(getResources().getString(R.string.reading_wrong_format_content));
                        builder.create().show();
                    }
                }
                break;
            case REQ_CODE_SPEECH_FOOD:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String speechReceived = result.get(0);
                    foodET.setText(speechReceived);
                }
                break;
            case REQ_CODE_SPEECH_NOTES:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String speechReceived = result.get(0);
                    notesET.setText(speechReceived);
                }
                break;
        }

    }
}