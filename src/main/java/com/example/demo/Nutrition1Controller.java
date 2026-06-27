package com.example.demo;
import com.gluonhq.DB;
import javafx.scene.Node;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.transformation.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import net.sf.jasperreports.engine.*;
import java.sql.Connection;

public class Nutrition1Controller {

    @FXML private TableView<FeedingRecord> feedingTable;
    @FXML private TableColumn<FeedingRecord, Integer> feedIdColumn;
    @FXML private TableColumn<FeedingRecord, Integer> barnColumn;
    @FXML private TableColumn<FeedingRecord, Double> priceColumn;
    @FXML private TableColumn<FeedingRecord, LocalDate> dateColumn;
    @FXML private TableColumn<FeedingRecord, Integer> quantityColumn;
    @FXML private TableColumn<FeedingRecord, Integer> waterColumn;
    @FXML private TableColumn<FeedingRecord, Integer> mealColumn;
    @FXML private TableColumn<FeedingRecord, String> feedTypeColumn;
    @FXML private TableColumn<FeedingRecord, Void> actionsColumn;
    @FXML private ComboBox<String> filterCombo;
    @FXML private TextField filterField;
    @FXML private Button btnAddFeeding;

    private final ObservableList<FeedingRecord> masterList = FXCollections.observableArrayList();
    private FilteredList<FeedingRecord> filteredList;

    private final String URL = "jdbc:postgresql://localhost:5432/DB_project";
    private final String USER = "postgres";
    private final String PASS = "139963";

    @FXML
    public void initialize() {

        feedIdColumn.setCellValueFactory(new PropertyValueFactory<>("feedId"));
        barnColumn.setCellValueFactory(new PropertyValueFactory<>("barnId"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        waterColumn.setCellValueFactory(new PropertyValueFactory<>("water"));
        mealColumn.setCellValueFactory(new PropertyValueFactory<>("mealNumber"));
        feedTypeColumn.setCellValueFactory(new PropertyValueFactory<>("feedType"));

        filterCombo.getItems().addAll("Barn ID", "Feed Type", "Meal Number");
        filterCombo.setValue("Barn ID");

        filterField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        loadFeedingRecords();

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final HBox box = new HBox(10);
            private final ImageView editIcon = createIcon("edit.png");
            private final ImageView deleteIcon = createIcon("bin.png");

            {
                box.getChildren().addAll(editIcon, deleteIcon);
                box.setStyle("-fx-alignment: center;");

                editIcon.setOnMouseClicked(e -> {
                    FeedingRecord r = getTableView().getItems().get(getIndex());
                    editRecord(r);
                });

                deleteIcon.setOnMouseClicked(e -> {
                    FeedingRecord r = getTableView().getItems().get(getIndex());
                    deleteRecord(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
    @FXML
    private void openNutritionReport(MouseEvent event) {
        try {
            Connection conn = DB.getConnection();

            InputStream reportStream =
                    getClass().getResourceAsStream("/com/example/demo/src/reports/Blank_A4.jrxml");

            if (reportStream == null) {
                throw new RuntimeException("JRXML file not found in /reports");
            }

            JasperReport jasperReport =
                    JasperCompileManager.compileReport(reportStream);

            JasperPrint jasperPrint =
                    JasperFillManager.fillReport(jasperReport, null, conn);

            JasperViewer.viewReport(jasperPrint, false);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Report Error");
            alert.setHeaderText("Cannot open report");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadFeedingRecords() {
        ObservableList<FeedingRecord> list = FXCollections.observableArrayList();
        String query = "SELECT FeedID, BarnID, Price, PurchaseDate, Quantity, Water, MealNumber, Notes, FeedType FROM Feed";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                FeedingRecord record = new FeedingRecord(
                        rs.getInt("FeedID"),
                        rs.getInt("BarnID"),
                        rs.getDouble("Price"),
                        rs.getDate("PurchaseDate").toLocalDate(),
                        rs.getInt("Quantity"),
                        rs.getInt("Water"),
                        rs.getInt("MealNumber"),
                        rs.getString("Notes"),
                        rs.getString("FeedType")
                );
                list.add(record);
            }

            masterList.setAll(list);

            if (filteredList == null) {
                filteredList = new FilteredList<>(masterList, r -> true);
                feedingTable.setItems(filteredList);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ImageView createIcon(String filename) {
        String fullPath = "/images/" + filename;
        InputStream is = getClass().getResourceAsStream(fullPath);

        Image img = new Image(is);
        ImageView icon = new ImageView(img);
        icon.setFitWidth(18);
        icon.setFitHeight(18);
        icon.setStyle("-fx-cursor: hand;");
        return icon;
    }

    private void editRecord(FeedingRecord record) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/Nutrition2.fxml"));
            Parent root = loader.load();

            Nutrition2Controller controller = loader.getController();
            controller.loadRecordForEdit(record);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Meal");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshTable() {
        loadFeedingRecords();
    }

    private void deleteRecord(FeedingRecord record) {

        if (record == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this record?");
        alert.setContentText("This action cannot be undone.");

        ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesBtn, cancelBtn);

        Optional<ButtonType> result = alert.showAndWait();

        if (!result.isPresent() || result.get() == cancelBtn) {
            return;
        }


        int id = record.getFeedId();
        String sql = "DELETE FROM feed WHERE feedid = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        masterList.remove(record);

        applyFilter();

    }

    public static class FeedingRecord {

        private int feedId;
        private int barnId;
        private double price;
        private LocalDate purchaseDate;
        private int quantity;
        private int water;
        private int mealNumber;
        private String feedType;

        private String notes; // مهم جداً

        public FeedingRecord(int feedId, int barnId, double price, LocalDate purchaseDate,
                             int quantity, int water, int mealNumber, String notes, String feedType) {

            this.feedId = feedId;
            this.barnId = barnId;
            this.price = price;
            this.purchaseDate = purchaseDate;
            this.quantity = quantity;
            this.water = water;
            this.mealNumber = mealNumber;
            this.notes = notes;
            this.feedType = feedType;
        }

        public int getFeedId() { return feedId; }
        public int getBarnId() { return barnId; }
        public double getPrice() { return price; }
        public LocalDate getPurchaseDate() { return purchaseDate; }
        public int getQuantity() { return quantity; }
        public int getWater() { return water; }
        public int getMealNumber() { return mealNumber; }
        public String getFeedType() { return feedType; }
    }

    private void applyFilter() {

        if (filteredList == null) return;

        String filterType = filterCombo.getValue();
        String text = filterField.getText();
        if (text == null) text = "";
        String value = text.trim();

        if (value.isEmpty()) {
            filteredList.setPredicate(record -> true);
            return;
        }

        filteredList.setPredicate(record -> {
            switch (filterType) {

                case "Barn ID":
                    return String.valueOf(record.getBarnId()).equals(value);

                case "Feed Type":
                    return record.getFeedType() != null &&
                            record.getFeedType().toLowerCase().contains(value.toLowerCase());

                case "Meal Number":
                    return String.valueOf(record.getMealNumber()).equals(value);

                default:
                    return true;
            }
        });
    }

    @FXML
    private void onOpenAddMeal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/Nutrition2.fxml"));
            Parent root = loader.load();

            Nutrition2Controller controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Meal");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
