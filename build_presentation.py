import os
import sys
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN
from pptx.enum.shapes import MSO_SHAPE

def create_presentation():
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)

    # Color Palette
    PRIMARY_COLOR = RGBColor(15, 23, 42)    # Slate 900
    ACCENT_COLOR = RGBColor(37, 99, 235)    # Royal Blue 600
    TEXT_MAIN = RGBColor(30, 41, 59)        # Slate 800
    TEXT_MUTED = RGBColor(71, 85, 105)     # Slate 600
    BG_LIGHT = RGBColor(248, 250, 252)     # Slate 50
    CARD_BG = RGBColor(255, 255, 255)      # White
    BORDER_COLOR = RGBColor(226, 232, 240) # Slate 200

    blank_layout = prs.slide_layouts[6]

    def add_header(slide, title_text, category_text="JAVA LOG ANALYZER & SUPABASE MONITORING SYSTEM"):
        # Header Box
        header_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.4), Inches(11.733), Inches(0.9))
        tf = header_box.text_frame
        tf.word_wrap = True
        tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0
        
        # Category
        p0 = tf.paragraphs[0]
        p0.text = category_text.upper()
        p0.font.size = Pt(10)
        p0.font.bold = True
        p0.font.color.rgb = ACCENT_COLOR
        
        # Title
        p1 = tf.add_paragraph()
        p1.text = title_text
        p1.font.size = Pt(22)
        p1.font.bold = True
        p1.font.color.rgb = PRIMARY_COLOR
        p1.space_before = Pt(4)

    def add_card(slide, left, top, width, height, bg_color=CARD_BG, border_color=BORDER_COLOR):
        shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        shape.fill.solid()
        shape.fill.fore_color.rgb = bg_color
        shape.line.color.rgb = border_color
        shape.line.width = Pt(1)
        return shape

    # ==========================================
    # SLIDE 1: TITLE SLIDE
    # ==========================================
    s1 = prs.slides.add_slide(blank_layout)
    bg1 = add_card(s1, Inches(0), Inches(0), Inches(13.333), Inches(7.5), bg_color=PRIMARY_COLOR, border_color=PRIMARY_COLOR)
    
    # Title Text Frame
    tf1 = s1.shapes.add_textbox(Inches(1.0), Inches(1.8), Inches(11.333), Inches(4.5)).text_frame
    tf1.word_wrap = True
    
    p = tf1.paragraphs[0]
    p.text = "⚡ JAVA LOG ANALYZER & SUPABASE MONITORING SYSTEM"
    p.font.size = Pt(28)
    p.font.bold = True
    p.font.color.rgb = RGBColor(96, 165, 250)
    
    p = tf1.add_paragraph()
    p.text = "Enterprise Log Parsing, Rule-Based Anomaly Detection, Real Gmail SMTP OTP Auth & Supabase Cloud Persistence"
    p.font.size = Pt(16)
    p.font.color.rgb = RGBColor(203, 213, 225)
    p.space_before = Pt(12)
    
    p = tf1.add_paragraph()
    p.text = "\nAcademic Internship Major Project Presentation"
    p.font.size = Pt(14)
    p.font.bold = True
    p.font.color.rgb = RGBColor(255, 255, 255)
    p.space_before = Pt(24)

    p = tf1.add_paragraph()
    p.text = "Presented by: Yashaswini R  |  Domain: Java Software Engineering  |  Tech Stack: Core Java 21, React 18, Supabase PostgreSQL"
    p.font.size = Pt(12)
    p.font.color.rgb = RGBColor(148, 163, 184)
    p.space_before = Pt(8)

    # ==========================================
    # SLIDE 2: INTRODUCTION & PROJECT OVERVIEW
    # ==========================================
    s2 = prs.slides.add_slide(blank_layout)
    add_header(s2, "1. Project Overview & System Introduction")
    
    c1 = add_card(s2, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.3))
    tf = s2.shapes.add_textbox(Inches(1.1), Inches(1.8), Inches(5.0), Inches(4.7)).text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "📌 Background & Core Purpose"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = PRIMARY_COLOR
    
    points1 = [
        "Modern enterprise applications generate thousands of raw log records daily.",
        "Manual inspection of log files is tedious, error-prone, and inefficient.",
        "Automated log analysis is critical for maintaining high system uptime and security.",
        "This project provides a full-stack observability solution combining Core Java 21 and React."
    ]
    for pt in points1:
        p = tf.add_paragraph()
        p.text = "• " + pt
        p.font.size = Pt(13)
        p.font.color.rgb = TEXT_MAIN
        p.space_before = Pt(10)

    c2 = add_card(s2, Inches(6.8), Inches(1.5), Inches(5.733), Inches(5.3))
    tf2 = s2.shapes.add_textbox(Inches(7.1), Inches(1.8), Inches(5.1), Inches(4.7)).text_frame
    tf2.word_wrap = True
    p = tf2.paragraphs[0]
    p.text = "🚀 Key Highlights & Deliverables"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = ACCENT_COLOR

    points2 = [
        "Regex Log Parser: Parses unstructured Apache, Nginx, and Syslog log streams.",
        "Security Alert Engine: Detects brute-force logins and repeated connection failures.",
        "Real Gmail SMTP OTP: Pure Java SSL Socket client delivering 6-digit verification codes.",
        "Cloud Database Persistence: Connects to Supabase PostgreSQL via JDBC SSL.",
        "Interactive Dashboard: Modern React + Vite web dashboard with real-time metric cards."
    ]
    for pt in points2:
        p = tf2.add_paragraph()
        p.text = "✔ " + pt
        p.font.size = Pt(13)
        p.font.color.rgb = TEXT_MAIN
        p.space_before = Pt(10)

    # ==========================================
    # SLIDE 3: PROBLEM STATEMENT
    # ==========================================
    s3 = prs.slides.add_slide(blank_layout)
    add_header(s3, "2. Problem Statement & Key Challenges")

    probs = [
        ("Manual Inspection Bottleneck", "DevOps engineers struggle to manually read plain-text log files containing tens of thousands of lines."),
        ("Undetected Cyber Attacks", "Brute-force password guessing attempts go unnoticed in standard logs without real-time threshold alert rules."),
        ("Silent Infrastructure Failures", "Database connection drops and 5xx internal server errors accumulate unnoticed until complete service downtime occurs."),
        ("Lack of Central Analytics", "Unstructured text log files do not provide queryable database metrics, trend charts, or automated summary reports.")
    ]

    coords = [(0.8, 1.6), (6.8, 1.6), (0.8, 4.3), (6.8, 4.3)]
    for idx, (title, desc) in enumerate(probs):
        x, y = coords[idx]
        add_card(s3, Inches(x), Inches(y), Inches(5.733), Inches(2.4))
        tf = s3.shapes.add_textbox(Inches(x + 0.2), Inches(y + 0.2), Inches(5.3), Inches(2.0)).text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = f"⚠️ {title}"
        p.font.size = Pt(15)
        p.font.bold = True
        p.font.color.rgb = RGBColor(225, 29, 72)
        
        p = tf.add_paragraph()
        p.text = desc
        p.font.size = Pt(12)
        p.font.color.rgb = TEXT_MUTED
        p.space_before = Pt(8)

    # ==========================================
    # SLIDE 4: SYSTEM ARCHITECTURE & TECH STACK
    # ==========================================
    s4 = prs.slides.add_slide(blank_layout)
    add_header(s4, "3. System Architecture & Tech Stack")

    stacks = [
        ("Core Java Backend", "Java 21 LTS", "HttpServer, Regex Engine, Thread Pools, pure SSL Socket client."),
        ("Frontend Web App", "React 18 + Vite", "Vanilla CSS glassmorphism UI, Lucide icons, responsive dashboard."),
        ("Cloud Database Tier", "Supabase PostgreSQL", "JDBC SSL connection (postgresql-42.7.3.jar), public.users & logs schema."),
        ("Email Security Service", "Gmail SMTP SSL", "smtp.gmail.com:465 pure SSL socket, BCrypt $2a$12$ password hashing.")
    ]

    for idx, (layer, tech, detail) in enumerate(stacks):
        x = 0.8 + idx * 2.98
        add_card(s4, Inches(x), Inches(1.6), Inches(2.8), Inches(5.2))
        tf = s4.shapes.add_textbox(Inches(x + 0.15), Inches(1.8), Inches(2.5), Inches(4.8)).text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = layer
        p.font.size = Pt(14)
        p.font.bold = True
        p.font.color.rgb = PRIMARY_COLOR
        
        p = tf.add_paragraph()
        p.text = tech
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = ACCENT_COLOR
        p.space_before = Pt(6)
        
        p = tf.add_paragraph()
        p.text = detail
        p.font.size = Pt(11)
        p.font.color.rgb = TEXT_MUTED
        p.space_before = Pt(10)

    # ==========================================
    # SLIDE 5: LOG PARSING ENGINE
    # ==========================================
    s5 = prs.slides.add_slide(blank_layout)
    add_header(s5, "4. Log Parsing & Regular Expression Engine")

    add_card(s5, Inches(0.8), Inches(1.5), Inches(11.733), Inches(5.3))
    tf = s5.shapes.add_textbox(Inches(1.1), Inches(1.8), Inches(11.1), Inches(4.7)).text_frame
    tf.word_wrap = True

    p = tf.paragraphs[0]
    p.text = "🔍 Regex Extraction & LogEntry Structuring"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = PRIMARY_COLOR

    p_items = [
        ("Multi-Format Parsing Pattern", "^(?<timestamp>\\\\d{4}-\\\\d{2}-\\\\d{2}\\\\s+\\\\d{2}:\\\\d{2}:\\\\d{2})\\\\s+\\\\[(?<level>INFO|WARN|WARNING|ERROR|FATAL)\\\\]\\\\s+(?<message>.*)$"),
        ("Structured Object Creation", "Extracts raw timestamp, maps severity to LogLevel enum, captures message body, and tracks raw line numbers."),
        ("Resilient Error Handling", "Malformed or unparseable lines are safely counted as skipped lines without halting execution."),
        ("High Performance Stream Reading", "Processes large files using BufferedReader buffer blocks (8192 bytes) for memory efficiency.")
    ]

    for title, val in p_items:
        p = tf.add_paragraph()
        p.text = "▪ " + title + ": "
        p.font.bold = True
        p.font.size = Pt(13)
        p.font.color.rgb = ACCENT_COLOR
        p.space_before = Pt(12)
        
        p2 = tf.add_paragraph()
        p2.text = val
        p2.font.size = Pt(12)
        p2.font.color.rgb = TEXT_MAIN
        p2.space_before = Pt(2)

    # ==========================================
    # SLIDE 6: RULE-BASED ANOMALY DETECTION
    # ==========================================
    s6 = prs.slides.add_slide(blank_layout)
    add_header(s6, "5. Rule-Based Anomaly & Security Alert Engine")

    alerts = [
        ("🔐 Brute-Force Login Detection", "Monitors failed authentication events across a rolling 5-minute window. Triggers SECURITY ALERT if failed login count exceeds 2."),
        ("⚡ Repeated Error Spike Detection", "Tracks identical error messages within 10-minute intervals. Triggers ALERT when repeated error count exceeds 3 occurrences."),
        ("📊 System Warning Activity", "Aggregates warning logs across execution runs to alert DevOps teams before system performance degrades."),
        ("💾 Database Alert Persistence", "All generated Alert objects are saved to public.alerts table in Supabase PostgreSQL for historical audit tracking.")
    ]

    for idx, (title, desc) in enumerate(alerts):
        x = 0.8 + (idx % 2) * 6.0
        y = 1.6 + (idx // 2) * 2.7
        add_card(s6, Inches(x), Inches(y), Inches(5.733), Inches(2.4))
        tf = s6.shapes.add_textbox(Inches(x + 0.2), Inches(y + 0.2), Inches(5.3), Inches(2.0)).text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = title
        p.font.size = Pt(15)
        p.font.bold = True
        p.font.color.rgb = PRIMARY_COLOR
        
        p = tf.add_paragraph()
        p.text = desc
        p.font.size = Pt(12)
        p.font.color.rgb = TEXT_MUTED
        p.space_before = Pt(8)

    # ==========================================
    # SLIDE 7: REAL GMAIL SMTP OTP AUTHENTICATION
    # ==========================================
    s7 = prs.slides.add_slide(blank_layout)
    add_header(s7, "6. Real Gmail SMTP SSL OTP Delivery System")

    add_card(s7, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.3))
    tf1 = s7.shapes.add_textbox(Inches(1.1), Inches(1.8), Inches(5.0), Inches(4.7)).text_frame
    tf1.word_wrap = True
    p = tf1.paragraphs[0]
    p.text = "✉️ Pure Java SSL Socket Client"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = PRIMARY_COLOR

    pts1 = [
        "Built using standard SSLSocketFactory connecting directly to smtp.gmail.com:465.",
        "Zero external Maven dependencies — light, enterprise-ready Java code.",
        "Supports AUTH LOGIN protocol with Base64 encoded credentials.",
        "Robust multi-line EHLO response parser handling 250- headers."
    ]
    for pt in pts1:
        p = tf1.add_paragraph()
        p.text = "• " + pt
        p.font.size = Pt(13)
        p.font.color.rgb = TEXT_MAIN
        p.space_before = Pt(10)

    c2 = add_card(s7, Inches(6.8), Inches(1.5), Inches(5.733), Inches(5.3))
    tf2 = s7.shapes.add_textbox(Inches(7.1), Inches(1.8), Inches(5.1), Inches(4.7)).text_frame
    tf2.word_wrap = True
    p = tf2.paragraphs[0]
    p.text = "🔒 Security & Privacy Workflow"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = ACCENT_COLOR

    pts2 = [
        "Sends real 6-digit OTP verification codes directly to user Gmail inbox.",
        "OTP is NEVER displayed on the browser screen or pre-filled in inputs.",
        "OTP is stripped from HTTP API JSON responses to prevent network sniffing.",
        "Passwords are never stored in plain text — hashed via BCrypt $2a$12$."
    ]
    for pt in pts2:
        p = tf2.add_paragraph()
        p.text = "✔ " + pt
        p.font.size = Pt(13)
        p.font.color.rgb = TEXT_MAIN
        p.space_before = Pt(10)

    # ==========================================
    # SLIDE 8: SUPABASE POSTGRESQL DATABASE PERSISTENCE
    # ==========================================
    s8 = prs.slides.add_slide(blank_layout)
    add_header(s8, "7. Supabase PostgreSQL Cloud Integration")

    db_items = [
        ("Standard PostgreSQL JDBC Driver", "Uses postgresql-42.7.3.jar connecting via SSL JDBC connection string SUPABASE_DB_URL."),
        ("Zero Hardcoded Secrets", "All database host, port, user, and password credentials managed securely via DotEnvLoader (.env)."),
        ("Automatic Schema Initializer", "Creates public.users, public.logs, public.alerts, and public.password_reset_tokens tables automatically."),
        ("Batch Data Synchronization", "Executes PreparedStatement batch queries for fast, efficient synchronization of large log callsets.")
    ]

    for idx, (title, desc) in enumerate(db_items):
        x = 0.8 + (idx % 2) * 6.0
        y = 1.6 + (idx // 2) * 2.7
        add_card(s8, Inches(x), Inches(y), Inches(5.733), Inches(2.4))
        tf = s8.shapes.add_textbox(Inches(x + 0.2), Inches(y + 0.2), Inches(5.3), Inches(2.0)).text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = "🗄️ " + title
        p.font.size = Pt(15)
        p.font.bold = True
        p.font.color.rgb = ACCENT_COLOR
        
        p = tf.add_paragraph()
        p.text = desc
        p.font.size = Pt(12)
        p.font.color.rgb = TEXT_MUTED
        p.space_before = Pt(8)

    # ==========================================
    # SLIDE 9: REACT FRONTEND DASHBOARD (WITH IMAGE)
    # ==========================================
    s9 = prs.slides.add_slide(blank_layout)
    add_header(s9, "8. React Observability Dashboard UI")

    add_card(s9, Inches(0.8), Inches(1.5), Inches(5.2), Inches(5.3))
    tf = s9.shapes.add_textbox(Inches(1.0), Inches(1.8), Inches(4.8), Inches(4.7)).text_frame
    tf.word_wrap = True

    p = tf.paragraphs[0]
    p.text = "💻 Modern UI Components"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = PRIMARY_COLOR

    uip = [
        "Live Metric Cards: Displays total logs, INFO, WARNING, ERROR, and skipped line counts.",
        "Interactive Log Explorer: Instant keyword search & dropdown severity filtering.",
        "Sidebar Navigation: Switch between Dashboard, Logs, Security Alerts & Supabase DB views.",
        "Drag-and-Drop Upload: Process custom log files directly from the browser interface."
    ]
    for pt in uip:
        p = tf.add_paragraph()
        p.text = "• " + pt
        p.font.size = Pt(12)
        p.font.color.rgb = TEXT_MAIN
        p.space_before = Pt(10)

    # Add Image if exists
    img_path = os.path.join("screenshots", "media_1787838643798.png")
    if os.path.exists(img_path):
        s9.shapes.add_picture(img_path, Inches(6.3), Inches(1.5), Inches(6.2), Inches(5.3))

    # ==========================================
    # SLIDE 10: SECURITY ALERTS VIEW (WITH IMAGE)
    # ==========================================
    s10 = prs.slides.add_slide(blank_layout)
    add_header(s10, "9. Security Alerts & Anomaly Analytics View")

    add_card(s10, Inches(0.8), Inches(1.5), Inches(5.2), Inches(5.3))
    tf = s10.shapes.add_textbox(Inches(1.0), Inches(1.8), Inches(4.8), Inches(4.7)).text_frame
    tf.word_wrap = True

    p = tf.paragraphs[0]
    p.text = "🛡️ Real-Time Security Feed"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = RGBColor(225, 29, 72)

    secp = [
        "Real-Time Alert Feed: Highlights brute-force login attempts and repeated error spikes.",
        "Visual Status Badges: Distinct badges for SECURITY ALERT, ERROR, and WARNING.",
        "Timestamp Tracking: Precise event timestamping for incident response.",
        "Supabase Audit Sync: Persists security incidents for forensic auditing."
    ]
    for pt in secp:
        p = tf.add_paragraph()
        p.text = "• " + pt
        p.font.size = Pt(12)
        p.font.color.rgb = TEXT_MAIN
        p.space_before = Pt(10)

    img_path2 = os.path.join("screenshots", "media_1787838662180.png")
    if os.path.exists(img_path2):
        s10.shapes.add_picture(img_path2, Inches(6.3), Inches(1.5), Inches(6.2), Inches(5.3))

    # ==========================================
    # SLIDE 11: REPORT GENERATOR ENGINE
    # ==========================================
    s11 = prs.slides.add_slide(blank_layout)
    add_header(s11, "10. Automated Summary Report Generation")

    add_card(s11, Inches(0.8), Inches(1.5), Inches(11.733), Inches(5.3))
    tf = s11.shapes.add_textbox(Inches(1.1), Inches(1.8), Inches(11.1), Inches(4.7)).text_frame
    tf.word_wrap = True

    p = tf.paragraphs[0]
    p.text = "📄 Executive System Summary Reports"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = PRIMARY_COLOR

    rep_pts = [
        ("Automated Report Builder", "ReportGenerator converts parsed log entries and detected alerts into a structured executive text report."),
        ("Key Metric Summary", "Includes total processed records, percentage breakdown of INFO vs WARN vs ERROR, and skipped line statistics."),
        ("File & Database Export", "Saves generated report files locally to reports/ system directory and commits report text to Supabase PostgreSQL database."),
        ("One-Click UI Download", "React dashboard allows DevOps managers to preview and copy executive reports directly from the web interface.")
    ]
    for title, desc in rep_pts:
        p = tf.add_paragraph()
        p.text = "▪ " + title + ": "
        p.font.bold = True
        p.font.size = Pt(13)
        p.font.color.rgb = ACCENT_COLOR
        p.space_before = Pt(12)
        
        p2 = tf.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(12)
        p2.font.color.rgb = TEXT_MAIN
        p2.space_before = Pt(2)

    # ==========================================
    # SLIDE 12: TESTING & QUALITY ASSURANCE
    # ==========================================
    s12 = prs.slides.add_slide(blank_layout)
    add_header(s12, "11. Comprehensive Testing & Quality Assurance")

    tests = [
        ("51 JUnit Test Cases", "100% green test execution across 7 comprehensive test suites."),
        ("LogParser & LogAnalyzer", "Tests regex parsing accuracy, missing timestamp fallbacks, and level counts."),
        ("AlertDetector Rule Tests", "Validates brute-force login and error spike threshold triggers."),
        ("AuthService & Security", "Tests BCrypt hashing, duplicate email checks, and OTP verification logic."),
        ("Edge Case Testing", "Verifies empty files, malformed log lines, and corrupt input streams handling.")
    ]

    for idx, (title, desc) in enumerate(tests):
        y = 1.6 + idx * 1.05
        add_card(s12, Inches(0.8), Inches(y), Inches(11.733), Inches(0.9))
        tf = s12.shapes.add_textbox(Inches(1.0), Inches(y + 0.15), Inches(11.3), Inches(0.6)).text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = "✅ " + title + " — "
        p.font.bold = True
        p.font.size = Pt(14)
        p.font.color.rgb = RGBColor(16, 185, 129)
        
        p.text += desc
        p.font.bold = False
        p.font.color.rgb = TEXT_MAIN

    # ==========================================
    # SLIDE 13: PROJECT IMPACT & BENEFIT
    # ==========================================
    s13 = prs.slides.add_slide(blank_layout)
    add_header(s13, "12. Business Value & Importance of Log Analysis")

    vals = [
        ("90% Faster Incident Resolution", "Automated log parsing and error grouping reduces Mean Time To Detect (MTTD) system anomalies."),
        ("Enhanced Security Auditing", "Real-time security alerts identify unauthorized login attempts before data breaches occur."),
        ("Zero Downtime Operations", "Early detection of connection drops allows engineers to fix infrastructure bottlenecks proactively."),
        ("Regulatory Compliance", "Centralized Supabase database logging provides historical audit trails required for compliance standards.")
    ]

    for idx, (title, desc) in enumerate(vals):
        x = 0.8 + (idx % 2) * 6.0
        y = 1.6 + (idx // 2) * 2.7
        add_card(s13, Inches(x), Inches(y), Inches(5.733), Inches(2.4))
        tf = s13.shapes.add_textbox(Inches(x + 0.2), Inches(y + 0.2), Inches(5.3), Inches(2.0)).text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = "💡 " + title
        p.font.size = Pt(15)
        p.font.bold = True
        p.font.color.rgb = PRIMARY_COLOR
        
        p = tf.add_paragraph()
        p.text = desc
        p.font.size = Pt(12)
        p.font.color.rgb = TEXT_MUTED
        p.space_before = Pt(8)

    # ==========================================
    # SLIDE 14: FUTURE ENHANCEMENTS
    # ==========================================
    s14 = prs.slides.add_slide(blank_layout)
    add_header(s14, "13. Future Roadmap & Scaling Features")

    road = [
        ("Slack & Webhook Integrations", "Real-time push notifications to DevOps team Slack channels during Critical Alert events."),
        ("ELK Stack & Kafka Stream Integration", "Scale log ingestion using Apache Kafka message broker for high-throughput enterprise pipelines."),
        ("AI / ML Anomaly Prediction", "Integrate machine learning models (Isolation Forests) for zero-day pattern anomaly detection."),
        ("Mobile Observability App", "Build native Flutter mobile application for remote server monitoring on iOS and Android.")
    ]

    for idx, (title, desc) in enumerate(road):
        x = 0.8 + (idx % 2) * 6.0
        y = 1.6 + (idx // 2) * 2.7
        add_card(s14, Inches(x), Inches(y), Inches(5.733), Inches(2.4))
        tf = s14.shapes.add_textbox(Inches(x + 0.2), Inches(y + 0.2), Inches(5.3), Inches(2.0)).text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = "🔮 " + title
        p.font.size = Pt(15)
        p.font.bold = True
        p.font.color.rgb = ACCENT_COLOR
        
        p = tf.add_paragraph()
        p.text = desc
        p.font.size = Pt(12)
        p.font.color.rgb = TEXT_MUTED
        p.space_before = Pt(8)

    # ==========================================
    # SLIDE 15: CONCLUSION & Q&A
    # ==========================================
    s15 = prs.slides.add_slide(blank_layout)
    bg15 = add_card(s15, Inches(0), Inches(0), Inches(13.333), Inches(7.5), bg_color=PRIMARY_COLOR, border_color=PRIMARY_COLOR)
    
    tf15 = s15.shapes.add_textbox(Inches(1.0), Inches(2.0), Inches(11.333), Inches(4.0)).text_frame
    tf15.word_wrap = True
    
    p = tf15.paragraphs[0]
    p.text = "🎉 THANK YOU!"
    p.font.size = Pt(36)
    p.font.bold = True
    p.font.color.rgb = RGBColor(96, 165, 250)
    p.alignment = PP_ALIGN.CENTER
    
    p = tf15.add_paragraph()
    p.text = "Java Log Analyzer & Supabase Monitoring System"
    p.font.size = Pt(20)
    p.font.bold = True
    p.font.color.rgb = RGBColor(255, 255, 255)
    p.alignment = PP_ALIGN.CENTER
    p.space_before = Pt(12)
    
    p = tf15.add_paragraph()
    p.text = "Questions & Feedback Welcome 💬"
    p.font.size = Pt(16)
    p.font.color.rgb = RGBColor(203, 213, 225)
    p.alignment = PP_ALIGN.CENTER
    p.space_before = Pt(20)

    # Save Presentation
    output_path = "PROJECT_PRESENTATION.pptx"
    prs.save(output_path)
    print(f"[SUCCESS] PowerPoint Presentation created successfully at: {os.path.abspath(output_path)}")

if __name__ == "__main__":
    create_presentation()
