openssl req -new -x509 -keyout ca-key -out ca-cert 
echo import CA to truststore
keytool -keystore clienttruststore -alias CARoot -import -file ca-cert
keytool -genkey -alias client -keyalg RSA -keystore clientkeystore -keysize 2048
keytool -keystore clientkeystore -alias client -certreq -file cert-file
echo sign with CA
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:password
echo import cert-chain to keystore
keytool -keystore clientkeystore -alias CARoot -import -file ca-cert
keytool -keystore clientkeystore -alias client -import -file cert-signed

echo list client keystore
keytool -list -v -keystore clientkeystore

echo repeat for server
keytool -genkey -alias client -keyalg RSA -keystore serverkeystore -keysize 2048
keytool -keystore serverkeystore -alias client -certreq -file cert-file
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:password
keytool -keystore serverkeystore -alias CARoot -import -file ca-cert
keytool -keystore serverkeystore -alias client -import -file cert-signed

keytool -list -v -keystore serverkeystore

keytool -keystore servertruststore -alias CARoot -import -file ca-cert
