package com.example.demo;
import com.gluonhq.DB;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class CostsController {

    // -----------------------------
    // UI Components
    // -----------------------------
    @FXML
    private Label totalCostsLabel;
    @FXML
    private ProgressBar treatmentProgress;
    @FXML
    private Label treatmentPercentLabel;
    @FXML
    private ProgressBar feedProgress;
    @FXML
    private Label feedPercentLabel;
    @FXML
    private ProgressBar laborProgress;
    @FXML
    private Label laborPercentLabel;
    @FXML
    private Label totalIncomeLabel;
    @FXML
    private Label netProfitLabel;
    @FXML
    private PieChart costsPieChart;

    // -----------------------------
    // Initialize
    // -----------------------------
    @FXML
    public void initialize() {

        // ---------- Total Costs ----------
        double totalCosts = getTotalCosts();
        totalCostsLabel.setText(String.format("%.2f ₪", totalCosts));

        // ---------- Total Income ----------
        double totalIncome = getTotalIncome();
        totalIncomeLabel.setText(String.format("%.2f ₪", totalIncome));

        // ---------- Net Profit ----------
        double netProfit = totalIncome - totalCosts;
        netProfitLabel.setText(String.format("%.2f ₪", netProfit));
        // ---------- Treatment ----------
        double treatmentRatio = getTreatmentPercentage();
        treatmentProgress.setProgress(treatmentRatio);
        treatmentPercentLabel.setText( Math.round(treatmentRatio * 100) + " %" );

        // ---------- Feed ----------
        double feedRatio = getFeedPercentage();
        feedProgress.setProgress(feedRatio);
        feedPercentLabel.setText( Math.round(feedRatio * 100) + " %"  );
        // ---------- Labor ----------
        double laborRatio = getLaborPercentage();
        laborProgress.setProgress(laborRatio);
        laborPercentLabel.setText(
                Math.round(laborRatio * 100) + " %");
        // ---------- Pie Chart ----------
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Labor Wages", laborRatio * 100),
                new PieChart.Data("Treatments", treatmentRatio * 100),
                new PieChart.Data("Feed", feedRatio * 100)
        );

        costsPieChart.setData(pieChartData);
        costsPieChart.setLegendVisible(false);

        Platform.runLater(() -> {
            String[] colors = {
                    "#C7A35C",  // Labor
                    "#C9A24D",  // Treatments
                    " #d2b48c"   // Feed
            };

            int i = 0;
            for (PieChart.Data data : costsPieChart.getData()) {
                data.getNode().setStyle(
                        "-fx-pie-color: " + colors[i % colors.length] + ";"
                );
                i++;
            }
        });

    }
    // -----------------------------
    // Get Treatment Costs
    // -----------------------------
    private double getTreatmentCosts() {

        String sql =
                "SELECT COALESCE(SUM(total_cost), 0) " +"FROM Treatment " +"WHERE exa_date >= date_trunc('month', CURRENT_DATE) " +
                        "AND exa_date < date_trunc('month', CURRENT_DATE) + INTERVAL '1 month'";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private double getFeedCosts() {

        String sql =
                "SELECT COALESCE(SUM(Price * Quantity), 0) " + "FROM Feed " +"WHERE PurchaseDate >= date_trunc('month', CURRENT_DATE) " +
                        "AND PurchaseDate < date_trunc('month', CURRENT_DATE) + INTERVAL '1 month'";


        try (Connection con =DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    // -----------------------------
// Get Labor Costs (Monthly Salaries)
// -----------------------------
    private double getLaborCosts() {

        String sql = "SELECT COALESCE(SUM(Salary), 0) FROM Employee WHERE EmploymentStatus = 'Active'";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // -----------------------------
    private double getTotalCosts() {
        return getTreatmentCosts() + getFeedCosts()+ getLaborCosts();
    }

    private double getTotalIncome() {

        String sql =
                "SELECT COALESCE(SUM(price * quantity), 0) " +
                        "FROM Production " +
                        "WHERE ProductionDate >= date_trunc('month', CURRENT_DATE)";

        try (Connection con =DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    private double getNetProfit() {
        return getTotalIncome() - getTotalCosts();
    }

    // -----------------------------
    // Treatment Percentage
    // -----------------------------
    private double getTreatmentPercentage() {

        double treatment = getTreatmentCosts();
        double total = getTotalCosts();

        if (total == 0) {
            return 0;
        }

        return treatment / total; // بين 0 و 1
    }

    // -----------------------------
    // Feed Percentage
    // -----------------------------
    private double getFeedPercentage() {

        double feed = getFeedCosts();
        double total = getTotalCosts();


        if (total == 0) return 0;

        return feed / total;   // بين 0 و 1
    }
    // -----------------------------
// Labor Percentage
// -----------------------------
    private double getLaborPercentage() {

        double labor = getLaborCosts();
        double total = getTotalCosts();

        if (total == 0) return 0;

        return labor / total; // بين 0 و 1
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
