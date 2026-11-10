/**
 * Centralized REST API client for the Healthcare Portal.
 */
const API_BASE = '/api';

const api = {
  async login(email, password, roleType = 'patient') {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, roleType }),
    });
    const data = await res.json();
    if (!res.ok) {
      const err = new Error(data.error || 'Authentication failed');
      err.data = data;
      err.notRegistered = data.notRegistered;
      throw err;
    }
    return data;
  },

  async register(registrationData) {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(registrationData),
    });
    const data = await res.json();
    if (!res.ok) {
      const err = new Error(data.error || 'Registration failed');
      err.data = data;
      throw err;
    }
    return data;
  },

  async logout() {
    try {
      await fetch(`${API_BASE}/auth/logout`, { method: 'POST' });
    } catch (e) {}
    localStorage.removeItem('carepulse_staff_auth');
  },

  async getDoctors(params = {}) {
    const qs = new URLSearchParams(params).toString();
    const res = await fetch(`${API_BASE}/doctors${qs ? '?' + qs : ''}`);
    if (!res.ok) throw new Error('Failed to load doctors');
    return res.json();
  },

  async getDoctorById(id) {
    const res = await fetch(`${API_BASE}/doctors/${encodeURIComponent(id)}`);
    if (!res.ok) throw new Error('Failed to fetch doctor profile');
    return res.json();
  },

  async getServices() {
    const res = await fetch(`${API_BASE}/services`);
    if (!res.ok) throw new Error('Failed to load services');
    return res.json();
  },

  async getAppointments(params = {}) {
    const qs = new URLSearchParams(params).toString();
    const res = await fetch(`${API_BASE}/appointments${qs ? '?' + qs : ''}`);
    if (!res.ok) throw new Error('Failed to load appointments');
    return res.json();
  },

  async createAppointment(appointmentData) {
    const res = await fetch(`${API_BASE}/appointments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(appointmentData),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'Failed to book appointment');
    return data;
  },

  async updateAppointment(id, updateData) {
    const res = await fetch(`${API_BASE}/appointments/${encodeURIComponent(id)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updateData),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'Failed to update appointment');
    return data;
  },

  async cancelAppointment(id, reason = '') {
    const res = await fetch(`${API_BASE}/appointments/${encodeURIComponent(id)}?reason=${encodeURIComponent(reason)}`, {
      method: 'DELETE',
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'Failed to cancel appointment');
    return data;
  },

  async getStats() {
    const res = await fetch(`${API_BASE}/stats`);
    if (!res.ok) throw new Error('Failed to fetch stats');
    return res.json();
  },
};

// UI Toast Notification
function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span>${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</span>
    <div>${message}</div>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}
