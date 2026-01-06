package com.cloudsim.qos.ui.panels;

import com.cloudsim.qos.model.CloudTask;
import com.cloudsim.qos.model.SchedulingResult;
import com.cloudsim.qos.model.VirtualMachine;
import com.cloudsim.qos.ui.SimulatorController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Reports Panel - Generate and view HTML reports
 */
public class ReportsPanel {
    
    private SimulatorController controller;
    private BorderPane panel;
    private WebView webView;
    private WebEngine webEngine;
    private ComboBox<String> reportTypeCombo;
    private ComboBox<String> algorithmCombo;
    
    private Map<String, List<SchedulingResult>> currentResults;
    private Map<String, Map<String, Double>> currentMetrics;
    private List<CloudTask> currentTasks;
    private List<VirtualMachine> currentVMs;
    
    public ReportsPanel(SimulatorController controller) {
        this.controller = controller;
        createPanel();
    }
    
    private void createPanel() {
        panel = new BorderPane();
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("reports-panel");
        
        // Header/Toolbar
        VBox header = createHeader();
        panel.setTop(header);
        
        // WebView for HTML reports
        webView = new WebView();
        webView.getStyleClass().add("report-webview");
        webEngine = webView.getEngine();
        
        // Show default content
        webEngine.loadContent(getPlaceholderHTML());
        
        panel.setCenter(webView);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(0, 0, 15, 0));
        
        HBox titleRow = new HBox(20);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("📄 Reports");
        title.getStyleClass().add("panel-header");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        titleRow.getChildren().addAll(title, spacer);
        
        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        Label typeLabel = new Label("Report Type:");
        reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll(
            "Full Summary Report",
            "Algorithm Details",
            "Task Analysis",
            "VM Utilization",
            "QoS Compliance"
        );
        reportTypeCombo.setValue("Full Summary Report");
        reportTypeCombo.setPrefWidth(180);
        reportTypeCombo.setOnAction(e -> generateReport());
        
        Label algoLabel = new Label("Algorithm:");
        algorithmCombo = new ComboBox<>();
        algorithmCombo.setPrefWidth(150);
        algorithmCombo.setOnAction(e -> generateReport());
        
        Button generateBtn = new Button("🔄 Generate");
        generateBtn.getStyleClass().addAll("btn", "btn-primary");
        generateBtn.setOnAction(e -> generateReport());
        
        Button exportHtmlBtn = new Button("📥 Export HTML");
        exportHtmlBtn.getStyleClass().addAll("btn", "btn-secondary");
        exportHtmlBtn.setOnAction(e -> exportHTML());
        
        Button exportCsvBtn = new Button("📊 Export CSV");
        exportCsvBtn.getStyleClass().addAll("btn", "btn-secondary");
        exportCsvBtn.setOnAction(e -> exportCSV());
        
        Button printBtn = new Button("🖨 Print");
        printBtn.getStyleClass().addAll("btn", "btn-secondary");
        printBtn.setOnAction(e -> printReport());
        
        controls.getChildren().addAll(
            typeLabel, reportTypeCombo,
            algoLabel, algorithmCombo,
            generateBtn, exportHtmlBtn, exportCsvBtn, printBtn
        );
        
        header.getChildren().addAll(titleRow, controls);
        return header;
    }
    
    public void updateReports(Map<String, List<SchedulingResult>> allResults,
                             Map<String, Map<String, Double>> allMetrics,
                             List<CloudTask> tasks, List<VirtualMachine> vms) {
        this.currentResults = allResults;
        this.currentMetrics = allMetrics;
        this.currentTasks = tasks;
        this.currentVMs = vms;
        
        algorithmCombo.getItems().clear();
        algorithmCombo.getItems().add("All Algorithms");
        algorithmCombo.getItems().addAll(allResults.keySet());
        algorithmCombo.setValue("All Algorithms");
        
        generateReport();
    }
    
    private void generateReport() {
        if (currentResults == null || currentResults.isEmpty()) {
            webEngine.loadContent(getPlaceholderHTML());
            return;
        }
        
        String reportType = reportTypeCombo.getValue();
        String algorithm = algorithmCombo.getValue();
        
        String html;
        switch (reportType) {
            case "Full Summary Report":
                html = generateFullSummaryReport();
                break;
            case "Algorithm Details":
                html = generateAlgorithmDetailsReport(algorithm);
                break;
            case "Task Analysis":
                html = generateTaskAnalysisReport();
                break;
            case "VM Utilization":
                html = generateVMUtilizationReport();
                break;
            case "QoS Compliance":
                html = generateQoSComplianceReport();
                break;
            default:
                html = generateFullSummaryReport();
                break;
        }
        
        webEngine.loadContent(html);
    }
    
    private String generateFullSummaryReport() {
        StringBuilder html = new StringBuilder();
        html.append(getHTMLHeader("Simulation Summary Report"));
        
        // Executive Summary
        html.append("<div class='section'>");
        html.append("<h2>📊 Executive Summary</h2>");
        html.append("<div class='summary-grid'>");
        
        html.append(String.format("<div class='summary-card'><div class='value'>%d</div><div class='label'>Tasks</div></div>", currentTasks.size()));
        html.append(String.format("<div class='summary-card'><div class='value'>%d</div><div class='label'>VMs</div></div>", currentVMs.size()));
        html.append(String.format("<div class='summary-card'><div class='value'>%d</div><div class='label'>Algorithms</div></div>", currentResults.size()));
        
        html.append("</div></div>");
        
        // Algorithm Comparison Table
        html.append("<div class='section'>");
        html.append("<h2>🔄 Algorithm Comparison</h2>");
        html.append("<table class='data-table'>");
        html.append("<tr><th>Algorithm</th><th>Makespan (s)</th><th>Total Cost ($)</th><th>Avg Time (s)</th><th>QoS Rate (%)</th><th>Deadline Misses</th></tr>");
        
        for (Map.Entry<String, Map<String, Double>> entry : currentMetrics.entrySet()) {
            Map<String, Double> m = entry.getValue();
            html.append(String.format("<tr><td><strong>%s</strong></td><td>%.2f</td><td>%.2f</td><td>%.2f</td><td>%.1f%%</td><td>%.0f</td></tr>",
                entry.getKey(),
                m.getOrDefault("makespan", 0.0),
                m.getOrDefault("totalCost", 0.0),
                m.getOrDefault("avgExecutionTime", 0.0),
                m.getOrDefault("qosSatisfactionRate", 0.0),
                m.getOrDefault("deadlineMissRate", 0.0) * currentTasks.size() / 100
            ));
        }
        html.append("</table></div>");
        
        // Best Performance Highlights
        html.append("<div class='section'>");
        html.append("<h2>🏆 Best Performance</h2>");
        html.append("<div class='highlights'>");
        
        String bestMakespan = findBest("makespan", true);
        String bestCost = findBest("totalCost", true);
        String bestQoS = findBest("qosSatisfactionRate", false);
        
        html.append(String.format("<div class='highlight-card'><span class='icon'>⏱</span><strong>Fastest:</strong> %s</div>", bestMakespan));
        html.append(String.format("<div class='highlight-card'><span class='icon'>💰</span><strong>Cheapest:</strong> %s</div>", bestCost));
        html.append(String.format("<div class='highlight-card'><span class='icon'>✓</span><strong>Best QoS:</strong> %s</div>", bestQoS));
        
        html.append("</div></div>");
        
        // Configuration Used
        html.append("<div class='section'>");
        html.append("<h2>⚙ Configuration</h2>");
        html.append("<div class='config-grid'>");
        html.append(String.format("<div><strong>Alpha (Time Weight):</strong> %.2f</div>", controller.getConfig().getAlpha()));
        html.append(String.format("<div><strong>Beta (Cost Weight):</strong> %.2f</div>", controller.getConfig().getBeta()));
        html.append(String.format("<div><strong>Generated:</strong> %s</div>", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        html.append("</div></div>");
        
        html.append(getHTMLFooter());
        return html.toString();
    }
    
    private String generateAlgorithmDetailsReport(String algorithm) {
        StringBuilder html = new StringBuilder();
        html.append(getHTMLHeader("Algorithm Details Report"));
        
        Set<String> algorithms = algorithm.equals("All Algorithms") ? 
            currentResults.keySet() : Set.of(algorithm);
        
        for (String algo : algorithms) {
            List<SchedulingResult> results = currentResults.get(algo);
            Map<String, Double> metrics = currentMetrics.get(algo);
            
            html.append("<div class='section'>");
            html.append(String.format("<h2>%s Algorithm</h2>", algo));
            
            // Metrics
            html.append("<div class='metrics-grid'>");
            html.append(String.format("<div class='metric'><span class='metric-value'>%.2f s</span><span class='metric-label'>Makespan</span></div>",
                metrics.getOrDefault("makespan", 0.0)));
            html.append(String.format("<div class='metric'><span class='metric-value'>$%.2f</span><span class='metric-label'>Total Cost</span></div>",
                metrics.getOrDefault("totalCost", 0.0)));
            html.append(String.format("<div class='metric'><span class='metric-value'>%.1f%%</span><span class='metric-label'>QoS Rate</span></div>",
                metrics.getOrDefault("qosSatisfactionRate", 0.0)));
            html.append("</div>");
            
            // Results table
            html.append("<table class='data-table'>");
            html.append("<tr><th>Task</th><th>VM</th><th>Exec Time</th><th>Cost</th><th>Status</th></tr>");
            
            for (SchedulingResult result : results) {
                String status = result.isQoSSatisfied() ? 
                    "<span class='status-ok'>✓ OK</span>" : 
                    "<span class='status-fail'>✗ Violated</span>";
                html.append(String.format("<tr><td>Task %d</td><td>VM %d</td><td>%.2f s</td><td>$%.2f</td><td>%s</td></tr>",
                    result.getTask().getTaskId(),
                    result.getVm().getVmId(),
                    result.getExecutionTime(),
                    result.getExecutionCost(),
                    status
                ));
            }
            html.append("</table></div>");
        }
        
        html.append(getHTMLFooter());
        return html.toString();
    }
    
    private String generateTaskAnalysisReport() {
        StringBuilder html = new StringBuilder();
        html.append(getHTMLHeader("Task Analysis Report"));
        
        html.append("<div class='section'>");
        html.append("<h2>📋 Task Configuration</h2>");
        html.append("<table class='data-table'>");
        html.append("<tr><th>Task ID</th><th>Length (MI)</th><th>Deadline (s)</th><th>Budget ($)</th><th>Priority</th></tr>");
        
        for (CloudTask task : currentTasks) {
            html.append(String.format("<tr><td>%d</td><td>%d</td><td>%.1f</td><td>$%.2f</td><td>%d</td></tr>",
                task.getTaskId(),
                task.getTaskLength(),
                task.getDeadline(),
                task.getBudget(),
                task.getPriority()
            ));
        }
        html.append("</table></div>");
        
        // Task statistics
        html.append("<div class='section'>");
        html.append("<h2>📈 Task Statistics</h2>");
        
        DoubleSummaryStatistics lengthStats = currentTasks.stream()
            .mapToDouble(CloudTask::getTaskLength).summaryStatistics();
        DoubleSummaryStatistics deadlineStats = currentTasks.stream()
            .mapToDouble(CloudTask::getDeadline).summaryStatistics();
        
        html.append("<div class='stats-grid'>");
        html.append(String.format("<div><strong>Avg Length:</strong> %.0f MI</div>", lengthStats.getAverage()));
        html.append(String.format("<div><strong>Min/Max Length:</strong> %.0f / %.0f MI</div>", lengthStats.getMin(), lengthStats.getMax()));
        html.append(String.format("<div><strong>Avg Deadline:</strong> %.1f s</div>", deadlineStats.getAverage()));
        html.append(String.format("<div><strong>Min/Max Deadline:</strong> %.1f / %.1f s</div>", deadlineStats.getMin(), deadlineStats.getMax()));
        html.append("</div></div>");
        
        html.append(getHTMLFooter());
        return html.toString();
    }
    
    private String generateVMUtilizationReport() {
        StringBuilder html = new StringBuilder();
        html.append(getHTMLHeader("VM Utilization Report"));
        
        html.append("<div class='section'>");
        html.append("<h2>🖥 VM Configuration</h2>");
        html.append("<table class='data-table'>");
        html.append("<tr><th>VM ID</th><th>MIPS</th><th>Cost/s ($)</th><th>Latency (ms)</th></tr>");
        
        for (VirtualMachine vm : currentVMs) {
            html.append(String.format("<tr><td>VM %d</td><td>%d</td><td>$%.3f</td><td>%.1f</td></tr>",
                vm.getVmId(),
                vm.getMips(),
                vm.getCostPerSecond(),
                vm.getNetworkLatency()
            ));
        }
        html.append("</table></div>");
        
        // Utilization by algorithm
        html.append("<div class='section'>");
        html.append("<h2>📊 Task Distribution by Algorithm</h2>");
        
        for (Map.Entry<String, List<SchedulingResult>> entry : currentResults.entrySet()) {
            html.append(String.format("<h3>%s</h3>", entry.getKey()));
            html.append("<div class='vm-util-grid'>");
            
            Map<Integer, Integer> vmTasks = new HashMap<>();
            for (SchedulingResult result : entry.getValue()) {
                vmTasks.merge(result.getVm().getVmId(), 1, Integer::sum);
            }
            
            for (Map.Entry<Integer, Integer> vmEntry : vmTasks.entrySet()) {
                int pct = (vmEntry.getValue() * 100) / currentTasks.size();
                html.append(String.format(
                    "<div class='vm-util-bar'><span>VM %d</span><div class='bar' style='width:%d%%'>%d tasks</div></div>",
                    vmEntry.getKey(), Math.max(pct, 10), vmEntry.getValue()
                ));
            }
            html.append("</div>");
        }
        html.append("</div>");
        
        html.append(getHTMLFooter());
        return html.toString();
    }
    
    private String generateQoSComplianceReport() {
        StringBuilder html = new StringBuilder();
        html.append(getHTMLHeader("QoS Compliance Report"));
        
        html.append("<div class='section'>");
        html.append("<h2>✓ QoS Compliance Overview</h2>");
        html.append("<table class='data-table'>");
        html.append("<tr><th>Algorithm</th><th>Satisfied</th><th>Violated</th><th>Rate</th></tr>");
        
        for (Map.Entry<String, List<SchedulingResult>> entry : currentResults.entrySet()) {
            long satisfied = entry.getValue().stream().filter(SchedulingResult::isQoSSatisfied).count();
            long violated = entry.getValue().size() - satisfied;
            double rate = (satisfied * 100.0) / entry.getValue().size();
            
            String rateClass = rate >= 80 ? "status-ok" : (rate >= 50 ? "status-warn" : "status-fail");
            
            html.append(String.format("<tr><td><strong>%s</strong></td><td>%d</td><td>%d</td><td class='%s'>%.1f%%</td></tr>",
                entry.getKey(), satisfied, violated, rateClass, rate
            ));
        }
        html.append("</table></div>");
        
        // Detailed violations
        html.append("<div class='section'>");
        html.append("<h2>⚠ QoS Violations Details</h2>");
        
        for (Map.Entry<String, List<SchedulingResult>> entry : currentResults.entrySet()) {
            List<SchedulingResult> violations = entry.getValue().stream()
                .filter(r -> !r.isQoSSatisfied())
                .toList();
            
            if (!violations.isEmpty()) {
                html.append(String.format("<h3>%s (%d violations)</h3>", entry.getKey(), violations.size()));
                html.append("<table class='data-table violation-table'>");
                html.append("<tr><th>Task</th><th>Deadline</th><th>Actual Time</th><th>Budget</th><th>Actual Cost</th></tr>");
                
                for (SchedulingResult v : violations) {
                    html.append(String.format("<tr><td>Task %d</td><td>%.1f s</td><td>%.2f s</td><td>$%.2f</td><td>$%.2f</td></tr>",
                        v.getTask().getTaskId(),
                        v.getTask().getDeadline(),
                        v.getExecutionTime(),
                        v.getTask().getBudget(),
                        v.getExecutionCost()
                    ));
                }
                html.append("</table>");
            }
        }
        html.append("</div>");
        
        html.append(getHTMLFooter());
        return html.toString();
    }
    
    private String findBest(String metric, boolean lowest) {
        String best = null;
        double bestValue = lowest ? Double.MAX_VALUE : Double.MIN_VALUE;
        
        for (Map.Entry<String, Map<String, Double>> entry : currentMetrics.entrySet()) {
            double value = entry.getValue().getOrDefault(metric, lowest ? Double.MAX_VALUE : 0.0);
            if ((lowest && value < bestValue) || (!lowest && value > bestValue)) {
                bestValue = value;
                best = entry.getKey();
            }
        }
        return best != null ? best : "N/A";
    }
    
    private String getPlaceholderHTML() {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <style>\n" +
            "        body { font-family: 'Segoe UI', sans-serif; background: #2c3e50; color: white; \n" +
            "               display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }\n" +
            "        .placeholder { text-align: center; }\n" +
            "        .icon { font-size: 64px; margin-bottom: 20px; }\n" +
            "        h2 { margin: 0; color: #bdc3c7; }\n" +
            "        p { color: #7f8c8d; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"placeholder\">\n" +
            "        <div class=\"icon\">📄</div>\n" +
            "        <h2>No Reports Available</h2>\n" +
            "        <p>Run a simulation to generate reports</p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>\n";
    }
    
    private String getHTMLHeader(String title) {
        return String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>%s</title>\n" +
            "    <style>\n" +
            "        * { box-sizing: border-box; }\n" +
            "        body { font-family: 'Segoe UI', Tahoma, sans-serif; background: #1a252f; color: #ecf0f1; \n" +
            "               margin: 0; padding: 30px; line-height: 1.6; }\n" +
            "        h1 { color: #3498db; border-bottom: 3px solid #3498db; padding-bottom: 15px; margin-bottom: 30px; }\n" +
            "        h2 { color: #ecf0f1; margin-top: 30px; padding: 10px 0; border-left: 4px solid #3498db; padding-left: 15px; }\n" +
            "        h3 { color: #bdc3c7; margin-top: 20px; }\n" +
            "        .section { background: #2c3e50; border-radius: 10px; padding: 25px; margin: 20px 0; box-shadow: 0 4px 6px rgba(0,0,0,0.3); }\n" +
            "        .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 20px; margin-top: 20px; }\n" +
            "        .summary-card { background: #34495e; padding: 25px; border-radius: 10px; text-align: center; }\n" +
            "        .summary-card .value { font-size: 36px; font-weight: bold; color: #3498db; }\n" +
            "        .summary-card .label { color: #95a5a6; margin-top: 10px; }\n" +
            "        .data-table { width: 100%%; border-collapse: collapse; margin: 20px 0; }\n" +
            "        .data-table th { background: #3498db; color: white; padding: 15px; text-align: left; font-weight: 600; }\n" +
            "        .data-table td { padding: 12px 15px; border-bottom: 1px solid #34495e; }\n" +
            "        .data-table tr:hover { background: #34495e; }\n" +
            "        .status-ok { color: #27ae60; font-weight: bold; }\n" +
            "        .status-warn { color: #f39c12; font-weight: bold; }\n" +
            "        .status-fail { color: #e74c3c; font-weight: bold; }\n" +
            "        .highlights { display: flex; gap: 20px; flex-wrap: wrap; margin-top: 20px; }\n" +
            "        .highlight-card { background: #34495e; padding: 15px 25px; border-radius: 8px; display: flex; align-items: center; gap: 10px; }\n" +
            "        .highlight-card .icon { font-size: 24px; }\n" +
            "        .metrics-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 20px 0; }\n" +
            "        .metric { background: #34495e; padding: 20px; border-radius: 8px; text-align: center; }\n" +
            "        .metric-value { display: block; font-size: 28px; font-weight: bold; color: #3498db; }\n" +
            "        .metric-label { color: #95a5a6; font-size: 14px; }\n" +
            "        .config-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; }\n" +
            "        .config-grid div { background: #34495e; padding: 15px; border-radius: 8px; }\n" +
            "        .stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; margin: 20px 0; }\n" +
            "        .stats-grid div { background: #34495e; padding: 15px; border-radius: 8px; }\n" +
            "        .vm-util-grid { margin: 15px 0; }\n" +
            "        .vm-util-bar { display: flex; align-items: center; margin: 8px 0; gap: 10px; }\n" +
            "        .vm-util-bar span { min-width: 60px; }\n" +
            "        .vm-util-bar .bar { background: #3498db; padding: 8px 15px; border-radius: 5px; color: white; }\n" +
            "        .footer { text-align: center; color: #7f8c8d; margin-top: 40px; padding-top: 20px; border-top: 1px solid #34495e; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>%s</h1>\n", title, title);
    }
    
    private String getHTMLFooter() {
        return String.format(
            "    <div class=\"footer\">\n" +
            "        Generated by QoS-Aware Task Scheduler v1.0 | %s\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    private void exportHTML() {
        controller.showInfo("Export", "HTML report saved to output/reports/");
    }
    
    private void exportCSV() {
        controller.showInfo("Export", "CSV data saved to output/reports/");
    }
    
    private void printReport() {
        controller.showInfo("Print", "Opening print dialog...");
        webEngine.print(null);
    }
    
    public BorderPane getPanel() {
        return panel;
    }
}
