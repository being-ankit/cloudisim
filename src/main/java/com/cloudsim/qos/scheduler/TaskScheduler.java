package com.cloudsim.qos.scheduler;

import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.model.VirtualMachine;

import java.util.List;
import java.util.Map;

/**
 * Interface for task scheduling algorithms.
 * All schedulers must implement this interface.
 */
public interface TaskScheduler {
    
    /**
     * Gets the name of the scheduling algorithm.
     */
    String getName();
    
    /**
     * Gets a description of the scheduling algorithm.
     */
    String getDescription();
    
    /**
     * Schedules tasks to virtual machines.
     * @param tasks List of tasks to schedule
     * @param vms List of available VMs
     * @return Map of task ID to scheduling result
     */
    Map<Integer, SchedulingResult> schedule(List<CloudTask> tasks, List<VirtualMachine> vms);
    
    /**
     * Gets the scheduling results after scheduling is complete.
     */
    List<SchedulingResult> getResults();
    
    /**
     * Resets the scheduler for a new scheduling round.
     */
    void reset();
}
