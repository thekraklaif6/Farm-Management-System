package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;


public class ForgotPasswordController {

    // ===== FXML Fields =====
    @FXML
    private TextField emailField;

    // ===== Services =====
    private final AdminService adminService = new AdminService();

    // ===== Button Action =====
    @FXML
    private void handleSendNewPassword() {

        String email = emailField.getText().trim();

        // 1️⃣ ايميل فاضي
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Missing Email",
                    "Please enter your email.");
            return;
        }

        // 2️⃣ ايميل مش موجود
        if (!adminService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR,
                    "Email Not Found",
                    "This email does not exist in the system.");
            return;
        }

        try {
            // 3️⃣ نجاح العملية
            com.example.demo.PasswordUtil PasswordUtil = new com.example.demo.PasswordUtil();
            String newPassword = PasswordUtil.generatePassword();
            adminService.updatePassword(email, newPassword);

            showAlert(Alert.AlertType.INFORMATION,
                    "Password Reset Successful",
                    "Your new password is:\n\n"
                            + newPassword
                            + "\n\nPlease login and change it immediately.");

        } catch (Exception e) {
            // 4️⃣ أي خطأ غير متوقع
            showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "Something went wrong. Please try again.");
        }
    }

    // ===== Alert Helper =====
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    void backButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/LOGIN.fxml"));
        Parent production2Root = loader.load();
        Scene production2Scene = new Scene(production2Root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(production2Scene);
        window.show();
    }
}
