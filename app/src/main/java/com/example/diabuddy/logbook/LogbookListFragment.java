package com.example.diabuddy.logbook;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.diabuddy.LoginActivity;
import com.example.diabuddy.R;
import com.example.diabuddy.UserActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.readPendingIntentOrNullFromParcel;

public class LogbookListFragment extends Fragment {

    static LogbookViewModel vm;

    public LogbookListFragment() {
        // Required empty public constructor
    }

    public static LogbookListFragment newInstance() {
        LogbookListFragment fragment = new LogbookListFragment();
        return fragment;
    }

    private ConstraintLayout empty;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<LogbookEntry> allLogbookEntries = new ArrayList<>();
    private ArrayList<LogbookEntry> logbookEntries = new ArrayList<>();
    private SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeOnlyFormat = new SimpleDateFormat("HH:mm");
    private View root;

    private ChipGroup chipGroup;
    private Chip readingChip, insulinChip, foodChip, exerciseChip, notesChip;
    private String notesQueryText = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_logbook_list, container, false);
        vm = new ViewModelProvider(this).get(LogbookViewModel.class);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        int red = getResources().getColor(R.color.red, getContext().getTheme());
        int yellow = getResources().getColor(R.color.yellow, getContext().getTheme());
        int tealEL = getResources().getColor(R.color.teal_extra_light, getContext().getTheme());
        int gray = getResources().getColor(R.color.light_gray, getContext().getTheme());

        empty = root.findViewById(R.id.logbook_empty);
        empty.setVisibility(View.GONE);

        recyclerView = root.findViewById(R.id.logbook_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        TextView averageTV = root.findViewById(R.id.average_tv);
        TextView rangeTV = root.findViewById(R.id.range_tv);
        CardView averageCV = root.findViewById(R.id.average_card);
        CardView rangeCV = root.findViewById(R.id.range_card);
        TextView bolusTV = root.findViewById(R.id.bolus_tv);
        TextView basalTV = root.findViewById(R.id.basal_tv);
        CardView bolusCV = root.findViewById(R.id.bolus_card);
        CardView basalCV = root.findViewById(R.id.basal_card);

        chipGroup = root.findViewById(R.id.chip_group);
        readingChip = root.findViewById(R.id.reading_chip);
        insulinChip = root.findViewById(R.id.insulin_chip);
        foodChip = root.findViewById(R.id.food_chip);
        exerciseChip = root.findViewById(R.id.exercise_chip);
        notesChip = root.findViewById(R.id.notes_chip);
        notesQueryText = "";

        notesChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    View dialogView = View.inflate(requireContext(), R.layout.dialog_logbook_search,null);

                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            notesChip.setChecked(false);
                            filterEntries();
                        }
                    });
                    builder.setPositiveButton(getString(R.string.search), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    EditText queryET = dialogView.findViewById(R.id.logbook_search_edit_text);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            notesQueryText = queryET.getText().toString().toLowerCase();
                            notesChip.setText(getResources().getString(R.string.notes) + " " + notesQueryText);
                            dialog.dismiss();
                            filterEntries();
                        }
                    });
                } else {
                    notesQueryText = "";
                    notesChip.setText(getResources().getString(R.string.search_text_in_notes));
                    filterEntries();
                }
            }
        });

        readingChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterEntries();
            }
        });
        insulinChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterEntries();
            }
        });
        foodChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterEntries();
            }
        });
        exerciseChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterEntries();
            }
        });

        final Observer<ArrayList<LogbookEntry>> observer = new Observer<ArrayList<LogbookEntry>>() {
            @Override
            public void onChanged(ArrayList<LogbookEntry> arr) {
                allLogbookEntries.clear();
                if (arr.isEmpty()) {
                    empty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    return;
                }
                Collections.sort(arr);
                allLogbookEntries.addAll(arr);
                filterEntries();

                String dateString = dateOnlyFormat.format(new Date());
                double total = 0.0, minReading = 1000000, maxReading = 0;
                int numReadings = 0, hypos = 0, hypers = 0;
                double bolus = 0.0, basal = 0.0;
                for (LogbookEntry l : allLogbookEntries) {
                    if (dateString.equals(dateOnlyFormat.format(l.getDatetime()))) { // today
                        double reading = l.getReading();
                        if (reading > 0.0) {
                            total += reading;
                            numReadings++;
                            minReading = Math.min(minReading, reading);
                            maxReading = Math.max(maxReading, reading);
                            if (numReadings <= 10) {
                                if (reading < vm.getLowerBound().getValue()) hypos++;
                                else if (reading > vm.getUpperBound().getValue()) hypers++;
                            }
                        }
                        double shortActing = l.getBolus() + l.getCorrection(), longActing = l.getBasal();
                        if (shortActing > 0.0) bolus += shortActing;
                        if (longActing > 0.0) basal += longActing;
                    } else break;
                }
                if (numReadings > 0) {
                    averageTV.setText(String.format("%.2f", (total / numReadings)));
                    if (total / numReadings < vm.getLowerBound().getValue()) averageCV.setCardBackgroundColor(red);
                    else if (total / numReadings <= vm.getUpperBound().getValue()) averageCV.setCardBackgroundColor(tealEL);
                    else averageCV.setCardBackgroundColor(yellow);
                    rangeTV.setText(minReading + " - " + maxReading);
                    rangeCV.setCardBackgroundColor(tealEL);

                    if (!vm.isWarnedFreq()) {
                        if (numReadings >= 10) { // checking for frequent low/high
                            if (hypos >= 5) {
                                frequent_warning(5);
                            } else if (hypers >= 5) {
                                frequent_warning(6);
                            }
                        }
                    }
                } else {
                    averageTV.setText("-");
                    averageCV.setCardBackgroundColor(gray);
                    rangeTV.setText("-");
                    rangeCV.setCardBackgroundColor(gray);
                }
                if (bolus > 0.0) {
                    bolusTV.setText(String.format("%.1f", bolus));
                    bolusCV.setCardBackgroundColor(tealEL);
                } else {
                    bolusTV.setText("-");
                    bolusCV.setCardBackgroundColor(gray);
                }
                if (basal > 0.0) {
                    basalTV.setText(String.format("%.1f", basal));
                    basalCV.setCardBackgroundColor(tealEL);
                } else {
                    basalTV.setText("-");
                    basalCV.setCardBackgroundColor(gray);
                }
            }
        };
        vm.getLogbookEntries().observe(getViewLifecycleOwner(), observer);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editEntry(logbookEntries.size(), view);
            }
        });
    }

    class RecyclerAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return new EntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_logbook, parent, false));
            } else {
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.divider_date, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder.getItemViewType() == 0) {
                EntryViewHolder holder = (EntryViewHolder) viewHolder;
                LogbookEntry entry = logbookEntries.get(position);
                holder.timeTV.setText(timeOnlyFormat.format(entry.getDatetime()));
                double reading = entry.getReading();
                if (reading == 0.0) {
                    holder.readingTV.setVisibility(View.GONE);
                    holder.readingUnitTV.setVisibility(View.GONE);
                    holder.readingIV.setVisibility(View.GONE);
                    holder.item.setCardBackgroundColor(getResources().getColor(R.color.light_gray, requireContext().getTheme()));
                } else {
                    holder.readingTV.setVisibility(View.VISIBLE);
                    holder.readingUnitTV.setVisibility(View.VISIBLE);
                    holder.readingIV.setVisibility(View.VISIBLE);
                    holder.readingTV.setText(reading + "");
                    if (reading < vm.getLowerBound().getValue()) holder.item.setCardBackgroundColor(getResources().getColor(R.color.red, requireContext().getTheme()));
                    else if (reading > vm.getUpperBound().getValue()) holder.item.setCardBackgroundColor(getResources().getColor(R.color.yellow, requireContext().getTheme()));
                    else holder.item.setCardBackgroundColor(getResources().getColor(R.color.teal_extra_light, requireContext().getTheme()));
                }
                holder.readingUnitTV.setText(vm.getGlucoseUnit());
                holder.insulinIV.setVisibility((entry.getInsulin() == 0.0) ? View.GONE : View.VISIBLE);
                holder.foodIV.setVisibility((entry.getCarbs() == 0.0 && entry.getFood().equals("")) ? View.GONE : View.VISIBLE);
                holder.exerciseIV.setVisibility((entry.getExercise() == 0) ? View.GONE : View.VISIBLE);
                holder.notesIV.setVisibility(entry.getNotes().equals("") ? View.GONE : View.VISIBLE);
            } else {
                HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
                holder.dateTV.setText(dateOnlyFormat.format(logbookEntries.get(position).getDatetime()));
            }
        }

        @Override
        public int getItemCount() {
            return logbookEntries.size();
        }

        @Override
        public int getItemViewType(int position) {
            return (logbookEntries.get(position).getReading() == -1.0) ? 1 : 0;
        }

        class EntryViewHolder extends RecyclerView.ViewHolder{
            int pos;
            CardView item;
            TextView timeTV;
            TextView readingTV;
            TextView readingUnitTV;
            ImageView readingIV;
            ImageView insulinIV;
            ImageView foodIV;
            ImageView exerciseIV;
            ImageView notesIV;

            public EntryViewHolder(@NonNull View itemView) {
                super(itemView);
                item = (CardView) itemView;
                timeTV = itemView.findViewById(R.id.time_tv);
                readingTV = itemView.findViewById(R.id.reading_tv);
                readingUnitTV = itemView.findViewById(R.id.reading_unit_tv);
                readingIV = itemView.findViewById(R.id.droplet_image);
                insulinIV = itemView.findViewById(R.id.injection_image);
                foodIV = itemView.findViewById(R.id.food_image);
                exerciseIV = itemView.findViewById(R.id.exercise_image);
                notesIV = itemView.findViewById(R.id.notes_image);

                itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        editEntry(getAdapterPosition(), view);
                    }
                });
            }
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder{
            TextView dateTV;

            public HeaderViewHolder(@NonNull View itemView) {
                super(itemView);
                dateTV = itemView.findViewById(R.id.date_text);
            }
        }
    }

    private void filterEntries() {
        boolean reading = readingChip.isChecked();
        boolean insulin = insulinChip.isChecked();
        boolean food = foodChip.isChecked();
        boolean exercise = exerciseChip.isChecked();
        boolean notes = notesChip.isChecked();
        if (!reading && !insulin && !food && !exercise && !notes) {
            logbookEntries.clear();
            logbookEntries.add(new LogbookEntry(allLogbookEntries.get(0).getDatetime(), -1)); // header
            for (int i = 0; i < allLogbookEntries.size(); i++) {
                logbookEntries.add(allLogbookEntries.get(i));
                if (i < allLogbookEntries.size() - 1 && !dateOnlyFormat.format(allLogbookEntries.get(i).getDatetime()).equals(dateOnlyFormat.format(allLogbookEntries.get(i + 1).getDatetime()))) { // different date
                    logbookEntries.add(new LogbookEntry(allLogbookEntries.get(i + 1).getDatetime(), -1));
                }
            }
        } else {
            logbookEntries.clear();
            ArrayList<LogbookEntry> filtered = new ArrayList<>();
            for (LogbookEntry l : allLogbookEntries) {
                boolean possible = true;
                if (reading && l.getReading() <= 0) possible = false;
                else if (insulin && l.getInsulin() <= 0) possible = false;
                else if (food && l.getCarbs() <= 0 && l.getFood().equals("")) possible = false;
                else if (exercise && l.getExercise() <= 0) possible = false;
                else if (notes && !l.getNotes().toLowerCase().contains(notesQueryText)) possible = false;
                if (possible) filtered.add(l);
                System.out.println(possible + " " + l.getNotes().toLowerCase() + " " + notesQueryText);
            }
            if (!filtered.isEmpty()) {
                logbookEntries.add(new LogbookEntry(filtered.get(0).getDatetime(), -1)); // header
                for (int i = 0; i < filtered.size(); i++) {
                    logbookEntries.add(filtered.get(i));
                    if (i < filtered.size() - 1 && !dateOnlyFormat.format(filtered.get(i).getDatetime()).equals(dateOnlyFormat.format(filtered.get(i + 1).getDatetime()))) { // different date
                        logbookEntries.add(new LogbookEntry(filtered.get(i + 1).getDatetime(), -1));
                    }
                }
            }
        }
        if (logbookEntries.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }


    private void editEntry(int index, View view) {
        int key = -1;
        if (index == logbookEntries.size()) {
            key = vm.createNewEntry();
        } else {
            key = logbookEntries.get(index).getKey();
        }
        Intent i = new Intent(getActivity(), EditActivity.class);
        i.putExtra("index", key);
        startActivity(i);
    }

    private void frequent_warning(int code) {
        String[] arr = getResources().getStringArray((code == 5 ? R.array.frequent_low_warnings : R.array.frequent_high_warnings));
        LogbookListFragment.vm.warn(code, arr[0]); // only one element in each array for now

        int notifID = 102;
        String channelID = "com.example.diabuddy2021.warnings";
        Context context = requireActivity().getApplicationContext();
        Intent i = new Intent(context, LoginActivity.class); // redirect to MessagesActivity after login
        i.putExtra("type", "warning");
        PendingIntent pending = PendingIntent.getActivity(context, 1, i, FLAG_UPDATE_CURRENT);
        String str = getResources().getString((code == 5 ? R.string.frequent_hypo_text : R.string.frequent_hyper_text));
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

}