package com.hospital.bedalloc.model;

public class Ward {
    private int wardId;
    private String wardType;
    private int floor;
    private int capacity;
    private double dailyCharge;

    public Ward() {}

    public Ward(int wardId, String wardType, int floor, int capacity, double dailyCharge) {
        this.wardId = wardId;
        this.wardType = wardType;
        this.floor = floor;
        this.capacity = capacity;
        this.dailyCharge = dailyCharge;
    }

    public int getWardId() { return wardId; }
    public void setWardId(int wardId) { this.wardId = wardId; }

    public String getWardType() { return wardType; }
    public void setWardType(String wardType) { this.wardType = wardType; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getDailyCharge() { return dailyCharge; }
    public void setDailyCharge(double dailyCharge) { this.dailyCharge = dailyCharge; }
}
