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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.net.ssl.osgi.Services;

/**
 * {@link CustomTrustManager}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public class CustomTrustManager extends AbstractTrustManager {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomTrustManager.class);

    /**
     * Creates a new {@link CustomTrustManager} instance.
     *
     * @return The new instance or <code>null</code> if initialization failed
     */
    public static CustomTrustManager newInstance() {
        TrustManagerAndParameters managerAndParameters = initCustomTrustManager();
        if (null == managerAndParameters) {
            return null;
        }
        return new CustomTrustManager(managerAndParameters.trustManager);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link CustomTrustManager}.
     */
    private CustomTrustManager(X509ExtendedTrustManager trustManager) {
        super(trustManager);
    }

    /**
     * Initialises the {@link CustomTrustManager}
     *
     * @return An {@link X509ExtendedTrustManager}
     */
    private static TrustManagerAndParameters initCustomTrustManager() {
        SSLConfigurationService sslConfigService = Services.getService(SSLConfigurationService.class);
        if (null == sslConfigService) {
            LOG.warn("Absent service {}. Assuming custom truststore is NOT supposed to be used.", SSLConfigurationService.class.getName());
            return null;
        }

        boolean useCustomTruststore = sslConfigService.isCustomTruststoreEnabled();
        if (false == useCustomTruststore) {
            LOG.info("Using custom truststore is disabled.");
            return null;
        }

        String trustStoreFile = sslConfigService.getCustomTruststoreLocation();
        if (Strings.isEmpty(trustStoreFile)) {
            LOG.error("Cannot load custom truststore file from empty location.");
            return null;
        }

        String password = sslConfigService.getCustomTruststorePassword();
        if (null == password) {
            LOG.error("Cannot load custom truststore file with empty password from location \"{}\"", trustStoreFile);
            return null;
        }

        File file = new File(trustStoreFile);
        if (!file.exists()) {
            LOG.error("Cannot load custom truststore from location \"{}\". The file does not exist.", trustStoreFile);
            return null;
        }

        try (InputStream in = new FileInputStream(file)) {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(in, password.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509ExtendedTrustManager) {
                    return new TrustManagerAndParameters((X509ExtendedTrustManager) tm);
                }
            }
        } catch (IOException e) {
            LOG.error("Unable to read custom truststore file from {}", file.getAbsolutePath(), e);
            //TODO re-throw or OXException?
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            LOG.error("Unable to initialize custom truststore file from {}", file.getAbsolutePath(), e);
            //TODO re-throw or OXException?
        }

        return null;
    }
}
