package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.ui.SimulatorController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simulation Panel - Run and monitor simulations
 */
public class SimulationPanel {
    
    private SimulatorController controller;
    private VBox panel;
    private VBox algorithmSelection;
    private TextArea logArea;
    private ProgressBar progressBar;
    private Label progressLabel;
    private Button runButton;
    private Button stopButton;
    private boolean isRunning = false;
    
    private List<CheckBox> algorithmChecks = new ArrayList<>();
    
    public SimulationPanel(SimulatorController controller) {
        this.controller = controller;
        createPanel();
    }
    
    private void createPanel() {
        panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.getStyleClass().add("simulation-panel");
        
        // Header
        Label header = new Label("▶ Run Simulation");
        header.getStyleClass().add("panel-header");
        
        // Main content - two columns
        HBox mainContent = new HBox(30);
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Left - Configuration
        VBox leftSection = createConfigSection();
        leftSection.setPrefWidth(400);
        
        // Right - Log and Progress
        VBox rightSection = createLogSection();
        HBox.setHgrow(rightSection, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(leftSection, rightSection);
        
        panel.getChildren().addAll(header, mainContent);
    }
    
    private VBox createConfigSection() {
        VBox section = new VBox(20);
        section.getStyleClass().add("config-section");
        
        // Pre-simulation checklist
        VBox checklist = createChecklist();
        
        // Algorithm selection
        algorithmSelection = createAlgorithmSelection();
        
        // Run controls
        VBox controls = createRunControls();
        
        section.getChildren().addAll(checklist, new Separator(), algorithmSelection, new Separator(), controls);
        return section;
    }
    
    private VBox createChecklist() {
        VBox checklist = new VBox(10);
        checklist.getStyleClass().add("checklist");
        checklist.setPadding(new Insets(15));
        
        Label title = new Label("Pre-Simulation Checklist");
        title.getStyleClass().add("section-title");
        
        HBox tasksCheck = createCheckItem("tasks-check", "Tasks Configured", true);
        HBox vmsCheck = createCheckItem("vms-check", "VMs Configured", true);
        HBox settingsCheck = createCheckItem("settings-check", "Settings Applied", true);
        
        // Update checks based on actual data
        Platform.runLater(() -> updateChecklist());
        
        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.getStyleClass().add("btn-small");
        refreshBtn.setOnAction(e -> updateChecklist());
        
        checklist.getChildren().addAll(title, tasksCheck, vmsCheck, settingsCheck, refreshBtn);
        return checklist;
    }
    
    private HBox createCheckItem(String id, String text, boolean checked) {
        HBox item = new HBox(10);
        item.setId(id);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Circle indicator = new Circle(6);
        indicator.setFill(checked ? Color.web("#27ae60") : Color.web("#e74c3c"));
        indicator.setId(id + "-indicator");
        
        Label label = new Label(text);
        label.setId(id + "-label");
        
        Label status = new Label(checked ? "✓ Ready" : "✗ Not Ready");
        status.setId(id + "-status");
        status.setStyle(checked ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        item.getChildren().addAll(indicator, label, spacer, status);
        return item;
    }
    
    private void updateChecklist() {
        updateCheckItem("tasks-check", controller.getTasks().size() > 0, 
            controller.getTasks().size() + " tasks");
        updateCheckItem("vms-check", controller.getVms().size() > 0,
            controller.getVms().size() + " VMs");
        updateCheckItem("settings-check", controller.getConfig() != null, "Applied");
    }
    
    private void updateCheckItem(String id, boolean ready, String detail) {
        HBox item = (HBox) panel.lookup("#" + id);
        if (item == null) return;
        
        Circle indicator = (Circle) item.lookup("#" + id + "-indicator");
        Label status = (Label) item.lookup("#" + id + "-status");
        
        if (indicator != null) {
            indicator.setFill(ready ? Color.web("#27ae60") : Color.web("#e74c3c"));
        }
        if (status != null) {
            status.setText(ready ? "✓ " + detail : "✗ Not Ready");
            status.setStyle(ready ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
        }
    }
    
    private VBox createAlgorithmSelection() {
        VBox selection = new VBox(12);
        selection.getStyleClass().add("algorithm-selection");
        selection.setPadding(new Insets(15));
        
        Label title = new Label("Select Algorithms to Compare");
        title.getStyleClass().add("section-title");
        
        String[][] algorithms = {
            {"QoS-Aware", "Weighted multi-objective optimization", "#27ae60"},
            {"FCFS", "First Come First Served baseline", "#3498db"},
            {"Random", "Random assignment baseline", "#e67e22"},
            {"Min-Min", "Min-Min heuristic algorithm", "#9b59b6"}
        };
        
        for (String[] algo : algorithms) {
            HBox algoBox = new HBox(10);
            algoBox.setAlignment(Pos.CENTER_LEFT);
            algoBox.getStyleClass().add("algo-item");
            algoBox.setPadding(new Insets(8));
            
            CheckBox check = new CheckBox();
            check.setSelected(true);
            check.setUserData(algo[0]);
            algorithmChecks.add(check);
            
            Circle dot = new Circle(5);
            dot.setFill(Color.web(algo[2]));
            
            VBox textBox = new VBox(2);
            Label nameLabel = new Label(algo[0]);
            nameLabel.setStyle("-fx-font-weight: bold;");
            Label descLabel = new Label(algo[1]);
            descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
            textBox.getChildren().addAll(nameLabel, descLabel);
            
            algoBox.getChildren().addAll(check, dot, textBox);
            selection.getChildren().add(algoBox);
        }
        
        // Select all / none buttons
        HBox selectButtons = new HBox(10);
        selectButtons.setAlignment(Pos.CENTER);
        
        Button selectAllBtn = new Button("Select All");
        selectAllBtn.getStyleClass().add("btn-small");
        selectAllBtn.setOnAction(e -> algorithmChecks.forEach(cb -> cb.setSelected(true)));
        
        Button selectNoneBtn = new Button("Select None");
        selectNoneBtn.getStyleClass().add("btn-small");
        selectNoneBtn.setOnAction(e -> algorithmChecks.forEach(cb -> cb.setSelected(false)));
        
        selectButtons.getChildren().addAll(selectAllBtn, selectNoneBtn);
        
        selection.getChildren().addAll(title);
        selection.getChildren().add(selectButtons);
        
        return selection;
    }
    
    private VBox createRunControls() {
        VBox controls = new VBox(15);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(20));
        controls.getStyleClass().add("run-controls");
        
        // Progress
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setVisible(false);
        
        progressLabel = new Label("");
        progressLabel.getStyleClass().add("progress-label");
        
        // Buttons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);
        
        runButton = new Button("▶ Run Simulation");
        runButton.getStyleClass().addAll("btn", "btn-success", "btn-large");
        runButton.setPrefWidth(180);
        runButton.setOnAction(e -> runSimulation());
        
        stopButton = new Button("⏹ Stop");
        stopButton.getStyleClass().addAll("btn", "btn-danger", "btn-large");
        stopButton.setPrefWidth(100);
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopSimulation());
        
        buttons.getChildren().addAll(runButton, stopButton);
        
        controls.getChildren().addAll(progressBar, progressLabel, buttons);
        return controls;
    }
    
    private VBox createLogSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("log-section");
        VBox.setVgrow(section, Priority.ALWAYS);
        
        HBox logHeader = new HBox(10);
        logHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Simulation Log");
        title.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button clearLogBtn = new Button("Clear");
        clearLogBtn.getStyleClass().add("btn-small");
        clearLogBtn.setOnAction(e -> logArea.clear());
        
        logHeader.getChildren().addAll(title, spacer, clearLogBtn);
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("log-area");
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        // Initial log message
        appendLog("═══════════════════════════════════════════════════");
        appendLog("  QoS-Aware Task Scheduling Simulator Ready");
        appendLog("═══════════════════════════════════════════════════");
        appendLog("");
        appendLog("Configure your simulation and click 'Run Simulation' to begin.");
        appendLog("");
        
        section.getChildren().addAll(logHeader, logArea);
        return section;
    }
    
    private void appendLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    private void runSimulation() {
        // Validate
        List<String> selectedAlgorithms = getSelectedAlgorithms();
        if (selectedAlgorithms.isEmpty()) {
            controller.showError("No Algorithm Selected", "Please select at least one algorithm to run.");
            return;
        }
        
        if (controller.getTasks().isEmpty()) {
            controller.showError("No Tasks", "Please add tasks before running simulation.");
            return;
        }
        
        if (controller.getVms().isEmpty()) {
            controller.showError("No VMs", "Please add VMs before running simulation.");
            return;
        }
        
        // Update UI
        isRunning = true;
        runButton.setDisable(true);
        stopButton.setDisable(false);
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        
        // Log start
        appendLog("");
        appendLog("═══════════════════════════════════════════════════");
        appendLog("  SIMULATION STARTED - " + java.time.LocalDateTime.now());
        appendLog("═══════════════════════════════════════════════════");
        appendLog("");
        appendLog("Configuration:");
        appendLog("  • Tasks: " + controller.getTasks().size());
        appendLog("  • VMs: " + controller.getVms().size());
        appendLog("  • Alpha (Time Weight): " + String.format("%.2f", controller.getConfig().getAlpha()));
        appendLog("  • Beta (Cost Weight): " + String.format("%.2f", controller.getConfig().getBeta()));
        appendLog("");
        appendLog("Algorithms to compare: " + String.join(", ", selectedAlgorithms));
        appendLog("");
        
        // Run simulation
        controller.runSimulation(selectedAlgorithms, () -> {
            isRunning = false;
            runButton.setDisable(false);
            stopButton.setDisable(true);
            progressBar.setProgress(1.0);
            
            // Add to history for each algorithm
            Map<String, Map<String, Double>> allMetrics = controller.getAllMetrics();
            for (String algorithm : selectedAlgorithms) {
                Map<String, Double> metrics = allMetrics.get(algorithm);
                if (metrics != null) {
                    controller.addHistoryEntry(
                        algorithm,
                        controller.getConfig().getAlpha(),
                        controller.getConfig().getBeta(),
                        metrics
                    );
                }
            }
            
            appendLog("");
            appendLog("═══════════════════════════════════════════════════");
            appendLog("  SIMULATION COMPLETED SUCCESSFULLY");
            appendLog("═══════════════════════════════════════════════════");
            appendLog("");
            appendLog("Results are ready! Navigate to:");
            appendLog("  • Results - View detailed scheduling results");
            appendLog("  • Charts - Visualize performance metrics");
            appendLog("  • Comparison - Compare algorithm performance");
            appendLog("");
            
            // Show navigation prompt
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Simulation Complete");
                alert.setHeaderText("Simulation completed successfully!");
                alert.setContentText("Would you like to view the results?");
                
                ButtonType viewResults = new ButtonType("View Results");
                ButtonType viewCharts = new ButtonType("View Charts");
                ButtonType stayHere = new ButtonType("Stay Here", ButtonBar.ButtonData.CANCEL_CLOSE);
                
                alert.getButtonTypes().setAll(viewResults, viewCharts, stayHere);
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == viewResults) {
                        controller.showPanel("results");
                    } else if (response == viewCharts) {
                        controller.showPanel("charts");
                    }
                });
            });
        });
    }
    
    private void stopSimulation() {
        isRunning = false;
        runButton.setDisable(false);
        stopButton.setDisable(true);
        progressBar.setProgress(0);
        progressBar.setVisible(false);
        
        appendLog("");
        appendLog("⚠ Simulation stopped by user");
        appendLog("");
    }
    
    private List<String> getSelectedAlgorithms() {
        List<String> selected = new ArrayList<>();
        for (CheckBox cb : algorithmChecks) {
            if (cb.isSelected()) {
                selected.add((String) cb.getUserData());
            }
        }
        return selected;
    }
    
    public VBox getPanel() {
        updateChecklist();
        return panel;
    }
}
