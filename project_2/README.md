# Medical Record System - Quick Test Guide

Use this guide to quickly test the authentication and access control logic.

## 1. Setup & Start Server

Run this once in a separate terminal to generate keys and start the server.

```bash
./start_server_and_pki.sh
```

---

## 2. Test Scenarios

Open a **new terminal** for each client session below.

### Doctor (Alice)

**Role:** Doctor | **Division:** Cardiology

**1. Start Client:**

```bash
./start_client.sh localhost 9876 doctor_alice password
```

**2. Test Commands (Type these in the client):**

```text
# Create a new record for patient_charlie associated with nurse_bob
WRITE rec1 patient_charlie;doctor_alice;nurse_bob;Cardiology;Blood Pressure 120/80

# Read the record we just created
READ rec1

# Modify the record (Doctor is associated)
WRITE rec1 patient_charlie;doctor_alice;nurse_bob;Cardiology;Blood Pressure 130/85
```

---

### Nurse (Bob)

**Role:** Nurse | **Division:** Cardiology

**1. Start Client:**

```bash
./start_client.sh localhost 9876 nurse_bob password
```

**2. Test Commands:**

```text
# Read a record in same division (Cardiology) - Should SUCCESS
READ rec1

# Try to delete a record - Should FAIL (Access Denied)
DELETE rec1

# Write to a record (Nurse is associated) - Should SUCCESS
WRITE rec1 patient_charlie;doctor_alice;nurse_bob;Cardiology;Patient resting comfortably
```

---

### Patient (Charlie)

**Role:** Patient

**1. Start Client:**

```bash
./start_client.sh localhost 9876 patient_charlie password
```

**2. Test Commands:**

```text
# Read own record - Should SUCCESS
READ rec1

# Try to write/modify record - Should FAIL
WRITE rec1 patient_charlie;doctor_alice;nurse_bob;Cardiology;Hacking into mainframe...
```

---

### Government (Dave)

**Role:** Government Agency

**1. Start Client:**

```bash
./start_client.sh localhost 9876 gov_dave password
```

**2. Test Commands:**

```text
# Read any record - Should SUCCESS
READ rec1

# Delete the record - Should SUCCESS
DELETE rec1

# Verify deletion (Read again) - Should Fail (Not Found)
READ rec1
```
