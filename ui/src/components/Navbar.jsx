import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="navbar-brand">
          <span className="navbar-logo">✂</span>
          <span className="navbar-title">Snip</span>
        </Link>

        <div className="navbar-right">
          {isAuthenticated ? (
            <>
              <Link to="/dashboard" className="navbar-link">Dashboard</Link>
              <div className="navbar-user">
                <span className="navbar-avatar">{user?.username?.charAt(0).toUpperCase()}</span>
                <span className="navbar-username">{user?.username}</span>
              </div>
              <button onClick={handleLogout} className="btn btn-ghost btn-sm" id="logout-btn">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost btn-sm">Login</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Sign Up</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
