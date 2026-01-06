package com.cloudsim.qos.scheduler;

import com.cloudsim.qos.config.SimulationConfig;
import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.model.VirtualMachine;
import com.cloudsim.qos.profiling.TaskProfilingModule;

import java.util.*;

/**
 * Module 4: QoS-Aware Scheduling Engine
 * 
 * This is the core module that implements the multi-objective task scheduling
 * algorithm considering QoS parameters such as latency, cost, and deadline.
 * 
 * Objective Function: QoS Score = α × Total Execution Time + β × Execution Cost
 * Where α and β are weighting factors.
 */
public class QoSAwareScheduler implements TaskScheduler {
    
    private TaskProfilingModule profilingModule;
    private SimulationConfig config;
    private List<SchedulingResult> results;
    private Map<Integer, SchedulingResult> resultMap;
    
    // VM load tracking for load balancing
    private Map<Integer, Double> vmLoadTime;
    private Map<Integer, Integer> vmTaskCount;
    
    // Statistics
    private int totalTasksScheduled;
    private int tasksWithQoSSatisfied;
    private int deadlinesMet;
    private int budgetsMet;
    
    public QoSAwareScheduler(SimulationConfig config) {
        this.config = config;
        this.profilingModule = new TaskProfilingModule();
        this.results = new ArrayList<>();
        this.resultMap = new HashMap<>();
        this.vmLoadTime = new HashMap<>();
        this.vmTaskCount = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "QoS-Aware Multi-Objective Scheduler";
    }
    
    @Override
    public String getDescription() {
        return "Schedules tasks based on QoS score considering deadline, budget, and latency constraints. " +
               "Uses weighted objective function: QoS Score = α×Time + β×Cost (α=" + 
               config.getAlpha() + ", β=" + config.getBeta() + ")";
    }
    
    @Override
    public Map<Integer, SchedulingResult> schedule(List<CloudTask> tasks, List<VirtualMachine> vms) {
        reset();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("QoS-AWARE SCHEDULING ENGINE");
        System.out.println("=".repeat(60));
        System.out.println("Algorithm: " + getName());
        System.out.println("Tasks: " + tasks.size() + ", VMs: " + vms.size());
        System.out.println("Weights: α=" + config.getAlpha() + " (time), β=" + config.getBeta() + " (cost)");
        
        // Initialize profiling
        profilingModule.initialize(tasks, vms);
        profilingModule.profileAllTasks();
        profilingModule.calculateQoSScores(config.getAlpha(), config.getBeta());
        
        // Initialize VM load tracking
        for (VirtualMachine vm : vms) {
            vmLoadTime.put(vm.getVmId(), 0.0);
            vmTaskCount.put(vm.getVmId(), 0);
        }
        
        // Sort tasks by priority (higher priority first) then by deadline (earliest first)
        List<CloudTask> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort((t1, t2) -> {
            int priorityCompare = Integer.compare(t2.getPriority(), t1.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            return Double.compare(t1.getDeadline(), t2.getDeadline());
        });
        
        System.out.println("\nScheduling tasks in priority order...\n");
        
        // Schedule each task
        for (CloudTask task : sortedTasks) {
            int taskIndex = findTaskIndex(tasks, task);
            SchedulingResult result = scheduleTask(taskIndex, task, vms);
            
            if (result != null) {
                results.add(result);
                resultMap.put(task.getTaskId(), result);
                updateStatistics(result);
                
                // Update VM load
                VirtualMachine assignedVM = result.getVm();
                double newLoad = vmLoadTime.get(assignedVM.getVmId()) + result.getTotalTime();
                vmLoadTime.put(assignedVM.getVmId(), newLoad);
                vmTaskCount.put(assignedVM.getVmId(), vmTaskCount.get(assignedVM.getVmId()) + 1);
            }
        }
        
        printSchedulingResults();
        return resultMap;
    }
    
    /**
     * Schedules a single task using QoS-aware algorithm.
     */
    private SchedulingResult scheduleTask(int taskIndex, CloudTask task, List<VirtualMachine> vms) {
        System.out.println("Scheduling Task " + task.getTaskId() + 
                " (Length: " + task.getTaskLength() + " MI, Deadline: " + task.getDeadline() + 
                "s, Budget: $" + task.getBudget() + ", Priority: " + task.getPriority() + ")");
        
        // Find best VM considering QoS score and current load
        int bestVMIndex = -1;
        double bestScore = Double.MAX_VALUE;
        
        // First pass: try to find a feasible VM
        for (int v = 0; v < vms.size(); v++) {
            VirtualMachine vm = vms.get(v);
            
            // Check feasibility
            boolean deadlineFeasible = profilingModule.canMeetDeadline(taskIndex, v);
            boolean budgetFeasible = profilingModule.canMeetBudget(taskIndex, v);
            
            if (config.isEnableDeadlineConstraint() && !deadlineFeasible) {
                continue;
            }
            if (config.isEnableBudgetConstraint() && !budgetFeasible) {
                continue;
            }
            
            // Calculate combined score with load balancing
            double qosScore = profilingModule.getQoSScore(taskIndex, v);
            double loadFactor = 1.0 + (vmLoadTime.get(vm.getVmId()) / 100.0);  // Penalize loaded VMs
            double combinedScore = qosScore * loadFactor;
            
            if (combinedScore < bestScore) {
                bestScore = combinedScore;
                bestVMIndex = v;
            }
        }
        
        // Second pass: if no feasible VM found, relax constraints
        if (bestVMIndex < 0) {
            System.out.println("  -> No feasible VM found, relaxing constraints...");
            
            for (int v = 0; v < vms.size(); v++) {
                VirtualMachine vm = vms.get(v);
                double qosScore = profilingModule.getQoSScore(taskIndex, v);
                double loadFactor = 1.0 + (vmLoadTime.get(vm.getVmId()) / 100.0);
                double combinedScore = qosScore * loadFactor;
                
                if (combinedScore < bestScore) {
                    bestScore = combinedScore;
                    bestVMIndex = v;
                }
            }
        }
        
        // Create scheduling result
        if (bestVMIndex >= 0) {
            VirtualMachine bestVM = vms.get(bestVMIndex);
            SchedulingResult result = new SchedulingResult(task, bestVM);
            result.calculateQoSScore(config.getAlpha(), config.getBeta());
            
            // Set timing information
            double startTime = vmLoadTime.get(bestVM.getVmId());
            result.setStartTime(startTime);
            result.setFinishTime(startTime + result.getTotalTime());
            
            // Update task with assignment info
            task.setAssignedVmId(bestVM.getVmId());
            task.setEstimatedExecutionTime(result.getTotalTime());
            task.setEstimatedCost(result.getCost());
            task.setDeadlineMet(result.isDeadlineSatisfied());
            task.setBudgetMet(result.isBudgetSatisfied());
            
            System.out.println("  -> Assigned to VM " + bestVM.getVmId() + 
                    " (Time: " + String.format("%.4f", result.getTotalTime()) + 
                    "s, Cost: $" + String.format("%.4f", result.getCost()) +
                    ", QoS: " + (result.isQoSSatisfied() ? "Satisfied" : "Violated") + ")");
            
            return result;
        }
        
        System.out.println("  -> FAILED to schedule task!");
        return null;
    }
    
    /**
     * Finds the index of a task in the original list.
     */
    private int findTaskIndex(List<CloudTask> tasks, CloudTask target) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getTaskId() == target.getTaskId()) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Updates scheduling statistics.
     */
    private void updateStatistics(SchedulingResult result) {
        totalTasksScheduled++;
        if (result.isQoSSatisfied()) {
            tasksWithQoSSatisfied++;
        }
        if (result.isDeadlineSatisfied()) {
            deadlinesMet++;
        }
        if (result.isBudgetSatisfied()) {
            budgetsMet++;
        }
    }
    
    /**
     * Prints the scheduling results summary.
     */
    private void printSchedulingResults() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("SCHEDULING SUMMARY");
        System.out.println("-".repeat(60));
        
        System.out.println("Total Tasks Scheduled: " + totalTasksScheduled);
        System.out.println("QoS Satisfied: " + tasksWithQoSSatisfied + 
                " (" + String.format("%.1f%%", (tasksWithQoSSatisfied * 100.0 / totalTasksScheduled)) + ")");
        System.out.println("Deadlines Met: " + deadlinesMet +
                " (" + String.format("%.1f%%", (deadlinesMet * 100.0 / totalTasksScheduled)) + ")");
        System.out.println("Budgets Met: " + budgetsMet +
                " (" + String.format("%.1f%%", (budgetsMet * 100.0 / totalTasksScheduled)) + ")");
        
        // Calculate totals
        double totalTime = 0, totalCost = 0;
        for (SchedulingResult result : results) {
            totalTime += result.getTotalTime();
            totalCost += result.getCost();
        }
        
        System.out.println("\nTotal Execution Time: " + String.format("%.4f", totalTime) + " seconds");
        System.out.println("Total Cost: $" + String.format("%.4f", totalCost));
        System.out.println("Average Time per Task: " + String.format("%.4f", totalTime / totalTasksScheduled) + " seconds");
        System.out.println("Average Cost per Task: $" + String.format("%.4f", totalCost / totalTasksScheduled));
        
        // VM utilization
        System.out.println("\nVM Utilization:");
        for (Map.Entry<Integer, Double> entry : vmLoadTime.entrySet()) {
            int tasks = vmTaskCount.get(entry.getKey());
            System.out.println("  VM " + entry.getKey() + ": " + tasks + " tasks, " +
                    String.format("%.4f", entry.getValue()) + "s total time");
        }
        
        System.out.println("=".repeat(60) + "\n");
    }
    
    @Override
    public List<SchedulingResult> getResults() {
        return new ArrayList<>(results);
    }
    
    @Override
    public void reset() {
        results.clear();
        resultMap.clear();
        vmLoadTime.clear();
        vmTaskCount.clear();
        totalTasksScheduled = 0;
        tasksWithQoSSatisfied = 0;
        deadlinesMet = 0;
        budgetsMet = 0;
    }
    
    // Getters for statistics
    public int getTotalTasksScheduled() {
        return totalTasksScheduled;
    }
    
    public int getTasksWithQoSSatisfied() {
        return tasksWithQoSSatisfied;
    }
    
    public int getDeadlinesMet() {
        return deadlinesMet;
    }
    
    public int getBudgetsMet() {
        return budgetsMet;
    }
    
    public double getQoSSatisfactionRate() {
        return totalTasksScheduled > 0 ? (tasksWithQoSSatisfied * 100.0 / totalTasksScheduled) : 0;
    }
    
    public double getDeadlineMissRate() {
        return totalTasksScheduled > 0 ? ((totalTasksScheduled - deadlinesMet) * 100.0 / totalTasksScheduled) : 0;
    }
    
    public TaskProfilingModule getProfilingModule() {
        return profilingModule;
    }
}
