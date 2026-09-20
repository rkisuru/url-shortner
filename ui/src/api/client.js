import axios from 'axios';

const client = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor — attach JWT token to every request
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor — handle 401 (expired/invalid token)
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // Redirect to login if not already there
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// ─── Auth API ───────────────────────────────────
export const authAPI = {
  register: (data) => client.post('/auth/register', data),
  login: (data) => client.post('/auth/login', data),
  me: () => client.get('/users/me'),
};

// ─── URL API ────────────────────────────────────
export const urlAPI = {
  create: (data) => client.post('/urls', data),
  getAll: () => client.get('/urls'),
  getOne: (shortCode) => client.get(`/urls/${shortCode}`),
  update: (shortCode, data) => client.put(`/urls/${shortCode}`, data),
  delete: (shortCode) => client.delete(`/urls/${shortCode}`),
  stats: (shortCode) => client.get(`/urls/${shortCode}/stats`),
};

export default client;
