package com.example.demo;

import com.gluonhq.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddBarnController implements Initializable {

    // --- FXML UI Elements ---
    @FXML private TextField barnNameField;
    @FXML private TextField locationField;
    @FXML private DatePicker establishmentDatePicker;
    @FXML private Spinner<Integer> capacitySpinner;
    @FXML private Spinner<Integer> currentAnimalCountSpinner;
    @FXML private Spinner<Double> barnAreaSpinner;
    @FXML private RadioButton cowsRadio;
    @FXML private RadioButton sheepRadio;
    @FXML private RadioButton goatRadio;
    @FXML private RadioButton activeRadio;
    @FXML private RadioButton maintenanceRadio;
    @FXML private RadioButton desertedRadio;
    @FXML private Button addButton;
    @FXML private ComboBox<Employee> responsibleEmployeeComboBox;

    // --- Controller State ---
    private boolean isUpdateMode = false;
    private int barnIdToUpdate = -1;
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private ToggleGroup typeOfBarnGroup;
    private ToggleGroup operationalStatusGroup;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupToggleGroups();
        setupSpinners();
        loadEmployeesIntoComboBox();
    }

    /**
     * This method is called from the BarnsViewController to prepare the screen for an update.
     * @param barn The barn object selected in the table.
     */
    public void initDataForUpdate(Barn barn) {
        barnNameField.setText(barn.getBarnName());
        locationField.setText(barn.getLocation());
        establishmentDatePicker.setValue(barn.getEstablishmentDate());
        capacitySpinner.getValueFactory().setValue(barn.getCapacity());
        currentAnimalCountSpinner.getValueFactory().setValue(barn.getCurrentAnimalCount());

        selectRadioButton(typeOfBarnGroup, barn.getTypeOfBarn());
        selectRadioButton(operationalStatusGroup, barn.getOperationalStatus());

        for (Employee emp : responsibleEmployeeComboBox.getItems()) {
            if (emp.getFullName().equals(barn.getResponsibleEmployeeName())) {
                responsibleEmployeeComboBox.setValue(emp);
                break;
            }
        }

        isUpdateMode = true;
        barnIdToUpdate = barn.getBarnID();
        addButton.setText("Save Changes");
    }


    @FXML
    void handleAddOrUpdateBarn(ActionEvent event) {
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
        String barnSql = "INSERT INTO Barn(BarnName, Capacity, BarnArea, DateOfEstablishment, LocationOfBarn, TypeOfBarn, OperationalStatus, CurrentAnimalCount) VALUES(?, ?, ?, ?, ?, ?, ?, ?) RETURNING BarnID;";
        String employeeBarnSql = "INSERT INTO Employee_Barn(employee_id, barn_id, start_date) VALUES(?, ?, ?);";

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            int newBarnId = -1;

            try (PreparedStatement barnPstmt = conn.prepareStatement(barnSql)) {
                setBarnStatementParameters(barnPstmt);
                ResultSet rs = barnPstmt.executeQuery();
                if (rs.next()) newBarnId = rs.getInt(1);
                else throw new SQLException("Creating barn failed, no ID obtained.");
            }

            Employee selectedEmployee = responsibleEmployeeComboBox.getValue();
            if (selectedEmployee != null && newBarnId != -1) {
                try (PreparedStatement empBarnPstmt = conn.prepareStatement(employeeBarnSql)) {
                    empBarnPstmt.setInt(1, selectedEmployee.getId());
                    empBarnPstmt.setInt(2, newBarnId);
                    empBarnPstmt.setDate(3, Date.valueOf(java.time.LocalDate.now()));
                    empBarnPstmt.executeUpdate();
                }
            }

            conn.commit();
            showAlertDialog(Alert.AlertType.INFORMATION, "Success", "New barn added successfully.");
            closeWindow();

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    private void executeUpdate() {
        String barnUpdateSql = "UPDATE Barn SET BarnName = ?, Capacity = ?, BarnArea = ?, DateOfEstablishment = ?, LocationOfBarn = ?, TypeOfBarn = ?, OperationalStatus = ?, CurrentAnimalCount = ? WHERE BarnID = ?;";
        String employeeDeleteSql = "DELETE FROM Employee_Barn WHERE barn_id = ?;";
        String employeeInsertSql = "INSERT INTO Employee_Barn(employee_id, barn_id, start_date) VALUES(?, ?, ?);";

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement barnPstmt = conn.prepareStatement(barnUpdateSql)) {
                setBarnStatementParameters(barnPstmt);
                barnPstmt.setInt(9, barnIdToUpdate);
                barnPstmt.executeUpdate();
            }

            try (PreparedStatement empDelPstmt = conn.prepareStatement(employeeDeleteSql)) {
                empDelPstmt.setInt(1, barnIdToUpdate);
                empDelPstmt.executeUpdate();
            }

            Employee selectedEmployee = responsibleEmployeeComboBox.getValue();
            if (selectedEmployee != null) {
                try (PreparedStatement empInsPstmt = conn.prepareStatement(employeeInsertSql)) {
                    empInsPstmt.setInt(1, selectedEmployee.getId());
                    empInsPstmt.setInt(2, barnIdToUpdate);
                    empInsPstmt.setDate(3, Date.valueOf(java.time.LocalDate.now()));
                    empInsPstmt.executeUpdate();
                }
            }

            conn.commit();
            showAlertDialog(Alert.AlertType.INFORMATION, "Success", "Barn details updated successfully.");
            closeWindow();

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    private void setBarnStatementParameters(PreparedStatement pstmt) throws SQLException {
        String typeOfBarn = ((RadioButton) typeOfBarnGroup.getSelectedToggle()).getText().toLowerCase();
        String operationalStatus = getSelectedOperationalStatus();

        pstmt.setString(1, barnNameField.getText());
        pstmt.setInt(2, capacitySpinner.getValue());
        pstmt.setDouble(3, barnAreaSpinner.getValue());
        pstmt.setDate(4, establishmentDatePicker.getValue() != null ? Date.valueOf(establishmentDatePicker.getValue()) : null);
        pstmt.setString(5, locationField.getText());
        pstmt.setString(6, typeOfBarn);
        pstmt.setString(7, operationalStatus);
        pstmt.setInt(8, currentAnimalCountSpinner.getValue());
    }

    // --- Setup and Helper Methods ---

    private void setupToggleGroups() {
        typeOfBarnGroup = new ToggleGroup();
        cowsRadio.setToggleGroup(typeOfBarnGroup);
        sheepRadio.setToggleGroup(typeOfBarnGroup);
        goatRadio.setToggleGroup(typeOfBarnGroup);

        operationalStatusGroup = new ToggleGroup();
        activeRadio.setToggleGroup(operationalStatusGroup);
        maintenanceRadio.setToggleGroup(operationalStatusGroup);
        desertedRadio.setToggleGroup(operationalStatusGroup);
    }

    private void setupSpinners() {
        capacitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, 50));
        currentAnimalCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, 0));
        barnAreaSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 10000.0, 100.0, 10.0));
    }

    private void loadEmployeesIntoComboBox() {
        String sql = "SELECT EmployeeID, FName, LName FROM Employee WHERE EmploymentStatus = 'Active'";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            employeeList.clear();
            while (rs.next()) {
                employeeList.add(new Employee(rs.getInt("EmployeeID"), rs.getString("FName") + " " + rs.getString("LName")));
            }
            responsibleEmployeeComboBox.setItems(employeeList);
        } catch (SQLException e) {
            showAlertDialog(Alert.AlertType.ERROR, "Database Error", "Failed to load employees.");
            e.printStackTrace();
        }
    }

    private boolean validateInput() {
        if (barnNameField.getText().trim().isEmpty()) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "The 'Barn name' field cannot be empty.");
            return false;
        }
        if (typeOfBarnGroup.getSelectedToggle() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select a 'Type of barn'.");
            return false;
        }
        if (operationalStatusGroup.getSelectedToggle() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select an 'Operational status'.");
            return false;
        }
        return true;
    }

    private void handleSqlException(SQLException e) {
        String errorMessage = "Database Error: " + e.getMessage();
        if (e.getSQLState() != null && e.getSQLState().equals("23505")) {
            errorMessage = "Error: The barn name '" + barnNameField.getText() + "' already exists.";
        }
        showAlertDialog(Alert.AlertType.ERROR, "Database Error", errorMessage);
        e.printStackTrace();
    }

    private void closeWindow() {
        ((Stage) addButton.getScene().getWindow()).close();
    }

    private String getSelectedOperationalStatus() {
        if (operationalStatusGroup.getSelectedToggle() == null) return null;
        String selectedText = ((RadioButton) operationalStatusGroup.getSelectedToggle()).getText();
        if (selectedText.equals("Under Maintenance")) {
            return "UnderMaintenance";
        }
        return selectedText;
    }

    private void selectRadioButton(ToggleGroup group, String textValue) {
        if (textValue == null) return;
        for (Toggle toggle : group.getToggles()) {
            RadioButton radio = (RadioButton) toggle;
            String radioText = radio.getText();
            if (radioText.equalsIgnoreCase(textValue) || (radioText.equals("Under Maintenance") && textValue.equals("UnderMaintenance"))) {
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
}
