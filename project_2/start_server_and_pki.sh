#!/bin/bash

cleanup() {
    echo ""
    echo "Cleaning up keystores..."
    rm -f keystores/*
}

# Run cleanup when script exits (Ctrl+C, normal exit, errors, etc.)
trap cleanup EXIT

echo "Setting up PKI infrastructure..."
chmod +x crypto/setup_pki.sh

# Ensure we start fresh
rm -rf keystores
# # Run setup_pki.sh from its directory so relative paths work as intended (creating ../keystores -> project_2/keystores)
(cd crypto && ./setup_pki.sh && cd ..)


echo ""
echo "Starting the server..."
if [ ! -d build/classes ] || [ -z "$(find build/classes -name '*.class' -print -quit)" ]; then
  echo "Error: build/classes not found or empty. Run ./build.sh before starting the server."
  exit 1
fi
# Pass system properties to the server
# Note: Using server_keystore.jks and server_truststore.jks which are created by setup_pki.sh
java \
  -Djavax.net.ssl.keyStore=keystores/server_keystore.jks \
  -Djavax.net.ssl.keyStorePassword=password \
  -Djavax.net.ssl.trustStore=keystores/server_truststore.jks \
  -Djavax.net.ssl.trustStorePassword=password \
  -Djavax.net.debug=ssl,handshake \
  -cp "build/classes:libs/*" src.networking.Server 9876