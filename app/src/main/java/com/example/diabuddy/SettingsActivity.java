package com.example.diabuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {


    private final DocumentReference settingsDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final String GLUCOSE_UNIT_KEY = "bgUnit";
    private final String LOWER_BOUND_KEY = "hypo";
    private final String UPPER_BOUND_KEY = "hyper";
    private final String DIAGNOSIS_KEY = "diagnosis";
    private final String DOB_KEY = "dob";
    private final String WEIGHT_KEY = "weight";
    private final String HEIGHT_KEY = "height";

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(findViewById(R.id.settings_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((EditText)findViewById(R.id.display_name_tv)).setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        ((EditText)findViewById(R.id.email_tv)).setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        EditText glucoseUnitTV = findViewById(R.id.glucose_unit_tv);
        EditText lowerBoundET = findViewById(R.id.lower_bound_et);
        EditText upperBoundET = findViewById(R.id.upper_bound_et);
        EditText diagnosisET = findViewById(R.id.diagnosis_et);
        EditText dobET = findViewById(R.id.dob_et);
        EditText weightET = findViewById(R.id.weight_et);
        EditText heightET = findViewById(R.id.height_et);

        MutableLiveData<Double> curLB = new MutableLiveData<>(), curUB = new MutableLiveData<>(), curWeight = new MutableLiveData<>();
        MutableLiveData<Integer> curDiagnosis = new MutableLiveData<>(), curHeight = new MutableLiveData<>();
        MutableLiveData<String> curDob = new MutableLiveData<>();

        settingsDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    glucoseUnitTV.setText((String) task.getResult().get(GLUCOSE_UNIT_KEY));
                    curLB.setValue((Double) task.getResult().get(LOWER_BOUND_KEY));
                    lowerBoundET.setText(curLB.getValue() + "");
                    curUB.setValue((Double) task.getResult().get(UPPER_BOUND_KEY));
                    upperBoundET.setText(curUB.getValue() + "");
                    curDiagnosis.setValue((Integer) task.getResult().get(DIAGNOSIS_KEY));
                    diagnosisET.setText(curDiagnosis.getValue() + "");
                    curDob.setValue((String) task.getResult().get(DOB_KEY));
                    dobET.setText(curDob.getValue());
                    curWeight.setValue((Double) task.getResult().get(WEIGHT_KEY));
                    weightET.setText(curWeight.getValue() + "");
                    curHeight.setValue((Integer) task.getResult().get(HEIGHT_KEY));
                    heightET.setText(curHeight.getValue() + "");
                }
            }
        });
        settingsDB.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null && value != null && value.exists()) {
                    glucoseUnitTV.setText((String) value.get(GLUCOSE_UNIT_KEY));
                    curLB.setValue((Double) value.get(LOWER_BOUND_KEY));
                    lowerBoundET.setText(curLB.getValue() + "");
                    curUB.setValue((Double) value.get(UPPER_BOUND_KEY));
                    upperBoundET.setText(curUB.getValue() + "");
                    curDiagnosis.setValue((Integer) value.get(DIAGNOSIS_KEY));
                    diagnosisET.setText(curDiagnosis.getValue() + "");
                    curDob.setValue((String) value.get(DOB_KEY));
                    dobET.setText(curDob.getValue());
                    curWeight.setValue((Double) value.get(WEIGHT_KEY));
                    weightET.setText(curWeight.getValue() + "");
                    curHeight.setValue((Integer) value.get(HEIGHT_KEY));
                    heightET.setText(curHeight.getValue() + "");
                }
            }
        });

        lowerBoundET.setFocusable(false);
        lowerBoundET.setClickable(true);
        lowerBoundET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                String string = getResources().getString(R.string.hypo_threshold) + " (" + glucoseUnitTV.getText() + "): " + getResources().getString(R.string.hypo_threshold_desc);
                if (glucoseUnitTV.getText().toString().equals(getResources().getString(R.string.mmol_l))) {
                    View dialogView = View.inflate(SettingsActivity.this, R.layout.dialog_decimal_picker,null);
                    NumberPicker np = dialogView.findViewById(R.id.np);
                    np.setMinValue(4);
                    np.setMaxValue(5);
                    np.setValue((int) (curLB.getValue() * 10) / 10);
                    NumberPicker dp = dialogView.findViewById(R.id.dp);
                    dp.setMinValue(0);
                    dp.setMaxValue(9);
                    dp.setValue((int) (curLB.getValue() * 10) % 10);
                    ((TextView) dialogView.findViewById(R.id.item_tv)).setText(string);
                    builder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            curLB.setValue((double) np.getValue() + ((double) dp.getValue() / 10.0));
                            lowerBoundET.setText(String.valueOf(curLB.getValue()));
                            settingsDB.update(LOWER_BOUND_KEY, curLB.getValue());
                            dialog.dismiss();
                        }
                    });
                } else {
                    View dialogView = View.inflate(SettingsActivity.this, R.layout.dialog_number_picker, null);
                    NumberPicker np = dialogView.findViewById(R.id.np);
                    np.setMinValue(70);
                    np.setMaxValue(130);
                    np.setValue((int) (curLB.getValue() * 10) / 10);
                    ((TextView) dialogView.findViewById(R.id.item_tv)).setText(string);
                    builder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            curLB.setValue((double) np.getValue());
                            lowerBoundET.setText(String.valueOf(curLB.getValue()));
                            settingsDB.update(LOWER_BOUND_KEY, curLB.getValue());
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        upperBoundET.setFocusable(false);
        upperBoundET.setClickable(true);
        upperBoundET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                String string = getResources().getString(R.string.hyper_threshold) + " (" + glucoseUnitTV.getText() + "): " + getResources().getString(R.string.hyper_threshold_desc);
                if (glucoseUnitTV.getText().toString().equals(getResources().getString(R.string.mmol_l))) {
                    View dialogView = View.inflate(SettingsActivity.this, R.layout.dialog_decimal_picker,null);
                    NumberPicker np = dialogView.findViewById(R.id.np);
                    np.setMinValue(6);
                    np.setMaxValue(10);
                    np.setValue((int) (curUB.getValue() * 10) / 10);
                    NumberPicker dp = dialogView.findViewById(R.id.dp);
                    dp.setMinValue(0);
                    dp.setMaxValue(9);
                    dp.setValue((int) (curUB.getValue() * 10) % 10);
                    ((TextView) dialogView.findViewById(R.id.item_tv)).setText(string);
                    builder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            curUB.setValue((double) np.getValue() + ((double) dp.getValue() / 10.0));
                            upperBoundET.setText(String.valueOf(curUB.getValue()));
                            settingsDB.update(UPPER_BOUND_KEY, curUB.getValue());
                            dialog.dismiss();
                        }
                    });
                } else {
                    View dialogView = View.inflate(SettingsActivity.this, R.layout.dialog_number_picker, null);
                    NumberPicker np = dialogView.findViewById(R.id.np);
                    np.setMinValue(110);
                    np.setMaxValue(190);
                    np.setValue((int) (curUB.getValue() * 10) / 10);
                    ((TextView) dialogView.findViewById(R.id.item_tv)).setText(string);
                    builder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { } });
                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            curUB.setValue((double) np.getValue());
                            upperBoundET.setText(String.valueOf(curUB.getValue()));
                            settingsDB.update(UPPER_BOUND_KEY, curUB.getValue());
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        diagnosisET.setFocusable(false);
        diagnosisET.setClickable(true);
        diagnosisET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View dialogView = View.inflate(SettingsActivity.this, R.layout.dialog_number_picker, null);
                NumberPicker np = dialogView.findViewById(R.id.np);
                np.setMinValue(1900);
                np.setMaxValue(Calendar.getInstance().get(Calendar.YEAR));
                np.setValue(curDiagnosis.getValue());
                ((TextView) dialogView.findViewById(R.id.item_tv)).setText(getResources().getString(R.string.year_of_diagnosis));
                builder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        curDiagnosis.setValue(np.getValue());
                        diagnosisET.setText(String.valueOf(curDiagnosis.getValue()));
                        settingsDB.update(DIAGNOSIS_KEY, curDiagnosis.getValue());
                        dialog.dismiss();
                    }
                });
            }
        });

        dobET.setFocusable(false);
        dobET.setClickable(true);
        // datetime pickers
        final Calendar calendarDate = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarDate.set(Calendar.YEAR,year);
                calendarDate.set(Calendar.MONTH,month);
                calendarDate.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                dobET.setText(dateFormatter.format(calendarDate.getTime()));
                settingsDB.update(DOB_KEY, dateFormatter.format(calendarDate.getTime()));
            }
        };
        dobET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SettingsActivity.this,dateSetListener,calendarDate.get(Calendar.YEAR),
                        calendarDate.get(Calendar.MONTH),calendarDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        weightET.setFocusable(false);
        weightET.setClickable(true);
        weightET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View dialogView = View.inflate(SettingsActivity.this, R.layout.dialog_decimal_picker,null);
                NumberPicker np = dialogView.findViewById(R.id.np);
                np.setMinValue(0);
                np.setMaxValue(500);
                np.setValue((int) (curWeight.getValue() * 10) / 10);
                NumberPicker dp = dialogView.findViewById(R.id.dp);
                dp.setMinValue(0);
                dp.setMaxValue(9);
                dp.setValue((int) (curWeight.getValue() * 10) % 10);
                ((TextView) dialogView.findViewById(R.id.item_tv)).setText(getResources().getString(R.string.weight_et));
                builder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        curWeight.setValue((double) np.getValue() + ((double) dp.getValue() / 10.0));
                        weightET.setText(String.valueOf(curWeight.getValue()));
                        settingsDB.update(WEIGHT_KEY, curWeight.getValue());
                        dialog.dismiss();
                    }
                });
            }
        });

        heightET.setFocusable(false);
        heightET.setClickable(true);
        heightET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View dialogView = View.inflate(SettingsActivity.this, R.layout.dialog_number_picker,null);
                NumberPicker np = dialogView.findViewById(R.id.np);
                np.setMinValue(0);
                np.setMaxValue(250);
                np.setValue((int)(curHeight.getValue()));
                ((TextView) dialogView.findViewById(R.id.item_tv)).setText(getResources().getString(R.string.height_et));
                builder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        curHeight.setValue(np.getValue());
                        heightET.setText(String.valueOf(curHeight.getValue()));
                        settingsDB.update(HEIGHT_KEY, curHeight.getValue());
                        dialog.dismiss();
                    }
                });
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