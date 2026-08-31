# Supabase Database Integration Guide

This guide explains how to connect your **Supabase PostgreSQL Database** to the **Java Log Analyzer & Monitoring System**.

---

## 🛠️ Step 1: Execute SQL Schema in Supabase

1. Log in to your [Supabase Dashboard](https://supabase.com/dashboard).
2. Select your project.
3. Open the **SQL Editor** from the left navigation sidebar.
4. Open the [`schema.sql`](file:///c:/Users/yasha/OneDrive/Desktop/TechVedhu/Java-Log-Analyzer-and-Monitoring-System/database/schema.sql) file in this folder, copy its contents, and paste them into the Supabase SQL Editor.
5. Click **Run**. This will automatically create three tables:
   - `public.logs`
   - `public.alerts`
   - `public.summary_reports`

---

## 🔑 Step 2: Obtain your Supabase Credentials

1. In Supabase Dashboard, go to **Project Settings** -> **API**.
2. Copy your **Project URL** (e.g. `https://xyzcompany.supabase.co`).
3. Copy your **anon / public key** or **service_role key** (e.g. `eyJhbGciOi...`).

---

## ⚙️ Step 3: Configure `database_config.properties`

Open [`database/database_config.properties`](file:///c:/Users/yasha/OneDrive/Desktop/TechVedhu/Java-Log-Analyzer-and-Monitoring-System/database/database_config.properties) and update the values:

```properties
supabase.enabled=true
supabase.url=https://YOUR_PROJECT_REF.supabase.co
supabase.key=YOUR_SUPABASE_ANON_KEY
```

Alternatively, you can add `SUPABASE_URL` and `SUPABASE_KEY` directly to a `.env` file in your home directory or workspace root.

---

## 🚀 Step 4: Run Application & Test Database Sync

Compile and launch the Java application:

```bash
cd backend
javac -d out src/*.java
java -cp out Main
```

From the Main Menu:
- Select **Option 8** to launch the Web Dashboard Server.
- Select **Option 9** (or Database Sync menu option) to push parsed logs and detected alerts directly into your Supabase database tables!
