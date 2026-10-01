import React from 'react';
import { ShieldAlert, AlertTriangle, AlertCircle, CheckCircle2, Server } from 'lucide-react';
import AlertsFeed from './AlertsFeed';
import LogTable from './LogTable';

export default function SecurityAlertsView({ alerts, logs, loadingLogs }) {
  const securityAlerts = alerts.filter(a => a.ruleType && a.ruleType.includes('SECURITY'));
  const errorLogs = logs.filter(l => l.level === 'ERROR' || l.level === 'WARNING');

  return (
    <div className="security-alerts-view" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Alert Metrics Cards */}
      <div className="metrics-row">
        <div className="metric-card card-error">
          <div className="metric-info">
            <span className="label">Total Active Alerts</span>
            <div className="val">{alerts.length}</div>
            <span style={{ fontSize: '0.78rem', color: '#fca5a5', marginTop: '4px' }}>
              {alerts.length > 0 ? 'Action Required' : 'All Systems Clear'}
            </span>
          </div>
          <div className="metric-icon-box icon-red">
            <ShieldAlert size={24} />
          </div>
        </div>

        <div className="metric-card card-warning">
          <div className="metric-info">
            <span className="label">Brute-Force & Security Alerts</span>
            <div className="val">{securityAlerts.length}</div>
            <span style={{ fontSize: '0.78rem', color: '#fcd34d', marginTop: '4px' }}>Failed Logins & Unauthorized Access</span>
          </div>
          <div className="metric-icon-box icon-amber">
            <AlertTriangle size={24} />
          </div>
        </div>

        <div className="metric-card card-info">
          <div className="metric-info">
            <span className="label">Error & Warning Logs</span>
            <div className="val">{errorLogs.length}</div>
            <span style={{ fontSize: '0.78rem', color: '#93c5fd', marginTop: '4px' }}>Flagged log events</span>
          </div>
          <div className="metric-icon-box icon-blue">
            <Server size={24} />
          </div>
        </div>
      </div>

      {/* Dedicated Alerts Feed Panel */}
      <AlertsFeed alerts={alerts} />

      {/* Flagged Log Events Table */}
      <div>
        <div style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff', marginBottom: '10px' }}>
          Flagged Operational Logs (ERROR & WARNING)
        </div>
        <LogTable logs={errorLogs} loading={loadingLogs} />
      </div>
    </div>
  );
}
