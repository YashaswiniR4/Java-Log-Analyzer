# 🌐 Free Cloud Deployment Guide

This guide outlines how to host your **Java Log Analyzer & Monitoring System** online for free so anyone can access your project via a public HTTPS URL.

---

## 🚀 Option 1: 1-Click Free Hosting on Render.com (Recommended)

[Render](https://render.com/) allows free hosting of Docker web services.

### Steps:
1. Log in to [Render.com](https://render.com/) with your GitHub account.
2. Click **New +** -> **Web Service**.
3. Connect your GitHub repository: `https://github.com/YashaswiniR4/Java-Log-Analyzer.git`.
4. Render will automatically detect the [`Dockerfile`](file:///c:/Users/yasha/OneDrive/Desktop/TechVedhu/Java-Log-Analyzer-and-Monitoring-System/Dockerfile) and [`render.yaml`](file:///c:/Users/yasha/OneDrive/Desktop/TechVedhu/Java-Log-Analyzer-and-Monitoring-System/render.yaml).
5. Click **Create Web Service**.
6. Render will build the container and provide your live public URL (e.g. `https://java-log-analyzer.onrender.com`).

---

## ⚡ Option 2: Deploy Frontend on Vercel

If you want to host the React UI on [Vercel](https://vercel.com/):

```bash
cd frontend
npx vercel --prod
```

Set the root directory to `frontend` and output folder to `dist`.

---

## 🗄️ Database & Email Configuration
Your cloud application automatically connects to:
- **Supabase Cloud Database**: Configured via `SUPABASE_DB_URL`
- **Gmail SMTP SSL Email**: Configured via `SMTP_USER=yashaswinir483@gmail.com`
