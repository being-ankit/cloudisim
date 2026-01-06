# QoS-Aware Multi-Objective Task Scheduling Simulator for Cloud Computing

A comprehensive simulation framework for evaluating Quality of Service (QoS) aware task scheduling algorithms in cloud computing environments, built using Java, CloudSim Plus, and JavaFX.

## 🖥️ Screenshots

The application features a modern dark-themed GUI with intuitive navigation:

- **Welcome Panel** - Quick start guide and overview
- **Configuration Panel** - Set scheduling parameters (α, β weights)
- **Task Management** - Add, edit, delete, and import tasks
- **VM Management** - Configure virtual machines
- **Simulation** - Run and monitor simulations in real-time
- **Results** - View detailed scheduling results
- **Charts** - Interactive visualizations (bar, pie, line charts)
- **Reports** - Generate HTML, PDF, and CSV reports
- **Comparison** - Side-by-side algorithm comparison
- **History** - Track and compare past simulation runs
- **Settings** - Customize theme, preferences, and export options

## Project Overview

This project implements a modular simulation system that demonstrates the effectiveness of QoS-aware scheduling compared to traditional baseline algorithms. It considers multiple objectives including execution time, cost, deadline satisfaction, and budget constraints.

### Key Features

- **Modern JavaFX GUI**: Professional dark/light themed interface
- **Multi-Objective Optimization**: Balances execution time and cost using weighted objective function
- **QoS Constraints**: Considers deadlines and budget limits for each task
- **Multiple Scheduling Algorithms**:
  - QoS-Aware Scheduler (proposed)
  - First Come First Serve (FCFS)
  - Random Scheduling
  - Min-Min Scheduling
- **Interactive Charts**: JFreeChart-powered visualizations
- **Comprehensive Performance Analysis**: Detailed metrics and comparisons
- **Report Generation**: Automatic generation of HTML, CSV, and text reports
- **Simulation History**: Track and compare multiple simulation runs
- **Import/Export**: Save and load configurations in JSON/CSV formats
- **Keyboard Shortcuts**: Quick navigation with Ctrl+1-9, F5, etc.

## Module Architecture

### Module 1: User Input and Configuration
- Accepts task and VM parameters from files or interactively
- Validates input data
- Stores configuration in structured format

### Module 2: Cloud Resource Modeling
- Models virtual machines with varying capabilities
- Integrates with CloudSim Plus for realistic simulation
- Manages VM states and attributes

### Module 3: Task Profiling
- Analyzes tasks and estimates execution requirements
- Creates execution time and cost matrices
- Determines QoS feasibility for task-VM pairs

### Module 4: QoS-Aware Scheduling Engine
- Implements multi-objective scheduling algorithm
- Considers deadline and budget constraints
- Uses weighted objective function: `QoS Score = α × Time + β × Cost`

### Module 5: Baseline Scheduling
- Implements FCFS, Random, and Min-Min schedulers
- Provides comparison baseline for QoS-aware approach

### Module 6: Performance Evaluation
- Calculates comprehensive performance metrics
- Compares scheduling algorithms
- Generates improvement analysis

### Module 7: Result Visualization
- Creates bar charts for metric comparisons
- Generates ASCII charts for console output
- Supports multiple output formats

### Module 8: Report Generation
- Generates detailed simulation reports
- Creates HTML and CSV outputs
- Documents all scheduling decisions

## Prerequisites

- **Java 21** or higher (required for JavaFX 21)
- **Maven 3.6** or higher

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd cloudsim
```

2. Build the project:
```bash
mvn clean package
```

## 🚀 Running the Application

### GUI Mode (Recommended)

Launch the modern JavaFX graphical interface:

```bash
mvn javafx:run
```

Or after building:
```bash
mvn exec:java -Dexec.mainClass="com.cloudsim.qos.ui.MainApplication"
```

### CLI Mode (Legacy)

#### Demo Mode (Default)
```bash
java -jar target/qos-task-scheduler-1.0-SNAPSHOT.jar
# or
java -jar target/qos-task-scheduler-1.0-SNAPSHOT.jar --demo
```

#### Interactive Mode
```bash
java -jar target/qos-task-scheduler-1.0-SNAPSHOT.jar --interactive
```

#### File-Based Configuration
```bash
java -jar target/qos-task-scheduler-1.0-SNAPSHOT.jar --file config
```

### Scalability Test
```bash
java -jar target/qos-task-scheduler-1.0-SNAPSHOT.jar --scalability
```

## Configuration Files

### tasks.json
```json
[
  {
    "taskId": 1,
    "taskLength": 5000,
    "deadline": 8.0,
    "budget": 1.5,
    "priority": 8
  }
]
```

### vms.json
```json
[
  {
    "vmId": 1,
    "mips": 1000,
    "costPerSecond": 0.05,
    "networkLatency": 0.1,
    "numberOfPes": 2
  }
]
```

### config.json
```json
{
  "alpha": 0.5,
  "beta": 0.5,
  "enableVisualization": true,
  "outputDirectory": "output"
}
```

## Output

The simulation generates the following outputs:

- **Charts**: PNG images comparing scheduler performance
- **Reports**: 
  - Text report with detailed analysis
  - HTML report for presentation
  - CSV file for data analysis
- **Configuration**: Saved configuration for reproducibility

## Performance Metrics

- Average Execution Time
- Total Execution Cost
- Makespan
- Deadline Miss Rate
- Budget Violation Rate
- QoS Satisfaction Rate
- Throughput

## Project Structure

```
cloudsim/
├── pom.xml
├── README.md
├── config/
│   ├── tasks.json
│   ├── vms.json
│   └── config.json
├── src/main/java/com/cloudsim/qos/
│   ├── Main.java                    # CLI Entry Point
│   ├── config/
│   │   ├── InputConfigurationModule.java
│   │   └── SimulationConfig.java
│   ├── model/
│   │   ├── CloudTask.java
│   │   ├── VirtualMachine.java
│   │   └── SchedulingResult.java
│   ├── resource/
│   │   └── CloudResourceModelingModule.java
│   ├── profiling/
│   │   └── TaskProfilingModule.java
│   ├── scheduler/
│   │   ├── TaskScheduler.java
│   │   ├── QoSAwareScheduler.java
│   │   ├── FCFSScheduler.java
│   │   ├── RandomScheduler.java
│   │   └── MinMinScheduler.java
│   ├── evaluation/
│   │   └── PerformanceEvaluationModule.java
│   ├── visualization/
│   │   ├── ResultVisualizationModule.java
│   │   └── ReportGenerationModule.java
│   └── ui/                          # JavaFX GUI
│       ├── MainApplication.java     # GUI Entry Point
│       ├── SimulatorController.java # Main Controller
│       ├── components/              # Reusable UI Components
│       │   └── StatusBar.java
│       ├── panels/                  # UI Panels
│       │   ├── WelcomePanel.java
│       │   ├── ConfigurationPanel.java
│       │   ├── TaskManagementPanel.java
│       │   ├── VMManagementPanel.java
│       │   ├── SimulationPanel.java
│       │   ├── ResultsPanel.java
│       │   ├── ChartsPanel.java
│       │   ├── ReportsPanel.java
│       │   ├── ComparisonPanel.java
│       │   ├── HistoryPanel.java
│       │   └── SettingsPanel.java
│       └── utils/                   # Utility Classes
│           ├── ConfigurationManager.java
│           ├── KeyboardShortcuts.java
│           └── TooltipHelper.java
├── src/main/resources/
│   └── css/
│       ├── main.css                 # Dark Theme
│       └── light-theme.css          # Light Theme
└── output/
    ├── charts/
    └── reports/
```

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+1` | Welcome Panel |
| `Ctrl+2` | Configuration Panel |
| `Ctrl+3` | Tasks Panel |
| `Ctrl+4` | VMs Panel |
| `Ctrl+5` | Simulation Panel |
| `Ctrl+6` | Results Panel |
| `Ctrl+7` | Charts Panel |
| `Ctrl+8` | Reports Panel |
| `Ctrl+9` | Comparison Panel |
| `Ctrl+N` | New Configuration |
| `Ctrl+O` | Open Configuration |
| `Ctrl+S` | Save Configuration |
| `F1` | Help |
| `F5` | Run Simulation |

## Algorithm Details

### QoS-Aware Scheduling Algorithm

The proposed QoS-aware scheduler uses a multi-objective optimization approach:

1. **Task Profiling**: Calculate execution time and cost for all task-VM combinations
2. **QoS Score Calculation**: 
   ```
   QoS Score = α × (normalized_time) + β × (normalized_cost) + penalties
   ```
3. **Constraint Checking**: Verify deadline and budget feasibility
4. **Task Prioritization**: Sort tasks by priority and deadline
5. **VM Selection**: Choose VM with minimum QoS score considering load balancing

### Objective Function

```
Minimize: QoS_Score = α × T_exec + β × C_exec

Subject to:
- T_total ≤ Deadline
- C_exec ≤ Budget

Where:
- α + β = 1 (weighting factors)
- T_exec = Task_Length / VM_MIPS
- T_total = T_exec + Network_Latency
- C_exec = T_exec × Cost_per_Second
```

## Dependencies

- **CloudSim Plus 7.3.2** - Cloud simulation framework
- **JavaFX 21.0.1** - Modern GUI framework
- **Gson 2.10.1** - JSON parsing
- **JFreeChart 1.5.4** - Chart visualization
- **Apache Commons Math 3.6.1** - Statistics
- **Logback 1.4.11** - Logging

## 🎨 Themes

The application supports two themes:
- **Dark Theme** (default) - Easy on the eyes for extended use
- **Light Theme** - Classic light appearance

Switch themes via Settings panel or they persist across sessions.

## 📊 Understanding the Results

### QoS Score
The QoS score is calculated as:
```
QoS Score = α × (normalized_time) + β × (normalized_cost)
```
Lower scores indicate better performance.

### Why More VMs Can Reduce Cost
With more VMs, the scheduler has more choices to:
- Select VMs with lower cost per second
- Find VMs with higher MIPS (faster = less time = less cost)
- Run tasks in parallel, reducing waiting time

## License

This project is developed for academic purposes.

## Authors

QoS Task Scheduling Team

## References

1. CloudSim Plus: https://cloudsimplus.org/
2. R. Buyya, R. Ranjan, and R. N. Calheiros, "Modeling and Simulation of Scalable Cloud Computing Environments"
3. Task Scheduling in Cloud Computing: A Survey
