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

package com.openexchange.ssl.internal;

import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.ssl.osgi.Services;
import com.openexchange.ssl.utils.HostList;

/**
 * {@link SSLProperties}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public enum SSLProperties {

    /* Enables logging SSL details. Have a look at http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/ReadDebug.html for more details. */
    SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED(SSLProperties.SECURE_CONNECTIONS_DEBUG_LOGS_KEY, false),

    HOSTNAME_VERIFICATION_ENABLED(SSLProperties.HOSTNAME_VERIFICATION_ENABLED_KEY, true),

    DEFAULT_TRUSTSTORE_ENABLED(SSLProperties.DEFAULT_TRUSTSTORE_ENABLED_KEY, true),

    CUSTOM_TRUSTSTORE_ENABLED(SSLProperties.CUSTOM_TRUSTSTORE_ENABLED_KEY, false),

    CUSTOM_TRUSTSTORE_LOCATION(SSLProperties.CUSTOM_TRUSTSTORE_PATH_KEY, SSLProperties.EMPTY_STRING),

    CUSTOM_TRUSTSTORE_PASSWORD(SSLProperties.CUSTOM_TRUSTSTORE_PASSWORD_KEY, SSLProperties.EMPTY_STRING),

    ;

    static final String EMPTY_STRING = "";

    static final String SECURE_CONNECTIONS_DEBUG_LOGS_KEY = "com.openexchange.ssl.debug.logs";

    static final String CUSTOM_TRUSTSTORE_PATH_KEY = "com.openexchange.ssl.custom.truststore.path";

    static final String CUSTOM_TRUSTSTORE_PASSWORD_KEY = "com.openexchange.ssl.custom.truststore.password";

    static final String DEFAULT_TRUSTSTORE_ENABLED_KEY = "com.openexchange.ssl.default.truststore.enabled";

    static final String CUSTOM_TRUSTSTORE_ENABLED_KEY = "com.openexchange.ssl.custom.truststore.enabled";

    static final String SECURE_CONNECTIONS_KEY = "com.openexchange.ssl.only";

    private static volatile Boolean isSecureEnabled;

    public static boolean isSecureEnabled() {
        Boolean tmp = isSecureEnabled;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = isSecureEnabled;
                if (null == tmp) {
                    ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return true;
                    }
                    boolean prop = service.getBoolProperty(SECURE_CONNECTIONS_KEY, true);
                    tmp = new Boolean(prop);
                    isSecureEnabled = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    static final String PROTOCOLS_KEY = "com.openexchange.ssl.protocols";

    static final String PROTOCOLS_DEFAULTS = "SSLv3, TLSv1.2";

    private static volatile String[] protocols;

    public static String[] supportedProtocols() {
        String[] tmp = protocols;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = protocols;
                if (null == tmp) {
                    ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return Strings.splitByComma(PROTOCOLS_DEFAULTS);
                    }
                    String prop = service.getProperty(PROTOCOLS_KEY, PROTOCOLS_DEFAULTS);
                    tmp = Strings.splitByComma(prop);
                    protocols = tmp;
                }
            }
        }
        return tmp;
    }

    static final String CIPHERS_KEY = "com.openexchange.ssl.ciphers";

    static final String CIPHERS_DEFAULTS = "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_ECDHE_ECDSA_WITH_RC4_128_SHA, TLS_ECDHE_RSA_WITH_RC4_128_SHA, SSL_RSA_WITH_RC4_128_SHA, TLS_ECDH_ECDSA_WITH_RC4_128_SHA, TLS_ECDH_RSA_WITH_RC4_128_SHA, TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_RC4_128_MD5, TLS_EMPTY_RENEGOTIATION_INFO_SCSV";

    private static volatile String[] ciphers;

    public static String[] supportedCiphers() {
        String[] tmp = ciphers;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = ciphers;
                if (null == tmp) {
                    ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return Strings.splitByComma(CIPHERS_DEFAULTS);
                    }
                    String prop = service.getProperty(CIPHERS_KEY, CIPHERS_DEFAULTS);
                    tmp = Strings.splitByComma(prop);
                    ciphers = tmp;
                }
            }
        }
        return tmp;
    }

    static final String HOSTNAME_VERIFICATION_ENABLED_KEY = "com.openexchange.ssl.hostname.verification.enabled";

    private static volatile Boolean verifyHostname;

    public static boolean isVerifyHostname() {
        Boolean tmp = verifyHostname;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = verifyHostname;
                if (null == tmp) {
                    ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return true;
                    }
                    boolean prop = service.getBoolProperty(HOSTNAME_VERIFICATION_ENABLED_KEY, true);
                    tmp = new Boolean(prop);
                    verifyHostname = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    static final String TRUSTSTORE_WHITELIST_KEY = "com.openexchange.ssl.truststore.whitelist";

    static final String TRUSTSTORE_WHITELIST_DEFAULT = "127.0.0.1-127.255.255.255,localhost";

    private static volatile HostList whitelistedHosts;

    private static HostList whitelistedHosts() {
        HostList tmp = whitelistedHosts;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = whitelistedHosts;
                if (null == tmp) {
                    ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return HostList.valueOf(TRUSTSTORE_WHITELIST_DEFAULT);
                    }
                    String prop = service.getProperty(TRUSTSTORE_WHITELIST_KEY, TRUSTSTORE_WHITELIST_DEFAULT);
                    if (Strings.isNotEmpty(prop)) {
                        prop = prop.trim();
                    }
                    tmp = HostList.valueOf(prop);
                    whitelistedHosts = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Checks if specified host name is white-listed.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostName The host name; either a machine name or a textual representation of its IP address
     * @return <code>true</code> if white-listed; otherwise <code>false</code>
     */
    public static boolean isWhitelisted(String hostName) {
        if (Strings.isEmpty(hostName)) {
            return false;
        }
        return whitelistedHosts().contains(hostName);
    }

    public static void reload() {
        whitelistedHosts = null;
        isSecureEnabled = null;
    }

    //***************************************/

    private final String propertyName;

    private String defaultValue;

    private boolean defaultBoolValue;

    private SSLProperties(final String propertyName, final String defaultValue) {
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
    }

    private SSLProperties(final String propertyName, final boolean defaultBoolValue) {
        this.propertyName = propertyName;
        this.defaultBoolValue = defaultBoolValue;
    }

    public String getName() {
        return propertyName;
    }

    public String getDefault() {
        return defaultValue;
    }

    public boolean getDefaultBoolean() {
        return defaultBoolValue;
    }
}
