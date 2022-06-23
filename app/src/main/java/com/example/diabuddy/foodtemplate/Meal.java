package com.example.diabuddy.foodtemplate;

import java.util.ArrayList;

public class Meal implements Comparable<Meal> {
    private int id;
    private String name;
    private ArrayList<FoodItem> list;

    public Meal() { }

    public Meal(int id, String name, ArrayList<FoodItem> list) {
        this.id = id;
        this.name = name;
        this.list = list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<FoodItem> getList() {
        return list;
    }

    public void setList(ArrayList<FoodItem> list) {
        this.list = list;
    }

    public double getCarbs() {
        double ans = 0;
        for (FoodItem f : list) ans += f.getCarbs();
        return ans;
    }

    @Override
    public int compareTo(Meal o) {
        return this.id - o.id;
    }

    @Override
    public String toString() {
        return name + " " + list;
    }
}
