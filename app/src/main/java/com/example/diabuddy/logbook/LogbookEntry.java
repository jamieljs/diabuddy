package com.example.diabuddy.logbook;

import java.util.Date;

public class LogbookEntry implements Comparable<LogbookEntry> {

    private int key; // for database
    private Date datetime = new Date();
    private double reading = 0.0; // 0 when nothing was included, -1 if it is not an actual logbook entry (deleted, or placeholder for headers)
    private double bolus = 0.0;
    private double correction = 0.0;
    private double basal = 0.0;
    private double carbs = 0.0;
    private String food = "";
    private int exercise = 0; // minutes
    private int intensity = 0; // 1 to 5 inclusive
    private String notes = ""; // e.g. hba1c

    public LogbookEntry(int key) {
        this.key = key;
    }

    public LogbookEntry(Date datetime, double reading) {
        this.key = -1;
        this.datetime = datetime;
        this.reading = reading;
    }

    public LogbookEntry(int key, Date datetime, double reading, double bolus, double correction, double basal, double carbs, String food, int exercise, int intensity, String notes) {
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

    public int getKey() {
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
        return bolus + basal + correction;
    }
}
