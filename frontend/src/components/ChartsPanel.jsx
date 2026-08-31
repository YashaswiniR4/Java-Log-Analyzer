import React from 'react';
import { BarChart3, PieChart, AlertTriangle } from 'lucide-react';

export default function ChartsPanel({ summary, logs }) {
  const total = summary.totalLogs || 1;
  const infoPct = Math.round((summary.infoCount / total) * 100);
  const warningPct = Math.round((summary.warningCount / total) * 100);
  const errorPct = Math.round((summary.errorCount / total) * 100);

  // Group top errors
  const errorCounts = {};
  logs.forEach(log => {
    if (log.level === 'ERROR') {
      const msg = log.message;
      errorCounts[msg] = (errorCounts[msg] || 0) + 1;
    }
  });

  const topErrors = Object.entries(errorCounts)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 3);

  // Sample volume buckets for histogram simulation
  const volumeBuckets = [
    { time: '09:00', info: 4, warning: 1, error: 0 },
    { time: '09:05', info: 3, warning: 4, error: 1 },
    { time: '09:10', info: 2, warning: 5, error: 4 },
    { time: '09:15', info: 3, warning: 3, error: 2 },
    { time: '09:20', info: 4, warning: 2, error: 1 },
    { time: '09:25', info: 2, warning: 1, error: 0 }
  ];

  return (
    <div class="charts-panel-grid">
      {/* 1. Log Volume Timeline Histogram */}
      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title">
            <BarChart3 size={16} class="icon-blue" />
            <span>Log Volume Timeline</span>
          </div>
          <span class="chart-badge">Active Stream</span>
        </div>
        <div class="histogram-container">
          <div class="histogram-bars">
            {volumeBuckets.map((bucket, idx) => {
              const maxVal = 10;
              const infoH = (bucket.info / maxVal) * 100;
              const warnH = (bucket.warning / maxVal) * 100;
              const errH = (bucket.error / maxVal) * 100;

              return (
                <div key={idx} class="histogram-col">
                  <div class="histogram-stack">
                    <div class="bar-segment err" style={{ height: `${errH}%` }}></div>
                    <div class="bar-segment warn" style={{ height: `${warnH}%` }}></div>
                    <div class="bar-segment info" style={{ height: `${infoH}%` }}></div>
                  </div>
                  <span class="col-label">{bucket.time}</span>
                </div>
              );
            })}
          </div>
          <div class="chart-legend">
            <span class="legend-item"><span class="dot info"></span> INFO ({summary.infoCount})</span>
            <span class="legend-item"><span class="dot warn"></span> WARNING ({summary.warningCount})</span>
            <span class="legend-item"><span class="dot err"></span> ERROR ({summary.errorCount})</span>
          </div>
        </div>
      </div>

      {/* 2. Severity Distribution Donut Chart */}
      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title">
            <PieChart size={16} class="icon-purple" />
            <span>Severity Distribution</span>
          </div>
          <span class="chart-badge">{summary.totalLogs} Logs</span>
        </div>
        <div class="donut-chart-container">
          <div class="donut-graphic">
            <svg viewBox="0 0 100 100" class="donut-svg">
              {/* SVG Donut Circle Representation */}
              <circle cx="50" cy="50" r="38" fill="none" stroke="#1f293d" strokeWidth="14" />
              <circle
                cx="50" cy="50" r="38"
                fill="none"
                stroke="#3b82f6"
                strokeWidth="14"
                strokeDasharray={`${infoPct * 2.38} 238`}
                strokeDashoffset="0"
                transform="rotate(-90 50 50)"
              />
              <circle
                cx="50" cy="50" r="38"
                fill="none"
                stroke="#f59e0b"
                strokeWidth="14"
                strokeDasharray={`${warningPct * 2.38} 238`}
                strokeDashoffset={`-${infoPct * 2.38}`}
                transform="rotate(-90 50 50)"
              />
              <circle
                cx="50" cy="50" r="38"
                fill="none"
                stroke="#ef4444"
                strokeWidth="14"
                strokeDasharray={`${errorPct * 2.38} 238`}
                strokeDashoffset={`-${(infoPct + warningPct) * 2.38}`}
                transform="rotate(-90 50 50)"
              />
            </svg>
            <div class="donut-center-text">
              <span class="center-value">{summary.totalLogs}</span>
              <span class="center-label">TOTAL</span>
            </div>
          </div>

          <div class="donut-legend-grid">
            <div class="donut-legend-card info">
              <div class="legend-header">
                <span class="dot"></span>
                <span>INFO</span>
              </div>
              <div class="legend-stat">{summary.infoCount} <span class="pct">({infoPct}%)</span></div>
            </div>
            <div class="donut-legend-card warning">
              <div class="legend-header">
                <span class="dot"></span>
                <span>WARNING</span>
              </div>
              <div class="legend-stat">{summary.warningCount} <span class="pct">({warningPct}%)</span></div>
            </div>
            <div class="donut-legend-card error">
              <div class="legend-header">
                <span class="dot"></span>
                <span>ERROR</span>
              </div>
              <div class="legend-stat">{summary.errorCount} <span class="pct">({errorPct}%)</span></div>
            </div>
          </div>
        </div>
      </div>

      {/* 3. Top Occurring Errors Breakdown */}
      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title">
            <AlertTriangle size={16} class="icon-red" />
            <span>Top Recurring Errors</span>
          </div>
          <span class="chart-badge danger">{summary.errorCount} Total Errors</span>
        </div>
        <div class="top-errors-list">
          {topErrors.length === 0 ? (
            <div class="empty-state-text">No recurring error patterns detected.</div>
          ) : (
            topErrors.map(([msg, count], idx) => {
              const pct = Math.round((count / (summary.errorCount || 1)) * 100);
              return (
                <div key={idx} class="error-item">
                  <div class="error-item-info">
                    <span class="error-msg">{msg}</span>
                    <span class="error-count-tag">{count} occurrences ({pct}%)</span>
                  </div>
                  <div class="error-progress-bar">
                    <div class="progress-fill" style={{ width: `${pct}%` }}></div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
