import React, { useState, useEffect, useRef } from 'react';
import Sidebar from './components/Sidebar';
import ChartsPanel from './components/ChartsPanel';
import AlertsFeed from './components/AlertsFeed';
import LogTable from './components/LogTable';
import ReportModal from './components/ReportModal';
import { 
  Activity, 
  RotateCw, 
  FileText, 
  Database, 
  Upload,
  CheckCircle2, 
  AlertCircle,
  X,
  Layers, 
  Info,
  Server
} from 'lucide-react';
import './App.css';

export default function App() {
  const [activeTab, setActiveTab] = useState('explorer');
  const [selectedLevels, setSelectedLevels] = useState(['INFO', 'WARNING', 'ERROR']);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [dateFilter, setDateFilter] = useState('');

  const [summary, setSummary] = useState({
    totalLogs: 0,
    infoCount: 0,
    warningCount: 0,
    errorCount: 0,
    skippedLines: 0,
    activeFile: 'logs/application.log',
    dbConnected: false,
    dbStatus: 'Checking Supabase connection...'
  });

  const [logs, setLogs] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loadingLogs, setLoadingLogs] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [notification, setNotification] = useState(null);
  const [reportModalOpen, setReportModalOpen] = useState(false);
  const [reportText, setReportText] = useState('');

  const fileInputRef = useRef(null);

  useEffect(() => {
    fetchSummary();
    fetchAlerts();
    fetchLogs();
  }, [selectedLevels, dateFilter]);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchLogs();
    }, 300);
    return () => clearTimeout(timer);
  }, [searchKeyword]);

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 6000);
  };

  const toggleLevel = (lvl) => {
    if (selectedLevels.includes(lvl)) {
      if (selectedLevels.length > 1) {
        setSelectedLevels(selectedLevels.filter(l => l !== lvl));
      }
    } else {
      setSelectedLevels([...selectedLevels, lvl]);
    }
  };

  const handleClearFilters = () => {
    setSelectedLevels(['INFO', 'WARNING', 'ERROR']);
    setSearchKeyword('');
    setDateFilter('');
  };

  const fetchSummary = async () => {
    try {
      const res = await fetch('/api/summary');
      if (res.ok) {
        const data = await res.json();
        setSummary(data);
      }
    } catch (err) {
      console.error('Summary fetch error:', err);
    }
  };

  const fetchAlerts = async () => {
    try {
      const res = await fetch('/api/alerts');
      if (res.ok) {
        const data = await res.json();
        setAlerts(data);
      }
    } catch (err) {
      console.error('Alerts fetch error:', err);
    }
  };

  const fetchLogs = async () => {
    setLoadingLogs(true);
    try {
      let levelParam = 'ALL';
      if (selectedLevels.length === 1) {
        levelParam = selectedLevels[0];
      }

      let url = `/api/logs?level=${encodeURIComponent(levelParam)}`;
      if (searchKeyword.trim()) {
        url += `&search=${encodeURIComponent(searchKeyword.trim())}`;
      }
      if (dateFilter) {
        url += `&date=${encodeURIComponent(dateFilter)}`;
      }

      const res = await fetch(url);
      if (res.ok) {
        let data = await res.json();
        if (selectedLevels.length > 0 && selectedLevels.length < 3) {
          data = data.filter(item => selectedLevels.includes(item.level));
        }
        setLogs(data);
      }
    } catch (err) {
      console.error('Logs fetch error:', err);
    } finally {
      setLoadingLogs(false);
    }
  };

  const handleUploadClick = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.name.endsWith('.log') && !file.name.endsWith('.txt')) {
      showNotification('error', 'Invalid file type. Please select a .log or .txt file.');
      return;
    }

    setUploading(true);
    try {
      const text = await file.text();
      const res = await fetch('/api/upload', {
        method: 'POST',
        headers: {
          'Content-Type': 'text/plain; charset=UTF-8'
        },
        body: text
      });

      const data = await res.json();
      if (data.success) {
        // Reset search & date filters to display all uploaded entries
        setSelectedLevels(['INFO', 'WARNING', 'ERROR']);
        setSearchKeyword('');
        setDateFilter('');

        showNotification('success', data.message);
        await fetchSummary();
        await fetchLogs();
        await fetchAlerts();
      } else {
        showNotification('error', data.message || 'File processing failed.');
      }
    } catch (err) {
      showNotification('error', 'Failed to upload log file: ' + err.message);
    } finally {
      setUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleSyncDatabase = async () => {
    setSyncing(true);
    try {
      const res = await fetch('/api/sync', { method: 'POST' });
      const data = await res.json();
      showNotification(data.success ? 'success' : 'error', data.message);
      fetchSummary();
      fetchLogs();
    } catch (err) {
      showNotification('error', 'Failed to connect to database sync API.');
    } finally {
      setSyncing(false);
    }
  };

  const handleViewReport = async () => {
    setReportModalOpen(true);
    setReportText('Generating summary report...');
    try {
      const res = await fetch('/api/report');
      if (res.ok) {
        const text = await res.text();
        setReportText(text);
      }
    } catch (err) {
      setReportText('Failed to load report.');
    }
  };

  return (
    <div class="app-layout">
      {/* Hidden File Selector Input */}
      <input
        type="file"
        ref={fileInputRef}
        accept=".log,.txt"
        style={{ display: 'none' }}
        onChange={handleFileUpload}
      />

      {/* Left Sidebar Navigation & Filter Accordion */}
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        selectedLevels={selectedLevels}
        toggleLevel={toggleLevel}
        searchKeyword={searchKeyword}
        setSearchKeyword={setSearchKeyword}
        dateFilter={dateFilter}
        setDateFilter={setDateFilter}
        summary={summary}
        handleClearFilters={handleClearFilters}
      />

      {/* Main Content Area */}
      <div class="main-viewport">
        {/* Top Header */}
        <header class="top-header">
          <div class="header-title-wrapper">
            <h1>Logs Explorer & Security Observability</h1>
            <span class="badge-connected">
              <span class="dot"></span>
              {summary.dbConnected ? 'Connected to Supabase PostgreSQL' : 'Local Mode'}
            </span>
          </div>

          <div class="header-right-actions">
            <button class="btn btn-upload" onClick={handleUploadClick} disabled={uploading}>
              <Upload size={14} />
              <span>{uploading ? 'Analyzing File...' : 'Upload Log'}</span>
            </button>

            <button class="btn btn-secondary" onClick={handleSyncDatabase} disabled={syncing}>
              <Database size={14} />
              <span>{syncing ? 'Syncing...' : 'Sync Supabase'}</span>
            </button>

            <button class="btn btn-primary" onClick={handleViewReport}>
              <FileText size={14} />
              <span>Generate Report</span>
            </button>
          </div>
        </header>

        {/* Notification Toast Banner */}
        {notification && (
          <div class={`toast-notification ${notification.type}`}>
            {notification.type === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
            <span>{notification.message}</span>
            <button class="btn-toast-close" onClick={() => setNotification(null)}>
              <X size={14} />
            </button>
          </div>
        )}

        {/* Viewport Content */}
        <div class="content-body">
          {/* Top Metrics Cards Row */}
          <div class="metrics-row">
            <div class="metric-card card-total">
              <div class="metric-info">
                <span class="label">Total Logs</span>
                <div class="val">{summary.totalLogs}</div>
              </div>
              <div class="metric-icon-box icon-blue">
                <Layers size={20} />
              </div>
            </div>

            <div class="metric-card card-info">
              <div class="metric-info">
                <span class="label">INFO Logs</span>
                <div class="val">{summary.infoCount}</div>
              </div>
              <div class="metric-icon-box icon-blue">
                <Info size={20} />
              </div>
            </div>

            <div class="metric-card card-warning">
              <div class="metric-info">
                <span class="label">WARNING Logs</span>
                <div class="val">{summary.warningCount}</div>
              </div>
              <div class="metric-icon-box icon-amber">
                <AlertCircle size={20} />
              </div>
            </div>

            <div class="metric-card card-error">
              <div class="metric-info">
                <span class="label">ERROR Logs</span>
                <div class="val">{summary.errorCount}</div>
              </div>
              <div class="metric-icon-box icon-red">
                <Server size={20} />
              </div>
            </div>
          </div>

          {/* Observability Charts Panel */}
          <ChartsPanel summary={summary} logs={logs} />

          {/* Security Alerts Feed Panel */}
          <AlertsFeed alerts={alerts} />

          {/* Expandable Logs Explorer Table */}
          <LogTable logs={logs} loading={loadingLogs} />
        </div>
      </div>

      {/* Summary Report Modal */}
      <ReportModal
        open={reportModalOpen}
        onClose={() => setReportModalOpen(false)}
        reportText={reportText}
      />
    </div>
  );
}
