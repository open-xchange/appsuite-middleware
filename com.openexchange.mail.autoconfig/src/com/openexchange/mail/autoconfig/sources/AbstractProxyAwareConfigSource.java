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

package com.openexchange.mail.autoconfig.sources;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * Connects to the Mozilla ISPDB. For more information see <a
 * href="https://developer.mozilla.org/en/Thunderbird/Autoconfiguration">https://developer.mozilla.org/en/Thunderbird/Autoconfiguration</a>
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added google-common cache
 */
public abstract class AbstractProxyAwareConfigSource extends AbstractConfigSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractProxyAwareConfigSource.class);

    private static final String PROPERTY_ISPDB_PROXY = "com.openexchange.mail.autoconfig.http.proxy";
    private static final String PROPERTY_ISPDB_PROXY_LOGIN = "com.openexchange.mail.autoconfig.http.proxy.login";
    private static final String PROPERTY_ISPDB_PROXY_PASSWORD = "com.openexchange.mail.autoconfig.http.proxy.password";

    // -------------------------------------------------------------------------------------------------- //

    /** The OSGi service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractProxyAwareConfigSource}.
     *
     * @param services The service look-up
     */
    protected AbstractProxyAwareConfigSource(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the HTTP proxy if configured
     *
     * @param client The {@link DefaultHttpClient} instance to use
     * @param view The config view
     * @return The HTTP proxy or <code>null</code>
     * @throws OXException If proxy cannot be returned
     */
    protected HttpHost getHttpProxyIfEnabled(DefaultHttpClient client, ConfigView view) throws OXException {
        ComposedConfigProperty<String> property = view.property(PROPERTY_ISPDB_PROXY, String.class);
        if (!property.isDefined()) {
            return null;
        }

        // Get & check proxy setting
        String proxy = property.get();
        if (false != Strings.isEmpty(proxy)) {
            return null;
        }

        // Parse & apply proxy settings
        try {
            URL proxyUrl;
            {
                String sProxyUrl = Strings.asciiLowerCase(proxy.trim());
                if (sProxyUrl.startsWith("://")) {
                    sProxyUrl = new StringBuilder(sProxyUrl.length() + 4).append("http").append(sProxyUrl).toString();
                } else if (false == sProxyUrl.startsWith("http://") && false == sProxyUrl.startsWith("https://")) {
                    sProxyUrl = new StringBuilder(sProxyUrl.length() + 7).append("http://").append(sProxyUrl).toString();
                }
                proxyUrl = new URL(sProxyUrl);
            }

            boolean isHttps = proxyUrl.getProtocol().equalsIgnoreCase("https");
            int prxyPort = proxyUrl.getPort();
            if (prxyPort == -1) {
                prxyPort = isHttps ? 443 : 80;
            }

            HttpHost httpHost = new HttpHost(proxyUrl.getHost(), prxyPort, proxyUrl.getProtocol());

            ComposedConfigProperty<String> propLogin = view.property(PROPERTY_ISPDB_PROXY_LOGIN, String.class);
            if (propLogin.isDefined()) {
                ComposedConfigProperty<String> propPassword = view.property(PROPERTY_ISPDB_PROXY_PASSWORD, String.class);
                if (propPassword.isDefined()) {
                    String proxyLogin = propLogin.get();
                    String proxyPassword = propPassword.get();
                    if (false == Strings.isEmpty(proxyLogin) && false == Strings.isEmpty(proxyPassword)) {
                        proxyLogin = proxyLogin.trim();
                        proxyPassword = proxyPassword.trim();

                        Credentials credentials = new UsernamePasswordCredentials(proxyLogin, proxyPassword);
                        client.getCredentialsProvider().setCredentials(new AuthScope(httpHost.getHostName(), httpHost.getPort()), credentials);
                    }
                }
            }

            return httpHost;
        } catch (MalformedURLException e) {
            LOG.warn("Unable to parse proxy URL: {}", proxy, e);
            return null;
        } catch (NumberFormatException e) {
            LOG.warn("Invalid proxy setting: {}", proxy, e);
            return null;
        } catch (RuntimeException e) {
            LOG.warn("Could not apply proxy: {}", proxy, e);
            return null;
        }
    }

}
