
package com.openexchange.net.ssl.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * {@link CheckCert}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CheckCert {

    public static void main(String[] args) throws Exception {
        String host;
        int port;
        char[] passphrase;
        if ((args.length == 1) || (args.length == 2)) {
            String[] c = args[0].split(":");
            host = c[0];
            port = (c.length == 1) ? 443 : Integer.parseInt(c[1]);
            String p = (args.length == 1) ? "changeit" : args[1];
            passphrase = p.toCharArray();
        } else {
            System.out.println("Usage: java CheckCert <host>[:port] [passphrase]");
            host = "www.open-xchange.com";
            port = 443;
            passphrase = "changeit".toCharArray();
            //return;
        }

        File file = new File("jssecacerts");
        if (file.isFile() == false) {
            char SEP = File.separatorChar;
            File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
            file = new File(dir, "jssecacerts");
            if (file.isFile() == false) {
                file = new File(dir, "cacerts");
            }
        }
        System.out.println("Loading KeyStore " + file.getAbsolutePath() + "...");
        InputStream in = new FileInputStream(file);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
        in.close();

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        AcceptAllTrustManager tm = new AcceptAllTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory factory = context.getSocketFactory();

        System.out.println("Opening connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try {
            System.out.println("Starting SSL handshake...");
            socket.startHandshake();
            socket.close();
            System.out.println();
            System.out.println("No errors, certificate is already trusted");
        } catch (SSLException e) {
            System.out.println("ERROR: ");
            e.printStackTrace();
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            System.out.println("Could not obtain server certificate chain");
            return;
        }

        System.out.println();
        System.out.println("Server sent " + chain.length + " certificate(s):");
        System.out.println();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            System.out.println("Certificate " + (i + 1));
            System.out.println("     Common Name......: " + cert.getSubjectDN());
            System.out.println("     Issued by........: " + cert.getIssuerDN());
            System.out.println("     Issued on........: " + cert.getNotBefore());
            System.out.println("     Expiration Date..: " + cert.getNotAfter());
            System.out.println("     Serial Number....: " + cert.getSerialNumber().toString(16));
            System.out.println("     Signature........: " + toHexString(cert.getSignature()));
            System.out.println("  Public Key Info");
            PublicKey pk = cert.getPublicKey();
            System.out.println("     Algorithm........: " + pk.getAlgorithm());
            System.out.println("     Format...........: " + pk.getFormat());
            System.out.println("   " + pk);
            sha256.update(cert.getEncoded());
            sha1.update(cert.getEncoded());
            md5.update(cert.getEncoded());
            System.out.println("  Fingerprints");
            System.out.println("     MD5..............: " + toHexString(md5.digest()));
            System.out.println("     SHA1.............: " + toHexString(sha1.digest()));
            System.out.println("     SHA-256..........: " + toHexString(sha256.digest()));
            System.out.println();
        }
    }

    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static class AcceptAllTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        AcceptAllTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return tm.getAcceptedIssuers();
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //noop
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }

}
