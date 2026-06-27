package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.postgresql.jdbc.EscapedFunctions.USER;

public class ProductionController implements Initializable {

    @FXML
    private AnchorPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        loadView("milk_view.fxml");
    }

    @FXML
    void showMilkView(ActionEvent event) {
        System.out.println("Button Clicked: Loading Milk View...");
        loadView("milk_view.fxml");
    }

    @FXML
    void showWoolView(ActionEvent event) {
        System.out.println("Button Clicked: Loading Wool View...");
        loadView("wool_view&goatSkin.fxml");
    }

    @FXML
    void showMeatView(ActionEvent event) {
        System.out.println("Button Clicked: Loading Meat View...");
        loadView("meat_view.fxml");
    }
    @FXML
     void AddProduction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/src/Production2.fxml"));
        Parent production2Root = loader.load();
        Scene production2Scene = new Scene(production2Root);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(production2Scene);
        window.show();
    }

    private void loadView(String fxmlFileName) {
        try {

            String fullPath = "/com/example/demo/src/" + fxmlFileName;


            System.out.println("Attempting to load: " + fullPath);

            Node view = FXMLLoader.load(getClass().getResource(fullPath));

            if (view == null) {
                System.err.println("CRITICAL: FXML file not found at path: " + fullPath);
                return;
            }

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("Failed to load FXML file: " + fxmlFileName);
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

