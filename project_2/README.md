# Medical Record System - Comprehensive Test Suite

## 1. Info & Setup

### Info: Which users are available

These users are created in in crypto/setup_pki.sh:

```bash
# Cardiology Division
create_entity "doctor_alice" "client" "CN=Alice Alison, OU=Cardiology, O=Doctor, C=SE"
create_entity "nurse_bob" "client" "CN=Bob Bobson, OU=Cardiology, O=Nurse, C=SE"
create_entity "nurse_carol" "client" "CN=Carol Carlson, OU=Cardiology, O=Nurse, C=SE"

# Oncology Division
create_entity "doctor_mallory" "client" "CN=Mallory Mallet, OU=Oncology, O=Doctor, C=SE"

# Patients & Gov
create_entity "patient_charlie" "client" "CN=Charlie Charles, OU=None, O=Patient, C=SE"
create_entity "patient_eve" "client" "CN=Eve Evans, OU=None, O=Patient, C=SE"
create_entity "gov_dave" "client" "CN=Dave Davidson, OU=None, O=Government, C=SE"
```

### Start the server

```bash
./start_server_and_pki.sh
```

## 2. Live Test Scenarios

NOTE: Use a different terminal to connect to the server for each scenario.

### Scenario A: Doctor (Alice - Cardiology)

**Login:** `./start_client.sh localhost 9876 doctor_alice password`

```bash
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Blood Pressure 120/80
WRITE rec_charlie_followup patient_charlie;doctor_alice;nurse_carol;Cardiology;Heart rate 75 bpm
READ rec_charlie_bp
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Updated 125/82
DELETE rec_charlie_bp
LIST
```

### Scenario B: Nurse (Bob - Cardiology)

**Login:** `./start_client.sh localhost 9876 nurse_bob password`

```bash
READ rec_charlie_bp
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Nurse assessment
READ rec_charlie_followup
WRITE rec_charlie_followup patient_charlie;doctor_alice;nurse_carol;Cardiology;Bob overwrite attempt
LIST
```

### Scenario C: Doctor (Mallory - Oncology)

**Login:** `./start_client.sh localhost 9876 doctor_mallory password`

```bash
READ rec_charlie_bp
WRITE rec_eve_oncology patient_eve;doctor_mallory;nurse_carol;Oncology;Initial Exam
READ rec_eve_oncology
WRITE rec_charlie_bp patient_charlie;doctor_mallory;nurse_carol;Oncology;Unauthorized update
```

### Scenario D: Patient (Charlie)

**Login:** `./start_client.sh localhost 9876 patient_charlie password`

```bash
READ rec_charlie_bp
READ rec_eve_oncology
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Patient overwrite attempt
LIST
```

### Scenario E: Government Agency (Dave)

**Login:** `./start_client.sh localhost 9876 gov_dave password`

```bash
READ rec_charlie_bp
READ rec_eve_oncology
LIST
DELETE rec_charlie_bp
DELETE rec_eve_oncology
WRITE rec_charlie_bp patient_charlie;doctor_alice;nurse_bob;Cardiology;Agency attempt
```

## 3. Audit Log Inspection

Check in localDB/audit_log.txt
