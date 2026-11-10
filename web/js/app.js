/**
 * Client controller for index.html (Doctor search, filters, booking modal)
 */
let allDoctors = [];
let allServices = [];
let selectedDoctor = null;
let currentSpecialtyFilter = 'All';

document.addEventListener('DOMContentLoaded', () => {
  init();
});

async function init() {
  try {
    const [doctors, services, stats] = await Promise.all([
      api.getDoctors(),
      api.getServices(),
      api.getStats().catch(() => null),
    ]);

    allDoctors = doctors;
    allServices = services;

    renderDoctors(allDoctors);
    renderServices(allServices);
    setupDoctorModalSelect(allDoctors);

    if (stats) {
      const elDocs = document.getElementById('statDoctors');
      const elBkgs = document.getElementById('statBookings');
      if (elDocs) elDocs.textContent = `${stats.totalDoctors}+`;
      if (elBkgs) elBkgs.textContent = `${stats.totalAppointments + 2500}+`;
    }

    // Min date = today
    const dateInput = document.getElementById('modalDateInput');
    if (dateInput) {
      const today = new Date().toISOString().split('T')[0];
      dateInput.min = today;
      dateInput.value = today;
    }

    setupEventListeners();
  } catch (err) {
    console.error(err);
    showToast('Failed to load portal data. Check server connection.', 'error');
  }
}

function setupEventListeners() {
  // Filter tags
  const tags = document.querySelectorAll('.filter-tag');
  tags.forEach(tag => {
    tag.addEventListener('click', () => {
      tags.forEach(t => t.classList.remove('active'));
      tag.classList.add('active');
      currentSpecialtyFilter = tag.dataset.specialty;
      applyFilters();
    });
  });

  // Search input live enter
  const searchInput = document.getElementById('searchInput');
  if (searchInput) {
    searchInput.addEventListener('input', () => applyFilters());
  }

  const specSelect = document.getElementById('specialtySelect');
  if (specSelect) {
    specSelect.addEventListener('change', () => {
      currentSpecialtyFilter = specSelect.value;
      // Sync tag
      document.querySelectorAll('.filter-tag').forEach(t => {
        t.classList.toggle('active', t.dataset.specialty === currentSpecialtyFilter);
      });
      applyFilters();
    });
  }
}

function applyFilters() {
  const query = (document.getElementById('searchInput')?.value || '').trim().toLowerCase();

  const filtered = allDoctors.filter(d => {
    const matchesSpec = (currentSpecialtyFilter === 'All') ||
      (d.specialty.toLowerCase() === currentSpecialtyFilter.toLowerCase());

    const matchesQuery = !query ||
      d.name.toLowerCase().includes(query) ||
      d.specialty.toLowerCase().includes(query) ||
      d.hospital.toLowerCase().includes(query) ||
      d.bio.toLowerCase().includes(query);

    return matchesSpec && matchesQuery;
  });

  renderDoctors(filtered);
}

function renderDoctors(doctors) {
  const grid = document.getElementById('doctorsGrid');
  if (!grid) return;

  if (doctors.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1/-1; text-align: center; padding: 3rem; background: white; border-radius: 12px; border: 1px dashed var(--border);">
        <h3 style="color: var(--text-dark); margin-bottom: 0.5rem;">No doctors found matching criteria</h3>
        <p style="color: var(--text-muted); font-size: 0.95rem;">Try choosing "All Specialties" or adjusting your search term.</p>
      </div>
    `;
    return;
  }

  grid.innerHTML = doctors.map(d => `
    <div class="doctor-card">
      <div class="doctor-card-top">
        <img src="${d.imageUrl}" alt="${d.name}" class="doctor-avatar" onerror="this.src='https://images.unsplash.com/photo-1622253692010-333f2da6031d?auto=format&fit=crop&q=80&w=400'">
        <div class="doctor-info">
          <span class="doctor-specialty-badge">${d.specialty}</span>
          <h3 class="doctor-name">${d.name}</h3>
          <div class="doctor-qualifications">${d.qualifications}</div>
          <div class="doctor-meta">
            <span class="rating">★ ${d.rating.toFixed(1)} <span style="color: var(--text-muted); font-weight: 400;">(${d.reviewsCount})</span></span>
            <span>•</span>
            <span class="experience">${d.experienceYears} yrs exp</span>
          </div>
        </div>
      </div>

      <div class="doctor-card-body">
        <p class="doctor-bio">${d.bio}</p>
        <div class="doctor-hospital">📍 ${d.hospital}</div>
        
        <div>
          <div style="font-size: 0.75rem; font-weight: 600; color: var(--text-muted); margin-bottom: 0.3rem;">AVAILABLE SLOTS:</div>
          <div class="doctor-slots">
            ${d.availableSlots.map(s => `<span class="slot-tag">${s}</span>`).join('')}
          </div>
        </div>
      </div>

      <div class="doctor-card-footer">
        <div>
          <span class="consult-fee">Consultation Fee</span>
          <div class="fee-amount">₹${d.consultationFee.toFixed(0)}</div>
        </div>
        <button class="btn btn-primary" onclick="openBookingModalForDoctor('${d.id}')">
          Book Visit
        </button>
      </div>
    </div>
  `).join('');
}

function renderServices(services) {
  const grid = document.getElementById('servicesGrid');
  if (!grid) return;

  grid.innerHTML = services.map(s => `
    <div class="service-card">
      <div>
        <div class="service-icon-box">🩺</div>
        <span class="doctor-specialty-badge" style="background: var(--secondary-light); color: var(--secondary);">${s.category}</span>
        <h3 class="service-title">${s.name}</h3>
        <p class="service-desc">${s.description}</p>
      </div>
      <div>
        <div class="service-meta-row">
          <span>⏱ Duration: <strong>${s.duration}</strong></span>
          <span style="font-weight: 800; font-size: 1.15rem; color: var(--primary);">₹${s.cost.toFixed(0)}</span>
        </div>
        <div class="service-prep">💡 ${s.preparation}</div>
      </div>
    </div>
  `).join('');
}

function setupDoctorModalSelect(doctors) {
  const select = document.getElementById('modalDoctorSelect');
  if (!select) return;

  select.innerHTML = doctors.map(d => `
    <option value="${d.id}">${d.name} — ${d.specialty} (₹${d.consultationFee.toFixed(0)})</option>
  `).join('');

  if (doctors.length > 0) {
    selectDoctorInModal(doctors[0].id);
  }
}

function openBookingModal() {
  const modal = document.getElementById('bookingModal');
  if (!modal) return;
  modal.classList.add('active');

  const select = document.getElementById('modalDoctorSelect');
  if (select && select.value) {
    selectDoctorInModal(select.value);
  }
}

function openBookingModalForDoctor(doctorId) {
  openBookingModal();
  const select = document.getElementById('modalDoctorSelect');
  if (select) {
    select.value = doctorId;
    selectDoctorInModal(doctorId);
  }
}

function closeBookingModal() {
  const modal = document.getElementById('bookingModal');
  if (modal) modal.classList.remove('active');
}

function onDoctorSelectChange() {
  const select = document.getElementById('modalDoctorSelect');
  if (select) {
    selectDoctorInModal(select.value);
  }
}

function selectDoctorInModal(doctorId) {
  const doctor = allDoctors.find(d => d.id === doctorId);
  if (!doctor) return;
  selectedDoctor = doctor;

  const container = document.getElementById('modalSlotsContainer');
  const selectedSlotInput = document.getElementById('modalSelectedSlot');
  selectedSlotInput.value = doctor.availableSlots[0] || '';

  if (container) {
    container.innerHTML = doctor.availableSlots.map((slot, idx) => `
      <div class="slot-btn ${idx === 0 ? 'selected' : ''}" onclick="selectSlot('${slot}', this)">
        ${slot}
      </div>
    `).join('');
  }
}

function selectSlot(slot, element) {
  document.querySelectorAll('.slot-btn').forEach(btn => btn.classList.remove('selected'));
  element.classList.add('selected');
  document.getElementById('modalSelectedSlot').value = slot;
}

async function handleBookingSubmit(e) {
  e.preventDefault();
  const submitBtn = document.getElementById('modalSubmitBtn');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Processing Booking...';

  const doctorId = document.getElementById('modalDoctorSelect').value;
  const consultType = document.querySelector('input[name="modalConsultType"]:checked').value;
  const appointmentDate = document.getElementById('modalDateInput').value;
  const timeSlot = document.getElementById('modalSelectedSlot').value;
  const patientName = document.getElementById('modalPatientName').value.trim();
  const patientPhone = document.getElementById('modalPatientPhone').value.trim();
  const patientEmail = document.getElementById('modalPatientEmail').value.trim();
  const patientAge = document.getElementById('modalPatientAge').value;
  const patientGender = document.getElementById('modalPatientGender').value;
  const symptoms = document.getElementById('modalSymptoms').value.trim();
  const notes = document.getElementById('modalNotes').value.trim();

  if (!timeSlot) {
    showToast('Please select a time slot.', 'error');
    submitBtn.disabled = false;
    submitBtn.textContent = 'Confirm Appointment';
    return;
  }

  try {
    const res = await api.createAppointment({
      doctorId,
      type: consultType,
      appointmentDate,
      timeSlot,
      patientName,
      patientPhone,
      patientEmail,
      patientAge,
      patientGender,
      symptoms,
      notes,
    });

    const newAppt = res.data;

    // Render Success View inside Modal
    const modalBody = document.getElementById('bookingModalBody');
    modalBody.innerHTML = `
      <div style="text-align: center; padding: 1.5rem 1rem;">
        <div style="width: 64px; height: 64px; background: var(--success-light); color: var(--success); font-size: 2rem; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.2rem;">
          ✓
        </div>
        <h3 style="font-size: 1.4rem; color: var(--text-dark); margin-bottom: 0.4rem;">Appointment Confirmed!</h3>
        <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 1.2rem;">
          Your booking has been scheduled. A confirmation has been registered in the hospital system.
        </p>
        
        <div style="background: #f8fafc; border: 1px solid var(--border); border-radius: 10px; padding: 1.2rem; text-align: left; max-width: 440px; margin: 0 auto 1.5rem;">
          <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
            <span style="color: var(--text-muted); font-size: 0.85rem;">Booking Reference:</span>
            <strong style="color: var(--primary); font-size: 0.95rem;">${newAppt.id}</strong>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
            <span style="color: var(--text-muted); font-size: 0.85rem;">Doctor:</span>
            <strong style="font-size: 0.9rem;">${newAppt.doctorName} (${newAppt.specialty})</strong>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
            <span style="color: var(--text-muted); font-size: 0.85rem;">Date & Time:</span>
            <strong style="font-size: 0.9rem;">${newAppt.appointmentDate} at ${newAppt.timeSlot}</strong>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
            <span style="color: var(--text-muted); font-size: 0.85rem;">Mode:</span>
            <span class="badge badge-confirmed">${newAppt.type}</span>
          </div>
          <div style="display: flex; justify-content: space-between;">
            <span style="color: var(--text-muted); font-size: 0.85rem;">Patient:</span>
            <strong style="font-size: 0.9rem;">${newAppt.patientName} (${newAppt.patientPhone})</strong>
          </div>
        </div>

        <div style="display: flex; gap: 0.8rem; justify-content: center;">
          <button class="btn btn-secondary" onclick="window.location.reload()">Book Another</button>
          <a href="appointments.html" class="btn btn-primary">Go to My Appointments →</a>
        </div>
      </div>
    `;

    showToast('Appointment successfully booked!', 'success');
  } catch (err) {
    console.error(err);
    showToast(err.message || 'Error booking appointment', 'error');
    submitBtn.disabled = false;
    submitBtn.textContent = 'Confirm Appointment';
  }
}
