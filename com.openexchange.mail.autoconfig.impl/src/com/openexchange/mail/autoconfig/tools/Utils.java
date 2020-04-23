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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.autoconfig.tools;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.net.URISyntaxException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class Utils {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Utils.class);
    }

    public static final String PROPERTY_ISPDB_PROXY = "com.openexchange.mail.autoconfig.http.proxy";
    public static final String PROPERTY_ISPDB_PROXY_LOGIN = "com.openexchange.mail.autoconfig.http.proxy.login";
    public static final String PROPERTY_ISPDB_PROXY_PASSWORD = "com.openexchange.mail.autoconfig.http.proxy.password";

    public static final String OX_CONTEXT_ID = "OX-Context-Object";
    public static final String OX_USER_ID = "OX-User-Object";
    public static final String OX_TARGET_ID = "OX-Target-Object";

    /**
     * Initializes a new {@link Utils}.
     */
    private Utils() {
        super();
    }

    public static ProxyInfo getHttpProxyIfEnabled(ConfigView view) throws OXException {
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
                    if (Strings.isNotEmpty(proxyLogin) && Strings.isNotEmpty(proxyPassword)) {
                        proxyLogin = proxyLogin.trim();
                        proxyPassword = proxyPassword.trim();
                    }
                }
            }

            return new ProxyInfo(proxyUrl, proxyLogin, proxyPassword);
        } catch (URISyntaxException e) {
            LoggerHolder.LOGGER.warn("Unable to parse proxy URL: {}", proxy, e);
            return null;
        } catch (NumberFormatException e) {
            LoggerHolder.LOGGER.warn("Invalid proxy setting: {}", proxy, e);
            return null;
        } catch (RuntimeException e) {
            LoggerHolder.LOGGER.warn("Could not apply proxy: {}", proxy, e);
            return null;
        }
    }

    /**
     * Checks if given host/port denote the primary IMAP account of specified user.
     *
     * @param host The host
     * @param port The port
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if given host/port denote the primary IMAP account; otherwise <code>false</code>
     */
    public static boolean isPrimaryImapAccount(String host, int port, int userId, int contextId) {
        try {
            MailAccountStorageService storageService = Services.optService(MailAccountStorageService.class);
            if (storageService == null) {
                return false;
            }

            MailAccount defaultMailAccount = storageService.getDefaultMailAccount(userId, contextId);
            if (false == Strings.asciiLowerCase(defaultMailAccount.getMailProtocol()).startsWith("imap")) {
                return false;
            }
            return IDNA.toASCII(host).equals(IDNA.toASCII(defaultMailAccount.getMailServer())) && port == defaultMailAccount.getMailPort();
        } catch (Exception e) {
           LoggerHolder.LOGGER.warn("Failed to check for primary IMAP account of user {} in context {}", I(userId), I(contextId), e);
           return false;
        }
    }

}
