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

package com.openexchange.hazelcast.configuration.internal;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.instance.BuildInfoProvider;
import com.hazelcast.nio.ssl.SSLContextFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.java.ConfigAwareKeyStore;
import com.openexchange.java.Strings;

/**
 * {@link HazelcastSSLFactory}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
class HazelcastSSLFactory implements SSLContextFactory, Reloadable {

    private static final String SSL_PROTOCOLS = "com.openexchange.hazelcast.ssl.protocols";

    private static final String TRUST_STORE    = "com.openexchange.hazelcast.ssl.trustStore";
    private static final String TRUST_PASSWORD = "com.openexchange.hazelcast.ssl.trustStorePassword";
    private static final String TRUST_TYPE     = "com.openexchange.hazelcast.ssl.trustManagerAlgorithm";

    private static final String KEY_STORE    = "com.openexchange.hazelcast.ssl.keyStore";
    private static final String KEY_PASSWORD = "com.openexchange.hazelcast.ssl.keyStorePassword";
    private static final String KEY_TYPE     = "com.openexchange.hazelcast.ssl.keyManagerAlgorithm";

    /* Hazelcast only accepts JKS key stores */
    private static final String TYPE = "JKS";

    /** All necessary properties for Hazelcast to run SSL. */
    private static final String[] SSL_PROPERTIES = new String[] {
        SSL_PROTOCOLS, TRUST_STORE, TRUST_PASSWORD, TRUST_TYPE, KEY_STORE, KEY_PASSWORD, KEY_TYPE
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastSSLFactory.class);

    private SSLContext sslContext;

    private final String protocol;

    private final ReentrantLock lock = new ReentrantLock(true);

    /**
     * Initializes a new {@link HazelcastSSLFactory}.
     * 
     * @param configService The {@link ConfigurationService}
     * 
     */
    public HazelcastSSLFactory(ConfigurationService configService) {
        super();
        // Find out protocol
        String protocol = null;
        String[] candidates = Strings.splitByComma(configService.getProperty(SSL_PROTOCOLS, "TLS,TLSv1,TLSv1.1,SSL,SSLv2,SSLv3"));
        for (int i = candidates.length - 1; i >= 0; i--) {
            try {
                sslContext = SSLContext.getInstance(candidates[i]);
                protocol = candidates[i];
                break;
            } catch (Throwable e) {
                LOGGER.info("Didn't find SSLContext for {}", candidates[i], e);
            }
        }
        this.protocol = protocol;
    }

    @Override
    public SSLContext getSSLContext() {
        lock.lock();
        try {
            return sslContext;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void init(Properties properties) throws Exception {
        lock.lock();
        try {
            if (Strings.isEmpty(protocol)) {
                LOGGER.debug("No SSLContext loaded. Nothing to initialize.");
                return;
            }
            if (properties.isEmpty()) {
                LOGGER.debug("Can't initialize SSLContext without properties");
                return;
            }
            ConfigAwareKeyStore trustStore = new ConfigAwareKeyStore(properties, TRUST_STORE, TRUST_PASSWORD, TYPE);
            ConfigAwareKeyStore keyStore = new ConfigAwareKeyStore(properties, KEY_STORE, KEY_PASSWORD, TYPE);

            loadKeyStore(properties, trustStore);
            loadKeyStore(properties, keyStore);

            // Initialize SSLContext
            try {
                TrustManagerFactory trustManagerFactory = null;
                KeyManagerFactory keyManagerFactory = null;

                if (trustStore.isConfigured()) {
                    trustManagerFactory = TrustManagerFactory.getInstance(properties.getProperty(TRUST_TYPE, TrustManagerFactory.getDefaultAlgorithm()));
                    trustManagerFactory.init(trustStore.getKeyStore());
                }

                if (keyStore.isConfigured()) {
                    keyManagerFactory = KeyManagerFactory.getInstance(properties.getProperty(KEY_TYPE, KeyManagerFactory.getDefaultAlgorithm()));
                    keyManagerFactory.init(keyStore.getKeyStore(), null != properties.getProperty(KEY_PASSWORD) ? properties.getProperty(KEY_PASSWORD).toCharArray() : null);
                }

                sslContext.init(null == keyManagerFactory ? new KeyManager[] {} : keyManagerFactory.getKeyManagers(), null == trustManagerFactory ? new TrustManager[] {} : trustManagerFactory.getTrustManagers(), null);
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
                LOGGER.error("Unable to initialize SSLContext for Hazelcast.", e);
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    private void loadKeyStore(Properties properties, ConfigAwareKeyStore store) {
        try {
            store.reloadStore(properties);
        } catch (Exception e) {
            LOGGER.error("Unable to load key stoer {}", store, e);
        }
    }

    /**
     * Get all necessary properties for a SSL configuration
     * 
     * @param configService The {@link ConfigurationService} to get the properties from
     * @return The {@link #SSL_PROPERTIES} as {@link Properties}
     */
    Properties getPropertiesFromService(ConfigurationService configService) {
        Properties properties = new Properties();
        for (String property : SSL_PROPERTIES) {
            String value = configService.getProperty(property);
            if (Strings.isNotEmpty(value)) {
                properties.setProperty(property, value);
            }
        }
        return properties;
    }

    /* ############### Reloadable ############### */

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        if (BuildInfoProvider.getBuildInfo().isEnterprise()) {
            try {
                // Locks itself
                init(getPropertiesFromService(configService));
            } catch (Exception e) {
                LOGGER.error("Unable to reload {}.", HazelcastSSLFactory.class.getSimpleName(), e);
            }
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(SSL_PROPERTIES);
    }

}
