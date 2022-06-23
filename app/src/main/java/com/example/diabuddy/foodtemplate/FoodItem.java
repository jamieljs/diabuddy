package com.example.diabuddy.foodtemplate;

public class FoodItem implements Comparable<FoodItem> {
    private int id;
    private String name;
    private double carbs; // in grams

    public FoodItem() { }

    public FoodItem(int id, String name, double carbohydrates) {
        this.id = id;
        this.name = name;
        this.carbs = carbohydrates;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    @Override
    public int compareTo(FoodItem o) {
        return this.id - o.id;
    }

    @Override
    public String toString() {
        return name;
    }
}
