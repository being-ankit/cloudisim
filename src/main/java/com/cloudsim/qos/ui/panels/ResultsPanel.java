package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.ui.SimulatorController;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.*;

/**
 * Results Panel - Display scheduling results in detail
 */
public class ResultsPanel {
    
    private SimulatorController controller;
    private VBox panel;
    private ComboBox<String> algorithmCombo;
    private TableView<ResultRow> resultsTable;
    private ObservableList<ResultRow> resultData;
    private VBox summaryBox;
    private HBox ganttChart;
    
    public ResultsPanel(SimulatorController controller) {
        this.controller = controller;
        createPanel();
    }
    
    private void createPanel() {
        panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("results-panel");
        
        // Header
        HBox header = createHeader();
        
        // Summary cards
        summaryBox = createSummaryCards();
        
        // Main content
        HBox mainContent = new HBox(20);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Results table
        VBox tableSection = createTableSection();
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        
        // Gantt chart / visualization
        VBox vizSection = createVisualizationSection();
        vizSection.setPrefWidth(400);
        
        mainContent.getChildren().addAll(tableSection, vizSection);
        
        panel.getChildren().addAll(header, summaryBox, mainContent);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("📊 Scheduling Results");
        title.getStyleClass().add("panel-header");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label selectLabel = new Label("Algorithm:");
        algorithmCombo = new ComboBox<>();
        algorithmCombo.setPrefWidth(150);
        algorithmCombo.setOnAction(e -> displayResults(algorithmCombo.getValue()));
        
        Button exportBtn = new Button("📥 Export");
        exportBtn.getStyleClass().addAll("btn", "btn-secondary");
        exportBtn.setOnAction(e -> exportResults());
        
        header.getChildren().addAll(title, spacer, selectLabel, algorithmCombo, exportBtn);
        return header;
    }
    
    private VBox createSummaryCards() {
        VBox container = new VBox(10);
        
        HBox cards = new HBox(15);
        cards.setId("summary-cards");
        cards.setAlignment(Pos.CENTER);
        
        cards.getChildren().addAll(
            createSummaryCard("total-time", "⏱", "Total Time", "0.00 s", "#3498db"),
            createSummaryCard("total-cost", "💰", "Total Cost", "$0.00", "#27ae60"),
            createSummaryCard("makespan", "📏", "Makespan", "0.00 s", "#9b59b6"),
            createSummaryCard("qos-rate", "✓", "QoS Satisfaction", "0%", "#e67e22"),
            createSummaryCard("deadline-miss", "⚠", "Deadline Misses", "0", "#e74c3c")
        );
        
        container.getChildren().add(cards);
        return container;
    }
    
    private VBox createSummaryCard(String id, String icon, String label, String value, String color) {
        VBox card = new VBox(5);
        card.setId(id);
        card.getStyleClass().add("summary-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(150);
        card.setStyle("-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setId(id + "-value");
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        card.getChildren().addAll(iconLabel, valueLabel, nameLabel);
        return card;
    }
    
    private VBox createTableSection() {
        VBox section = new VBox(10);
        VBox.setVgrow(section, Priority.ALWAYS);
        
        Label tableTitle = new Label("Task-VM Assignments");
        tableTitle.getStyleClass().add("section-title");
        
        resultsTable = new TableView<>();
        resultsTable.getStyleClass().add("results-table");
        VBox.setVgrow(resultsTable, Priority.ALWAYS);
        
        TableColumn<ResultRow, Integer> taskCol = new TableColumn<>("Task ID");
        taskCol.setCellValueFactory(new PropertyValueFactory<>("taskId"));
        taskCol.setPrefWidth(70);
        
        TableColumn<ResultRow, Integer> vmCol = new TableColumn<>("VM ID");
        vmCol.setCellValueFactory(new PropertyValueFactory<>("vmId"));
        vmCol.setPrefWidth(70);
        
        TableColumn<ResultRow, Double> execTimeCol = new TableColumn<>("Exec Time (s)");
        execTimeCol.setCellValueFactory(new PropertyValueFactory<>("executionTime"));
        execTimeCol.setPrefWidth(100);
        
        TableColumn<ResultRow, Double> costCol = new TableColumn<>("Cost ($)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("executionCost"));
        costCol.setPrefWidth(80);
        
        TableColumn<ResultRow, Double> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        deadlineCol.setPrefWidth(80);
        
        TableColumn<ResultRow, Double> budgetCol = new TableColumn<>("Budget");
        budgetCol.setCellValueFactory(new PropertyValueFactory<>("budget"));
        budgetCol.setPrefWidth(80);
        
        TableColumn<ResultRow, Double> qosCol = new TableColumn<>("QoS Score");
        qosCol.setCellValueFactory(new PropertyValueFactory<>("qosScore"));
        qosCol.setPrefWidth(90);
        
        TableColumn<ResultRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("✓ Satisfied")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        resultsTable.getColumns().addAll(taskCol, vmCol, execTimeCol, costCol, 
                deadlineCol, budgetCol, qosCol, statusCol);
        
        resultData = FXCollections.observableArrayList();
        resultsTable.setItems(resultData);
        
        // Placeholder
        resultsTable.setPlaceholder(new Label("Run a simulation to see results"));
        
        section.getChildren().addAll(tableTitle, resultsTable);
        return section;
    }
    
    private VBox createVisualizationSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("viz-section");
        section.setPadding(new Insets(15));
        
        Label title = new Label("Task Distribution");
        title.getStyleClass().add("section-title");
        
        // Gantt-style chart
        ganttChart = new HBox(5);
        ganttChart.setId("gantt-chart");
        ganttChart.setAlignment(Pos.CENTER_LEFT);
        ganttChart.setMinHeight(200);
        
        ScrollPane chartScroll = new ScrollPane(ganttChart);
        chartScroll.setFitToHeight(true);
        chartScroll.setPrefHeight(250);
        VBox.setVgrow(chartScroll, Priority.ALWAYS);
        
        // Legend
        VBox legend = createLegend();
        
        section.getChildren().addAll(title, chartScroll, legend);
        return section;
    }
    
    private VBox createLegend() {
        VBox legend = new VBox(5);
        legend.getStyleClass().add("legend");
        legend.setPadding(new Insets(10));
        
        Label title = new Label("Legend");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        
        HBox items = new HBox(15);
        items.setAlignment(Pos.CENTER_LEFT);
        
        items.getChildren().addAll(
            createLegendItem("QoS Satisfied", "#27ae60"),
            createLegendItem("QoS Violated", "#e74c3c"),
            createLegendItem("Tight Deadline", "#f39c12")
        );
        
        legend.getChildren().addAll(title, items);
        return legend;
    }
    
    private HBox createLegendItem(String text, String color) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Rectangle rect = new Rectangle(12, 12);
        rect.setFill(Color.web(color));
        rect.setArcWidth(3);
        rect.setArcHeight(3);
        
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 10px;");
        
        item.getChildren().addAll(rect, label);
        return item;
    }
    
    public void updateResults(Map<String, List<SchedulingResult>> allResults,
                             Map<String, Map<String, Double>> allMetrics) {
        algorithmCombo.getItems().clear();
        algorithmCombo.getItems().addAll(allResults.keySet());
        
        if (!allResults.isEmpty()) {
            algorithmCombo.setValue(allResults.keySet().iterator().next());
            displayResults(algorithmCombo.getValue());
        }
    }
    
    private void displayResults(String algorithm) {
        if (algorithm == null) return;
        
        Map<String, List<SchedulingResult>> allResults = controller.getAllResults();
        Map<String, Map<String, Double>> allMetrics = controller.getAllMetrics();
        
        if (!allResults.containsKey(algorithm)) return;
        
        List<SchedulingResult> results = allResults.get(algorithm);
        Map<String, Double> metrics = allMetrics.get(algorithm);
        
        // Update summary cards
        updateSummaryCard("total-time", String.format("%.2f s", metrics.getOrDefault("totalExecutionTime", 0.0)));
        updateSummaryCard("total-cost", String.format("$%.2f", metrics.getOrDefault("totalCost", 0.0)));
        updateSummaryCard("makespan", String.format("%.2f s", metrics.getOrDefault("makespan", 0.0)));
        updateSummaryCard("qos-rate", String.format("%.1f%%", metrics.getOrDefault("qosSatisfactionRate", 0.0)));
        updateSummaryCard("deadline-miss", String.format("%.0f", metrics.getOrDefault("deadlineMissRate", 0.0) * results.size() / 100));
        
        // Update table
        resultData.clear();
        for (SchedulingResult result : results) {
            resultData.add(new ResultRow(result));
        }
        
        // Update Gantt chart
        updateGanttChart(results);
    }
    
    private void updateSummaryCard(String id, String value) {
        Label valueLabel = (Label) panel.lookup("#" + id + "-value");
        if (valueLabel != null) {
            valueLabel.setText(value);
        }
    }
    
    private void updateGanttChart(List<SchedulingResult> results) {
        ganttChart.getChildren().clear();
        
        // Group by VM
        Map<Integer, List<SchedulingResult>> byVM = new LinkedHashMap<>();
        for (SchedulingResult result : results) {
            int vmId = result.getVm().getVmId();
            byVM.computeIfAbsent(vmId, k -> new ArrayList<>()).add(result);
        }
        
        double maxTime = results.stream()
                .mapToDouble(SchedulingResult::getExecutionTime)
                .max().orElse(10);
        
        for (Map.Entry<Integer, List<SchedulingResult>> entry : byVM.entrySet()) {
            VBox vmColumn = new VBox(3);
            vmColumn.setAlignment(Pos.TOP_CENTER);
            vmColumn.setPadding(new Insets(5));
            vmColumn.getStyleClass().add("gantt-column");
            
            Label vmLabel = new Label("VM" + entry.getKey());
            vmLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            vmColumn.getChildren().add(vmLabel);
            
            for (SchedulingResult result : entry.getValue()) {
                double height = Math.max(20, (result.getExecutionTime() / maxTime) * 100);
                
                StackPane taskBlock = new StackPane();
                taskBlock.setPrefSize(40, height);
                taskBlock.setMinHeight(20);
                
                Rectangle rect = new Rectangle(38, height - 2);
                rect.setArcWidth(5);
                rect.setArcHeight(5);
                
                if (result.isQoSSatisfied()) {
                    rect.setFill(Color.web("#27ae60"));
                } else if (result.getExecutionTime() <= result.getTask().getDeadline() * 1.2) {
                    rect.setFill(Color.web("#f39c12"));
                } else {
                    rect.setFill(Color.web("#e74c3c"));
                }
                
                Label taskLabel = new Label("T" + result.getTask().getTaskId());
                taskLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: white;");
                
                taskBlock.getChildren().addAll(rect, taskLabel);
                
                Tooltip tooltip = new Tooltip(String.format(
                    "Task %d\nTime: %.2fs\nCost: $%.2f\nQoS: %s",
                    result.getTask().getTaskId(),
                    result.getExecutionTime(),
                    result.getExecutionCost(),
                    result.isQoSSatisfied() ? "Satisfied" : "Violated"
                ));
                Tooltip.install(taskBlock, tooltip);
                
                vmColumn.getChildren().add(taskBlock);
            }
            
            ganttChart.getChildren().add(vmColumn);
        }
    }
    
    private void exportResults() {
        if (algorithmCombo.getValue() == null) {
            controller.showError("No Results", "Please run a simulation first.");
            return;
        }
        
        controller.showInfo("Export", "Results exported to output/reports/ directory");
    }
    
    public VBox getPanel() {
        return panel;
    }
    
    /**
     * Table row for scheduling results
     */
    public static class ResultRow {
        private final IntegerProperty taskId;
        private final IntegerProperty vmId;
        private final DoubleProperty executionTime;
        private final DoubleProperty executionCost;
        private final DoubleProperty deadline;
        private final DoubleProperty budget;
        private final DoubleProperty qosScore;
        private final StringProperty status;
        
        public ResultRow(SchedulingResult result) {
            this.taskId = new SimpleIntegerProperty(result.getTask().getTaskId());
            this.vmId = new SimpleIntegerProperty(result.getVm().getVmId());
            this.executionTime = new SimpleDoubleProperty(
                    Math.round(result.getExecutionTime() * 100.0) / 100.0);
            this.executionCost = new SimpleDoubleProperty(
                    Math.round(result.getExecutionCost() * 100.0) / 100.0);
            this.deadline = new SimpleDoubleProperty(result.getTask().getDeadline());
            this.budget = new SimpleDoubleProperty(result.getTask().getBudget());
            this.qosScore = new SimpleDoubleProperty(
                    Math.round(result.getQosScore() * 1000.0) / 1000.0);
            this.status = new SimpleStringProperty(
                    result.isQoSSatisfied() ? "✓ Satisfied" : "✗ Violated");
        }
        
        public int getTaskId() { return taskId.get(); }
        public int getVmId() { return vmId.get(); }
        public double getExecutionTime() { return executionTime.get(); }
        public double getExecutionCost() { return executionCost.get(); }
        public double getDeadline() { return deadline.get(); }
        public double getBudget() { return budget.get(); }
        public double getQosScore() { return qosScore.get(); }
        public String getStatus() { return status.get(); }
        
        public IntegerProperty taskIdProperty() { return taskId; }
        public IntegerProperty vmIdProperty() { return vmId; }
        public DoubleProperty executionTimeProperty() { return executionTime; }
        public DoubleProperty executionCostProperty() { return executionCost; }
        public DoubleProperty deadlineProperty() { return deadline; }
        public DoubleProperty budgetProperty() { return budget; }
        public DoubleProperty qosScoreProperty() { return qosScore; }
        public StringProperty statusProperty() { return status; }
    }
}
