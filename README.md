# Java Log Analyzer & Monitoring System

A clean, modular, professional Core Java application designed to parse, analyze, filter, monitor, and generate operational security alerts from system and application log files. Features **Supabase PostgreSQL Database Integration**, a **Console CLI Interface**, an **Embedded Java REST API Server**, and a **Web Dashboard Frontend**.

---

## 📁 Project Structure

```text
Java-Log-Analyzer-and-Monitoring-System/
│
├── frontend/
│   ├── index.html            # Web Dashboard HTML5 UI
│   ├── style.css             # Glassmorphism Dark Mode Stylesheet
│   └── app.js                # Frontend REST API integration controller
│
├── backend/
│   └── src/
│       ├── Main.java         # Application Entry Point (CLI + Web + Database)
│       ├── LogEntry.java     # Parsed Log Record Model
│       ├── LogLevel.java     # Log Level Enum (INFO, WARNING, ERROR)
│       ├── LogParser.java    # Regex Log Line Parser
│       ├── LogAnalyzer.java  # Analytical Query Engine
│       ├── AlertDetector.java# Pattern Analysis Engine (4 Alert Rules)
│       ├── Alert.java        # Alert Entity Model
│       ├── DatabaseManager.java # Supabase / PostgreSQL Database Integration
│       ├── ReportGenerator.java # Summary Report Builder
│       ├── FileUtils.java    # Safe File I/O Utilities
│       └── LogWebServer.java # JDK HttpServer REST API & Static Asset Server
│
├── database/
│   ├── schema.sql            # SQL Table Creation Script (logs, alerts, summary_reports)
│   ├── database_config.properties # Supabase Database Connection Configuration File
│   └── README_DATABASE.md    # Detailed Database Setup & Integration Guide
│
├── logs/
│   └── application.log       # Sample Log File (~50 entries + test anomalies)
│
├── reports/
│   └── summary_report.txt    # Auto-Generated Summary Report
│
├── screenshots/
│   └── README_SCREENSHOTS.md # Screenshot visual layout guide
│
├── test/
│   └── sample_test_cases.txt # Documented 13-Test Scenario Matrix
│
├── README.md                 # Master Project Documentation
├── PROJECT_REPORT.md         # 25-Section Academic Internship Project Report
├── PRESENTATION_SLIDES.md     # 15-Slide PowerPoint Deck Content
└── .gitignore                 # Git Ignore Configuration
```

---

## ⚡ Supabase Database Setup

1. Log in to [Supabase](https://supabase.com/).
2. Open your project's **SQL Editor** and paste the content of [`database/schema.sql`](file:///c:/Users/yasha/OneDrive/Desktop/TechVedhu/Java-Log-Analyzer-and-Monitoring-System/database/schema.sql). Click **Run**.
3. Copy your **Supabase URL** and **Anon API Key** from Settings -> API.
4. Update [`database/database_config.properties`](file:///c:/Users/yasha/OneDrive/Desktop/TechVedhu/Java-Log-Analyzer-and-Monitoring-System/database/database_config.properties):
   ```properties
   supabase.enabled=true
   supabase.url=https://YOUR_PROJECT_REF.supabase.co
   supabase.key=YOUR_SUPABASE_ANON_KEY
   ```

---

## 🚀 How to Compile & Run

### Step 1: Open Terminal in `backend` Folder
```bash
cd Java-Log-Analyzer-and-Monitoring-System/backend
```

### Step 2: Compile Java Source Files
```bash
javac -d out src/*.java
```

### Step 3: Launch Application
```bash
java -cp out Main
```

### Step 4: Open Web Dashboard UI
Select **Option 8** in the CLI menu to launch the Web Dashboard server, then visit:
```text
http://localhost:8080/
```

---

## 🌐 REST API Endpoints

- `GET /api/summary`: Summary metrics JSON object.
- `GET /api/logs?level=ERROR&search=database`: Filtered log array.
- `GET /api/alerts`: Triggered security alerts JSON array.
- `GET /api/report`: Generated text report.
- `POST /api/db-sync`: Synchronize logs & alerts with Supabase PostgreSQL database.

---

## 👨‍💻 Author & License

Developed as an Individual Major Project for Java Software Engineering Internship Evaluation. Open source under MIT License.
