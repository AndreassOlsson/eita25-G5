#!/bin/bash
# Project 2 PKI Setup Script
# Run this in the 'crypto' directory to generate keys in '../keystores'

PASS="password"
# Define the server's identity. 
SERVER_CN="CN=HospitalServer, OU=IT, O=Hospital, C=SE"

# Output directory relative to this script
OUT_DIR="../keystores"
mkdir -p "$OUT_DIR"

echo "=== STEP 1: Creating the Certificate Authority (CA) ==="
# -x509: Create a self-signed certificate.
# -nodes: Do not encrypt the private key (no password prompt every time we sign something).
# -subj: Hardcodes the identity of the CA.
openssl req -x509 -newkey rsa:2048 -nodes -keyout "$OUT_DIR/ca_private_key.pem" -out "$OUT_DIR/ca_cert.pem" -days 365 -subj "/CN=HospitalCA/O=Hospital/C=SE"

# Create a file to prevent users from acting as their own CAs. This marks it as an end-entity certificate. 
echo "basicConstraints=CA:FALSE" > "$OUT_DIR/v3_extension.ext"

# --- Helper Function to Create Entities ---
# Arguments: $1 = filename_prefix, $2 = alias, $3 = dname_string
create_entity() {
    local PREFIX=$1
    local ALIAS=$2
    local DNAME=$3

    echo "--- Generating PKI for: $ALIAS ---"
    
    # 1. Create Truststore (Contains only the CA cert) 
    keytool -importcert -file "$OUT_DIR/ca_cert.pem" -alias ca -keystore "$OUT_DIR/${PREFIX}_truststore.jks" -storepass $PASS -noprompt

    # 2. Create Keystore & Keypair (The Private Key)
    keytool -genkeypair -alias $ALIAS -keyalg RSA -keysize 2048 -keystore "$OUT_DIR/${PREFIX}_keystore.jks" -storepass $PASS -keypass $PASS -dname "$DNAME"

    # 3. Generate CSR (Certificate Signing Request)
    keytool -certreq -alias $ALIAS -file "$OUT_DIR/${PREFIX}.csr" -keystore "$OUT_DIR/${PREFIX}_keystore.jks" -storepass $PASS

    # 4. CA signs the CSR to create the Certificate 
    openssl x509 -req -in "$OUT_DIR/${PREFIX}.csr" -CA "$OUT_DIR/ca_cert.pem" -CAkey "$OUT_DIR/ca_private_key.pem" -CAcreateserial -out "$OUT_DIR/${PREFIX}_cert.pem" -days 365 -extfile "$OUT_DIR/v3_extension.ext"

    # 5. Import CA Cert into Keystore (Must be done before importing the signed cert)
    keytool -importcert -alias ca_root -file "$OUT_DIR/ca_cert.pem" -keystore "$OUT_DIR/${PREFIX}_keystore.jks" -storepass $PASS -noprompt

    # 6. Import Signed Cert into Keystore
    keytool -importcert -alias $ALIAS -file "$OUT_DIR/${PREFIX}_cert.pem" -keystore "$OUT_DIR/${PREFIX}_keystore.jks" -storepass $PASS

    # Cleanup temporary files
    rm "$OUT_DIR/${PREFIX}.csr" "$OUT_DIR/${PREFIX}_cert.pem"
}

echo "=== STEP 2: Creating Server Keys ==="
create_entity "server" "server" "$SERVER_CN"

echo "=== STEP 3: Creating User Keys for Small Example ==="
# Notice the naming convention: CN = Name, OU = Division, O = Role 
create_entity "doctor_alice" "client" "CN=Alice Alison, OU=Cardiology, O=Doctor, C=SE"
create_entity "nurse_bob" "client" "CN=Bob Bobson, OU=Cardiology, O=Nurse, C=SE"
create_entity "patient_charlie" "client" "CN=Charlie Charles, OU=None, O=Patient, C=SE"
create_entity "gov_dave" "client" "CN=Dave Davidson, OU=None, O=Government, C=SE"

echo "=== Setup Complete! Keys are in $OUT_DIR ==="
# Cleanup CA keys (In a real system, these would be locked in a vault)
# rm "$OUT_DIR/ca_private_key.pem" "$OUT_DIR/ca_cert.pem" "$OUT_DIR/ca_cert.srl" "$OUT_DIR/v3_extension.ext"