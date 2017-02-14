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
            LOG.warn("Absent service " + SSLConfigurationService.class.getName() + ". Assuming custom truststore is NOT supposed to be used.");
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
            LOG.error("Cannot load custom truststore file with empty password from location \"" + trustStoreFile + "\"");
            return null;
        }

        File file = new File(trustStoreFile);
        if (!file.exists()) {
            LOG.error("Cannot load custom truststore from location \"" + trustStoreFile + "\". The file does not exist.");
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
            LOG.error("Unable to read custom truststore file from " + file.getAbsolutePath(), e);
            //TODO re-throw or OXException?
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            LOG.error("Unable to initialize custom truststore file from " + file.getAbsolutePath(), e);
            //TODO re-throw or OXException?
        }

        return null;
    }
}
