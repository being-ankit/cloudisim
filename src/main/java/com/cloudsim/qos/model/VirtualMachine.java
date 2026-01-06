package com.cloudsim.qos.model;

/**
 * Represents a Virtual Machine in the cloud infrastructure.
 * Contains VM specifications and cost model for scheduling decisions.
 */
public class VirtualMachine {
    private int vmId;
    private double mips;            // Processing power in Million Instructions Per Second
    private double costPerSecond;   // Cost per second of execution
    private double networkLatency;  // Network latency in seconds
    private int numberOfPes;        // Number of processing elements (cores)
    private long ram;               // RAM in MB
    private long bandwidth;         // Bandwidth in Mbps
    private long storage;           // Storage in MB
    
    // VM state
    private boolean available;
    private double currentLoad;     // Current load percentage (0-100)
    private double totalExecutionTime;
    private double totalCost;
    private int tasksExecuted;
    
    public VirtualMachine() {
        this.available = true;
        this.currentLoad = 0;
    }
    
    public VirtualMachine(int vmId, double mips, double costPerSecond, double networkLatency) {
        this();
        this.vmId = vmId;
        this.mips = mips;
        this.costPerSecond = costPerSecond;
        this.networkLatency = networkLatency;
        this.numberOfPes = 1;
        this.ram = 2048;
        this.bandwidth = 1000;
        this.storage = 10000;
    }
    
    public VirtualMachine(int vmId, double mips, double costPerSecond, double networkLatency,
                          int numberOfPes, long ram, long bandwidth, long storage) {
        this(vmId, mips, costPerSecond, networkLatency);
        this.numberOfPes = numberOfPes;
        this.ram = ram;
        this.bandwidth = bandwidth;
        this.storage = storage;
    }
    
    /**
     * Calculates the execution time for a given task length.
     * @param taskLength Task length in Million Instructions
     * @return Execution time in seconds (excluding latency)
     */
    public double calculateExecutionTime(long taskLength) {
        return (double) taskLength / mips;
    }
    
    /**
     * Calculates the total time including network latency.
     * @param taskLength Task length in Million Instructions
     * @return Total time in seconds
     */
    public double calculateTotalTime(long taskLength) {
        return calculateExecutionTime(taskLength) + networkLatency;
    }
    
    /**
     * Calculates the execution cost for a given task length.
     * @param taskLength Task length in Million Instructions
     * @return Execution cost
     */
    public double calculateExecutionCost(long taskLength) {
        return calculateExecutionTime(taskLength) * costPerSecond;
    }
    
    /**
     * Updates VM statistics after task execution.
     * @param executionTime Time taken to execute the task
     * @param cost Cost of execution
     */
    public void recordTaskExecution(double executionTime, double cost) {
        this.totalExecutionTime += executionTime;
        this.totalCost += cost;
        this.tasksExecuted++;
    }
    
    /**
     * Resets VM statistics for new scheduling scenario.
     */
    public void reset() {
        this.totalExecutionTime = 0;
        this.totalCost = 0;
        this.tasksExecuted = 0;
        this.currentLoad = 0;
        this.available = true;
    }
    
    // Getters and Setters
    public int getVmId() {
        return vmId;
    }
    
    public void setVmId(int vmId) {
        this.vmId = vmId;
    }
    
    public double getMips() {
        return mips;
    }
    
    public void setMips(double mips) {
        this.mips = mips;
    }
    
    public double getCostPerSecond() {
        return costPerSecond;
    }
    
    public void setCostPerSecond(double costPerSecond) {
        this.costPerSecond = costPerSecond;
    }
    
    public double getNetworkLatency() {
        return networkLatency;
    }
    
    public void setNetworkLatency(double networkLatency) {
        this.networkLatency = networkLatency;
    }
    
    public int getNumberOfPes() {
        return numberOfPes;
    }
    
    public void setNumberOfPes(int numberOfPes) {
        this.numberOfPes = numberOfPes;
    }
    
    public long getRam() {
        return ram;
    }
    
    public void setRam(long ram) {
        this.ram = ram;
    }
    
    public long getBandwidth() {
        return bandwidth;
    }
    
    public void setBandwidth(long bandwidth) {
        this.bandwidth = bandwidth;
    }
    
    public long getStorage() {
        return storage;
    }
    
    public void setStorage(long storage) {
        this.storage = storage;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public double getCurrentLoad() {
        return currentLoad;
    }
    
    public void setCurrentLoad(double currentLoad) {
        this.currentLoad = currentLoad;
    }
    
    public double getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    public int getTasksExecuted() {
        return tasksExecuted;
    }
    
    /**
     * Creates a copy of this VM for use in different scheduling scenarios.
     */
    public VirtualMachine copy() {
        VirtualMachine copy = new VirtualMachine(vmId, mips, costPerSecond, networkLatency,
                numberOfPes, ram, bandwidth, storage);
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("VM[id=%d, mips=%.0f, cost=$%.4f/s, latency=%.3fs, PEs=%d]",
                vmId, mips, costPerSecond, networkLatency, numberOfPes);
    }
}
