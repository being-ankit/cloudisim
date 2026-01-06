package com.cloudsim.qos.resource;

import com.cloudsim.qos.config.SimulationConfig;
import com.cloudsim.qos.model.VirtualMachine;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import java.util.*;

/**
 * Module 2: Cloud Resource Modeling Module
 * 
 * This module simulates cloud infrastructure by modeling virtual machines
 * with varying capabilities such as processing speed, cost, and latency.
 */
public class CloudResourceModelingModule {
    
    private CloudSim simulation;
    private List<Datacenter> datacenters;
    private List<Host> hosts;
    private List<Vm> cloudSimVms;
    private DatacenterBroker broker;
    private Map<Integer, VirtualMachine> vmMapping;  // Maps CloudSim VM ID to our VM model
    private SimulationConfig config;
    
    public CloudResourceModelingModule(SimulationConfig config) {
        this.config = config;
        this.datacenters = new ArrayList<>();
        this.hosts = new ArrayList<>();
        this.cloudSimVms = new ArrayList<>();
        this.vmMapping = new HashMap<>();
    }
    
    /**
     * Initializes the CloudSim simulation environment.
     */
    public void initializeSimulation() {
        simulation = new CloudSim();
        System.out.println("CloudSim Plus simulation initialized.");
    }
    
    /**
     * Creates datacenter(s) to host the VMs.
     */
    public void createDatacenters() {
        for (int i = 0; i < config.getNumberOfDatacenters(); i++) {
            Datacenter datacenter = createDatacenter("Datacenter_" + i);
            datacenters.add(datacenter);
        }
        System.out.println("Created " + datacenters.size() + " datacenter(s).");
    }
    
    /**
     * Creates a single datacenter with hosts.
     */
    private Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        
        for (int i = 0; i < config.getNumberOfHosts(); i++) {
            Host host = createHost();
            hostList.add(host);
            hosts.add(host);
        }
        
        return new DatacenterSimple(simulation, hostList);
    }
    
    /**
     * Creates a host with processing elements.
     */
    private Host createHost() {
        List<Pe> peList = new ArrayList<>();
        
        for (int i = 0; i < config.getHostPes(); i++) {
            peList.add(new PeSimple(config.getHostMips()));
        }
        
        return new HostSimple(config.getHostRam(), config.getHostBandwidth(),
                config.getHostStorage(), peList);
    }
    
    /**
     * Creates a broker to manage VM and cloudlet submissions.
     */
    public void createBroker() {
        broker = new DatacenterBrokerSimple(simulation);
        System.out.println("Datacenter broker created.");
    }
    
    /**
     * Creates CloudSim VMs from our VirtualMachine model.
     * @param virtualMachines List of VirtualMachine objects to convert
     */
    public void createVirtualMachines(List<VirtualMachine> virtualMachines) {
        cloudSimVms.clear();
        vmMapping.clear();
        
        for (VirtualMachine vm : virtualMachines) {
            Vm cloudSimVm = createCloudSimVm(vm);
            cloudSimVms.add(cloudSimVm);
            vmMapping.put((int) cloudSimVm.getId(), vm);
        }
        
        broker.submitVmList(cloudSimVms);
        System.out.println("Created and submitted " + cloudSimVms.size() + " VMs to broker.");
    }
    
    /**
     * Converts our VirtualMachine model to CloudSim Vm.
     */
    private Vm createCloudSimVm(VirtualMachine vm) {
        Vm cloudSimVm = new VmSimple(vm.getMips(), vm.getNumberOfPes());
        cloudSimVm.setRam(vm.getRam());
        cloudSimVm.setBw(vm.getBandwidth());
        cloudSimVm.setSize(vm.getStorage());
        
        return cloudSimVm;
    }
    
    /**
     * Gets VM by ID.
     */
    public VirtualMachine getVirtualMachine(int vmId) {
        return vmMapping.get(vmId);
    }
    
    /**
     * Gets all VMs as a list.
     */
    public List<VirtualMachine> getAllVirtualMachines() {
        return new ArrayList<>(vmMapping.values());
    }
    
    /**
     * Gets CloudSim VM by index.
     */
    public Vm getCloudSimVm(int index) {
        if (index >= 0 && index < cloudSimVms.size()) {
            return cloudSimVms.get(index);
        }
        return null;
    }
    
    /**
     * Gets all CloudSim VMs.
     */
    public List<Vm> getCloudSimVms() {
        return cloudSimVms;
    }
    
    /**
     * Gets the broker.
     */
    public DatacenterBroker getBroker() {
        return broker;
    }
    
    /**
     * Gets the simulation instance.
     */
    public CloudSim getSimulation() {
        return simulation;
    }
    
    /**
     * Starts the simulation.
     */
    public void startSimulation() {
        simulation.start();
    }
    
    /**
     * Checks if simulation is running.
     */
    public boolean isRunning() {
        return simulation.isRunning();
    }
    
    /**
     * Gets VM state information.
     */
    public Map<Integer, VMState> getVMStates() {
        Map<Integer, VMState> states = new HashMap<>();
        
        for (Vm vm : cloudSimVms) {
            VMState state = new VMState();
            state.vmId = (int) vm.getId();
            state.mips = vm.getMips();
            state.numberOfPes = vm.getNumberOfPes();
            state.isCreated = vm.isCreated();
            state.totalExecutionTime = vm.getTotalExecutionTime();
            
            states.put(state.vmId, state);
        }
        
        return states;
    }
    
    /**
     * Prints resource summary.
     */
    public void printResourceSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("CLOUD RESOURCE SUMMARY");
        System.out.println("=".repeat(60));
        
        System.out.println("\nDatacenters: " + datacenters.size());
        System.out.println("Total Hosts: " + hosts.size());
        System.out.println("Total VMs: " + cloudSimVms.size());
        
        System.out.println("\nVM Details:");
        System.out.println(String.format("  %-6s %-10s %-6s %-10s %-10s",
                "ID", "MIPS", "PEs", "RAM(MB)", "Status"));
        System.out.println("  " + "-".repeat(45));
        
        for (Vm vm : cloudSimVms) {
            VirtualMachine ourVm = vmMapping.get((int) vm.getId());
            System.out.println(String.format("  %-6d %-10.0f %-6d %-10d %-10s",
                    vm.getId(), vm.getMips(), vm.getNumberOfPes(), vm.getRam(),
                    vm.isCreated() ? "Created" : "Pending"));
        }
        
        System.out.println("=".repeat(60) + "\n");
    }
    
    /**
     * Resets all VMs for a new scheduling scenario.
     */
    public void resetVMs() {
        for (VirtualMachine vm : vmMapping.values()) {
            vm.reset();
        }
    }
    
    /**
     * Inner class to represent VM state.
     */
    public static class VMState {
        public int vmId;
        public double mips;
        public long numberOfPes;
        public boolean isCreated;
        public double totalExecutionTime;
        
        @Override
        public String toString() {
            return String.format("VMState[id=%d, mips=%.0f, created=%s]",
                    vmId, mips, isCreated);
        }
    }
}
