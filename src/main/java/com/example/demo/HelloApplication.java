package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/demo/src/main.fxml"));
         //FXMLLoader fxmlLoader1 = new FXMLLoader(HelloApplication.class.getResource("/com/example/demo/src/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
       // Scene scene1 = new Scene(fxmlLoader1.load(), 452, 650);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

}
