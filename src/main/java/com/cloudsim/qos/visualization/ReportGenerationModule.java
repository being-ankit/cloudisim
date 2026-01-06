package com.cloudsim.qos.visualization;

import com.cloudsim.qos.evaluation.PerformanceEvaluationModule.PerformanceMetrics;
import com.cloudsim.qos.model.SchedulingResult;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Module 8: Report and Documentation Module
 * 
 * This module focuses on documenting system design, implementation details,
 * and results. It generates reports in various formats.
 */
public class ReportGenerationModule {
    
    private String outputDirectory;
    private StringBuilder reportContent;
    
    public ReportGenerationModule(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.reportContent = new StringBuilder();
        new java.io.File(outputDirectory).mkdirs();
    }
    
    /**
     * Generates a complete simulation report.
     */
    public void generateReport(Map<String, PerformanceMetrics> metricsMap,
                               Map<String, List<SchedulingResult>> allResults,
                               double alpha, double beta) throws IOException {
        
        reportContent = new StringBuilder();
        
        addHeader();
        addSimulationParameters(alpha, beta);
        addSchedulingResults(allResults);
        addPerformanceComparison(metricsMap);
        addDetailedMetrics(metricsMap);
        addImprovementAnalysis(metricsMap);
        addConclusions(metricsMap);
        addFooter();
        
        // Save report
        String filename = "simulation_report_" + getTimestamp() + ".txt";
        saveReport(filename);
        
        // Also generate HTML report
        generateHTMLReport(metricsMap, allResults, alpha, beta);
        
        // Generate CSV for data analysis
        generateCSVReport(metricsMap);
    }
    
    private void addHeader() {
        reportContent.append("=".repeat(80)).append("\n");
        reportContent.append("QoS-AWARE MULTI-OBJECTIVE TASK SCHEDULING SIMULATOR\n");
        reportContent.append("SIMULATION REPORT\n");
        reportContent.append("=".repeat(80)).append("\n\n");
        reportContent.append("Generated: ").append(new Date()).append("\n\n");
    }
    
    private void addSimulationParameters(double alpha, double beta) {
        reportContent.append("-".repeat(80)).append("\n");
        reportContent.append("SIMULATION PARAMETERS\n");
        reportContent.append("-".repeat(80)).append("\n");
        reportContent.append("Alpha (Time Weight): ").append(alpha).append("\n");
        reportContent.append("Beta (Cost Weight): ").append(beta).append("\n");
        reportContent.append("Objective Function: QoS Score = α × Time + β × Cost\n\n");
    }
    
    private void addSchedulingResults(Map<String, List<SchedulingResult>> allResults) {
        reportContent.append("-".repeat(80)).append("\n");
        reportContent.append("TASK ALLOCATION RESULTS\n");
        reportContent.append("-".repeat(80)).append("\n\n");
        
        for (Map.Entry<String, List<SchedulingResult>> entry : allResults.entrySet()) {
            reportContent.append("*** ").append(entry.getKey()).append(" ***\n");
            reportContent.append(String.format("%-8s %-8s %-12s %-12s %-10s %-10s\n",
                    "Task", "VM", "Time(s)", "Cost($)", "Deadline", "Budget"));
            reportContent.append("-".repeat(60)).append("\n");
            
            for (SchedulingResult result : entry.getValue()) {
                reportContent.append(String.format("%-8d %-8d %-12.4f %-12.4f %-10s %-10s\n",
                        result.getTask().getTaskId(),
                        result.getVm().getVmId(),
                        result.getTotalTime(),
                        result.getCost(),
                        result.isDeadlineSatisfied() ? "Met" : "MISSED",
                        result.isBudgetSatisfied() ? "Met" : "EXCEEDED"));
            }
            reportContent.append("\n");
        }
    }
    
    private void addPerformanceComparison(Map<String, PerformanceMetrics> metricsMap) {
        reportContent.append("-".repeat(80)).append("\n");
        reportContent.append("PERFORMANCE COMPARISON\n");
        reportContent.append("-".repeat(80)).append("\n\n");
        
        reportContent.append(String.format("%-30s %12s %12s %12s %12s\n",
                "Scheduler", "Avg Time(s)", "Total Cost", "DL Miss%", "QoS Sat%"));
        reportContent.append("-".repeat(80)).append("\n");
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            PerformanceMetrics m = entry.getValue();
            reportContent.append(String.format("%-30s %12.4f %12.4f %12.2f %12.2f\n",
                    entry.getKey(), m.averageExecutionTime, m.totalCost,
                    m.deadlineMissRate, m.qosSatisfactionRate));
        }
        reportContent.append("\n");
    }
    
    private void addDetailedMetrics(Map<String, PerformanceMetrics> metricsMap) {
        reportContent.append("-".repeat(80)).append("\n");
        reportContent.append("DETAILED METRICS\n");
        reportContent.append("-".repeat(80)).append("\n\n");
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            PerformanceMetrics m = entry.getValue();
            reportContent.append("*** ").append(entry.getKey()).append(" ***\n");
            reportContent.append("  Tasks Processed: ").append(m.totalTasks).append("\n");
            reportContent.append("  Total Execution Time: ").append(String.format("%.4f", m.totalExecutionTime)).append(" s\n");
            reportContent.append("  Average Execution Time: ").append(String.format("%.4f", m.averageExecutionTime)).append(" s\n");
            reportContent.append("  Makespan: ").append(String.format("%.4f", m.makespan)).append(" s\n");
            reportContent.append("  Total Cost: $").append(String.format("%.4f", m.totalCost)).append("\n");
            reportContent.append("  Average Cost: $").append(String.format("%.4f", m.averageCost)).append("\n");
            reportContent.append("  Deadlines Met: ").append(m.deadlinesMet).append("/").append(m.totalTasks).append("\n");
            reportContent.append("  Budgets Met: ").append(m.budgetsMet).append("/").append(m.totalTasks).append("\n");
            reportContent.append("  QoS Satisfied: ").append(m.qosSatisfied).append("/").append(m.totalTasks).append("\n");
            reportContent.append("  Throughput: ").append(String.format("%.4f", m.throughput)).append(" tasks/s\n\n");
        }
    }
    
    private void addImprovementAnalysis(Map<String, PerformanceMetrics> metricsMap) {
        reportContent.append("-".repeat(80)).append("\n");
        reportContent.append("IMPROVEMENT ANALYSIS\n");
        reportContent.append("-".repeat(80)).append("\n\n");
        
        PerformanceMetrics qosMetrics = null;
        String qosName = null;
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            if (entry.getKey().contains("QoS")) {
                qosMetrics = entry.getValue();
                qosName = entry.getKey();
                break;
            }
        }
        
        if (qosMetrics == null) {
            reportContent.append("QoS scheduler metrics not found.\n\n");
            return;
        }
        
        reportContent.append("Comparison of ").append(qosName).append(" vs Baseline Schedulers:\n\n");
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            if (entry.getKey().equals(qosName)) continue;
            
            PerformanceMetrics baseline = entry.getValue();
            
            reportContent.append("vs ").append(entry.getKey()).append(":\n");
            
            double timeImprovement = ((baseline.averageExecutionTime - qosMetrics.averageExecutionTime) 
                    / baseline.averageExecutionTime) * 100;
            reportContent.append("  Time: ").append(String.format("%+.2f", timeImprovement)).append("% ")
                    .append(timeImprovement >= 0 ? "(better)" : "(worse)").append("\n");
            
            double costImprovement = ((baseline.totalCost - qosMetrics.totalCost) 
                    / baseline.totalCost) * 100;
            reportContent.append("  Cost: ").append(String.format("%+.2f", costImprovement)).append("% ")
                    .append(costImprovement >= 0 ? "(better)" : "(worse)").append("\n");
            
            double qosImprovement = qosMetrics.qosSatisfactionRate - baseline.qosSatisfactionRate;
            reportContent.append("  QoS: ").append(String.format("%+.2f", qosImprovement)).append(" pp ")
                    .append(qosImprovement >= 0 ? "(better)" : "(worse)").append("\n\n");
        }
    }
    
    private void addConclusions(Map<String, PerformanceMetrics> metricsMap) {
        reportContent.append("-".repeat(80)).append("\n");
        reportContent.append("CONCLUSIONS\n");
        reportContent.append("-".repeat(80)).append("\n\n");
        
        // Find best performer in each category
        String bestTime = "", bestCost = "", bestQoS = "";
        double minTime = Double.MAX_VALUE, minCost = Double.MAX_VALUE, maxQoS = 0;
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            PerformanceMetrics m = entry.getValue();
            if (m.averageExecutionTime < minTime) {
                minTime = m.averageExecutionTime;
                bestTime = entry.getKey();
            }
            if (m.totalCost < minCost) {
                minCost = m.totalCost;
                bestCost = entry.getKey();
            }
            if (m.qosSatisfactionRate > maxQoS) {
                maxQoS = m.qosSatisfactionRate;
                bestQoS = entry.getKey();
            }
        }
        
        reportContent.append("Best Performance by Category:\n");
        reportContent.append("  - Best Execution Time: ").append(bestTime).append("\n");
        reportContent.append("  - Lowest Cost: ").append(bestCost).append("\n");
        reportContent.append("  - Highest QoS Satisfaction: ").append(bestQoS).append("\n\n");
        
        reportContent.append("The QoS-Aware scheduler demonstrates the effectiveness of\n");
        reportContent.append("multi-objective optimization in cloud task scheduling by\n");
        reportContent.append("balancing execution time, cost, and QoS constraint satisfaction.\n\n");
    }
    
    private void addFooter() {
        reportContent.append("=".repeat(80)).append("\n");
        reportContent.append("END OF REPORT\n");
        reportContent.append("=".repeat(80)).append("\n");
    }
    
    /**
     * Saves the report to a file.
     */
    private void saveReport(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputDirectory + "/" + filename))) {
            writer.print(reportContent.toString());
        }
        System.out.println("Report saved: " + filename);
    }
    
    /**
     * Generates an HTML report for better presentation.
     */
    public void generateHTMLReport(Map<String, PerformanceMetrics> metricsMap,
                                   Map<String, List<SchedulingResult>> allResults,
                                   double alpha, double beta) throws IOException {
        
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>QoS Task Scheduling Simulation Report</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("h1 { color: #2c3e50; }\n");
        html.append("h2 { color: #34495e; border-bottom: 2px solid #3498db; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #3498db; color: white; }\n");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }\n");
        html.append(".met { color: green; }\n");
        html.append(".missed { color: red; font-weight: bold; }\n");
        html.append(".metric-card { background: #f8f9fa; padding: 15px; margin: 10px; border-radius: 5px; display: inline-block; }\n");
        html.append("</style>\n</head>\n<body>\n");
        
        html.append("<h1>QoS-Aware Multi-Objective Task Scheduling Simulator</h1>\n");
        html.append("<p>Report Generated: ").append(new Date()).append("</p>\n");
        
        // Parameters
        html.append("<h2>Simulation Parameters</h2>\n");
        html.append("<p>Alpha (Time Weight): ").append(alpha).append("</p>\n");
        html.append("<p>Beta (Cost Weight): ").append(beta).append("</p>\n");
        html.append("<p>Objective Function: QoS Score = α × Time + β × Cost</p>\n");
        
        // Performance Comparison Table
        html.append("<h2>Performance Comparison</h2>\n");
        html.append("<table>\n<tr><th>Scheduler</th><th>Avg Time (s)</th><th>Total Cost ($)</th>");
        html.append("<th>Makespan (s)</th><th>Deadline Miss %</th><th>QoS Satisfaction %</th></tr>\n");
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            PerformanceMetrics m = entry.getValue();
            html.append("<tr>");
            html.append("<td>").append(entry.getKey()).append("</td>");
            html.append("<td>").append(String.format("%.4f", m.averageExecutionTime)).append("</td>");
            html.append("<td>").append(String.format("%.4f", m.totalCost)).append("</td>");
            html.append("<td>").append(String.format("%.4f", m.makespan)).append("</td>");
            html.append("<td>").append(String.format("%.2f", m.deadlineMissRate)).append("</td>");
            html.append("<td>").append(String.format("%.2f", m.qosSatisfactionRate)).append("</td>");
            html.append("</tr>\n");
        }
        html.append("</table>\n");
        
        // Task Allocation Details
        html.append("<h2>Task Allocation Details</h2>\n");
        
        for (Map.Entry<String, List<SchedulingResult>> entry : allResults.entrySet()) {
            html.append("<h3>").append(entry.getKey()).append("</h3>\n");
            html.append("<table>\n<tr><th>Task</th><th>VM</th><th>Time (s)</th><th>Cost ($)</th>");
            html.append("<th>Deadline</th><th>Budget</th></tr>\n");
            
            for (SchedulingResult result : entry.getValue()) {
                html.append("<tr>");
                html.append("<td>").append(result.getTask().getTaskId()).append("</td>");
                html.append("<td>").append(result.getVm().getVmId()).append("</td>");
                html.append("<td>").append(String.format("%.4f", result.getTotalTime())).append("</td>");
                html.append("<td>").append(String.format("%.4f", result.getCost())).append("</td>");
                html.append("<td class='").append(result.isDeadlineSatisfied() ? "met" : "missed").append("'>");
                html.append(result.isDeadlineSatisfied() ? "Met" : "MISSED").append("</td>");
                html.append("<td class='").append(result.isBudgetSatisfied() ? "met" : "missed").append("'>");
                html.append(result.isBudgetSatisfied() ? "Met" : "EXCEEDED").append("</td>");
                html.append("</tr>\n");
            }
            html.append("</table>\n");
        }
        
        // Charts reference
        html.append("<h2>Visual Charts</h2>\n");
        html.append("<p>Charts are saved as PNG files in the output directory:</p>\n");
        html.append("<ul>\n");
        html.append("<li>execution_time_comparison.png</li>\n");
        html.append("<li>cost_comparison.png</li>\n");
        html.append("<li>deadline_miss_comparison.png</li>\n");
        html.append("<li>qos_satisfaction_comparison.png</li>\n");
        html.append("<li>makespan_comparison.png</li>\n");
        html.append("</ul>\n");
        
        html.append("</body>\n</html>");
        
        String filename = "simulation_report.html";
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputDirectory + "/" + filename))) {
            writer.print(html.toString());
        }
        System.out.println("HTML Report saved: " + filename);
    }
    
    /**
     * Generates a CSV report for data analysis.
     */
    public void generateCSVReport(Map<String, PerformanceMetrics> metricsMap) throws IOException {
        String filename = "performance_metrics.csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputDirectory + "/" + filename))) {
            // Header
            writer.println("Scheduler,TotalTasks,TotalTime,AvgTime,StdDevTime,Makespan," +
                    "TotalCost,AvgCost,StdDevCost,DeadlinesMet,DeadlineMissRate," +
                    "BudgetsMet,BudgetViolationRate,QoSSatisfied,QoSSatisfactionRate,Throughput");
            
            // Data
            for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
                PerformanceMetrics m = entry.getValue();
                writer.printf("%s,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%d,%.4f,%d,%.4f,%d,%.4f,%.6f%n",
                        entry.getKey(), m.totalTasks, m.totalExecutionTime, m.averageExecutionTime,
                        m.stdDevTime, m.makespan, m.totalCost, m.averageCost, m.stdDevCost,
                        m.deadlinesMet, m.deadlineMissRate, m.budgetsMet, m.budgetViolationRate,
                        m.qosSatisfied, m.qosSatisfactionRate, m.throughput);
            }
        }
        System.out.println("CSV Report saved: " + filename);
    }
    
    private String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
}
