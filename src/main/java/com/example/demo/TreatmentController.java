package com.example.demo;

import com.gluonhq.DB;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TreatmentController {

    // ---------------------------------------------------------
    //  UI Components
    // ---------------------------------------------------------
    @FXML private ComboBox<String> addAnimalCombo;
    @FXML private DatePicker examDatePicker;
    @FXML private ComboBox<String> doctorCombo;
    @FXML private ComboBox<String> medicationcombo;
    @FXML private Spinner<Integer> dosageField;
    @FXML private Spinner<Integer> durationField;
    @FXML private TextField typeField;
    @FXML private TextField  formField;
    @FXML private ComboBox<String> animalSelectCombo;

    @FXML private Label c1DateLabel, c1MedLabel, c1DoctorLabel, c1DosageLabel, c1DurationLabel;
    @FXML private Label c2DateLabel, c2MedLabel, c2DoctorLabel, c2DosageLabel, c2DurationLabel;
    @FXML private Label c3DateLabel, c3MedLabel, c3DoctorLabel, c3DosageLabel, c3DurationLabel;
    @FXML private Label c4DateLabel, c4MedLabel, c4DoctorLabel, c4DosageLabel, c4DurationLabel;

    // ---------------------------------------------------------
    //  Medication Prices (STATIC MAP)
    // ---------------------------------------------------------
    private final Map<String, Double> medicationPrices = Map.of(
            "PPR Vaccine", 2.50,
            "Multivitamin", 1.20,
            "Fenbendazole", 0.80,
            "FMD Vaccine", 3.00,
            "Oxytocin", 3.00
    );

    // ---------------------------------------------------------
    //  Initialization
    // ---------------------------------------------------------
    public void initialize() {
        loadAnimals();
        loadDoctors();
        loadMedications();

        dosageField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));
        durationField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 1));
    }

    // ---------------------------------------------------------
    //  Loaders
    // ---------------------------------------------------------
    private void loadMedications() {
        medicationcombo.getItems().addAll(medicationPrices.keySet());
    }

    private void loadAnimals() {
        String sql = "SELECT AnimalID FROM Animal";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("AnimalID"));
                addAnimalCombo.getItems().add(id);
                animalSelectCombo.getItems().add(id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDoctors() {
        String sql = "SELECT EmployeeID, FName, LName FROM Employee WHERE JobTitle = 'Veterinarian'";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("EmployeeID");
                String name = rs.getString("FName") + " " + rs.getString("LName");
                doctorCombo.getItems().add(id + " - " + name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------
    //  Add Treatment (PRICE CALCULATED & STORED)
    // ---------------------------------------------------------
    @FXML
    private void onAddTreatment() {

        if (addAnimalCombo.getValue() == null ||
                doctorCombo.getValue() == null ||
                examDatePicker.getValue() == null ||
                medicationcombo.getValue() == null) {

            showAlert("Please fill ALL required fields.");
            return;
        }

        try (Connection conn = DB.getConnection()) {

            String sql = "INSERT INTO Treatment " +
                    "(animal_id, employee_id, exa_date, doc_name, medication_name, unit_price, dosage, duration, total_cost) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            int animalID = Integer.parseInt(addAnimalCombo.getValue());
            int doctorID = extractDoctorID();
            LocalDate date = examDatePicker.getValue();

            String doctorName = doctorCombo.getValue().split(" - ")[1];
            String medicationName = medicationcombo.getValue();

            double unitPrice = medicationPrices.get(medicationName);
            int dosage = dosageField.getValue();
            int duration = durationField.getValue();
            double totalCost = unitPrice * dosage *duration;

            stmt.setInt(1, animalID);
            stmt.setInt(2, doctorID);
            stmt.setTimestamp(3, Timestamp.valueOf(date.atStartOfDay()));
            stmt.setString(4, doctorName);
            stmt.setString(5, medicationName);
            stmt.setDouble(6, unitPrice);
            stmt.setInt(7, dosage);
            stmt.setInt(8, duration);
            stmt.setDouble(9, totalCost);

            stmt.executeUpdate();

            showAlert("Treatment added successfully!");
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error inserting treatment!");
        }
    }

    // ---------------------------------------------------------
    //  Helpers
    // ---------------------------------------------------------
    private int extractDoctorID() {
        try {
            return Integer.parseInt(doctorCombo.getValue().split(" - ")[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    private void clearForm() {
        addAnimalCombo.setValue(null);
        doctorCombo.setValue(null);
        examDatePicker.setValue(null);
        medicationcombo.setValue(null);
        dosageField.getValueFactory().setValue(1);
        durationField.getValueFactory().setValue(1);
        typeField.clear();
        formField.clear();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ---------------------------------------------------------
    //  Display Cards (UNCHANGED)
    // ---------------------------------------------------------
    class TreatmentRecord {
        Timestamp date;
        String doctor;
        String med;
        int dosage;
        int duration;

        TreatmentRecord(Timestamp d, String doc, String m, int ds, int du) {
            date = d;
            doctor = doc;
            med = m;
            dosage = ds;
            duration = du;
        }
    }

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private List<TreatmentRecord> fetchTreatmentsForAnimal(int animalId) {
        List<TreatmentRecord> list = new ArrayList<>();

        String sql = "SELECT exa_date, doc_name, medication_name, dosage, duration " +
                "FROM Treatment WHERE animal_id = ? ORDER BY treatment_id DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, animalId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new TreatmentRecord(
                        rs.getTimestamp("exa_date"),
                        rs.getString("doc_name"),
                        rs.getString("medication_name"),
                        rs.getInt("dosage"),
                        rs.getInt("duration")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void clearAllCards() {
        c1DateLabel.setText(""); c1MedLabel.setText(""); c1DoctorLabel.setText(""); c1DosageLabel.setText(""); c1DurationLabel.setText("");
        c2DateLabel.setText(""); c2MedLabel.setText(""); c2DoctorLabel.setText(""); c2DosageLabel.setText(""); c2DurationLabel.setText("");
        c3DateLabel.setText(""); c3MedLabel.setText(""); c3DoctorLabel.setText(""); c3DosageLabel.setText(""); c3DurationLabel.setText("");
        c4DateLabel.setText(""); c4MedLabel.setText(""); c4DoctorLabel.setText(""); c4DosageLabel.setText(""); c4DurationLabel.setText("");
    }

    private void fillCards(List<TreatmentRecord> treatments) {
        clearAllCards();
        if (treatments.size() > 0) fillCard(c1DateLabel, treatments.get(0));
        if (treatments.size() > 1) fillCard(c2DateLabel, treatments.get(1));
        if (treatments.size() > 2) fillCard(c3DateLabel, treatments.get(2));
        if (treatments.size() > 3) fillCard(c4DateLabel, treatments.get(3));
    }

    private void fillCard(Label label, TreatmentRecord t) {
        label.setText(
                "Examination Date : " + t.date.toLocalDateTime().toLocalDate().format(dateFormatter) + "\n" +
                        "Medication Name : " + t.med + "\n" +
                        "Doctor : " + t.doctor + "\n" +
                        "Dosage : " + t.dosage + "\n" +
                        "Duration : " + t.duration
        );
    }

    @FXML
    private void onAnimalSelected(ActionEvent event) {
        if (animalSelectCombo.getValue() == null) return;
        int animalID = Integer.parseInt(animalSelectCombo.getValue());
        fillCards(fetchTreatmentsForAnimal(animalID));
    }
    private void switchScene(MouseEvent e, String fxml) throws IOException {
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource("/com/example/demo/src/" + fxml))
        );
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void Barns5(MouseEvent e) throws IOException { switchScene(e, "BRANS.fxml"); }
    public void Production5(MouseEvent e) throws IOException { switchScene(e, "Production.fxml"); }
    public void Treatment5(MouseEvent e) throws IOException { switchScene(e, "Treatment.fxml"); }
    public void Costs5(MouseEvent e) throws IOException { switchScene(e, "Costs.fxml"); }
    public void Employees5(MouseEvent e) throws IOException { switchScene(e, "Employee.fxml"); }
    public void Home5(MouseEvent e) throws IOException { switchScene(e, "main.fxml"); }
    public void Animals5(MouseEvent e) throws IOException { switchScene(e, "animal.fxml"); }
    public void Nutrition5(MouseEvent e) throws IOException { switchScene(e, "Nutrition.fxml"); }

}
