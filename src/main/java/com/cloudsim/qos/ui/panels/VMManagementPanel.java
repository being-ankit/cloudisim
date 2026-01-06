package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.model.VirtualMachine;
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

import java.util.Random;

/**
 * VM Management Panel - CRUD operations for virtual machines
 */
public class VMManagementPanel {
    
    private SimulatorController controller;
    private VBox panel;
    private TableView<VMRow> vmTable;
    private ObservableList<VMRow> vmData;
    
    // Form fields
    private TextField vmIdField;
    private TextField mipsField;
    private TextField pesField;
    private TextField ramField;
    private TextField bandwidthField;
    private TextField costField;
    private TextField latencyField;
    
    public VMManagementPanel(SimulatorController controller) {
        this.controller = controller;
        createPanel();
        loadData();
    }
    
    private void createPanel() {
        panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("vm-panel");
        
        // Header
        HBox header = createHeader();
        
        // Main content
        HBox mainContent = new HBox(20);
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        
        // VM table and visualization (left)
        VBox leftSection = createLeftSection();
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        
        // Form section (right)
        VBox formSection = createFormSection();
        formSection.setPrefWidth(350);
        
        mainContent.getChildren().addAll(leftSection, formSection);
        
        // Actions
        HBox actions = createActionButtons();
        
        panel.getChildren().addAll(header, mainContent, actions);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("🖥 Virtual Machine Management");
        title.getStyleClass().add("panel-header");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statsLabel = new Label("Total VMs: 0 | Total MIPS: 0");
        statsLabel.setId("vm-stats");
        statsLabel.getStyleClass().add("stats-label");
        
        header.getChildren().addAll(title, spacer, statsLabel);
        return header;
    }
    
    private VBox createLeftSection() {
        VBox section = new VBox(15);
        
        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search VMs...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, old, val) -> filterVMs(val));
        
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Sort by ID", "Sort by MIPS", "Sort by Cost");
        sortCombo.setValue("Sort by ID");
        sortCombo.setOnAction(e -> sortVMs(sortCombo.getValue()));
        
        toolbar.getChildren().addAll(searchField, sortCombo);
        
        // Table
        vmTable = new TableView<>();
        vmTable.getStyleClass().add("vm-table");
        VBox.setVgrow(vmTable, Priority.ALWAYS);
        
        TableColumn<VMRow, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("vmId"));
        idCol.setPrefWidth(50);
        
        TableColumn<VMRow, Integer> mipsCol = new TableColumn<>("MIPS");
        mipsCol.setCellValueFactory(new PropertyValueFactory<>("mips"));
        mipsCol.setPrefWidth(80);
        
        TableColumn<VMRow, Integer> pesCol = new TableColumn<>("PEs");
        pesCol.setCellValueFactory(new PropertyValueFactory<>("pes"));
        pesCol.setPrefWidth(50);
        
        TableColumn<VMRow, Integer> ramCol = new TableColumn<>("RAM (MB)");
        ramCol.setCellValueFactory(new PropertyValueFactory<>("ram"));
        ramCol.setPrefWidth(80);
        
        TableColumn<VMRow, Double> costCol = new TableColumn<>("Cost/s ($)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("costPerSecond"));
        costCol.setPrefWidth(80);
        
        TableColumn<VMRow, Double> latencyCol = new TableColumn<>("Latency (ms)");
        latencyCol.setCellValueFactory(new PropertyValueFactory<>("latency"));
        latencyCol.setPrefWidth(90);
        
        TableColumn<VMRow, Void> powerCol = new TableColumn<>("Power");
        powerCol.setPrefWidth(100);
        powerCol.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar powerBar = new ProgressBar();
            {
                powerBar.setPrefWidth(80);
                powerBar.setPrefHeight(15);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    VMRow row = getTableView().getItems().get(getIndex());
                    double power = row.getMips() / 15000.0; // Normalize to max expected MIPS
                    powerBar.setProgress(Math.min(1.0, power));
                    
                    if (power > 0.7) {
                        powerBar.setStyle("-fx-accent: #27ae60;");
                    } else if (power > 0.4) {
                        powerBar.setStyle("-fx-accent: #f39c12;");
                    } else {
                        powerBar.setStyle("-fx-accent: #e74c3c;");
                    }
                    setGraphic(powerBar);
                }
            }
        });
        
        vmTable.getColumns().addAll(idCol, mipsCol, pesCol, ramCol, costCol, latencyCol, powerCol);
        
        vmTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
        
        vmData = FXCollections.observableArrayList();
        vmTable.setItems(vmData);
        
        // VM Visualization
        VBox visualization = createVisualization();
        
        section.getChildren().addAll(toolbar, vmTable, visualization);
        return section;
    }
    
    private VBox createVisualization() {
        VBox viz = new VBox(10);
        viz.getStyleClass().add("vm-visualization");
        viz.setPadding(new Insets(15));
        
        Label title = new Label("VM Resource Distribution");
        title.setStyle("-fx-font-weight: bold;");
        
        HBox vmBoxes = new HBox(10);
        vmBoxes.setId("vm-viz-boxes");
        vmBoxes.setAlignment(Pos.CENTER_LEFT);
        
        viz.getChildren().addAll(title, vmBoxes);
        return viz;
    }
    
    private void updateVisualization() {
        HBox vmBoxes = (HBox) panel.lookup("#vm-viz-boxes");
        if (vmBoxes == null) return;
        
        vmBoxes.getChildren().clear();
        
        double maxMips = controller.getVms().stream()
                .mapToDouble(VirtualMachine::getMips)
                .max().orElse(10000);
        
        for (VirtualMachine vm : controller.getVms()) {
            VBox vmBox = new VBox(5);
            vmBox.setAlignment(Pos.CENTER);
            vmBox.setPadding(new Insets(5));
            vmBox.getStyleClass().add("vm-viz-box");
            
            // Size based on MIPS
            double height = 30 + (vm.getMips() * 70.0 / maxMips);
            
            Rectangle rect = new Rectangle(50, height);
            double intensity = vm.getMips() / (double) maxMips;
            rect.setFill(Color.rgb(
                (int)(52 + (39 - 52) * intensity),
                (int)(152 + (174 - 152) * intensity),
                (int)(219 + (96 - 219) * intensity)
            ));
            rect.setArcWidth(5);
            rect.setArcHeight(5);
            
            Label idLabel = new Label("VM" + vm.getVmId());
            idLabel.setStyle("-fx-font-size: 10px;");
            
            Label mipsLabel = new Label(vm.getMips() + " MIPS");
            mipsLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #7f8c8d;");
            
            vmBox.getChildren().addAll(rect, idLabel, mipsLabel);
            vmBoxes.getChildren().add(vmBox);
        }
    }
    
    private VBox createFormSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("form-section");
        section.setPadding(new Insets(20));
        
        Label formTitle = new Label("VM Configuration");
        formTitle.getStyleClass().add("form-title");
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        
        int row = 0;
        
        // VM ID
        form.add(new Label("VM ID:"), 0, row);
        vmIdField = new TextField();
        vmIdField.setPromptText("Auto-generated");
        vmIdField.setDisable(true);
        form.add(vmIdField, 1, row++);
        
        // MIPS
        form.add(new Label("MIPS:"), 0, row);
        mipsField = new TextField();
        mipsField.setPromptText("e.g., 10000");
        form.add(mipsField, 1, row++);
        
        // PEs
        form.add(new Label("Processing Elements:"), 0, row);
        pesField = new TextField("1");
        pesField.setPromptText("e.g., 4");
        form.add(pesField, 1, row++);
        
        // RAM
        form.add(new Label("RAM (MB):"), 0, row);
        ramField = new TextField("2048");
        ramField.setPromptText("e.g., 2048");
        form.add(ramField, 1, row++);
        
        // Bandwidth
        form.add(new Label("Bandwidth (Mbps):"), 0, row);
        bandwidthField = new TextField("1000");
        bandwidthField.setPromptText("e.g., 1000");
        form.add(bandwidthField, 1, row++);
        
        // Cost
        form.add(new Label("Cost per Second ($):"), 0, row);
        costField = new TextField();
        costField.setPromptText("e.g., 0.05");
        form.add(costField, 1, row++);
        
        // Latency
        form.add(new Label("Network Latency (ms):"), 0, row);
        latencyField = new TextField();
        latencyField.setPromptText("e.g., 10");
        form.add(latencyField, 1, row++);
        
        // Form buttons
        HBox formButtons = new HBox(10);
        formButtons.setAlignment(Pos.CENTER);
        formButtons.setPadding(new Insets(15, 0, 0, 0));
        
        Button addBtn = new Button("➕ Add VM");
        addBtn.getStyleClass().addAll("btn", "btn-success");
        addBtn.setOnAction(e -> addVM());
        
        Button updateBtn = new Button("✏ Update");
        updateBtn.getStyleClass().addAll("btn", "btn-primary");
        updateBtn.setOnAction(e -> updateVM());
        
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> deleteVM());
        
        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().addAll("btn", "btn-secondary");
        clearBtn.setOnAction(e -> clearForm());
        
        formButtons.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);
        
        // Presets
        VBox presets = createPresets();
        
        section.getChildren().addAll(formTitle, form, formButtons, new Separator(), presets);
        return section;
    }
    
    private VBox createPresets() {
        VBox presets = new VBox(10);
        presets.getStyleClass().add("presets-section");
        presets.setPadding(new Insets(10));
        
        Label title = new Label("Quick Presets");
        title.setStyle("-fx-font-weight: bold;");
        
        FlowPane buttons = new FlowPane(10, 10);
        buttons.setAlignment(Pos.CENTER);
        
        Button smallBtn = new Button("Small");
        smallBtn.getStyleClass().add("preset-btn");
        smallBtn.setOnAction(e -> applyPreset(5000, 1, 1024, 500, 0.02, 15));
        
        Button mediumBtn = new Button("Medium");
        mediumBtn.getStyleClass().add("preset-btn");
        mediumBtn.setOnAction(e -> applyPreset(10000, 2, 2048, 1000, 0.05, 10));
        
        Button largeBtn = new Button("Large");
        largeBtn.getStyleClass().add("preset-btn");
        largeBtn.setOnAction(e -> applyPreset(15000, 4, 4096, 2000, 0.10, 5));
        
        Button xlBtn = new Button("X-Large");
        xlBtn.getStyleClass().add("preset-btn");
        xlBtn.setOnAction(e -> applyPreset(20000, 8, 8192, 5000, 0.20, 2));
        
        buttons.getChildren().addAll(smallBtn, mediumBtn, largeBtn, xlBtn);
        
        // Quick generate
        HBox genControls = new HBox(10);
        genControls.setAlignment(Pos.CENTER);
        genControls.setPadding(new Insets(10, 0, 0, 0));
        
        Spinner<Integer> countSpinner = new Spinner<>(1, 20, 3);
        countSpinner.setPrefWidth(70);
        
        Button generateBtn = new Button("Generate VMs");
        generateBtn.getStyleClass().addAll("btn", "btn-primary");
        generateBtn.setOnAction(e -> generateVMs(countSpinner.getValue()));
        
        genControls.getChildren().addAll(new Label("Count:"), countSpinner, generateBtn);
        
        presets.getChildren().addAll(title, buttons, genControls);
        return presets;
    }
    
    private void applyPreset(int mips, int pes, int ram, int bw, double cost, double latency) {
        mipsField.setText(String.valueOf(mips));
        pesField.setText(String.valueOf(pes));
        ramField.setText(String.valueOf(ram));
        bandwidthField.setText(String.valueOf(bw));
        costField.setText(String.valueOf(cost));
        latencyField.setText(String.valueOf(latency));
    }
    
    private HBox createActionButtons() {
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(15, 0, 0, 0));
        
        Button prevBtn = new Button("← Tasks");
        prevBtn.getStyleClass().addAll("btn", "btn-secondary");
        prevBtn.setOnAction(e -> controller.showPanel("tasks"));
        
        Button clearAllBtn = new Button("Clear All VMs");
        clearAllBtn.getStyleClass().addAll("btn", "btn-danger");
        clearAllBtn.setOnAction(e -> clearAllVMs());
        
        Button nextBtn = new Button("Next: Run Simulation →");
        nextBtn.getStyleClass().addAll("btn", "btn-success");
        nextBtn.setOnAction(e -> controller.showPanel("simulation"));
        
        actions.getChildren().addAll(prevBtn, clearAllBtn, nextBtn);
        return actions;
    }
    
    public void loadData() {
        vmData.clear();
        for (VirtualMachine vm : controller.getVms()) {
            vmData.add(new VMRow(vm));
        }
        updateStats();
        updateVisualization();
    }
    
    public void refreshData() {
        loadData();
    }
    
    private void updateStats() {
        Label statsLabel = (Label) panel.lookup("#vm-stats");
        if (statsLabel != null) {
            double totalMips = controller.getVms().stream()
                    .mapToDouble(VirtualMachine::getMips)
                    .sum();
            statsLabel.setText("Total VMs: " + vmData.size() + " | Total MIPS: " + String.format("%.0f", totalMips));
        }
    }
    
    private void filterVMs(String filter) {
        if (filter == null || filter.isEmpty()) {
            loadData();
            return;
        }
        
        ObservableList<VMRow> filtered = FXCollections.observableArrayList();
        for (VirtualMachine vm : controller.getVms()) {
            if (String.valueOf(vm.getVmId()).contains(filter) ||
                String.valueOf(vm.getMips()).contains(filter)) {
                filtered.add(new VMRow(vm));
            }
        }
        vmData.setAll(filtered);
    }
    
    private void sortVMs(String sortBy) {
        switch (sortBy) {
            case "Sort by MIPS":
                vmData.sort((a, b) -> Double.compare(b.getMips(), a.getMips()));
                break;
            case "Sort by Cost":
                vmData.sort((a, b) -> Double.compare(a.getCostPerSecond(), b.getCostPerSecond()));
                break;
            default:
                vmData.sort((a, b) -> Integer.compare(a.getVmId(), b.getVmId()));
        }
    }
    
    private void populateForm(VMRow row) {
        vmIdField.setText(String.valueOf(row.getVmId()));
        mipsField.setText(String.valueOf(row.getMips()));
        pesField.setText(String.valueOf(row.getPes()));
        ramField.setText(String.valueOf(row.getRam()));
        bandwidthField.setText(String.valueOf(row.getBandwidth()));
        costField.setText(String.valueOf(row.getCostPerSecond()));
        latencyField.setText(String.valueOf(row.getLatency()));
    }
    
    private void clearForm() {
        vmIdField.clear();
        mipsField.clear();
        pesField.setText("1");
        ramField.setText("2048");
        bandwidthField.setText("1000");
        costField.clear();
        latencyField.clear();
        vmTable.getSelectionModel().clearSelection();
    }
    
    private void addVM() {
        try {
            int newId = controller.getVms().stream()
                    .mapToInt(VirtualMachine::getVmId)
                    .max().orElse(0) + 1;
            
            int mips = Integer.parseInt(mipsField.getText().trim());
            int pes = Integer.parseInt(pesField.getText().trim());
            int ram = Integer.parseInt(ramField.getText().trim());
            long bw = Long.parseLong(bandwidthField.getText().trim());
            double cost = Double.parseDouble(costField.getText().trim());
            double latency = Double.parseDouble(latencyField.getText().trim());
            
            VirtualMachine newVM = new VirtualMachine(newId, mips, cost, latency);
            newVM.setNumberOfPes(pes);
            newVM.setRam(ram);
            newVM.setBandwidth(bw);
            
            controller.getVms().add(newVM);
            
            loadData();
            clearForm();
            controller.getStatusBar().setStatus("VM " + newId + " added successfully");
            controller.updateQuickStats();
            
        } catch (NumberFormatException e) {
            controller.showError("Invalid Input", "Please enter valid numeric values.");
        }
    }
    
    private void updateVM() {
        VMRow selected = vmTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            controller.showError("No Selection", "Please select a VM to update.");
            return;
        }
        
        try {
            int mips = Integer.parseInt(mipsField.getText().trim());
            int pes = Integer.parseInt(pesField.getText().trim());
            int ram = Integer.parseInt(ramField.getText().trim());
            long bw = Long.parseLong(bandwidthField.getText().trim());
            double cost = Double.parseDouble(costField.getText().trim());
            double latency = Double.parseDouble(latencyField.getText().trim());
            
            for (VirtualMachine vm : controller.getVms()) {
                if (vm.getVmId() == selected.getVmId()) {
                    vm.setMips(mips);
                    vm.setNumberOfPes(pes);
                    vm.setRam(ram);
                    vm.setBandwidth(bw);
                    vm.setCostPerSecond(cost);
                    vm.setNetworkLatency(latency);
                    break;
                }
            }
            
            loadData();
            controller.getStatusBar().setStatus("VM " + selected.getVmId() + " updated");
            
        } catch (NumberFormatException e) {
            controller.showError("Invalid Input", "Please enter valid numeric values.");
        }
    }
    
    private void deleteVM() {
        VMRow selected = vmTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            controller.showError("No Selection", "Please select a VM to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete VM " + selected.getVmId() + "?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            controller.getVms().removeIf(vm -> vm.getVmId() == selected.getVmId());
            loadData();
            clearForm();
            controller.getStatusBar().setStatus("VM deleted");
            controller.updateQuickStats();
        }
    }
    
    private void generateVMs(int count) {
        Random random = new Random();
        int[] mipsOptions = {5000, 8000, 10000, 12000, 15000};
        double[] costOptions = {0.02, 0.04, 0.06, 0.08, 0.10};
        
        int startId = controller.getVms().stream()
                .mapToInt(VirtualMachine::getVmId)
                .max().orElse(0) + 1;
        
        for (int i = 0; i < count; i++) {
            int id = startId + i;
            int mips = mipsOptions[random.nextInt(mipsOptions.length)];
            double cost = costOptions[random.nextInt(costOptions.length)];
            double latency = 5 + random.nextDouble() * 20;
            
            VirtualMachine vm = new VirtualMachine(id, mips, cost, latency);
            vm.setNumberOfPes(1 + random.nextInt(4));
            vm.setRam(1024 * (1 + random.nextInt(4)));
            vm.setBandwidth(500 + random.nextInt(2000));
            
            controller.getVms().add(vm);
        }
        
        loadData();
        controller.getStatusBar().setStatus("Generated " + count + " VMs");
        controller.updateQuickStats();
    }
    
    private void clearAllVMs() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All VMs");
        confirm.setHeaderText("Delete all VMs?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            controller.getVms().clear();
            loadData();
            clearForm();
            controller.getStatusBar().setStatus("All VMs cleared");
            controller.updateQuickStats();
        }
    }
    
    public VBox getPanel() {
        return panel;
    }
    
    /**
     * Table row wrapper for VirtualMachine
     */
    public static class VMRow {
        private final IntegerProperty vmId;
        private final DoubleProperty mips;
        private final IntegerProperty pes;
        private final LongProperty ram;
        private final LongProperty bandwidth;
        private final DoubleProperty costPerSecond;
        private final DoubleProperty latency;
        
        public VMRow(VirtualMachine vm) {
            this.vmId = new SimpleIntegerProperty(vm.getVmId());
            this.mips = new SimpleDoubleProperty(vm.getMips());
            this.pes = new SimpleIntegerProperty(vm.getNumberOfPes());
            this.ram = new SimpleLongProperty(vm.getRam());
            this.bandwidth = new SimpleLongProperty(vm.getBandwidth());
            this.costPerSecond = new SimpleDoubleProperty(vm.getCostPerSecond());
            this.latency = new SimpleDoubleProperty(vm.getNetworkLatency());
        }
        
        public int getVmId() { return vmId.get(); }
        public double getMips() { return mips.get(); }
        public int getPes() { return pes.get(); }
        public long getRam() { return ram.get(); }
        public long getBandwidth() { return bandwidth.get(); }
        public double getCostPerSecond() { return costPerSecond.get(); }
        public double getLatency() { return latency.get(); }
        
        public IntegerProperty vmIdProperty() { return vmId; }
        public DoubleProperty mipsProperty() { return mips; }
        public IntegerProperty pesProperty() { return pes; }
        public LongProperty ramProperty() { return ram; }
        public LongProperty bandwidthProperty() { return bandwidth; }
        public DoubleProperty costPerSecondProperty() { return costPerSecond; }
        public DoubleProperty latencyProperty() { return latency; }
    }
}
