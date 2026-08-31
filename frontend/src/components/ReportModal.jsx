import React from 'react';
import { FileText, Download, Copy, X, Check } from 'lucide-react';

export default function ReportModal({ open, onClose, reportText }) {
  const [copied, setCopied] = React.useState(false);

  if (!open) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(reportText);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownload = () => {
    const blob = new Blob([reportText], { type: 'text/plain;charset=utf-8' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'summary_report.txt';
    link.click();
  };

  return (
    <div class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <div class="modal-title">
            <FileText size={18} class="icon-blue" />
            <h3>Generated Summary Report</h3>
          </div>
          <button class="btn-close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>
        <div class="modal-body">
          <pre class="report-code">{reportText || 'Generating summary report...'}</pre>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" onClick={handleCopy}>
            {copied ? <Check size={14} color="#10b981" /> : <Copy size={14} />}
            <span>{copied ? 'Copied!' : 'Copy'}</span>
          </button>
          <button class="btn btn-primary" onClick={handleDownload}>
            <Download size={14} />
            <span>Download Report (.txt)</span>
          </button>
        </div>
      </div>
    </div>
  );
}
