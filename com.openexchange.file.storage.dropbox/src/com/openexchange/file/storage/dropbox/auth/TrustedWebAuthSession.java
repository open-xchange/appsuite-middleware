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

package com.openexchange.file.storage.dropbox.auth;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.WebAuthSession;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;


/**
 * {@link TrustedWebAuthSession}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TrustedWebAuthSession extends WebAuthSession {

    /**
     * Initializes a new {@link TrustedWebAuthSession}.
     */
    public TrustedWebAuthSession(AppKeyPair appKeyPair, AccessType type) {
        super(appKeyPair, type);
    }

    /**
     * Initializes a new {@link TrustedWebAuthSession}.
     */
    public TrustedWebAuthSession(AppKeyPair appKeyPair, AccessType type, AccessTokenPair accessTokenPair) {
        super(appKeyPair, type, accessTokenPair);
    }

    @Override
    public synchronized HttpClient getHttpClient() {
        final HttpClient httpClient = super.getHttpClient();
        javax.net.ssl.SSLSocketFactory f = SSLSocketFactoryProvider.getDefault();
        SSLConfigurationService sslConfig = DropboxServices.getService(SSLConfigurationService.class);
        final ClientConnectionManager connectionManager = httpClient.getConnectionManager();
        final SchemeRegistry schemeRegistry = connectionManager.getSchemeRegistry();
        schemeRegistry.register(new Scheme("https", new SSLSocketFactory(f, sslConfig.getSupportedCipherSuites(), sslConfig.getSupportedProtocols(), new StrictHostnameVerifier()), 443));
        return httpClient;
    }

}
