package com.cloudsim.qos.simulation;

import com.cloudsim.qos.config.SimulationConfig;
import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.VirtualMachine;
import com.cloudsim.qos.resource.CloudResourceModelingModule;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;

/**
 * CloudSim Integration Module
 * 
 * This module provides integration with CloudSim Plus for realistic
 * cloud simulation. It converts our model objects to CloudSim entities
 * and runs the actual simulation.
 */
public class CloudSimIntegration {
    
    private CloudResourceModelingModule resourceModule;
    private SimulationConfig config;
    private List<Cloudlet> cloudlets;
    
    public CloudSimIntegration(SimulationConfig config) {
        this.config = config;
        this.resourceModule = new CloudResourceModelingModule(config);
        this.cloudlets = new ArrayList<>();
    }
    
    /**
     * Initializes the CloudSim simulation environment.
     */
    public void initialize() {
        resourceModule.initializeSimulation();
        resourceModule.createDatacenters();
        resourceModule.createBroker();
    }
    
    /**
     * Sets up VMs in the simulation.
     */
    public void setupVirtualMachines(List<VirtualMachine> vms) {
        resourceModule.createVirtualMachines(vms);
    }
    
    /**
     * Creates cloudlets from our task model.
     */
    public void createCloudlets(List<CloudTask> tasks) {
        cloudlets.clear();
        
        for (CloudTask task : tasks) {
            Cloudlet cloudlet = new CloudletSimple(task.getTaskLength(), 1);
            cloudlet.setUtilizationModelCpu(new UtilizationModelFull());
            cloudlets.add(cloudlet);
        }
        
        DatacenterBroker broker = resourceModule.getBroker();
        broker.submitCloudletList(cloudlets);
        
        System.out.println("Created " + cloudlets.size() + " cloudlets from tasks.");
    }
    
    /**
     * Binds cloudlets to specific VMs based on scheduling decisions.
     * @param taskToVmMapping Map of task index to VM index
     */
    public void bindCloudletsToVMs(java.util.Map<Integer, Integer> taskToVmMapping) {
        DatacenterBroker broker = resourceModule.getBroker();
        
        for (java.util.Map.Entry<Integer, Integer> entry : taskToVmMapping.entrySet()) {
            int taskIndex = entry.getKey();
            int vmIndex = entry.getValue();
            
            if (taskIndex < cloudlets.size() && vmIndex < resourceModule.getCloudSimVms().size()) {
                Cloudlet cloudlet = cloudlets.get(taskIndex);
                cloudlet.setVm(resourceModule.getCloudSimVms().get(vmIndex));
            }
        }
    }
    
    /**
     * Runs the CloudSim simulation.
     */
    public void runSimulation() {
        System.out.println("\nStarting CloudSim simulation...");
        resourceModule.startSimulation();
        System.out.println("CloudSim simulation completed.");
    }
    
    /**
     * Gets the simulation results.
     */
    public List<SimulationResult> getResults() {
        List<SimulationResult> results = new ArrayList<>();
        
        DatacenterBroker broker = resourceModule.getBroker();
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        
        for (Cloudlet cloudlet : finishedCloudlets) {
            SimulationResult result = new SimulationResult();
            result.cloudletId = (int) cloudlet.getId();
            result.vmId = (int) cloudlet.getVm().getId();
            result.status = cloudlet.getStatus().name();
            result.executionTime = cloudlet.getActualCpuTime();
            result.startTime = cloudlet.getExecStartTime();
            result.finishTime = cloudlet.getFinishTime();
            result.waitingTime = cloudlet.getWaitingTime();
            
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Prints the simulation results.
     */
    public void printResults() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CLOUDSIM SIMULATION RESULTS");
        System.out.println("=".repeat(80));
        
        DatacenterBroker broker = resourceModule.getBroker();
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        
        System.out.println(String.format("%-12s %-8s %-12s %-12s %-12s %-12s %-12s",
                "Cloudlet", "VM", "Status", "Exec Time", "Start", "Finish", "Wait"));
        System.out.println("-".repeat(80));
        
        for (Cloudlet cloudlet : finishedCloudlets) {
            System.out.println(String.format("%-12d %-8d %-12s %-12.4f %-12.4f %-12.4f %-12.4f",
                    cloudlet.getId(),
                    cloudlet.getVm().getId(),
                    cloudlet.getStatus(),
                    cloudlet.getActualCpuTime(),
                    cloudlet.getExecStartTime(),
                    cloudlet.getFinishTime(),
                    cloudlet.getWaitingTime()));
        }
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * Gets the CloudSim simulation instance.
     */
    public CloudSim getSimulation() {
        return resourceModule.getSimulation();
    }
    
    /**
     * Gets the resource module.
     */
    public CloudResourceModelingModule getResourceModule() {
        return resourceModule;
    }
    
    /**
     * Inner class to hold simulation results.
     */
    public static class SimulationResult {
        public int cloudletId;
        public int vmId;
        public String status;
        public double executionTime;
        public double startTime;
        public double finishTime;
        public double waitingTime;
        
        @Override
        public String toString() {
            return String.format("SimResult[cloudlet=%d, vm=%d, time=%.4f, status=%s]",
                    cloudletId, vmId, executionTime, status);
        }
    }
}
