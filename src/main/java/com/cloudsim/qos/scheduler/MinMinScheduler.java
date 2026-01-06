package com.cloudsim.qos.scheduler;

import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.model.VirtualMachine;

import java.util.*;

/**
 * Module 5: Baseline Scheduling Module - Min-Min Scheduler
 * 
 * This module implements the Min-Min scheduling algorithm.
 * It assigns tasks to VMs by selecting the task with minimum execution time
 * and assigning it to the VM that gives minimum completion time.
 */
public class MinMinScheduler implements TaskScheduler {
    
    private List<SchedulingResult> results;
    private Map<Integer, SchedulingResult> resultMap;
    private Map<Integer, Double> vmCompletionTime;
    
    public MinMinScheduler() {
        this.results = new ArrayList<>();
        this.resultMap = new HashMap<>();
        this.vmCompletionTime = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "Min-Min Scheduling";
    }
    
    @Override
    public String getDescription() {
        return "Assigns tasks by selecting the task with minimum execution time " +
               "and mapping it to the VM that gives minimum completion time.";
    }
    
    @Override
    public Map<Integer, SchedulingResult> schedule(List<CloudTask> tasks, List<VirtualMachine> vms) {
        reset();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("MIN-MIN SCHEDULING");
        System.out.println("=".repeat(60));
        System.out.println("Algorithm: " + getName());
        System.out.println("Tasks: " + tasks.size() + ", VMs: " + vms.size());
        
        // Initialize VM completion times
        for (VirtualMachine vm : vms) {
            vmCompletionTime.put(vm.getVmId(), 0.0);
        }
        
        // Create a list of unscheduled tasks
        List<CloudTask> unscheduledTasks = new ArrayList<>(tasks);
        
        while (!unscheduledTasks.isEmpty()) {
            // Find task-VM pair with minimum completion time
            CloudTask bestTask = null;
            VirtualMachine bestVM = null;
            double minCompletionTime = Double.MAX_VALUE;
            
            for (CloudTask task : unscheduledTasks) {
                for (VirtualMachine vm : vms) {
                    double executionTime = vm.calculateTotalTime(task.getTaskLength());
                    double completionTime = vmCompletionTime.get(vm.getVmId()) + executionTime;
                    
                    if (completionTime < minCompletionTime) {
                        minCompletionTime = completionTime;
                        bestTask = task;
                        bestVM = vm;
                    }
                }
            }
            
            if (bestTask != null && bestVM != null) {
                // Create scheduling result
                SchedulingResult result = new SchedulingResult(bestTask, bestVM);
                
                // Set timing information
                double startTime = vmCompletionTime.get(bestVM.getVmId());
                result.setStartTime(startTime);
                result.setFinishTime(minCompletionTime);
                
                // Update VM completion time
                vmCompletionTime.put(bestVM.getVmId(), minCompletionTime);
                
                // Update task
                bestTask.setAssignedVmId(bestVM.getVmId());
                bestTask.setEstimatedExecutionTime(result.getTotalTime());
                bestTask.setEstimatedCost(result.getCost());
                bestTask.setDeadlineMet(result.isDeadlineSatisfied());
                bestTask.setBudgetMet(result.isBudgetSatisfied());
                
                results.add(result);
                resultMap.put(bestTask.getTaskId(), result);
                unscheduledTasks.remove(bestTask);
                
                System.out.println("Task " + bestTask.getTaskId() + " -> VM " + bestVM.getVmId() +
                        " (Time: " + String.format("%.4f", result.getTotalTime()) + 
                        "s, Cost: $" + String.format("%.4f", result.getCost()) + ")");
            }
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
        
        System.out.println("\n--- Min-Min Summary ---");
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
        vmCompletionTime.clear();
    }
}
