package com.cloudsim.qos.ui;

import com.cloudsim.qos.config.InputConfigurationModule;
import com.cloudsim.qos.config.SimulationConfig;
import com.cloudsim.qos.evaluation.PerformanceEvaluationModule;
import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.model.VirtualMachine;
import com.cloudsim.qos.profiling.TaskProfilingModule;
import com.cloudsim.qos.scheduler.*;
import com.cloudsim.qos.ui.components.*;
import com.cloudsim.qos.ui.panels.*;
import com.cloudsim.qos.ui.utils.*;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

/**
 * Main Controller for the Simulator UI
 * Manages all panels and coordinates simulation execution
 */
public class SimulatorController {
    
    private BorderPane root;
    private VBox sidebar;
    private StackPane contentArea;
    private StatusBar statusBar;
    
    // Panels
    private WelcomePanel welcomePanel;
    private ConfigurationPanel configPanel;
    private TaskManagementPanel taskPanel;
    private VMManagementPanel vmPanel;
    private SimulationPanel simulationPanel;
    private ResultsPanel resultsPanel;
    private ChartsPanel chartsPanel;
    private ReportsPanel reportsPanel;
    private ComparisonPanel comparisonPanel;
    private SettingsPanel settingsPanel;
    private HistoryPanel historyPanel;
    
    // Data
    private List<CloudTask> tasks = new ArrayList<>();
    private List<VirtualMachine> vms = new ArrayList<>();
    private SimulationConfig config;
    private Map<String, List<SchedulingResult>> allResults = new LinkedHashMap<>();
    private Map<String, Map<String, Double>> allMetrics = new LinkedHashMap<>();
    
    // Sidebar buttons for highlighting
    private Map<String, Button> sidebarButtons = new HashMap<>();
    private String currentPanel = "welcome";
    
    // Theme management
    private String currentTheme = "dark";
    private static final String DARK_THEME = "/css/main.css";
    private static final String LIGHT_THEME = "/css/light-theme.css";
    private Preferences prefs = Preferences.userNodeForPackage(SimulatorController.class);
    
    // Run counter
    private int runCounter = 0;
    
    public SimulatorController() {
        initializeUI();
        initializePanels();
        initializeData();
    }
    
    private void initializeUI() {
        root = new BorderPane();
        root.getStyleClass().add("main-container");
        
        // Create header
        HBox header = createHeader();
        root.setTop(header);
        
        // Create sidebar
        sidebar = createSidebar();
        root.setLeft(sidebar);
        
        // Create content area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        contentArea.setPadding(new Insets(20));
        root.setCenter(contentArea);
        
        // Create status bar
        statusBar = new StatusBar();
        root.setBottom(statusBar);
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setSpacing(20);
        
        // Logo/Title
        Label logo = new Label("☁");
        logo.getStyleClass().add("logo");
        
        VBox titleBox = new VBox(2);
        Label title = new Label("QoS-Aware Task Scheduler");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Multi-Objective Cloud Computing Simulator");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Quick action buttons
        Button loadBtn = new Button("📂 Load Config");
        loadBtn.getStyleClass().addAll("header-button");
        loadBtn.setOnAction(e -> loadConfiguration());
        
        Button saveBtn = new Button("💾 Save Config");
        saveBtn.getStyleClass().addAll("header-button");
        saveBtn.setOnAction(e -> saveConfiguration());
        
        Button helpBtn = new Button("❓ Help");
        helpBtn.getStyleClass().addAll("header-button");
        helpBtn.setOnAction(e -> showHelp());
        
        header.getChildren().addAll(logo, titleBox, spacer, loadBtn, saveBtn, helpBtn);
        return header;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setSpacing(5);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        
        // Navigation sections
        Label configSection = new Label("CONFIGURATION");
        configSection.getStyleClass().add("sidebar-section");
        
        Button welcomeBtn = createSidebarButton("🏠", "Welcome", "welcome");
        Button configBtn = createSidebarButton("⚙", "Parameters", "config");
        Button tasksBtn = createSidebarButton("📋", "Tasks", "tasks");
        Button vmsBtn = createSidebarButton("🖥", "Virtual Machines", "vms");
        
        Label simSection = new Label("SIMULATION");
        simSection.getStyleClass().add("sidebar-section");
        
        Button runBtn = createSidebarButton("▶", "Run Simulation", "simulation");
        Button resultsBtn = createSidebarButton("📊", "Results", "results");
        Button historyBtn = createSidebarButton("📜", "History", "history");
        
        Label analysisSection = new Label("ANALYSIS");
        analysisSection.getStyleClass().add("sidebar-section");
        
        Button chartsBtn = createSidebarButton("📈", "Charts", "charts");
        Button reportsBtn = createSidebarButton("📄", "Reports", "reports");
        Button compareBtn = createSidebarButton("⚖", "Comparison", "comparison");
        
        Label toolsSection = new Label("TOOLS");
        toolsSection.getStyleClass().add("sidebar-section");
        
        Button settingsBtn = createSidebarButton("🔧", "Settings", "settings");
        
        // Store buttons for highlighting
        sidebarButtons.put("welcome", welcomeBtn);
        sidebarButtons.put("config", configBtn);
        sidebarButtons.put("tasks", tasksBtn);
        sidebarButtons.put("vms", vmsBtn);
        sidebarButtons.put("simulation", runBtn);
        sidebarButtons.put("results", resultsBtn);
        sidebarButtons.put("history", historyBtn);
        sidebarButtons.put("charts", chartsBtn);
        sidebarButtons.put("reports", reportsBtn);
        sidebarButtons.put("comparison", compareBtn);
        sidebarButtons.put("settings", settingsBtn);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Quick stats at bottom
        VBox statsBox = createQuickStats();
        
        sidebar.getChildren().addAll(
            configSection,
            welcomeBtn, configBtn, tasksBtn, vmsBtn,
            new Separator(),
            simSection,
            runBtn, resultsBtn, historyBtn,
            new Separator(),
            analysisSection,
            chartsBtn, reportsBtn, compareBtn,
            new Separator(),
            toolsSection,
            settingsBtn,
            spacer,
            statsBox
        );
        
        return sidebar;
    }
    
    private Button createSidebarButton(String icon, String text, String panelId) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("sidebar-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> showPanel(panelId));
        return btn;
    }
    
    private VBox createQuickStats() {
        VBox stats = new VBox(8);
        stats.getStyleClass().add("quick-stats");
        stats.setPadding(new Insets(15));
        
        Label statsTitle = new Label("Quick Stats");
        statsTitle.getStyleClass().add("stats-title");
        
        Label tasksLabel = new Label("Tasks: 0");
        tasksLabel.setId("stats-tasks");
        
        Label vmsLabel = new Label("VMs: 0");
        vmsLabel.setId("stats-vms");
        
        Label runsLabel = new Label("Runs: 0");
        runsLabel.setId("stats-runs");
        
        stats.getChildren().addAll(statsTitle, tasksLabel, vmsLabel, runsLabel);
        return stats;
    }
    
    private void initializePanels() {
        welcomePanel = new WelcomePanel(this);
        configPanel = new ConfigurationPanel(this);
        taskPanel = new TaskManagementPanel(this);
        vmPanel = new VMManagementPanel(this);
        simulationPanel = new SimulationPanel(this);
        resultsPanel = new ResultsPanel(this);
        chartsPanel = new ChartsPanel(this);
        reportsPanel = new ReportsPanel(this);
        comparisonPanel = new ComparisonPanel(this);
        settingsPanel = new SettingsPanel(this);
        historyPanel = new HistoryPanel(this);
    }
    
    private void initializeData() {
        // Initialize with default config
        config = new SimulationConfig(0.5, 0.5);
        
        // Generate sample data
        InputConfigurationModule inputModule = new InputConfigurationModule();
        inputModule.generateSampleConfiguration(10, 5);
        tasks = new ArrayList<>(inputModule.getTasks());
        vms = new ArrayList<>(inputModule.getVirtualMachines());
        
        updateQuickStats();
    }
    
    public void showPanel(String panelId) {
        // Update button highlighting
        sidebarButtons.values().forEach(btn -> btn.getStyleClass().remove("active"));
        if (sidebarButtons.containsKey(panelId)) {
            sidebarButtons.get(panelId).getStyleClass().add("active");
        }
        
        currentPanel = panelId;
        contentArea.getChildren().clear();
        
        Node panel;
        switch (panelId) {
            case "welcome":
                panel = welcomePanel.getPanel();
                break;
            case "config":
                panel = configPanel.getPanel();
                break;
            case "tasks":
                panel = taskPanel.getPanel();
                break;
            case "vms":
                panel = vmPanel.getPanel();
                break;
            case "simulation":
                panel = simulationPanel.getPanel();
                break;
            case "results":
                panel = resultsPanel.getPanel();
                break;
            case "charts":
                panel = chartsPanel.getPanel();
                break;
            case "reports":
                panel = reportsPanel.getPanel();
                break;
            case "comparison":
                panel = comparisonPanel.getPanel();
                break;
            case "settings":
                panel = settingsPanel.getPanel();
                break;
            case "history":
                panel = historyPanel.getPanel();
                break;
            default:
                panel = welcomePanel.getPanel();
                break;
        }
        
        contentArea.getChildren().add(panel);
        statusBar.setStatus("Viewing: " + panelId.substring(0, 1).toUpperCase() + panelId.substring(1));
    }
    
    public void showWelcome() {
        showPanel("welcome");
    }
    
    public void updateQuickStats() {
        Platform.runLater(() -> {
            Label tasksLabel = (Label) root.lookup("#stats-tasks");
            Label vmsLabel = (Label) root.lookup("#stats-vms");
            Label runsLabel = (Label) root.lookup("#stats-runs");
            
            if (tasksLabel != null) tasksLabel.setText("Tasks: " + tasks.size());
            if (vmsLabel != null) vmsLabel.setText("VMs: " + vms.size());
            if (runsLabel != null) runsLabel.setText("Runs: " + allResults.size());
        });
    }
    
    public void runSimulation(List<String> selectedAlgorithms, Runnable onComplete) {
        statusBar.setStatus("Running simulation...");
        statusBar.showProgress(true);
        
        CompletableFuture.runAsync(() -> {
            try {
                allResults.clear();
                allMetrics.clear();
                
                // TaskProfilingModule can be used for advanced profiling features
                @SuppressWarnings("unused")
                TaskProfilingModule profiler = new TaskProfilingModule(tasks, vms);
                PerformanceEvaluationModule evaluator = new PerformanceEvaluationModule();
                
                int total = selectedAlgorithms.size();
                int current = 0;
                
                for (String algorithm : selectedAlgorithms) {
                    current++;
                    int finalCurrent = current;
                    Platform.runLater(() -> 
                        statusBar.setStatus("Running " + algorithm + " (" + finalCurrent + "/" + total + ")...")
                    );
                    
                    TaskScheduler scheduler = createScheduler(algorithm);
                    scheduler.schedule(tasks, vms);
                    List<SchedulingResult> results = scheduler.getResults();
                    
                    allResults.put(algorithm, results);
                    allMetrics.put(algorithm, evaluator.calculateMetrics(results, tasks, vms));
                    
                    Thread.sleep(100); // Small delay for visual feedback
                }
                
                Platform.runLater(() -> {
                    statusBar.setStatus("Simulation completed successfully!");
                    statusBar.showProgress(false);
                    updateQuickStats();
                    
                    // Update all panels with new data
                    resultsPanel.updateResults(allResults, allMetrics);
                    chartsPanel.updateCharts(allResults, allMetrics);
                    comparisonPanel.updateData(allResults, allMetrics);
                    reportsPanel.updateReports(allResults, allMetrics, tasks, vms);
                    
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusBar.setStatus("Error: " + e.getMessage());
                    statusBar.showProgress(false);
                    showError("Simulation Error", e.getMessage());
                });
            }
        });
    }
    
    private TaskScheduler createScheduler(String algorithm) {
        switch (algorithm) {
            case "QoS-Aware":
                return new QoSAwareScheduler(config);
            case "FCFS":
                return new FCFSScheduler();
            case "Random":
                return new RandomScheduler();
            case "Min-Min":
                return new MinMinScheduler();
            default:
                return new QoSAwareScheduler(config);
        }
    }
    
    private void loadConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Configuration");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        fileChooser.setInitialDirectory(new File("config"));
        
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            try {
                InputConfigurationModule inputModule = new InputConfigurationModule();
                inputModule.loadFromFile(file.getParent(), 
                    file.getName().replace(".json", ""));
                
                // Check what type of config was loaded
                if (!inputModule.getTasks().isEmpty()) {
                    tasks = new ArrayList<>(inputModule.getTasks());
                }
                if (!inputModule.getVirtualMachines().isEmpty()) {
                    vms = new ArrayList<>(inputModule.getVirtualMachines());
                }
                
                updateQuickStats();
                taskPanel.refreshData();
                vmPanel.refreshData();
                statusBar.setStatus("Configuration loaded from: " + file.getName());
                
            } catch (Exception e) {
                showError("Load Error", "Failed to load configuration: " + e.getMessage());
            }
        }
    }
    
    private void saveConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Configuration");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        fileChooser.setInitialFileName("simulation_config.json");
        
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file != null) {
            try {
                InputConfigurationModule inputModule = new InputConfigurationModule();
                inputModule.setTasks(tasks);
                inputModule.setVirtualMachines(vms);
                inputModule.saveConfiguration(file.getParent());
                statusBar.setStatus("Configuration saved to: " + file.getName());
                
            } catch (Exception e) {
                showError("Save Error", "Failed to save configuration: " + e.getMessage());
            }
        }
    }
    
    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("QoS-Aware Task Scheduler - Help");
        alert.setContentText(
            "Welcome to the QoS-Aware Multi-Objective Task Scheduling Simulator!\n\n" +
            "Quick Start:\n" +
            "1. Configure tasks and VMs in their respective panels\n" +
            "2. Adjust scheduling parameters in Settings\n" +
            "3. Run simulation to compare algorithms\n" +
            "4. View results, charts, and reports\n\n" +
            "Key Features:\n" +
            "• QoS-Aware scheduling with deadline and budget constraints\n" +
            "• Multiple baseline algorithms for comparison\n" +
            "• Interactive charts and detailed reports\n" +
            "• Export results in various formats\n\n" +
            "For more details, see the README.md file.");
        alert.showAndWait();
    }
    
    public void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    public void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // Getters and Setters
    public BorderPane getRoot() { return root; }
    public List<CloudTask> getTasks() { return tasks; }
    public List<VirtualMachine> getVms() { return vms; }
    public SimulationConfig getConfig() { return config; }
    public Map<String, List<SchedulingResult>> getAllResults() { return allResults; }
    public Map<String, Map<String, Double>> getAllMetrics() { return allMetrics; }
    public StatusBar getStatusBar() { return statusBar; }
    
    public void setTasks(List<CloudTask> tasks) { 
        this.tasks = tasks; 
        updateQuickStats();
    }
    
    public void setVms(List<VirtualMachine> vms) { 
        this.vms = vms; 
        updateQuickStats();
    }
    
    public void setConfig(SimulationConfig config) { 
        this.config = config; 
    }
    
    // Theme Management
    public void setTheme(String theme) {
        if (root.getScene() != null) {
            Scene scene = root.getScene();
            scene.getStylesheets().clear();
            
            String cssPath = "dark".equals(theme) ? DARK_THEME : LIGHT_THEME;
            String css = getClass().getResource(cssPath).toExternalForm();
            scene.getStylesheets().add(css);
            
            currentTheme = theme;
            prefs.put("theme", theme);
            statusBar.setStatus("Theme changed to: " + theme);
        }
    }
    
    public String getCurrentTheme() {
        return currentTheme;
    }
    
    // Add history entry after simulation completes
    public void addHistoryEntry(String algorithm, double alpha, double beta, Map<String, Double> metrics) {
        historyPanel.addHistoryEntry(
            algorithm,
            tasks.size(),
            vms.size(),
            alpha,
            beta,
            allResults.get(algorithm),
            metrics
        );
    }
    
    // Setup keyboard shortcuts for the scene
    public void setupKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(event -> {
            // Ctrl+1-9 for quick navigation
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case DIGIT1: showPanel("welcome"); break;
                    case DIGIT2: showPanel("config"); break;
                    case DIGIT3: showPanel("tasks"); break;
                    case DIGIT4: showPanel("vms"); break;
                    case DIGIT5: showPanel("simulation"); break;
                    case DIGIT6: showPanel("results"); break;
                    case DIGIT7: showPanel("charts"); break;
                    case DIGIT8: showPanel("reports"); break;
                    case DIGIT9: showPanel("comparison"); break;
                    case N: // New configuration
                        initializeData();
                        taskPanel.refreshData();
                        vmPanel.refreshData();
                        statusBar.setStatus("New configuration created");
                        break;
                    case O: loadConfiguration(); break;
                    case S: saveConfiguration(); break;
                    case R: 
                        if (event.isShiftDown()) {
                            showPanel("results");
                        } else {
                            showPanel("simulation");
                        }
                        break;
                    default: break;
                }
            }
            
            // Function keys
            switch (event.getCode()) {
                case F1: showHelp(); break;
                case F5: showPanel("simulation"); break;
                default: break;
            }
        });
        
        // Apply saved theme
        String savedTheme = prefs.get("theme", "dark");
        setTheme(savedTheme);
    }
    
    public HistoryPanel getHistoryPanel() { return historyPanel; }
    public ConfigurationPanel getConfigPanel() { return configPanel; }
}
