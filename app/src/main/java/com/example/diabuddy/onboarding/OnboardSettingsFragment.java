package com.example.diabuddy.onboarding;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.diabuddy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OnboardSettingsFragment extends Fragment {

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public OnboardSettingsFragment() {
        // Required empty public constructor
    }

    public static OnboardSettingsFragment newInstance() {
        OnboardSettingsFragment fragment = new OnboardSettingsFragment();
        return fragment;
    }

    private final DocumentReference settingsDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final String GLUCOSE_UNIT_KEY = "bgUnit";
    private final String LOWER_BOUND_KEY = "hypo";
    private final String UPPER_BOUND_KEY = "hyper";

    private MutableLiveData<String> unit = new MutableLiveData<>();
    private MutableLiveData<Double> curLB = new MutableLiveData<>(), curUB = new MutableLiveData<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_onboard_settings, container, false);

        RadioButton mmolRB = root.findViewById(R.id.mmol_l_radio), mgRB = root.findViewById(R.id.mg_dl_radio);
        EditText lowerBoundET = root.findViewById(R.id.lower_bound_et), upperBoundET = root.findViewById(R.id.upper_bound_et);
        TextView hypoUnit = root.findViewById(R.id.hypo_unit), hyperUnit = root.findViewById(R.id.hyper_unit);

        mmolRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unit.setValue(getResources().getString(R.string.mmol_l));
            }
        });
        mgRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unit.setValue(getResources().getString(R.string.mg_dl));
            }
        });
        final Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                hypoUnit.setText(unit.getValue());
                hyperUnit.setText(unit.getValue());
            }
        };
        unit.observe(getViewLifecycleOwner(), observer);

        mmolRB.performClick();

        settingsDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    curLB.setValue((Double) task.getResult().get(LOWER_BOUND_KEY));
                    lowerBoundET.setText(curLB.getValue() + "");
                    curUB.setValue((Double) task.getResult().get(UPPER_BOUND_KEY));
                    upperBoundET.setText(curUB.getValue() + "");
                }
            }
        });

        Button cont = root.findViewById(R.id.continue_button);
        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage(getResources().getString(R.string.no_unit_change));
                builder.setPositiveButton(getResources().getString(R.string.continue_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settingsDB.update(GLUCOSE_UNIT_KEY, unit.getValue(), LOWER_BOUND_KEY, curLB.getValue(), UPPER_BOUND_KEY, curUB.getValue());
                        Navigation.findNavController(root).navigate(R.id.action_onboardSettingsFragment_to_walkthroughFragment);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.back), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                builder.create().show();
            }
        });

        lowerBoundET.setFocusable(false);
        lowerBoundET.setClickable(true);
        lowerBoundET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                String string = getResources().getString(R.string.hypo_threshold) + " (" + unit.getValue() + "): " + getResources().getString(R.string.hypo_threshold_desc);
                if (unit.getValue().equals(getResources().getString(R.string.mmol_l))) {
                    View dialogView = View.inflate(requireContext(), R.layout.dialog_decimal_picker,null);
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
                    View dialogView = View.inflate(requireContext(), R.layout.dialog_number_picker, null);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                String string = getResources().getString(R.string.hyper_threshold) + " (" + unit.getValue() + "): " + getResources().getString(R.string.hyper_threshold_desc);
                if (unit.getValue().equals(getResources().getString(R.string.mmol_l))) {
                    View dialogView = View.inflate(requireContext(), R.layout.dialog_decimal_picker,null);
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
                    View dialogView = View.inflate(requireContext(), R.layout.dialog_number_picker, null);
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

        return root;
    }
}