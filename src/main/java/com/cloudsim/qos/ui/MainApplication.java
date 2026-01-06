package com.cloudsim.qos.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application Entry Point
 * QoS-Aware Multi-Objective Task Scheduling Simulator
 */
public class MainApplication extends Application {
    
    public static final String APP_TITLE = "QoS-Aware Cloud Task Scheduler";
    public static final String APP_VERSION = "1.0";
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Create the main UI controller
            SimulatorController controller = new SimulatorController();
            
            // Create the main scene
            Scene scene = new Scene(controller.getRoot(), 1400, 900);
            
            // Load CSS styling (default dark theme)
            String css = getClass().getResource("/css/main.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            // Configure the primary stage
            primaryStage.setTitle(APP_TITLE + " v" + APP_VERSION);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            
            // Setup keyboard shortcuts
            controller.setupKeyboardShortcuts(scene);
            
            // Show the window
            primaryStage.show();
            
            // Initialize with welcome message
            controller.showWelcome();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
