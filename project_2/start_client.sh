#!/bin/bash
# Wrapper to start the client with the correct system properties

if [ $# -lt 3 ]; then
    echo "Usage: ./start_client.sh <host> <port> <user_prefix> [keystore_pass]"
    echo "Example: ./start_client.sh localhost 9876 doctor_alice <password>"
    exit 1
fi

HOST=$1
PORT=$2
USER_PREFIX=$3
KEY_PASS=$4

# Keystore path based on the user prefix convention used in setup_pki.sh
KEYSTORE="keystores/${USER_PREFIX}_keystore.jks"
TRUSTSTORE="keystores/${USER_PREFIX}_truststore.jks"

if [ ! -f "$KEYSTORE" ]; then
    echo "Error: Keystore not found at $KEYSTORE"
    echo "Did you run start_server_and_pki.sh first to generate keys?"
    exit 1
fi

echo "Starting client for $USER_PREFIX..."

# Run the client with system properties for configuring SSL
java \
  -Djavax.net.ssl.keyStore="$KEYSTORE" \
  -Djavax.net.ssl.keyStorePassword="$KEY_PASS" \
  -Djavax.net.ssl.trustStore="$TRUSTSTORE" \
  -Djavax.net.ssl.trustStorePassword="$KEY_PASS" \
  -cp . src.networking.client "$HOST" "$PORT" "$USER_PREFIX"
