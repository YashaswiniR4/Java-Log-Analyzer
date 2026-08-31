import React from 'react';
import { ShieldAlert, AlertTriangle, BellRing, CheckCircle2 } from 'lucide-react';

export default function AlertsFeed({ alerts }) {
  return (
    <div class="alerts-feed-panel">
      <div class="panel-header">
        <div class="panel-title-wrapper">
          <ShieldAlert size={20} class="icon-alert" />
          <div>
            <h2>Active Security & System Alerts</h2>
            <p class="panel-subtitle">Rule-based anomaly detection triggered alerts</p>
          </div>
        </div>
        <span class={`badge ${alerts.length > 0 ? 'badge-error' : 'badge-success'}`}>
          {alerts.length} {alerts.length === 1 ? 'Alert' : 'Alerts'} Active
        </span>
      </div>

      <div class="alerts-grid-wrapper">
        {alerts.length === 0 ? (
          <div class="alerts-empty-state">
            <CheckCircle2 size={32} class="icon-success" />
            <div>
              <h3>All Systems Normal</h3>
              <p>No operational anomalies or brute-force security threats detected.</p>
            </div>
          </div>
        ) : (
          alerts.map((alert, idx) => {
            const isSecurity = alert.ruleType.includes('SECURITY');
            return (
              <div key={idx} class={`alert-feed-card ${isSecurity ? 'security' : 'system'}`}>
                <div class="alert-feed-header">
                  <span class={`alert-rule-tag ${isSecurity ? 'tag-security' : 'tag-system'}`}>
                    {isSecurity ? <ShieldAlert size={13} /> : <AlertTriangle size={13} />}
                    {alert.ruleType}
                  </span>
                  <span class="alert-feed-time">{alert.timestamp}</span>
                </div>
                <div class="alert-feed-message">{alert.message}</div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
