# CarePulse - Healthcare Portal and Appointment Booking System

A modern, full-stack healthcare web application that enables patients to search for doctors, book and manage clinical appointments, explore medical services, and allows hospital doctors/staff to manage appointment queues and clinical records.

---

## Key Features

1. **Doctor Directory & Multi-Criteria Search**:
   - Filter by 8+ medical specialties: Cardiology, Neurology, Pediatrics, Orthopedics, Dermatology, General Medicine, Dentistry, and Oncology.
   - Search by physician name, hospital affiliation, symptoms, or keywords.
   - Doctor profiles featuring board certifications, experience, ratings & reviews, consultation fees, and available time slots.

2. **Interactive Appointment Booking Wizard**:
   - In-Person vs Virtual Video Telehealth consultation modes.
   - Dynamic date picker with minimum-date validation and real-time slot selection.
   - Patient intake form (Name, Age, Gender, Contact, Symptoms/Chief Complaint).
   - Generates unique reference code (`APT-XXXX`) and immediate booking confirmation.

3. **Patient Appointment Management Dashboard** (`appointments.html`):
   - Real-time status filtering (`Confirmed`, `Pending`, `Completed`, `Cancelled`).
   - Reschedule visits with new dates and slots.
   - Cancellation workflow with reason recording.
   - Patient medical and clinical notes editor.
   - Official printable appointment slip generator with verification code.

4. **Doctor & Hospital Staff Operations Portal** (`admin.html`):
   - Live clinical metrics (Active Doctors, Pending Appointments, Completed Sessions, Total Intake).
   - Queue management with one-click status completion.
   - Physician consultation notes and prescription recording.

5. **Zero-Configuration Full-Stack Architecture**:
   - Embedded Java REST server (zero third-party dependencies, standard Java SE 26).
   - File persistence in `data/appointments.json`.
   - Responsive web frontend with clean modern CSS styling.

---

## Directory Structure

```
healthcare-portal/
├── src/
│   └── com/
│       └── healthcare/
│           ├── model/
│           │   ├── Doctor.java               # Doctor entity
│           │   ├── HealthcareService.java    # Clinic service offerings
│           │   └── Appointment.java          # Appointment booking & clinical notes
│           ├── repository/
│           │   └── DataStore.java            # Thread-safe in-memory store + JSON persistence
│           ├── util/
│           │   └── JsonUtil.java             # JSON serialization and parsing
│           ├── api/
│           │   ├── DoctorApiHandler.java     # /api/doctors
│           │   ├── ServiceApiHandler.java    # /api/services
│           │   ├── AppointmentApiHandler.java# /api/appointments
│           │   └── StatsApiHandler.java      # /api/stats
│           ├── server/
│           │   ├── StaticFileHandler.java    # Serves web assets (HTML/CSS/JS)
│           │   └── HealthcareServer.java     # Multi-threaded HTTP server
│           └── Main.java                     # Application entry point
├── web/
│   ├── index.html                            # Patient portal, search & booking
│   ├── appointments.html                     # Appointment management & printable slip
│   ├── admin.html                            # Doctor & staff clinical queue
│   ├── css/
│   │   └── style.css                         # Modern medical theme
│   └── js/
│       ├── api.js                            # REST client & toast notifications
│       ├── app.js                            # Doctor search, filters, booking logic
│       ├── appointments.js                   # Patient dashboard controller
│       └── admin.js                          # Staff queue controller
├── test/
│   └── com/
│       └── healthcare/
│           └── HealthcarePortalTest.java     # Automated unit & integration tests
├── run.bat                                   # One-click start & browser launch
└── README.md
```

---

## How to Run

### Option 1: One-Click Launch
Double-click `run.bat` in the project folder. It will compile the server, start the backend, and automatically open your default browser to `http://localhost:8080`.

### Option 2: Manual Terminal Launch
```powershell
cd C:\Users\abhil\OneDrive\Documents\healthcare-portal

# Compile
javac -d bin src\com\healthcare\model\*.java src\com\healthcare\repository\*.java src\com\healthcare\util\*.java src\com\healthcare\api\*.java src\com\healthcare\server\*.java src\com\healthcare\Main.java test\com\healthcare\HealthcarePortalTest.java

# Run tests
java -cp bin com.healthcare.HealthcarePortalTest

# Start Server
java -cp bin com.healthcare.Main
```
Navigate to:
- **Patient Portal**: `http://localhost:8080`
- **My Appointments**: `http://localhost:8080/appointments.html`
- **Doctor Staff Portal**: `http://localhost:8080/admin.html`
