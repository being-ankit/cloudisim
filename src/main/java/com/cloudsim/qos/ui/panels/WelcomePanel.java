package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.ui.SimulatorController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Welcome Panel - Landing page with quick actions
 */
public class WelcomePanel {
    
    private SimulatorController controller;
    private VBox panel;
    
    public WelcomePanel(SimulatorController controller) {
        this.controller = controller;
        createPanel();
    }
    
    private void createPanel() {
        panel = new VBox(30);
        panel.getStyleClass().add("welcome-panel");
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(40));
        
        // Hero section
        VBox hero = createHeroSection();
        
        // Quick actions
        HBox quickActions = createQuickActions();
        
        // Features grid
        GridPane features = createFeaturesGrid();
        
        // Getting started steps
        VBox gettingStarted = createGettingStarted();
        
        panel.getChildren().addAll(hero, quickActions, features, gettingStarted);
    }
    
    private VBox createHeroSection() {
        VBox hero = new VBox(15);
        hero.setAlignment(Pos.CENTER);
        
        Label cloudIcon = new Label("☁");
        cloudIcon.setStyle("-fx-font-size: 72px; -fx-text-fill: #3498db;");
        
        Label title = new Label("QoS-Aware Task Scheduler");
        title.getStyleClass().add("welcome-title");
        
        Label subtitle = new Label("Multi-Objective Cloud Computing Simulation Platform");
        subtitle.getStyleClass().add("welcome-subtitle");
        
        Label description = new Label(
            "Design, simulate, and analyze task scheduling algorithms with " +
            "Quality of Service constraints including deadlines and budgets."
        );
        description.getStyleClass().add("welcome-description");
        description.setWrapText(true);
        description.setMaxWidth(600);
        description.setAlignment(Pos.CENTER);
        
        hero.getChildren().addAll(cloudIcon, title, subtitle, description);
        return hero;
    }
    
    private HBox createQuickActions() {
        HBox actions = new HBox(20);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(20, 0, 20, 0));
        
        Button newSimBtn = createActionButton("🚀", "New Simulation", 
            "Start a new simulation with default settings", "#27ae60");
        newSimBtn.setOnAction(e -> controller.showPanel("simulation"));
        
        Button configBtn = createActionButton("⚙", "Configure", 
            "Customize tasks, VMs, and parameters", "#3498db");
        configBtn.setOnAction(e -> controller.showPanel("config"));
        
        Button demoBtn = createActionButton("🎯", "Quick Demo", 
            "Run a demo simulation instantly", "#9b59b6");
        demoBtn.setOnAction(e -> runQuickDemo());
        
        Button viewResultsBtn = createActionButton("📊", "View Results", 
            "See previous simulation results", "#e67e22");
        viewResultsBtn.setOnAction(e -> controller.showPanel("results"));
        
        actions.getChildren().addAll(newSimBtn, configBtn, demoBtn, viewResultsBtn);
        return actions;
    }
    
    private Button createActionButton(String icon, String title, String desc, String color) {
        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(150);
        content.setPrefHeight(120);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        
        content.getChildren().addAll(iconLabel, titleLabel, descLabel);
        
        Button btn = new Button();
        btn.setGraphic(content);
        btn.getStyleClass().add("action-card");
        btn.setStyle("-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0;");
        
        return btn;
    }
    
    private GridPane createFeaturesGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));
        
        String[][] features = {
            {"⏱", "Deadline Constraints", "Ensure tasks complete within time limits"},
            {"💰", "Budget Optimization", "Minimize costs while meeting QoS requirements"},
            {"📈", "Performance Analysis", "Compare multiple scheduling algorithms"},
            {"📊", "Visual Charts", "Interactive graphs and visualizations"},
            {"📄", "Detailed Reports", "HTML, CSV, and text report generation"},
            {"🔄", "Real-time Simulation", "CloudSim Plus integration for accurate modeling"}
        };
        
        int col = 0, row = 0;
        for (String[] feature : features) {
            VBox featureBox = createFeatureBox(feature[0], feature[1], feature[2]);
            grid.add(featureBox, col, row);
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
        
        return grid;
    }
    
    private VBox createFeatureBox(String icon, String title, String desc) {
        VBox box = new VBox(5);
        box.getStyleClass().add("feature-box");
        box.setPadding(new Insets(15));
        box.setPrefWidth(200);
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        header.getChildren().addAll(iconLabel, titleLabel);
        
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);
        
        box.getChildren().addAll(header, descLabel);
        return box;
    }
    
    private VBox createGettingStarted() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(20));
        section.getStyleClass().add("getting-started");
        
        Label title = new Label("Getting Started");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        HBox steps = new HBox(40);
        steps.setAlignment(Pos.CENTER);
        
        steps.getChildren().addAll(
            createStep("1", "Configure", "Set up tasks and VMs"),
            createArrow(),
            createStep("2", "Simulate", "Run scheduling algorithms"),
            createArrow(),
            createStep("3", "Analyze", "Compare results and metrics"),
            createArrow(),
            createStep("4", "Export", "Generate reports")
        );
        
        section.getChildren().addAll(title, steps);
        return section;
    }
    
    private VBox createStep(String number, String title, String desc) {
        VBox step = new VBox(5);
        step.setAlignment(Pos.CENTER);
        
        StackPane circle = new StackPane();
        Circle bg = new Circle(20);
        bg.setFill(Color.web("#3498db"));
        Label num = new Label(number);
        num.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        circle.getChildren().addAll(bg, num);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        step.getChildren().addAll(circle, titleLabel, descLabel);
        return step;
    }
    
    private Label createArrow() {
        Label arrow = new Label("→");
        arrow.setStyle("-fx-font-size: 24px; -fx-text-fill: #bdc3c7;");
        return arrow;
    }
    
    private void runQuickDemo() {
        controller.getStatusBar().setStatus("Starting quick demo...");
        controller.runSimulation(
            java.util.Arrays.asList("QoS-Aware", "FCFS", "Random", "Min-Min"),
            () -> controller.showPanel("results")
        );
    }
    
    public VBox getPanel() {
        return panel;
    }
}
