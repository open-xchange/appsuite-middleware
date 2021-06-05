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

package com.openexchange.database.internal;

import java.security.KeyStore;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadables;
import com.openexchange.database.internal.Configuration.ConfigurationDifference;
import com.openexchange.database.internal.ConfigurationListener.ConfigDBListener;
import com.openexchange.database.internal.reloadable.ConnectionReloader;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentHashSet;
import com.openexchange.java.ConfigAwareKeyStore;

/**
 * {@link ConnectionReloaderImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ConnectionReloaderImpl implements ForcedReloadable, ConnectionReloader {

    private static final String USE_SSL = "useSSL";

    private static final String CLIENT_CERT_PATH_NAME     = "clientCertificateKeyStoreUrl";
    private static final String CLIENT_CERT_PASSWORD_NAME = "clientCertificateKeyStorePassword";
    private static final String CLIENT_CERT_TYPE          = "clientCertificateKeyStoreType";

    private static final String TRUST_CERT_PATH_NAME     = "trustCertificateKeyStoreUrl";
    private static final String TRUST_CERT_PASSWORD_NAME = "trustCertificateKeyStorePassword";
    private static final String TRUST_CERT_TYPE          = "trustCertificateKeyStoreType";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionReloaderImpl.class);

    private final ConcurrentHashSet<ConfigurationListener> listerners;

    private final ConcurrentHashMap<String, ConfigAwareKeyStore> stores;

    private volatile Configuration configuration;

    /**
     * Initializes a new {@link ConnectionReloaderImpl}.
     *
     * @param configuration The {@link Configuration} for connections
     */
    public ConnectionReloaderImpl(Configuration configuration) {
        super();
        listerners = new ConcurrentHashSet<>(4);
        stores = new ConcurrentHashMap<>(4);
        this.configuration = configuration;

        stores.put("CAStore", new ConfigAwareKeyStore(TRUST_CERT_PATH_NAME, TRUST_CERT_PASSWORD_NAME, TRUST_CERT_TYPE));
        stores.put("ClientStore", new ConfigAwareKeyStore(CLIENT_CERT_PATH_NAME, CLIENT_CERT_PASSWORD_NAME, CLIENT_CERT_TYPE));

        // Ignore listeners on start up
        loadKeyStores(configuration);
    }

    @Override
    public boolean loadKeyStores(Configuration configuration) {
        // Do we need to do something?
        if (false == isSSL(configuration)) {
            return false;
        }

        boolean retval = false;
        for (Entry<String, ConfigAwareKeyStore> entry : stores.entrySet()) {
            try {
                retval |= entry.getValue().reloadStore(configuration.getJdbcProps());
            } catch (Exception e) {
                LOGGER.error("Unable to load keystore!", e);
            }
        }

        return retval;
    }

    private boolean isSSL(Configuration configuration) {
        return Boolean.parseBoolean(configuration.getJdbcProps().getProperty(USE_SSL));
    }

    @Override
    public boolean setConfigurationListener(ConfigurationListener listener) {
        return null == listener ? false : listerners.add(listener);
    }

    @Override
    public boolean removeConfigurationListener(int poolId) {
        for (Iterator<ConfigurationListener> iterator = listerners.iterator(); iterator.hasNext();) {
            if (poolId == iterator.next().getPoolId()) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            Configuration configuration = new Configuration();
            configuration.readConfiguration(configService, false);
            // Check if key store was modified or configuration was changed and we need to notify
            boolean keyStoreUpdate = loadKeyStores(configuration);
            ConfigurationDifference configDifference = this.configuration.getDifferenceTo(configuration);
            if (keyStoreUpdate || configDifference.anythingDifferent()) {
                configuration.logCurrentPoolConfig();
                JdbcPropertiesImpl.getInstance().setJdbcProperties(configuration.getJdbcProps());
                notify(keyStoreUpdate, configuration, configDifference);
                this.configuration = configuration;
            }
        } catch (OXException e) {
            LOGGER.error("Unable to reinitialze configuration!", e);
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.getInterestsForAll();
    }

    /**
     * Notifies the {@link ConfigurationListener}. Looks up which properties has changed and notifies only relevant
     *
     * @param keyStoreUpdate <code>true</code> if a {@link KeyStore} was updated
     * @param configuration The new {@link Configuration}
     * @param configDifference The detected differences
     */
    private void notify(boolean keyStoreUpdate, Configuration configuration, ConfigurationDifference configDifference) {
        if (keyStoreUpdate || configDifference.areJdbcPropsDifferent()) {
            listerners.stream().sorted().forEach(l -> l.notify(configuration));
        } else if (configDifference.areConfigDbReadPropsDifferent() || configDifference.areConfigDbWritePropsDifferent()) {
            listerners.stream().filter(l -> ConfigDBListener.class.isAssignableFrom(l.getClass())).sorted().forEach(l -> l.notify(configuration));
        }
    }

}
