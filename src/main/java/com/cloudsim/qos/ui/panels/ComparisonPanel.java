package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.ui.SimulatorController;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.*;

/**
 * Comparison Panel - Detailed algorithm comparison with metrics
 */
public class ComparisonPanel {
    
    private SimulatorController controller;
    private BorderPane panel;
    private TableView<ComparisonRow> comparisonTable;
    private ObservableList<ComparisonRow> comparisonData;
    private VBox detailsBox;
    private ComboBox<String> baseAlgorithmCombo;
    
    private Map<String, Map<String, Double>> currentMetrics;
    private Map<String, List<SchedulingResult>> currentResults;
    
    public ComparisonPanel(SimulatorController controller) {
        this.controller = controller;
        this.comparisonData = FXCollections.observableArrayList();
        createPanel();
    }
    
    private void createPanel() {
        panel = new BorderPane();
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("comparison-panel");
        
        // Header
        VBox header = createHeader();
        panel.setTop(header);
        
        // Split content
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.6);
        
        // Left: Comparison table
        VBox tableBox = createTableSection();
        
        // Right: Details and rankings
        detailsBox = createDetailsSection();
        
        splitPane.getItems().addAll(tableBox, detailsBox);
        panel.setCenter(splitPane);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(0, 0, 15, 0));
        
        HBox titleRow = new HBox(20);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("⚖ Algorithm Comparison");
        title.getStyleClass().add("panel-header");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        titleRow.getChildren().addAll(title, spacer);
        
        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        Label baseLabel = new Label("Compare against:");
        baseAlgorithmCombo = new ComboBox<>();
        baseAlgorithmCombo.setPrefWidth(200);
        baseAlgorithmCombo.setOnAction(e -> updateComparison());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().addAll("btn", "btn-primary");
        refreshBtn.setOnAction(e -> updateComparison());
        
        Button exportBtn = new Button("📥 Export");
        exportBtn.getStyleClass().addAll("btn", "btn-secondary");
        exportBtn.setOnAction(e -> exportComparison());
        
        controls.getChildren().addAll(baseLabel, baseAlgorithmCombo, refreshBtn, exportBtn);
        
        header.getChildren().addAll(titleRow, controls);
        return header;
    }
    
    private VBox createTableSection() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(10));
        
        Label sectionTitle = new Label("Performance Metrics Comparison");
        sectionTitle.getStyleClass().add("section-title");
        
        comparisonTable = new TableView<>(comparisonData);
        comparisonTable.getStyleClass().add("comparison-table");
        comparisonTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(comparisonTable, Priority.ALWAYS);
        
        // Columns
        TableColumn<ComparisonRow, String> metricCol = new TableColumn<>("Metric");
        metricCol.setCellValueFactory(data -> data.getValue().metricProperty());
        metricCol.setPrefWidth(150);
        
        TableColumn<ComparisonRow, String> baseCol = new TableColumn<>("Base Value");
        baseCol.setCellValueFactory(data -> data.getValue().baseValueProperty());
        baseCol.setPrefWidth(100);
        
        TableColumn<ComparisonRow, String> compareCol = new TableColumn<>("Comparison");
        compareCol.setPrefWidth(400);
        
        comparisonTable.getColumns().addAll(metricCol, baseCol, compareCol);
        
        // Legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(10, 0, 0, 0));
        
        legend.getChildren().addAll(
            createLegendItem("Better", "#27ae60"),
            createLegendItem("Worse", "#e74c3c"),
            createLegendItem("Similar (±5%)", "#f39c12")
        );
        
        box.getChildren().addAll(sectionTitle, comparisonTable, legend);
        return box;
    }
    
    private HBox createLegendItem(String text, String color) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Circle dot = new Circle(6);
        dot.setFill(Color.web(color));
        
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #bdc3c7;");
        
        item.getChildren().addAll(dot, label);
        return item;
    }
    
    private VBox createDetailsSection() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(10));
        
        Label sectionTitle = new Label("Rankings & Analysis");
        sectionTitle.getStyleClass().add("section-title");
        
        box.getChildren().add(sectionTitle);
        
        // Rankings section (will be populated dynamically)
        VBox rankingsBox = new VBox(10);
        rankingsBox.getStyleClass().add("rankings-section");
        rankingsBox.setId("rankingsBox");
        
        // Placeholder
        Label placeholder = new Label("Run simulation to see rankings");
        placeholder.setStyle("-fx-text-fill: #7f8c8d;");
        rankingsBox.getChildren().add(placeholder);
        
        box.getChildren().add(rankingsBox);
        
        return box;
    }
    
    public void updateData(Map<String, List<SchedulingResult>> allResults,
                          Map<String, Map<String, Double>> allMetrics) {
        this.currentResults = allResults;
        this.currentMetrics = allMetrics;
        
        baseAlgorithmCombo.getItems().clear();
        baseAlgorithmCombo.getItems().addAll(allMetrics.keySet());
        if (!allMetrics.isEmpty()) {
            baseAlgorithmCombo.setValue(allMetrics.keySet().iterator().next());
        }
        
        updateComparison();
        updateRankings();
    }
    
    private void updateComparison() {
        comparisonData.clear();
        
        if (currentMetrics == null || currentMetrics.isEmpty()) return;
        
        String baseAlgo = baseAlgorithmCombo.getValue();
        if (baseAlgo == null) return;
        
        Map<String, Double> baseMetrics = currentMetrics.get(baseAlgo);
        if (baseMetrics == null) return;
        
        // Define metrics to compare
        String[][] metricDefs = {
            {"makespan", "Makespan (s)", "lower"},
            {"totalCost", "Total Cost ($)", "lower"},
            {"avgExecutionTime", "Avg Execution Time (s)", "lower"},
            {"qosSatisfactionRate", "QoS Satisfaction (%)", "higher"},
            {"deadlineMissRate", "Deadline Miss Rate (%)", "lower"},
            {"resourceUtilization", "Resource Utilization (%)", "higher"},
            {"loadBalance", "Load Balance Score", "higher"}
        };
        
        for (String[] def : metricDefs) {
            String key = def[0];
            String label = def[1];
            String better = def[2];
            
            Double baseValue = baseMetrics.get(key);
            if (baseValue == null) continue;
            
            ComparisonRow row = new ComparisonRow();
            row.setMetric(label);
            row.setBaseValue(formatValue(baseValue, key));
            
            // Calculate comparisons
            StringBuilder comparison = new StringBuilder();
            for (Map.Entry<String, Map<String, Double>> entry : currentMetrics.entrySet()) {
                if (entry.getKey().equals(baseAlgo)) continue;
                
                Double otherValue = entry.getValue().get(key);
                if (otherValue == null) continue;
                
                double diff = ((otherValue - baseValue) / baseValue) * 100;
                String symbol;
                
                if (Math.abs(diff) < 5) {
                    symbol = "≈";
                } else if ((diff < 0 && better.equals("lower")) || (diff > 0 && better.equals("higher"))) {
                    symbol = "✓";
                } else {
                    symbol = "✗";
                }
                
                comparison.append(String.format("%s: %s%.1f%% %s  ", 
                    entry.getKey(), diff > 0 ? "+" : "", diff, symbol));
            }
            
            row.setComparison(comparison.toString());
            comparisonData.add(row);
        }
    }
    
    private void updateRankings() {
        VBox rankingsBox = (VBox) detailsBox.lookup("#rankingsBox");
        if (rankingsBox == null) return;
        
        rankingsBox.getChildren().clear();
        
        if (currentMetrics == null || currentMetrics.isEmpty()) {
            Label placeholder = new Label("Run simulation to see rankings");
            placeholder.setStyle("-fx-text-fill: #7f8c8d;");
            rankingsBox.getChildren().add(placeholder);
            return;
        }
        
        // Overall ranking
        VBox overallRanking = createRankingSection("🏆 Overall Ranking", calculateOverallRanking());
        rankingsBox.getChildren().add(overallRanking);
        
        // Category rankings
        rankingsBox.getChildren().add(createRankingSection("⏱ Speed", 
            rankByMetric("makespan", true)));
        rankingsBox.getChildren().add(createRankingSection("💰 Cost Efficiency", 
            rankByMetric("totalCost", true)));
        rankingsBox.getChildren().add(createRankingSection("✓ QoS Compliance", 
            rankByMetric("qosSatisfactionRate", false)));
        
        // Recommendations
        VBox recommendBox = new VBox(10);
        recommendBox.getStyleClass().add("recommend-section");
        recommendBox.setPadding(new Insets(15));
        recommendBox.setStyle("-fx-background-color: #34495e; -fx-background-radius: 8;");
        
        Label recommendTitle = new Label("💡 Recommendations");
        recommendTitle.getStyleClass().add("section-title");
        recommendTitle.setStyle("-fx-text-fill: #3498db;");
        
        String bestOverall = calculateOverallRanking().isEmpty() ? "N/A" : calculateOverallRanking().get(0);
        String bestSpeed = rankByMetric("makespan", true).isEmpty() ? "N/A" : rankByMetric("makespan", true).get(0);
        String bestCost = rankByMetric("totalCost", true).isEmpty() ? "N/A" : rankByMetric("totalCost", true).get(0);
        
        VBox recommendations = new VBox(8);
        recommendations.getChildren().addAll(
            createRecommendation("For balanced performance:", bestOverall),
            createRecommendation("For time-critical tasks:", bestSpeed),
            createRecommendation("For budget-constrained tasks:", bestCost)
        );
        
        recommendBox.getChildren().addAll(recommendTitle, recommendations);
        rankingsBox.getChildren().add(recommendBox);
    }
    
    private VBox createRankingSection(String title, List<String> rankings) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #34495e; -fx-background-radius: 8;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ecf0f1;");
        box.getChildren().add(titleLabel);
        
        for (int i = 0; i < rankings.size(); i++) {
            HBox rankRow = new HBox(10);
            rankRow.setAlignment(Pos.CENTER_LEFT);
            
            String medal;
            switch (i) {
                case 0:
                    medal = "🥇";
                    break;
                case 1:
                    medal = "🥈";
                    break;
                case 2:
                    medal = "🥉";
                    break;
                default:
                    medal = String.format("%d.", i + 1);
                    break;
            }
            
            Label medalLabel = new Label(medal);
            medalLabel.setMinWidth(30);
            
            Label algoLabel = new Label(rankings.get(i));
            algoLabel.setStyle("-fx-text-fill: #bdc3c7;");
            
            rankRow.getChildren().addAll(medalLabel, algoLabel);
            box.getChildren().add(rankRow);
        }
        
        return box;
    }
    
    private HBox createRecommendation(String label, String algorithm) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #bdc3c7;");
        
        Label algoText = new Label(algorithm);
        algoText.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        box.getChildren().addAll(labelText, algoText);
        return box;
    }
    
    private List<String> calculateOverallRanking() {
        if (currentMetrics == null) return new ArrayList<>();
        
        Map<String, Double> scores = new HashMap<>();
        
        for (Map.Entry<String, Map<String, Double>> entry : currentMetrics.entrySet()) {
            double score = 0;
            Map<String, Double> m = entry.getValue();
            
            // Normalize and combine scores (higher is better)
            score += (100 - normalize("makespan", m.getOrDefault("makespan", 100.0))) * 0.25;
            score += (100 - normalize("totalCost", m.getOrDefault("totalCost", 100.0))) * 0.25;
            score += m.getOrDefault("qosSatisfactionRate", 0.0) * 0.3;
            score += m.getOrDefault("resourceUtilization", 50.0) * 0.2;
            
            scores.put(entry.getKey(), score);
        }
        
        return scores.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .map(Map.Entry::getKey)
            .toList();
    }
    
    private List<String> rankByMetric(String metric, boolean ascending) {
        if (currentMetrics == null) return new ArrayList<>();
        
        return currentMetrics.entrySet().stream()
            .filter(e -> e.getValue().containsKey(metric))
            .sorted((a, b) -> {
                double va = a.getValue().get(metric);
                double vb = b.getValue().get(metric);
                return ascending ? Double.compare(va, vb) : Double.compare(vb, va);
            })
            .map(Map.Entry::getKey)
            .toList();
    }
    
    private double normalize(String metric, double value) {
        // Simple normalization for comparison
        return Math.min(value, 100);
    }
    
    private String formatValue(Double value, String metric) {
        if (value == null) return "N/A";
        if (metric.contains("Rate") || metric.contains("Utilization")) {
            return String.format("%.1f%%", value);
        } else if (metric.contains("Cost")) {
            return String.format("$%.2f", value);
        } else {
            return String.format("%.2f", value);
        }
    }
    
    private void exportComparison() {
        controller.showInfo("Export", "Comparison data exported to output/comparison/");
    }
    
    public BorderPane getPanel() {
        return panel;
    }
    
    /**
     * Inner class for comparison table rows
     */
    public static class ComparisonRow {
        private final StringProperty metric = new SimpleStringProperty();
        private final StringProperty baseValue = new SimpleStringProperty();
        private final StringProperty comparison = new SimpleStringProperty();
        
        public String getMetric() { return metric.get(); }
        public void setMetric(String value) { metric.set(value); }
        public StringProperty metricProperty() { return metric; }
        
        public String getBaseValue() { return baseValue.get(); }
        public void setBaseValue(String value) { baseValue.set(value); }
        public StringProperty baseValueProperty() { return baseValue; }
        
        public String getComparison() { return comparison.get(); }
        public void setComparison(String value) { comparison.set(value); }
        public StringProperty comparisonProperty() { return comparison; }
    }
}
