package com.example.demo;

import com.gluonhq.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

public class BarnsController implements Initializable {

    // --- Table & Columns ---
    @FXML private TableView<Barn> barnsTable;
    @FXML private TableColumn<Barn, Integer> idCol;
    @FXML private TableColumn<Barn, String> barnNameCol;
    @FXML private TableColumn<Barn, String> typeCol;
    @FXML private TableColumn<Barn, Integer> capacityCol;
    @FXML private TableColumn<Barn, Integer> currentNumberCol;
    @FXML private TableColumn<Barn, String> statusCol;
    @FXML private TableColumn<Barn, String> employeeCol;
    @FXML private TableColumn<Barn, Void> actionsCol;

    // --- Search ---
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchByComboBox;

    // --- Data ---
    private final ObservableList<Barn> masterBarnList = FXCollections.observableArrayList();
    private FilteredList<Barn> filteredBarnList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        idCol.setCellValueFactory(new PropertyValueFactory<>("barnID"));
        barnNameCol.setCellValueFactory(new PropertyValueFactory<>("barnName"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeOfBarn"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        currentNumberCol.setCellValueFactory(new PropertyValueFactory<>("currentAnimalCount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("operationalStatus"));
        employeeCol.setCellValueFactory(new PropertyValueFactory<>("responsibleEmployeeName"));

        setupActionsColumn();
        setupSearch();
        loadBarnsData();
    }

    // =====================================================
    // Load Data
    // =====================================================
    private void loadBarnsData() {
        masterBarnList.clear();

        String sql = """
            SELECT b.BarnID, b.BarnName, b.TypeOfBarn, b.Capacity,
                   b.CurrentAnimalCount, b.OperationalStatus,
                   b.DateOfEstablishment, b.LocationOfBarn,
                   e.FName, e.LName
            FROM Barn b
            LEFT JOIN Employee_Barn eb ON b.BarnID = eb.barn_id
            LEFT JOIN Employee e ON eb.employee_id = e.EmployeeID
            ORDER BY b.BarnID
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String employeeName = (rs.getString("FName") == null)
                        ? "N/A"
                        : rs.getString("FName") + " " + rs.getString("LName");

                Date d = rs.getDate("DateOfEstablishment");
                LocalDate estDate = (d != null) ? d.toLocalDate() : null;

                masterBarnList.add(new Barn(
                        rs.getInt("BarnID"),
                        rs.getString("BarnName"),
                        rs.getString("TypeOfBarn"),
                        rs.getInt("Capacity"),
                        rs.getInt("CurrentAnimalCount"),
                        rs.getString("OperationalStatus"),
                        employeeName,
                        estDate,
                        rs.getString("LocationOfBarn")
                ));
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
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
                    Barn barn = getTableView().getItems().get(getIndex());
                    openUpdateBarnPopup(barn);
                });

                deleteBtn.setOnAction(e -> {
                    Barn barn = getTableView().getItems().get(getIndex());
                    deleteBarnDirect(barn);
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

    private void openUpdateBarnPopup(Barn barn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/AddBarn.fxml"));
            Parent root = loader.load();

            AddBarnController controller = loader.getController();
            controller.initDataForUpdate(barn);

            Stage stage = new Stage();
            stage.setTitle("Update Barn");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBarnsData();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void deleteBarnDirect(Barn barn) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete barn: " + barn.getBarnName());
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        String sql = "DELETE FROM Barn WHERE BarnID = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, barn.getBarnID());
            ps.executeUpdate();
            masterBarnList.remove(barn);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    // =====================================================
    // Search
    // =====================================================
    private void setupSearch() {

        searchByComboBox.getItems().addAll("ID", "Name", "Type", "Status", "Employee");
        searchByComboBox.setValue("Name");

        filteredBarnList = new FilteredList<>(masterBarnList, p -> true);
        barnsTable.setItems(filteredBarnList);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredBarnList.setPredicate(barn -> {
                if (newVal == null || newVal.isEmpty()) return true;

                String q = newVal.toLowerCase();
                return switch (searchByComboBox.getValue()) {
                    case "ID" -> String.valueOf(barn.getBarnID()).contains(q);
                    case "Name" -> barn.getBarnName().toLowerCase().contains(q);
                    case "Type" -> barn.getTypeOfBarn().toLowerCase().contains(q);
                    case "Status" -> barn.getOperationalStatus().toLowerCase().contains(q);
                    case "Employee" -> barn.getResponsibleEmployeeName().toLowerCase().contains(q);
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

    public void Barns1(MouseEvent e) throws IOException { switchScene(e, "BRANS.fxml"); }
    public void Production1(MouseEvent e) throws IOException { switchScene(e, "Production.fxml"); }
    public void Treatment1(MouseEvent e) throws IOException { switchScene(e, "Treatment.fxml"); }
    public void Costs1(MouseEvent e) throws IOException { switchScene(e, "Costs.fxml"); }
    public void Employees1(MouseEvent e) throws IOException { switchScene(e, "Employee.fxml"); }
    public void Home1(MouseEvent e) throws IOException { switchScene(e, "main.fxml"); }
    public void Animals1(MouseEvent e) throws IOException { switchScene(e, "animal.fxml"); }
    public void Nutrition1(MouseEvent e) throws IOException { switchScene(e, "Nutrition.fxml"); }

    public void Add(MouseEvent mouseEvent) throws IOException {
        switchScene(mouseEvent, "AddBarn.fxml");
    }

    // =====================================================
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
