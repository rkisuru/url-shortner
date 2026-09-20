import './LoadingSpinner.css';

export default function LoadingSpinner({ size = 'md', text }) {
  return (
    <div className="loading-spinner-container">
      <div className={`loading-spinner loading-spinner-${size}`} />
      {text && <p className="loading-text">{text}</p>}
    </div>
  );
}
