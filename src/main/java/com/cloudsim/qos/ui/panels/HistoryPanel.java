package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.ui.SimulatorController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * History Panel - View and compare past simulation runs
 */
public class HistoryPanel {
    
    private SimulatorController controller;
    private VBox panel;
    private TableView<HistoryEntry> historyTable;
    private ObservableList<HistoryEntry> historyData = FXCollections.observableArrayList();
    private VBox detailsPane;
    
    private static int runCounter = 0;
    
    public HistoryPanel(SimulatorController controller) {
        this.controller = controller;
        createPanel();
    }
    
    private void createPanel() {
        panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.getStyleClass().add("history-panel");
        
        // Header
        Label header = new Label("📜 Simulation History");
        header.getStyleClass().add("panel-header");
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        // Main content - split view
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.6);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Left - History table
        VBox tableContainer = createTableSection();
        
        // Right - Details pane
        detailsPane = createDetailsPane();
        
        splitPane.getItems().addAll(tableContainer, detailsPane);
        
        panel.getChildren().addAll(header, toolbar, splitPane);
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 8;");
        
        // Search
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search history...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterHistory(newVal));
        
        // Filter by algorithm
        ComboBox<String> algorithmFilter = new ComboBox<>();
        algorithmFilter.getItems().addAll("All Algorithms", "QoS-Aware", "FCFS", "Random", "Min-Min");
        algorithmFilter.setValue("All Algorithms");
        algorithmFilter.setOnAction(e -> filterByAlgorithm(algorithmFilter.getValue()));
        
        // Date range
        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("From date");
        fromDate.setPrefWidth(130);
        
        DatePicker toDate = new DatePicker();
        toDate.setPromptText("To date");
        toDate.setPrefWidth(130);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Actions
        Button compareBtn = new Button("Compare Selected");
        compareBtn.getStyleClass().add("btn-primary");
        compareBtn.setOnAction(e -> compareSelected());
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> deleteSelected());
        
        Button clearBtn = new Button("Clear All");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> clearHistory());
        
        toolbar.getChildren().addAll(
            searchField, algorithmFilter, 
            new Label("Date:"), fromDate, new Label("to"), toDate,
            spacer, compareBtn, deleteBtn, clearBtn
        );
        
        return toolbar;
    }
    
    private VBox createTableSection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        
        historyTable = new TableView<>();
        historyTable.setItems(historyData);
        historyTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        historyTable.setPlaceholder(new Label("No simulation history yet.\nRun a simulation to see results here."));
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        
        // Columns
        TableColumn<HistoryEntry, Integer> idCol = new TableColumn<>("#");
        idCol.setCellValueFactory(new PropertyValueFactory<>("runId"));
        idCol.setPrefWidth(50);
        
        TableColumn<HistoryEntry, String> dateCol = new TableColumn<>("Date/Time");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        dateCol.setPrefWidth(150);
        
        TableColumn<HistoryEntry, String> algoCol = new TableColumn<>("Algorithm");
        algoCol.setCellValueFactory(new PropertyValueFactory<>("algorithm"));
        algoCol.setPrefWidth(100);
        
        TableColumn<HistoryEntry, Integer> tasksCol = new TableColumn<>("Tasks");
        tasksCol.setCellValueFactory(new PropertyValueFactory<>("taskCount"));
        tasksCol.setPrefWidth(60);
        
        TableColumn<HistoryEntry, Integer> vmsCol = new TableColumn<>("VMs");
        vmsCol.setCellValueFactory(new PropertyValueFactory<>("vmCount"));
        vmsCol.setPrefWidth(50);
        
        TableColumn<HistoryEntry, Double> makespanCol = new TableColumn<>("Makespan");
        makespanCol.setCellValueFactory(new PropertyValueFactory<>("makespan"));
        makespanCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%.2f", item));
            }
        });
        makespanCol.setPrefWidth(80);
        
        TableColumn<HistoryEntry, Double> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        costCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });
        costCol.setPrefWidth(80);
        
        TableColumn<HistoryEntry, Double> qosCol = new TableColumn<>("QoS Score");
        qosCol.setCellValueFactory(new PropertyValueFactory<>("qosScore"));
        qosCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", item * 100));
                    if (item >= 0.8) {
                        setStyle("-fx-text-fill: #27ae60;");
                    } else if (item >= 0.5) {
                        setStyle("-fx-text-fill: #f39c12;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c;");
                    }
                }
            }
        });
        qosCol.setPrefWidth(80);
        
        TableColumn<HistoryEntry, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(item.equals("Success") 
                        ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;"
                        : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;");
                    setGraphic(badge);
                }
            }
        });
        statusCol.setPrefWidth(80);
        
        historyTable.getColumns().addAll(idCol, dateCol, algoCol, tasksCol, vmsCol, makespanCol, costCol, qosCol, statusCol);
        
        // Selection listener
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDetailsPane(newVal);
            }
        });
        
        // Stats bar
        HBox statsBar = new HBox(20);
        statsBar.setAlignment(Pos.CENTER_LEFT);
        statsBar.setPadding(new Insets(10));
        statsBar.setStyle("-fx-background-color: #34495e; -fx-background-radius: 5;");
        
        Label totalRuns = new Label("Total Runs: 0");
        Label avgMakespan = new Label("Avg Makespan: -");
        Label avgCost = new Label("Avg Cost: -");
        Label bestAlgo = new Label("Best Performer: -");
        
        statsBar.getChildren().addAll(totalRuns, avgMakespan, avgCost, bestAlgo);
        
        container.getChildren().addAll(historyTable, statsBar);
        return container;
    }
    
    private VBox createDetailsPane() {
        VBox details = new VBox(15);
        details.setPadding(new Insets(15));
        details.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 8;");
        
        Label title = new Label("📋 Run Details");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label placeholder = new Label("Select a run to view details");
        placeholder.setStyle("-fx-text-fill: #7f8c8d;");
        
        details.getChildren().addAll(title, new Separator(), placeholder);
        return details;
    }
    
    private void updateDetailsPane(HistoryEntry entry) {
        detailsPane.getChildren().clear();
        
        Label title = new Label("📋 Run #" + entry.getRunId() + " Details");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        detailsPane.getChildren().addAll(title, new Separator());
        
        // Basic info
        VBox basicInfo = new VBox(8);
        basicInfo.getChildren().addAll(
            createDetailRow("Timestamp:", entry.getTimestamp()),
            createDetailRow("Algorithm:", entry.getAlgorithm()),
            createDetailRow("Status:", entry.getStatus()),
            new Separator()
        );
        
        // Configuration
        Label configTitle = new Label("Configuration");
        configTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        VBox configInfo = new VBox(8);
        configInfo.getChildren().addAll(
            createDetailRow("Tasks:", String.valueOf(entry.getTaskCount())),
            createDetailRow("VMs:", String.valueOf(entry.getVmCount())),
            createDetailRow("Alpha (α):", String.format("%.2f", entry.getAlpha())),
            createDetailRow("Beta (β):", String.format("%.2f", entry.getBeta())),
            new Separator()
        );
        
        // Results
        Label resultsTitle = new Label("Results");
        resultsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        VBox resultsInfo = new VBox(8);
        resultsInfo.getChildren().addAll(
            createDetailRow("Makespan:", String.format("%.2f ms", entry.getMakespan())),
            createDetailRow("Total Cost:", String.format("$%.2f", entry.getTotalCost())),
            createDetailRow("QoS Score:", String.format("%.1f%%", entry.getQosScore() * 100)),
            createDetailRow("Avg Response:", String.format("%.2f ms", entry.getAvgResponseTime())),
            createDetailRow("Throughput:", String.format("%.2f tasks/s", entry.getThroughput())),
            new Separator()
        );
        
        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10, 0, 0, 0));
        
        Button loadBtn = new Button("Load Config");
        loadBtn.getStyleClass().add("btn-primary");
        loadBtn.setOnAction(e -> loadConfiguration(entry));
        
        Button exportBtn = new Button("Export");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> exportEntry(entry));
        
        actions.getChildren().addAll(loadBtn, exportBtn);
        
        detailsPane.getChildren().addAll(
            basicInfo, configTitle, configInfo, resultsTitle, resultsInfo, actions
        );
    }
    
    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-text-fill: #7f8c8d;");
        labelNode.setMinWidth(100);
        
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #ecf0f1;");
        
        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }
    
    public void addHistoryEntry(String algorithm, int taskCount, int vmCount, 
                                double alpha, double beta,
                                List<SchedulingResult> results,
                                Map<String, Double> metrics) {
        runCounter++;
        
        double makespan = metrics.getOrDefault("makespan", 0.0);
        double totalCost = metrics.getOrDefault("totalCost", 0.0);
        double qosScore = metrics.getOrDefault("qosScore", 0.0);
        double avgResponse = metrics.getOrDefault("avgResponseTime", 0.0);
        double throughput = metrics.getOrDefault("throughput", 0.0);
        
        HistoryEntry entry = new HistoryEntry(
            runCounter,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            algorithm,
            taskCount,
            vmCount,
            alpha,
            beta,
            makespan,
            totalCost,
            qosScore,
            avgResponse,
            throughput,
            "Success"
        );
        
        historyData.add(0, entry); // Add to beginning
        
        // Keep only last 100 entries
        if (historyData.size() > 100) {
            historyData.remove(historyData.size() - 1);
        }
    }
    
    private void filterHistory(String searchText) {
        // Implement filtering logic
    }
    
    private void filterByAlgorithm(String algorithm) {
        // Implement algorithm filtering
    }
    
    private void compareSelected() {
        List<HistoryEntry> selected = historyTable.getSelectionModel().getSelectedItems();
        if (selected.size() < 2) {
            controller.showError("Selection Required", "Please select at least 2 runs to compare.");
            return;
        }
        
        // Show comparison dialog
        showComparisonDialog(selected);
    }
    
    private void showComparisonDialog(List<HistoryEntry> entries) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Run Comparison");
        dialog.setHeaderText("Comparing " + entries.size() + " simulation runs");
        
        // Create comparison table
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Headers
        grid.add(new Label("Metric"), 0, 0);
        for (int i = 0; i < entries.size(); i++) {
            Label header = new Label("Run #" + entries.get(i).getRunId());
            header.setStyle("-fx-font-weight: bold;");
            grid.add(header, i + 1, 0);
        }
        
        // Metrics
        String[] metrics = {"Algorithm", "Makespan", "Total Cost", "QoS Score", "Tasks", "VMs"};
        for (int row = 0; row < metrics.length; row++) {
            grid.add(new Label(metrics[row]), 0, row + 1);
            
            for (int col = 0; col < entries.size(); col++) {
                HistoryEntry e = entries.get(col);
                String value;
                switch (row) {
                    case 0:
                        value = e.getAlgorithm();
                        break;
                    case 1:
                        value = String.format("%.2f", e.getMakespan());
                        break;
                    case 2:
                        value = String.format("$%.2f", e.getTotalCost());
                        break;
                    case 3:
                        value = String.format("%.1f%%", e.getQosScore() * 100);
                        break;
                    case 4:
                        value = String.valueOf(e.getTaskCount());
                        break;
                    case 5:
                        value = String.valueOf(e.getVmCount());
                        break;
                    default:
                        value = "";
                        break;
                }
                grid.add(new Label(value), col + 1, row + 1);
            }
        }
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void deleteSelected() {
        List<HistoryEntry> selected = new ArrayList<>(historyTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            controller.showError("No Selection", "Please select runs to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + selected.size() + " run(s)?");
        confirm.setContentText("This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                historyData.removeAll(selected);
            }
        });
    }
    
    private void clearHistory() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear History");
        confirm.setHeaderText("Clear all simulation history?");
        confirm.setContentText("This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                historyData.clear();
                runCounter = 0;
            }
        });
    }
    
    private void loadConfiguration(HistoryEntry entry) {
        controller.showInfo("Load Configuration", 
            "Configuration from Run #" + entry.getRunId() + " loaded successfully.");
    }
    
    private void exportEntry(HistoryEntry entry) {
        controller.showInfo("Export", 
            "Run #" + entry.getRunId() + " exported successfully.");
    }
    
    public VBox getPanel() {
        return panel;
    }
    
    /**
     * History entry data class
     */
    public static class HistoryEntry {
        private final IntegerProperty runId;
        private final StringProperty timestamp;
        private final StringProperty algorithm;
        private final IntegerProperty taskCount;
        private final IntegerProperty vmCount;
        private final DoubleProperty alpha;
        private final DoubleProperty beta;
        private final DoubleProperty makespan;
        private final DoubleProperty totalCost;
        private final DoubleProperty qosScore;
        private final DoubleProperty avgResponseTime;
        private final DoubleProperty throughput;
        private final StringProperty status;
        
        public HistoryEntry(int runId, String timestamp, String algorithm, 
                          int taskCount, int vmCount, double alpha, double beta,
                          double makespan, double totalCost, double qosScore,
                          double avgResponseTime, double throughput, String status) {
            this.runId = new SimpleIntegerProperty(runId);
            this.timestamp = new SimpleStringProperty(timestamp);
            this.algorithm = new SimpleStringProperty(algorithm);
            this.taskCount = new SimpleIntegerProperty(taskCount);
            this.vmCount = new SimpleIntegerProperty(vmCount);
            this.alpha = new SimpleDoubleProperty(alpha);
            this.beta = new SimpleDoubleProperty(beta);
            this.makespan = new SimpleDoubleProperty(makespan);
            this.totalCost = new SimpleDoubleProperty(totalCost);
            this.qosScore = new SimpleDoubleProperty(qosScore);
            this.avgResponseTime = new SimpleDoubleProperty(avgResponseTime);
            this.throughput = new SimpleDoubleProperty(throughput);
            this.status = new SimpleStringProperty(status);
        }
        
        // Getters
        public int getRunId() { return runId.get(); }
        public String getTimestamp() { return timestamp.get(); }
        public String getAlgorithm() { return algorithm.get(); }
        public int getTaskCount() { return taskCount.get(); }
        public int getVmCount() { return vmCount.get(); }
        public double getAlpha() { return alpha.get(); }
        public double getBeta() { return beta.get(); }
        public double getMakespan() { return makespan.get(); }
        public double getTotalCost() { return totalCost.get(); }
        public double getQosScore() { return qosScore.get(); }
        public double getAvgResponseTime() { return avgResponseTime.get(); }
        public double getThroughput() { return throughput.get(); }
        public String getStatus() { return status.get(); }
        
        // Property getters
        public IntegerProperty runIdProperty() { return runId; }
        public StringProperty timestampProperty() { return timestamp; }
        public StringProperty algorithmProperty() { return algorithm; }
        public IntegerProperty taskCountProperty() { return taskCount; }
        public IntegerProperty vmCountProperty() { return vmCount; }
        public DoubleProperty makespanProperty() { return makespan; }
        public DoubleProperty totalCostProperty() { return totalCost; }
        public DoubleProperty qosScoreProperty() { return qosScore; }
        public StringProperty statusProperty() { return status; }
    }
}
