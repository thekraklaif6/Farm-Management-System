package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.*;
import java.time.LocalDate;
import java.sql.*;

public class Nutrition2Controller {

    @FXML private ComboBox<Integer> cbBarn;
    @FXML private DatePicker dpDate;

    @FXML private RadioButton rbBarley;
    @FXML private RadioButton rbGrass;
    @FXML private RadioButton rbCorn;
    @FXML private RadioButton rbMixed;

    @FXML private ToggleGroup tgFeedType;

    @FXML private Spinner<Integer> spQuantity;
    @FXML private Spinner<Integer> spWater;
    @FXML private Spinner<Integer> spMealNumber;
    @FXML private Spinner<Double> spPrice;

    @FXML private TextArea txtNotes;

    @FXML private Button btnAddMeal;
    @FXML private Button btnBack;

    private Nutrition1Controller parentController;
    private Nutrition1Controller.FeedingRecord editingRecord;

    // ==================================================
    // INITIALIZE
    // ==================================================
    @FXML
    public void initialize() {
        loadBarnIDs();

        tgFeedType = new ToggleGroup();
        rbBarley.setToggleGroup(tgFeedType);
        rbGrass.setToggleGroup(tgFeedType);
        rbCorn.setToggleGroup(tgFeedType);
        rbMixed.setToggleGroup(tgFeedType);

        spQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 1));
        spWater.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 200, 0));
        spMealNumber.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        spPrice.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 500.0, 0.0, 0.1));

        txtNotes.clear();
    }

    private void loadBarnIDs() {
        cbBarn.getItems().clear();

        String url = "jdbc:postgresql://localhost:5432/DataBase1";
        String user = "postgres";
        String pass = "123456";

        String sql = "SELECT BarnID FROM barn ORDER BY BarnID";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbBarn.getItems().add(rs.getInt("BarnID"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================================================
    // ADD NEW OR UPDATE MEAL
    // ==================================================
    @FXML
    private void onAddMeal() {

        Integer barnID = cbBarn.getValue();
        LocalDate date = dpDate.getValue();

        String feedType = null;
        if (rbBarley.isSelected()) feedType = "Barley";
        else if (rbGrass.isSelected()) feedType = "Grass";
        else if (rbCorn.isSelected()) feedType = "Corn";
        else if (rbMixed.isSelected()) feedType = "Mixed Feed";

        Integer quantity = spQuantity.getValue();
        Integer water = spWater.getValue();
        Integer mealNumber = spMealNumber.getValue();
        Double price = spPrice.getValue();
        String notes = txtNotes.getText();

        // ================================
        // ➕➕➕ ADDED (CALCULATE TOTAL COST)
        // ================================
        double totalCost = price * quantity;

        // VALIDATION
        if (barnID == null) { showError("Missing Data", "Please select a barn."); return; }
        if (date == null) { showError("Missing Data", "Please select a purchase date."); return; }
        if (feedType == null) { showError("Missing Data", "Please select a feed type."); return; }
        if (quantity <= 0) { showError("Invalid Quantity", "Quantity must be greater than 0."); return; }
        if (price < 0) { showError("Invalid Price", "Price cannot be negative."); return; }

        String url = "jdbc:postgresql://localhost:5432/DataBase1";
        String user = "postgres";
        String pass = "123456";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {

            // ================================
            // CASE 1 → UPDATE
            // ================================
            if (editingRecord != null) {

                String sql =
                        "UPDATE Feed SET BarnID=?, Price=?, PurchaseDate=?, Quantity=?, Water=?, MealNumber=?, Notes=?, FeedType=?, total_cost=? " +
                                "WHERE FeedID=?";

                PreparedStatement ps = conn.prepareStatement(sql);

                ps.setInt(1, barnID);
                ps.setDouble(2, price);
                ps.setDate(3, Date.valueOf(date));
                ps.setInt(4, quantity);
                ps.setInt(5, water);
                ps.setInt(6, mealNumber);
                ps.setString(7, notes);
                ps.setString(8, feedType);
                ps.setDouble(9, totalCost);
                ps.setInt(10, editingRecord.getFeedId());

                ps.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Meal Updated");
                alert.setHeaderText(null);
                alert.setContentText("The meal record has been updated successfully.");
                alert.showAndWait();

            } else {

                // ================================
                // CASE 2 → INSERT
                // ================================
                String sql =
                        "INSERT INTO Feed (BarnID, Price, PurchaseDate, Quantity, Water, MealNumber, Notes, FeedType, total_cost) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement ps = conn.prepareStatement(sql);

                ps.setInt(1, barnID);
                ps.setDouble(2, price);
                ps.setDate(3, Date.valueOf(date));
                ps.setInt(4, quantity);
                ps.setInt(5, water);
                ps.setInt(6, mealNumber);
                ps.setString(7, notes);
                ps.setString(8, feedType);
                ps.setDouble(9, totalCost);

                ps.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Meal Saved");
                alert.setHeaderText(null);
                alert.setContentText("Feed data has been successfully recorded.");
                alert.showAndWait();
            }

            if (parentController != null) {
                parentController.refreshTable();
            }

            ((Stage) cbBarn.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Database Error", e.getMessage());
        }
    }

    // ==================================================
    // LOAD RECORD FOR EDIT
    // ==================================================
    public void loadRecordForEdit(Nutrition1Controller.FeedingRecord record) {

        this.editingRecord = record;

        cbBarn.setValue(record.getBarnId());
        dpDate.setValue(record.getPurchaseDate());

        switch (record.getFeedType()) {
            case "Barley": rbBarley.setSelected(true); break;
            case "Grass": rbGrass.setSelected(true); break;
            case "Corn": rbCorn.setSelected(true); break;
            case "Mixed Feed": rbMixed.setSelected(true); break;
        }

        spQuantity.getValueFactory().setValue(record.getQuantity());
        spWater.getValueFactory().setValue(record.getWater());
        spMealNumber.getValueFactory().setValue(record.getMealNumber());
        spPrice.getValueFactory().setValue(record.getPrice());

        txtNotes.setText("");
    }

    // ==================================================
    // BACK BUTTON
    // ==================================================
    @FXML
    private void onBack() {
        ((Stage) btnBack.getScene().getWindow()).close();
    }

    // ==================================================
    // HELPERS
    // ==================================================
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

    public void setParentController(Nutrition1Controller controller) {
        this.parentController = controller;
    }
}
