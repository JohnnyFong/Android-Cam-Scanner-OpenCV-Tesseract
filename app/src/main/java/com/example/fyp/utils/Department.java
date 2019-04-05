package com.example.fyp.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Department {
    private String name;
    private ArrayList<String> lineManager;

    public Department(){

    }

    public Department(String name, ArrayList<String> lineManager) {
        this.name = name;
        this.lineManager = lineManager;
    }

    public ArrayList<String> getLineManager() {
        return lineManager;
    }

    public void setLineManager(ArrayList<String> lineManager) {
        this.lineManager = lineManager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
