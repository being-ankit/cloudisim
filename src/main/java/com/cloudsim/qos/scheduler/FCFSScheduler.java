package com.cloudsim.qos.scheduler;

import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.model.VirtualMachine;

import java.util.*;

/**
 * Module 5: Baseline Scheduling Module - First Come First Serve (FCFS)
 * 
 * This module implements the FCFS scheduling algorithm used for comparison
 * with the proposed QoS-aware approach. Tasks are assigned to VMs in the
 * order they arrive, using round-robin VM selection.
 */
public class FCFSScheduler implements TaskScheduler {
    
    private List<SchedulingResult> results;
    private Map<Integer, SchedulingResult> resultMap;
    private Map<Integer, Double> vmLoadTime;
    
    public FCFSScheduler() {
        this.results = new ArrayList<>();
        this.resultMap = new HashMap<>();
        this.vmLoadTime = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "First Come First Serve (FCFS)";
    }
    
    @Override
    public String getDescription() {
        return "Assigns tasks to VMs in arrival order using round-robin VM selection. " +
               "Does not consider QoS constraints.";
    }
    
    @Override
    public Map<Integer, SchedulingResult> schedule(List<CloudTask> tasks, List<VirtualMachine> vms) {
        reset();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("FCFS SCHEDULING");
        System.out.println("=".repeat(60));
        System.out.println("Algorithm: " + getName());
        System.out.println("Tasks: " + tasks.size() + ", VMs: " + vms.size());
        
        // Initialize VM load tracking
        for (VirtualMachine vm : vms) {
            vmLoadTime.put(vm.getVmId(), 0.0);
        }
        
        int vmIndex = 0;
        
        // Process tasks in order (FCFS)
        for (CloudTask task : tasks) {
            // Round-robin VM selection
            VirtualMachine selectedVM = vms.get(vmIndex);
            vmIndex = (vmIndex + 1) % vms.size();
            
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
        
        System.out.println("\n--- FCFS Summary ---");
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
}
