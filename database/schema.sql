-- ================================================================================
--               SUPABASE / POSTGRESQL DATABASE SCHEMA SCRIPT
-- ================================================================================
-- Paste this script directly into your Supabase SQL Editor to create all required tables.

-- 1. Create logs table
CREATE TABLE IF NOT EXISTS public.logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    level VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    raw_line_number INT DEFAULT -1,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast searching by log level and timestamp
CREATE INDEX IF NOT EXISTS idx_logs_level ON public.logs(level);
CREATE INDEX IF NOT EXISTS idx_logs_timestamp ON public.logs(timestamp);

-- 2. Create alerts table
CREATE TABLE IF NOT EXISTS public.alerts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rule_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for searching alerts by rule type
CREATE INDEX IF NOT EXISTS idx_alerts_rule_type ON public.alerts(rule_type);

-- 3. Create summary_reports table
CREATE TABLE IF NOT EXISTS public.summary_reports (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    generated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    total_logs INT NOT NULL,
    info_count INT NOT NULL,
    warning_count INT NOT NULL,
    error_count INT NOT NULL,
    skipped_lines INT NOT NULL,
    report_text TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create users table for Company-Level Authentication Flow
CREATE TABLE IF NOT EXISTS public.users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    otp_code VARCHAR(10),
    otp_expires_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON public.users(username);

-- 5. Create password_reset_tokens table
CREATE TABLE IF NOT EXISTS public.password_reset_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Enable Row Level Security (RLS) policies for Supabase public access (optional)
ALTER TABLE public.logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.summary_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.password_reset_tokens ENABLE ROW LEVEL SECURITY;

-- Allow read/write access via anon key for testing
CREATE POLICY "Allow public read access on logs" ON public.logs FOR SELECT USING (true);
CREATE POLICY "Allow public insert access on logs" ON public.logs FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow public read access on alerts" ON public.alerts FOR SELECT USING (true);
CREATE POLICY "Allow public insert access on alerts" ON public.alerts FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow public read access on summary_reports" ON public.summary_reports FOR SELECT USING (true);
CREATE POLICY "Allow public insert access on summary_reports" ON public.summary_reports FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow public read access on users" ON public.users FOR SELECT USING (true);
CREATE POLICY "Allow public insert access on users" ON public.users FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update access on users" ON public.users FOR UPDATE USING (true);

CREATE POLICY "Allow public read access on reset_tokens" ON public.password_reset_tokens FOR SELECT USING (true);
CREATE POLICY "Allow public insert access on reset_tokens" ON public.password_reset_tokens FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update access on reset_tokens" ON public.password_reset_tokens FOR UPDATE USING (true);
