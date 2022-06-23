package com.example.diabuddy.trends;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diabuddy2021.R;
import com.example.diabuddy2021.logbook.LogbookEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class TrendsFragment extends Fragment {

    private View root;
    private ConstraintLayout printArea;
    private TextView averageTV, rangeTV, insulinDayTV, insulinKiloTV, carbsTV, exerciseTV, hba1cTV;
    private CardView averageCV, hba1cCV;
    private ProgressBar inRangePB, hypoPB, hyperPB;
    private TextView inRangeTV, hypoTV, hyperTV;
    private ImageView scatterIV;
    private LineChart chart;

    private int red, yellow, tealEL, gray;
    private TypedValue typedValue = new TypedValue();

    private TrendsViewModel vm;
    private ArrayAdapter<String> dateAdapter;
    private ArrayList<String> spinnerList = new ArrayList<>(Arrays.asList("past day", "past 7 days", "past 14 days", "past 30 days", "past 90 days", "custom date range"));
    private int[] days = {0, 6, 13, 29, 89};
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat hourFormatter = new SimpleDateFormat("HH");
    private SimpleDateFormat minuteFormatter = new SimpleDateFormat("mm");
    private Date startDate = new Date(), endDate = new Date();

    private final String[] chartLabels = new String[] { "Median", "Min", "Max" };

    public static TrendsFragment newInstance() {
        return new TrendsFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        vm = new ViewModelProvider(this).get(TrendsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_trends, container, false);
        red = getResources().getColor(R.color.red, getContext().getTheme());
        yellow = getResources().getColor(R.color.yellow, getContext().getTheme());
        tealEL = getResources().getColor(R.color.teal_extra_light, getContext().getTheme());
        gray = getResources().getColor(R.color.light_gray, getContext().getTheme());

        requireContext().getTheme().resolveAttribute(R.attr.colorOnBackground, typedValue, true);
        View divider = root.findViewById(R.id.divider), divider2 = root.findViewById(R.id.divider_2);
        divider.setBackgroundColor(typedValue.data);
        divider2.setBackgroundColor(typedValue.data);

        averageTV = root.findViewById(R.id.average_tv);
        rangeTV = root.findViewById(R.id.range_tv);
        insulinDayTV = root.findViewById(R.id.insulin_day_tv);
        insulinKiloTV = root.findViewById(R.id.insulin_kilo_tv);
        carbsTV = root.findViewById(R.id.carbs_tv);
        exerciseTV = root.findViewById(R.id.exercise_tv);
        hba1cTV = root.findViewById(R.id.hba1c_tv);
        averageCV = root.findViewById(R.id.average_card);
        hba1cCV = root.findViewById(R.id.hba1c_card);

        hba1cTV.setText("-");
        hba1cCV.setCardBackgroundColor(gray);

        inRangePB = root.findViewById(R.id.in_range_pb);
        hypoPB = root.findViewById(R.id.hypo_pb);
        hyperPB = root.findViewById(R.id.hyper_pb);
        inRangeTV = root.findViewById(R.id.num_in_range);
        hypoTV = root.findViewById(R.id.num_hypo);
        hyperTV = root.findViewById(R.id.num_hyper);

        chart = root.findViewById(R.id.line_chart);

        TextInputLayout startTIL = root.findViewById(R.id.start_date);
        TextInputLayout endTIL = root.findViewById(R.id.end_date);
        EditText startET = startTIL.getEditText();
        EditText endET = endTIL.getEditText();
        startET.setClickable(true);
        startET.setFocusable(false);
        endET.setClickable(true);
        endET.setFocusable(false);
        final Calendar calendarStart = Calendar.getInstance();
        final Calendar calendarEnd = Calendar.getInstance();

        Spinner spinner = root.findViewById(R.id.date_range_spinner);
        dateAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerList);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dateAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == spinnerList.size() - 1) {
                    startTIL.setVisibility(View.VISIBLE);
                    endTIL.setVisibility(View.VISIBLE);
                    startDate = new Date();
                    endDate = startDate;
                    String curDate = dateFormatter.format(startDate);
                    startET.setText(curDate);
                    endET.setText(curDate);
                    calendarStart.setTime(startDate);
                    calendarEnd.setTime(endDate);
                } else {
                    startTIL.setVisibility(View.GONE);
                    endTIL.setVisibility(View.GONE);
                    endDate = new Date();
                    startDate = Date.from(endDate.toInstant().minusSeconds(days[position] * 24 * 60 * 60));
                    calendarStart.setTime(startDate);
                    calendarEnd.setTime(endDate);
                }
                updateViews();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        spinner.setSelection(0);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (!startET.getText().toString().isEmpty() && !endET.getText().toString().isEmpty()) {
                    try {
                        startDate = dateFormatter.parse(startET.getText().toString());
                        endDate = dateFormatter.parse(endET.getText().toString());
                        if (endDate.before(startDate)) {
                            endDate = startDate;
                            endET.setText(dateFormatter.format(endDate));
                            Toast.makeText(getContext(), "End date must be on or after the start date!", Toast.LENGTH_LONG).show();
                        }
                        calendarStart.setTime(startDate);
                        calendarEnd.setTime(endDate);
                        updateViews();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        startET.addTextChangedListener(watcher);
        endET.addTextChangedListener(watcher);

        DatePickerDialog.OnDateSetListener startSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarStart.set(Calendar.YEAR,year);
                calendarStart.set(Calendar.MONTH,month);
                calendarStart.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                startET.setText(dateFormatter.format(calendarStart.getTime()));
            }
        };
        startET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(requireContext(), startSetListener,calendarStart.get(Calendar.YEAR),
                        calendarStart.get(Calendar.MONTH),calendarStart.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        DatePickerDialog.OnDateSetListener endSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarEnd.set(Calendar.YEAR,year);
                calendarEnd.set(Calendar.MONTH,month);
                calendarEnd.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                endET.setText(dateFormatter.format(calendarEnd.getTime()));
            }
        };
        endET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(requireContext(), endSetListener,calendarEnd.get(Calendar.YEAR),
                        calendarEnd.get(Calendar.MONTH),calendarEnd.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        return root;
    }

    private void updateViews() {
        ArrayList<LogbookEntry> logbookEntries = vm.getEntries(startDate, endDate);
        try {
            String dateOnly = dateFormatter.format(startDate);
            startDate = formatter.parse(dateOnly + " 00:00");
            dateOnly = dateFormatter.format(endDate);
            endDate = formatter.parse(dateOnly + " 00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int days = (int) (ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant())) + 1;

        double totalReading = 0.0, minReading = 1000000, maxReading = 0;
        int hypo = 0, hyper = 0, inRange = 0;
        double insulin = 0.0, weightInKg = vm.getWeightInKg().getValue(), carbs = 0.0, exercise = 0.0;
        for (int i = 0; i < logbookEntries.size(); i++) {
            double reading = logbookEntries.get(i).getReading();
            if (reading > 0.0) {
                totalReading += reading;
                minReading = Math.min(minReading, reading);
                maxReading = Math.max(maxReading, reading);
                if (reading < vm.getLowerBound().getValue()) hypo++;
                else if (reading <= vm.getUpperBound().getValue()) inRange++;
                else hyper++;
            }
            insulin += logbookEntries.get(i).getInsulin();
            carbs += logbookEntries.get(i).getCarbs();
            exercise += logbookEntries.get(i).getExercise();
        }
        int numReadings = hypo + hyper + inRange;
        if (numReadings > 0) {
            averageTV.setText(String.format("%.2f", (totalReading / numReadings)));
            if (totalReading / numReadings < vm.getLowerBound().getValue()) averageCV.setCardBackgroundColor(red);
            else if (totalReading / numReadings <= vm.getUpperBound().getValue()) averageCV.setCardBackgroundColor(tealEL);
            else averageCV.setCardBackgroundColor(yellow);

            rangeTV.setText(minReading + " - " + maxReading);

            double est = totalReading / numReadings;
            if (vm.getUnit().getValue().equals("mmol/L")) est = (2.59 + est) / 1.59;
            else est = (46.7 + est) / 28.7;
            hba1cTV.setText(String.format("%.1f%%", est));
            if (est > 8) hba1cCV.setCardBackgroundColor(yellow);
            else if (est < 4) hba1cCV.setCardBackgroundColor(red);
            else hba1cCV.setCardBackgroundColor(tealEL);
        } else {
            averageTV.setText("-");
            averageCV.setCardBackgroundColor(gray);
            rangeTV.setText("-");
            hba1cTV.setText("-");
            hba1cCV.setCardBackgroundColor(gray);
        }
        insulinDayTV.setText(String.format("%.2f", (insulin / days)));
        if (weightInKg == 0.0) insulinKiloTV.setText("-");
        else insulinKiloTV.setText(String.format("%.2f", (insulin / weightInKg / days)));
        carbsTV.setText(String.format("%.2f", (carbs / days)));
        exerciseTV.setText(String.format("%.1f", (exercise / days)));

        int total = hypo + hyper + inRange;
        if (total == 0) {
            hyperPB.setProgress(0);
            hypoPB.setProgress(0);
            inRangePB.setProgress(0);
            hyperTV.setText("0 (0%)");
            hypoTV.setText("0 (0%)");
            inRangeTV.setText("0 (0%)");
        } else {
            int hyperPercent = (int)((double) hyper / (double) total * 100);
            hyperPB.setProgress(hyperPercent);
            int hypoPercent = (int)((double) (hypo + hyper) / (double) total * 100);
            hypoPB.setProgress(hypoPercent);
            inRangePB.setProgress(100);
            hyperTV.setText(hyper + " (" + hyperPercent + "%)");
            hypoTV.setText(hypo + " (" + (int)((double) hypo / (double) total * 100) + "%)");
            inRangeTV.setText(inRange + " (" + (int)((double) inRange / (double) total * 100) + "%)");
        }

        int[] chartColors = new int[]{getResources().getColor(R.color.teal_light, requireContext().getTheme()),
                getResources().getColor(R.color.red_dark, requireContext().getTheme()),
                getResources().getColor(R.color.yellow_dark, requireContext().getTheme())};

        chart.setDrawGridBackground(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawBorders(false);
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setDrawAxisLine(true);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(true);
        chart.getAxisRight().setDrawAxisLine(true);
        chart.getAxisRight().setDrawGridLines(true);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setDrawGridLines(true);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(48);
        chart.getXAxis().setLabelCount(9, true);
        chart.getXAxis().setValueFormatter(new MyValueFormatter());

        chart.setNoDataTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));
        chart.setBackgroundColor(getResources().getColor(R.color.white, requireContext().getTheme()));

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setCustom(new LegendEntry[]{new LegendEntry("Median", Legend.LegendForm.DEFAULT, l.getFormSize(), l.getFormLineWidth(), l.getFormLineDashEffect(), chartColors[0]),
                new LegendEntry("Min", Legend.LegendForm.DEFAULT, l.getFormSize(), l.getFormLineWidth(), l.getFormLineDashEffect(), chartColors[1]),
                new LegendEntry("Max", Legend.LegendForm.DEFAULT, l.getFormSize(), l.getFormLineWidth(), l.getFormLineDashEffect(), chartColors[2])});

        double[][] data = new double[3][48];
        ArrayList<Double>[] median = new ArrayList[48];
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        for (int i = 0; i < 48; i++) {
            median[i] = new ArrayList<>();
            data[1][i] = 1000000; // min
            data[2][i] = 0; // max
        }
        for (int i = 0; i < logbookEntries.size(); i++) {
            double reading = logbookEntries.get(i).getReading();
            Date date = logbookEntries.get(i).getDatetime();
            int t = (Integer.parseInt(hourFormatter.format(date)) * 2) + (Integer.parseInt(minuteFormatter.format(date)) / 30); // integer from 0 to 47 inclusive
            if (reading > 0.0) {
                median[t].add(reading);
                data[1][t] = Math.min(data[1][t], reading);
                data[2][t] = Math.max(data[2][t], reading);
            }
        }
        for (int i = 0; i < 48; i++) {
            Collections.sort(median[i]);
            if (median[i].isEmpty()) data[0][i] = 0;
            else data[0][i] = (median[i].get((median[i].size()-1)/2) + median[i].get((median[i].size())/2)) /2;
        }

        for (int x = 1; x < 4; x++) {
            int i = x % 3;
            ArrayList<Entry> values = new ArrayList<Entry>();
            int counter = 0;
            for (int j = 0; j < 48; j++) {
                if (data[i][j] > 0 && data[i][j] < 1000000) {
                    values.add(new Entry(j, (float)data[i][j])); // integer from 0 to 47 inclusive. 0 is 12.15am, 1 is 12.45am etc.
                    counter++;
                }
            }
            if (counter == 0) continue;
            LineDataSet d = new LineDataSet(values, chartLabels[i]);
            d.setLineWidth(1.5f);
            d.setCircleRadius(3f);
            d.setColor(chartColors[i]);
            d.setCircleColor(chartColors[i]);
            d.setHighLightColor(getResources().getColor(R.color.black, requireContext().getTheme()));
            dataSets.add(d);
        }

        if (dataSets.isEmpty()) {
            chart.setData(null);
            chart.invalidate();
            return;
        }

        float lb = vm.getLowerBound().getValue().floatValue(), ub = vm.getUpperBound().getValue().floatValue();
        ArrayList<Entry> values = new ArrayList<Entry>();
        values.add(new Entry(0,ub));
        values.add(new Entry(48,ub));
        LineDataSet shade = new LineDataSet(values,"Target Range");
        shade.setFillColor(getResources().getColor(R.color.translucent_teal, requireContext().getTheme()));
        shade.setColor(getResources().getColor(R.color.transparent, requireContext().getTheme()));
        shade.setDrawFilled(true);
        shade.setDrawValues(false);
        shade.setDrawCircles(false);
        shade.setHighlightEnabled(false);
        dataSets.add(shade);

        ArrayList<Entry> values2 = new ArrayList<>();
        values2.add(new Entry(0, lb));
        values2.add(new Entry(48, lb));
        LineDataSet cover = new LineDataSet(values2, "Below Range");
        cover.setFillColor(getResources().getColor(R.color.red, requireContext().getTheme()));
        cover.setColor(getResources().getColor(R.color.transparent, requireContext().getTheme()));
        cover.setDrawFilled(true);
        cover.setDrawValues(false);
        cover.setDrawCircles(false);
        cover.setHighlightEnabled(false);
        dataSets.add(cover);

        LimitLine line1 = new LimitLine(ub);
        line1.setLineColor(tealEL);
        line1.setLineWidth(2);
        LimitLine line2 = new LimitLine(lb);
        line2.setLineColor(tealEL);
        line2.setLineWidth(2);
        chart.getAxisLeft().addLimitLine(line1);
        chart.getAxisLeft().addLimitLine(line2);

        chart.setData(new LineData(dataSets));
        chart.invalidate();
    }

    private static class MyValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            double hour = Math.floor(value / 2);
            double minute = Math.floor((value / 2 - hour) * 60);
            return String.format("%02d:%02d", (int) hour, (int) minute);
        }
    }
}