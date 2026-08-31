import React, { useState } from 'react';
import { 
  ChevronDown, 
  ChevronRight, 
  Terminal, 
  Clock, 
  Tag, 
  Hash, 
  Info, 
  AlertTriangle, 
  AlertOctagon 
} from 'lucide-react';

export default function LogTable({ logs, loading }) {
  const [expandedIndex, setExpandedIndex] = useState(null);

  const toggleExpand = (idx) => {
    setExpandedIndex(expandedIndex === idx ? null : idx);
  };

  const getLevelBadge = (level) => {
    switch (level) {
      case 'ERROR':
        return (
          <span class="badge-level badge-error">
            <AlertOctagon size={12} /> ERROR
          </span>
        );
      case 'WARNING':
        return (
          <span class="badge-level badge-warning">
            <AlertTriangle size={12} /> WARNING
          </span>
        );
      default:
        return (
          <span class="badge-level badge-info">
            <Info size={12} /> INFO
          </span>
        );
    }
  };

  return (
    <div class="log-table-container">
      <div class="table-wrapper">
        <table class="logs-table">
          <thead>
            <tr>
              <th class="th-expand"></th>
              <th class="th-line">Line #</th>
              <th class="th-time">Timestamp</th>
              <th class="th-level">Level</th>
              <th class="th-msg">Log Event Message</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="5" class="loading-td">
                  <div class="loading-spinner"></div>
                  <span>Fetching log records...</span>
                </td>
              </tr>
            ) : logs.length === 0 ? (
              <tr>
                <td colSpan="5" class="empty-td">
                  No matching log entries found for active filters.
                </td>
              </tr>
            ) : (
              logs.map((log, idx) => {
                const isExpanded = expandedIndex === idx;
                return (
                  <React.Fragment key={idx}>
                    <tr 
                      class={`log-row row-level-${log.level.toLowerCase()} ${isExpanded ? 'expanded' : ''}`}
                      onClick={() => toggleExpand(idx)}
                    >
                      <td class="td-expand">
                        {isExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                      </td>
                      <td class="td-line">#{log.rawLineNumber > 0 ? log.rawLineNumber : '-'}</td>
                      <td class="td-time">{log.timestamp}</td>
                      <td class="td-level">{getLevelBadge(log.level)}</td>
                      <td class="td-msg">{log.message}</td>
                    </tr>
                    {isExpanded && (
                      <tr class="log-detail-row">
                        <td colSpan="5">
                          <div class="log-detail-card">
                            <div class="detail-header">
                              <Terminal size={14} />
                              <span>JSON Event Inspector</span>
                            </div>
                            <pre class="json-inspector">
{JSON.stringify({
  lineNumber: log.rawLineNumber,
  timestamp: log.timestamp,
  level: log.level,
  message: log.message,
  parsedAt: new Date().toISOString()
}, null, 2)}
                            </pre>
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
