package com.example.demo;

import com.gluonhq.DB;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    private final AdminService adminService = new AdminService();
    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Missing Data",
                    "Please enter email and password");
            return;
        }

        if (checkLogin(email, password)) {
            openNextPage();
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Login Failed",
                    "Invalid email or password");
        }
    }

    // -----------------------------------------
    // Check Login from Database
    // -----------------------------------------
    private boolean checkLogin(String email, String password) {

        String sql = "SELECT 1 FROM Admin WHERE Email = ? AND Password = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------------------
    // Open Next Page
    // -----------------------------------------
    private void openNextPage() {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/com/example/demo/src/main.fxml"))
            );

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------
    // Alert Helper
    // -----------------------------------------
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    @FXML
    private void openNewView() {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/com/example/demo/src/forgotPassword.fxml"))
            );

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AdminService getAdminService() {
        return adminService;
    }
}
