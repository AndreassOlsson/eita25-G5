package src.networking;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public class server {
  private static int numConnectedClients = 0;

  public static void main(String[] args) throws Exception {
    int port = Integer.parseInt(args[0]);
    SSLServerSocket ss = (SSLServerSocket) getServerSocketFactory("TLSv1.2")
        .createServerSocket(port, 0, InetAddress.getByName(null));
    ss.setNeedClientAuth(true);

    while (true) {
      SSLSocket socket = (SSLSocket) ss.accept();
      new Thread(() -> handleClient(socket)).start();
    }
  }

  static void handleClient(SSLSocket socket) {
    try {
      socket.startHandshake();
      SSLSession session = socket.getSession();
      X509Certificate clientCert = (X509Certificate) session.getPeerCertificates()[0];

      String subjectDN = clientCert.getSubjectX500Principal().getName();
      String cn = getFieldFromDN(subjectDN, "CN");
      String ou = getFieldFromDN(subjectDN, "OU");
      String o  = getFieldFromDN(subjectDN, "O");

      synchronized (server.class) { numConnectedClients++; }
      System.out.println("CN=" + cn + " OU=" + ou + " O=" + o);

      // ... your I/O loop here ...

      socket.close();
      synchronized (server.class) { numConnectedClients--; }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  static String getFieldFromDN(String dn, String field) {
    try {
        LdapName ldapDN = new LdapName(dn);
        for (Rdn rdn : ldapDN.getRdns()) {
            if (rdn.getType().equalsIgnoreCase(field)) {
                return rdn.getValue().toString();
            }
        }
    } catch (Exception ignored) {}
    return null;
  }
  

  private static ServerSocketFactory getServerSocketFactory(String type) {
    if (type.equals("TLSv1.2")) {
      SSLServerSocketFactory ssf = null;
      try { // set up key manager to perform server authentication
        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");
        char[] password = "password".toCharArray();
        // keystore password (storepass)
        ks.load(new FileInputStream("serverkeystore"), password);  
        // truststore password (storepass)
        ts.load(new FileInputStream("servertruststore"), password); 
        kmf.init(ks, password); // certificate password (keypass)
        tmf.init(ts);  // possible to use keystore as truststore here
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        ssf = ctx.getServerSocketFactory();
        return ssf;
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      return ServerSocketFactory.getDefault();
    }
    return null;
  }
}