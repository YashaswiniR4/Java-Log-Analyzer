document.addEventListener('DOMContentLoaded', () => {
    let currentLevel = 'ALL';
    let searchDebounceTimer = null;

    const statTotal = document.getElementById('stat-total');
    const statInfo = document.getElementById('stat-info');
    const statWarning = document.getElementById('stat-warning');
    const statError = document.getElementById('stat-error');
    const statSkipped = document.getElementById('stat-skipped');
    const alertCountBadge = document.getElementById('alert-count-badge');
    const alertsContainer = document.getElementById('alerts-container');
    const logsTbody = document.getElementById('logs-tbody');

    const searchInput = document.getElementById('search-input');
    const dateInput = document.getElementById('date-input');
    const btnClear = document.getElementById('btn-clear');
    const btnSyncDb = document.getElementById('btn-sync-db');
    const pills = document.querySelectorAll('.pill');

    const btnReport = document.getElementById('btn-report');
    const reportModal = document.getElementById('report-modal');
    const reportText = document.getElementById('report-text');
    const btnCloseModal = document.getElementById('btn-close-modal');
    const btnDismissModal = document.getElementById('btn-dismiss-modal');
    const btnDownloadReport = document.getElementById('btn-download-report');

    initDashboard();

    function initDashboard() {
        fetchSummary();
        fetchAlerts();
        fetchLogs();
        setupEventListeners();
    }

    function setupEventListeners() {
        pills.forEach(pill => {
            pill.addEventListener('click', () => {
                pills.forEach(p => p.classList.remove('active'));
                pill.classList.add('active');
                currentLevel = pill.getAttribute('data-level');
                fetchLogs();
            });
        });

        searchInput.addEventListener('input', () => {
            clearTimeout(searchDebounceTimer);
            searchDebounceTimer = setTimeout(() => {
                fetchLogs();
            }, 250);
        });

        dateInput.addEventListener('change', () => {
            fetchLogs();
        });

        btnClear.addEventListener('click', () => {
            currentLevel = 'ALL';
            pills.forEach(p => p.classList.remove('active'));
            document.querySelector('.pill[data-level="ALL"]').classList.add('active');
            searchInput.value = '';
            dateInput.value = '';
            fetchLogs();
        });

        btnSyncDb.addEventListener('click', syncDatabase);

        btnReport.addEventListener('click', openReportModal);
        btnCloseModal.addEventListener('click', closeModal);
        btnDismissModal.addEventListener('click', closeModal);
        btnDownloadReport.addEventListener('click', downloadReport);
    }

    async function fetchSummary() {
        try {
            const res = await fetch('/api/summary');
            if (!res.ok) throw new Error('Failed to fetch summary');
            const data = await res.json();

            statTotal.textContent = data.totalLogs;
            statInfo.textContent = data.infoCount;
            statWarning.textContent = data.warningCount;
            statError.textContent = data.errorCount;
            statSkipped.textContent = data.skippedLines;

            if (data.supabaseConfigured) {
                btnSyncDb.textContent = '⚡ Sync with Supabase (Connected)';
            } else {
                btnSyncDb.textContent = '⚙️ Config Supabase DB';
            }
        } catch (err) {
            console.error('Summary fetch error:', err);
        }
    }

    async function fetchAlerts() {
        try {
            const res = await fetch('/api/alerts');
            if (!res.ok) throw new Error('Failed to fetch alerts');
            const alerts = await res.json();

            alertCountBadge.textContent = `${alerts.length} Alert${alerts.length === 1 ? '' : 's'}`;

            if (alerts.length === 0) {
                alertsContainer.innerHTML = `
                    <div style="grid-column: 1/-1; color: var(--text-muted); font-size: 14px;">
                        ✅ No operational anomalies or security alerts detected. System is running normally.
                    </div>`;
                return;
            }

            alertsContainer.innerHTML = alerts.map(alert => {
                const isSecurity = alert.ruleType.includes('SECURITY');
                const badgeClass = isSecurity ? 'warning' : 'danger';
                return `
                    <div class="alert-card ${isSecurity ? 'security' : ''}">
                        <div class="alert-card-header">
                            <span class="alert-badge ${badgeClass}">${alert.ruleType}</span>
                            <span class="alert-time">${alert.timestamp}</span>
                        </div>
                        <div class="alert-msg">${alert.message}</div>
                    </div>
                `;
            }).join('');
        } catch (err) {
            console.error('Alerts fetch error:', err);
            alertsContainer.innerHTML = `<div style="color: var(--color-error);">Failed to load alerts.</div>`;
        }
    }

    async function fetchLogs() {
        try {
            let url = '/api/logs?';
            const queryParams = [];

            if (currentLevel !== 'ALL') {
                queryParams.push(`level=${encodeURIComponent(currentLevel)}`);
            }
            if (searchInput.value.trim() !== '') {
                queryParams.push(`search=${encodeURIComponent(searchInput.value.trim())}`);
            }
            if (dateInput.value !== '') {
                queryParams.push(`date=${encodeURIComponent(dateInput.value)}`);
            }

            url += queryParams.join('&');

            const res = await fetch(url);
            if (!res.ok) throw new Error('Failed to fetch logs');
            const logs = await res.json();

            renderLogsTable(logs);
        } catch (err) {
            console.error('Logs fetch error:', err);
            logsTbody.innerHTML = `<tr><td colspan="4" style="color: var(--color-error); text-align: center;">Error loading log entries.</td></tr>`;
        }
    }

    function renderLogsTable(logs) {
        if (!logs || logs.length === 0) {
            logsTbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 24px;">No matching log entries found.</td></tr>`;
            return;
        }

        logsTbody.innerHTML = logs.map(entry => {
            const levelClass = entry.level.toLowerCase();
            const lineNum = entry.rawLineNumber > 0 ? entry.rawLineNumber : '-';
            return `
                <tr>
                    <td class="line-col">#${lineNum}</td>
                    <td class="time-col">${entry.timestamp}</td>
                    <td><span class="badge-level badge-${levelClass}">${entry.level}</span></td>
                    <td class="msg-col">${escapeHtml(entry.message)}</td>
                </tr>
            `;
        }).join('');
    }

    async function syncDatabase() {
        btnSyncDb.disabled = true;
        btnSyncDb.textContent = '⏳ Syncing with Supabase...';
        try {
            const res = await fetch('/api/db-sync', { method: 'POST' });
            const data = await res.json();
            alert(data.message);
        } catch (err) {
            alert('Failed to connect to database sync API.');
        } finally {
            btnSyncDb.disabled = false;
            fetchSummary();
        }
    }

    async function openReportModal() {
        reportModal.classList.remove('hidden');
        reportText.textContent = 'Generating summary report...';
        try {
            const res = await fetch('/api/report');
            if (!res.ok) throw new Error('Failed to load report');
            const text = await res.text();
            reportText.textContent = text;
        } catch (err) {
            reportText.textContent = 'Failed to load report content.';
        }
    }

    function closeModal() {
        reportModal.classList.add('hidden');
    }

    function downloadReport() {
        const text = reportText.textContent;
        const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'summary_report.txt';
        link.click();
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, "&amp;")
                  .replace(/</g, "&lt;")
                  .replace(/>/g, "&gt;")
                  .replace(/"/g, "&quot;")
                  .replace(/'/g, "&#039;");
    }
});
