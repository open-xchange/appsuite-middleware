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

package com.openexchange.net.ssl.config.impl.internal;

import static com.openexchange.net.ssl.config.impl.internal.CipherSuiteName.nameFor;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.openexchange.net.ssl.config.TrustLevel;

/**
 * {@link SSLProperties} include configurations made by the administrator. This means that only server wide configurations can be found here. ConfigCascade properities should not be added here.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public enum SSLProperties {

    /* Enables logging SSL details. Have a look at http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/ReadDebug.html for more details. */
    SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED(SSLProperties.SECURE_CONNECTIONS_DEBUG_LOGS_KEY, false),

    DEFAULT_TRUSTSTORE_ENABLED(SSLProperties.DEFAULT_TRUSTSTORE_ENABLED_KEY, true),

    CUSTOM_TRUSTSTORE_ENABLED(SSLProperties.CUSTOM_TRUSTSTORE_ENABLED_KEY, false),

    CUSTOM_TRUSTSTORE_LOCATION(SSLProperties.CUSTOM_TRUSTSTORE_PATH_KEY, SSLProperties.EMPTY_STRING),

    CUSTOM_TRUSTSTORE_PASSWORD(SSLProperties.CUSTOM_TRUSTSTORE_PASSWORD_KEY, SSLProperties.EMPTY_STRING),

    HOSTNAME_VERIFICATION_ENABLED(SSLProperties.HOSTNAME_VERIFICATION_ENABLED_KEY, true),

    ;

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

    // ------------------------------------------------------------------------------------------------------------

    private static final String EMPTY_STRING = "";

    private static final AtomicReference<List<String>> DEFAULT_CIPHER_SUITES_REFERENCE = new AtomicReference<List<String>>(null);
    private static final AtomicReference<List<String>> SUPPORTED_CIPHER_SUITES_REFERENCE = new AtomicReference<List<String>>(null);
    private static final AtomicReference<List<String>> SUPPORTED_PROTOCOLS_REFERENCE = new AtomicReference<List<String>>(null);

    /**
     * Initializes the JVM's supported cipher suites that are the names of the cipher suites, which could be enabled for use on an SSL connection.
     */
    public static void initJvmDefaults() {
        javax.net.ssl.SSLSocketFactory sslSocketFactory = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();

        String[] jvmSupported = sslSocketFactory.getSupportedCipherSuites();
        SUPPORTED_CIPHER_SUITES_REFERENCE.set(ImmutableList.<String> copyOf(jvmSupported));

        String[] jvmDefaults = sslSocketFactory.getDefaultCipherSuites();
        DEFAULT_CIPHER_SUITES_REFERENCE.set(ImmutableList.<String> copyOf(jvmDefaults));

        // Auto-detect protocols
        List<String> protocols = new LinkedList<String>();
        // Allow the specification of a specific provider (or set?)
        for (Provider provider : Security.getProviders()) {
            for (Object prop : provider.keySet()) {
                String key = (String) prop;
                if (key.startsWith("SSLContext.") && !key.equals("SSLContext.Default") && key.matches(".*[0-9].*")) {
                    protocols.add(key.substring("SSLContext.".length()));
                } else if (key.startsWith("Alg.Alias.SSLContext.") && key.matches(".*[0-9].*")) {
                    protocols.add(key.substring("Alg.Alias.SSLContext.".length()));
                }
            }
        }
        Collections.sort(protocols); // Should give us a nice sort-order by default
        SUPPORTED_PROTOCOLS_REFERENCE.set(ImmutableList.<String> copyOf(protocols));
    }

    /**
     * Gets the names of the cipher suites which could be enabled for use on an SSL connection.
     * <p>
     * Normally, only a subset of these will actually be enabled by default, since this list may include cipher suites which do not meet quality of service requirements for those defaults.
     * Such cipher suites are useful in specialized applications.
     *
     * @return The list of cipher suites (or <code>null</code> if not initialized)
     */
    public static List<String> getSupportedCipherSuites() {
        return SUPPORTED_CIPHER_SUITES_REFERENCE.get();
    }

    /**
     * Gets the list of cipher suites which are enabled by default.
     * <p>
     * Unless a different list is enabled, handshaking on an SSL connection will use one of these cipher suites.
     * The minimum quality of service for these defaults requires confidentiality protection and server authentication (that is, no anonymous cipher suites).
     *
     * @return The list of cipher suites (or <code>null</code> if not initialized)
     */
    public static List<String> getDefaultCipherSuites() {
        return DEFAULT_CIPHER_SUITES_REFERENCE.get();
    }

    /**
     * Gets the list of protocols which could be enabled for use on an SSL connection.
     *
     * @return The list of protocols (or <code>null</code> if not initialized)
     */
    public static List<String> getSupportedProtocols() {
        return SUPPORTED_PROTOCOLS_REFERENCE.get();
    }

    static final String SECURE_CONNECTIONS_DEBUG_LOGS_KEY = "com.openexchange.net.ssl.debug.logs";

    static final String DEFAULT_TRUSTSTORE_ENABLED_KEY = "com.openexchange.net.ssl.default.truststore.enabled";

    static final String CUSTOM_TRUSTSTORE_ENABLED_KEY = "com.openexchange.net.ssl.custom.truststore.enabled";

    static final String CUSTOM_TRUSTSTORE_PATH_KEY = "com.openexchange.net.ssl.custom.truststore.path";

    static final String CUSTOM_TRUSTSTORE_PASSWORD_KEY = "com.openexchange.net.ssl.custom.truststore.password";

    static final String HOSTNAME_VERIFICATION_ENABLED_KEY = "com.openexchange.net.ssl.hostname.verification.enabled";

    //---------- Reloadable Properties -------------//

    static final String TRUST_LEVEL_KEY = "com.openexchange.net.ssl.trustlevel";

    static final String TRUST_LEVEL_DEFAULT = "all";

    /**
     * Gets the configured trust level.
     *
     * @param service The service to use
     * @return The trust level
     */
    public static TrustLevel trustLevel(ConfigurationService service) {
        if (null == service) {
            org.slf4j.LoggerFactory.getLogger(SSLProperties.class).info("ConfigurationService not yet available. Use default value for 'com.openexchange.net.ssl.trustlevel'.");
            return TrustLevel.TRUST_ALL;
        }
        String prop = service.getProperty(TRUST_LEVEL_KEY, TRUST_LEVEL_DEFAULT);
        return TrustLevel.find(prop);
    }

    /**
     * Creates a new <code>RestrictedConfig</code> instance reading configuration from specified service.
     *
     * @param service The service to use
     * @return The created <code>RestrictedConfig</code> instance
     */
    public static RestrictedConfig newConfig(ConfigurationService service) {
        String[] protocols;
        {
            String prop = service.getProperty(PROTOCOLS_KEY, PROTOCOLS_DEFAULT);
            protocols = Strings.splitByComma(prop);
        }

        String[] ciphers;
        {
            String prop = service.getProperty(CIPHERS_KEY);
            String[] tmp = null == prop ? getJvmApplicableCipherSuites(CIPHERS_DEFAULT_DESIRED, true) : getJvmApplicableCipherSuites(Arrays.asList(Strings.splitByComma(prop)), false);
            ciphers = tmp;
        }

        HostList whitelistedHosts;
        {
            String prop = service.getProperty(TRUSTSTORE_WHITELIST_KEY, TRUSTSTORE_WHITELIST_DEFAULT);
            if (Strings.isNotEmpty(prop)) {
                prop = prop.trim();
            }
            HostList tmp = HostList.valueOf(prop);
            whitelistedHosts = tmp;
        }

        return new RestrictedConfig(protocols, ciphers, whitelistedHosts);
    }

    static final String PROTOCOLS_KEY = "com.openexchange.net.ssl.protocols";

    static final String PROTOCOLS_DEFAULT = "TLSv1, TLSv1.1, TLSv1.2";

    static final String CIPHERS_KEY = "com.openexchange.net.ssl.ciphersuites";

    static final List<String> CIPHERS_DEFAULT_DESIRED = ImmutableList.<String> builder().add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV").build();

    private static String[] getJvmApplicableCipherSuites(List<String> desiredCipherSuites, boolean allowDefaultOnes) {
        // Get the cipher suites actually supported by running JVM
        List<String> jvmDefaults = SUPPORTED_CIPHER_SUITES_REFERENCE.get();
        Map<CipherSuiteName, String> supportedCipherSuites = new HashMap<>(jvmDefaults.size());
        for (String jvmDefaultCipherSuite : jvmDefaults) {
            supportedCipherSuites.put(nameFor(jvmDefaultCipherSuite), jvmDefaultCipherSuite);
        }

        // Grab the desired ones from JVM's default cipher suites
        List<String> defaultCipherSuites = new ArrayList<>(desiredCipherSuites.size());
        for (String desiredCipherSuite : desiredCipherSuites) {
            String supportedCipherSuite = supportedCipherSuites.get(nameFor(desiredCipherSuite));
            if (null != supportedCipherSuite) {
                defaultCipherSuites.add(desiredCipherSuite);
            }
        }

        if (defaultCipherSuites.isEmpty()) {
            // None of desired cipher suites is supported by JVM...
            if (allowDefaultOnes) {
                // Just use JVM default ones
                return jvmDefaults.toArray(new String[jvmDefaults.size()]);
            }

            // Illegal configuration
            throw new IllegalStateException("None of the desired cipher suites is actually supported by the Java Virtual Machine. Please list supported ones in value for 'com.openexchange.net.ssl.ciphersuites'.");
        }

        return defaultCipherSuites.toArray(new String[defaultCipherSuites.size()]);
    }

    static final String TRUSTSTORE_WHITELIST_KEY = "com.openexchange.net.ssl.whitelist";

    static final String TRUSTSTORE_WHITELIST_DEFAULT = "127.0.0.1-127.255.255.255,localhost";

}
