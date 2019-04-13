package com.example.fyp.utils;

public class Claim {
    private String userID;
    private String status;
    private double amount;
    private String managerID;
    private String department;

    public Claim(String userID, String status, double amount, String managerID, String department) {
        this.userID = userID;
        this.status = status;
        this.amount = amount;
        this.managerID = managerID;
        this.department = department;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
