/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.tools.ssl;

import static com.openexchange.java.Autoboxing.I;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.util.Tools;
import com.openexchange.log.LogProperties;

/**
 * This trust manager simply trusts all certificates.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class TrustAllManager extends X509ExtendedTrustManager implements TrustManager {

    private static final X509Certificate[] EMPTY_CERTS = new X509Certificate[0];
    private static final Logger LOG = LoggerFactory.getLogger(TrustAllManager.class);

    /**
     * Friendly constructor to allow instantiation only for the TrustAllSSLSocketFactory.
     */
    protected TrustAllManager() {
        super();
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        // Nothing to do, cause we trust all
        log("");
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        // Nothing to do, cause we trust all
        log("");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        // Nothing to do, cause we trust all
        log(socket.getInetAddress().getHostName());
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        // Nothing to do, cause we trust all
        log(socket.getInetAddress().getHostName());
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        // Nothing to do, cause we trust all
        log(engine.getPeerHost());
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        // Nothing to do, cause we trust all
        log(engine.getPeerHost());
    }

    /**
     * Log in debug level
     *
     * @param host The host
     */
    private void log(String host) {
        LOG.debug("No SSL certificate check for host {}. User {} in context {} trusts all.", host, I(getUserId()), I(getContextId()));
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return EMPTY_CERTS;
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
