package com.cloudsim.qos.config;

/**
 * Simulation configuration parameters.
 * Contains weights and settings for the QoS-aware scheduling algorithm.
 */
public class SimulationConfig {
    
    // QoS weights (must sum to 1.0 for normalized scoring)
    private double alpha = 0.5;     // Weight for execution time
    private double beta = 0.5;      // Weight for cost
    
    // Simulation settings
    private int numberOfRuns = 1;
    private boolean enableVisualization = true;
    private boolean saveResults = true;
    private String outputDirectory = "output";
    
    // Algorithm settings
    private boolean enableDeadlineConstraint = true;
    private boolean enableBudgetConstraint = true;
    private double deadlineTolerance = 0.1;  // 10% tolerance
    private double budgetTolerance = 0.1;    // 10% tolerance
    private double maxDeadline = 100.0;      // Maximum deadline constraint
    private double maxBudget = 100.0;        // Maximum budget constraint
    
    // CloudSim specific settings
    private int numberOfDatacenters = 1;
    private int numberOfHosts = 1;
    private long hostRam = 16384;        // MB
    private long hostStorage = 1000000;  // MB
    private long hostBandwidth = 10000;  // Mbps
    private int hostPes = 8;
    private double hostMips = 10000;
    
    public SimulationConfig() {
    }
    
    public SimulationConfig(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }
    
    // Getters and Setters
    public double getAlpha() {
        return alpha;
    }
    
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }
    
    public double getBeta() {
        return beta;
    }
    
    public void setBeta(double beta) {
        this.beta = beta;
    }
    
    public int getNumberOfRuns() {
        return numberOfRuns;
    }
    
    public void setNumberOfRuns(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }
    
    public boolean isEnableVisualization() {
        return enableVisualization;
    }
    
    public void setEnableVisualization(boolean enableVisualization) {
        this.enableVisualization = enableVisualization;
    }
    
    public boolean isSaveResults() {
        return saveResults;
    }
    
    public void setSaveResults(boolean saveResults) {
        this.saveResults = saveResults;
    }
    
    public String getOutputDirectory() {
        return outputDirectory;
    }
    
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    public boolean isEnableDeadlineConstraint() {
        return enableDeadlineConstraint;
    }
    
    public void setEnableDeadlineConstraint(boolean enableDeadlineConstraint) {
        this.enableDeadlineConstraint = enableDeadlineConstraint;
    }
    
    public boolean isEnableBudgetConstraint() {
        return enableBudgetConstraint;
    }
    
    public void setEnableBudgetConstraint(boolean enableBudgetConstraint) {
        this.enableBudgetConstraint = enableBudgetConstraint;
    }
    
    public double getDeadlineTolerance() {
        return deadlineTolerance;
    }
    
    public void setDeadlineTolerance(double deadlineTolerance) {
        this.deadlineTolerance = deadlineTolerance;
    }
    
    public double getBudgetTolerance() {
        return budgetTolerance;
    }
    
    public void setBudgetTolerance(double budgetTolerance) {
        this.budgetTolerance = budgetTolerance;
    }
    
    public double getMaxDeadline() {
        return maxDeadline;
    }
    
    public void setMaxDeadline(double maxDeadline) {
        this.maxDeadline = maxDeadline;
    }
    
    public double getMaxBudget() {
        return maxBudget;
    }
    
    public void setMaxBudget(double maxBudget) {
        this.maxBudget = maxBudget;
    }
    
    public int getNumberOfDatacenters() {
        return numberOfDatacenters;
    }
    
    public void setNumberOfDatacenters(int numberOfDatacenters) {
        this.numberOfDatacenters = numberOfDatacenters;
    }
    
    public int getNumberOfHosts() {
        return numberOfHosts;
    }
    
    public void setNumberOfHosts(int numberOfHosts) {
        this.numberOfHosts = numberOfHosts;
    }
    
    public long getHostRam() {
        return hostRam;
    }
    
    public void setHostRam(long hostRam) {
        this.hostRam = hostRam;
    }
    
    public long getHostStorage() {
        return hostStorage;
    }
    
    public void setHostStorage(long hostStorage) {
        this.hostStorage = hostStorage;
    }
    
    public long getHostBandwidth() {
        return hostBandwidth;
    }
    
    public void setHostBandwidth(long hostBandwidth) {
        this.hostBandwidth = hostBandwidth;
    }
    
    public int getHostPes() {
        return hostPes;
    }
    
    public void setHostPes(int hostPes) {
        this.hostPes = hostPes;
    }
    
    public double getHostMips() {
        return hostMips;
    }
    
    public void setHostMips(double hostMips) {
        this.hostMips = hostMips;
    }
    
    @Override
    public String toString() {
        return String.format("SimulationConfig[alpha=%.2f, beta=%.2f, runs=%d]",
                alpha, beta, numberOfRuns);
    }
}
