package com.cloudsim.qos.ui.utils;

import com.cloudsim.qos.config.SimulationConfig;
import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.VirtualMachine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for importing and exporting configurations
 */
public class ConfigurationManager {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Export configuration to JSON file
     */
    public static void exportConfiguration(Window owner, 
                                          SimulationConfig config, 
                                          List<CloudTask> tasks, 
                                          List<VirtualMachine> vms) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Configuration");
        fileChooser.setInitialFileName("simulation_config.json");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                ConfigurationData data = new ConfigurationData(config, tasks, vms);
                String json = gson.toJson(data);
                Files.writeString(file.toPath(), json);
            } catch (IOException e) {
                throw new RuntimeException("Failed to export configuration: " + e.getMessage());
            }
        }
    }
    
    /**
     * Import configuration from JSON file
     */
    public static ConfigurationData importConfiguration(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Configuration");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            try {
                String json = Files.readString(file.toPath());
                return gson.fromJson(json, ConfigurationData.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to import configuration: " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Export tasks to CSV
     */
    public static void exportTasksToCSV(Window owner, List<CloudTask> tasks) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Tasks to CSV");
        fileChooser.setInitialFileName("tasks.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Header
                writer.println("TaskId,TaskLength,Deadline,Budget,Priority");
                
                // Data
                for (CloudTask task : tasks) {
                    writer.printf("%d,%d,%.2f,%.2f,%d%n",
                        task.getTaskId(),
                        task.getTaskLength(),
                        task.getDeadline(),
                        task.getBudget(),
                        task.getPriority()
                    );
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to export tasks: " + e.getMessage());
            }
        }
    }
    
    /**
     * Import tasks from CSV
     */
    public static List<CloudTask> importTasksFromCSV(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Tasks from CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            List<CloudTask> tasks = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine(); // Skip header
                
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        CloudTask task = new CloudTask(
                            Integer.parseInt(parts[0].trim()),   // taskId
                            Long.parseLong(parts[1].trim()),     // taskLength
                            Double.parseDouble(parts[2].trim()), // deadline
                            Double.parseDouble(parts[3].trim()), // budget
                            Integer.parseInt(parts[4].trim())    // priority
                        );
                        tasks.add(task);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                throw new RuntimeException("Failed to import tasks: " + e.getMessage());
            }
            return tasks;
        }
        return null;
    }
    
    /**
     * Export VMs to CSV
     */
    public static void exportVMsToCSV(Window owner, List<VirtualMachine> vms) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export VMs to CSV");
        fileChooser.setInitialFileName("virtual_machines.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Header
                writer.println("VmId,MIPS,CostPerSecond,NetworkLatency,PEs,RAM,Bandwidth,Storage");
                
                // Data
                for (VirtualMachine vm : vms) {
                    writer.printf("%d,%.0f,%.4f,%.2f,%d,%d,%d,%d%n",
                        vm.getVmId(),
                        vm.getMips(),
                        vm.getCostPerSecond(),
                        vm.getNetworkLatency(),
                        vm.getNumberOfPes(),
                        vm.getRam(),
                        vm.getBandwidth(),
                        vm.getStorage()
                    );
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to export VMs: " + e.getMessage());
            }
        }
    }
    
    /**
     * Import VMs from CSV
     */
    public static List<VirtualMachine> importVMsFromCSV(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import VMs from CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            List<VirtualMachine> vms = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine(); // Skip header
                
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 8) {
                        VirtualMachine vm = new VirtualMachine(
                            Integer.parseInt(parts[0].trim()),     // vmId
                            Double.parseDouble(parts[1].trim()),   // mips
                            Double.parseDouble(parts[2].trim()),   // costPerSecond
                            Double.parseDouble(parts[3].trim()),   // networkLatency
                            Integer.parseInt(parts[4].trim()),     // pes
                            Long.parseLong(parts[5].trim()),       // ram
                            Long.parseLong(parts[6].trim()),       // bandwidth
                            Long.parseLong(parts[7].trim())        // storage
                        );
                        vms.add(vm);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                throw new RuntimeException("Failed to import VMs: " + e.getMessage());
            }
            return vms;
        }
        return null;
    }
    
    /**
     * Configuration data wrapper for JSON serialization
     */
    public static class ConfigurationData {
        public double alpha;
        public double beta;
        public boolean enableDeadlineConstraint;
        public boolean enableBudgetConstraint;
        public double maxDeadline;
        public double maxBudget;
        public List<TaskData> tasks;
        public List<VMData> vms;
        public String version = "1.0";
        public long timestamp = System.currentTimeMillis();
        
        public ConfigurationData() {}
        
        public ConfigurationData(SimulationConfig config, List<CloudTask> tasks, List<VirtualMachine> vms) {
            this.alpha = config.getAlpha();
            this.beta = config.getBeta();
            this.enableDeadlineConstraint = config.isEnableDeadlineConstraint();
            this.enableBudgetConstraint = config.isEnableBudgetConstraint();
            this.maxDeadline = config.getMaxDeadline();
            this.maxBudget = config.getMaxBudget();
            
            this.tasks = new ArrayList<>();
            for (CloudTask t : tasks) {
                this.tasks.add(new TaskData(t));
            }
            
            this.vms = new ArrayList<>();
            for (VirtualMachine vm : vms) {
                this.vms.add(new VMData(vm));
            }
        }
        
        public List<CloudTask> getTasks() {
            List<CloudTask> result = new ArrayList<>();
            if (tasks != null) {
                for (TaskData td : tasks) {
                    result.add(td.toCloudTask());
                }
            }
            return result;
        }
        
        public List<VirtualMachine> getVMs() {
            List<VirtualMachine> result = new ArrayList<>();
            if (vms != null) {
                for (VMData vd : vms) {
                    result.add(vd.toVM());
                }
            }
            return result;
        }
    }
    
    public static class TaskData {
        public int taskId;
        public long taskLength;
        public double deadline;
        public double budget;
        public int priority;
        
        public TaskData() {}
        
        public TaskData(CloudTask task) {
            this.taskId = task.getTaskId();
            this.taskLength = task.getTaskLength();
            this.deadline = task.getDeadline();
            this.budget = task.getBudget();
            this.priority = task.getPriority();
        }
        
        public CloudTask toCloudTask() {
            return new CloudTask(taskId, taskLength, deadline, budget, priority);
        }
    }
    
    public static class VMData {
        public int vmId;
        public double mips;
        public double costPerSecond;
        public double networkLatency;
        public int pes;
        public long ram;
        public long bandwidth;
        public long storage;
        
        public VMData() {}
        
        public VMData(VirtualMachine vm) {
            this.vmId = vm.getVmId();
            this.mips = vm.getMips();
            this.costPerSecond = vm.getCostPerSecond();
            this.networkLatency = vm.getNetworkLatency();
            this.pes = vm.getNumberOfPes();
            this.ram = vm.getRam();
            this.bandwidth = vm.getBandwidth();
            this.storage = vm.getStorage();
        }
        
        public VirtualMachine toVM() {
            return new VirtualMachine(vmId, mips, costPerSecond, networkLatency, pes, ram, bandwidth, storage);
        }
    }
}
