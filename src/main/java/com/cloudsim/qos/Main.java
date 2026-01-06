package com.cloudsim.qos;

import com.cloudsim.qos.config.InputConfigurationModule;
import com.cloudsim.qos.config.SimulationConfig;
import com.cloudsim.qos.evaluation.PerformanceEvaluationModule;
import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.model.VirtualMachine;
import com.cloudsim.qos.scheduler.*;
import com.cloudsim.qos.visualization.ReportGenerationModule;
import com.cloudsim.qos.visualization.ResultVisualizationModule;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.*;

/**
 * Main Simulation Runner
 * 
 * This is the main entry point for the QoS-Aware Multi-Objective 
 * Task Scheduling Simulator for Cloud Computing.
 * 
 * It orchestrates all modules and runs the complete simulation.
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║     QoS-Aware Multi-Objective Task Scheduling Simulator          ║");
        System.out.println("║                    for Cloud Computing                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        try {
            // Parse command line arguments
            SimulationMode mode = parseArguments(args);
            
            // Run simulation based on mode
            switch (mode) {
                case DEMO:
                    runDemoSimulation();
                    break;
                case INTERACTIVE:
                    runInteractiveSimulation();
                    break;
                case FILE:
                    runFileBasedSimulation(args);
                    break;
                case SCALABILITY_TEST:
                    runScalabilityTest();
                    return; // GUI runs on its own thread
                default:
                    runDemoSimulation();
            }
            
        } catch (Exception e) {
            System.err.println("Simulation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Parses command line arguments.
     */
    private static SimulationMode parseArguments(String[] args) {
        if (args.length == 0) {
            return SimulationMode.DEMO;
        }
        
        String arg = args[0].toLowerCase();
        switch (arg) {
            case "-d":
            case "--demo":
                return SimulationMode.DEMO;
            case "-i":
            case "--interactive":
                return SimulationMode.INTERACTIVE;
            case "-f":
            case "--file":
                return SimulationMode.FILE;
            case "-s":
            case "--scalability":
                return SimulationMode.SCALABILITY_TEST;
            case "-h":
            case "--help":
                printHelp();
                System.exit(0);
                return SimulationMode.DEMO; // unreachable but needed for compilation
            default:
                return SimulationMode.DEMO;
        }
    }
    
    /**
     * Prints help information.
     */
    private static void printHelp() {
        System.out.println("Usage: java -jar qos-task-scheduler.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -d, --demo          Run demo simulation with sample data (default)");
        System.out.println("  -i, --interactive   Run interactive mode for manual input");
        System.out.println("  -f, --file <dir>    Load configuration from files");
        System.out.println("  -s, --scalability   Run scalability tests");
        System.out.println("  -g, --gui           Launch graphical user interface");
        System.out.println("  -h, --help          Show this help message");
    }
    
    /**
     * Runs the demo simulation with sample data.
     */
    private static void runDemoSimulation() throws IOException {
        System.out.println("Running Demo Simulation...\n");
        
        // Module 1: Configuration
        InputConfigurationModule configModule = new InputConfigurationModule();
        
        // Generate sample configuration
        int numTasks = 20;
        int numVMs = 5;
        configModule.generateSampleConfiguration(numTasks, numVMs);
        
        // Set simulation parameters
        SimulationConfig config = new SimulationConfig(0.5, 0.5);  // Equal weights
        config.setOutputDirectory("output");
        config.setEnableVisualization(true);
        configModule.setSimulationConfig(config);
        
        // Print configuration
        configModule.printConfigurationSummary();
        
        // Save configuration for reference
        configModule.saveConfiguration("output/config");
        
        // Run the simulation
        runSimulation(configModule.getTasks(), configModule.getVirtualMachines(), config);
    }
    
    /**
     * Runs interactive simulation with user input.
     */
    private static void runInteractiveSimulation() throws IOException {
        System.out.println("Running Interactive Simulation...\n");
        
        InputConfigurationModule configModule = new InputConfigurationModule();
        configModule.collectInputInteractively();
        configModule.printConfigurationSummary();
        
        runSimulation(configModule.getTasks(), configModule.getVirtualMachines(), 
                configModule.getSimulationConfig());
    }
    
    /**
     * Runs simulation from configuration files.
     */
    private static void runFileBasedSimulation(String[] args) throws IOException {
        String configDir = args.length > 1 ? args[1] : "config";
        
        System.out.println("Loading configuration from: " + configDir + "\n");
        
        InputConfigurationModule configModule = new InputConfigurationModule();
        configModule.loadFromFiles(
                configDir + "/tasks.json",
                configDir + "/vms.json",
                configDir + "/config.json"
        );
        
        configModule.printConfigurationSummary();
        
        runSimulation(configModule.getTasks(), configModule.getVirtualMachines(),
                configModule.getSimulationConfig());
    }
    
    /**
     * Runs scalability tests with varying task and VM counts.
     */
    private static void runScalabilityTest() throws IOException {
        System.out.println("Running Scalability Test...\n");
        
        int[] taskCounts = {10, 20, 50, 100, 200};
        int[] vmCounts = {3, 5, 10};
        
        SimulationConfig config = new SimulationConfig(0.5, 0.5);
        config.setOutputDirectory("output/scalability");
        
        System.out.println(String.format("%-10s %-8s %-15s %-15s %-15s",
                "Tasks", "VMs", "QoS Time(ms)", "FCFS Time(ms)", "Random Time(ms)"));
        System.out.println("-".repeat(70));
        
        for (int numTasks : taskCounts) {
            for (int numVMs : vmCounts) {
                InputConfigurationModule configModule = new InputConfigurationModule();
                configModule.generateSampleConfiguration(numTasks, numVMs);
                
                List<CloudTask> tasks = configModule.getTasks();
                List<VirtualMachine> vms = configModule.getVirtualMachines();
                
                // Time QoS scheduler
                long start = System.currentTimeMillis();
                QoSAwareScheduler qosScheduler = new QoSAwareScheduler(config);
                qosScheduler.schedule(copyTasks(tasks), copyVMs(vms));
                long qosTime = System.currentTimeMillis() - start;
                
                // Time FCFS scheduler
                start = System.currentTimeMillis();
                FCFSScheduler fcfsScheduler = new FCFSScheduler();
                fcfsScheduler.schedule(copyTasks(tasks), copyVMs(vms));
                long fcfsTime = System.currentTimeMillis() - start;
                
                // Time Random scheduler
                start = System.currentTimeMillis();
                RandomScheduler randomScheduler = new RandomScheduler(42);
                randomScheduler.schedule(copyTasks(tasks), copyVMs(vms));
                long randomTime = System.currentTimeMillis() - start;
                
                System.out.println(String.format("%-10d %-8d %-15d %-15d %-15d",
                        numTasks, numVMs, qosTime, fcfsTime, randomTime));
            }
        }
    }
    
    /**
     * Main simulation runner that orchestrates all modules.
     */
    private static void runSimulation(List<CloudTask> tasks, List<VirtualMachine> vms,
                                      SimulationConfig config) throws IOException {
        
        String outputDir = config.getOutputDirectory();
        
        // Storage for results
        Map<String, List<SchedulingResult>> allResults = new LinkedHashMap<>();
        
        // Module 6: Performance Evaluation
        PerformanceEvaluationModule evaluator = new PerformanceEvaluationModule();
        
        // ===== Run QoS-Aware Scheduler =====
        System.out.println("\n" + "▓".repeat(60));
        System.out.println("Running QoS-Aware Scheduler...");
        System.out.println("▓".repeat(60));
        
        QoSAwareScheduler qosScheduler = new QoSAwareScheduler(config);
        qosScheduler.schedule(copyTasks(tasks), copyVMs(vms));
        allResults.put(qosScheduler.getName(), qosScheduler.getResults());
        evaluator.evaluate(qosScheduler, qosScheduler.getResults());
        
        // ===== Run FCFS Scheduler =====
        System.out.println("\n" + "▓".repeat(60));
        System.out.println("Running FCFS Scheduler...");
        System.out.println("▓".repeat(60));
        
        FCFSScheduler fcfsScheduler = new FCFSScheduler();
        fcfsScheduler.schedule(copyTasks(tasks), copyVMs(vms));
        allResults.put(fcfsScheduler.getName(), fcfsScheduler.getResults());
        evaluator.evaluate(fcfsScheduler, fcfsScheduler.getResults());
        
        // ===== Run Random Scheduler =====
        System.out.println("\n" + "▓".repeat(60));
        System.out.println("Running Random Scheduler...");
        System.out.println("▓".repeat(60));
        
        RandomScheduler randomScheduler = new RandomScheduler(42);  // Fixed seed for reproducibility
        randomScheduler.schedule(copyTasks(tasks), copyVMs(vms));
        allResults.put(randomScheduler.getName(), randomScheduler.getResults());
        evaluator.evaluate(randomScheduler, randomScheduler.getResults());
        
        // ===== Run Min-Min Scheduler =====
        System.out.println("\n" + "▓".repeat(60));
        System.out.println("Running Min-Min Scheduler...");
        System.out.println("▓".repeat(60));
        
        MinMinScheduler minMinScheduler = new MinMinScheduler();
        minMinScheduler.schedule(copyTasks(tasks), copyVMs(vms));
        allResults.put(minMinScheduler.getName(), minMinScheduler.getResults());
        evaluator.evaluate(minMinScheduler, minMinScheduler.getResults());
        
        // ===== Performance Analysis =====
        System.out.println("\n" + "▓".repeat(60));
        System.out.println("PERFORMANCE ANALYSIS");
        System.out.println("▓".repeat(60));
        
        evaluator.printComparisonTable();
        evaluator.printDetailedReport();
        evaluator.printImprovementAnalysis(qosScheduler.getName());
        
        // ===== Visualization =====
        System.out.println("\n" + "▓".repeat(60));
        System.out.println("GENERATING VISUALIZATIONS");
        System.out.println("▓".repeat(60));
        
        ResultVisualizationModule visualizer = new ResultVisualizationModule(outputDir + "/charts");
        
        // Generate ASCII charts (always works)
        visualizer.printASCIIComparison(evaluator.getAllMetrics());
        
        // Try to generate graphical charts
        try {
            visualizer.generateAllCharts(evaluator.getAllMetrics());
            visualizer.generateCombinedMetricsChart(evaluator.getAllMetrics());
            
            // Generate task distribution charts for each scheduler
            for (Map.Entry<String, List<SchedulingResult>> entry : allResults.entrySet()) {
                visualizer.generateTaskDistributionChart(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            System.out.println("Note: Could not generate graphical charts. " +
                    "ASCII charts are displayed above.");
        }
        
        // ===== Report Generation =====
        System.out.println("\n" + "▓".repeat(60));
        System.out.println("GENERATING REPORTS");
        System.out.println("▓".repeat(60));
        
        ReportGenerationModule reportGenerator = new ReportGenerationModule(outputDir + "/reports");
        reportGenerator.generateReport(evaluator.getAllMetrics(), allResults, 
                config.getAlpha(), config.getBeta());
        
        // ===== Summary =====
        System.out.println("\n" + "═".repeat(60));
        System.out.println("SIMULATION COMPLETE");
        System.out.println("═".repeat(60));
        System.out.println("\nOutput files saved to: " + outputDir);
        System.out.println("  - Charts: " + outputDir + "/charts/");
        System.out.println("  - Reports: " + outputDir + "/reports/");
        System.out.println("  - Config: " + outputDir + "/config/");
        System.out.println("\nThank you for using QoS-Aware Task Scheduling Simulator!");
    }
    
    /**
     * Creates a copy of tasks list for independent scheduling.
     */
    private static List<CloudTask> copyTasks(List<CloudTask> tasks) {
        List<CloudTask> copy = new ArrayList<>();
        for (CloudTask task : tasks) {
            copy.add(task.copy());
        }
        return copy;
    }
    
    /**
     * Creates a copy of VMs list for independent scheduling.
     */
    private static List<VirtualMachine> copyVMs(List<VirtualMachine> vms) {
        List<VirtualMachine> copy = new ArrayList<>();
        for (VirtualMachine vm : vms) {
            copy.add(vm.copy());
        }
        return copy;
    }
    
    
    
    /**
     * Simulation mode enumeration.
     */
    private enum SimulationMode {
        DEMO,
        INTERACTIVE,
        FILE,
        SCALABILITY_TEST,
    }
}
