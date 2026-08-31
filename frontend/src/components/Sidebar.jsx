import React from 'react';
import { 
  LayoutDashboard, 
  Search, 
  ShieldAlert, 
  Database, 
  FileText, 
  CheckSquare, 
  Square,
  Calendar,
  Filter,
  Activity
} from 'lucide-react';

export default function Sidebar({
  activeTab,
  setActiveTab,
  selectedLevels,
  toggleLevel,
  searchKeyword,
  setSearchKeyword,
  dateFilter,
  setDateFilter,
  summary,
  handleClearFilters
}) {
  return (
    <aside class="sidebar">
      {/* Brand Header */}
      <div class="sidebar-brand">
        <div class="brand-icon">
          <Activity size={22} color="#ffffff" />
        </div>
        <div class="brand-text">
          <h2>LogAnalyzer <span class="brand-badge">PRO</span></h2>
          <p>Enterprise Observability</p>
        </div>
      </div>

      {/* Navigation Menu */}
      <div class="sidebar-section">
        <div class="section-title">NAVIGATION</div>
        <nav class="sidebar-nav">
          <button 
            class={`nav-item ${activeTab === 'explorer' ? 'active' : ''}`}
            onClick={() => setActiveTab('explorer')}
          >
            <Search size={18} />
            <span>Logs Explorer</span>
          </button>
          <button 
            class={`nav-item ${activeTab === 'alerts' ? 'active' : ''}`}
            onClick={() => setActiveTab('alerts')}
          >
            <ShieldAlert size={18} />
            <span>Security Alerts</span>
            {summary.errorCount > 0 && (
              <span class="nav-badge danger">{summary.errorCount}</span>
            )}
          </button>
          <button 
            class={`nav-item ${activeTab === 'supabase' ? 'active' : ''}`}
            onClick={() => setActiveTab('supabase')}
          >
            <Database size={18} />
            <span>Supabase DB</span>
            <span class={`status-dot ${summary.dbConnected ? 'online' : 'offline'}`}></span>
          </button>
        </nav>
      </div>

      {/* Filters Accordion Panel */}
      <div class="sidebar-section filters-section">
        <div class="section-header">
          <span class="section-title"><Filter size={14} /> FILTERS</span>
          <button class="btn-text-clear" onClick={handleClearFilters}>Clear all</button>
        </div>

        {/* Search Input */}
        <div class="filter-group">
          <label class="filter-label">Search Query</label>
          <div class="search-input-wrapper">
            <Search size={14} class="search-icon-input" />
            <input
              type="text"
              class="sidebar-input"
              placeholder="Search keyword (db, failed)..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
            />
          </div>
        </div>

        {/* Severity Checkboxes */}
        <div class="filter-group">
          <label class="filter-label">Severity Level</label>
          <div class="severity-checkboxes">
            <button 
              class={`checkbox-row ${selectedLevels.includes('INFO') ? 'checked info' : ''}`}
              onClick={() => toggleLevel('INFO')}
            >
              {selectedLevels.includes('INFO') ? <CheckSquare size={16} /> : <Square size={16} />}
              <span class="severity-tag tag-info">INFO</span>
              <span class="count-pill">{summary.infoCount}</span>
            </button>

            <button 
              class={`checkbox-row ${selectedLevels.includes('WARNING') ? 'checked warning' : ''}`}
              onClick={() => toggleLevel('WARNING')}
            >
              {selectedLevels.includes('WARNING') ? <CheckSquare size={16} /> : <Square size={16} />}
              <span class="severity-tag tag-warning">WARNING</span>
              <span class="count-pill">{summary.warningCount}</span>
            </button>

            <button 
              class={`checkbox-row ${selectedLevels.includes('ERROR') ? 'checked error' : ''}`}
              onClick={() => toggleLevel('ERROR')}
            >
              {selectedLevels.includes('ERROR') ? <CheckSquare size={16} /> : <Square size={16} />}
              <span class="severity-tag tag-error">ERROR</span>
              <span class="count-pill">{summary.errorCount}</span>
            </button>
          </div>
        </div>

        {/* Date Filter */}
        <div class="filter-group">
          <label class="filter-label">Date Filter</label>
          <div class="date-input-wrapper">
            <Calendar size={14} class="date-icon-input" />
            <input
              type="date"
              class="sidebar-input"
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)}
            />
          </div>
        </div>
      </div>

      {/* Sidebar Footer */}
      <div class="sidebar-footer">
        <div class="db-status-card">
          <div class="status-indicator">
            <span class={`dot ${summary.dbConnected ? 'online' : 'offline'}`}></span>
            <span class="status-text">{summary.dbConnected ? 'Supabase PostgreSQL' : 'Local Mode'}</span>
          </div>
          <p class="status-sub">{summary.dbStatus}</p>
        </div>
      </div>
    </aside>
  );
}
