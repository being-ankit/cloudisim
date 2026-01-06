package com.cloudsim.qos.config;

import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.VirtualMachine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Module 1: User Input and Configuration Module
 * 
 * This module is responsible for collecting and managing input parameters
 * required for the scheduling process. It allows users to define cloud tasks
 * and virtual machines along with their QoS constraints.
 */
public class InputConfigurationModule {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private List<CloudTask> tasks;
    private List<VirtualMachine> virtualMachines;
    private SimulationConfig simulationConfig;
    
    public InputConfigurationModule() {
        this.tasks = new ArrayList<>();
        this.virtualMachines = new ArrayList<>();
        this.simulationConfig = new SimulationConfig();
    }
    
    /**
     * Loads configuration from JSON files.
     * @param tasksFile Path to tasks JSON file
     * @param vmsFile Path to VMs JSON file
     * @param configFile Path to simulation config file
     */
    public void loadFromFiles(String tasksFile, String vmsFile, String configFile) throws IOException {
        loadTasksFromFile(tasksFile);
        loadVMsFromFile(vmsFile);
        if (configFile != null) {
            loadSimulationConfig(configFile);
        }
        validateInputs();
    }
    
    /**
     * Loads tasks from a JSON file.
     */
    public void loadTasksFromFile(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<CloudTask>>(){}.getType();
            tasks = gson.fromJson(reader, listType);
            System.out.println("Loaded " + tasks.size() + " tasks from " + filePath);
        }
    }
    
    /**
     * Loads VMs from a JSON file.
     */
    public void loadVMsFromFile(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<VirtualMachine>>(){}.getType();
            virtualMachines = gson.fromJson(reader, listType);
            System.out.println("Loaded " + virtualMachines.size() + " VMs from " + filePath);
        }
    }
    
    /**
     * Loads simulation configuration from a JSON file.
     */
    public void loadSimulationConfig(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            simulationConfig = gson.fromJson(reader, SimulationConfig.class);
            System.out.println("Loaded simulation configuration from " + filePath);
        }
    }
    
    /**
     * Interactive mode - accepts input from user via console.
     */
    public void collectInputInteractively() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n=== QoS-Aware Task Scheduling Simulator ===");
        System.out.println("Enter configuration parameters:\n");
        
        // Collect simulation config
        System.out.print("Enter alpha (time weight, 0-1): ");
        simulationConfig.setAlpha(scanner.nextDouble());
        
        System.out.print("Enter beta (cost weight, 0-1): ");
        simulationConfig.setBeta(scanner.nextDouble());
        
        // Collect tasks
        System.out.print("\nEnter number of tasks: ");
        int numTasks = scanner.nextInt();
        
        for (int i = 0; i < numTasks; i++) {
            System.out.println("\n--- Task " + (i + 1) + " ---");
            CloudTask task = collectTaskInput(scanner, i + 1);
            tasks.add(task);
        }
        
        // Collect VMs
        System.out.print("\nEnter number of VMs: ");
        int numVMs = scanner.nextInt();
        
        for (int i = 0; i < numVMs; i++) {
            System.out.println("\n--- VM " + (i + 1) + " ---");
            VirtualMachine vm = collectVMInput(scanner, i + 1);
            virtualMachines.add(vm);
        }
        
        validateInputs();
        System.out.println("\nConfiguration complete!");
    }
    
    /**
     * Collects single task input from user.
     */
    private CloudTask collectTaskInput(Scanner scanner, int taskId) {
        System.out.print("Task Length (Million Instructions): ");
        long length = scanner.nextLong();
        
        System.out.print("Deadline (seconds): ");
        double deadline = scanner.nextDouble();
        
        System.out.print("Budget (cost limit): ");
        double budget = scanner.nextDouble();
        
        System.out.print("Priority (1-10): ");
        int priority = scanner.nextInt();
        
        return new CloudTask(taskId, length, deadline, budget, priority);
    }
    
    /**
     * Collects single VM input from user.
     */
    private VirtualMachine collectVMInput(Scanner scanner, int vmId) {
        System.out.print("Processing Power (MIPS): ");
        double mips = scanner.nextDouble();
        
        System.out.print("Cost per Second ($): ");
        double cost = scanner.nextDouble();
        
        System.out.print("Network Latency (seconds): ");
        double latency = scanner.nextDouble();
        
        System.out.print("Number of PEs (cores): ");
        int pes = scanner.nextInt();
        
        return new VirtualMachine(vmId, mips, cost, latency, pes, 2048, 1000, 10000);
    }
    
    /**
     * Generates sample/default configuration for testing.
     */
    public void generateSampleConfiguration(int numTasks, int numVMs) {
        tasks.clear();
        virtualMachines.clear();
        
        // Generate sample tasks with varying requirements
        for (int i = 1; i <= numTasks; i++) {
            long length = 1000 + (long)(Math.random() * 9000);  // 1000-10000 MI
            double deadline = 5 + Math.random() * 15;           // 5-20 seconds
            double budget = 0.5 + Math.random() * 2.0;          // $0.5-$2.5
            int priority = 1 + (int)(Math.random() * 10);       // 1-10
            
            tasks.add(new CloudTask(i, length, deadline, budget, priority));
        }
        
        // Generate sample VMs with varying capabilities
        double[][] vmConfigs = {
            {1000, 0.05, 0.1},   // Low-cost, slow
            {2000, 0.08, 0.08},  // Medium
            {3000, 0.12, 0.05},  // High-performance
            {4000, 0.15, 0.03},  // Premium
            {1500, 0.06, 0.12},  // Economy
        };
        
        for (int i = 0; i < numVMs; i++) {
            int configIndex = i % vmConfigs.length;
            double mips = vmConfigs[configIndex][0] + (Math.random() * 500 - 250);
            double cost = vmConfigs[configIndex][1] * (0.9 + Math.random() * 0.2);
            double latency = vmConfigs[configIndex][2] * (0.8 + Math.random() * 0.4);
            
            virtualMachines.add(new VirtualMachine(i + 1, mips, cost, latency,
                    1 + (int)(Math.random() * 4), 2048, 1000, 10000));
        }
        
        System.out.println("Generated sample configuration: " + numTasks + " tasks, " + numVMs + " VMs");
    }
    
    /**
     * Validates all input values.
     */
    public void validateInputs() {
        List<String> errors = new ArrayList<>();
        
        // Validate tasks
        for (CloudTask task : tasks) {
            if (task.getTaskLength() <= 0) {
                errors.add("Task " + task.getTaskId() + ": Invalid task length");
            }
            if (task.getDeadline() <= 0) {
                errors.add("Task " + task.getTaskId() + ": Invalid deadline");
            }
            if (task.getBudget() <= 0) {
                errors.add("Task " + task.getTaskId() + ": Invalid budget");
            }
        }
        
        // Validate VMs
        for (VirtualMachine vm : virtualMachines) {
            if (vm.getMips() <= 0) {
                errors.add("VM " + vm.getVmId() + ": Invalid MIPS");
            }
            if (vm.getCostPerSecond() <= 0) {
                errors.add("VM " + vm.getVmId() + ": Invalid cost per second");
            }
            if (vm.getNetworkLatency() < 0) {
                errors.add("VM " + vm.getVmId() + ": Invalid network latency");
            }
        }
        
        // Validate simulation config
        if (simulationConfig.getAlpha() < 0 || simulationConfig.getAlpha() > 1) {
            errors.add("Invalid alpha value (must be 0-1)");
        }
        if (simulationConfig.getBeta() < 0 || simulationConfig.getBeta() > 1) {
            errors.add("Invalid beta value (must be 0-1)");
        }
        
        if (!errors.isEmpty()) {
            System.err.println("\nValidation Errors:");
            errors.forEach(e -> System.err.println("  - " + e));
            throw new IllegalArgumentException("Input validation failed");
        }
        
        System.out.println("Input validation successful.");
    }
    
    /**
     * Saves current configuration to JSON files.
     */
    public void saveConfiguration(String outputDir) throws IOException {
        new File(outputDir).mkdirs();
        
        // Save tasks
        try (Writer writer = new FileWriter(outputDir + "/tasks.json")) {
            gson.toJson(tasks, writer);
        }
        
        // Save VMs
        try (Writer writer = new FileWriter(outputDir + "/vms.json")) {
            gson.toJson(virtualMachines, writer);
        }
        
        // Save config
        try (Writer writer = new FileWriter(outputDir + "/config.json")) {
            gson.toJson(simulationConfig, writer);
        }
        
        System.out.println("Configuration saved to " + outputDir);
    }
    
    /**
     * Prints current configuration summary.
     */
    public void printConfigurationSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("CONFIGURATION SUMMARY");
        System.out.println("=".repeat(60));
        
        System.out.println("\nSimulation Parameters:");
        System.out.println("  Alpha (Time Weight): " + simulationConfig.getAlpha());
        System.out.println("  Beta (Cost Weight): " + simulationConfig.getBeta());
        
        System.out.println("\nTasks (" + tasks.size() + "):");
        System.out.println(String.format("  %-6s %-12s %-12s %-12s %-8s",
                "ID", "Length(MI)", "Deadline(s)", "Budget($)", "Priority"));
        System.out.println("  " + "-".repeat(50));
        for (CloudTask task : tasks) {
            System.out.println(String.format("  %-6d %-12d %-12.2f %-12.2f %-8d",
                    task.getTaskId(), task.getTaskLength(), task.getDeadline(),
                    task.getBudget(), task.getPriority()));
        }
        
        System.out.println("\nVirtual Machines (" + virtualMachines.size() + "):");
        System.out.println(String.format("  %-6s %-10s %-12s %-12s %-6s",
                "ID", "MIPS", "Cost($/s)", "Latency(s)", "PEs"));
        System.out.println("  " + "-".repeat(50));
        for (VirtualMachine vm : virtualMachines) {
            System.out.println(String.format("  %-6d %-10.0f %-12.4f %-12.4f %-6d",
                    vm.getVmId(), vm.getMips(), vm.getCostPerSecond(),
                    vm.getNetworkLatency(), vm.getNumberOfPes()));
        }
        
        System.out.println("=".repeat(60) + "\n");
    }
    
    // Getters
    public List<CloudTask> getTasks() {
        return new ArrayList<>(tasks);
    }
    
    public List<VirtualMachine> getVirtualMachines() {
        return new ArrayList<>(virtualMachines);
    }
    
    public SimulationConfig getSimulationConfig() {
        return simulationConfig;
    }
    
    public void setSimulationConfig(SimulationConfig config) {
        this.simulationConfig = config;
    }
    
    public void setTasks(List<CloudTask> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }
    
    public void setVirtualMachines(List<VirtualMachine> vms) {
        this.virtualMachines = new ArrayList<>(vms);
    }
    
    /**
     * Loads configuration from a directory with standard file names.
     * @param directory The directory containing config files
     * @param baseName The base name for configuration files (ignored, uses standard names)
     */
    public void loadFromFile(String directory, String baseName) throws IOException {
        File tasksFile = new File(directory, "tasks.json");
        File vmsFile = new File(directory, "vms.json");
        File configFile = new File(directory, "config.json");
        
        if (tasksFile.exists()) {
            loadTasksFromFile(tasksFile.getAbsolutePath());
        }
        if (vmsFile.exists()) {
            loadVMsFromFile(vmsFile.getAbsolutePath());
        }
        if (configFile.exists()) {
            loadSimulationConfig(configFile.getAbsolutePath());
        }
    }
}
