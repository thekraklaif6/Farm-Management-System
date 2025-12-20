package com.example.demo;

import java.time.LocalDate;
import java.util.Objects;

public class Employee {

    private int id;
    private String nationalId;
    private String fName;
    private String mName;
    private String lName;
    private LocalDate hireDate;
    private String jobTitle;
    private String gender;
    private int workingHours;
    private LocalDate dateOfBirth;
    private String employmentStatus;
    private double salary;

    // ✅ constructor صحيح
    public Employee(int id,
                    String fName,
                    String mName,
                    String lName,
                    LocalDate hireDate,
                    String jobTitle,
                    String gender,
                    int workingHours,
                    LocalDate dateOfBirth,
                    String employmentStatus,
                    double salary,
                    String nationalId) {

        this.id = id;
        this.fName = fName;
        this.mName = mName;
        this.lName = lName;
        this.hireDate = hireDate;
        this.jobTitle = jobTitle;
        this.gender = gender;
        this.workingHours = workingHours;
        this.dateOfBirth = dateOfBirth;
        this.employmentStatus = employmentStatus;
        this.salary = salary;
        this.nationalId = nationalId;
    }

    public Employee(int employeeID, String fName) {
    }

    // ===== Getters (JavaFX يعتمد عليهم) =====
    public int getId() { return id; }
    public String getNationalId() { return nationalId; }
    public String getFName() { return fName; }
    public String getMName() { return mName; }
    public String getLName() { return lName; }
    public LocalDate getHireDate() { return hireDate; }
    public String getJobTitle() { return jobTitle; }
    public String getGender() { return gender; }
    public int getWorkingHours() { return workingHours; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getEmploymentStatus() { return employmentStatus; }
    public double getSalary() { return salary; }

    // ===== full name =====
    public String getFullName() {
        return (fName + " " +
                (mName != null ? mName + " " : "") +
                lName).trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee e = (Employee) o;
        return id == e.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
