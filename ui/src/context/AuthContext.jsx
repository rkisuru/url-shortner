import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authAPI } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [loading, setLoading] = useState(false);

  const isAuthenticated = !!token;

  const saveAuth = useCallback((authResponse) => {
    const { token: jwt, username, email, role } = authResponse;
    localStorage.setItem('token', jwt);
    localStorage.setItem('user', JSON.stringify({ username, email, role }));
    setToken(jwt);
    setUser({ username, email, role });
  }, []);

  const login = useCallback(async (credentials) => {
    setLoading(true);
    try {
      const { data } = await authAPI.login(credentials);
      saveAuth(data);
      return { success: true };
    } catch (error) {
      const message = error.response?.data?.message || 'Login failed';
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, [saveAuth]);

  const register = useCallback(async (credentials) => {
    setLoading(true);
    try {
      const { data } = await authAPI.register(credentials);
      saveAuth(data);
      return { success: true };
    } catch (error) {
      const message =
        error.response?.data?.message ||
        error.response?.data?.fieldErrors
          ? Object.values(error.response.data.fieldErrors).join(', ')
          : 'Registration failed';
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, [saveAuth]);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  // Verify token on mount
  useEffect(() => {
    if (token && !user) {
      authAPI.me()
        .then(({ data }) => setUser({ username: data.username, email: data.email, role: data.role }))
        .catch(() => logout());
    }
  }, [token, user, logout]);

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
