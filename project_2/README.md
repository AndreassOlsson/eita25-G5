# Medical Record System - Comprehensive Test Suite

This test suite is designed to verify the implementation of the access control rules and the audit logging requirements as specified in the project description.

## 1. Prerequisites & Setup

Ensure all entities are created with the correct naming convention: `CN=Name, OU=Division, O=Role`.

**Entities:**

- **Cardiology Division:** Alice (Doctor), Bob (Nurse), Carol (Nurse)
- **Oncology Division:** Mallory (Doctor)
- **Patients:** Charlie, Eve
- **Government Agency:** Dave

**Start the server:**

```bash
./start_server_and_pki.sh
```

Then in a separate terminal, start a client session with the appropriate user.

---

## 2. Live Test Scenarios

### Scenario A: Doctor (Alice - Cardiology)

**Login:** `./start_client.sh localhost 9876 doctor_alice password`

**Goals:** Create new Cardiology records, read them, and verify that Alice can overwrite her own data but cannot delete.

**Commands (copy/paste block):**

```
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Blood Pressure 120/80
WRITE rec_charlie_followup patient_charlie;doctor_alice;nurse_carol;Cardiology;Heart rate 75 bpm
READ rec_charlie_bp
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Updated 125/82
DELETE rec_charlie_bp
LIST
```

**Expected:**

- Two CREATE operations succeed.
- READ returns the serialized record.
- Overwrite succeeds because Alice is the treating doctor.
- DELETE returns `DENIED` (doctors cannot delete).
- LIST shows both `rec_charlie_bp` and `rec_charlie_followup` while they exist.

---

### Scenario B: Nurse (Bob - Cardiology)

**Login:** `./start_client.sh localhost 9876 nurse_bob password`

**Goals:** Confirm Bob can write only to his assigned records while still seeing other Cardiology records.

**Commands:**

```
READ rec_charlie_bp
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Nurse assessment
READ rec_charlie_followup
WRITE rec_charlie_followup patient_charlie;doctor_alice;nurse_carol;Cardiology;Bob overwrite attempt
LIST
```

**Expected:**

- READ/WRITE on `rec_charlie_bp` return `OK`.
- READ on `rec_charlie_followup` returns `OK` (same division).
- WRITE on `rec_charlie_followup` is `DENIED` because Bob is not the assigned nurse.
- LIST output includes Cardiology records but not Oncology records.

---

### Scenario C: Doctor (Mallory - Oncology)

**Login:** `./start_client.sh localhost 9876 doctor_mallory password`

**Goals:** Validate strong confidentiality between divisions and Mallory's ability to manage Oncology records.

**Commands:**

```
READ rec_charlie_bp
WRITE rec_eve_oncology patient_eve;doctor_mallory;nurse_carol;Oncology;Initial Exam
READ rec_eve_oncology
WRITE rec_charlie_bp patient_charlie;doctor_mallory;nurse_carol;Oncology;Unauthorized update
```

**Expected:**

- READ on Cardiology record returns `DENIED`.
- CREATE + READ for `rec_eve_oncology` succeed.
- Attempting to overwrite `rec_charlie_bp` fails because Mallory is not the assigned doctor.

---

### Scenario D: Patient (Charlie)

**Login:** `./start_client.sh localhost 9876 patient_charlie password`

**Goals:** Ensure patients can only read their own data and that LIST filtering works for them.

**Commands:**

```
READ rec_charlie_bp
READ rec_eve_oncology
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Patient overwrite attempt
LIST
```

**Expected:**

- READ own record succeeds.
- READ Eve's record is `DENIED`.
- WRITE is `DENIED` (patients have no write permissions).
- LIST returns only Charlie's record IDs.

---

### Scenario E: Government Agency (Dave)

**Login:** `./start_client.sh localhost 9876 gov_dave password`

**Goals:** Prove Dave can read and delete everything but cannot write.

**Commands:**

```
READ rec_charlie_bp
READ rec_eve_oncology
LIST
DELETE rec_charlie_bp
DELETE rec_eve_oncology
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Agency attempt
```

**Expected:**

- READ/LIST always succeed regardless of division.
- DELETE succeeds for both records.
- WRITE returns `DENIED` (government users have no write privilege).

---

## 3. Audit Log Inspection

Check the server-side logs to confirm every action above was recorded with:

- **Who:** User identity from certificate/common name
- **What:** Action (READ/WRITE/DELETE) and target record
- **When:** Timestamp of the request
