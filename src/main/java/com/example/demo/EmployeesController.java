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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class EmployeesController implements Initializable {

    // Table
    @FXML private TableView<Employee> employeesTable;
    @FXML private TableColumn<Employee, Double> salaryCol;
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private TableColumn<Employee, Integer> hoursCol;
    @FXML private TableColumn<Employee, String> hireDateCol;
    @FXML private TableColumn<Employee, String> jobCol;
    @FXML private TableColumn<Employee, String> genderCol;
    @FXML private TableColumn<Employee, LocalDate> birthDateCol;
    @FXML private TableColumn<Employee, String> nameCol;
    @FXML private TableColumn<Employee, String> nationalIdCol;
    @FXML private TableColumn<Employee, Void> actionsCol;

    // Search
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchByComboBox;

    private final ObservableList<Employee> masterEmployeeList = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredEmployeeList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        setupActionsColumn();
        setupSearch();
        loadEmployeesData();
    }

    private void setupTableColumns() {
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("employmentStatus"));
        hoursCol.setCellValueFactory(new PropertyValueFactory<>("workingHours"));
        hireDateCol.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        jobCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthDateCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nationalIdCol.setCellValueFactory(new PropertyValueFactory<>("nationalId"));
    }


    private void loadEmployeesData() {
        masterEmployeeList.clear();
        String sql = "SELECT * FROM Employee";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                LocalDate hireDate = rs.getDate("HireDate") != null
                        ? rs.getDate("HireDate").toLocalDate()
                        : null;

                LocalDate birthDate = rs.getDate("DateOfBirth") != null
                        ? rs.getDate("DateOfBirth").toLocalDate()
                        : null;

                masterEmployeeList.add(new Employee(
                        rs.getInt("EmployeeID"),
                        rs.getString("FName"),
                        rs.getString("MName"),
                        rs.getString("LName"),
                        hireDate,
                        rs.getString("JobTitle"),
                        rs.getString("Gender"),
                        rs.getInt("WorkingHours"),
                        birthDate,
                        rs.getString("EmploymentStatus"),
                        rs.getDouble("Salary"),
                        rs.getString("NationalID")
                ));
            }

        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }


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
                    Employee employee = getTableView().getItems().get(getIndex());
                    openUpdateEmployeePopup(employee);
                });

                deleteBtn.setOnAction(e -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    deleteEmployee(employee);
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

    private void openUpdateEmployeePopup(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/Employee2.fxml"));
            Parent root = loader.load();
            AddEmployeeController controller = loader.getController();
            controller.initDataForUpdate(employee);

            Stage stage = new Stage();
            stage.setTitle("Update Employee");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadEmployeesData();  // refresh table

        } catch (IOException e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void deleteEmployee(Employee employee) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this employee?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM Employee WHERE EmployeeID = ?";
                try (Connection conn = DB.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, employee.getId());
                    ps.executeUpdate();
                    masterEmployeeList.remove(employee);
                } catch (SQLException e) {
                    showAlert("Database Error", e.getMessage());
                }
            }
        });
    }

    private void setupSearch() {
        searchByComboBox.getItems().addAll("Full Name", "Job Title", "Gender", "Employment Status", "National ID");
        searchByComboBox.setValue("Full Name");

        filteredEmployeeList = new FilteredList<>(masterEmployeeList, p -> true);
        employeesTable.setItems(filteredEmployeeList);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredEmployeeList.setPredicate(employee -> {
                if (newVal == null || newVal.isEmpty()) return true;

                String q = newVal.toLowerCase();
                return switch (searchByComboBox.getValue()) {
                    case "Full Name" -> employee.getFullName().toLowerCase().contains(q);
                    case "Job Title" -> employee.getJobTitle().toLowerCase().contains(q);
                    case "Gender" -> employee.getGender().toLowerCase().contains(q);
                    case "Employment Status" -> employee.getEmploymentStatus().toLowerCase().contains(q);
                    case "National ID" -> employee.getNationalId().toLowerCase().contains(q);
                    default -> true;
                };
            });
        });
    }
    private void switchScene(MouseEvent e, String fxml) throws IOException {
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource("/com/example/demo/src/" + fxml))
        );
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void Barns4(MouseEvent e) throws IOException { switchScene(e, "BRANS.fxml"); }
    public void Production4(MouseEvent e) throws IOException { switchScene(e, "Production.fxml"); }
    public void Treatment4(MouseEvent e) throws IOException { switchScene(e, "Treatment.fxml"); }
    public void Costs4(MouseEvent e) throws IOException { switchScene(e, "Costs.fxml"); }
    public void Employees4(MouseEvent e) throws IOException { switchScene(e, "Employee.fxml"); }
    public void Home4(MouseEvent e) throws IOException { switchScene(e, "main.fxml"); }
    public void Animals4(MouseEvent e) throws IOException { switchScene(e, "animal.fxml"); }
    public void Nutrition4(MouseEvent e) throws IOException { switchScene(e, "Nutrition.fxml"); }

    public void AddEmployee(MouseEvent e) throws IOException {
        switchScene(e, "Employee2.fxml");
    }
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}