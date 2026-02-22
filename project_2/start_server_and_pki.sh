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
javac src/**/*.java
java -cp . src.networking.server 9876