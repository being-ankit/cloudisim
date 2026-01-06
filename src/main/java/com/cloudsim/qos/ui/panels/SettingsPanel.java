package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.ui.SimulatorController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.prefs.Preferences;

/**
 * Settings Panel - Application preferences and configuration
 */
public class SettingsPanel {
    
    private SimulatorController controller;
    private VBox panel;
    private Preferences prefs;
    
    // Theme settings
    private ToggleGroup themeGroup;
    private RadioButton darkTheme;
    private RadioButton lightTheme;
    
    // Export settings
    private TextField defaultExportPath;
    private CheckBox autoSaveResults;
    private CheckBox showConfirmDialogs;
    
    // Simulation settings
    private Spinner<Integer> defaultTaskCount;
    private Spinner<Integer> defaultVMCount;
    private CheckBox enableAnimations;
    private CheckBox enableSounds;
    
    // Chart settings
    private ComboBox<String> chartStyle;
    private CheckBox showGridLines;
    private CheckBox showLegend;
    
    public SettingsPanel(SimulatorController controller) {
        this.controller = controller;
        this.prefs = Preferences.userNodeForPackage(SettingsPanel.class);
        createPanel();
        loadPreferences();
    }
    
    private void createPanel() {
        panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.getStyleClass().add("settings-panel");
        
        // Header
        Label header = new Label("⚙ Settings");
        header.getStyleClass().add("panel-header");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("settings-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        VBox content = new VBox(25);
        content.setPadding(new Insets(10));
        
        // Appearance Section
        content.getChildren().add(createAppearanceSection());
        
        // General Section
        content.getChildren().add(createGeneralSection());
        
        // Simulation Defaults Section
        content.getChildren().add(createSimulationSection());
        
        // Chart Settings Section
        content.getChildren().add(createChartSection());
        
        // Export Settings Section
        content.getChildren().add(createExportSection());
        
        // Keyboard Shortcuts Section
        content.getChildren().add(createShortcutsSection());
        
        // Action Buttons
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(20, 0, 0, 0));
        
        Button resetBtn = new Button("Reset to Defaults");
        resetBtn.getStyleClass().add("btn-secondary");
        resetBtn.setOnAction(e -> resetToDefaults());
        
        Button saveBtn = new Button("Save Settings");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> savePreferences());
        
        actions.getChildren().addAll(resetBtn, saveBtn);
        content.getChildren().add(actions);
        
        scrollPane.setContent(content);
        panel.getChildren().addAll(header, scrollPane);
    }
    
    private VBox createAppearanceSection() {
        VBox section = createSection("🎨 Appearance");
        
        // Theme selection
        Label themeLabel = new Label("Theme:");
        themeGroup = new ToggleGroup();
        
        darkTheme = new RadioButton("Dark Theme");
        darkTheme.setToggleGroup(themeGroup);
        darkTheme.setSelected(true);
        
        lightTheme = new RadioButton("Light Theme");
        lightTheme.setToggleGroup(themeGroup);
        
        HBox themeBox = new HBox(20, themeLabel, darkTheme, lightTheme);
        themeBox.setAlignment(Pos.CENTER_LEFT);
        
        themeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == lightTheme) {
                controller.setTheme("light");
            } else {
                controller.setTheme("dark");
            }
        });
        
        // Animations
        enableAnimations = new CheckBox("Enable animations");
        enableAnimations.setSelected(true);
        
        // Sounds
        enableSounds = new CheckBox("Enable sound effects");
        enableSounds.setSelected(false);
        
        section.getChildren().addAll(themeBox, enableAnimations, enableSounds);
        return section;
    }
    
    private VBox createGeneralSection() {
        VBox section = createSection("⚡ General");
        
        showConfirmDialogs = new CheckBox("Show confirmation dialogs before destructive actions");
        showConfirmDialogs.setSelected(true);
        
        autoSaveResults = new CheckBox("Auto-save simulation results");
        autoSaveResults.setSelected(false);
        
        CheckBox rememberLastConfig = new CheckBox("Remember last configuration on startup");
        rememberLastConfig.setSelected(true);
        
        CheckBox checkUpdates = new CheckBox("Check for updates on startup");
        checkUpdates.setSelected(false);
        
        section.getChildren().addAll(showConfirmDialogs, autoSaveResults, rememberLastConfig, checkUpdates);
        return section;
    }
    
    private VBox createSimulationSection() {
        VBox section = createSection("🔬 Simulation Defaults");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        // Default task count
        Label taskLabel = new Label("Default task count:");
        defaultTaskCount = new Spinner<>(1, 1000, 10);
        defaultTaskCount.setEditable(true);
        defaultTaskCount.setPrefWidth(100);
        
        grid.add(taskLabel, 0, 0);
        grid.add(defaultTaskCount, 1, 0);
        
        // Default VM count
        Label vmLabel = new Label("Default VM count:");
        defaultVMCount = new Spinner<>(1, 100, 5);
        defaultVMCount.setEditable(true);
        defaultVMCount.setPrefWidth(100);
        
        grid.add(vmLabel, 0, 1);
        grid.add(defaultVMCount, 1, 1);
        
        // Default alpha
        Label alphaLabel = new Label("Default α (time weight):");
        Spinner<Double> defaultAlpha = new Spinner<>(0.0, 1.0, 0.5, 0.1);
        defaultAlpha.setPrefWidth(100);
        
        grid.add(alphaLabel, 0, 2);
        grid.add(defaultAlpha, 1, 2);
        
        // Default beta
        Label betaLabel = new Label("Default β (cost weight):");
        Spinner<Double> defaultBeta = new Spinner<>(0.0, 1.0, 0.5, 0.1);
        defaultBeta.setPrefWidth(100);
        
        grid.add(betaLabel, 0, 3);
        grid.add(defaultBeta, 1, 3);
        
        section.getChildren().add(grid);
        return section;
    }
    
    private VBox createChartSection() {
        VBox section = createSection("📊 Charts");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        // Chart style
        Label styleLabel = new Label("Chart style:");
        chartStyle = new ComboBox<>();
        chartStyle.getItems().addAll("Modern", "Classic", "Minimal");
        chartStyle.setValue("Modern");
        chartStyle.setPrefWidth(150);
        
        grid.add(styleLabel, 0, 0);
        grid.add(chartStyle, 1, 0);
        
        // Grid lines
        showGridLines = new CheckBox("Show grid lines");
        showGridLines.setSelected(true);
        
        // Legend
        showLegend = new CheckBox("Show legend");
        showLegend.setSelected(true);
        
        // Animation
        CheckBox animateCharts = new CheckBox("Animate chart transitions");
        animateCharts.setSelected(true);
        
        section.getChildren().addAll(grid, showGridLines, showLegend, animateCharts);
        return section;
    }
    
    private VBox createExportSection() {
        VBox section = createSection("📁 Export");
        
        // Default export path
        Label pathLabel = new Label("Default export directory:");
        
        HBox pathBox = new HBox(10);
        defaultExportPath = new TextField(System.getProperty("user.home") + "/CloudSim_Exports");
        defaultExportPath.setPrefWidth(350);
        HBox.setHgrow(defaultExportPath, Priority.ALWAYS);
        
        Button browseBtn = new Button("Browse...");
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Export Directory");
            File dir = chooser.showDialog(panel.getScene().getWindow());
            if (dir != null) {
                defaultExportPath.setText(dir.getAbsolutePath());
            }
        });
        
        pathBox.getChildren().addAll(defaultExportPath, browseBtn);
        
        // Export format
        Label formatLabel = new Label("Default report format:");
        ComboBox<String> exportFormat = new ComboBox<>();
        exportFormat.getItems().addAll("HTML", "PDF", "Markdown", "JSON");
        exportFormat.setValue("HTML");
        
        HBox formatBox = new HBox(10, formatLabel, exportFormat);
        formatBox.setAlignment(Pos.CENTER_LEFT);
        
        // Include options
        CheckBox includeCharts = new CheckBox("Include charts in exported reports");
        includeCharts.setSelected(true);
        
        CheckBox includeRawData = new CheckBox("Include raw data in exports");
        includeRawData.setSelected(false);
        
        section.getChildren().addAll(pathLabel, pathBox, formatBox, includeCharts, includeRawData);
        return section;
    }
    
    private VBox createShortcutsSection() {
        VBox section = createSection("⌨ Keyboard Shortcuts");
        
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(8);
        grid.getStyleClass().add("shortcuts-grid");
        
        String[][] shortcuts = {
            {"Ctrl+N", "New configuration"},
            {"Ctrl+O", "Open configuration"},
            {"Ctrl+S", "Save configuration"},
            {"Ctrl+R", "Run simulation"},
            {"Ctrl+E", "Export results"},
            {"Ctrl+1-9", "Switch panels"},
            {"F5", "Refresh data"},
            {"F11", "Toggle fullscreen"},
            {"Ctrl+Q", "Quit application"},
            {"Ctrl+Z", "Undo"},
            {"Ctrl+Y", "Redo"},
            {"Ctrl+,", "Open settings"}
        };
        
        for (int i = 0; i < shortcuts.length; i++) {
            Label key = new Label(shortcuts[i][0]);
            key.getStyleClass().add("shortcut-key");
            key.setStyle("-fx-background-color: #34495e; -fx-padding: 4 8; -fx-background-radius: 4;");
            
            Label desc = new Label(shortcuts[i][1]);
            desc.getStyleClass().add("shortcut-desc");
            
            int col = i < 6 ? 0 : 2;
            int row = i < 6 ? i : i - 6;
            
            grid.add(key, col, row);
            grid.add(desc, col + 1, row);
        }
        
        section.getChildren().add(grid);
        return section;
    }
    
    private VBox createSection(String title) {
        VBox section = new VBox(15);
        section.getStyleClass().add("settings-section");
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #34495e;");
        
        section.getChildren().addAll(titleLabel, sep);
        return section;
    }
    
    private void loadPreferences() {
        // Load saved preferences
        boolean isDark = prefs.getBoolean("darkTheme", true);
        if (isDark) {
            darkTheme.setSelected(true);
        } else {
            lightTheme.setSelected(true);
        }
        
        enableAnimations.setSelected(prefs.getBoolean("enableAnimations", true));
        enableSounds.setSelected(prefs.getBoolean("enableSounds", false));
        showConfirmDialogs.setSelected(prefs.getBoolean("showConfirmDialogs", true));
        autoSaveResults.setSelected(prefs.getBoolean("autoSaveResults", false));
        defaultTaskCount.getValueFactory().setValue(prefs.getInt("defaultTaskCount", 10));
        defaultVMCount.getValueFactory().setValue(prefs.getInt("defaultVMCount", 5));
        defaultExportPath.setText(prefs.get("exportPath", System.getProperty("user.home") + "/CloudSim_Exports"));
        chartStyle.setValue(prefs.get("chartStyle", "Modern"));
        showGridLines.setSelected(prefs.getBoolean("showGridLines", true));
        showLegend.setSelected(prefs.getBoolean("showLegend", true));
    }
    
    private void savePreferences() {
        prefs.putBoolean("darkTheme", darkTheme.isSelected());
        prefs.putBoolean("enableAnimations", enableAnimations.isSelected());
        prefs.putBoolean("enableSounds", enableSounds.isSelected());
        prefs.putBoolean("showConfirmDialogs", showConfirmDialogs.isSelected());
        prefs.putBoolean("autoSaveResults", autoSaveResults.isSelected());
        prefs.putInt("defaultTaskCount", defaultTaskCount.getValue());
        prefs.putInt("defaultVMCount", defaultVMCount.getValue());
        prefs.put("exportPath", defaultExportPath.getText());
        prefs.put("chartStyle", chartStyle.getValue());
        prefs.putBoolean("showGridLines", showGridLines.isSelected());
        prefs.putBoolean("showLegend", showLegend.isSelected());
        
        controller.showInfo("Settings Saved", "Your preferences have been saved successfully.");
    }
    
    private void resetToDefaults() {
        darkTheme.setSelected(true);
        enableAnimations.setSelected(true);
        enableSounds.setSelected(false);
        showConfirmDialogs.setSelected(true);
        autoSaveResults.setSelected(false);
        defaultTaskCount.getValueFactory().setValue(10);
        defaultVMCount.getValueFactory().setValue(5);
        defaultExportPath.setText(System.getProperty("user.home") + "/CloudSim_Exports");
        chartStyle.setValue("Modern");
        showGridLines.setSelected(true);
        showLegend.setSelected(true);
        
        controller.setTheme("dark");
    }
    
    public boolean isShowConfirmDialogs() {
        return showConfirmDialogs.isSelected();
    }
    
    public boolean isAutoSaveResults() {
        return autoSaveResults.isSelected();
    }
    
    public int getDefaultTaskCount() {
        return defaultTaskCount.getValue();
    }
    
    public int getDefaultVMCount() {
        return defaultVMCount.getValue();
    }
    
    public String getExportPath() {
        return defaultExportPath.getText();
    }
    
    public VBox getPanel() {
        return panel;
    }
}
