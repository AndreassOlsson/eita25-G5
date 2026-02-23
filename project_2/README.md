# Medical Record System (Project 2)

A secure, TLS-encrypted client-server application for managing medical records with role-based access control.

## Quick Start

### 1. Setup PKI and Start Server

This script generates all necessary keys/certificates and starts the server.

```bash
./start_server_and_pki.sh
```

_The server will listen on port 9876._
_(Default password for all keystores is `password`)_

### 2. Start a Client

Open a new terminal for each client. Usage:

```bash
./start_client.sh <host> <port> <user_id> <password>
```

**Users (created by setup script):**

- `doctor_alice` (Doctor, Cardiology)
- `nurse_bob` (Nurse, Cardiology)
- `patient_charlie` (Patient)
- `gov_dave` (Government Agency)

#### Example: Login as Doctor Alice

```bash
./start_client.sh localhost 9876 doctor_alice password
```

---

## Examples

Once connected, try these commands:

### Valid Scenarios (Things that should work)

**1. Doctor creates a record:**
_(As `doctor_alice`)_

```text
WRITE rec1 patient_charlie;doctor_alice;nurse_bob;Cardiology;Blood Pressure 120/80
```

_Server Response: `OK Record written`_

**2. Nurse reads the record (Same Division/Associated):**
_(As `nurse_bob`)_

```text
READ rec1
```

_Server Response: `OK rec1;patient_charlie;doctor_alice;nurse_bob;Cardiology;Blood Pressure 120/80`_

**3. Patient reads their own record:**
_(As `patient_charlie`)_

```text
READ rec1
```

_Server Response: `OK ...`_

**4. Government deletes a record:**
_(As `gov_dave`)_

```text
DELETE rec1
```

_Server Response: `OK Record deleted`_

### Invalid Scenarios (Things that should fail)

**1. Nurse tries to delete a record:**
_(As `nurse_bob`)_

```text
DELETE rec1
```

_Server Response: `DENIED User nurse_bob not allowed to delete records.`_

**2. Patient tries to read someone else's record:**
_(As `patient_charlie` trying to read a record where they are not the patient)_

```text
READ rec2
```

_Server Response: `DENIED Access denied for user: patient_charlie`_

**3. Writing with invalid format:**

```text
WRITE rec1 bad_data
```

_Server Response: `ERROR Invalid data format. Expected: patientId;doctorId;nurseId;division;data`_

---
