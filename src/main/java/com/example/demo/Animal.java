package com.example.demo;

import java.time.LocalDate;
import java.time.Period;

public class Animal {
    private int animalID;
    private double weight;
    private double purchasePrice;
    private String gender;
    private LocalDate dateOfBirth;
    private int barnID;
    private String typeOfAnimal;
    private String healthStatus;
    private String barnName;  // من join

    public Animal(int animalID, double weight, double purchasePrice, String gender, LocalDate dateOfBirth, int barnID, String typeOfAnimal, String healthStatus, String barnName) {
        this.animalID = animalID;
        this.weight = weight;
        this.purchasePrice = purchasePrice;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.barnID = barnID;
        this.typeOfAnimal = typeOfAnimal;
        this.healthStatus = healthStatus;
        this.barnName = barnName;
    }

    // Getters (مع getAge جديد لحساب العمر)
    public int getAnimalID() { return animalID; }
    public double getWeight() { return weight; }
    public double getPurchasePrice() { return purchasePrice; }
    public String getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public int getBarnID() { return barnID; }
    public String getTypeOfAnimal() { return typeOfAnimal; }
    public String getHealthStatus() { return healthStatus; }
    public String getBarnName() { return barnName; }

    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();  // حساب العمر بالسنين
    }

    // Setters (للـ Update)
    public void setWeight(double weight) { this.weight = weight; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setBarnID(int barnID) { this.barnID = barnID; }
    public void setTypeOfAnimal(String typeOfAnimal) { this.typeOfAnimal = typeOfAnimal; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public void setBarnName(String barnName) { this.barnName = barnName; }

    // toString للـ ComboBox
    @Override
    public String toString() {
        String type = (typeOfAnimal != null) ? typeOfAnimal.substring(0, 1).toUpperCase() + typeOfAnimal.substring(1) : "Unknown";
        return type + " (ID: " + animalID + ")";
    }
}