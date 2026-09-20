import { useState } from 'react';

export default function EditUrlModal({ url, onClose, onUpdate }) {
  const [longUrl, setLongUrl] = useState(url.longUrl);
  const [expiresAt, setExpiresAt] = useState(
    url.expiresAt ? new Date(url.expiresAt).toISOString().slice(0, 16) : ''
  );
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!longUrl.trim()) {
      setError('URL is required');
      return;
    }

    setLoading(true);
    try {
      const data = {
        longUrl: longUrl.trim(),
        expiresAt: expiresAt ? new Date(expiresAt).toISOString().replace('Z', '') : null,
      };
      await onUpdate(url.shortCode, data);
      onClose();
    } catch (err) {
      const msg =
        err.response?.data?.messages?.[0] ||
        err.response?.data?.message ||
        'Failed to update URL';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Edit URL</h2>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>

        <div style={{ marginBottom: 16, padding: '8px 12px', background: 'var(--bg-glass)', borderRadius: 'var(--radius-sm)' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-tertiary)' }}>Short Code: </span>
          <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-accent)' }}>{url.shortCode}</span>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group" style={{ marginBottom: 16 }}>
            <label className="form-label" htmlFor="edit-long-url">Destination URL</label>
            <input
              id="edit-long-url"
              type="url"
              className="form-input"
              value={longUrl}
              onChange={(e) => setLongUrl(e.target.value)}
              autoFocus
              required
            />
          </div>

          <div className="form-group" style={{ marginBottom: 16 }}>
            <label className="form-label" htmlFor="edit-expires">Expiration Date</label>
            <input
              id="edit-expires"
              type="datetime-local"
              className="form-input"
              value={expiresAt}
              onChange={(e) => setExpiresAt(e.target.value)}
            />
          </div>

          {error && <p className="form-error" style={{ marginBottom: 12 }}>{error}</p>}

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading} id="edit-url-submit">
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
