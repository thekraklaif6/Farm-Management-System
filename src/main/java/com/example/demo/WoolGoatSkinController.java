package com.example.demo;

import com.gluonhq.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

public class WoolGoatSkinController implements Initializable {

    @FXML private TableView<Production> woolGoatTable;
    @FXML private TableColumn<Production, Double> priceCol;
    @FXML private TableColumn<Production, Integer> quantityCol;
    @FXML private TableColumn<Production, String> dateCol;
    @FXML private TableColumn<Production, Integer> animalCol;
    @FXML private TableColumn<Production, Integer> idCol;
    @FXML private TableColumn<Production, Void> actionsCol;

    @FXML private ComboBox<String> searchByComboBox;
    @FXML private TextField searchField;

    @FXML private Label totalWoolLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label avgWoolPerSheepLabel;
    @FXML private Label totalGoatSkinLabel;
    @FXML private Label avgGoatPerGoatLabel;

    private ObservableList<Production> woolGoatData = FXCollections.observableArrayList();
    private FilteredList<Production> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        setupActionsColumn();
        loadWoolGoatData();

        filteredData = new FilteredList<>(woolGoatData, p -> true);
        woolGoatTable.setItems(filteredData);

        searchByComboBox.getItems().addAll("Price", "Quantity", "Date", "Animal", "ID");
        searchField.setOnKeyReleased(event -> handleSearch());

        updateSummary();
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

    private void loadWoolGoatData() {
        woolGoatData.clear();
        String sql = "SELECT * FROM Production WHERE ProductionType IN ('wool','goatskin')";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                woolGoatData.add(new Production(
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
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String selectedField = searchByComboBox.getValue();
        if (selectedField == null) return;

        String query = searchField.getText().toLowerCase();

        filteredData.setPredicate(p -> {
            if (query.isEmpty()) return true;
            return switch (selectedField) {
                case "Price" -> String.valueOf(p.getPrice()).contains(query);
                case "Quantity" -> String.valueOf(p.getQuantity()).contains(query);
                case "Date" -> p.getProductionDate().toLowerCase().contains(query);
                case "Animal" -> String.valueOf(p.getAnimalID()).contains(query);
                case "ID" -> String.valueOf(p.getProductionID()).contains(query);
                default -> true;
            };
        });
    }

    private void openUpdatePopup(Production p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/demo/src/Production2.fxml"));
            Parent root = loader.load();

            AddProductionController controller = loader.getController();
            controller.setUpdateMode(p);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(woolGoatTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setTitle("Update Production");
            stage.showAndWait();

            loadWoolGoatData();
            updateSummary();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void deleteProduction(Production p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this record?", ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM Production WHERE ProductionID = ?";
                try (Connection con = DB.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, p.getProductionID());
                    ps.executeUpdate();

                    loadWoolGoatData();
                    updateSummary();

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete: " + e.getMessage());
                }
            }
        });
    }

    private void updateSummary() {
        // Total Wool (Sheep)
        int totalWool = woolGoatData.stream()
                .filter(p -> "wool".equalsIgnoreCase(p.getProductionType()))
                .mapToInt(Production::getQuantity)
                .sum();
        totalWoolLabel.setText(String.valueOf(totalWool));

        // Average per Sheep
        long sheepCount = woolGoatData.stream()
                .filter(p -> "wool".equalsIgnoreCase(p.getProductionType()))
                .count();
        avgWoolPerSheepLabel.setText(sheepCount > 0 ? String.valueOf(totalWool / sheepCount) : "0");

        // Total GoatSkin
        int totalGoatSkin = woolGoatData.stream()
                .filter(p -> "goatskin".equalsIgnoreCase(p.getProductionType()))
                .mapToInt(Production::getQuantity)
                .sum();
        totalGoatSkinLabel.setText(String.valueOf(totalGoatSkin));

        // Average per Goat
        long goatCount = woolGoatData.stream()
                .filter(p -> "goatskin".equalsIgnoreCase(p.getProductionType()))
                .count();
        avgGoatPerGoatLabel.setText(goatCount > 0 ? String.valueOf(totalGoatSkin / goatCount) : "0");

        // Total Revenue
        double totalRevenue = woolGoatData.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
        totalRevenueLabel.setText(String.format("%.2f $", totalRevenue));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
