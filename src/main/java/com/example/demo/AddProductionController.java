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
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddProductionController implements Initializable {

    // --- FXML UI Elements ---
    @FXML private DatePicker ProductionDate;
    @FXML private RadioButton rbSheep;
    @FXML private RadioButton rbCow;
    @FXML private RadioButton rbGoat;
    @FXML private ComboBox<Integer> cbAnimalID;
    @FXML private ComboBox<String> cbProductionType;
    @FXML private Spinner<Integer> spQuantity;
    @FXML private TextField txtPrice;
    @FXML private Button btnBackProduction;
    @FXML private Button btnAddProduction;

    // --- Class Members ---
    private ToggleGroup animalGroup;
    private boolean isUpdateMode = false;
    private int updateProductionId = -1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // إعداد Spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1);
        spQuantity.setValueFactory(valueFactory);

        // تعطيل الحقول التي تعتمد على اختيار نوع الحيوان
        cbAnimalID.setDisable(true);
        cbProductionType.setDisable(true);

        // إعداد مجموعة أزرار الراديو
        animalGroup = new ToggleGroup();
        rbSheep.setToggleGroup(animalGroup);
        rbCow.setToggleGroup(animalGroup);
        rbGoat.setToggleGroup(animalGroup);

        // المستمع لتغيير الاختيار
        animalGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioButton selectedRadio = (RadioButton) newToggle;
                String animalType = selectedRadio.getText().toLowerCase();
                loadAnimalIDs(animalType);
                loadProductionTypes(animalType);
            } else {
                cbAnimalID.getItems().clear();
                cbAnimalID.setDisable(true);
                cbProductionType.getItems().clear();
                cbProductionType.setDisable(true);
            }
        });

        // ربط الـ button
        btnAddProduction.setOnAction(e -> handleSave());
    }

    // لملء الحقول في Update mode
    public void setUpdateMode(Production p) {
        isUpdateMode = true;
        updateProductionId = p.getProductionID();
        ProductionDate.setValue(LocalDate.parse(p.getProductionDate()));
        txtPrice.setText(String.valueOf(p.getPrice()));
        spQuantity.getValueFactory().setValue(p.getQuantity());
        cbProductionType.setValue(p.getProductionType());

        // اختيار الـ RadioButton حسب Animal Type
        String animalType = getAnimalType(p.getAnimalID());
        if ("cows".equalsIgnoreCase(animalType)) rbCow.setSelected(true);
        else if ("sheep".equalsIgnoreCase(animalType)) rbSheep.setSelected(true);
        else if ("goat".equalsIgnoreCase(animalType)) rbGoat.setSelected(true);

        // load IDs و select the ID
        loadAnimalIDs(animalType);
        cbAnimalID.setValue(p.getAnimalID());

        btnAddProduction.setText("Update");  // غير النص
    }

    private String getAnimalType(int animalId) {
        String sql = "SELECT TypeOfAnimal FROM Animal WHERE AnimalID = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, animalId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("TypeOfAnimal").toLowerCase();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load animal type: " + e.getMessage());
        }
        return "";
    }

    private void loadAnimalIDs(String type) {
        cbAnimalID.getItems().clear();
        cbAnimalID.setDisable(false);
        String query = "SELECT AnimalID FROM Animal WHERE TypeOfAnimal = ? AND HealthStatus = 'Alive'";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                cbAnimalID.getItems().add(rs.getInt("AnimalID"));
            }
            if (!found) {
                showError("No Animals Found", "There are no alive animals of type: " + type);
                cbAnimalID.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load animal IDs: " + e.getMessage());
        }
    }

    private void loadProductionTypes(String animalType) {
        cbProductionType.getItems().clear();
        cbProductionType.setDisable(false);
        if ("cows".equalsIgnoreCase(animalType)) {
            cbProductionType.getItems().addAll("milk", "clear");
            cbProductionType.setDisable(true);
        } else if ("sheep".equalsIgnoreCase(animalType)) {
            cbProductionType.getItems().clear();
            cbProductionType.getItems().addAll("milk", "meat", "wool");
        } else if ("goat".equalsIgnoreCase(animalType)) {
            cbProductionType.getItems().clear();
            cbProductionType.getItems().addAll("milk", "meat", "goatskin");
        }
    }

    @FXML
    private void handleSave() {
        LocalDate date = ProductionDate.getValue();
        RadioButton selectedAnimalRadio = (RadioButton) animalGroup.getSelectedToggle();
        Integer animalID = cbAnimalID.getValue();
        String productionType = cbProductionType.getValue();
        Integer quantity = spQuantity.getValue();
        String priceText = txtPrice.getText();

        // ... validation

        if (date == null || selectedAnimalRadio == null || animalID == null || productionType == null || priceText.trim().isEmpty()) {
            showError("Missing Data", "Please fill all required fields.");
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            showError("Invalid Date", "Production date cannot be in the future.");
            return;
        }
        if (quantity <= 0) {
            showError("Invalid Quantity", "Quantity must be positive.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) throw new NumberFormatException("Negative price");
        } catch (NumberFormatException ex) {
            showError("Invalid Price", "Price must be a positive numeric value.");
            return;
        }

        String sql = isUpdateMode ? "UPDATE Production SET ProductionDate = ?, ProductionType = ?, Price = ?, Quantity = ?, AnimalID = ? WHERE ProductionID = ?"
                : "INSERT INTO Production (ProductionDate, ProductionType, Price, Quantity, AnimalID, EmployeeID) VALUES (?, ?, ?, ?, ?, 1)";

        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setString(2, productionType.toLowerCase());
            ps.setDouble(3, price);
            ps.setInt(4, quantity);
            ps.setInt(5, animalID);
            if (isUpdateMode) {
                ps.setInt(6, updateProductionId);
            } else {
                ps.setString(2, productionType.toLowerCase());
                // ... إلخ للـ INSERT fields
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                showInfo("Success", isUpdateMode ? "Production updated successfully." : "Production data has been successfully recorded.");
                clearForm();
                if (isUpdateMode) closeWindow();  // إغلاق لو Update، أو clear لـ Add لوحده
            } else {
                showError("Operation Failed", isUpdateMode ? "The production could not be updated." : "The new production record could not be saved.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to save production data: " + e.getMessage());
        }
    }

    @FXML
    private void onBackProduction() {
        ((Stage) btnBackProduction.getScene().getWindow()).close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        ProductionDate.setValue(null);
        if (animalGroup.getSelectedToggle() != null) animalGroup.getSelectedToggle().setSelected(false);
        cbAnimalID.getItems().clear();
        cbAnimalID.setDisable(true);
        cbProductionType.getItems().clear();
        cbProductionType.setDisable(true);
        spQuantity.getValueFactory().setValue(1);
        txtPrice.clear();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnAddProduction.getScene().getWindow();
        stage.close();
    }
    @FXML
    void backButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/Production.fxml"));
        Parent production2Root = loader.load();
        Scene production2Scene = new Scene(production2Root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(production2Scene);
        window.show();
    }
}