/**
 * Client controller for appointments.html (Patient Appointment Management)
 */
let allAppointments = [];
let currentStatusFilter = 'All';

document.addEventListener('DOMContentLoaded', () => {
  loadAppointments();
  setupFilterPills();
});

async function loadAppointments() {
  try {
    allAppointments = await api.getAppointments();
    updateStatsRibbon();
    filterAppointments();
  } catch (err) {
    console.error(err);
    showToast('Failed to load appointments.', 'error');
  }
}

function updateStatsRibbon() {
  const total = allAppointments.length;
  let upcoming = 0;
  let completed = 0;
  let cancelled = 0;

  allAppointments.forEach(a => {
    const s = a.status.toLowerCase();
    if (s === 'confirmed' || s === 'pending') upcoming++;
    else if (s === 'completed') completed++;
    else if (s === 'cancelled') cancelled++;
  });

  document.getElementById('statTotal').textContent = total;
  document.getElementById('statUpcoming').textContent = upcoming;
  document.getElementById('statCompleted').textContent = completed;
  document.getElementById('statCancelled').textContent = cancelled;
}

function setupFilterPills() {
  const pills = document.querySelectorAll('#statusFilterPills .filter-tag');
  pills.forEach(pill => {
    pill.addEventListener('click', () => {
      pills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      currentStatusFilter = pill.dataset.status;
      filterAppointments();
    });
  });
}

function filterAppointments() {
  const query = (document.getElementById('appointmentSearch')?.value || '').trim().toLowerCase();

  const filtered = allAppointments.filter(a => {
    const matchesStatus = (currentStatusFilter === 'All') ||
      (a.status.toLowerCase() === currentStatusFilter.toLowerCase());

    const matchesQuery = !query ||
      a.id.toLowerCase().includes(query) ||
      a.patientName.toLowerCase().includes(query) ||
      a.doctorName.toLowerCase().includes(query) ||
      a.patientPhone.toLowerCase().includes(query) ||
      a.specialty.toLowerCase().includes(query);

    return matchesStatus && matchesQuery;
  });

  renderAppointments(filtered);
}

function renderAppointments(list) {
  const container = document.getElementById('appointmentsList');
  if (!container) return;

  if (list.length === 0) {
    container.innerHTML = `
      <div style="background: white; border: 1px dashed var(--border); border-radius: 12px; padding: 3rem; text-align: center;">
        <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">📅</div>
        <h3 style="color: var(--text-dark); margin-bottom: 0.4rem;">No appointments found</h3>
        <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 1.2rem;">No bookings match the selected status filter or search query.</p>
        <a href="index.html" class="btn btn-primary">Schedule New Appointment</a>
      </div>
    `;
    return;
  }

  container.innerHTML = list.map(a => {
    const isCancelled = a.status.toLowerCase() === 'cancelled';
    const isCompleted = a.status.toLowerCase() === 'completed';

    let badgeClass = 'badge-confirmed';
    if (a.status.toLowerCase() === 'completed') badgeClass = 'badge-completed';
    else if (a.status.toLowerCase() === 'cancelled') badgeClass = 'badge-cancelled';
    else if (a.status.toLowerCase() === 'pending') badgeClass = 'badge-pending';

    return `
      <div class="appt-card" id="card-${a.id}">
        <div>
          <div class="appt-header-row">
            <span class="appt-id-badge">${a.id}</span>
            <span class="badge ${badgeClass}">${a.status}</span>
            <span class="badge" style="background: #f1f5f9; color: #475569;">${a.type}</span>
            <span style="font-size: 0.8rem; color: var(--text-muted); margin-left: auto;">Booked: ${a.createdAt}</span>
          </div>

          <h3 style="font-size: 1.25rem; font-weight: 700; color: var(--text-dark); margin-bottom: 0.3rem;">
            ${a.doctorName} <span style="font-size: 0.95rem; color: var(--text-muted); font-weight: 500;">(${a.specialty})</span>
          </h3>

          <div style="display: flex; gap: 1.5rem; flex-wrap: wrap; margin-bottom: 0.8rem; font-size: 0.9rem;">
            <span>🗓 <strong>${a.appointmentDate}</strong></span>
            <span>⏰ <strong>${a.timeSlot}</strong></span>
            <span>👤 Patient: <strong>${a.patientName}</strong> (${a.patientAge}y, ${a.patientGender})</span>
            <span>📞 ${a.patientPhone}</span>
          </div>

          <div style="font-size: 0.88rem; color: #334155; margin-bottom: 0.5rem;">
            <strong>Symptoms / Reason:</strong> ${a.symptoms}
          </div>

          <div class="notes-box">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.2rem;">
              <strong style="color: var(--text-dark);">Clinical & Visit Notes:</strong>
              <button class="btn btn-sm btn-secondary" onclick="openNotesModal('${a.id}')">✏️ Edit Notes</button>
            </div>
            <div>${a.notes || 'No special notes recorded.'}</div>
          </div>
        </div>

        <div class="appt-actions-col">
          <button class="btn btn-outline btn-sm" onclick="openSlipModal('${a.id}')">
            🖨 Print Visit Slip
          </button>
          
          ${!isCancelled && !isCompleted ? `
            <button class="btn btn-secondary btn-sm" onclick="openRescheduleModal('${a.id}')">
              🔄 Reschedule Visit
            </button>
            <button class="btn btn-danger btn-sm" onclick="confirmCancelAppointment('${a.id}')">
              ✕ Cancel Booking
            </button>
          ` : ''}

          ${isCompleted ? `
            <div style="font-size: 0.8rem; color: var(--primary); text-align: center; font-weight: 600;">
              ✓ Consultation Concluded
            </div>
          ` : ''}
          
          ${isCancelled ? `
            <div style="font-size: 0.8rem; color: var(--danger); text-align: center; font-weight: 600;">
              ✕ Booking Cancelled
            </div>
          ` : ''}
        </div>
      </div>
    `;
  }).join('');
}

// Reschedule Workflow
function openRescheduleModal(id) {
  const appt = allAppointments.find(a => a.id === id);
  if (!appt) return;

  document.getElementById('rescheduleApptId').value = appt.id;
  document.getElementById('rescheduleDoctorName').value = `${appt.doctorName} (${appt.specialty})`;
  
  const dateInput = document.getElementById('rescheduleDate');
  dateInput.min = new Date().toISOString().split('T')[0];
  dateInput.value = appt.appointmentDate;

  document.getElementById('rescheduleSlotSelect').value = appt.timeSlot;
  document.getElementById('rescheduleReason').value = '';

  document.getElementById('rescheduleModal').classList.add('active');
}

function closeRescheduleModal() {
  document.getElementById('rescheduleModal').classList.remove('active');
}

async function submitReschedule() {
  const id = document.getElementById('rescheduleApptId').value;
  const date = document.getElementById('rescheduleDate').value;
  const slot = document.getElementById('rescheduleSlotSelect').value;
  const reason = document.getElementById('rescheduleReason').value.trim();

  if (!date || !slot) {
    showToast('Please select both date and time slot.', 'error');
    return;
  }

  try {
    const existing = allAppointments.find(a => a.id === id);
    const updatedNotes = (existing?.notes || '') + (reason ? ` | Rescheduled: ${reason}` : ' | Rescheduled by patient');

    await api.updateAppointment(id, {
      appointmentDate: date,
      timeSlot: slot,
      status: 'Confirmed',
      notes: updatedNotes,
    });

    closeRescheduleModal();
    showToast('Appointment rescheduled successfully!', 'success');
    loadAppointments();
  } catch (err) {
    showToast(err.message || 'Failed to reschedule', 'error');
  }
}

// Cancel Workflow
async function confirmCancelAppointment(id) {
  const reason = prompt('Please enter cancellation reason (optional):');
  if (reason === null) return; // User cancelled prompt

  try {
    await api.cancelAppointment(id, reason);
    showToast('Appointment cancelled.', 'success');
    loadAppointments();
  } catch (err) {
    showToast(err.message || 'Failed to cancel', 'error');
  }
}

// Notes Workflow
function openNotesModal(id) {
  const appt = allAppointments.find(a => a.id === id);
  if (!appt) return;

  document.getElementById('notesApptId').value = appt.id;
  document.getElementById('notesContent').value = appt.notes || '';
  document.getElementById('notesModal').classList.add('active');
}

function closeNotesModal() {
  document.getElementById('notesModal').classList.remove('active');
}

async function submitNotes() {
  const id = document.getElementById('notesApptId').value;
  const notes = document.getElementById('notesContent').value.trim();

  try {
    await api.updateAppointment(id, { notes });
    closeNotesModal();
    showToast('Notes updated successfully.', 'success');
    loadAppointments();
  } catch (err) {
    showToast(err.message || 'Failed to update notes', 'error');
  }
}

// Printable Slip Generator
function openSlipModal(id) {
  const a = allAppointments.find(item => item.id === id);
  if (!a) return;

  const content = document.getElementById('slipContent');
  content.innerHTML = `
    <div id="printable-slip" style="border: 2px solid var(--primary); padding: 1.5rem; border-radius: 12px; background: white; font-family: sans-serif;">
      <div style="display: flex; justify-content: space-between; border-bottom: 2px solid var(--primary); padding-bottom: 1rem; margin-bottom: 1.2rem;">
        <div>
          <h2 style="color: var(--primary); margin-bottom: 0.2rem;">CarePulse Chennai Multispecialty Hospital Network</h2>
          <div style="font-size: 0.85rem; color: #64748b;">Greams Road • Alwarpet • Manapakkam • Vadapalani • 24/7 Helpline: +91 44 2829 0200 / 108</div>
          <div style="font-size: 0.85rem; color: #64748b;">Official Patient Clinical Appointment Verification Slip</div>
        </div>
        <div style="text-align: right;">
          <div style="font-size: 1.2rem; font-weight: 800; color: var(--primary);">${a.id}</div>
          <div style="font-size: 0.8rem; color: #64748b;">Status: <strong>${a.status.toUpperCase()}</strong></div>
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.2rem; font-size: 0.9rem;">
        <div>
          <h4 style="color: #475569; margin-bottom: 0.3rem;">PATIENT INFORMATION</h4>
          <div><strong>Name:</strong> ${a.patientName}</div>
          <div><strong>Age / Gender:</strong> ${a.patientAge} years, ${a.patientGender}</div>
          <div><strong>Contact:</strong> ${a.patientPhone}</div>
          <div><strong>Email:</strong> ${a.patientEmail}</div>
        </div>
        <div>
          <h4 style="color: #475569; margin-bottom: 0.3rem;">APPOINTMENT DETAILS</h4>
          <div><strong>Doctor:</strong> ${a.doctorName}</div>
          <div><strong>Department:</strong> ${a.specialty}</div>
          <div><strong>Date:</strong> ${a.appointmentDate}</div>
          <div><strong>Time:</strong> ${a.timeSlot}</div>
          <div><strong>Mode:</strong> ${a.type}</div>
        </div>
      </div>

      <div style="background: #f8fafc; padding: 0.8rem; border-radius: 8px; font-size: 0.85rem; margin-bottom: 1rem;">
        <strong>Reported Symptoms / Reason:</strong> ${a.symptoms}
      </div>

      <div style="background: #f8fafc; padding: 0.8rem; border-radius: 8px; font-size: 0.85rem; margin-bottom: 1.5rem;">
        <strong>Visit Instructions:</strong> Please present this slip at the reception desk 15 minutes before your time slot with valid photo identification.
      </div>

      <div style="text-align: center; border-top: 1px dashed #cbd5e1; padding-top: 0.8rem; font-size: 0.8rem; color: #94a3b8;">
        Generated automatically by CarePulse Health Portal • Verification Code: [${a.id}-SECURE-VERIFIED]
      </div>
    </div>
  `;

  document.getElementById('slipModal').classList.add('active');
}

function closeSlipModal() {
  document.getElementById('slipModal').classList.remove('active');
}
