package com.example.demo;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Barn {

    // Use JavaFX properties for automatic table updates
    private final IntegerProperty barnID;
    private  final StringProperty barnName;
    private final StringProperty typeOfBarn;
    private final IntegerProperty capacity;
    private final IntegerProperty currentAnimalCount;
    private final StringProperty operationalStatus;
    private final StringProperty responsibleEmployeeName;
    private final ObjectProperty<LocalDate> establishmentDate; // For passing to update screen
    private final StringProperty location; // For passing to update screen

    // Constructor for displaying data in the table
    public Barn(int barnID, String barnName, String typeOfBarn, int capacity, int currentAnimalCount, String operationalStatus, String responsibleEmployeeName, LocalDate establishmentDate, String location) {
        this.barnID = new SimpleIntegerProperty(barnID);
        this.barnName = new SimpleStringProperty(barnName);
        this.typeOfBarn = new SimpleStringProperty(typeOfBarn);
        this.capacity = new SimpleIntegerProperty(capacity);
        this.currentAnimalCount = new SimpleIntegerProperty(currentAnimalCount);
        this.operationalStatus = new SimpleStringProperty(operationalStatus);
        this.responsibleEmployeeName = new SimpleStringProperty(responsibleEmployeeName);
        this.establishmentDate = new SimpleObjectProperty<>(establishmentDate);
        this.location = new SimpleStringProperty(location);
    }

    // --- Getters for the values ---
    public int getBarnID() { return barnID.get(); }
    public String getBarnName() { return barnName.get(); }
    public String getTypeOfBarn() { return typeOfBarn.get(); }
    public int getCapacity() { return capacity.get(); }
    public int getCurrentAnimalCount() { return currentAnimalCount.get(); }
    public String getOperationalStatus() { return operationalStatus.get(); }
    public String getResponsibleEmployeeName() { return responsibleEmployeeName.get(); }
    public LocalDate getEstablishmentDate() { return establishmentDate.get(); }
    public String getLocation() { return location.get(); }

    // --- Setters ---
    public void setBarnName(String name) { this.barnName.set(name); }
    // Add other setters if needed

    // --- Property Getters (Essential for JavaFX TableView) ---
    public IntegerProperty barnIDProperty() { return barnID; }
    public StringProperty barnNameProperty() { return barnName; }
    public StringProperty typeOfBarnProperty() { return typeOfBarn; }
    public IntegerProperty capacityProperty() { return capacity; }
    public IntegerProperty currentAnimalCountProperty() { return currentAnimalCount; }
    public StringProperty operationalStatusProperty() { return operationalStatus; }
    public StringProperty responsibleEmployeeNameProperty() { return responsibleEmployeeName; }
}
