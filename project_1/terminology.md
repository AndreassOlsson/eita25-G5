### 1. The Core Infrastructure (Roles & Files)

**Certificate Authority (CA)**

- **Swedish:** _Certifikatutfärdare (CA)_
- **Definition:** An entity trusted to sign digital certificates for others. In the real world, this is a company like Verisign or Let's Encrypt.
- **Project Context:** You act as the CA in Step 1. You will create a "root" certificate that is used to sign (vouch for) the client and server certificates.

**Keystore**

- **Swedish:** _Nyckellager_ (often just called "Keystore")
- **Definition:** A password-protected file (database) that holds your _own_ identity. It contains your private key and your specific public certificate chain.
- **Project Context:** You will create two: `clientkeystore` and `serverkeystore`. This is where the private keys live, so these files must be kept safe.

**Truststore**

- **Swedish:** _Lager för betrodda certifikat_ (often just called "Truststore")
- **Definition:** A file that contains the certificates of _others_ that you trust. It usually only holds public certificates (specifically CA certificates), not private keys.
- **Project Context:** You will create `clienttruststore` and `servertruststore`. By putting your CA certificate in here, you tell your Java program: "Trust any certificate signed by this CA".

**Certificate Chain**

- **Swedish:** _Certifikatskedja_
- **Definition:** A list of certificates linked together. It starts with your personal certificate, leads to the CA that signed it, and potentially up to a Root CA.
- **Project Context:** When you import your signed certificate back into the keystore, you must establish a "chain of length 2" (Your Cert + The CA Cert) so the verifier can trace the trust back to the source.

---

### 2. The Identity & Standards (The "What")

**X.509**

- **Swedish:** _X.509-standard_
- **Definition:** The standard format for public key certificates. It defines what fields (like name, issuer, expiration) must be inside the file.
- **Project Context:** You are creating an "X.509 CA certificate". The instructions also mention "version 1" vs "version 3," referring to different revisions of this standard (Version 3 supports "extensions").

**CN (Common Name)**

- **Swedish:** _Common Name_ (Allmänt namn)
- **Definition:** A specific field inside the certificate that identifies _who_ owns it. For websites, this is usually the domain name (e.g., https://www.google.com/search?q=google.com).
- **Project Context:** This is strictly enforced!
- For the CA, CN must be "CA".

- For the Client, CN must be your names/STIL-IDs.

- For the Server, CN must be "MyServer".

**CSR (Certificate Signing Request)**

- **Swedish:** _Certifikatsigneringsförfrågan_
- **Definition:** A digital application form. You create a key pair, then generate a CSR containing your public key and your name (CN). You send this "blueprint" to the CA to be signed.
- **Project Context:** You generate a CSR using `keytool` (Step 4) and then use `OpenSSL` to sign it (Step 5), converting the request into a valid certificate.

**Extensions**

- **Swedish:** _Tillägg / Extensioner_
- **Definition:** Extra fields in X.509 v3 certificates that define specific usage rules (e.g., "This certificate can only be used for a Server, not a CA").
- **Project Context:** Question C asks you to investigate what these are. You might need to configure them to ensure your certificates are version 3.

**Serial Number**

- **Swedish:** _Serienummer_
- **Definition:** A unique number assigned by the CA to every certificate it issues.
- **Project Context:** You must modify the Java code to print this number when a connection is made.

---

### 3. The Tools & Protocols (The "How")

**TLS (Transport Layer Security)**

- **Swedish:** _Transport Layer Security_
- **Definition:** The cryptographic protocol that provides secure communication over a network. It replaces the older SSL (Secure Sockets Layer).
- **Project Context:** The ultimate goal of the project is to set up a "TLS connection" between your client and server code.

**OpenSSL**

- **Swedish:** _OpenSSL_
- **Definition:** A powerful, open-source command-line toolkit for TLS and SSL protocols. It is the "Swiss Army Knife" of cryptography.
- **Project Context:** You use this to act as the CA. It is the only tool in this project capable of _signing_ certificates (turning a CSR into a Certificate).

**Keytool**

- **Swedish:** _Keytool_
- **Definition:** A key and certificate management utility included with Java.
- **Project Context:** You use this to manage your `.jks` (Java KeyStore) files. It handles creating keys and packaging them, but it cannot sign other people's certificates.

**PEM / DER / JKS (File Formats)**

- **Swedish:** _Filformat_
- **Definition:**
- _PEM/DER:_ Standard formats for storing certificates (OpenSSL uses these).
- _JKS (Java KeyStore):_ Java's specific format for storing keys.

- **Project Context:** You will likely need to move data between these formats. `keytool` works with JKS, while `OpenSSL` usually outputs PEM or DER.

---

### 4. Technical Commands & Switches mentioned

- **-CAcreateserial:** An OpenSSL switch. It generates a random serial number for a certificate if you don't provide a serial number file. You need to investigate this for Question A.

- **setNeedClientAuth(true):** A Java method in the server code. It forces the server to _demand_ a certificate from the client (Mutual TLS). If the client doesn't send one, the server hangs up. You need to explain this for Question G.
