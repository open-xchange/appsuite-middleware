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

package com.openexchange.net.ssl.internal;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.osgi.Services;

/**
 * {@link DefaultTrustManager}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class DefaultTrustManager extends AbstractTrustManager {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultTrustManager.class);

    /**
     * Creates a new {@link DefaultTrustManager} instance.
     *
     * @return The new instance or <code>null</code> if initialization failed
     */
    public static DefaultTrustManager newInstance() {
        TrustManagerAndParameters managerAndParameters = initDefaultTrustManager();
        if (null == managerAndParameters) {
            return null;
        }
        return new DefaultTrustManager(managerAndParameters.trustManager);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link CustomTrustManager}.
     */
    private DefaultTrustManager(X509ExtendedTrustManager trustManager) {
        super(trustManager);
    }

    /**
     * Initialises the {@link CustomTrustManager}
     *
     * @return An {@link X509ExtendedTrustManager}
     */
    private static TrustManagerAndParameters initDefaultTrustManager() {
        boolean useDefaultTruststore;
        SSLConfigurationService sslConfigService = Services.getService(SSLConfigurationService.class);
        {
            if (null == sslConfigService) {
                LOG.warn("Absent service {}. Assuming default JVM truststore is supposed to be used.", SSLConfigurationService.class.getName());
                useDefaultTruststore = true;
            } else {
                useDefaultTruststore = sslConfigService.isDefaultTruststoreEnabled();
            }
        }

        if (false == useDefaultTruststore) {
            LOG.info("Using default JVM truststore is disabled.");
            return null;
        }

        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); // Using null here initializes the TMF with the default trust store.

            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509ExtendedTrustManager) {
                    return new TrustManagerAndParameters((X509ExtendedTrustManager) tm);
                }
            }
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            LOG.error("Unable to initialize default truststore.", e);
            //TODO re-throw or OXException?
        }

        return null;
    }
}
