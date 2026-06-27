package com.example.demo;

import com.gluonhq.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;

public class AddAnimalController implements Initializable {

    // --- FXML UI Elements ---
    @FXML private Spinner<Double> weightSpinner;
    @FXML private Spinner<Double> purchasePriceSpinner;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ComboBox<Integer> barnComboBox;
    @FXML private RadioButton cowsRadio;
    @FXML private RadioButton sheepRadio;
    @FXML private RadioButton goatRadio;
    @FXML private RadioButton aliveRadio;
    @FXML private RadioButton sickRadio;
    @FXML private RadioButton soldRadio;
    @FXML private RadioButton deadRadio;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private Button addButton;

    // --- Controller State ---
    private boolean isUpdateMode = false;
    private int animalIdToUpdate = -1;
    private final ObservableList<Integer> barnList = FXCollections.observableArrayList();
    private ToggleGroup genderGroup;
    private ToggleGroup typeOfAnimalGroup;
    private ToggleGroup healthStatusGroup;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupToggleGroups();
        setupSpinners();
        loadBarnsIntoComboBox();
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        ageSpinner.setEditable(false);  // read-only

        // Listener عشان يحسب Age عند change date
        dateOfBirthPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int age = Period.between(newVal, LocalDate.now()).getYears();
                ageSpinner.getValueFactory().setValue(age);
            } else {
                ageSpinner.getValueFactory().setValue(0);
            }
        });
    }

    public void initDataForUpdate(Animal animal) {
        weightSpinner.getValueFactory().setValue(animal.getWeight());
        purchasePriceSpinner.getValueFactory().setValue(animal.getPurchasePrice());
        dateOfBirthPicker.setValue(animal.getDateOfBirth());
        barnComboBox.setValue(animal.getBarnID());

        selectRadioButton(genderGroup, animal.getGender());
        selectRadioButton(typeOfAnimalGroup, animal.getTypeOfAnimal());
        selectRadioButton(healthStatusGroup, animal.getHealthStatus());

        isUpdateMode = true;
        animalIdToUpdate = animal.getAnimalID();
        addButton.setText("Save Changes");
    }

    @FXML
    void handleAddOrUpdateAnimal(ActionEvent event) {
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
        String sql = "INSERT INTO Animal(Weight, PurchasePrice, GenderAnimal, DateOfBirth, BarnID, TypeOfAnimal, HealthStatus) VALUES(?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setAnimalStatementParameters(pstmt);
            pstmt.executeUpdate();
            showAlertDialog(Alert.AlertType.INFORMATION, "Success", "New animal added successfully.");
            closeWindow();

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    private void executeUpdate() {
        String sql = "UPDATE Animal SET Weight = ?, PurchasePrice = ?, GenderAnimal = ?, DateOfBirth = ?, BarnID = ?, TypeOfAnimal = ?, HealthStatus = ? WHERE AnimalID = ?;";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setAnimalStatementParameters(pstmt);
            pstmt.setInt(8, animalIdToUpdate);
            pstmt.executeUpdate();
            showAlertDialog(Alert.AlertType.INFORMATION, "Success", "Animal details updated successfully.");
            closeWindow();

        } catch (SQLException e) {
            handleSqlException(e);
        }
    }

    private void setAnimalStatementParameters(PreparedStatement pstmt) throws SQLException {
        String gender = ((RadioButton) genderGroup.getSelectedToggle()).getText();
        String typeOfAnimal = ((RadioButton) typeOfAnimalGroup.getSelectedToggle()).getText().toLowerCase();
        String healthStatus = ((RadioButton) healthStatusGroup.getSelectedToggle()).getText();

        pstmt.setDouble(1, weightSpinner.getValue());
        pstmt.setDouble(2, purchasePriceSpinner.getValue());
        pstmt.setString(3, gender);
        pstmt.setDate(4, dateOfBirthPicker.getValue() != null ? Date.valueOf(dateOfBirthPicker.getValue()) : null);
        pstmt.setInt(5, barnComboBox.getValue());
        pstmt.setString(6, typeOfAnimal);
        pstmt.setString(7, healthStatus);
    }

    // --- Setup and Helper Methods ---

    private void setupToggleGroups() {
        genderGroup = new ToggleGroup();
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);

        typeOfAnimalGroup = new ToggleGroup();
        cowsRadio.setToggleGroup(typeOfAnimalGroup);
        sheepRadio.setToggleGroup(typeOfAnimalGroup);
        goatRadio.setToggleGroup(typeOfAnimalGroup);

        healthStatusGroup = new ToggleGroup();
        aliveRadio.setToggleGroup(healthStatusGroup);
        sickRadio.setToggleGroup(healthStatusGroup);
        soldRadio.setToggleGroup(healthStatusGroup);
        deadRadio.setToggleGroup(healthStatusGroup);
    }

    private void setupSpinners() {
        weightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1000.0, 50.0, 5.0));
        purchasePriceSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 10000.0, 100.0, 10.0));
    }

    private void loadBarnsIntoComboBox() {
        String sql = "SELECT BarnID FROM Barn WHERE OperationalStatus = 'Active'";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            barnList.clear();
            while (rs.next()) {
                barnList.add(rs.getInt("BarnID"));
            }
            barnComboBox.setItems(barnList);
        } catch (SQLException e) {
            showAlertDialog(Alert.AlertType.ERROR, "Database Error", "Failed to load barns.");
            e.printStackTrace();
        }
    }

    private boolean validateInput() {
        if (genderGroup.getSelectedToggle() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select a 'Gender'.");
            return false;
        }
        if (typeOfAnimalGroup.getSelectedToggle() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select a 'Type of animal'.");
            return false;
        }
        if (healthStatusGroup.getSelectedToggle() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select a 'Health status'.");
            return false;
        }
        if (barnComboBox.getValue() == null) {
            showAlertDialog(Alert.AlertType.WARNING, "Validation Warning", "You must select a 'Barn'.");
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/animal.fxml"));
        Parent production2Root = loader.load();
        Scene production2Scene = new Scene(production2Root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(production2Scene);
        window.show();
    }
}