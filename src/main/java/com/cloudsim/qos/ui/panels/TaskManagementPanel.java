package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.ui.SimulatorController;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Task Management Panel - CRUD operations for cloud tasks
 */
public class TaskManagementPanel {
    
    private SimulatorController controller;
    private VBox panel;
    private TableView<TaskRow> taskTable;
    private ObservableList<TaskRow> taskData;
    
    // Form fields
    private TextField taskIdField;
    private TextField taskLengthField;
    private TextField deadlineField;
    private TextField budgetField;
    private ComboBox<String> priorityCombo;
    
    public TaskManagementPanel(SimulatorController controller) {
        this.controller = controller;
        createPanel();
        loadData();
    }
    
    private void createPanel() {
        panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("task-panel");
        
        // Header
        HBox header = createHeader();
        
        // Main content - split between table and form
        HBox mainContent = new HBox(20);
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        
        // Task table (left side)
        VBox tableSection = createTableSection();
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        
        // Form section (right side)
        VBox formSection = createFormSection();
        formSection.setPrefWidth(350);
        
        mainContent.getChildren().addAll(tableSection, formSection);
        
        // Action buttons
        HBox actions = createActionButtons();
        
        panel.getChildren().addAll(header, mainContent, actions);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("📋 Task Management");
        title.getStyleClass().add("panel-header");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Quick stats
        Label statsLabel = new Label("Total Tasks: 0");
        statsLabel.setId("task-stats");
        statsLabel.getStyleClass().add("stats-label");
        
        header.getChildren().addAll(title, spacer, statsLabel);
        return header;
    }
    
    private VBox createTableSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("table-section");
        
        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search tasks...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, old, val) -> filterTasks(val));
        
        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.getStyleClass().add("btn-small");
        refreshBtn.setOnAction(e -> loadData());
        
        toolbar.getChildren().addAll(searchField, refreshBtn);
        
        // Table
        taskTable = new TableView<>();
        taskTable.getStyleClass().add("task-table");
        VBox.setVgrow(taskTable, Priority.ALWAYS);
        
        // Columns
        TableColumn<TaskRow, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("taskId"));
        idCol.setPrefWidth(50);
        
        TableColumn<TaskRow, Long> lengthCol = new TableColumn<>("Length (MI)");
        lengthCol.setCellValueFactory(new PropertyValueFactory<>("taskLength"));
        lengthCol.setPrefWidth(100);
        
        TableColumn<TaskRow, Double> deadlineCol = new TableColumn<>("Deadline (s)");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        deadlineCol.setPrefWidth(100);
        
        TableColumn<TaskRow, Double> budgetCol = new TableColumn<>("Budget ($)");
        budgetCol.setCellValueFactory(new PropertyValueFactory<>("budget"));
        budgetCol.setPrefWidth(90);
        
        TableColumn<TaskRow, Integer> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(70);
        
        TableColumn<TaskRow, String> statusCol = new TableColumn<>("Status");
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
                    if (item.equals("Ready")) {
                        setStyle("-fx-text-fill: #27ae60;");
                    } else if (item.equals("Tight")) {
                        setStyle("-fx-text-fill: #e67e22;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c;");
                    }
                }
            }
        });
        
        taskTable.getColumns().addAll(idCol, lengthCol, deadlineCol, budgetCol, priorityCol, statusCol);
        
        // Selection listener
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
        
        taskData = FXCollections.observableArrayList();
        taskTable.setItems(taskData);
        
        section.getChildren().addAll(toolbar, taskTable);
        return section;
    }
    
    private VBox createFormSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("form-section");
        section.setPadding(new Insets(20));
        
        Label formTitle = new Label("Task Details");
        formTitle.getStyleClass().add("form-title");
        
        // Form fields
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(15);
        
        int row = 0;
        
        // Task ID
        form.add(new Label("Task ID:"), 0, row);
        taskIdField = new TextField();
        taskIdField.setPromptText("Auto-generated");
        taskIdField.setDisable(true);
        form.add(taskIdField, 1, row++);
        
        // Task Length
        form.add(new Label("Length (MI):"), 0, row);
        taskLengthField = new TextField();
        taskLengthField.setPromptText("e.g., 10000");
        form.add(taskLengthField, 1, row++);
        
        // Deadline
        form.add(new Label("Deadline (s):"), 0, row);
        deadlineField = new TextField();
        deadlineField.setPromptText("e.g., 50");
        form.add(deadlineField, 1, row++);
        
        // Budget
        form.add(new Label("Budget ($):"), 0, row);
        budgetField = new TextField();
        budgetField.setPromptText("e.g., 10.0");
        form.add(budgetField, 1, row++);
        
        // Priority
        form.add(new Label("Priority:"), 0, row);
        priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("1 - Low", "2 - Medium", "3 - High", "4 - Critical", "5 - Urgent");
        priorityCombo.setValue("3 - High");
        priorityCombo.setMaxWidth(Double.MAX_VALUE);
        form.add(priorityCombo, 1, row++);
        
        // Form buttons
        HBox formButtons = new HBox(10);
        formButtons.setAlignment(Pos.CENTER);
        formButtons.setPadding(new Insets(15, 0, 0, 0));
        
        Button addBtn = new Button("➕ Add Task");
        addBtn.getStyleClass().addAll("btn", "btn-success");
        addBtn.setOnAction(e -> addTask());
        
        Button updateBtn = new Button("✏ Update");
        updateBtn.getStyleClass().addAll("btn", "btn-primary");
        updateBtn.setOnAction(e -> updateTask());
        
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> deleteTask());
        
        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().addAll("btn", "btn-secondary");
        clearBtn.setOnAction(e -> clearForm());
        
        formButtons.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);
        
        // Quick generate
        VBox quickGen = new VBox(10);
        quickGen.getStyleClass().add("quick-generate");
        quickGen.setPadding(new Insets(15));
        
        Label quickTitle = new Label("Quick Generate");
        quickTitle.setStyle("-fx-font-weight: bold;");
        
        HBox genControls = new HBox(10);
        genControls.setAlignment(Pos.CENTER_LEFT);
        
        Spinner<Integer> countSpinner = new Spinner<>(1, 100, 10);
        countSpinner.setPrefWidth(80);
        
        Button generateBtn = new Button("Generate Tasks");
        generateBtn.getStyleClass().addAll("btn", "btn-primary");
        generateBtn.setOnAction(e -> generateTasks(countSpinner.getValue()));
        
        genControls.getChildren().addAll(new Label("Count:"), countSpinner, generateBtn);
        quickGen.getChildren().addAll(quickTitle, genControls);
        
        section.getChildren().addAll(formTitle, form, formButtons, new Separator(), quickGen);
        return section;
    }
    
    private HBox createActionButtons() {
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(15, 0, 0, 0));
        
        Button prevBtn = new Button("← Settings");
        prevBtn.getStyleClass().addAll("btn", "btn-secondary");
        prevBtn.setOnAction(e -> controller.showPanel("config"));
        
        Button clearAllBtn = new Button("Clear All Tasks");
        clearAllBtn.getStyleClass().addAll("btn", "btn-danger");
        clearAllBtn.setOnAction(e -> clearAllTasks());
        
        Button nextBtn = new Button("Next: Virtual Machines →");
        nextBtn.getStyleClass().addAll("btn", "btn-success");
        nextBtn.setOnAction(e -> controller.showPanel("vms"));
        
        actions.getChildren().addAll(prevBtn, clearAllBtn, nextBtn);
        return actions;
    }
    
    public void loadData() {
        taskData.clear();
        for (CloudTask task : controller.getTasks()) {
            taskData.add(new TaskRow(task));
        }
        updateStats();
    }
    
    public void refreshData() {
        loadData();
    }
    
    private void updateStats() {
        Label statsLabel = (Label) panel.lookup("#task-stats");
        if (statsLabel != null) {
            statsLabel.setText("Total Tasks: " + taskData.size());
        }
    }
    
    private void filterTasks(String filter) {
        if (filter == null || filter.isEmpty()) {
            loadData();
            return;
        }
        
        String lowerFilter = filter.toLowerCase();
        ObservableList<TaskRow> filtered = FXCollections.observableArrayList();
        
        for (CloudTask task : controller.getTasks()) {
            if (String.valueOf(task.getTaskId()).contains(lowerFilter) ||
                String.valueOf(task.getTaskLength()).contains(lowerFilter)) {
                filtered.add(new TaskRow(task));
            }
        }
        
        taskData.setAll(filtered);
    }
    
    private void populateForm(TaskRow row) {
        taskIdField.setText(String.valueOf(row.getTaskId()));
        taskLengthField.setText(String.valueOf(row.getTaskLength()));
        deadlineField.setText(String.valueOf(row.getDeadline()));
        budgetField.setText(String.valueOf(row.getBudget()));
        priorityCombo.setValue(row.getPriority() + " - " + getPriorityName(row.getPriority()));
    }
    
    private String getPriorityName(int priority) {
        switch (priority) {
            case 1: return "Low";
            case 2: return "Medium";
            case 3: return "High";
            case 4: return "Critical";
            case 5: return "Urgent";
            default: return "Medium";
        }
    }
    
    private void clearForm() {
        taskIdField.clear();
        taskLengthField.clear();
        deadlineField.clear();
        budgetField.clear();
        priorityCombo.setValue("3 - High");
        taskTable.getSelectionModel().clearSelection();
    }
    
    private void addTask() {
        try {
            int newId = controller.getTasks().stream()
                    .mapToInt(CloudTask::getTaskId)
                    .max().orElse(0) + 1;
            
            long length = Long.parseLong(taskLengthField.getText().trim());
            double deadline = Double.parseDouble(deadlineField.getText().trim());
            double budget = Double.parseDouble(budgetField.getText().trim());
            int priority = Integer.parseInt(priorityCombo.getValue().substring(0, 1));
            
            CloudTask newTask = new CloudTask(newId, length, deadline, budget, priority);
            controller.getTasks().add(newTask);
            
            loadData();
            clearForm();
            controller.getStatusBar().setStatus("Task " + newId + " added successfully");
            controller.updateQuickStats();
            
        } catch (NumberFormatException e) {
            controller.showError("Invalid Input", "Please enter valid numeric values for all fields.");
        }
    }
    
    private void updateTask() {
        TaskRow selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            controller.showError("No Selection", "Please select a task to update.");
            return;
        }
        
        try {
            long length = Long.parseLong(taskLengthField.getText().trim());
            double deadline = Double.parseDouble(deadlineField.getText().trim());
            double budget = Double.parseDouble(budgetField.getText().trim());
            int priority = Integer.parseInt(priorityCombo.getValue().substring(0, 1));
            
            // Find and update the task
            for (CloudTask task : controller.getTasks()) {
                if (task.getTaskId() == selected.getTaskId()) {
                    task.setTaskLength(length);
                    task.setDeadline(deadline);
                    task.setBudget(budget);
                    task.setPriority(priority);
                    break;
                }
            }
            
            loadData();
            controller.getStatusBar().setStatus("Task " + selected.getTaskId() + " updated");
            
        } catch (NumberFormatException e) {
            controller.showError("Invalid Input", "Please enter valid numeric values.");
        }
    }
    
    private void deleteTask() {
        TaskRow selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            controller.showError("No Selection", "Please select a task to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Task " + selected.getTaskId() + "?");
        confirm.setContentText("This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            controller.getTasks().removeIf(t -> t.getTaskId() == selected.getTaskId());
            loadData();
            clearForm();
            controller.getStatusBar().setStatus("Task deleted");
            controller.updateQuickStats();
        }
    }
    
    private void generateTasks(int count) {
        Random random = new Random();
        int startId = controller.getTasks().stream()
                .mapToInt(CloudTask::getTaskId)
                .max().orElse(0) + 1;
        
        for (int i = 0; i < count; i++) {
            int id = startId + i;
            long length = 5000 + random.nextInt(45000);
            double deadline = 20 + random.nextDouble() * 80;
            double budget = 5 + random.nextDouble() * 45;
            int priority = 1 + random.nextInt(5);
            
            controller.getTasks().add(new CloudTask(id, length, deadline, budget, priority));
        }
        
        loadData();
        controller.getStatusBar().setStatus("Generated " + count + " tasks");
        controller.updateQuickStats();
    }
    
    private void clearAllTasks() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Tasks");
        confirm.setHeaderText("Delete all tasks?");
        confirm.setContentText("This will remove all " + controller.getTasks().size() + " tasks.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            controller.getTasks().clear();
            loadData();
            clearForm();
            controller.getStatusBar().setStatus("All tasks cleared");
            controller.updateQuickStats();
        }
    }
    
    public VBox getPanel() {
        return panel;
    }
    
    /**
     * Table row wrapper for CloudTask
     */
    public static class TaskRow {
        private final IntegerProperty taskId;
        private final LongProperty taskLength;
        private final DoubleProperty deadline;
        private final DoubleProperty budget;
        private final IntegerProperty priority;
        private final StringProperty status;
        
        public TaskRow(CloudTask task) {
            this.taskId = new SimpleIntegerProperty(task.getTaskId());
            this.taskLength = new SimpleLongProperty(task.getTaskLength());
            this.deadline = new SimpleDoubleProperty(task.getDeadline());
            this.budget = new SimpleDoubleProperty(task.getBudget());
            this.priority = new SimpleIntegerProperty(task.getPriority());
            
            // Determine status based on constraints
            String statusText = "Ready";
            if (task.getDeadline() < 30 || task.getBudget() < 10) {
                statusText = "Tight";
            }
            if (task.getDeadline() < 15 || task.getBudget() < 5) {
                statusText = "Critical";
            }
            this.status = new SimpleStringProperty(statusText);
        }
        
        public int getTaskId() { return taskId.get(); }
        public long getTaskLength() { return taskLength.get(); }
        public double getDeadline() { return deadline.get(); }
        public double getBudget() { return budget.get(); }
        public int getPriority() { return priority.get(); }
        public String getStatus() { return status.get(); }
        
        public IntegerProperty taskIdProperty() { return taskId; }
        public LongProperty taskLengthProperty() { return taskLength; }
        public DoubleProperty deadlineProperty() { return deadline; }
        public DoubleProperty budgetProperty() { return budget; }
        public IntegerProperty priorityProperty() { return priority; }
        public StringProperty statusProperty() { return status; }
    }
}
