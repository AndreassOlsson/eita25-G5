# Technical Manual: Medical Records Security Project

## 1. Project Overview and Scope

- The purpose of this project is to solve a "real case" problem requiring security mechanisms.
- The system is a medical records database managed by a server and accessed remotely over an open network.
- The implementation scope requires a small example consisting of a few patients, nurses, doctors, and one government agency.
- A full database system is not strictly required; records can be stored in any convenient manner.
- A graphical user interface (GUI) is entirely optional.
- The focus is on implementing confidentiality and a simple access control scheme.

## 2. System Architecture

- The system must be developed using the Java programming language.
- Java version 11 or above is recommended.
- The architecture consists of a public client program and a server application.
- The server stores all medical records and responds to client requests.
- The server is assumed to be in a physically protected room with proper backups.

## 3. Security and Network Requirements

- Communication between the client and server must be encrypted.
- The communication must be established using the TLS network standard.
- Strong, two-factor authentication is required for individuals accessing the system.
- Two-factor authentication can be achieved using certificates, requiring a keystore and the keystore password.
- All certificates must be signed by a Certificate Authority (CA).
- The CA's private key must be used to sign all other certificates.
- The CA certificate must be installed in a truststore on both the client and the server.
- The actual medical records do not need to be stored encrypted on the server.

## 4. Audit Logging Requirements

- The system must maintain an audit log.
- Every action and access to a medical record must be properly logged.
- The audit log must record information detailing who performed the action, what the action was, and when it occurred.

## 5. Actors and Access Control Rules

- Only individual access is permitted; group access is not allowed.
- Each medical record contains the associated treating nurse's name, treating doctor's name, the hospital division, and medical data.
- Doctors and nurses are associated with a specific hospital division.

### Patient Access

- A patient can read their own list of records.

### Nurse Access

- A nurse can read all records associated with them.
- A nurse can write to all records associated with them.
- A nurse can read all records associated with their hospital division.

### Doctor Access

- A doctor can read all records associated with them.
- A doctor can write to all records associated with them.
- A doctor can read all records associated with their hospital division.
- A doctor can create new records for a patient they are treating.
- When creating a record, the doctor must associate a nurse with that specific record.

### Government Agency Access

- A government agency is allowed to read all types of records.
- A government agency is allowed to delete all types of records.
