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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.ssl.TrustedSSLSocketFactory;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;

/**
 * {@link SSLSocketFactoryInitializer}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class SSLSocketFactoryInitializer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SSLSocketFactoryInitializer.class);

    public static SocketFactory init(ConfigurationService configService) {
        if (configService.getBoolProperty(SSLProperty.SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED.getName(), SSLProperty.SECURE_CONNECTIONS_DEBUG_LOGS_ENABLED.getDefaultBoolean())) {
            System.setProperty("javax.net.debug", "ssl:record");
            //            System.setProperty("javax.net.debug", "ssl");
            LOG.info("Enabeld SSL debug logging.");
        }

        if (!configService.getBoolProperty(SSLProperty.SECURE_CONNECTIONS_ENABLED.getName(), SSLProperty.SECURE_CONNECTIONS_ENABLED.getDefaultBoolean())) {
            return TrustAllSSLSocketFactory.getDefault();
        }

        // Taken from http://stackoverflow.com/questions/24555890/using-a-custom-truststore-in-java-as-well-as-the-default-one
        List<X509TrustManager> trustManagers = new ArrayList<>();
        X509TrustManager defaultTrustManager = initDefaultTrustManager(configService);
        if (defaultTrustManager != null) {
            X509TrustManager defaultTm = new DefaultTrustManager(defaultTrustManager);
            trustManagers.add(defaultTm);
        }

        X509TrustManager customTrustManager = initCustomTrustManager(configService);
        if (customTrustManager != null) {
            X509TrustManager customTm = new CustomTrustManager(customTrustManager);
            trustManagers.add(customTm);
        }
        TrustManager[] trustManagersArray = new TrustManager[trustManagers.size()];
        trustManagers.toArray(trustManagersArray);

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagersArray, null);
            // You don't have to set this as the default context, it depends on the library you're using.
            SSLContext.setDefault(sslContext);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Cannot retrieve SSL context.", e);
        } catch (KeyManagementException e) {
            LOG.error("Unable to init SSL context.", e);
        }

        boolean verifyHostname = configService.getBoolProperty(SSLProperty.HOSTNAME_VERIFICATION_ENABLED.getName(), SSLProperty.HOSTNAME_VERIFICATION_ENABLED.getDefaultBoolean());
        if (verifyHostname) {
            HttpsURLConnection.setDefaultHostnameVerifier(new DefaultHostnameVerifier());
        }
        return TrustedSSLSocketFactory.getDefault();
    }

    private static X509TrustManager initDefaultTrustManager(ConfigurationService configService) {
        boolean useDefaultTruststore = configService.getBoolProperty(SSLProperty.DEFAULT_TRUSTSTORE_ENABLED.getName(), SSLProperty.DEFAULT_TRUSTSTORE_ENABLED.getDefaultBoolean());

        if (useDefaultTruststore) {
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore) null); // Using null here initializes the TMF with the default trust store.

                for (TrustManager tm : tmf.getTrustManagers()) {
                    if (tm instanceof X509TrustManager) {
                        return (X509TrustManager) tm;
                    }
                }
            } catch (KeyStoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            LOG.info("Using default JVM truststore is disabled by configuration.");
        }

        return null;
    }

    private static X509TrustManager initCustomTrustManager(ConfigurationService configService) {
        boolean useCustomTruststore = configService.getBoolProperty(SSLProperty.CUSTOM_TRUSTSTORE_ENABLED.getName(), SSLProperty.CUSTOM_TRUSTSTORE_ENABLED.getDefaultBoolean());
        if (useCustomTruststore) {
            String trustStoreFile = configService.getProperty(SSLProperty.CUSTOM_TRUSTSTORE_LOCATION.getName(), SSLProperty.CUSTOM_TRUSTSTORE_LOCATION.getDefault());
            if (Strings.isEmpty(trustStoreFile)) {
                LOG.error("Cannot load custom truststore file from location " + trustStoreFile + ". Adapt " + SSLProperty.CUSTOM_TRUSTSTORE_LOCATION.getName() + " to be able to use trusted connections only.");
                return null;
            }

            File file = new File(trustStoreFile);
            if (!file.exists()) {
                LOG.error("Cannot find custom truststore file from location " + trustStoreFile + ". The file does not exist.");
                return null;
            }
            String password = configService.getProperty(SSLProperty.CUSTOM_TRUSTSTORE_PASSWORD.getName(), SSLProperty.CUSTOM_TRUSTSTORE_PASSWORD.getDefault());
            try (InputStream in = new FileInputStream(file)) {
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(in, password.toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);

                for (TrustManager tm : tmf.getTrustManagers()) {
                    if (tm instanceof X509TrustManager) {
                        return (X509TrustManager) tm;
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (KeyStoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (CertificateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            LOG.info("Using custom truststore is disabled by configuration.");
        }
        return null;
    }
}
