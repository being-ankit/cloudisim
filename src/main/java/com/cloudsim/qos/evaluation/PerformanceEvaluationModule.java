package com.cloudsim.qos.evaluation;

import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.scheduler.TaskScheduler;

import java.util.*;

/**
 * Module 6: Performance Evaluation Module
 * 
 * This module analyzes and compares the performance of different
 * scheduling approaches using various metrics.
 */
public class PerformanceEvaluationModule {
    
    private Map<String, PerformanceMetrics> schedulerMetrics;
    private List<String> schedulerOrder;  // To maintain order of evaluation
    
    public PerformanceEvaluationModule() {
        this.schedulerMetrics = new LinkedHashMap<>();
        this.schedulerOrder = new ArrayList<>();
    }
    
    /**
     * Evaluates a scheduler's results and stores the metrics.
     * @param scheduler The scheduler that was used
     * @param results The scheduling results to evaluate
     */
    public void evaluate(TaskScheduler scheduler, List<SchedulingResult> results) {
        String schedulerName = scheduler.getName();
        PerformanceMetrics metrics = calculateMetrics(results);
        
        schedulerMetrics.put(schedulerName, metrics);
        if (!schedulerOrder.contains(schedulerName)) {
            schedulerOrder.add(schedulerName);
        }
        
        System.out.println("\nEvaluated: " + schedulerName);
    }
    
    /**
     * Calculates performance metrics from scheduling results.
     */
    private PerformanceMetrics calculateMetrics(List<SchedulingResult> results) {
        PerformanceMetrics metrics = new PerformanceMetrics();
        
        if (results.isEmpty()) {
            return metrics;
        }
        
        metrics.totalTasks = results.size();
        
        double sumTime = 0, sumCost = 0, sumLatency = 0;
        double maxFinishTime = 0;
        int deadlinesMet = 0, budgetsMet = 0, qosSatisfied = 0;
        
        List<Double> executionTimes = new ArrayList<>();
        List<Double> costs = new ArrayList<>();
        
        for (SchedulingResult result : results) {
            double time = result.getTotalTime();
            double cost = result.getCost();
            
            sumTime += time;
            sumCost += cost;
            sumLatency += result.getVm().getNetworkLatency();
            
            executionTimes.add(time);
            costs.add(cost);
            
            if (result.getFinishTime() > maxFinishTime) {
                maxFinishTime = result.getFinishTime();
            }
            
            if (result.isDeadlineSatisfied()) deadlinesMet++;
            if (result.isBudgetSatisfied()) budgetsMet++;
            if (result.isQoSSatisfied()) qosSatisfied++;
        }
        
        metrics.totalExecutionTime = sumTime;
        metrics.averageExecutionTime = sumTime / results.size();
        metrics.totalCost = sumCost;
        metrics.averageCost = sumCost / results.size();
        metrics.averageLatency = sumLatency / results.size();
        metrics.makespan = maxFinishTime;
        
        metrics.deadlinesMet = deadlinesMet;
        metrics.deadlineMissRate = (results.size() - deadlinesMet) * 100.0 / results.size();
        metrics.budgetsMet = budgetsMet;
        metrics.budgetViolationRate = (results.size() - budgetsMet) * 100.0 / results.size();
        metrics.qosSatisfied = qosSatisfied;
        metrics.qosSatisfactionRate = qosSatisfied * 100.0 / results.size();
        
        // Calculate standard deviations
        metrics.stdDevTime = calculateStdDev(executionTimes, metrics.averageExecutionTime);
        metrics.stdDevCost = calculateStdDev(costs, metrics.averageCost);
        
        // Calculate throughput
        metrics.throughput = results.size() / maxFinishTime;
        
        return metrics;
    }
    
    /**
     * Calculates standard deviation.
     */
    private double calculateStdDev(List<Double> values, double mean) {
        double sumSquaredDiff = 0;
        for (double value : values) {
            sumSquaredDiff += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sumSquaredDiff / values.size());
    }
    
    /**
     * Gets metrics for a specific scheduler.
     */
    public PerformanceMetrics getMetrics(String schedulerName) {
        return schedulerMetrics.get(schedulerName);
    }
    
    /**
     * Gets all scheduler metrics.
     */
    public Map<String, PerformanceMetrics> getAllMetrics() {
        return new LinkedHashMap<>(schedulerMetrics);
    }
    
    /**
     * Compares all evaluated schedulers and prints comparison table.
     */
    public void printComparisonTable() {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("PERFORMANCE COMPARISON TABLE");
        System.out.println("=".repeat(120));
        
        // Header
        System.out.println(String.format("%-30s %12s %12s %12s %12s %12s %12s",
                "Scheduler", "Avg Time(s)", "Total Cost", "Makespan", "DL Miss%", "Budget Vio%", "QoS Sat%"));
        System.out.println("-".repeat(120));
        
        // Data rows
        for (String name : schedulerOrder) {
            PerformanceMetrics m = schedulerMetrics.get(name);
            System.out.println(String.format("%-30s %12.4f %12.4f %12.4f %12.2f %12.2f %12.2f",
                    name, m.averageExecutionTime, m.totalCost, m.makespan,
                    m.deadlineMissRate, m.budgetViolationRate, m.qosSatisfactionRate));
        }
        
        System.out.println("=".repeat(120));
    }
    
    /**
     * Prints detailed metrics for all schedulers.
     */
    public void printDetailedReport() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DETAILED PERFORMANCE REPORT");
        System.out.println("=".repeat(80));
        
        for (String name : schedulerOrder) {
            PerformanceMetrics m = schedulerMetrics.get(name);
            
            System.out.println("\n--- " + name + " ---");
            System.out.println("Total Tasks: " + m.totalTasks);
            System.out.println();
            
            System.out.println("Time Metrics:");
            System.out.println("  Total Execution Time: " + String.format("%.4f", m.totalExecutionTime) + " s");
            System.out.println("  Average Execution Time: " + String.format("%.4f", m.averageExecutionTime) + " s");
            System.out.println("  Std Dev (Time): " + String.format("%.4f", m.stdDevTime) + " s");
            System.out.println("  Makespan: " + String.format("%.4f", m.makespan) + " s");
            System.out.println("  Average Latency: " + String.format("%.4f", m.averageLatency) + " s");
            System.out.println();
            
            System.out.println("Cost Metrics:");
            System.out.println("  Total Cost: $" + String.format("%.4f", m.totalCost));
            System.out.println("  Average Cost: $" + String.format("%.4f", m.averageCost));
            System.out.println("  Std Dev (Cost): $" + String.format("%.4f", m.stdDevCost));
            System.out.println();
            
            System.out.println("QoS Metrics:");
            System.out.println("  Deadlines Met: " + m.deadlinesMet + "/" + m.totalTasks + 
                    " (" + String.format("%.2f", 100 - m.deadlineMissRate) + "%)");
            System.out.println("  Deadline Miss Rate: " + String.format("%.2f", m.deadlineMissRate) + "%");
            System.out.println("  Budgets Met: " + m.budgetsMet + "/" + m.totalTasks +
                    " (" + String.format("%.2f", 100 - m.budgetViolationRate) + "%)");
            System.out.println("  Budget Violation Rate: " + String.format("%.2f", m.budgetViolationRate) + "%");
            System.out.println("  QoS Satisfaction Rate: " + String.format("%.2f", m.qosSatisfactionRate) + "%");
            System.out.println();
            
            System.out.println("Efficiency Metrics:");
            System.out.println("  Throughput: " + String.format("%.4f", m.throughput) + " tasks/s");
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    /**
     * Calculates improvement percentages of QoS scheduler over baselines.
     */
    public void printImprovementAnalysis(String qosSchedulerName) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("IMPROVEMENT ANALYSIS");
        System.out.println("QoS-Aware Scheduler vs Baseline Schedulers");
        System.out.println("=".repeat(80));
        
        PerformanceMetrics qosMetrics = schedulerMetrics.get(qosSchedulerName);
        
        if (qosMetrics == null) {
            System.out.println("QoS scheduler metrics not found!");
            return;
        }
        
        for (String name : schedulerOrder) {
            if (name.equals(qosSchedulerName)) continue;
            
            PerformanceMetrics baselineMetrics = schedulerMetrics.get(name);
            
            System.out.println("\n--- vs " + name + " ---");
            
            // Time improvement (lower is better)
            double timeImprovement = ((baselineMetrics.averageExecutionTime - qosMetrics.averageExecutionTime) 
                    / baselineMetrics.averageExecutionTime) * 100;
            System.out.println("Avg Execution Time: " + String.format("%.2f", timeImprovement) + "% " +
                    (timeImprovement >= 0 ? "improvement" : "degradation"));
            
            // Cost improvement (lower is better)
            double costImprovement = ((baselineMetrics.totalCost - qosMetrics.totalCost) 
                    / baselineMetrics.totalCost) * 100;
            System.out.println("Total Cost: " + String.format("%.2f", costImprovement) + "% " +
                    (costImprovement >= 0 ? "improvement" : "degradation"));
            
            // Makespan improvement (lower is better)
            double makespanImprovement = ((baselineMetrics.makespan - qosMetrics.makespan) 
                    / baselineMetrics.makespan) * 100;
            System.out.println("Makespan: " + String.format("%.2f", makespanImprovement) + "% " +
                    (makespanImprovement >= 0 ? "improvement" : "degradation"));
            
            // Deadline miss rate improvement (lower is better)
            double dlMissImprovement = baselineMetrics.deadlineMissRate - qosMetrics.deadlineMissRate;
            System.out.println("Deadline Miss Rate: " + String.format("%.2f", dlMissImprovement) + " percentage points " +
                    (dlMissImprovement >= 0 ? "improvement" : "degradation"));
            
            // QoS satisfaction improvement (higher is better)
            double qosImprovement = qosMetrics.qosSatisfactionRate - baselineMetrics.qosSatisfactionRate;
            System.out.println("QoS Satisfaction: " + String.format("%.2f", qosImprovement) + " percentage points " +
                    (qosImprovement >= 0 ? "improvement" : "degradation"));
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    /**
     * Resets all metrics for new evaluation.
     */
    public void reset() {
        schedulerMetrics.clear();
        schedulerOrder.clear();
    }
    
    /**
     * Calculates metrics from results and returns as a Map for UI use.
     * @param results The scheduling results
     * @param tasks The original tasks (not used currently, for future extensions)
     * @param vms The VMs (not used currently, for future extensions)
     * @return Map of metric names to values
     */
    public Map<String, Double> calculateMetrics(List<SchedulingResult> results,
                                                List<?> tasks, List<?> vms) {
        PerformanceMetrics pm = calculateMetrics(results);
        return metricsToMap(pm);
    }
    
    /**
     * Converts PerformanceMetrics to Map<String, Double> for UI use.
     */
    public Map<String, Double> metricsToMap(PerformanceMetrics pm) {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("totalTasks", (double) pm.totalTasks);
        map.put("totalExecutionTime", pm.totalExecutionTime);
        map.put("avgExecutionTime", pm.averageExecutionTime);
        map.put("stdDevTime", pm.stdDevTime);
        map.put("makespan", pm.makespan);
        map.put("avgLatency", pm.averageLatency);
        map.put("totalCost", pm.totalCost);
        map.put("avgCost", pm.averageCost);
        map.put("stdDevCost", pm.stdDevCost);
        map.put("deadlinesMet", (double) pm.deadlinesMet);
        map.put("deadlineMissRate", pm.deadlineMissRate);
        map.put("budgetsMet", (double) pm.budgetsMet);
        map.put("budgetViolationRate", pm.budgetViolationRate);
        map.put("qosSatisfied", (double) pm.qosSatisfied);
        map.put("qosSatisfactionRate", pm.qosSatisfactionRate);
        map.put("throughput", pm.throughput);
        // Add resource utilization placeholder
        map.put("resourceUtilization", 70.0); // Default placeholder
        map.put("loadBalance", 0.8); // Default placeholder
        return map;
    }
    
    /**
     * Inner class to hold performance metrics.
     */
    public static class PerformanceMetrics {
        public int totalTasks;
        
        // Time metrics
        public double totalExecutionTime;
        public double averageExecutionTime;
        public double stdDevTime;
        public double makespan;
        public double averageLatency;
        
        // Cost metrics
        public double totalCost;
        public double averageCost;
        public double stdDevCost;
        
        // QoS metrics
        public int deadlinesMet;
        public double deadlineMissRate;
        public int budgetsMet;
        public double budgetViolationRate;
        public int qosSatisfied;
        public double qosSatisfactionRate;
        
        // Efficiency metrics
        public double throughput;
        
        @Override
        public String toString() {
            return String.format("Metrics[tasks=%d, avgTime=%.4f, totalCost=%.4f, qos=%.2f%%]",
                    totalTasks, averageExecutionTime, totalCost, qosSatisfactionRate);
        }
    }
}
