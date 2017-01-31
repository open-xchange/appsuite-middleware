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

import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.Tools;
import com.openexchange.log.LogProperties;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.net.ssl.management.Certificate;
import com.openexchange.net.ssl.management.SSLCertificateManagementService;
import com.openexchange.net.ssl.management.exception.SSLCertificateManagementSQLExceptionCode;
import com.openexchange.net.ssl.osgi.Services;

/**
 * {@link AbstractTrustManager}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public abstract class AbstractTrustManager extends X509ExtendedTrustManager {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractTrustManager.class);

    protected final X509ExtendedTrustManager trustManager;

    protected static PKIXParameters params;

    /**
     * Initializes a new {@link AbstractTrustManager}.
     *
     * @param trustManager The trust manager
     */
    protected AbstractTrustManager(X509ExtendedTrustManager trustManager) {
        super();
        this.trustManager = trustManager;
    }

    public boolean isInitialized() {
        return this.trustManager != null;
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
            this.trustManager.checkServerTrusted(chain, authType, socket);
            return;
        } catch (CertificateException e) {
            handleCertificateException(chain, e);
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
     * Handles the specified {@link CertificateException}. It first verifies whether the issuer of the
     * specified {@link X509Certificate} chain is trusted, then whether the user trusts the certificate
     * 
     * @param chain The {@link X509Certificate} chain
     * @param e The {@link CertificateException} to handle/or re-throw
     * @throws CertificateException if the specified {@link X509Certificate} chain is not trusted by the user
     */
    private void handleCertificateException(X509Certificate[] chain, CertificateException e) throws CertificateException {
        // Try to determine the reason of failure
        if (!e.getMessage().contains("unable to find valid certification path to requested target")) {
            throw e;
        }
        int user = Tools.getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_USER_ID));
        int context = Tools.getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID));

        // a) Check if the user trusts it
        checkUserTrustsServer(user, context, chain, e);
        // b) Check if the certificate is self-signed
        checkSelfSigned(user, context, chain);
        // c) Check if the root certificate authority is trusted
        checkRootCATrusted(user, context, chain);
    }

    /**
     * Checks whether the specified {@link X509Certificate} chain is a self-signed certificate
     * 
     * @param chain The {@link X509Certificate} chain
     * @throws CertificateException If the specified {@link X509Certificate} is self-signed
     */
    private void checkSelfSigned(int userId, int contextId, X509Certificate[] chain) throws CertificateException {
        // Self-signed certificates are the only certificates in the chain
        if (chain.length < 2) {
            String fingerprint = "";
            try {
                fingerprint = getFingerprint(chain[0]);
                cacheCertificate(userId, contextId, chain[0]);
            } catch (NoSuchAlgorithmException e) {
                //TODO: detailed error log
                LOG.error("Cannot retrieve the fingerprint for the chain");
            }
            throw new CertificateException(SSLExceptionCode.SELF_SIGNED_CERTIFICATE.create(fingerprint));
        }
    }

    /**
     * Checks whether the issuer of the specified {@link X509Certificate} chain is a trusted root CA
     * 
     * @param chain The {@link X509Certificate} chain
     * @throws CertificateException if the root CA is not trusted by the user
     */
    private void checkRootCATrusted(int userId, int contextId, X509Certificate[] chain) throws CertificateException {
        Set<TrustAnchor> anchors = params.getTrustAnchors();

        //The root CA is always the last element of the chain
        X509Certificate rootCert = chain[chain.length - 1];
        for (TrustAnchor a : anchors) {
            if (a.getTrustedCert().getSubjectDN().equals(rootCert.getIssuerDN())) {
                LOG.debug("The root CA '{}' is trusted", rootCert.getIssuerDN());
                return;
            }
        }

        cacheCertificate(userId, contextId, rootCert);
        throw new CertificateException(SSLExceptionCode.ROOT_CA_UNTRUSTED.create(rootCert.getIssuerDN()));
    }

    /**
     * Checks whether the user trusts the specified {@link X509Certificate}
     * 
     * @param chain The certificate to check
     */
    private void checkUserTrustsServer(int userId, int contextId, X509Certificate[] chain, CertificateException ce) throws CertificateException {
        /*
         * MW-445: Main Concept
         * - Check if the server is to be trusted; if yes return
         * - Check if the user is allowed to accept untrusted certificates; if not throw an exception
         * - Retrieve the fingerprint(s) of the certificate
         * - Search if the user already accepted the certificate; if yes return
         * |_ Throw an exception in case the user previously denied the certificate
         * |_ Throw an exception with the indication that the server is untrusted (the user can then choose what to do)
         */

        // TODO: Check if the user is allowed to accept untrusted certificates
        // via config

        if (chain == null) {
            LOG.error("Could not obtain server certificate chain");
            //TODO: throw exception
            throw new IllegalArgumentException("The server certificate chain cannot be null");
        }

        X509Certificate cert = chain[0];
        String fingerprint = null;
        try {
            fingerprint = getFingerprint(cert);
            SSLCertificateManagementService certificateManagement = Services.getService(SSLCertificateManagementService.class);
            if (!certificateManagement.isTrusted(userId, contextId, fingerprint)) {
                throw new CertificateException(SSLExceptionCode.USER_DOES_NOT_TRUST_CERTIFICATE.create(userId, contextId, fingerprint));
            }
        } catch (NoSuchAlgorithmException e) {
            //TODO: detailed error log
            LOG.error("Cannot retrieve the fingerprint for the chain");
        } catch (OXException e) {
            if (SSLCertificateManagementSQLExceptionCode.CERTIFICATE_NOT_FOUND.equals(e)) {
                throw new CertificateException(SSLExceptionCode.USER_DOES_NOT_TRUST_CERTIFICATE.create(userId, contextId, fingerprint));
            }
            throw new CertificateException(e);
        }
        // TODO: Create a list with the untrusted certificates and pass them as parameters to a nested OX exception
        //        if (!untrustedFingerprints.isEmpty() || !unknownFingerprints.isEmpty()) {
        //            StringBuilder builder = new StringBuilder("[");
        //            for (Certificate c : unknownFingerprints) {
        //                builder.append(c.toString()).append(",");
        //            }
        //            builder.setLength(builder.length() - 1);
        //            builder.append("]");
        //            throw new CertificateException(SSLExceptionCode.USER_DOES_NOT_TRUST_CERTIFICATE.create(userId, contextId, untrustedFingerprints.toString(), builder.toString()));
        //        }
    }

    /**
     * Caches the specified {@link X509Certificate} for future reference
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param chain The {@link X509Certificate}
     * @throws CertificateException if an error is occurred
     */
    private void cacheCertificate(int userId, int contextId, X509Certificate cert) throws CertificateException {
        try {
            // Create the certificate
            Certificate certificate = new Certificate(getFingerprint(cert));
            certificate.setCommonName(getHostFromPrincipal(cert));
            certificate.setExpirationTimestamp(cert.getNotAfter().getTime());
            certificate.setExpirationTimestamp(cert.getNotBefore().getTime());
            certificate.setIssuer(cert.getIssuerDN().toString());
            certificate.setSerialNumber(cert.getSerialNumber().toString(16));
            certificate.setSignature(toHex(cert.getSignature()));
            certificate.setTrusted(false);

            // Cache it
            SSLCertificateManagementService certificateManagement = Services.getService(SSLCertificateManagementService.class);
            certificateManagement.cache(userId, contextId, certificate);
        } catch (OXException | NoSuchAlgorithmException e) {
            throw new CertificateException(e);
        }
    }

    /**
     * Retrieves the SHA-256 fingerprint from the specified {@link X509Certificate}
     * 
     * @param certificate The certificate from which to retrieve the fingerprint
     * @return The SHA-256 fingerprint of the specified {@link X509Certificate}
     * @throws NoSuchAlgorithmException
     * @throws CertificateEncodingException
     */
    private String getFingerprint(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(certificate.getEncoded());
        return toHex(sha256.digest());
    }

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * Converts the specified byte array to a hexadecimal string
     * 
     * @param bytes The byte array to convert
     * @return The hexadecimal representation of the byte array
     * @throws IllegalArgumentException if the specified byte array is empty
     */
    private String toHex(byte[] bytes) {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("The specified byte array cannot be empty");
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
