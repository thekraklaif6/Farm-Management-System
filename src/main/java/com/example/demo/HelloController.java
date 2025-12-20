package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.Objects;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private BreakIterator Login;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }


    public void openNewView(ActionEvent event ) throws IOException{
        Parent root=   FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/forgotPassword.fxml")));

        Stage stage = (Stage)
                ((Node)event.getSource()).getScene().getWindow();
        Scene scene =new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onLoginButtonClick() {

        Login.setText("Welcome to JavaFX Application!");
    }

    public void openHomeView(ActionEvent event ) throws IOException{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/main.fxml")));

        Stage stage = (Stage)
                ((Node)event.getSource()).getScene().getWindow();
        Scene scene =new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
