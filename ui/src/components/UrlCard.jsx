import { useState } from 'react';
import { Link } from 'react-router-dom';
import './UrlCard.css';

export default function UrlCard({ url, onDelete, onEdit }) {
  const [copied, setCopied] = useState(false);

  const isExpired = url.expiresAt && new Date(url.expiresAt) < new Date();

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(url.shortUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Fallback
      const input = document.createElement('input');
      input.value = url.shortUrl;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  return (
    <div className="url-card glass-card animate-fade-in">
      <div className="url-card-header">
        <div className="url-card-code-row">
          <a
            href={url.shortUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="url-card-short"
          >
            {url.shortUrl.replace(/^https?:\/\//, '')}
          </a>
          <span className={`badge ${isExpired ? 'badge-expired' : 'badge-active'}`}>
            {isExpired ? 'Expired' : 'Active'}
          </span>
        </div>
        <p className="url-card-long" title={url.longUrl}>
          {url.longUrl}
        </p>
      </div>

      <div className="url-card-meta">
        <div className="url-card-meta-item">
          <span className="meta-label">Clicks</span>
          <span className="meta-value">{url.clickCount ?? 0}</span>
        </div>
        <div className="url-card-meta-item">
          <span className="meta-label">Created</span>
          <span className="meta-value">{formatDate(url.createdAt)}</span>
        </div>
        <div className="url-card-meta-item">
          <span className="meta-label">Expires</span>
          <span className="meta-value">{formatDate(url.expiresAt)}</span>
        </div>
      </div>

      <div className="url-card-actions">
        <button className="btn btn-secondary btn-sm" onClick={handleCopy} id={`copy-${url.shortCode}`}>
          {copied ? '✓ Copied' : '⧉ Copy'}
        </button>
        <Link to={`/stats/${url.shortCode}`} className="btn btn-secondary btn-sm" id={`stats-${url.shortCode}`}>
          📊 Stats
        </Link>
        <button className="btn btn-secondary btn-sm" onClick={() => onEdit(url)} id={`edit-${url.shortCode}`}>
          ✏️ Edit
        </button>
        <button className="btn btn-danger btn-sm" onClick={() => onDelete(url.shortCode)} id={`delete-${url.shortCode}`}>
          🗑 Delete
        </button>
      </div>
    </div>
  );
}
