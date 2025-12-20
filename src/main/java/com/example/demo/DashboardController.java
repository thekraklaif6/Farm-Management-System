package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class DashboardController {

    @FXML
    private PieChart animalChart;

    // Optionally fx:id for legend labels if you want to update numbers dynamically
    // @FXML private Label sheepLabel; etc.

    @FXML
    public void initialize() {
        // البيانات — استبدل القيم حسب حاجتك
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Sheep", 78),
                new PieChart.Data("Cattle", 45),
                new PieChart.Data("Goats", 32),
                new PieChart.Data("Pigs", 28)
        );

        animalChart.setData(pieData);
        animalChart.setLegendVisible(false); // لأننا سنعرض legend يدوي

        // بعد إضافة البيانات لازم ننتظر nodes لتظهر ثم نلوّن الشرائح
        // هذا الكود يضمن تلوين الشرائح عبر lookup بعد العرض
        animalChart.applyCss(); // force CSS pass
        for (int i = 0; i < pieData.size(); i++) {
            PieChart.Data d = pieData.get(i);
            Node slice = d.getNode();

            // ربط ألوان حسب الفهرس (أو بناء على الاسم)
            String color;
            switch (i) {
                case 0: color = "#C9561E"; break; // sheep (برتقالي داكن)
                case 1: color = "#24461A"; break; // cattle (أخضر داكن)
                case 2: color = "#A9B79C"; break; // goats (أخضر فاتح)
                default: color = "#E7D6C8"; break; // pigs (بيج)
            }

            slice.setStyle("-fx-pie-color: " + color + ";");

            // إضافة tooltip بسيط (اختياري)
            javafx.scene.control.Tooltip t = new javafx.scene.control.Tooltip(
                    d.getName() + ": " + (int)d.getPieValue()
            );
            javafx.scene.control.Tooltip.install(slice, t);
        }

        // --- إذا أردت تحديث أرقام الـ legend labels في الـ UI:
        // sheepLabel.setText("Sheep: " + 78);
        // ... وهكذا
    }
}
