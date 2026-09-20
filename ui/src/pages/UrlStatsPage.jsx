import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { urlAPI } from '../api/client';
import LoadingSpinner from '../components/LoadingSpinner';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Line, Bar } from 'react-chartjs-2';
import { QRCodeSVG } from 'qrcode.react';
import './UrlStatsPage.css';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

export default function UrlStatsPage() {
  const { shortCode } = useParams();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  const fetchStats = useCallback(async () => {
    try {
      const { data } = await urlAPI.stats(shortCode);
      setStats(data);
    } catch (err) {
      setError(
        err.response?.status === 404
          ? 'URL not found'
          : 'Failed to load statistics'
      );
    } finally {
      setLoading(false);
    }
  }, [shortCode]);

  useEffect(() => {
    fetchStats();
    // Poll every 10 s to keep click count and charts up-to-date in real time
    const intervalId = setInterval(fetchStats, 10_000);
    return () => clearInterval(intervalId);
  }, [fetchStats]);

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'long',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(stats.shortUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (loading) return <div className="page"><LoadingSpinner text="Loading statistics..." /></div>;

  if (error) {
    return (
      <div className="page">
        <div className="container">
          <div className="empty-state animate-fade-in">
            <div className="empty-state-icon">⚠️</div>
            <h3>{error}</h3>
            <p><Link to="/dashboard" className="btn btn-secondary" style={{ marginTop: 16, display: 'inline-flex' }}>← Back to Dashboard</Link></p>
          </div>
        </div>
      </div>
    );
  }

  const isExpired = stats.expiresAt && new Date(stats.expiresAt) < new Date();

  // ─── Chart Data ────────────────────────────────
  const dailyChartData = {
    labels: (stats.dailyClicks || []).map((d) => {
      const date = new Date(d.date);
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }),
    datasets: [
      {
        label: 'Clicks',
        data: (stats.dailyClicks || []).map((d) => d.count),
        borderColor: 'rgba(99, 102, 241, 1)',
        backgroundColor: 'rgba(99, 102, 241, 0.1)',
        borderWidth: 2,
        pointBackgroundColor: 'rgba(99, 102, 241, 1)',
        pointRadius: 4,
        pointHoverRadius: 6,
        tension: 0.4,
        fill: true,
      },
    ],
  };

  const dailyChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(15, 23, 42, 0.9)',
        titleColor: '#e2e8f0',
        bodyColor: '#e2e8f0',
        borderColor: 'rgba(99, 102, 241, 0.3)',
        borderWidth: 1,
        padding: 12,
        cornerRadius: 8,
      },
    },
    scales: {
      x: {
        grid: { color: 'rgba(148, 163, 184, 0.08)' },
        ticks: { color: 'rgba(148, 163, 184, 0.6)', font: { size: 11 } },
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(148, 163, 184, 0.08)' },
        ticks: {
          color: 'rgba(148, 163, 184, 0.6)',
          font: { size: 11 },
          stepSize: 1,
        },
      },
    },
  };

  const referrerChartData = {
    labels: (stats.topReferrers || []).map((r) => r.referrer),
    datasets: [
      {
        label: 'Clicks',
        data: (stats.topReferrers || []).map((r) => r.count),
        backgroundColor: [
          'rgba(99, 102, 241, 0.8)',
          'rgba(168, 85, 247, 0.8)',
          'rgba(236, 72, 153, 0.8)',
          'rgba(59, 130, 246, 0.8)',
          'rgba(16, 185, 129, 0.8)',
          'rgba(245, 158, 11, 0.8)',
          'rgba(239, 68, 68, 0.8)',
          'rgba(20, 184, 166, 0.8)',
          'rgba(139, 92, 246, 0.8)',
          'rgba(249, 115, 22, 0.8)',
        ],
        borderRadius: 6,
        borderSkipped: false,
      },
    ],
  };

  const referrerChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(15, 23, 42, 0.9)',
        titleColor: '#e2e8f0',
        bodyColor: '#e2e8f0',
        borderColor: 'rgba(99, 102, 241, 0.3)',
        borderWidth: 1,
        padding: 12,
        cornerRadius: 8,
      },
    },
    scales: {
      x: {
        beginAtZero: true,
        grid: { color: 'rgba(148, 163, 184, 0.08)' },
        ticks: { color: 'rgba(148, 163, 184, 0.6)', font: { size: 11 }, stepSize: 1 },
      },
      y: {
        grid: { display: false },
        ticks: { color: 'rgba(148, 163, 184, 0.8)', font: { size: 12 } },
      },
    },
  };

  const hasClicks = (stats.dailyClicks || []).length > 0;
  const hasReferrers = (stats.topReferrers || []).length > 0;

  return (
    <div className="page">
      <div className="container">
        <Link to="/dashboard" className="stats-back animate-fade-in">← Back to Dashboard</Link>

        <div className="stats-header animate-fade-in">
          <h1>URL Statistics</h1>
          <span className={`badge ${isExpired ? 'badge-expired' : 'badge-active'}`}>
            {isExpired ? 'Expired' : 'Active'}
          </span>
        </div>

        {/* URL Info Card */}
        <div className="stats-url-info glass-card animate-fade-in-up">
          <div className="stats-url-row">
            <span className="stats-label">Short URL</span>
            <div className="stats-url-actions">
              <a href={stats.shortUrl} target="_blank" rel="noopener noreferrer" className="stats-short-url">
                {stats.shortUrl.replace(/^https?:\/\//, '')}
              </a>
              <button className="btn-copy" onClick={handleCopy} title="Copy to clipboard">
                {copied ? '✓' : '📋'}
              </button>
            </div>
          </div>
          <div className="stats-url-row">
            <span className="stats-label">Destination</span>
            <a href={stats.longUrl} target="_blank" rel="noopener noreferrer" className="stats-long-url">
              {stats.longUrl}
            </a>
          </div>
        </div>

        {/* Summary Cards */}
        <div className="stats-grid animate-fade-in-up" style={{ animationDelay: '0.1s' }}>
          <div className="stat-card glass-card">
            <div className="stat-icon">👆</div>
            <div className="stat-number">{stats.clickCount ?? 0}</div>
            <div className="stat-label">Total Clicks</div>
          </div>
          <div className="stat-card glass-card">
            <div className="stat-icon">📅</div>
            <div className="stat-date">{formatDate(stats.createdAt)}</div>
            <div className="stat-label">Created</div>
          </div>
          <div className="stat-card glass-card">
            <div className="stat-icon">⏰</div>
            <div className="stat-date">{formatDate(stats.expiresAt)}</div>
            <div className="stat-label">Expires</div>
          </div>
        </div>

        {/* Charts Row */}
        <div className="stats-charts-row animate-fade-in-up" style={{ animationDelay: '0.2s' }}>
          {/* Daily Clicks Chart */}
          <div className="chart-card glass-card">
            <h3 className="chart-title">📈 Daily Clicks (Last 30 Days)</h3>
            {hasClicks ? (
              <div className="chart-container">
                <Line data={dailyChartData} options={dailyChartOptions} />
              </div>
            ) : (
              <div className="chart-empty">
                <span>No click data yet</span>
              </div>
            )}
          </div>

          {/* Top Referrers Chart */}
          <div className="chart-card glass-card">
            <h3 className="chart-title">🔗 Top Referrers</h3>
            {hasReferrers ? (
              <div className="chart-container">
                <Bar data={referrerChartData} options={referrerChartOptions} />
              </div>
            ) : (
              <div className="chart-empty">
                <span>No referrer data yet</span>
              </div>
            )}
          </div>
        </div>

        {/* QR Code */}
        <div className="qr-section glass-card animate-fade-in-up" style={{ animationDelay: '0.3s' }}>
          <h3 className="chart-title">📱 QR Code</h3>
          <p className="qr-description">Scan to open the shortened URL</p>
          <div className="qr-code-wrapper">
            <QRCodeSVG
              value={stats.shortUrl}
              size={180}
              bgColor="transparent"
              fgColor="#e2e8f0"
              level="M"
              includeMargin={false}
            />
          </div>
          <p className="qr-url">{stats.shortUrl}</p>
        </div>
      </div>
    </div>
  );
}
