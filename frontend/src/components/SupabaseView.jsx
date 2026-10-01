import React from 'react';
import { Database, CheckCircle2, AlertCircle, RefreshCw, Server, Layers, ShieldAlert, Table } from 'lucide-react';
import LogTable from './LogTable';

export default function SupabaseView({ summary, logs, alerts, handleSyncDatabase, syncing, loadingLogs }) {
  return (
    <div className="supabase-view-container" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Supabase Status Header Card */}
      <div className="metrics-row">
        <div className="metric-card card-total">
          <div className="metric-info">
            <span className="label">Database Connection</span>
            <div className="val" style={{ fontSize: '1.2rem', marginTop: '4px' }}>
              {summary.dbConnected ? 'Supabase PostgreSQL' : 'Local Dev Mode'}
            </div>
            <span style={{ fontSize: '0.78rem', color: summary.dbConnected ? '#10b981' : '#f59e0b', marginTop: '4px' }}>
              ● {summary.dbStatus}
            </span>
          </div>
          <div className="metric-icon-box icon-blue">
            <Database size={24} />
          </div>
        </div>

        <div className="metric-card card-info">
          <div className="metric-info">
            <span className="label">Total Logs Synced</span>
            <div className="val">{summary.totalLogs}</div>
            <span style={{ fontSize: '0.78rem', color: '#94a3b8', marginTop: '4px' }}>Table: public.logs</span>
          </div>
          <div className="metric-icon-box icon-blue">
            <Layers size={24} />
          </div>
        </div>

        <div className="metric-card card-warning">
          <div className="metric-info">
            <span className="label">Active Alerts Synced</span>
            <div className="val">{alerts.length}</div>
            <span style={{ fontSize: '0.78rem', color: '#94a3b8', marginTop: '4px' }}>Table: public.alerts</span>
          </div>
          <div className="metric-icon-box icon-amber">
            <ShieldAlert size={24} />
          </div>
        </div>
      </div>

      {/* Database Schema & Synchronization Action Card */}
      <div className="alerts-feed-panel">
        <div className="panel-header">
          <div className="panel-title-wrapper">
            <Table size={20} className="icon-alert" />
            <div>
              <h2>Supabase Database Schema & Sync Status</h2>
              <p className="panel-subtitle">PostgreSQL database tables and live synchronization engine</p>
            </div>
          </div>
          <button className="btn btn-secondary" onClick={handleSyncDatabase} disabled={syncing}>
            <RefreshCw size={14} className={syncing ? 'spin' : ''} />
            <span>{syncing ? 'Synchronizing...' : 'Sync Supabase Now'}</span>
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '14px', marginTop: '16px' }}>
          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '0.82rem', fontWeight: 600, color: '#60a5fa' }}>📋 public.logs</div>
            <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '4px' }}>Stores parsed log entries with raw timestamps, levels, messages, and line numbers.</div>
          </div>
          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '0.82rem', fontWeight: 600, color: '#f59e0b' }}>🚨 public.alerts</div>
            <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '4px' }}>Stores triggered security & operational anomaly detection rules.</div>
          </div>
          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '0.82rem', fontWeight: 600, color: '#10b981' }}>📑 public.summary_reports</div>
            <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '4px' }}>Stores generated system health and summary analysis reports.</div>
          </div>
          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '14px' }}>
            <div style={{ fontSize: '0.82rem', fontWeight: 600, color: '#a855f7' }}>🔐 public.users</div>
            <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '4px' }}>Stores BCrypt hashed password credentials ($2a$12$...) and user profiles.</div>
          </div>
        </div>
      </div>

      {/* Synced Database Log Records Stream */}
      <div>
        <div style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff', marginBottom: '10px' }}>
          Database Log Records Stream (public.logs)
        </div>
        <LogTable logs={logs} loading={loadingLogs} />
      </div>
    </div>
  );
}
