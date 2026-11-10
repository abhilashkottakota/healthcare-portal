/**
 * Client controller for admin.html (Doctor & Hospital Staff Portal with Auth Protection)
 */
let adminAppointments = [];

document.addEventListener('DOMContentLoaded', () => {
  checkAuthState();
});

function checkAuthState() {
  const authRaw = localStorage.getItem('carepulse_staff_auth');
  const loginGate = document.getElementById('loginGateSection');
  const dashboard = document.getElementById('protectedDashboardSection');
  const navAuth = document.getElementById('navAuthSection');

  if (authRaw) {
    try {
      const auth = JSON.parse(authRaw);
      loginGate.style.display = 'none';
      dashboard.style.display = 'block';

      if (navAuth) {
        navAuth.innerHTML = `
          <div style="display: flex; align-items: center; gap: 0.6rem;">
            <span class="badge badge-confirmed">● Active: ${auth.name}</span>
            <button class="btn btn-sm btn-outline" onclick="handleLogout()" style="color: var(--danger); border-color: var(--danger);">Logout</button>
          </div>
        `;
      }

      const greeting = document.getElementById('authenticatedUserGreeting');
      if (greeting) {
        greeting.textContent = `Logged in as ${auth.name} (${auth.role}). You have full access to patient queue and diagnosis notes.`;
      }

      loadAdminData();
      return;
    } catch (e) {
      localStorage.removeItem('carepulse_staff_auth');
    }
  }

  // Not authenticated
  loginGate.style.display = 'block';
  dashboard.style.display = 'none';
  if (navAuth) {
    navAuth.innerHTML = `<span class="badge badge-cancelled">🔒 Restricted Access</span>`;
  }
}

function fillCredentials(user, pass) {
  document.getElementById('loginUsername').value = user;
  document.getElementById('loginPassword').value = pass;
}

async function handleLoginSubmit(e) {
  e.preventDefault();
  const submitBtn = document.getElementById('loginSubmitBtn');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Verifying credentials...';

  const user = document.getElementById('loginUsername').value.trim();
  const pass = document.getElementById('loginPassword').value.trim();

  try {
    const res = await api.login(user, pass);
    localStorage.setItem('carepulse_staff_auth', JSON.stringify(res.data));
    showToast(`Welcome back, ${res.data.name}!`, 'success');
    checkAuthState();
  } catch (err) {
    showToast(err.message || 'Invalid username or password', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = '🔐 Sign In to Doctor Portal';
  }
}

async function handleLogout() {
  await api.logout();
  showToast('Logged out of Doctor Portal.', 'info');
  checkAuthState();
}

async function loadAdminData() {
  try {
    const [stats, appts] = await Promise.all([
      api.getStats(),
      api.getAppointments(),
    ]);

    adminAppointments = appts;

    // Render Stats
    document.getElementById('adminDoctorsCount').textContent = stats.totalDoctors;
    document.getElementById('adminUpcomingCount').textContent = stats.upcomingCount;
    document.getElementById('adminCompletedCount').textContent = stats.completedCount;
    document.getElementById('adminTotalCount').textContent = stats.totalAppointments;

    renderAdminTable(adminAppointments);
  } catch (err) {
    console.error(err);
    showToast('Failed to load clinic dashboard data.', 'error');
  }
}

function filterAdminQueue() {
  const query = (document.getElementById('adminSearchInput')?.value || '').trim().toLowerCase();

  const filtered = adminAppointments.filter(a => {
    return !query ||
      a.id.toLowerCase().includes(query) ||
      a.patientName.toLowerCase().includes(query) ||
      a.doctorName.toLowerCase().includes(query) ||
      a.patientPhone.toLowerCase().includes(query) ||
      a.status.toLowerCase().includes(query) ||
      a.specialty.toLowerCase().includes(query);
  });

  renderAdminTable(filtered);
}

function renderAdminTable(list) {
  const tbody = document.getElementById('adminTableBody');
  if (!tbody) return;

  if (list.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
          No patient appointments found matching search filter.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = list.map(a => {
    let badgeClass = 'badge-confirmed';
    if (a.status.toLowerCase() === 'completed') badgeClass = 'badge-completed';
    else if (a.status.toLowerCase() === 'cancelled') badgeClass = 'badge-cancelled';
    else if (a.status.toLowerCase() === 'pending') badgeClass = 'badge-pending';

    const isCompleted = a.status.toLowerCase() === 'completed';
    const isCancelled = a.status.toLowerCase() === 'cancelled';

    return `
      <tr>
        <td style="font-family: monospace; font-weight: 700; color: var(--primary);">${a.id}</td>
        <td>
          <div style="font-weight: 600; color: var(--text-dark);">${a.patientName}</div>
          <div style="font-size: 0.8rem; color: var(--text-muted);">${a.patientAge}y, ${a.patientGender} • ${a.patientPhone}</div>
        </td>
        <td>
          <div style="font-weight: 600;">${a.doctorName}</div>
          <div style="font-size: 0.8rem; color: var(--primary);">${a.specialty}</div>
        </td>
        <td>
          <div>${a.appointmentDate}</div>
          <div style="font-size: 0.8rem; color: var(--text-muted);">${a.timeSlot}</div>
        </td>
        <td>
          <span style="font-size: 0.82rem; background: #f1f5f9; padding: 0.2rem 0.5rem; border-radius: 4px;">${a.type}</span>
        </td>
        <td>
          <span class="badge ${badgeClass}">${a.status}</span>
        </td>
        <td style="max-width: 220px; font-size: 0.82rem; color: #475569; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
          ${a.notes || '<span style="color: #94a3b8;">No notes</span>'}
        </td>
        <td style="text-align: right; white-space: nowrap;">
          <button class="btn btn-secondary btn-sm" onclick="openDoctorNotesModal('${a.id}')">
            📝 Clinical Notes
          </button>
          ${!isCompleted && !isCancelled ? `
            <button class="btn btn-primary btn-sm" onclick="quickComplete('${a.id}')" style="margin-left: 0.3rem;">
              ✓ Complete
            </button>
          ` : ''}
        </td>
      </tr>
    `;
  }).join('');
}

function openDoctorNotesModal(id) {
  const appt = adminAppointments.find(a => a.id === id);
  if (!appt) return;

  document.getElementById('adminApptId').value = appt.id;
  document.getElementById('adminPatientInfo').value = `${appt.patientName} (${appt.patientAge}y, ${appt.patientGender}) - ${appt.patientPhone}`;
  document.getElementById('adminSymptoms').value = appt.symptoms;
  document.getElementById('adminNotesInput').value = appt.notes || '';
  document.getElementById('adminStatusSelect').value = appt.status;

  document.getElementById('doctorNotesModal').classList.add('active');
}

function closeDoctorNotesModal() {
  document.getElementById('doctorNotesModal').classList.remove('active');
}

async function submitDoctorNotes() {
  const id = document.getElementById('adminApptId').value;
  const notes = document.getElementById('adminNotesInput').value.trim();
  const status = document.getElementById('adminStatusSelect').value;

  try {
    await api.updateAppointment(id, { notes, status });
    closeDoctorNotesModal();
    showToast('Clinical record and appointment status saved.', 'success');
    loadAdminData();
  } catch (err) {
    showToast(err.message || 'Failed to update record', 'error');
  }
}

async function quickComplete(id) {
  try {
    await api.updateAppointment(id, { status: 'Completed' });
    showToast(`Appointment ${id} marked as completed.`, 'success');
    loadAdminData();
  } catch (err) {
    showToast(err.message || 'Failed to complete appointment', 'error');
  }
}
