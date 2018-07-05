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

import java.net.URI;
import java.net.URISyntaxException;
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

    public static final class ProxyInfo {
        final URI proxyUrl;
        final String proxyLogin;
        final String proxyPassword;

        ProxyInfo(URI proxyUrl, String proxyLogin, String proxyPassword) {
            super();
            this.proxyUrl = proxyUrl;
            this.proxyLogin = proxyLogin;
            this.proxyPassword = proxyPassword;
        }

        @Override
        public String toString() {
            return proxyUrl.toString();
        }
    }

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
     * @param view The config view
     * @return The HTTP proxy or <code>null</code>
     * @throws OXException If proxy cannot be returned
     */
    protected ProxyInfo getHttpProxyIfEnabled(ConfigView view) throws OXException {
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
            URI proxyUrl;
            {
                String sProxyUrl = Strings.asciiLowerCase(proxy.trim());
                if (sProxyUrl.startsWith("://")) {
                    sProxyUrl = new StringBuilder(sProxyUrl.length() + 4).append("http").append(sProxyUrl).toString();
                } else if (false == sProxyUrl.startsWith("http://") && false == sProxyUrl.startsWith("https://")) {
                    sProxyUrl = new StringBuilder(sProxyUrl.length() + 7).append("http://").append(sProxyUrl).toString();
                }
                proxyUrl = new URI(sProxyUrl);
            }

            String proxyLogin = null;
            String proxyPassword = null;

            ComposedConfigProperty<String> propLogin = view.property(PROPERTY_ISPDB_PROXY_LOGIN, String.class);
            if (propLogin.isDefined()) {
                ComposedConfigProperty<String> propPassword = view.property(PROPERTY_ISPDB_PROXY_PASSWORD, String.class);
                if (propPassword.isDefined()) {
                    proxyLogin = propLogin.get();
                    proxyPassword = propPassword.get();
                    if (false == Strings.isEmpty(proxyLogin) && false == Strings.isEmpty(proxyPassword)) {
                        proxyLogin = proxyLogin.trim();
                        proxyPassword = proxyPassword.trim();
                    }
                }
            }

            return new ProxyInfo(proxyUrl, proxyLogin, proxyPassword);
        } catch (URISyntaxException e) {
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
