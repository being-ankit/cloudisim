package com.cloudsim.qos.model;

/**
 * Represents a cloud task with QoS constraints.
 * This class encapsulates all task-related parameters including
 * deadline and budget constraints for QoS-aware scheduling.
 */
public class CloudTask {
    private int taskId;
    private long taskLength;        // in Million Instructions (MI)
    private double deadline;        // in seconds
    private double budget;          // maximum cost limit
    private int priority;           // task priority (1-10)
    private double arrivalTime;     // time when task arrives
    
    // Execution results (filled after scheduling)
    private int assignedVmId;
    private double estimatedExecutionTime;
    private double estimatedCost;
    private double actualExecutionTime;
    private double actualCost;
    private boolean deadlineMet;
    private boolean budgetMet;
    
    public CloudTask() {
    }
    
    public CloudTask(int taskId, long taskLength, double deadline, double budget) {
        this.taskId = taskId;
        this.taskLength = taskLength;
        this.deadline = deadline;
        this.budget = budget;
        this.priority = 5; // default priority
        this.arrivalTime = 0;
        this.assignedVmId = -1;
    }
    
    public CloudTask(int taskId, long taskLength, double deadline, double budget, int priority) {
        this(taskId, taskLength, deadline, budget);
        this.priority = priority;
    }
    
    // Getters and Setters
    public int getTaskId() {
        return taskId;
    }
    
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
    
    public long getTaskLength() {
        return taskLength;
    }
    
    public void setTaskLength(long taskLength) {
        this.taskLength = taskLength;
    }
    
    public double getDeadline() {
        return deadline;
    }
    
    public void setDeadline(double deadline) {
        this.deadline = deadline;
    }
    
    public double getBudget() {
        return budget;
    }
    
    public void setBudget(double budget) {
        this.budget = budget;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = Math.max(1, Math.min(10, priority));
    }
    
    public double getArrivalTime() {
        return arrivalTime;
    }
    
    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public int getAssignedVmId() {
        return assignedVmId;
    }
    
    public void setAssignedVmId(int assignedVmId) {
        this.assignedVmId = assignedVmId;
    }
    
    public double getEstimatedExecutionTime() {
        return estimatedExecutionTime;
    }
    
    public void setEstimatedExecutionTime(double estimatedExecutionTime) {
        this.estimatedExecutionTime = estimatedExecutionTime;
    }
    
    public double getEstimatedCost() {
        return estimatedCost;
    }
    
    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
    
    public double getActualExecutionTime() {
        return actualExecutionTime;
    }
    
    public void setActualExecutionTime(double actualExecutionTime) {
        this.actualExecutionTime = actualExecutionTime;
    }
    
    public double getActualCost() {
        return actualCost;
    }
    
    public void setActualCost(double actualCost) {
        this.actualCost = actualCost;
    }
    
    public boolean isDeadlineMet() {
        return deadlineMet;
    }
    
    public void setDeadlineMet(boolean deadlineMet) {
        this.deadlineMet = deadlineMet;
    }
    
    public boolean isBudgetMet() {
        return budgetMet;
    }
    
    public void setBudgetMet(boolean budgetMet) {
        this.budgetMet = budgetMet;
    }
    
    /**
     * Creates a copy of this task for use in different scheduling scenarios.
     */
    public CloudTask copy() {
        CloudTask copy = new CloudTask(taskId, taskLength, deadline, budget, priority);
        copy.setArrivalTime(arrivalTime);
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("CloudTask[id=%d, length=%d MI, deadline=%.2fs, budget=$%.2f, priority=%d]",
                taskId, taskLength, deadline, budget, priority);
    }
}
