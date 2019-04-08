package com.example.fyp.utils;

public class User {

    private String id;
    private String name;
    private String email;
    private String phnum;
    private String department;
    private String lineManager;

    public User(){

    }

    public User(String id, String name, String email, String phnum, String department, String lineManager) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phnum = phnum;
        this.department = department;
        this.lineManager = lineManager;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhnum() {
        return phnum;
    }

    public void setPhnum(String phnum) {
        this.phnum = phnum;
    }

    public String getLineManager() {
        return lineManager;
    }

    public void setLineManager(String lineManager) {
        this.lineManager = lineManager;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return name;
    }


}
