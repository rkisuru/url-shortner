import { useState } from 'react';

export default function CreateUrlModal({ onClose, onCreate }) {
  const [longUrl, setLongUrl] = useState('');
  const [customAlias, setCustomAlias] = useState('');
  const [expiresAt, setExpiresAt] = useState('');
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
        customAlias: customAlias.trim() || null,
        expiresAt: expiresAt ? new Date(expiresAt).toISOString().replace('Z', '') : null,
      };
      await onCreate(data);
      onClose();
    } catch (err) {
      const msg =
        err.response?.data?.messages?.[0] ||
        err.response?.data?.message ||
        'Failed to create URL';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Shorten a URL</h2>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group" style={{ marginBottom: 16 }}>
            <label className="form-label" htmlFor="create-long-url">Destination URL</label>
            <input
              id="create-long-url"
              type="url"
              className="form-input"
              placeholder="https://example.com/very-long-path..."
              value={longUrl}
              onChange={(e) => setLongUrl(e.target.value)}
              autoFocus
              required
            />
          </div>

          <div className="form-group" style={{ marginBottom: 16 }}>
            <label className="form-label" htmlFor="create-alias">Custom Alias (optional)</label>
            <input
              id="create-alias"
              type="text"
              className="form-input"
              placeholder="my-brand"
              value={customAlias}
              onChange={(e) => setCustomAlias(e.target.value)}
              pattern="^[a-zA-Z0-9_-]{3,20}$"
              title="3-20 alphanumeric characters, hyphens, or underscores"
            />
          </div>

          <div className="form-group" style={{ marginBottom: 16 }}>
            <label className="form-label" htmlFor="create-expires">Expiration Date (optional)</label>
            <input
              id="create-expires"
              type="datetime-local"
              className="form-input"
              value={expiresAt}
              onChange={(e) => setExpiresAt(e.target.value)}
              min={new Date().toISOString().slice(0, 16)}
            />
          </div>

          {error && <p className="form-error" style={{ marginBottom: 12 }}>{error}</p>}

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading} id="create-url-submit">
              {loading ? 'Creating...' : '✂ Shorten URL'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
