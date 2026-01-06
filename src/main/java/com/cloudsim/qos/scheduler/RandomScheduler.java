package com.cloudsim.qos.scheduler;

import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.model.VirtualMachine;

import java.util.*;

/**
 * Module 5: Baseline Scheduling Module - Random Scheduling
 * 
 * This module implements random task scheduling used for comparison
 * with the proposed QoS-aware approach. Tasks are randomly assigned
 * to VMs without considering any optimization criteria.
 */
public class RandomScheduler implements TaskScheduler {
    
    private List<SchedulingResult> results;
    private Map<Integer, SchedulingResult> resultMap;
    private Map<Integer, Double> vmLoadTime;
    private Random random;
    private long seed;
    
    public RandomScheduler() {
        this(System.currentTimeMillis());
    }
    
    public RandomScheduler(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.results = new ArrayList<>();
        this.resultMap = new HashMap<>();
        this.vmLoadTime = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "Random Scheduling";
    }
    
    @Override
    public String getDescription() {
        return "Randomly assigns tasks to VMs without any optimization. " +
               "Does not consider QoS constraints or task requirements.";
    }
    
    @Override
    public Map<Integer, SchedulingResult> schedule(List<CloudTask> tasks, List<VirtualMachine> vms) {
        reset();
        random = new Random(seed);  // Reset random for reproducibility
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RANDOM SCHEDULING");
        System.out.println("=".repeat(60));
        System.out.println("Algorithm: " + getName());
        System.out.println("Tasks: " + tasks.size() + ", VMs: " + vms.size());
        System.out.println("Random Seed: " + seed);
        
        // Initialize VM load tracking
        for (VirtualMachine vm : vms) {
            vmLoadTime.put(vm.getVmId(), 0.0);
        }
        
        // Process tasks
        for (CloudTask task : tasks) {
            // Random VM selection
            int vmIndex = random.nextInt(vms.size());
            VirtualMachine selectedVM = vms.get(vmIndex);
            
            // Create scheduling result
            SchedulingResult result = new SchedulingResult(task, selectedVM);
            
            // Set timing information
            double startTime = vmLoadTime.get(selectedVM.getVmId());
            result.setStartTime(startTime);
            result.setFinishTime(startTime + result.getTotalTime());
            
            // Update VM load
            vmLoadTime.put(selectedVM.getVmId(), result.getFinishTime());
            
            // Update task
            task.setAssignedVmId(selectedVM.getVmId());
            task.setEstimatedExecutionTime(result.getTotalTime());
            task.setEstimatedCost(result.getCost());
            task.setDeadlineMet(result.isDeadlineSatisfied());
            task.setBudgetMet(result.isBudgetSatisfied());
            
            results.add(result);
            resultMap.put(task.getTaskId(), result);
            
            System.out.println("Task " + task.getTaskId() + " -> VM " + selectedVM.getVmId() +
                    " (Time: " + String.format("%.4f", result.getTotalTime()) + 
                    "s, Cost: $" + String.format("%.4f", result.getCost()) + ")");
        }
        
        printSummary();
        return resultMap;
    }
    
    private void printSummary() {
        double totalTime = 0, totalCost = 0;
        int deadlinesMet = 0, budgetsMet = 0;
        
        for (SchedulingResult result : results) {
            totalTime += result.getTotalTime();
            totalCost += result.getCost();
            if (result.isDeadlineSatisfied()) deadlinesMet++;
            if (result.isBudgetSatisfied()) budgetsMet++;
        }
        
        System.out.println("\n--- Random Summary ---");
        System.out.println("Total Time: " + String.format("%.4f", totalTime) + "s");
        System.out.println("Total Cost: $" + String.format("%.4f", totalCost));
        System.out.println("Deadlines Met: " + deadlinesMet + "/" + results.size());
        System.out.println("Budgets Met: " + budgetsMet + "/" + results.size());
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
    }
    
    public void setSeed(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }
}
