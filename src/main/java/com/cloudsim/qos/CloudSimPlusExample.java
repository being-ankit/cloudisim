package com.cloudsim.qos;

import com.cloudsim.qos.config.InputConfigurationModule;
import com.cloudsim.qos.config.SimulationConfig;
import com.cloudsim.qos.evaluation.PerformanceEvaluationModule;
import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.model.VirtualMachine;
import com.cloudsim.qos.scheduler.*;
import com.cloudsim.qos.simulation.CloudSimIntegration;
import com.cloudsim.qos.resource.CloudResourceModelingModule;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;

import java.util.*;

/**
 * CloudSim Plus Full Integration Example
 * 
 * This class demonstrates full integration with CloudSim Plus,
 * running actual cloud simulation with our QoS-aware scheduling.
 */
public class CloudSimPlusExample {
    
    private static final int HOSTS = 2;
    private static final int HOST_PES = 8;
    private static final int HOST_MIPS = 10000;
    private static final int HOST_RAM = 16384;
    private static final long HOST_BW = 10000;
    private static final long HOST_STORAGE = 1000000;
    
    private CloudSim simulation;
    private DatacenterBroker broker;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    
    public static void main(String[] args) {
        new CloudSimPlusExample().run();
    }
    
    public void run() {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║     CloudSim Plus Full Integration Example                       ║");
        System.out.println("║     QoS-Aware Task Scheduling with Real Simulation               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Initialize CloudSim
        simulation = new CloudSim();
        
        // Create datacenter
        Datacenter datacenter = createDatacenter();
        
        // Create broker
        broker = new DatacenterBrokerSimple(simulation);
        
        // Generate tasks and VMs using our configuration module
        InputConfigurationModule configModule = new InputConfigurationModule();
        configModule.generateSampleConfiguration(15, 5);
        
        List<CloudTask> tasks = configModule.getTasks();
        List<VirtualMachine> vms = configModule.getVirtualMachines();
        
        // Create CloudSim VMs
        vmList = createVmList(vms);
        broker.submitVmList(vmList);
        
        // Run QoS-aware scheduling to get task-VM mapping
        SimulationConfig config = new SimulationConfig(0.5, 0.5);
        QoSAwareScheduler qosScheduler = new QoSAwareScheduler(config);
        Map<Integer, SchedulingResult> schedulingResults = qosScheduler.schedule(tasks, vms);
        
        // Create cloudlets based on scheduling decisions
        cloudletList = createCloudletList(tasks, schedulingResults, vmList);
        broker.submitCloudletList(cloudletList);
        
        // Run simulation
        System.out.println("\nStarting CloudSim Plus simulation...\n");
        simulation.start();
        
        // Print results
        printSimulationResults();
        
        // Compare with baseline (for reference)
        System.out.println("\n--- Scheduling Decision Summary ---");
        for (SchedulingResult result : qosScheduler.getResults()) {
            System.out.println(String.format("Task %d -> VM %d (QoS: %s)",
                    result.getTask().getTaskId(),
                    result.getVm().getVmId(),
                    result.isQoSSatisfied() ? "Satisfied" : "Violated"));
        }
    }
    
    /**
     * Creates a datacenter with hosts.
     */
    private Datacenter createDatacenter() {
        List<Host> hostList = new ArrayList<>();
        
        for (int i = 0; i < HOSTS; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < HOST_PES; j++) {
                peList.add(new PeSimple(HOST_MIPS));
            }
            
            Host host = new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList);
            hostList.add(host);
        }
        
        return new DatacenterSimple(simulation, hostList);
    }
    
    /**
     * Creates CloudSim VMs from our VM model.
     */
    private List<Vm> createVmList(List<VirtualMachine> vms) {
        List<Vm> list = new ArrayList<>();
        
        for (VirtualMachine vm : vms) {
            Vm cloudSimVm = new VmSimple(vm.getMips(), vm.getNumberOfPes());
            cloudSimVm.setRam(vm.getRam());
            cloudSimVm.setBw(vm.getBandwidth());
            cloudSimVm.setSize(vm.getStorage());
            list.add(cloudSimVm);
        }
        
        return list;
    }
    
    /**
     * Creates cloudlets based on scheduling decisions.
     */
    private List<Cloudlet> createCloudletList(List<CloudTask> tasks, 
            Map<Integer, SchedulingResult> schedulingResults, List<Vm> vmList) {
        
        List<Cloudlet> list = new ArrayList<>();
        
        for (CloudTask task : tasks) {
            SchedulingResult result = schedulingResults.get(task.getTaskId());
            
            Cloudlet cloudlet = new CloudletSimple(task.getTaskLength(), 1);
            cloudlet.setUtilizationModelCpu(new UtilizationModelDynamic(0.5));
            
            // Bind to VM based on scheduling decision
            if (result != null) {
                int vmIndex = result.getVm().getVmId() - 1;  // Convert to 0-based index
                if (vmIndex >= 0 && vmIndex < vmList.size()) {
                    cloudlet.setVm(vmList.get(vmIndex));
                }
            }
            
            list.add(cloudlet);
        }
        
        return list;
    }
    
    /**
     * Prints simulation results.
     */
    private void printSimulationResults() {
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        
        System.out.println("\n" + "=".repeat(90));
        System.out.println("CLOUDSIM PLUS SIMULATION RESULTS");
        System.out.println("=".repeat(90));
        
        System.out.println(String.format("%-10s %-8s %-12s %-12s %-12s %-12s %-12s",
                "Cloudlet", "VM", "Status", "CPU Time", "Start", "Finish", "Wait"));
        System.out.println("-".repeat(90));
        
        double totalCpuTime = 0;
        double maxFinishTime = 0;
        
        for (Cloudlet cloudlet : finishedCloudlets) {
            System.out.println(String.format("%-10d %-8d %-12s %-12.4f %-12.4f %-12.4f %-12.4f",
                    cloudlet.getId(),
                    cloudlet.getVm().getId(),
                    cloudlet.getStatus(),
                    cloudlet.getActualCpuTime(),
                    cloudlet.getExecStartTime(),
                    cloudlet.getFinishTime(),
                    cloudlet.getWaitingTime()));
            
            totalCpuTime += cloudlet.getActualCpuTime();
            maxFinishTime = Math.max(maxFinishTime, cloudlet.getFinishTime());
        }
        
        System.out.println("-".repeat(90));
        System.out.println(String.format("Total CPU Time: %.4f seconds", totalCpuTime));
        System.out.println(String.format("Makespan: %.4f seconds", maxFinishTime));
        System.out.println(String.format("Throughput: %.4f tasks/second", finishedCloudlets.size() / maxFinishTime));
        System.out.println("=".repeat(90));
    }
}
