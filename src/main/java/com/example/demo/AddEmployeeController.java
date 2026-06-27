package com.example.demo;

import com.gluonhq.DB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddEmployeeController implements Initializable {

    // --- FXML UI Elements ---
    @FXML private TextField nationalIdField;
    @FXML private TextField employeeNameField;
    @FXML private RadioButton activeRadio;
    @FXML private RadioButton vacationRadio;
    @FXML private RadioButton offWorkRadio;
    @FXML private Spinner<Integer> workingHoursSpinner;
    @FXML private ComboBox<String> jobTitleComboBox;
    @FXML private Spinner<Double> salarySpinner;
    @FXML private DatePicker hireDatePicker;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private Button addButton;

    // --- Controller State ---
    private boolean isUpdateMode = false;
    private int employeeIdToUpdate = -1;
    private ToggleGroup statusGroup;
    private ToggleGroup genderGroup;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupToggleGroups();
        setupSpinners();
        loadJobTitles();
    }

    public void initDataForUpdate(Employee employee) {
        nationalIdField.setText(employee.getNationalId());
        employeeNameField.setText(employee.getFullName());
        workingHoursSpinner.getValueFactory().setValue(employee.getWorkingHours());
        jobTitleComboBox.setValue(employee.getJobTitle());
        salarySpinner.getValueFactory().setValue(employee.getSalary());
        hireDatePicker.setValue(employee.getHireDate());
        dateOfBirthPicker.setValue(employee.getDateOfBirth());

        selectRadioButton(statusGroup, employee.getEmploymentStatus());
        selectRadioButton(genderGroup, employee.getGender());

        isUpdateMode = true;
        employeeIdToUpdate = employee.getId();
        addButton.setText("Save Changes");
    }

    @FXML
    void handleAddOrUpdateEmployee(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        if (isUpdateMode) {
            executeUpdate();
        } else {
            executeInsert();
        }
    }

    private void executeInsert() {
        String sql = "INSERT INTO Employee(NationalID, FName, MName, LName, HireDate, JobTitle, Gender, WorkingHours, DateOfBirth, EmploymentStatus, Salary) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setEmployeeStatementParameters(pstmt);
            pstmt.executeUpdate();
            showAlertDialog(Alert.AlertType.INFORMATION, "Success", "New employee added successfully.");
            closeWindow();

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    private void executeUpdate() {
        String sql = "UPDATE Employee SET NationalID = ?, FName = ?, MName = ?, LName = ?, HireDate = ?, JobTitle = ?, Gender = ?, WorkingHours = ?, DateOfBirth = ?, EmploymentStatus = ?, Salary = ? WHERE EmployeeID = ?;";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setEmployeeStatementParameters(pstmt);
            pstmt.setInt(12, employeeIdToUpdate);
            pstmt.executeUpdate();
            showAlertDialog(Alert.AlertType.INFORMATION, "Success", "Employee details updated successfully.");
            closeWindow();

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    private void setEmployeeStatementParameters(PreparedStatement pstmt) throws SQLException {
        String nationalId = nationalIdField.getText();
        String fullName = employeeNameField.getText().trim();
        String[] names = fullName.split(" ");
        String fName = names.length > 0 ? names[0] : "";
        String mName = names.length > 1 ? names[1] : "";
        String lName = names.length > 2 ? names[2] : "";

        String status = ((RadioButton) statusGroup.getSelectedToggle()).getText();
        String jobTitle = jobTitleComboBox.getValue();
        String gender = ((RadioButton) genderGroup.getSelectedToggle()).getText();

        pstmt.setString(1, nationalId);
        pstmt.setString(2, fName);
        pstmt.setString(3, mName);
        pstmt.setString(4, lName);
        pstmt.setDate(5, hireDatePicker.getValue() != null ? Date.valueOf(hireDatePicker.getValue()) : null);
        pstmt.setString(6, jobTitle);
        pstmt.setString(7, gender);
        pstmt.setInt(8, workingHoursSpinner.getValue());
        pstmt.setDate(9, dateOfBirthPicker.getValue() != null ? Date.valueOf(dateOfBirthPicker.getValue()) : null);
        pstmt.setString(10, status);
        pstmt.setDouble(11, salarySpinner.getValue());
    }

    // --- Setup and Helper Methods ---

    private void setupToggleGroups() {
        statusGroup = new ToggleGroup();
        activeRadio.setToggleGroup(statusGroup);
        vacationRadio.setToggleGroup(statusGroup);
        offWorkRadio.setToggleGroup(statusGroup);

        genderGroup = new ToggleGroup();
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);
    }

    private void setupSpinners() {
        workingHoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 8));
        salarySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 10000.0, 1000.0, 100.0));
    }

    private void loadJobTitles() {
        jobTitleComboBox.getItems().addAll("Farm Manager", "Veterinarian", "Worker", "Technician", "Supervisor", "Caretaker", "Cleaner");
    }

    private boolean validateInput() {
        if (nationalIdField.getText().trim().isEmpty()) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "The 'National ID' field cannot be empty.");
            return false;
        }
        if (employeeNameField.getText().trim().isEmpty()) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "The 'Employee name' field cannot be empty.");
            return false;
        }
        if (statusGroup.getSelectedToggle() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select an 'Employment status'.");
            return false;
        }
        if (jobTitleComboBox.getValue() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select a 'Job title'.");
            return false;
        }
        if (genderGroup.getSelectedToggle() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select a 'Gender'.");
            return false;
        }
        if (dateOfBirthPicker.getValue() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select a 'Date of birth'.");
            return false;
        }
        return true;
    }

    private void handleSqlException(SQLException e) {
        String errorMessage = "Database Error: " + e.getMessage();
        if (e.getSQLState() != null && e.getSQLState().equals("23505")) {
            errorMessage = "Error: A duplicate entry exists.";
        }
        showAlertDialog(Alert.AlertType.ERROR, "Database Error", errorMessage);
        e.printStackTrace();
    }

    private void closeWindow() {
        ((Stage) addButton.getScene().getWindow()).close();
    }

    private void selectRadioButton(ToggleGroup group, String textValue) {
        if (textValue == null) return;
        for (Toggle toggle : group.getToggles()) {
            RadioButton radio = (RadioButton) toggle;
            if (radio.getText().equalsIgnoreCase(textValue)) {
                radio.setSelected(true);
                return;
            }
        }
    }

    private void showAlertDialog(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    void backButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/Employee.fxml"));
        Parent production2Root = loader.load();
        Scene production2Scene = new Scene(production2Root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(production2Scene);
        window.show();
    }
}