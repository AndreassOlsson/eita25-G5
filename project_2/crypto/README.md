# README: PKI Setup Script (setup_pki.sh)

This script acts as the IT department and Certificate Authority for our hospital system. It creates a closed, private PKI ecosystem rather than relying on a public CA from the internet. The script first generates the internal Root CA private key and its self-signed certificate. Crucially, this CA private key acts as the master rubber stamp for the entire system and must never live on the server itself. If the server were hacked and the CA private key was stored there, the attacker could forge mathematically valid employee identities. After establishing the CA, the script generates individual keypairs for the server and the various users. It uses the CA private key to sign all other certificates, which aligns with the project requirement that all certificates must be signed by a CA.

During the execution of this script, several different file extensions are generated.

- .pem are Privacy Enhanced Mail files, which are simply Base64 encoded text files that hold raw public certificates or unencrypted private keys.
- .jks are Java KeyStores, which act as encrypted digital vaults that securely bundle a private key and its corresponding certificate together.
- .csr are Certificate Signing Requests, which act as the application paperwork containing a public key and identity information that is sent to the CA for approval.
- .ext is a configuration extension used during the signing process to explicitly strip away a user's ability to act as their own CA (by making the certificate an end-entity certificate).
- .srl file is a simple text file that OpenSSL automatically creates to keep track of the serial numbers it assigns to the certificates it issues.

Ultimately, we generate these encrypted KeyStore vaults because the project requires users to have both the actual keystore and also the password to the keystore to achieve two-factor authentication.
