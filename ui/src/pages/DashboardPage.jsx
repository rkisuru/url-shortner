import { useState, useEffect, useCallback } from 'react';
import { urlAPI } from '../api/client';
import UrlCard from '../components/UrlCard';
import CreateUrlModal from '../components/CreateUrlModal';
import EditUrlModal from '../components/EditUrlModal';
import LoadingSpinner from '../components/LoadingSpinner';
import './DashboardPage.css';

export default function DashboardPage() {
  const [urls, setUrls] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [editingUrl, setEditingUrl] = useState(null);
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const fetchUrls = useCallback(async () => {
    try {
      const { data } = await urlAPI.getAll();
      // Sort newest first by createdAt
      const sorted = [...data].sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
      );
      setUrls(sorted);
      setError('');
    } catch (err) {
      setError('Failed to load URLs');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUrls();
    // Poll every 10 s to keep click counts up-to-date in real time
    const intervalId = setInterval(fetchUrls, 10_000);
    return () => clearInterval(intervalId);
  }, [fetchUrls]);

  const handleCreate = async (data) => {
    const { data: newUrl } = await urlAPI.create(data);
    // Prepend and keep sorted by createdAt descending
    setUrls((prev) =>
      [newUrl, ...prev].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    );
    showToast('URL created successfully!');
  };

  const handleUpdate = async (shortCode, data) => {
    const { data: updated } = await urlAPI.update(shortCode, data);
    setUrls((prev) =>
      prev.map((u) => (u.shortCode === shortCode ? updated : u))
    );
    showToast('URL updated successfully!');
  };

  const handleDelete = async (shortCode) => {
    if (!window.confirm('Delete this URL? This action cannot be undone.')) return;
    try {
      await urlAPI.delete(shortCode);
      setUrls((prev) => prev.filter((u) => u.shortCode !== shortCode));
      showToast('URL deleted');
    } catch {
      showToast('Failed to delete URL', 'error');
    }
  };

  return (
    <div className="page">
      <div className="container">
        <div className="dashboard-header animate-fade-in">
          <div>
            <h1 className="dashboard-title">Your URLs</h1>
            <p className="dashboard-subtitle">
              {urls.length > 0
                ? `${urls.length} shortened URL${urls.length !== 1 ? 's' : ''}`
                : 'Create your first short URL'}
            </p>
          </div>
          <button
            className="btn btn-primary"
            onClick={() => setShowCreate(true)}
            id="create-url-btn"
          >
            + New URL
          </button>
        </div>

        {loading ? (
          <LoadingSpinner text="Loading your URLs..." />
        ) : error ? (
          <div className="empty-state animate-fade-in">
            <div className="empty-state-icon">⚠️</div>
            <h3>{error}</h3>
            <p>
              <button className="btn btn-secondary" onClick={fetchUrls}>
                Try Again
              </button>
            </p>
          </div>
        ) : urls.length === 0 ? (
          <div className="empty-state animate-fade-in">
            <div className="empty-state-icon">✂️</div>
            <h3>No URLs yet</h3>
            <p>Click "New URL" to create your first shortened link.</p>
          </div>
        ) : (
          <div className="url-list stagger-children">
            {urls.map((url) => (
              <UrlCard
                key={url.shortCode}
                url={url}
                onDelete={handleDelete}
                onEdit={setEditingUrl}
              />
            ))}
          </div>
        )}
      </div>

      {showCreate && (
        <CreateUrlModal
          onClose={() => setShowCreate(false)}
          onCreate={handleCreate}
        />
      )}

      {editingUrl && (
        <EditUrlModal
          url={editingUrl}
          onClose={() => setEditingUrl(null)}
          onUpdate={handleUpdate}
        />
      )}

      {toast && (
        <div className={`toast toast-${toast.type}`}>
          {toast.message}
        </div>
      )}
    </div>
  );
}
