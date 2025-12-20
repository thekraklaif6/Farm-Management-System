package com.example.demo;

public class Production {
    private int productionID;
    private String productionDate;
    private String productionType;
    private double price;
    private int quantity;
    private int animalID;
    private int employeeID;

    public Production(int productionID, String productionDate, String productionType, double price, int quantity, int animalID, int employeeID) {
        this.productionID = productionID;
        this.productionDate = productionDate;
        this.productionType = productionType;
        this.price = price;
        this.quantity = quantity;
        this.animalID = animalID;
        this.employeeID = employeeID;

    }

    // Getters (للـ PropertyValueFactory في Table)
    public int getProductionID() { return productionID; }
    public String getProductionDate() { return productionDate; }
    public String getProductionType() { return productionType; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getAnimalID() { return animalID; }

}