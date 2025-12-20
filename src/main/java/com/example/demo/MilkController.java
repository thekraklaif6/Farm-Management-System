package com.example.demo;

import com.gluonhq.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MilkController implements Initializable {

    @FXML private TableView<Production> milkTable;
    @FXML private TableColumn<Production, Double> priceCol;
    @FXML private TableColumn<Production, Integer> quantityCol;
    @FXML private TableColumn<Production, String> dateCol;
    @FXML private TableColumn<Production, Integer> animalCol;
    @FXML private TableColumn<Production, Integer> idCol;
    @FXML private TableColumn<Production, Void> actionsCol;
    @FXML private ComboBox<String> searchByComboBox;
    @FXML private TextField searchField;
    @FXML private PieChart milkPieChart;
    @FXML private BarChart<String, Number> milkBarChart;
    @FXML private Label totalRevenueLabel;

    private ObservableList<Production> milkData = FXCollections.observableArrayList();
    private FilteredList<Production> filteredMilkData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        setupActionsColumn();
        loadMilkData();
        filteredMilkData = new FilteredList<>(milkData, p -> true);
        milkTable.setItems(filteredMilkData);

        searchByComboBox.getItems().addAll("Price", "Quantity", "Date", "Animal", "ID");
        searchField.setOnKeyReleased(this::handleSearch);

        updateCharts();
        if (totalRevenueLabel != null) {
            totalRevenueLabel.setText("Total Revenue: $" + calculateTotalRevenue());
        }
    }

    private void setupTableColumns() {
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        animalCol.setCellValueFactory(new PropertyValueFactory<>("animalID"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("productionID"));
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<Production, Void>() {
            private final Button updateBtn = new Button();
            private final Button deleteBtn = new Button();

            {
                // Icons (adjust paths if needed)
                ImageView updateIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/edit.png"))));
                updateIcon.setFitWidth(20);
                updateIcon.setFitHeight(20);
                updateBtn.setGraphic(updateIcon);

                ImageView deleteIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/bin.png"))));
                deleteIcon.setFitWidth(20);
                deleteIcon.setFitHeight(20);
                deleteBtn.setGraphic(deleteIcon);

                updateBtn.setOnAction(event -> {
                    Production p = getTableView().getItems().get(getIndex());
                    openUpdatePopup(p);
                });
                deleteBtn.setOnAction(event -> {
                    Production p = getTableView().getItems().get(getIndex());
                    deleteProduction(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actionsBox = new HBox(updateBtn, deleteBtn);
                    actionsBox.setSpacing(10);
                    setGraphic(actionsBox);
                }
            }
        });
    }

    private void loadMilkData() {
        milkData.clear();
        String sql = "SELECT * FROM Production WHERE ProductionType = 'milk'";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                milkData.add(new Production(
                        rs.getInt("ProductionID"),
                        rs.getDate("ProductionDate").toString(),
                        rs.getString("ProductionType"),
                        rs.getDouble("Price"),
                        rs.getInt("Quantity"),
                        rs.getInt("AnimalID"),
                        rs.getInt("EmployeeID")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load milk data: " + e.getMessage());
        }
    }

    private void handleSearch(KeyEvent event) {
        String selectedField = searchByComboBox.getValue();
        if (selectedField == null) return;

        String query = searchField.getText().toLowerCase();
        filteredMilkData.setPredicate(p -> {
            if (query.isEmpty()) return true;
            switch (selectedField) {
                case "Price": return String.valueOf(p.getPrice()).toLowerCase().contains(query);
                case "Quantity": return String.valueOf(p.getQuantity()).toLowerCase().contains(query);
                case "Date": return p.getProductionDate().toLowerCase().contains(query);
                case "Animal": return String.valueOf(p.getAnimalID()).toLowerCase().contains(query);
                case "ID": return String.valueOf(p.getProductionID()).toLowerCase().contains(query);
                default: return false;
            }
        });
    }

    private void openUpdatePopup(Production p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/Production2.fxml"));
            Parent root = loader.load();
            AddProductionController controller = loader.getController();
            controller.setUpdateMode(p);

            Stage stage = new Stage();
            stage.setTitle("Update Production");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(milkTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadMilkData();
            updateCharts();
            if (totalRevenueLabel != null) totalRevenueLabel.setText("Total Revenue: $" + calculateTotalRevenue());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open update: " + e.getMessage());
        }
    }

    private void deleteProduction(Production p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this record?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM Production WHERE ProductionID = ?";
                try (Connection con = DB.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, p.getProductionID());
                    ps.executeUpdate();
                    loadMilkData();
                    updateCharts();
                    if (totalRevenueLabel != null) totalRevenueLabel.setText("Total Revenue: $" + calculateTotalRevenue());
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete: " + e.getMessage());
                }
            }
        });
    }

    private void updateCharts() {

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        milkData.stream()
                .collect(Collectors.groupingBy(Production::getAnimalID, Collectors.summingInt(Production::getQuantity)))
                .forEach((animalId, totalQty) -> pieData.add(new PieChart.Data("Animal " + animalId, totalQty)));
        milkPieChart.setData(pieData);

        // BarChart: سعر متوسط + ربح لكل حيوان
        XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
        priceSeries.setName("Average Price");
        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        profitSeries.setName("Profit");
        milkData.stream()
                .collect(Collectors.groupingBy(Production::getAnimalID))
                .forEach((animalId, list) -> {
                    double avgPrice = list.stream().mapToDouble(Production::getPrice).average().orElse(0);
                    double profit = list.stream().mapToDouble(pr -> pr.getPrice() * pr.getQuantity()).sum();
                    priceSeries.getData().add(new XYChart.Data<>("Animal " + animalId, avgPrice));
                    profitSeries.getData().add(new XYChart.Data<>("Animal " + animalId, profit));
                });
        milkBarChart.getData().clear();
        milkBarChart.getData().addAll(priceSeries, profitSeries);
    }


    private double calculateTotalRevenue() {
        return milkData.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}