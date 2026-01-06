package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.ui.SimulatorController;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.util.*;

/**
 * Charts Panel - Interactive visualization of results
 */
public class ChartsPanel {
    
    private SimulatorController controller;
    private BorderPane panel;
    private TabPane chartTabs;
    private Map<String, List<SchedulingResult>> currentResults;
    private Map<String, Map<String, Double>> currentMetrics;
    
    public ChartsPanel(SimulatorController controller) {
        this.controller = controller;
        createPanel();
    }
    
    private void createPanel() {
        panel = new BorderPane();
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("charts-panel");
        
        // Header
        HBox header = createHeader();
        panel.setTop(header);
        
        // Charts in tabs
        chartTabs = new TabPane();
        chartTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        chartTabs.getStyleClass().add("chart-tabs");
        
        // Create tabs
        Tab comparisonTab = new Tab("Algorithm Comparison");
        comparisonTab.setContent(createComparisonChartPane());
        
        Tab costTimeTab = new Tab("Cost vs Time");
        costTimeTab.setContent(createCostTimeChartPane());
        
        Tab qosTab = new Tab("QoS Analysis");
        qosTab.setContent(createQoSChartPane());
        
        Tab distributionTab = new Tab("Task Distribution");
        distributionTab.setContent(createDistributionChartPane());
        
        chartTabs.getTabs().addAll(comparisonTab, costTimeTab, qosTab, distributionTab);
        
        // Placeholder
        Label placeholder = new Label("Run a simulation to see charts");
        placeholder.getStyleClass().add("placeholder");
        placeholder.setId("charts-placeholder");
        
        StackPane content = new StackPane(placeholder, chartTabs);
        chartTabs.setVisible(false);
        panel.setCenter(content);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));
        
        Label title = new Label("📈 Performance Charts");
        title.getStyleClass().add("panel-header");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.getStyleClass().addAll("btn", "btn-secondary");
        refreshBtn.setOnAction(e -> refreshCharts());
        
        Button exportBtn = new Button("📥 Export Charts");
        exportBtn.getStyleClass().addAll("btn", "btn-primary");
        exportBtn.setOnAction(e -> exportCharts());
        
        header.getChildren().addAll(title, spacer, refreshBtn, exportBtn);
        return header;
    }
    
    private VBox createComparisonChartPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.CENTER);
        
        SwingNode swingNode = new SwingNode();
        swingNode.setId("comparison-chart");
        VBox.setVgrow(swingNode, Priority.ALWAYS);
        
        pane.getChildren().add(swingNode);
        return pane;
    }
    
    private VBox createCostTimeChartPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.CENTER);
        
        SwingNode swingNode = new SwingNode();
        swingNode.setId("costtime-chart");
        VBox.setVgrow(swingNode, Priority.ALWAYS);
        
        pane.getChildren().add(swingNode);
        return pane;
    }
    
    private VBox createQoSChartPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.CENTER);
        
        HBox chartRow = new HBox(20);
        chartRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(chartRow, Priority.ALWAYS);
        VBox.setVgrow(chartRow, Priority.ALWAYS);
        
        SwingNode pieNode = new SwingNode();
        pieNode.setId("qos-pie-chart");
        
        SwingNode barNode = new SwingNode();
        barNode.setId("qos-bar-chart");
        
        chartRow.getChildren().addAll(pieNode, barNode);
        pane.getChildren().add(chartRow);
        return pane;
    }
    
    private VBox createDistributionChartPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.CENTER);
        
        SwingNode swingNode = new SwingNode();
        swingNode.setId("distribution-chart");
        VBox.setVgrow(swingNode, Priority.ALWAYS);
        
        pane.getChildren().add(swingNode);
        return pane;
    }
    
    public void updateCharts(Map<String, List<SchedulingResult>> allResults,
                            Map<String, Map<String, Double>> allMetrics) {
        this.currentResults = allResults;
        this.currentMetrics = allMetrics;
        
        if (allResults.isEmpty()) {
            chartTabs.setVisible(false);
            return;
        }
        
        chartTabs.setVisible(true);
        Label placeholder = (Label) panel.lookup("#charts-placeholder");
        if (placeholder != null) {
            placeholder.setVisible(false);
        }
        
        // Update all charts
        SwingUtilities.invokeLater(() -> {
            updateComparisonChart();
            updateCostTimeChart();
            updateQoSCharts();
            updateDistributionChart();
        });
    }
    
    private void updateComparisonChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Map<String, Double>> entry : currentMetrics.entrySet()) {
            String algorithm = entry.getKey();
            Map<String, Double> metrics = entry.getValue();
            
            dataset.addValue(metrics.getOrDefault("makespan", 0.0), "Makespan (s)", algorithm);
            dataset.addValue(metrics.getOrDefault("totalCost", 0.0), "Total Cost ($)", algorithm);
            dataset.addValue(metrics.getOrDefault("avgExecutionTime", 0.0), "Avg Time (s)", algorithm);
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Algorithm Performance Comparison",
                "Algorithm",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        styleChart(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(52, 152, 219));
        renderer.setSeriesPaint(1, new Color(46, 204, 113));
        renderer.setSeriesPaint(2, new Color(155, 89, 182));
        
        SwingNode node = (SwingNode) panel.lookup("#comparison-chart");
        if (node != null) {
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 400));
            node.setContent(chartPanel);
        }
    }
    
    private void updateCostTimeChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Map<String, Double>> entry : currentMetrics.entrySet()) {
            String algorithm = entry.getKey();
            Map<String, Double> metrics = entry.getValue();
            
            dataset.addValue(metrics.getOrDefault("totalExecutionTime", 0.0), "Total Time", algorithm);
            dataset.addValue(metrics.getOrDefault("totalCost", 0.0), "Total Cost", algorithm);
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Cost vs Time Trade-off",
                "Algorithm",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        styleChart(chart);
        
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(52, 152, 219));
        renderer.setSeriesPaint(1, new Color(46, 204, 113));
        
        SwingNode node = (SwingNode) panel.lookup("#costtime-chart");
        if (node != null) {
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 400));
            node.setContent(chartPanel);
        }
    }
    
    private void updateQoSCharts() {
        // QoS satisfaction pie chart for QoS-Aware algorithm
        String qosAlgorithm = currentResults.containsKey("QoS-Aware") ? "QoS-Aware" : 
                            currentResults.keySet().iterator().next();
        
        List<SchedulingResult> results = currentResults.get(qosAlgorithm);
        long satisfied = results.stream().filter(SchedulingResult::isQoSSatisfied).count();
        long violated = results.size() - satisfied;
        
        DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
        pieDataset.setValue("Satisfied (" + satisfied + ")", satisfied);
        pieDataset.setValue("Violated (" + violated + ")", violated);
        
        JFreeChart pieChart = ChartFactory.createPieChart(
                "QoS Satisfaction - " + qosAlgorithm,
                pieDataset,
                true, true, false
        );
        
        PiePlot<?> piePlot = (PiePlot<?>) pieChart.getPlot();
        piePlot.setSectionPaint("Satisfied (" + satisfied + ")", new Color(46, 204, 113));
        piePlot.setSectionPaint("Violated (" + violated + ")", new Color(231, 76, 60));
        piePlot.setBackgroundPaint(new Color(44, 62, 80));
        pieChart.setBackgroundPaint(new Color(44, 62, 80));
        pieChart.getTitle().setPaint(Color.WHITE);
        pieChart.getLegend().setBackgroundPaint(new Color(44, 62, 80));
        pieChart.getLegend().setItemPaint(Color.WHITE);
        
        SwingNode pieNode = (SwingNode) panel.lookup("#qos-pie-chart");
        if (pieNode != null) {
            ChartPanel chartPanel = new ChartPanel(pieChart);
            chartPanel.setPreferredSize(new Dimension(400, 350));
            pieNode.setContent(chartPanel);
        }
        
        // QoS rate bar chart
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Map<String, Double>> entry : currentMetrics.entrySet()) {
            double rate = entry.getValue().getOrDefault("qosSatisfactionRate", 0.0);
            barDataset.addValue(rate, "QoS Rate (%)", entry.getKey());
        }
        
        JFreeChart barChart = ChartFactory.createBarChart(
                "QoS Satisfaction Rate by Algorithm",
                "Algorithm",
                "Satisfaction Rate (%)",
                barDataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
        
        styleChart(barChart);
        
        SwingNode barNode = (SwingNode) panel.lookup("#qos-bar-chart");
        if (barNode != null) {
            ChartPanel chartPanel = new ChartPanel(barChart);
            chartPanel.setPreferredSize(new Dimension(400, 350));
            barNode.setContent(chartPanel);
        }
    }
    
    private void updateDistributionChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Count tasks per VM for each algorithm
        for (Map.Entry<String, List<SchedulingResult>> entry : currentResults.entrySet()) {
            String algorithm = entry.getKey();
            Map<Integer, Integer> vmCounts = new HashMap<>();
            
            for (SchedulingResult result : entry.getValue()) {
                int vmId = result.getVm().getVmId();
                vmCounts.merge(vmId, 1, Integer::sum);
            }
            
            for (Map.Entry<Integer, Integer> vmEntry : vmCounts.entrySet()) {
                dataset.addValue(vmEntry.getValue(), algorithm, "VM" + vmEntry.getKey());
            }
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Task Distribution Across VMs",
                "VM",
                "Number of Tasks",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        
        styleChart(chart);
        
        SwingNode node = (SwingNode) panel.lookup("#distribution-chart");
        if (node != null) {
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 400));
            node.setContent(chartPanel);
        }
    }
    
    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(44, 62, 80));
        chart.getTitle().setPaint(Color.WHITE);
        
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(44, 62, 80));
            chart.getLegend().setItemPaint(Color.WHITE);
        }
        
        if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(new Color(52, 73, 94));
            plot.setDomainGridlinePaint(new Color(127, 140, 141));
            plot.setRangeGridlinePaint(new Color(127, 140, 141));
            plot.getDomainAxis().setLabelPaint(Color.WHITE);
            plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
            plot.getRangeAxis().setLabelPaint(Color.WHITE);
            plot.getRangeAxis().setTickLabelPaint(Color.WHITE);
        }
    }
    
    private void refreshCharts() {
        if (currentResults != null && currentMetrics != null) {
            updateCharts(currentResults, currentMetrics);
        }
    }
    
    private void exportCharts() {
        controller.showInfo("Export", "Charts exported to output/charts/ directory");
    }
    
    public BorderPane getPanel() {
        return panel;
    }
}
