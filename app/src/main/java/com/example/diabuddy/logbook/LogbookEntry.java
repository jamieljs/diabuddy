package com.example.diabuddy.logbook;

import java.util.Date;

public class LogbookEntry implements Comparable<LogbookEntry> {

    private String key; // for database
    private Date datetime = new Date();
    private double reading = -1;
    private double bolus = -1;
    private double correction = -1;
    private double basal = -1;
    private double carbs = -1;
    private String food = "";
    private int exercise = -1; // minutes
    private int intensity = -1; // 1 to 5 inclusive
    private double hba1c = -1;
    private double ketones = -1;
    private int systolic = -1;
    private int diastolic = -1;
    private String notes = ""; // e.g. hba1c

    public LogbookEntry(String key) {
        this.key = key;
    }

    public LogbookEntry(Date datetime, double reading) {
        this.key = "-";
        this.datetime = datetime;
        this.reading = reading;
    }

    public LogbookEntry(String key, Date datetime, double reading, double bolus, double correction, double basal, double carbs, String food, int exercise, int intensity, double hba1c, double ketones, int systolic, int diastolic, String notes) {
        this.key = key;
        this.datetime = datetime;
        this.reading = reading;
        this.bolus = bolus;
        this.correction = correction;
        this.basal = basal;
        this.carbs = carbs;
        this.food = food;
        this.exercise = exercise;
        this.intensity = intensity;
        this.hba1c = hba1c;
        this.ketones = ketones;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.notes = notes;
    }

    public LogbookEntry(LogbookEntry logbookEntry) {
        this.key = logbookEntry.key;
        this.datetime = logbookEntry.datetime;
        this.reading = logbookEntry.reading;
        this.bolus = logbookEntry.bolus;
        this.correction = logbookEntry.correction;
        this.basal = logbookEntry.basal;
        this.carbs = logbookEntry.carbs;
        this.food = logbookEntry.food;
        this.exercise = logbookEntry.exercise;
        this.intensity = logbookEntry.intensity;
        this.notes = logbookEntry.notes;
    }

    @Override
    public int compareTo(LogbookEntry o) {
        return -this.datetime.compareTo(o.datetime);
    }

    public String getKey() {
        return key;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public double getReading() {
        return reading;
    }

    public void setReading(double reading) {
        this.reading = reading;
    }

    public double getBolus() {
        return bolus;
    }

    public void setBolus(double bolus) {
        this.bolus = bolus;
    }

    public double getCorrection() {
        return correction;
    }

    public void setCorrection(double correction) {
        this.correction = correction;
    }

    public double getBasal() {
        return basal;
    }

    public void setBasal(double basal) {
        this.basal = basal;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public String getFood() { return food; }

    public void setFood(String food) { this.food = food; }

    public int getExercise() {
        return exercise;
    }

    public void setExercise(int exercise) {
        this.exercise = exercise;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getInsulin() {
        int res = 0;
        if (bolus > 0) res += bolus;
        if (basal > 0) res += basal;
        if (correction > 0) res += correction;
        return res;
    }

    public double getHba1c() {
        return hba1c;
    }

    public void setHba1c(double hba1c) {
        this.hba1c = hba1c;
    }

    public double getKetones() {
        return ketones;
    }

    public void setKetones(double ketones) {
        this.ketones = ketones;
    }

    public int getSystolic() {
        return systolic;
    }

    public void setSystolic(int systolic) {
        this.systolic = systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(int diastolic) {
        this.diastolic = diastolic;
    }
}
