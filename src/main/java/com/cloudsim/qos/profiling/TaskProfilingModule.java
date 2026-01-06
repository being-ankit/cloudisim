package com.cloudsim.qos.profiling;

import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.VirtualMachine;

import java.util.*;

/**
 * Module 3: Task Profiling Module
 * 
 * This module analyzes tasks and estimates their execution requirements
 * on different virtual machines. It calculates execution time, cost,
 * and creates a profile matrix for scheduling decisions.
 */
public class TaskProfilingModule {
    
    private List<CloudTask> tasks;
    private List<VirtualMachine> vms;
    
    // Profile matrices
    private double[][] executionTimeMatrix;   // [task][vm] -> execution time
    private double[][] totalTimeMatrix;       // [task][vm] -> total time (with latency)
    private double[][] executionCostMatrix;   // [task][vm] -> execution cost
    private double[][] qosScoreMatrix;        // [task][vm] -> QoS score
    
    // Task-VM feasibility
    private boolean[][] deadlineFeasible;     // [task][vm] -> can meet deadline
    private boolean[][] budgetFeasible;       // [task][vm] -> can meet budget
    
    public TaskProfilingModule() {
        this.tasks = new ArrayList<>();
        this.vms = new ArrayList<>();
    }
    
    /**
     * Constructor that initializes with tasks and VMs.
     * @param tasks List of tasks to profile
     * @param vms List of available VMs
     */
    public TaskProfilingModule(List<CloudTask> tasks, List<VirtualMachine> vms) {
        this();
        initialize(tasks, vms);
    }
    
    /**
     * Initializes the profiling module with tasks and VMs.
     * @param tasks List of tasks to profile
     * @param vms List of available VMs
     */
    public void initialize(List<CloudTask> tasks, List<VirtualMachine> vms) {
        this.tasks = new ArrayList<>(tasks);
        this.vms = new ArrayList<>(vms);
        
        int numTasks = tasks.size();
        int numVMs = vms.size();
        
        // Initialize matrices
        executionTimeMatrix = new double[numTasks][numVMs];
        totalTimeMatrix = new double[numTasks][numVMs];
        executionCostMatrix = new double[numTasks][numVMs];
        qosScoreMatrix = new double[numTasks][numVMs];
        deadlineFeasible = new boolean[numTasks][numVMs];
        budgetFeasible = new boolean[numTasks][numVMs];
        
        System.out.println("Task Profiling Module initialized with " + 
                numTasks + " tasks and " + numVMs + " VMs.");
    }
    
    /**
     * Performs complete profiling of all task-VM combinations.
     */
    public void profileAllTasks() {
        System.out.println("\nProfiling all task-VM combinations...");
        
        for (int t = 0; t < tasks.size(); t++) {
            CloudTask task = tasks.get(t);
            
            for (int v = 0; v < vms.size(); v++) {
                VirtualMachine vm = vms.get(v);
                profileTaskOnVM(t, task, v, vm);
            }
        }
        
        System.out.println("Profiling complete.");
    }
    
    /**
     * Profiles a single task on a single VM.
     */
    private void profileTaskOnVM(int taskIndex, CloudTask task, int vmIndex, VirtualMachine vm) {
        // Calculate execution time: Task Length / VM MIPS
        double executionTime = (double) task.getTaskLength() / vm.getMips();
        executionTimeMatrix[taskIndex][vmIndex] = executionTime;
        
        // Calculate total time: Execution Time + Network Latency
        double totalTime = executionTime + vm.getNetworkLatency();
        totalTimeMatrix[taskIndex][vmIndex] = totalTime;
        
        // Calculate execution cost: Execution Time × Cost per Second
        double executionCost = executionTime * vm.getCostPerSecond();
        executionCostMatrix[taskIndex][vmIndex] = executionCost;
        
        // Check deadline feasibility
        deadlineFeasible[taskIndex][vmIndex] = totalTime <= task.getDeadline();
        
        // Check budget feasibility
        budgetFeasible[taskIndex][vmIndex] = executionCost <= task.getBudget();
    }
    
    /**
     * Calculates QoS scores for all task-VM combinations.
     * @param alpha Weight for execution time (0-1)
     * @param beta Weight for cost (0-1)
     */
    public void calculateQoSScores(double alpha, double beta) {
        System.out.println("Calculating QoS scores (alpha=" + alpha + ", beta=" + beta + ")...");
        
        // Find max values for normalization
        double maxTime = findMaxTime();
        double maxCost = findMaxCost();
        
        for (int t = 0; t < tasks.size(); t++) {
            CloudTask task = tasks.get(t);
            
            for (int v = 0; v < vms.size(); v++) {
                // Normalize values
                double normalizedTime = totalTimeMatrix[t][v] / maxTime;
                double normalizedCost = executionCostMatrix[t][v] / maxCost;
                
                // Calculate base QoS score (lower is better)
                double qosScore = alpha * normalizedTime + beta * normalizedCost;
                
                // Add penalties for constraint violations
                if (!deadlineFeasible[t][v]) {
                    double violation = (totalTimeMatrix[t][v] - task.getDeadline()) / task.getDeadline();
                    qosScore += violation * 10;  // Heavy penalty
                }
                
                if (!budgetFeasible[t][v]) {
                    double violation = (executionCostMatrix[t][v] - task.getBudget()) / task.getBudget();
                    qosScore += violation * 10;  // Heavy penalty
                }
                
                // Consider task priority (higher priority = lower score modifier)
                double priorityModifier = (11 - task.getPriority()) / 10.0;
                qosScore *= priorityModifier;
                
                qosScoreMatrix[t][v] = qosScore;
            }
        }
    }
    
    /**
     * Finds the maximum total time in the matrix.
     */
    private double findMaxTime() {
        double max = 0;
        for (double[] row : totalTimeMatrix) {
            for (double time : row) {
                max = Math.max(max, time);
            }
        }
        return max > 0 ? max : 1;
    }
    
    /**
     * Finds the maximum cost in the matrix.
     */
    private double findMaxCost() {
        double max = 0;
        for (double[] row : executionCostMatrix) {
            for (double cost : row) {
                max = Math.max(max, cost);
            }
        }
        return max > 0 ? max : 1;
    }
    
    /**
     * Gets the estimated execution time for a task-VM pair.
     */
    public double getExecutionTime(int taskIndex, int vmIndex) {
        return executionTimeMatrix[taskIndex][vmIndex];
    }
    
    /**
     * Gets the total time (including latency) for a task-VM pair.
     */
    public double getTotalTime(int taskIndex, int vmIndex) {
        return totalTimeMatrix[taskIndex][vmIndex];
    }
    
    /**
     * Gets the execution cost for a task-VM pair.
     */
    public double getExecutionCost(int taskIndex, int vmIndex) {
        return executionCostMatrix[taskIndex][vmIndex];
    }
    
    /**
     * Gets the QoS score for a task-VM pair.
     */
    public double getQoSScore(int taskIndex, int vmIndex) {
        return qosScoreMatrix[taskIndex][vmIndex];
    }
    
    /**
     * Checks if a task can meet its deadline on a VM.
     */
    public boolean canMeetDeadline(int taskIndex, int vmIndex) {
        return deadlineFeasible[taskIndex][vmIndex];
    }
    
    /**
     * Checks if a task can meet its budget on a VM.
     */
    public boolean canMeetBudget(int taskIndex, int vmIndex) {
        return budgetFeasible[taskIndex][vmIndex];
    }
    
    /**
     * Checks if both QoS constraints are satisfied.
     */
    public boolean isQoSFeasible(int taskIndex, int vmIndex) {
        return deadlineFeasible[taskIndex][vmIndex] && budgetFeasible[taskIndex][vmIndex];
    }
    
    /**
     * Finds the best VM for a task based on QoS score.
     * @param taskIndex Index of the task
     * @param requireFeasible If true, only considers feasible VMs
     * @return Index of the best VM, or -1 if no feasible VM found
     */
    public int findBestVM(int taskIndex, boolean requireFeasible) {
        int bestVM = -1;
        double bestScore = Double.MAX_VALUE;
        
        for (int v = 0; v < vms.size(); v++) {
            if (requireFeasible && !isQoSFeasible(taskIndex, v)) {
                continue;
            }
            
            if (qosScoreMatrix[taskIndex][v] < bestScore) {
                bestScore = qosScoreMatrix[taskIndex][v];
                bestVM = v;
            }
        }
        
        return bestVM;
    }
    
    /**
     * Finds all feasible VMs for a task.
     */
    public List<Integer> findFeasibleVMs(int taskIndex) {
        List<Integer> feasible = new ArrayList<>();
        for (int v = 0; v < vms.size(); v++) {
            if (isQoSFeasible(taskIndex, v)) {
                feasible.add(v);
            }
        }
        return feasible;
    }
    
    /**
     * Gets the profile for a specific task.
     */
    public TaskProfile getTaskProfile(int taskIndex) {
        CloudTask task = tasks.get(taskIndex);
        TaskProfile profile = new TaskProfile(task);
        
        for (int v = 0; v < vms.size(); v++) {
            VMProfile vmProfile = new VMProfile();
            vmProfile.vmIndex = v;
            vmProfile.vmId = vms.get(v).getVmId();
            vmProfile.executionTime = executionTimeMatrix[taskIndex][v];
            vmProfile.totalTime = totalTimeMatrix[taskIndex][v];
            vmProfile.cost = executionCostMatrix[taskIndex][v];
            vmProfile.qosScore = qosScoreMatrix[taskIndex][v];
            vmProfile.deadlineFeasible = deadlineFeasible[taskIndex][v];
            vmProfile.budgetFeasible = budgetFeasible[taskIndex][v];
            
            profile.vmProfiles.add(vmProfile);
        }
        
        return profile;
    }
    
    /**
     * Prints the profiling results.
     */
    public void printProfilingResults() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TASK PROFILING RESULTS");
        System.out.println("=".repeat(80));
        
        // Print execution time matrix
        System.out.println("\nExecution Time Matrix (seconds):");
        printMatrix(executionTimeMatrix, "%.4f");
        
        // Print total time matrix
        System.out.println("\nTotal Time Matrix (with latency, seconds):");
        printMatrix(totalTimeMatrix, "%.4f");
        
        // Print cost matrix
        System.out.println("\nExecution Cost Matrix ($):");
        printMatrix(executionCostMatrix, "%.4f");
        
        // Print QoS score matrix
        System.out.println("\nQoS Score Matrix (lower is better):");
        printMatrix(qosScoreMatrix, "%.4f");
        
        // Print feasibility summary
        System.out.println("\nFeasibility Summary:");
        for (int t = 0; t < tasks.size(); t++) {
            CloudTask task = tasks.get(t);
            List<Integer> feasibleVMs = findFeasibleVMs(t);
            int bestVM = findBestVM(t, true);
            
            System.out.println(String.format("  Task %d: %d feasible VM(s), Best VM: %s",
                    task.getTaskId(), feasibleVMs.size(),
                    bestVM >= 0 ? "VM " + vms.get(bestVM).getVmId() : "None"));
        }
        
        System.out.println("=".repeat(80) + "\n");
    }
    
    /**
     * Prints a matrix with headers.
     */
    private void printMatrix(double[][] matrix, String format) {
        // Print VM headers
        System.out.print("          ");
        for (int v = 0; v < vms.size(); v++) {
            System.out.print(String.format("VM%-8d", vms.get(v).getVmId()));
        }
        System.out.println();
        
        // Print rows
        for (int t = 0; t < tasks.size(); t++) {
            System.out.print(String.format("Task %-4d ", tasks.get(t).getTaskId()));
            for (int v = 0; v < vms.size(); v++) {
                System.out.print(String.format(format + "  ", matrix[t][v]));
            }
            System.out.println();
        }
    }
    
    /**
     * Inner class to hold task profile information.
     */
    public static class TaskProfile {
        public CloudTask task;
        public List<VMProfile> vmProfiles = new ArrayList<>();
        
        public TaskProfile(CloudTask task) {
            this.task = task;
        }
    }
    
    /**
     * Inner class to hold VM-specific profile for a task.
     */
    public static class VMProfile {
        public int vmIndex;
        public int vmId;
        public double executionTime;
        public double totalTime;
        public double cost;
        public double qosScore;
        public boolean deadlineFeasible;
        public boolean budgetFeasible;
        
        public boolean isFeasible() {
            return deadlineFeasible && budgetFeasible;
        }
    }
}
