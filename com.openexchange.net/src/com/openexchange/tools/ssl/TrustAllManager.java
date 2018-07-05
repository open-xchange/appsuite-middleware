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

package com.openexchange.tools.ssl;

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

    /*
     * (non-Javadoc)
     * 
     * @see javax.net.ssl.X509ExtendedTrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String, java.net.Socket)
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        // Nothing to do, cause we trust all
        log(socket.getInetAddress().getHostName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String, java.net.Socket)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        // Nothing to do, cause we trust all
        log(socket.getInetAddress().getHostName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.net.ssl.X509ExtendedTrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String, javax.net.ssl.SSLEngine)
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        // Nothing to do, cause we trust all
        log(engine.getPeerHost());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String, javax.net.ssl.SSLEngine)
     */
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
        LOG.debug("No SSL certificate check for host {}. User {} in context {} trusts all.", host, getUserId(), getContextId());
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
