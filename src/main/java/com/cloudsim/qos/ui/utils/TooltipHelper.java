package com.cloudsim.qos.ui.utils;

import javafx.scene.control.*;

/**
 * Utility class for creating tooltips throughout the application
 */
public class TooltipHelper {
    
    private static final int SHOW_DELAY = 200;
    private static final int HIDE_DELAY = 5000;
    
    /**
     * Create and install a tooltip on a control
     */
    public static void install(Control control, String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(javafx.util.Duration.millis(SHOW_DELAY));
        tooltip.setHideDelay(javafx.util.Duration.millis(HIDE_DELAY));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(300);
        tooltip.setStyle("-fx-font-size: 12px;");
        control.setTooltip(tooltip);
    }
    
    /**
     * Install tooltip with a header and description
     */
    public static void install(Control control, String header, String description) {
        String text = header + "\n\n" + description;
        install(control, text);
    }
    
    // Pre-defined tooltips for common controls
    
    public static void installAlphaTooltip(Control control) {
        install(control, "Alpha (α) - Time Weight",
            "Controls the importance of execution time in the optimization.\n" +
            "• Higher α → Prioritizes faster execution\n" +
            "• Lower α → Less emphasis on time\n" +
            "• Range: 0.0 to 1.0\n" +
            "• α + β should equal 1.0 for balanced optimization");
    }
    
    public static void installBetaTooltip(Control control) {
        install(control, "Beta (β) - Cost Weight",
            "Controls the importance of execution cost in the optimization.\n" +
            "• Higher β → Prioritizes lower cost\n" +
            "• Lower β → Less emphasis on cost\n" +
            "• Range: 0.0 to 1.0\n" +
            "• α + β should equal 1.0 for balanced optimization");
    }
    
    public static void installDeadlineTooltip(Control control) {
        install(control, "Deadline Constraint",
            "When enabled, tasks must complete before their specified deadline.\n" +
            "Tasks that miss their deadline are marked as failed.\n" +
            "The scheduler will prioritize deadline-critical tasks.");
    }
    
    public static void installBudgetTooltip(Control control) {
        install(control, "Budget Constraint",
            "When enabled, task execution cost must stay within the specified budget.\n" +
            "Tasks exceeding budget are marked as over-budget.\n" +
            "The scheduler will consider cost limits during allocation.");
    }
    
    public static void installMIPSTooltip(Control control) {
        install(control, "MIPS (Million Instructions Per Second)",
            "Processing power of the virtual machine.\n" +
            "• Higher MIPS → Faster task execution\n" +
            "• Typical values: 500-10000\n" +
            "• Affects execution time and throughput");
    }
    
    public static void installPEsTooltip(Control control) {
        install(control, "PEs (Processing Elements)",
            "Number of CPU cores in the virtual machine.\n" +
            "• More PEs → Can run more tasks in parallel\n" +
            "• Typical values: 1-16\n" +
            "• Affects system capacity and parallelism");
    }
    
    public static void installRAMTooltip(Control control) {
        install(control, "RAM (Memory)",
            "Memory capacity of the virtual machine in MB.\n" +
            "• More RAM → Can handle larger tasks\n" +
            "• Typical values: 512-32768 MB\n" +
            "• Affects which tasks can be assigned");
    }
    
    public static void installBandwidthTooltip(Control control) {
        install(control, "Bandwidth",
            "Network bandwidth of the VM in Mbps.\n" +
            "• Higher bandwidth → Faster data transfer\n" +
            "• Affects file input/output times\n" +
            "• Important for data-intensive tasks");
    }
    
    public static void installCostTooltip(Control control) {
        install(control, "Cost Per Second",
            "Execution cost of the VM per second.\n" +
            "• Higher cost → More expensive execution\n" +
            "• Typical values: $0.001-$0.1/second\n" +
            "• Affects total execution cost");
    }
    
    public static void installTaskLengthTooltip(Control control) {
        install(control, "Task Length (MI)",
            "Computational workload of the task in Million Instructions.\n" +
            "• Larger length → Longer execution time\n" +
            "• Execution time = Length / VM MIPS\n" +
            "• Typical values: 1000-100000 MI");
    }
    
    public static void installTaskPriorityTooltip(Control control) {
        install(control, "Task Priority",
            "Importance level of the task.\n" +
            "• 1 = Low priority\n" +
            "• 2 = Normal priority\n" +
            "• 3 = High priority\n" +
            "Higher priority tasks are scheduled first.");
    }
    
    public static void installQoSScoreTooltip(Control control) {
        install(control, "QoS Score",
            "Quality of Service score (0-100%).\n" +
            "Combines multiple metrics:\n" +
            "• Deadline compliance\n" +
            "• Budget compliance\n" +
            "• Response time\n" +
            "• Resource utilization\n" +
            "Higher score = Better performance");
    }
    
    public static void installMakespanTooltip(Control control) {
        install(control, "Makespan",
            "Total time to complete all tasks.\n" +
            "Time from first task start to last task completion.\n" +
            "Lower makespan = More efficient scheduling.");
    }
    
    public static void installThroughputTooltip(Control control) {
        install(control, "Throughput",
            "Number of tasks completed per unit time.\n" +
            "Measured in tasks/second.\n" +
            "Higher throughput = More efficient system.");
    }
}
