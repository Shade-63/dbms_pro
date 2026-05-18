package com.hospital.bedalloc.model;

public class Bed {
    private int bedId;
    private int wardId;
    private String bedNumber;
    private String equipmentStatus;
    private String currentStatus;
    
    // Extra field for display
    private String wardType;

    public Bed() {}

    public Bed(int bedId, int wardId, String bedNumber, String equipmentStatus, String currentStatus) {
        this.bedId = bedId;
        this.wardId = wardId;
        this.bedNumber = bedNumber;
        this.equipmentStatus = equipmentStatus;
        this.currentStatus = currentStatus;
    }

    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }

    public int getWardId() { return wardId; }
    public void setWardId(int wardId) { this.wardId = wardId; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public String getEquipmentStatus() { return equipmentStatus; }
    public void setEquipmentStatus(String equipmentStatus) { this.equipmentStatus = equipmentStatus; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getWardType() { return wardType; }
    public void setWardType(String wardType) { this.wardType = wardType; }
}
