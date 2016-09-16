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

package com.openexchange.net.ssl.config;

import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.openexchange.net.ssl.apache.DefaultHostnameVerifier;
import com.openexchange.net.ssl.osgi.Services;

/**
 * {@link SSLProperties} include configurations made by the administrator. This means that only server wide configurations can be found here. ConfigCascade properities should not be added here.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public enum SSLProperties {

    /* Enables logging SSL details. Have a look at http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/ReadDebug.html for more details. */
    SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED(SSLProperties.SECURE_CONNECTIONS_DEBUG_LOGS_KEY, false),

    DEFAULT_TRUSTSTORE_ENABLED(SSLProperties.DEFAULT_TRUSTSTORE_ENABLED_KEY, true),

    CUSTOM_TRUSTSTORE_ENABLED(SSLProperties.CUSTOM_TRUSTSTORE_ENABLED_KEY, false),

    CUSTOM_TRUSTSTORE_LOCATION(SSLProperties.CUSTOM_TRUSTSTORE_PATH_KEY, SSLProperties.EMPTY_STRING),

    CUSTOM_TRUSTSTORE_PASSWORD(SSLProperties.CUSTOM_TRUSTSTORE_PASSWORD_KEY, SSLProperties.EMPTY_STRING),

    ;

    static final String EMPTY_STRING = "";

    static final String SECURE_CONNECTIONS_DEBUG_LOGS_KEY = "com.openexchange.net.ssl.debug.logs";

    static final String DEFAULT_TRUSTSTORE_ENABLED_KEY = "com.openexchange.net.ssl.default.truststore.enabled";

    static final String CUSTOM_TRUSTSTORE_ENABLED_KEY = "com.openexchange.net.ssl.custom.truststore.enabled";

    static final String CUSTOM_TRUSTSTORE_PATH_KEY = "com.openexchange.net.ssl.custom.truststore.path";

    static final String CUSTOM_TRUSTSTORE_PASSWORD_KEY = "com.openexchange.net.ssl.custom.truststore.password";

    //---------- Reloadable Properties - not CC aware -------------//

    static final String TRUST_LEVEL_KEY = "com.openexchange.net.ssl.trustlevel";

    static final String TRUST_LEVEL_DEFAULT = "none";

    private static volatile TrustLevel trustLevel;

    public static TrustLevel trustLevel() {
        TrustLevel tmp = trustLevel;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = trustLevel;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        org.slf4j.LoggerFactory.getLogger(SSLProperties.class).info("ConfigurationService not yet available. Use default value for 'com.openexchange.net.ssl.trustlevel'.");
                        return TrustLevel.find(TRUST_LEVEL_DEFAULT);
                    }
                    String prop = service.getProperty(TRUST_LEVEL_KEY, TRUST_LEVEL_DEFAULT);
                    tmp = TrustLevel.find(prop);
                    trustLevel = tmp;
                }
            }
        }
        return tmp;
    }

    static final String PROTOCOLS_KEY = "com.openexchange.net.ssl.protocols";

    static final String PROTOCOLS_DEFAULT = "TLSv1, TLSv1.1, TLSv1.2";

    private static volatile String[] protocols;

    public static String[] supportedProtocols() {
        String[] tmp = protocols;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = protocols;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        org.slf4j.LoggerFactory.getLogger(SSLProperties.class).info("ConfigurationService not yet available. Use default value for 'com.openexchange.net.ssl.protocols'.");
                        return Strings.splitByComma(PROTOCOLS_DEFAULT);
                    }
                    String prop = service.getProperty(PROTOCOLS_KEY, PROTOCOLS_DEFAULT);
                    tmp = Strings.splitByComma(prop);
                    protocols = tmp;
                }
            }
        }
        return tmp;
    }

    static final String CIPHERS_KEY = "com.openexchange.net.ssl.ciphersuites";

    static final String CIPHERS_DEFAULT = "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_ECDHE_ECDSA_WITH_RC4_128_SHA, TLS_ECDHE_RSA_WITH_RC4_128_SHA, SSL_RSA_WITH_RC4_128_SHA, TLS_ECDH_ECDSA_WITH_RC4_128_SHA, TLS_ECDH_RSA_WITH_RC4_128_SHA, TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_RC4_128_MD5, TLS_EMPTY_RENEGOTIATION_INFO_SCSV";

    private static volatile String[] ciphers;

    public static String[] supportedCipherSuites() {
        String[] tmp = ciphers;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = ciphers;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        org.slf4j.LoggerFactory.getLogger(SSLProperties.class).info("ConfigurationService not yet available. Use default value for 'com.openexchange.net.ssl.ciphersuites'.");
                        return Strings.splitByComma(CIPHERS_DEFAULT);
                    }
                    String prop = service.getProperty(CIPHERS_KEY, CIPHERS_DEFAULT);
                    tmp = Strings.splitByComma(prop);
                    ciphers = tmp;
                }
            }
        }
        return tmp;
    }

    static final String HOSTNAME_VERIFICATION_ENABLED_KEY = "com.openexchange.net.ssl.hostname.verification.enabled";

    private static volatile Boolean verifyHostname;

    public static boolean isVerifyHostname() {
        Boolean tmp = verifyHostname;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = verifyHostname;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        org.slf4j.LoggerFactory.getLogger(SSLProperties.class).info("ConfigurationService not yet available. Use default value for 'com.openexchange.net.ssl.hostname.verification.enabled'.");
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

    static final String TRUSTSTORE_WHITELIST_KEY = "com.openexchange.net.ssl.whitelist";

    static final String TRUSTSTORE_WHITELIST_DEFAULT = "127.0.0.1-127.255.255.255,localhost";

    private static volatile HostList whitelistedHosts;

    private static HostList whitelistedHosts() {
        HostList tmp = whitelistedHosts;
        if (null == tmp) {
            synchronized (SSLProperties.class) {
                tmp = whitelistedHosts;
                if (null == tmp) {
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        org.slf4j.LoggerFactory.getLogger(SSLProperties.class).info("ConfigurationService not yet available. Use default value for 'com.openexchange.net.ssl.whitelist'.");
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

    /**
     * Checks if one of the specified host names is white-listed.
     * <p>
     * The host names can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostNames The host names as an array; either a machine name or a textual representation of its IP address
     * @return <code>true</code> if at least one of the hosts is white-listed; otherwise <code>false</code>
     */
    public static boolean isWhitelisted(String... hostNames) {
        for (String hostName : hostNames) {
            boolean whitelisted = isWhitelisted(hostName);
            if (whitelisted) {
                return true;
            }
        }
        return false;
    }

    //---------- End of reloadable properties -------------//

    public static void reload() {
        trustLevel = null;
        protocols = null;
        ciphers = null;
        whitelistedHosts = null;
        verifyHostname = null;

        reinit();
    }

    private static void reinit() {
        if (isVerifyHostname()) {
            HttpsURLConnection.setDefaultHostnameVerifier(new DefaultHostnameVerifier());
        } else {
            HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
        }
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
