package com.example.demo; // تأكد من أن هذا هو الباكج الصحيح

import com.gluonhq.DB;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HomeController implements Initializable {

    // --- FXML UI Elements ---
    // Top Cards
    @FXML private Label totalAnimalsLabel;
    @FXML private Label dailyProductionLabel;
    @FXML private Label healthStatusLabel;
    @FXML private Label staffPresentLabel;
    @FXML private Label animalsSoldLabel;

    // Charts
    @FXML private PieChart animalDistributionChart;
    @FXML private LineChart<String, Number> productionTrendChart;

    // Quick Stats
    @FXML private Label salesRevenueLabel;
    @FXML private Label feedEfficiencyLabel;
    @FXML private Label reproductionRateLabel;

    @FXML
    private BorderPane bp;
    @FXML
    private AnchorPane ap;

    public void Barns(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/BRANS.fxml")));

        Stage stage = (Stage)
                ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void Animals(javafx.scene.input.MouseEvent mouseEvent) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/animal.fxml")));

        Stage stage = (Stage)
                ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void Nutrition(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/Nutrition.fxml")));

        Stage stage = (Stage)
                ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void Production(javafx.scene.input.MouseEvent mouseEvent) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/Production.fxml")));

        Stage stage = (Stage)
                ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void Treatment(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/Treatment.fxml")));

        Stage stage = (Stage)
                ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void Costs(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/Costs.fxml")));

        Stage stage = (Stage)
                ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void Employees(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/Employee.fxml")));

        Stage stage = (Stage)
                ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void Home(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/demo/src/main.fxml")));

        Stage stage = (Stage)
                ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDashboardData();
    }

    private void loadDashboardData() {
        // تحميل بيانات البطاقات العلوية
        loadTotalAnimals();
        loadDailyProduction();
        loadHealthStatus();
        loadStaffPresence();
        loadAnimalsSold();

        // تحميل بيانات الرسوم البيانية
        loadAnimalDistribution();
        loadProductionTrend();

        // تحميل بيانات الإحصائيات السريعة
        loadSalesRevenue();
        loadApproximateFeedEfficiency(); // ✅ استخدام الطريقة التقديرية
        loadReproductionRate(); // ✅ استخدام الطريقة التقديرية
    }

    // --- دوال جلب البيانات للبطاقات العلوية ---

    private void loadTotalAnimals() {
        String sql = "SELECT COUNT(*) FROM Animal WHERE HealthStatus = 'Alive'";
        try (Connection conn = DB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) totalAnimalsLabel.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) {
            e.printStackTrace();
            totalAnimalsLabel.setText("Error");
        }
    }

    private void loadDailyProduction() {
        String sql = "SELECT COALESCE(SUM(Quantity), 0) FROM Production WHERE ProductionType = 'milk' AND ProductionDate = CURRENT_DATE";
        try (Connection conn = DB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) dailyProductionLabel.setText(String.format("%.0f L", rs.getDouble(1)));
        } catch (SQLException e) {
            e.printStackTrace();
            dailyProductionLabel.setText("Error");
        }
    }

    private void loadHealthStatus() {
        String aliveSql = "SELECT COUNT(*) FROM Animal WHERE HealthStatus = 'Alive'";
        String totalSql = "SELECT COUNT(*) FROM Animal WHERE HealthStatus != 'Sold'";
        try (Connection conn = DB.getConnection()) {
            double aliveCount = 0;
            double totalCount = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(aliveSql); ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) aliveCount = rs.getInt(1);
            }
            try (PreparedStatement pstmt = conn.prepareStatement(totalSql); ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) totalCount = rs.getInt(1);
            }
            if (totalCount > 0) {
                healthStatusLabel.setText(String.format("%.0f%%", (aliveCount / totalCount) * 100));
            } else {
                healthStatusLabel.setText("N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            healthStatusLabel.setText("Error");
        }
    }

    private void loadStaffPresence() {
        String activeSql = "SELECT COUNT(*) FROM Employee WHERE EmploymentStatus = 'Active'";
        String totalSql = "SELECT COUNT(*) FROM Employee";
        try (Connection conn = DB.getConnection()) {
            int activeCount = 0;
            int totalCount = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(activeSql); ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) activeCount = rs.getInt(1);
            }
            try (PreparedStatement pstmt = conn.prepareStatement(totalSql); ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) totalCount = rs.getInt(1);
            }
            staffPresentLabel.setText(String.format("%d/%d", activeCount, totalCount));
        } catch (SQLException e) {
            e.printStackTrace();
            staffPresentLabel.setText("Error");
        }
    }

    private void loadAnimalsSold() {
        String sql = "SELECT COUNT(*) FROM Animal WHERE HealthStatus = 'Sold'";
        try (Connection conn = DB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) animalsSoldLabel.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) {
            e.printStackTrace();
            animalsSoldLabel.setText("Error");
        }
    }

    private void loadAnimalDistribution() {

        // Step 1: Query to get alive animals only
        String sql = "SELECT TypeOfAnimal FROM Animal WHERE HealthStatus = 'Alive'";

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Step 2: Count animals by type using a Map
            Map<String, Integer> animalCountMap = new HashMap<>();

            while (rs.next()) {
                String type = rs.getString("TypeOfAnimal");
                animalCountMap.put(type, animalCountMap.getOrDefault(type, 0) + 1);
            }

            // Step 3: Convert map to PieChart data
            for (Map.Entry<String, Integer> entry : animalCountMap.entrySet()) {
                String type = entry.getKey();
                int count = entry.getValue();

                // Capitalize first letter for display
                String displayName =
                        type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();

                pieChartData.add(new PieChart.Data(displayName, count));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to load animal distribution data.");
            return;
        }

        // Step 4: Set data to chart
        animalDistributionChart.setData(pieChartData);
        animalDistributionChart.setTitle("Animal Distribution");

        // Step 5: Apply colors AFTER chart is rendered
        Platform.runLater(() -> {

            Map<String, String> colorMap = new HashMap<>();
            colorMap.put("Cow",   "#8B4513"); // Brown
            colorMap.put("Goat",  "#D2B48C"); // Light brown
            colorMap.put("Sheep", "#F5DEB3"); // Wool-like color

            for (PieChart.Data data : animalDistributionChart.getData()) {
                String name = data.getName();

                if (data.getNode() != null && colorMap.containsKey(name)) {
                    data.getNode().setStyle(
                            "-fx-pie-color: " + colorMap.get(name) + ";"
                    );
                }
            }
        });
    }

    // ✅✅✅ THIS IS THE NEW, MONTHLY-BASED METHOD ✅✅✅
    private void loadProductionTrend() {
        // The SQL query now groups revenue by the START of the month for the last 12 months.
        // date_trunc('month', ProductionDate) is a powerful PostgreSQL function that does this.
        String sql = """
        SELECT
            date_trunc('month', ProductionDate) AS production_month,
            SUM(Quantity * Price) AS total_revenue
        FROM
            Production
        WHERE
            ProductionDate >= date_trunc('month', CURRENT_DATE) - INTERVAL '11 months'
        GROUP BY
            production_month
        ORDER BY
            production_month ASC
    """;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Monthly Revenue"); // The name of the series is updated

        // A formatter to make the date on the X-axis look like "Jan", "Feb", "Mar"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM"); // "MMM" gives the 3-letter month name

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Format the month's date for display (e.g., "Jan", "Feb")
                String formattedMonth = rs.getDate("production_month").toLocalDate().format(formatter);
                // Add the total revenue for that month to the chart
                series.getData().add(new XYChart.Data<>(formattedMonth, rs.getDouble("total_revenue")));
            }

            productionTrendChart.getData().clear();
            productionTrendChart.getData().add(series);
            productionTrendChart.setTitle("Total Revenue Trend (Last 12 Months)"); // The chart title is updated

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to load monthly production trend data.");
        }
    }


    // --- دوال جلب البيانات للإحصائيات السريعة ---

    // ✅✅✅ NEW METHOD FOR Sales Revenue ✅✅✅
    private void loadSalesRevenue() {
        // This query now calculates the estimated revenue by multiplying
        // the purchase price by 1.5 for all sold animals.
        String sql = "SELECT SUM(PurchasePrice * 1.5) AS estimated_revenue FROM Animal WHERE HealthStatus = 'Sold'";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                double estimatedRevenue = rs.getDouble("estimated_revenue");
                salesRevenueLabel.setText(String.format("%.0f$", estimatedRevenue));
            } else {
                salesRevenueLabel.setText("0$");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            salesRevenueLabel.setText("Error");
        }
    }


    // ✅✅✅ THIS IS THE NEW, CORRECTED METHOD ✅✅✅
    private void loadApproximateFeedEfficiency() {
        // This query now calculates the TOTAL COST of the feed used, not just the quantity.
        // It does this by joining BarnFeed with the Feed table to get the price of each feed type.
        String sql = """
        SELECT
            (SELECT SUM(Quantity * Price) FROM Production) AS total_revenue,
            (SELECT SUM(bf.QuantityUsed * f.Price)
             FROM BarnFeed bf
             JOIN Feed f ON bf.FeedID = f.FeedID) AS total_feed_cost
    """;

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                double totalRevenue = rs.getDouble("total_revenue");
                double totalFeedCost = rs.getDouble("total_feed_cost");

                // Important check: Avoid division by zero if no feed has been used.
                if (totalFeedCost > 0) {
                    // This is the CORRECT formula: (Revenue / Cost) * 100
                    double efficiency = (totalRevenue / totalFeedCost) * 100;
                    feedEfficiencyLabel.setText(String.format("%.0f%%", efficiency));
                } else {
                    // If no feed cost, efficiency is not applicable.
                    feedEfficiencyLabel.setText("N/A");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            feedEfficiencyLabel.setText("Error");
        }
    }

    // ✅✅✅ THE ACCURATE METHOD USING ANIMAL AGE ✅✅✅
    private void loadReproductionRate() {
        // This query is more precise:
        // 1. New Births: Animals born within the last year.
        // 2. Eligible Females: Alive females older than 1 year.
        //    We calculate age using: age(CURRENT_DATE, DateOfBirth)
        String sql = """
        SELECT
            (SELECT COUNT(*) FROM Animal WHERE DateOfBirth >= CURRENT_DATE - INTERVAL '1 year') AS new_births,
            (SELECT COUNT(*) FROM Animal
             WHERE GenderAnimal = 'Female'
               AND HealthStatus = 'Alive'
               AND age(CURRENT_DATE, DateOfBirth) > INTERVAL '1 year') AS eligible_females
    """;

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                double newBirthsCount = rs.getDouble("new_births");
                double eligibleFemalesCount = rs.getDouble("eligible_females");

                // Avoid division by zero
                if (eligibleFemalesCount > 0) {
                    // The rate is (new births / eligible females) * 100
                    double rate = (newBirthsCount / eligibleFemalesCount) * 100;
                    reproductionRateLabel.setText(String.format("%.0f%%", rate));
                } else {
                    // This will be shown if there are no females older than 1 year.
                    reproductionRateLabel.setText("N/A");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            reproductionRateLabel.setText("Error");
        }
    }

}
