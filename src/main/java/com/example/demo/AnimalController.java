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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class AnimalController implements Initializable {

    // ===== Table =====
    @FXML private TableView<Animal> animalsTable;
    @FXML private TableColumn<Animal, Integer> idCol;
    @FXML private TableColumn<Animal, String> typeCol;
    @FXML private TableColumn<Animal, String> genderCol;
    @FXML private TableColumn<Animal, Integer> ageCol;
    @FXML private TableColumn<Animal, Double> weightCol;
    @FXML private TableColumn<Animal, String> healthCol;
    @FXML private TableColumn<Animal, String> barnCol;
    @FXML private TableColumn<Animal, Void> actionsCol;

    // ===== Search =====
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchByComboBox;

    private final ObservableList<Animal> masterAnimalList = FXCollections.observableArrayList();
    private FilteredList<Animal> filteredAnimalList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        idCol.setCellValueFactory(new PropertyValueFactory<>("animalID"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeOfAnimal"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        healthCol.setCellValueFactory(new PropertyValueFactory<>("healthStatus"));
        barnCol.setCellValueFactory(new PropertyValueFactory<>("barnName"));

        setupActionsColumn();
        setupSearch();
        loadAnimalsData();
    }

    // =====================================================
    // Load Data (query محسنة بدون AnimalName، Age من DateOfBirth)
    // =====================================================
    private void loadAnimalsData() {

        masterAnimalList.clear();

        String sql = """
            SELECT a.AnimalID, a.TypeOfAnimal,
                   a.GenderAnimal AS Gender, a.Weight AS CurrentWeight,
                   a.HealthStatus, a.BarnID, a.DateOfBirth,
                   b.BarnName
            FROM Animal a
            LEFT JOIN Barn b ON a.BarnID = b.BarnID
            ORDER BY a.AnimalID
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LocalDate dateOfBirth = rs.getDate("DateOfBirth") != null ? rs.getDate("DateOfBirth").toLocalDate() : null;
                masterAnimalList.add(new Animal(
                        rs.getInt("AnimalID"),
                        rs.getDouble("CurrentWeight"),
                        0.0,  // PurchasePrice لو مش موجود هون، جيبه لو بدك
                        rs.getString("Gender"),
                        rs.getDate("DateOfBirth") != null ? rs.getDate("DateOfBirth").toLocalDate() : null,
                        rs.getInt("BarnID"),
                        rs.getString("TypeOfAnimal"),
                        rs.getString("HealthStatus"),
                        rs.getString("BarnName")
                ));
            }

        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }

    // =====================================================
    // Actions Column
    // =====================================================
    private void setupActionsColumn() {

        actionsCol.setCellFactory(param -> new TableCell<>() {

            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();

            {
                ImageView editIcon = new ImageView(
                        new Image(getClass().getResourceAsStream("/images/edit.png"))
                );
                editIcon.setFitWidth(18);
                editIcon.setFitHeight(18);
                editBtn.setGraphic(editIcon);

                ImageView deleteIcon = new ImageView(
                        new Image(getClass().getResourceAsStream("/images/bin.png"))
                );
                deleteIcon.setFitWidth(18);
                deleteIcon.setFitHeight(18);
                deleteBtn.setGraphic(deleteIcon);

                editBtn.setOnAction(e -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    openUpdateAnimalPopup(animal);
                });

                deleteBtn.setOnAction(e -> {
                    Animal animal = getTableView().getItems().get(getIndex());
                    deleteAnimalDirect(animal);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10, editBtn, deleteBtn);
                    box.setStyle("-fx-alignment: CENTER;");
                    setGraphic(box);
                }
            }
        });
    }

    private void openUpdateAnimalPopup(Animal animal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/AddAnimal.fxml"));
            Parent root = loader.load();

            AddAnimalController controller = loader.getController();
            controller.initDataForUpdate(animal);

            Stage stage = new Stage();
            stage.setTitle("Update Animal");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAnimalsData();

        } catch (IOException e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void deleteAnimalDirect(Animal animal) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        String sql = "DELETE FROM Animal WHERE AnimalID = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, animal.getAnimalID());
            ps.executeUpdate();
            masterAnimalList.remove(animal);

        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }

    // =====================================================
    // Search
    // =====================================================
    private void setupSearch() {

        searchByComboBox.getItems().addAll(
                "ID", "Type", "Gender", "Health", "Barn"
        );
        searchByComboBox.setValue("Type");

        filteredAnimalList = new FilteredList<>(masterAnimalList, p -> true);
        animalsTable.setItems(filteredAnimalList);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredAnimalList.setPredicate(animal -> {
                if (newVal == null || newVal.isEmpty()) return true;

                String q = newVal.toLowerCase();
                return switch (searchByComboBox.getValue()) {
                    case "ID" -> String.valueOf(animal.getAnimalID()).contains(q);
                    case "Type" -> animal.getTypeOfAnimal().toLowerCase().contains(q);
                    case "Gender" -> animal.getGender().toLowerCase().contains(q);
                    case "Health" -> animal.getHealthStatus().toLowerCase().contains(q);
                    case "Barn" -> String.valueOf(animal.getBarnID()).contains(q);  // أو barnName لو بدك
                    default -> true;
                };
            });
        });
    }

    // =====================================================
    // Navigation
    // =====================================================
    private void switchScene(MouseEvent e, String fxml) throws IOException {
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource("/com/example/demo/src/" + fxml))
        );
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void Barns3(MouseEvent e) throws IOException { switchScene(e, "BRANS.fxml"); }
    public void Production3(MouseEvent e) throws IOException { switchScene(e, "Production.fxml"); }
    public void Treatment3(MouseEvent e) throws IOException { switchScene(e, "Treatment.fxml"); }
    public void Costs3(MouseEvent e) throws IOException { switchScene(e, "Costs.fxml"); }
    public void Employees3(MouseEvent e) throws IOException { switchScene(e, "Employee.fxml"); }
    public void Home3(MouseEvent e) throws IOException { switchScene(e, "main.fxml"); }
    public void Animals3(MouseEvent e) throws IOException { switchScene(e, "animal.fxml"); }
    public void Nutrition3(MouseEvent e) throws IOException { switchScene(e, "Nutrition.fxml"); }

    public void Add(MouseEvent e) throws IOException {
        switchScene(e, "AddAnimal.fxml");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}