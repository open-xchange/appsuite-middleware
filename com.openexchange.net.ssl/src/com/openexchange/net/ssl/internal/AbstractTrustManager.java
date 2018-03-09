/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.net.ssl.internal;

import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;
import com.openexchange.log.LogProperties;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.net.ssl.management.Certificate;
import com.openexchange.net.ssl.management.DefaultCertificate;
import com.openexchange.net.ssl.management.SSLCertificateManagementService;
import com.openexchange.net.ssl.management.exception.SSLCertificateManagementExceptionCode;
import com.openexchange.net.ssl.osgi.Services;

/**
 * {@link AbstractTrustManager}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public abstract class AbstractTrustManager extends X509ExtendedTrustManager {

    private static final String FINGERPRINT_NAME = "fingerprint";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractTrustManager.class);

    // ---------------------------------------------------------------------------------------------------------------------

    /** The associated X509 trust manager */
    protected final X509ExtendedTrustManager trustManager;

    /**
     * Initializes a new {@link AbstractTrustManager}.
     *
     * @param trustManager The trust manager
     */
    protected AbstractTrustManager(X509ExtendedTrustManager trustManager) {
        super();
        this.trustManager = trustManager;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.trustManager.getAcceptedIssuers();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String, java.net.Socket)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        if (Services.getService(SSLConfigurationService.class).isWhitelisted(socket.getInetAddress().getHostName())) {
            return;
        }

        try {
            checkCommonName(getUserId(), getContextId(), chain, socket);
            this.trustManager.checkServerTrusted(chain, authType, socket);
        } catch (CertificateException e) {
            handleCertificateException(chain, socket, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        Set<String> hosts = new HashSet<String>();
        String host = getHostFromPrincipal(chain[0]);
        hosts.add(host);

        Collection<List<?>> subjectAltNames = chain[0].getSubjectAlternativeNames();
        if (subjectAltNames != null) {
            for (List<?> altName : subjectAltNames) {
                String value = (String) altName.get(1);
                hosts.add(value);
            }
        }

        String[] hostsArray = hosts.toArray(new String[0]);
        SSLConfigurationService sslConfigurationService = Services.getService(SSLConfigurationService.class);
        for (String lHost : hostsArray) {
            if (sslConfigurationService.isWhitelisted(lHost)) {
                return;
            }
        }

        try {
            this.trustManager.checkServerTrusted(chain, authType);
            return;
        } catch (CertificateException e) {
            handleCertificateException(chain, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String, javax.net.ssl.SSLEngine)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        if (Services.getService(SSLConfigurationService.class).isWhitelisted(engine.getSession().getPeerHost())) {
            return;
        }

        try {
            this.trustManager.checkServerTrusted(chain, authType, engine);
            return;
        } catch (CertificateException e) {
            handleCertificateException(chain, e);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // do not check client
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        // do not check client
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        // do not check client
    }

    ////////////////////////////////////////////////// HELPERS ///////////////////////////////////////////////

    /**
     * Returns the hostname from the specified {@link X509Certificate}
     *
     * @param x509Certificate the {@link X509Certificate}
     * @return The hostname or <code>null</code> if none could be found
     */
    private String getHostFromPrincipal(X509Certificate x509Certificate) {
        String dn = x509Certificate.getSubjectDN().getName();
        try {
            LdapName ln = new LdapName(dn);
            for (Rdn rdn : ln.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    return (String) rdn.getValue();
                }
            }
        } catch (InvalidNameException e) {
            LOG.warn("Unable to retrieve host from certificate.", e);
        }
        return null;
    }

    /**
     * Retrieves the hostname from the specified {@link Socket}
     *
     * @param socket The {@link Socket} from which to retrieve the hostname
     * @return The hostname or <code>null</code> if the socket is <code>null</code>,
     *         or not connected
     */
    private String getHostFromSocket(Socket socket) {
        if (socket == null) {
            LOG.debug("The socket is null");
            return null;
        }
        InetAddress inetAddress = socket.getInetAddress();
        if (inetAddress == null) {
            LOG.debug("The socket is not connected.");
            return null;
        }
        return inetAddress.getHostName();
    }

    /**
     * Handles the specified {@link CertificateException}. It first verifies whether the issuer of the
     * specified {@link X509Certificate} chain is trusted, then whether the user trusts the certificate
     *
     * @param chain The {@link X509Certificate} chain
     * @param e The {@link CertificateException} to handle/or re-throw
     * @throws CertificateException if the specified {@link X509Certificate} chain is not trusted by the user
     */
    private void handleCertificateException(X509Certificate[] chain, CertificateException e) throws CertificateException {
        handleCertificateException(chain, null, e);
    }

    /**
     * Handles the specified {@link CertificateException}. It first verifies whether the issuer of the
     * specified {@link X509Certificate} chain is trusted, then whether the user trusts the certificate
     *
     * @param chain The {@link X509Certificate} chain
     * @param e The {@link CertificateException} to handle/or re-throw
     * @throws CertificateException if the specified {@link X509Certificate} chain is not trusted by the user
     */
    private void handleCertificateException(X509Certificate[] chain, Socket socket, CertificateException e) throws CertificateException {
        if (chain == null || chain.length == 0) {
            throw new IllegalArgumentException("The supplied certificate chain is empty", e);
        }

        logChain(chain);

        // Fetch user details
        int user = getUserId();
        if (user < 0) {
            // Missing user information
            throw e;
        }
        int context = getContextId();
        if (context < 0) {
            // Missing context information
            throw e;
        }

        // Check if the user is allowed to accept untrusted certificates
        UserAwareSSLConfigurationService sslConfigurationService = Services.optService(UserAwareSSLConfigurationService.class);
        if (null == sslConfigurationService || false == sslConfigurationService.isAllowedToDefineTrustLevel(user, context)) {
            X509Certificate certificate = chain[0];
            String fingerprint = getFingerprint(certificate);
            throw new CertificateException(SSLExceptionCode.UNTRUSTED_CERTIFICATE.create(e, Collections.singletonMap(FINGERPRINT_NAME, fingerprint), fingerprint, getHostFromSocket(socket)));
        }

        // Check if the user trusts it
        checkUserTrustsServer(user, context, chain, socket, e);
    }

    /**
     * Log the entire {@link X509Certificate} chain in the debug level
     *
     * @param chain The chain to log
     * @throws CertificateEncodingException if the fingerprint of a certificate cannot be generated
     */
    private void logChain(X509Certificate[] chain) throws CertificateEncodingException {
        if (LOG.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder(512);
            List<Object> args = new ArrayList<>(32);
            String sep = Strings.getLineSeparator();
            for (int i = 0; i < chain.length; i++) {
                X509Certificate cert = chain[i];
                builder.append("{}Certificate ").append((i + 1)); args.add(sep);
                builder.append("{}     Common Name......: ").append(cert.getSubjectDN()); args.add(sep);
                builder.append("{}     Issued by........: ").append(cert.getIssuerDN()); args.add(sep);
                builder.append("{}     Issued on........: ").append(cert.getNotBefore()); args.add(sep);
                builder.append("{}     Expiration Date..: ").append(cert.getNotAfter()); args.add(sep);
                builder.append("{}     Serial Number....: ").append(cert.getSerialNumber().toString(16)); args.add(sep);
                builder.append("{}     Signature........: ").append(toHex(cert.getSignature())); args.add(sep);
                builder.append("{}  Public Key Info"); args.add(sep);
                PublicKey pk = cert.getPublicKey();
                builder.append("{}     Algorithm........: ").append(pk.getAlgorithm()); args.add(sep);
                builder.append("{}     Format...........: ").append(pk.getFormat()); args.add(sep);
                builder.append("{}   ").append(pk); args.add(sep);
                builder.append("{}  Fingerprint"); args.add(sep);
                builder.append("{}     SHA-256..........: ").append(getFingerprint(cert)); args.add(sep);
                builder.append("{}"); args.add(sep);
            }
            LOG.debug(builder.toString(), args.toArray(new Object[args.size()]));
        }
    }

    /**
     * Performs a preliminary check on the specified {@link CertificateException}
     * to determine any common/known reasons of failure
     *
     * @param e The {@link CertificateException} to check
     * @throws CertificateException
     */
    private void preliminaryChecks(int userId, int contextId, X509Certificate[] chain, Socket socket, CertificateException ce) throws CertificateException {
        if (ce == null) {
            return;
        }
        if (!(ce.getCause() instanceof CertPathValidatorException)) {
            return;
        }

        CertPathValidatorException cp = (CertPathValidatorException) ce.getCause();
        FailureResponse failureResponse = ReasonHandler.handle(cp.getReason());

        String socketHostname = getHostFromSocket(socket);
        String fingerprint = cacheCertificate(userId, contextId, chain[0], socketHostname, failureResponse.getFailureReason());
        throw new CertificateException(failureResponse.getSSLExceptionCode().create(Collections.singletonMap(FINGERPRINT_NAME, fingerprint), fingerprint, socketHostname, userId, contextId));
    }

    /**
     * Checks whether the user trusts the specified {@link X509Certificate}
     *
     * @param chain The certificate to check
     */
    private void checkUserTrustsServer(int userId, int contextId, X509Certificate[] chain, Socket socket, CertificateException ce) throws CertificateException {
        // The certificate under examination is always the first one in the chain
        X509Certificate cert = chain[0];
        String socketHostname = getHostFromSocket(socket);
        try {
            String fingerprint = getFingerprint(cert);

            SSLCertificateManagementService certificateManagement = Services.getService(SSLCertificateManagementService.class);
            Certificate certificate = certificateManagement.get(userId, contextId, socketHostname, fingerprint);
            if (!certificate.isTrusted()) {
                cacheCertificate(userId, contextId, cert, socketHostname, FailureReason.NOT_TRUSTED_BY_USER);
                throw new CertificateException(SSLExceptionCode.USER_DOES_NOT_TRUST_CERTIFICATE.create(Collections.singletonMap(FINGERPRINT_NAME, fingerprint), fingerprint, socketHostname, userId, contextId));
            }

            if (!Strings.isEmpty(socketHostname) && !socketHostname.equals(certificate.getHostName())) {
                cacheCertificate(userId, contextId, cert, socketHostname, FailureReason.INVALID_COMMON_NAME);
                throw new CertificateException(SSLExceptionCode.INVALID_HOSTNAME.create(Collections.singletonMap(FINGERPRINT_NAME, fingerprint), fingerprint, socketHostname));
            }
        } catch (OXException e) {
            if (SSLCertificateManagementExceptionCode.CERTIFICATE_NOT_FOUND.equals(e)) {
                // If not found in the user's store, try to determine the reason of failure
                // a) Perform some common preliminary checks
                preliminaryChecks(userId, contextId, chain, socket, ce);
                // b) Check if the certificate is self-signed
                checkSelfSigned(userId, contextId, chain, socketHostname);
                // c) Check if the root certificate authority is trusted
                checkRootCATrusted(userId, contextId, chain, socketHostname);
                // d) Check common name
                checkCommonName(userId, contextId, chain, socket);
                // e) Check if expired
                checkExpired(userId, contextId, chain);

                // If the previous checks did not fail, cache it for future reference and throw as last resort
                String fingerprint = cacheCertificate(userId, contextId, cert, socketHostname, FailureReason.UNTRUSTED_CERTIFICATE);
                throw new CertificateException(SSLExceptionCode.UNTRUSTED_CERTIFICATE.create(Collections.singletonMap(FINGERPRINT_NAME, fingerprint), fingerprint, socketHostname));
            }
            throw new CertificateException(e);
        }
    }

    /**
     * Checks whether the specified {@link X509Certificate} chain is a self-signed certificate
     *
     * @param chain The {@link X509Certificate} chain
     * @throws CertificateException If the specified {@link X509Certificate} is self-signed
     */
    private void checkSelfSigned(int userId, int contextId, X509Certificate[] chain, String socketHostname) throws CertificateException {
        // Self-signed certificates are the only certificates in the chain
        if (chain.length > 1) {
            return;
        }
        if (chain[0].getIssuerDN().equals(chain[0].getSubjectDN()) && !isRootCATrusted(chain)) {
            String fingerprint = cacheCertificate(userId, contextId, chain[0], socketHostname, FailureReason.SELF_SIGNED);
            throw new CertificateException(SSLExceptionCode.SELF_SIGNED_CERTIFICATE.create(Collections.singletonMap(FINGERPRINT_NAME, fingerprint), fingerprint, socketHostname));
        }
    }

    /**
     * Checks whether the issuer of the specified {@link X509Certificate} chain is a trusted root CA
     *
     * @param chain The {@link X509Certificate} chain
     * @throws CertificateException if the root CA is not trusted by the user
     */
    private void checkRootCATrusted(int userId, int contextId, X509Certificate[] chain, String socketHostname) throws CertificateException {
        if (isRootCATrusted(chain)) {
            return;
        }

        String fingerprint = cacheCertificate(userId, contextId, chain[0], socketHostname, FailureReason.UNTRUSTED_ISSUER);
        throw new CertificateException(SSLExceptionCode.UNTRUSTED_ROOT_AUTHORITY.create(Collections.singletonMap(FINGERPRINT_NAME, fingerprint), fingerprint, socketHostname));
    }

    /**
     * Perform a check whether the Certificate Authority that issued the specified chain is trusted
     *
     * @param chain The {@link X509Certificate} chain
     * @return <code>true</code> if the certificate authority is trusted, <code>false</code> otherwise
     */
    private boolean isRootCATrusted(X509Certificate[] chain) {
        X509Certificate[] acceptedIssuers = trustManager.getAcceptedIssuers();
        X509Certificate rootCert = chain[chain.length - 1];
        for (X509Certificate x509Certificate : acceptedIssuers) {
            if (x509Certificate.getSubjectDN().equals(rootCert.getIssuerDN())) {
                LOG.debug("The root CA '{}' is trusted", rootCert.getIssuerDN());
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the common name for which the specified {@link X509Certificate} was issued against the hostname
     * used in the specified {@link Socket}
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param chain The {@link X509Certificate} chain
     * @param socket The socket
     * @throws CertificateException if the common name is invalid
     */
    private void checkCommonName(int userId, int contextId, X509Certificate[] chain, Socket socket) throws CertificateException {
        if (socket == null) {
            LOG.debug("No socket was provided. Skipping common name check");
            return;
        }

        X509Certificate certificate = chain[0];
        String commonName = getHostFromPrincipal(certificate);
        if (Strings.isEmpty(commonName)) {
            LOG.debug("No common name retrieved from the certificate. Skipping common name check");
            return;
        }
        String hostname = getHostFromSocket(socket);
        if (commonName.equals(hostname)) {
            return;
        }

        // Check for wildcard certificates
        if (commonName.startsWith("*")) {
            int cnDotCount = commonName.split("\\.").length;
            int hnDotCount = hostname.split("\\.").length;
            if (hnDotCount == cnDotCount) {
                return;
            }
        }

        // Check for possible SubjectAlternativeNames
        Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
        if (subjectAlternativeNames != null) {
            for (List<?> list : subjectAlternativeNames) {
                if (list.contains(commonName)) {
                    return;
                }
            }
        }

        logChain(chain);

        String fingerprint = cacheCertificate(userId, contextId, chain[0], hostname, FailureReason.INVALID_COMMON_NAME);
        throw new CertificateException(SSLExceptionCode.INVALID_HOSTNAME.create(Collections.singletonMap(FINGERPRINT_NAME, fingerprint), fingerprint, hostname));
    }

    /**
     * Checks if the certificate is expired
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param chain The {@link X509Certificate} chain
     * @throws CertificateException if the specified {@link X509Certificate} is expired
     */
    private void checkExpired(int userId, int contextId, X509Certificate[] chain) throws CertificateException {
        X509Certificate certificate = chain[0];
        if (certificate.getNotAfter().getTime() < System.currentTimeMillis()) {
            String hostname = getHostFromPrincipal(chain[0]);
            String fingerprint = cacheCertificate(userId, contextId, chain[0], hostname, FailureReason.EXPIRED);
            throw new CertificateException(SSLExceptionCode.CERTIFICATE_IS_EXPIRED.create(fingerprint, hostname));
        }
    }

    /**
     * Caches the specified {@link X509Certificate} for future reference
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param chain The {@link X509Certificate}
     * @param failureReason The reason why the certificate is untrusted
     * @return returns the fingerprint of the {@link Certificate}
     * @throws CertificateException if an error is occurred
     */
    private String cacheCertificate(int userId, int contextId, X509Certificate cert, String hostname, FailureReason failureReason) throws CertificateException {
        try {
            // Create the certificate
            String fingerprint = getFingerprint(cert);
            long expirationTimestamp = cert.getNotAfter().getTime();
            DefaultCertificate.Builder certificate = DefaultCertificate.builder().fingerprint(fingerprint).commonName(getHostFromPrincipal(cert)).hostName(Strings.isEmpty(hostname) ? getHostFromPrincipal(cert) : hostname).expirationTimestamp(expirationTimestamp).issuedOnTimestamp(cert.getNotBefore().getTime()).issuer(cert.getIssuerDN().toString()).serialNumber(cert.getSerialNumber().toString(16)).signature(toHex(cert.getSignature())).trusted(false).expired(expirationTimestamp < System.currentTimeMillis()).failureReason(failureReason.getDetail());

            // Cache it
            SSLCertificateManagementService certificateManagement = Services.getService(SSLCertificateManagementService.class);
            certificateManagement.cache(userId, contextId, certificate.build());

            return fingerprint;
        } catch (OXException e) {
            throw new CertificateException(e);
        } catch (RuntimeException e) {
            throw new CertificateException(e);
        }
    }

    /**
     * Retrieves the SHA-256 fingerprint from the specified {@link X509Certificate}
     *
     * @param certificate The certificate from which to retrieve the fingerprint
     * @return The SHA-256 fingerprint of the specified {@link X509Certificate}
     * @throws CertificateEncodingException If an encoding error occurs
     * @throws IllegalArgumentException If the fingerprint cannot be hashed
     */
    private static String getFingerprint(X509Certificate certificate) throws CertificateEncodingException {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(certificate.getEncoded());
            return toHex(sha256.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("The cryptographic algorithm 'SHA-256' is not available.", e);
        }
    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * Converts the specified byte array to a hexadecimal string
     *
     * @param bytes The byte array to convert
     * @return The hexadecimal representation of the byte array
     * @throws IllegalArgumentException If the specified byte array is <code>null</code>
     */
    private static String toHex(byte[] bytes) {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("The specified byte array cannot be empty");
        }
        char[] hexChars = new char[bytes.length << 1];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Retrieves the context identifier from the {@link LogProperties}
     *
     * @return the context identifier from the LogProperties
     */
    private static int getContextId() {
        return getLogPropertyIntValue(LogProperties.Name.SESSION_CONTEXT_ID);
    }

    /**
     * Retrieves the user identifier from the {@link LogProperties}
     *
     * @return the user identifier from the {@link LogProperties}
     */
    private static int getUserId() {
        return getLogPropertyIntValue(LogProperties.Name.SESSION_USER_ID);
    }

    /**
     * Retrieves value of the specified property from the {@link LogProperties}
     *
     * @param name The log property's name
     * @return the property's value
     */
    private static int getLogPropertyIntValue(LogProperties.Name name) {
        return Tools.getUnsignedInteger(LogProperties.get(name));
    }
}
