package com.cloudsim.qos.visualization;

import com.cloudsim.qos.evaluation.PerformanceEvaluationModule.PerformanceMetrics;
import com.cloudsim.qos.model.SchedulingResult;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Module 7: Result Visualization Module
 * 
 * This module presents scheduling results and performance metrics
 * in an understandable visual format using charts and graphs.
 */
public class ResultVisualizationModule {
    
    private String outputDirectory;
    private int chartWidth = 800;
    private int chartHeight = 600;
    
    public ResultVisualizationModule(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        new File(outputDirectory).mkdirs();
    }
    
    /**
     * Generates all comparison charts.
     */
    public void generateAllCharts(Map<String, PerformanceMetrics> metricsMap) throws IOException {
        System.out.println("\nGenerating visualization charts...");
        
        generateExecutionTimeChart(metricsMap);
        generateCostComparisonChart(metricsMap);
        generateDeadlineMissChart(metricsMap);
        generateQoSSatisfactionChart(metricsMap);
        generateMakespanChart(metricsMap);
        generateThroughputChart(metricsMap);
        
        System.out.println("Charts saved to: " + outputDirectory);
    }
    
    /**
     * Generates bar chart for average execution time comparison.
     */
    public void generateExecutionTimeChart(Map<String, PerformanceMetrics> metricsMap) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            dataset.addValue(entry.getValue().averageExecutionTime, "Avg Execution Time (s)", 
                    shortenName(entry.getKey()));
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Average Execution Time Comparison",
                "Scheduler",
                "Time (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        customizeChart(chart);
        saveChart(chart, "execution_time_comparison.png");
    }
    
    /**
     * Generates bar chart for total cost comparison.
     */
    public void generateCostComparisonChart(Map<String, PerformanceMetrics> metricsMap) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            dataset.addValue(entry.getValue().totalCost, "Total Cost ($)", 
                    shortenName(entry.getKey()));
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Total Execution Cost Comparison",
                "Scheduler",
                "Cost ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        customizeChart(chart);
        saveChart(chart, "cost_comparison.png");
    }
    
    /**
     * Generates bar chart for deadline miss rate comparison.
     */
    public void generateDeadlineMissChart(Map<String, PerformanceMetrics> metricsMap) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            dataset.addValue(entry.getValue().deadlineMissRate, "Deadline Miss Rate (%)", 
                    shortenName(entry.getKey()));
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Deadline Miss Rate Comparison",
                "Scheduler",
                "Miss Rate (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        customizeChart(chart);
        saveChart(chart, "deadline_miss_comparison.png");
    }
    
    /**
     * Generates bar chart for QoS satisfaction rate comparison.
     */
    public void generateQoSSatisfactionChart(Map<String, PerformanceMetrics> metricsMap) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            dataset.addValue(entry.getValue().qosSatisfactionRate, "QoS Satisfaction (%)", 
                    shortenName(entry.getKey()));
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "QoS Satisfaction Rate Comparison",
                "Scheduler",
                "Satisfaction Rate (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        customizeChart(chart);
        saveChart(chart, "qos_satisfaction_comparison.png");
    }
    
    /**
     * Generates bar chart for makespan comparison.
     */
    public void generateMakespanChart(Map<String, PerformanceMetrics> metricsMap) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            dataset.addValue(entry.getValue().makespan, "Makespan (s)", 
                    shortenName(entry.getKey()));
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Makespan Comparison",
                "Scheduler",
                "Makespan (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        customizeChart(chart);
        saveChart(chart, "makespan_comparison.png");
    }
    
    /**
     * Generates bar chart for throughput comparison.
     */
    public void generateThroughputChart(Map<String, PerformanceMetrics> metricsMap) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            dataset.addValue(entry.getValue().throughput, "Throughput (tasks/s)", 
                    shortenName(entry.getKey()));
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Throughput Comparison",
                "Scheduler",
                "Throughput (tasks/second)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        customizeChart(chart);
        saveChart(chart, "throughput_comparison.png");
    }
    
    /**
     * Generates a combined metrics chart.
     */
    public void generateCombinedMetricsChart(Map<String, PerformanceMetrics> metricsMap) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Normalize values to 0-100 scale for comparison
        double maxTime = 0, maxCost = 0;
        for (PerformanceMetrics m : metricsMap.values()) {
            maxTime = Math.max(maxTime, m.averageExecutionTime);
            maxCost = Math.max(maxCost, m.totalCost);
        }
        
        for (Map.Entry<String, PerformanceMetrics> entry : metricsMap.entrySet()) {
            String name = shortenName(entry.getKey());
            PerformanceMetrics m = entry.getValue();
            
            dataset.addValue(m.averageExecutionTime / maxTime * 100, "Norm. Time", name);
            dataset.addValue(m.totalCost / maxCost * 100, "Norm. Cost", name);
            dataset.addValue(100 - m.deadlineMissRate, "Deadline Met %", name);
            dataset.addValue(m.qosSatisfactionRate, "QoS Sat %", name);
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Combined Performance Metrics (Normalized)",
                "Scheduler",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        customizeChart(chart);
        saveChart(chart, "combined_metrics.png");
    }
    
    /**
     * Generates task distribution chart for a scheduler.
     */
    public void generateTaskDistributionChart(String schedulerName, List<SchedulingResult> results) 
            throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Count tasks per VM
        Map<Integer, Integer> vmTaskCount = new java.util.TreeMap<>();
        for (SchedulingResult result : results) {
            int vmId = result.getVm().getVmId();
            vmTaskCount.merge(vmId, 1, Integer::sum);
        }
        
        for (Map.Entry<Integer, Integer> entry : vmTaskCount.entrySet()) {
            dataset.addValue(entry.getValue(), "Tasks", "VM " + entry.getKey());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Task Distribution - " + schedulerName,
                "Virtual Machine",
                "Number of Tasks",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        customizeChart(chart);
        String filename = "task_distribution_" + schedulerName.toLowerCase().replace(" ", "_") + ".png";
        saveChart(chart, filename);
    }
    
    /**
     * Customizes chart appearance.
     */
    private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // Customize renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        
        // Set colors
        renderer.setSeriesPaint(0, new Color(79, 129, 189));
        renderer.setSeriesPaint(1, new Color(192, 80, 77));
        renderer.setSeriesPaint(2, new Color(155, 187, 89));
        renderer.setSeriesPaint(3, new Color(128, 100, 162));
        
        // Rotate x-axis labels if needed
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
    }
    
    /**
     * Saves chart to file.
     */
    private void saveChart(JFreeChart chart, String filename) throws IOException {
        File outputFile = new File(outputDirectory, filename);
        ChartUtils.saveChartAsPNG(outputFile, chart, chartWidth, chartHeight);
        System.out.println("  Saved: " + filename);
    }
    
    /**
     * Shortens scheduler name for chart labels.
     */
    private String shortenName(String name) {
        if (name.length() > 20) {
            if (name.contains("QoS")) return "QoS-Aware";
            if (name.contains("FCFS")) return "FCFS";
            if (name.contains("Random")) return "Random";
            if (name.contains("Min-Min")) return "Min-Min";
            return name.substring(0, 17) + "...";
        }
        return name;
    }
    
    /**
     * Prints ASCII bar chart to console (for environments without GUI).
     */
    public void printASCIIChart(String title, Map<String, Double> data, String unit) {
        System.out.println("\n" + title);
        System.out.println("-".repeat(60));
        
        double maxValue = data.values().stream().mapToDouble(d -> d).max().orElse(1);
        int maxBarLength = 40;
        
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int barLength = (int) ((entry.getValue() / maxValue) * maxBarLength);
            String bar = "█".repeat(Math.max(1, barLength));
            System.out.println(String.format("%-20s %s %.4f %s",
                    shortenName(entry.getKey()), bar, entry.getValue(), unit));
        }
        
        System.out.println();
    }
    
    /**
     * Generates ASCII comparison charts for console output.
     */
    public void printASCIIComparison(Map<String, PerformanceMetrics> metricsMap) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ASCII VISUALIZATION");
        System.out.println("=".repeat(70));
        
        // Execution Time
        Map<String, Double> timeData = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, PerformanceMetrics> e : metricsMap.entrySet()) {
            timeData.put(e.getKey(), e.getValue().averageExecutionTime);
        }
        printASCIIChart("Average Execution Time", timeData, "s");
        
        // Total Cost
        Map<String, Double> costData = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, PerformanceMetrics> e : metricsMap.entrySet()) {
            costData.put(e.getKey(), e.getValue().totalCost);
        }
        printASCIIChart("Total Cost", costData, "$");
        
        // QoS Satisfaction
        Map<String, Double> qosData = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, PerformanceMetrics> e : metricsMap.entrySet()) {
            qosData.put(e.getKey(), e.getValue().qosSatisfactionRate);
        }
        printASCIIChart("QoS Satisfaction Rate", qosData, "%");
        
        // Deadline Miss Rate
        Map<String, Double> dlData = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, PerformanceMetrics> e : metricsMap.entrySet()) {
            dlData.put(e.getKey(), e.getValue().deadlineMissRate);
        }
        printASCIIChart("Deadline Miss Rate (lower is better)", dlData, "%");
        
        System.out.println("=".repeat(70));
    }
    
    public void setChartDimensions(int width, int height) {
        this.chartWidth = width;
        this.chartHeight = height;
    }
}
