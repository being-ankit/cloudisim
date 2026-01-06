package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.config.SimulationConfig;
import com.cloudsim.qos.ui.SimulatorController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Configuration Panel - Settings for simulation parameters
 */
public class ConfigurationPanel {
    
    private SimulatorController controller;
    private ScrollPane panel;
    
    // Input controls
    private Slider alphaSlider;
    private Slider betaSlider;
    private TextField maxDeadlineField;
    private TextField maxBudgetField;
    private CheckBox enableDeadlineCheck;
    private CheckBox enableBudgetCheck;
    private ComboBox<String> defaultAlgorithmCombo;
    private Spinner<Integer> randomSeedSpinner;
    
    public ConfigurationPanel(SimulatorController controller) {
        this.controller = controller;
        createPanel();
    }
    
    private void createPanel() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("config-panel");
        
        // Header
        Label header = new Label("⚙ Simulation Settings");
        header.getStyleClass().add("panel-header");
        
        Label description = new Label(
            "Configure the parameters for QoS-aware task scheduling simulation. " +
            "These settings affect how tasks are assigned to virtual machines."
        );
        description.getStyleClass().add("panel-description");
        description.setWrapText(true);
        
        // QoS Weight Section
        VBox qosSection = createQoSWeightSection();
        
        // Constraints Section
        VBox constraintsSection = createConstraintsSection();
        
        // Algorithm Section
        VBox algorithmSection = createAlgorithmSection();
        
        // Advanced Section
        VBox advancedSection = createAdvancedSection();
        
        // Action buttons
        HBox actions = createActionButtons();
        
        content.getChildren().addAll(
            header, description,
            new Separator(),
            qosSection,
            new Separator(),
            constraintsSection,
            new Separator(),
            algorithmSection,
            new Separator(),
            advancedSection,
            new Separator(),
            actions
        );
        
        panel = new ScrollPane(content);
        panel.setFitToWidth(true);
        panel.getStyleClass().add("config-scroll");
    }
    
    private VBox createQoSWeightSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("config-section");
        
        Label title = new Label("QoS Weight Configuration");
        title.getStyleClass().add("section-title");
        
        Label info = new Label(
            "The QoS score is calculated as: QoS = α × Time + β × Cost\n" +
            "Adjust weights to prioritize execution time vs. cost optimization."
        );
        info.getStyleClass().add("section-info");
        info.setWrapText(true);
        
        // Alpha slider
        VBox alphaBox = new VBox(5);
        HBox alphaHeader = new HBox(10);
        Label alphaLabel = new Label("α (Time Weight):");
        alphaLabel.setPrefWidth(150);
        Label alphaValue = new Label("0.50");
        alphaValue.getStyleClass().add("slider-value");
        alphaHeader.getChildren().addAll(alphaLabel, alphaValue);
        
        alphaSlider = new Slider(0, 1, 0.5);
        alphaSlider.setShowTickMarks(true);
        alphaSlider.setShowTickLabels(true);
        alphaSlider.setMajorTickUnit(0.25);
        alphaSlider.valueProperty().addListener((obs, old, val) -> {
            alphaValue.setText(String.format("%.2f", val.doubleValue()));
            // Auto-adjust beta
            betaSlider.setValue(1 - val.doubleValue());
        });
        alphaBox.getChildren().addAll(alphaHeader, alphaSlider);
        
        // Beta slider
        VBox betaBox = new VBox(5);
        HBox betaHeader = new HBox(10);
        Label betaLabel = new Label("β (Cost Weight):");
        betaLabel.setPrefWidth(150);
        Label betaValue = new Label("0.50");
        betaValue.getStyleClass().add("slider-value");
        betaHeader.getChildren().addAll(betaLabel, betaValue);
        
        betaSlider = new Slider(0, 1, 0.5);
        betaSlider.setShowTickMarks(true);
        betaSlider.setShowTickLabels(true);
        betaSlider.setMajorTickUnit(0.25);
        betaSlider.valueProperty().addListener((obs, old, val) -> {
            betaValue.setText(String.format("%.2f", val.doubleValue()));
        });
        betaBox.getChildren().addAll(betaHeader, betaSlider);
        
        // Visual weight indicator
        HBox weightIndicator = createWeightIndicator();
        
        section.getChildren().addAll(title, info, alphaBox, betaBox, weightIndicator);
        return section;
    }
    
    private HBox createWeightIndicator() {
        HBox indicator = new HBox();
        indicator.setAlignment(Pos.CENTER);
        indicator.setPadding(new Insets(10));
        
        ProgressBar timeBar = new ProgressBar(0.5);
        timeBar.setPrefWidth(200);
        timeBar.getStyleClass().add("time-bar");
        
        Label vsLabel = new Label(" vs ");
        
        ProgressBar costBar = new ProgressBar(0.5);
        costBar.setPrefWidth(200);
        costBar.getStyleClass().add("cost-bar");
        
        alphaSlider.valueProperty().addListener((obs, old, val) -> {
            timeBar.setProgress(val.doubleValue());
            costBar.setProgress(1 - val.doubleValue());
        });
        
        VBox timeBox = new VBox(5);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.getChildren().addAll(new Label("⏱ Time Priority"), timeBar);
        
        VBox costBox = new VBox(5);
        costBox.setAlignment(Pos.CENTER);
        costBox.getChildren().addAll(new Label("💰 Cost Priority"), costBar);
        
        indicator.getChildren().addAll(timeBox, vsLabel, costBox);
        return indicator;
    }
    
    private VBox createConstraintsSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("config-section");
        
        Label title = new Label("Constraint Settings");
        title.getStyleClass().add("section-title");
        
        // Deadline constraint
        HBox deadlineBox = new HBox(15);
        deadlineBox.setAlignment(Pos.CENTER_LEFT);
        
        enableDeadlineCheck = new CheckBox("Enable Deadline Constraints");
        enableDeadlineCheck.setSelected(true);
        
        Label maxDeadlineLabel = new Label("Max Deadline (s):");
        maxDeadlineField = new TextField("100");
        maxDeadlineField.setPrefWidth(100);
        maxDeadlineField.disableProperty().bind(enableDeadlineCheck.selectedProperty().not());
        
        deadlineBox.getChildren().addAll(enableDeadlineCheck, maxDeadlineLabel, maxDeadlineField);
        
        // Budget constraint
        HBox budgetBox = new HBox(15);
        budgetBox.setAlignment(Pos.CENTER_LEFT);
        
        enableBudgetCheck = new CheckBox("Enable Budget Constraints");
        enableBudgetCheck.setSelected(true);
        
        Label maxBudgetLabel = new Label("Max Budget ($):");
        maxBudgetField = new TextField("50");
        maxBudgetField.setPrefWidth(100);
        maxBudgetField.disableProperty().bind(enableBudgetCheck.selectedProperty().not());
        
        budgetBox.getChildren().addAll(enableBudgetCheck, maxBudgetLabel, maxBudgetField);
        
        section.getChildren().addAll(title, deadlineBox, budgetBox);
        return section;
    }
    
    private VBox createAlgorithmSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("config-section");
        
        Label title = new Label("Algorithm Selection");
        title.getStyleClass().add("section-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        
        Label defaultLabel = new Label("Default Algorithm:");
        defaultAlgorithmCombo = new ComboBox<>();
        defaultAlgorithmCombo.getItems().addAll("QoS-Aware", "FCFS", "Random", "Min-Min");
        defaultAlgorithmCombo.setValue("QoS-Aware");
        defaultAlgorithmCombo.setPrefWidth(150);
        
        grid.add(defaultLabel, 0, 0);
        grid.add(defaultAlgorithmCombo, 1, 0);
        
        // Algorithm descriptions
        VBox descriptions = new VBox(10);
        descriptions.setPadding(new Insets(10));
        descriptions.getStyleClass().add("algorithm-descriptions");
        
        descriptions.getChildren().addAll(
            createAlgorithmDesc("QoS-Aware", "Optimizes based on weighted time/cost with constraint handling", "#27ae60"),
            createAlgorithmDesc("FCFS", "First Come First Served - Simple round-robin assignment", "#3498db"),
            createAlgorithmDesc("Random", "Random task-to-VM assignment for baseline comparison", "#e67e22"),
            createAlgorithmDesc("Min-Min", "Assigns smallest tasks first to fastest VMs", "#9b59b6")
        );
        
        section.getChildren().addAll(title, grid, descriptions);
        return section;
    }
    
    private HBox createAlgorithmDesc(String name, String desc, String color) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + color + ";");
        
        Label nameLabel = new Label(name + ":");
        nameLabel.setStyle("-fx-font-weight: bold;");
        nameLabel.setPrefWidth(80);
        
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        box.getChildren().addAll(dot, nameLabel, descLabel);
        return box;
    }
    
    private VBox createAdvancedSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("config-section");
        
        TitledPane advanced = new TitledPane();
        advanced.setText("Advanced Settings");
        advanced.setExpanded(false);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        Label seedLabel = new Label("Random Seed:");
        randomSeedSpinner = new Spinner<>(0, 999999, 12345);
        randomSeedSpinner.setEditable(true);
        randomSeedSpinner.setPrefWidth(120);
        
        grid.add(seedLabel, 0, 0);
        grid.add(randomSeedSpinner, 1, 0);
        
        advanced.setContent(grid);
        section.getChildren().add(advanced);
        return section;
    }
    
    private HBox createActionButtons() {
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(20, 0, 0, 0));
        
        Button applyBtn = new Button("✓ Apply Settings");
        applyBtn.getStyleClass().addAll("btn", "btn-primary");
        applyBtn.setOnAction(e -> applySettings());
        
        Button resetBtn = new Button("↺ Reset to Defaults");
        resetBtn.getStyleClass().addAll("btn", "btn-secondary");
        resetBtn.setOnAction(e -> resetToDefaults());
        
        Button nextBtn = new Button("Next: Configure Tasks →");
        nextBtn.getStyleClass().addAll("btn", "btn-success");
        nextBtn.setOnAction(e -> controller.showPanel("tasks"));
        
        actions.getChildren().addAll(applyBtn, resetBtn, nextBtn);
        return actions;
    }
    
    private void applySettings() {
        try {
            double alpha = alphaSlider.getValue();
            double beta = betaSlider.getValue();
            
            SimulationConfig config = new SimulationConfig(alpha, beta);
            config.setEnableDeadlineConstraint(enableDeadlineCheck.isSelected());
            config.setEnableBudgetConstraint(enableBudgetCheck.isSelected());
            
            if (enableDeadlineCheck.isSelected()) {
                config.setMaxDeadline(Double.parseDouble(maxDeadlineField.getText()));
            }
            if (enableBudgetCheck.isSelected()) {
                config.setMaxBudget(Double.parseDouble(maxBudgetField.getText()));
            }
            
            controller.setConfig(config);
            controller.getStatusBar().setStatus("Settings applied successfully!");
            
        } catch (NumberFormatException e) {
            controller.showError("Invalid Input", "Please enter valid numeric values.");
        }
    }
    
    private void resetToDefaults() {
        alphaSlider.setValue(0.5);
        betaSlider.setValue(0.5);
        maxDeadlineField.setText("100");
        maxBudgetField.setText("50");
        enableDeadlineCheck.setSelected(true);
        enableBudgetCheck.setSelected(true);
        defaultAlgorithmCombo.setValue("QoS-Aware");
        randomSeedSpinner.getValueFactory().setValue(12345);
        
        controller.getStatusBar().setStatus("Settings reset to defaults");
    }
    
    public ScrollPane getPanel() {
        return panel;
    }
}
