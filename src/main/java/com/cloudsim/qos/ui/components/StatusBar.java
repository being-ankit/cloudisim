package com.cloudsim.qos.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Status bar component for displaying application status
 */
public class StatusBar extends HBox {
    
    private Label statusLabel;
    private Label timestampLabel;
    private ProgressIndicator progressIndicator;
    
    public StatusBar() {
        initialize();
    }
    
    private void initialize() {
        getStyleClass().add("status-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(8, 15, 8, 15));
        setSpacing(15);
        
        // Progress indicator (hidden by default)
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(16, 16);
        progressIndicator.setVisible(false);
        
        // Status message
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-text");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Timestamp
        timestampLabel = new Label("");
        timestampLabel.getStyleClass().add("status-timestamp");
        
        // Version info
        Label versionLabel = new Label("v1.0");
        versionLabel.getStyleClass().add("status-version");
        
        getChildren().addAll(progressIndicator, statusLabel, spacer, timestampLabel, versionLabel);
    }
    
    public void setStatus(String status) {
        statusLabel.setText(status);
        timestampLabel.setText(java.time.LocalTime.now().toString().substring(0, 8));
    }
    
    public void showProgress(boolean show) {
        progressIndicator.setVisible(show);
    }
    
    public void setProgress(double progress) {
        progressIndicator.setProgress(progress);
    }
}
