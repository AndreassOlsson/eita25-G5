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

# Technical Manual: Project Deliverables, Documentation, and Processes

## 1. Project Deliverables and Submission Format

- **Code and Configuration:** Can be compressed into an archive format such as `.tar.gz`, `.rar`, or similar.
- **Documentation Bundle:** Must be a single, uncompressed PDF file.
- **Presentation File:** Must be an uncompressed PDF submitted to Canvas the day before the presentation by 23:59.
- All submissions are handled electronically through Canvas.
- A designated "group manager" is responsible for booking presentation times, distributing/returning reports for review, and submitting the final files.

## 2. Documentation Requirements (The Report Bundle)

- **Language:** All written documentation must be in English.
- **Formatting:** Use single spacing and a normal-sized font with serifs. Do not use large fonts or spacing to pad the length. References must be included for all external resources, code, or arguments.
- **Length:** Approximately 11-12 A4 pages.
- **Required Sections & Order:**
  1. **Front page:** Names of group members (1 page).
  2. **High-level architectural overview:** (2 pages).
  3. **Ethical discussion:** (1 page).
  4. **Security evaluation:** (2-3 pages).
  5. **Peer-reviews of your report:** Include the reviews you received (2 pages).
  6. **Improvement sheet:** Summary of actions taken based on reviews (max 1 page).
  7. **Signed functionality review form:** (1 page).
  8. **Signed contribution statement form:** Outlining individual contributions (1 page).

## 3. Specific Report Section Details

### 3.1 High-Level Architectural Overview

- Must explain the program's structure at a high level without insignificant details.
- Must include an image (half page) illustrating the structure.
- The diagram and text must show how keystores and truststores are used, certificate hierarchy, main components, actors, information flow, and the access control scheme.
- Do not include a user guide.
- Target audience: A peer student who has not taken this specific security course.

### 3.2 Ethical Discussion

- Formulate (do not implement) an access control scheme suitable for a live production hospital environment that balances confidentiality, integrity, and availability.
- Compare this production scheme to your implemented scheme (which focuses mainly on confidentiality) and list the pros and cons of both.
- Discuss ethical considerations weighing factors like economics, patient health, availability, privacy, and vulnerabilities.
- Analyze these factors from the perspectives of three actors: hospital administration, engineer/security expert, and hospital staff.

### 3.3 Security Evaluation

- List all conceivable attack types and major security issues.
- Explain clearly how your system is protected against them, or motivate why no protection is needed.
- Explicitly explain how and why two-factor authentication was implemented.
- Dissect and explain the cipher suite identifier chosen when running the program.
- Discuss cipher suite selection (good vs. bad suites) and how to control this choice in Java.

## 4. Peer Review Processes

### 4.1 Functionality Review

- Conducted pairwise within a cluster of groups.
- Output is a signed functionality review form.
- All members must actively participate.

### 4.2 Report Review

- Each group reviews two other reports and receives two reviews.
- Written reviews should be dense and fit on at most one A4 page.
- Focus on verifying project goals, giving encouraging feedback, identifying major improvements, checking structure, language, and technical correctness.
- No grading is required.

## 5. Presentation Requirements

- **Format:** 6 minutes, mini-conference style, in English.
- **Participation:** All group members must contribute (e.g., speaking, running the demo, or handling slides). Attending your own session is mandatory.
- **Required Content:**
  - Convincingly demonstrate that a TLS connection is successfully set up.
  - Prove the system works and fulfills requirements (live demo or robust slide backup).
  - Show actual code snippets during the presentation.
  - Present a "special topic" (assigned in Canvas A-G) to teach the audience something new, advanced, or exciting.
  - Architectural overview is optional.

## 6. Implementation Resources and Technical Hints

- Functionality is prioritized over design; a GUI is entirely optional.
- Use the Java JSSE documentation, specifically looking at `ClassServer.java`, `ClassFileServer.java`, and `SSLSocketClientWithClientAuth` for sample code.
- Use the `keytool -dname` parameter to automate certificate generation.
- Truststores can be set via system properties: `java -Djavax.net.ssl.trustStore=... -Djavax.net.ssl.trustStorePassword=...` or hardcoded in Java.
