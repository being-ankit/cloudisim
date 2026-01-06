package com.cloudsim.qos.model;

/**
 * Represents the scheduling result for a task-VM assignment.
 * Contains all information about how a task was scheduled.
 */
public class SchedulingResult {
    private CloudTask task;
    private VirtualMachine vm;
    private double executionTime;
    private double totalTime;       // Including latency
    private double cost;
    private double qosScore;
    private boolean deadlineSatisfied;
    private boolean budgetSatisfied;
    private double startTime;
    private double finishTime;
    
    public SchedulingResult(CloudTask task, VirtualMachine vm) {
        this.task = task;
        this.vm = vm;
        calculateMetrics();
    }
    
    /**
     * Calculates execution metrics based on task and VM.
     */
    private void calculateMetrics() {
        this.executionTime = vm.calculateExecutionTime(task.getTaskLength());
        this.totalTime = vm.calculateTotalTime(task.getTaskLength());
        this.cost = vm.calculateExecutionCost(task.getTaskLength());
        this.deadlineSatisfied = totalTime <= task.getDeadline();
        this.budgetSatisfied = cost <= task.getBudget();
    }
    
    /**
     * Calculates QoS score based on weighted objectives.
     * @param alpha Weight for execution time
     * @param beta Weight for cost
     * @return QoS score (lower is better)
     */
    public double calculateQoSScore(double alpha, double beta) {
        // Normalize values for fair comparison
        double normalizedTime = totalTime / task.getDeadline();
        double normalizedCost = cost / task.getBudget();
        
        // Penalty for constraint violations
        double deadlinePenalty = deadlineSatisfied ? 0 : (totalTime - task.getDeadline()) * 10;
        double budgetPenalty = budgetSatisfied ? 0 : (cost - task.getBudget()) * 10;
        
        this.qosScore = alpha * normalizedTime + beta * normalizedCost + deadlinePenalty + budgetPenalty;
        return qosScore;
    }
    
    /**
     * Checks if all QoS constraints are satisfied.
     */
    public boolean isQoSSatisfied() {
        return deadlineSatisfied && budgetSatisfied;
    }
    
    // Getters and Setters
    public CloudTask getTask() {
        return task;
    }
    
    public VirtualMachine getVm() {
        return vm;
    }
    
    public double getExecutionTime() {
        return executionTime;
    }
    
    public double getTotalTime() {
        return totalTime;
    }
    
    public double getCost() {
        return cost;
    }
    
    /**
     * Alias for getCost() - returns execution cost.
     */
    public double getExecutionCost() {
        return cost;
    }
    
    public double getQosScore() {
        return qosScore;
    }
    
    public boolean isDeadlineSatisfied() {
        return deadlineSatisfied;
    }
    
    public boolean isBudgetSatisfied() {
        return budgetSatisfied;
    }
    
    public double getStartTime() {
        return startTime;
    }
    
    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }
    
    public double getFinishTime() {
        return finishTime;
    }
    
    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }
    
    @Override
    public String toString() {
        return String.format("Result[Task=%d -> VM=%d, Time=%.3fs, Cost=$%.4f, QoS=%s]",
                task.getTaskId(), vm.getVmId(), totalTime, cost,
                isQoSSatisfied() ? "Satisfied" : "Violated");
    }
}
