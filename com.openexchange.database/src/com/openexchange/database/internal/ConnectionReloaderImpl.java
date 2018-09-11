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

package com.openexchange.database.internal;

import java.security.KeyStore;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadables;
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
            configuration.readConfiguration(configService);
            // Check if key store was modified or configuration was changed and we need to notify
            boolean keyStoreUpdate = loadKeyStores(configuration);
            if (keyStoreUpdate || false == this.configuration.equals(configuration)) {
                notify(keyStoreUpdate, configuration);
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
     */
    private void notify(boolean keyStoreUpdate, Configuration configuration) {
        if (keyStoreUpdate || checkForChangedProperties(this.configuration.getJdbcProps(), configuration.getJdbcProps())) {
            listerners.stream().sorted().forEach(l -> l.notify(configuration));
        } else if (checkForChangedProperties(this.configuration.getConfigDbReadProps(), configuration.getConfigDbReadProps()) || checkForChangedProperties(this.configuration.getConfigDbWriteProps(), configuration.getConfigDbWriteProps())) {
            listerners.stream().filter(l -> ConfigDBListener.class.isAssignableFrom(l.getClass())).sorted().forEach(l -> l.notify(configuration));
        }
    }

    private boolean checkForChangedProperties(Properties oldProperties, Properties newProperties) {
        if (oldProperties.size() != newProperties.size()) {
            return true;
        }
        for (Entry<Object, Object> property : oldProperties.entrySet()) {
            String newProperty = newProperties.getProperty(String.valueOf(property.getKey()));
            if (null == newProperty) {
                if (null != property.getValue()) {
                    return true;
                }
            } else if (false == newProperty.equals(property.getValue())) {
                return true;
            }
        }
        return false;
    }

}
