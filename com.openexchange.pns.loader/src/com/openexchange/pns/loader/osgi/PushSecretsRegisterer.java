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

package com.openexchange.pns.loader.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.pns.loader.ClientIdentifierProvider;
import com.openexchange.pns.transport.apn.ApnOptions;
import com.openexchange.pns.transport.apn.ApnOptionsProvider;
import com.openexchange.pns.transport.apn.DefaultApnOptionsProvider;
import com.openexchange.pns.transport.gcm.DefaultGcmOptionsProvider;
import com.openexchange.pns.transport.gcm.GcmOptions;
import com.openexchange.pns.transport.gcm.GcmOptionsProvider;

/**
 * {@link PushSecretsRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class PushSecretsRegisterer implements ServiceTrackerCustomizer<ClientIdentifierProvider, ClientIdentifierProvider> {

    static final Logger LOG = LoggerFactory.getLogger(PushSecretsRegisterer.class);

    private final BundleContext context;
    private final Map<String, List<ServiceRegistration<?>>> serviceRegistrations; // Guarded by synchronized

    /**
     * Initializes a new {@link PushSecretsRegisterer}.
     *
     * @param context The bundle context
     */
    public PushSecretsRegisterer(BundleContext context) {
        super();
        this.context = context;
        serviceRegistrations = new HashMap<String, List<ServiceRegistration<?>>>();
    }

    @Override
    public synchronized ClientIdentifierProvider addingService(ServiceReference<ClientIdentifierProvider> reference) {
        ClientIdentifierProvider clientIdentifierProvider = context.getService(reference);
        register(clientIdentifierProvider.getClient());
        return clientIdentifierProvider;
    }

    @Override
    public void modifiedService(ServiceReference<ClientIdentifierProvider> reference, ClientIdentifierProvider clientIdentifierProvider) {
        // Nothing to do for changed properties of ClientIdentifierProvider.
    }

    @Override
    public synchronized void removedService(ServiceReference<ClientIdentifierProvider> reference, ClientIdentifierProvider clientIdentifierProvider) {
        unregister(clientIdentifierProvider.getClient());
        context.ungetService(reference);
    }

    private void register(String client) {
        Properties props = loadProperties(client);
        if (null == props) {
            // Failed to load client-associated .properties file
            return;
        }

        loadAndRegisterGcmOptions(client, props);
        loadAndRegisterApnOptions(client, props);

        // TODO APNS HTTP2
        // TODO Websockets
        // TODO wns
    }

    private void unregister(String client) {
        List<ServiceRegistration<?>> list = serviceRegistrations.remove(client);
        if (null != list) {
            for (ServiceRegistration<?> registration : list) {
                registration.unregister();
            }
        }
    }

    private void loadAndRegisterApnOptions(String client, Properties props) {
        ServiceRegistration<?> registration = null;
        try {
            ApnOptions apnOptions = loadApnOptionsFromFragment(client, props);
            if (null == apnOptions) {
                // Property keystore is missing and we assume that no APN certificate is wanted.
                return;
            }

            DefaultApnOptionsProvider apnOptionsProvider = new DefaultApnOptionsProvider(Collections.singletonMap(client, apnOptions));
            registration = context.registerService(ApnOptionsProvider.class, apnOptionsProvider, createServiceDictionary());
            rememberRegistration(client, registration);
            registration = null; // Everything went fine
            LOG.info("Loaded APN push certificates for client identifier {}", client);
        } catch (IOException e) {
            LOG.error("Failed to load APN certificate for push client {}", client, e);
        } finally {
            if (null != registration) {
                registration.unregister();
            }
        }
    }

    private void loadAndRegisterGcmOptions(String client, Properties props) {
        ServiceRegistration<GcmOptionsProvider> registration = null;
        try {
            String key = props.getProperty("gcmKey");
            if (null == key) {
                return;
            }

            GcmOptions options = new GcmOptions(key);
            DefaultGcmOptionsProvider gcmOptionsProvider = new DefaultGcmOptionsProvider(Collections.singletonMap(client, options));
            registration = context.registerService(GcmOptionsProvider.class, gcmOptionsProvider, createServiceDictionary());
            rememberRegistration(client, registration);
            registration = null; // Everything went fine
            LOG.info("Loaded GCM secret key for client identifier {}", client);
        } finally {
            if (null != registration) {
                registration.unregister();
            }
        }
    }

    private void rememberRegistration(String client, ServiceRegistration<?> registration) {
        List<ServiceRegistration<?>> list = serviceRegistrations.get(client);
        if (null == list) {
            List<ServiceRegistration<?>> newList = new ArrayList<>(4);
            list = serviceRegistrations.putIfAbsent(client, newList);
            if (list == null) {
                list = newList;
            }
        }
        list.add(registration);
    }

    private static Dictionary<String, Object> createServiceDictionary() {
        Dictionary<String, Object> dictionary = new Hashtable<>(1);
        dictionary.put(Constants.SERVICE_RANKING, Integer.valueOf(1000));
        return dictionary;
    }

    /**
     * Tries to load the client-associated <code>.properties</code> file, which is assumed to be named:
     * <pre>
     * &lt;client-identifier&gt; + ".properties"
     * </pre>
     *
     * @param client The client identifier
     * @return The loaded properties or <code>null</code>
     */
    private static Properties loadProperties(String client) {
        try (InputStream is = PushSecretsRegisterer.class.getClassLoader().getResourceAsStream(client + ".properties")) {
            if (null == is) {
                // No such .properties file for specified client identifier
                LOG.debug("Could not load resource {}.properties from com.openexchange.pns.loader fragment.", client);
                return null;
            }

            Properties retval = new Properties();
            retval.load(is);
            return retval;
        } catch (IOException e) {
            LOG.error("Failed to load properties for certificates for push client {}", client, e);
            return null;
        }
    }

    private static ApnOptions loadApnOptionsFromFragment(String client, Properties props) throws IOException {
        String filename = props.getProperty("apnKeystoreFile");
        if (null == filename) {
            return null;
        }

        String password = props.getProperty("apnPassword");
        if (null == password) {
            throw new IOException(client + ".properties is missing property password for keystore password.");
        }

        boolean production = Boolean.parseBoolean(props.getProperty("apnProduction", Boolean.TRUE.toString()));
        try (InputStream is = PushSecretsRegisterer.class.getClassLoader().getResourceAsStream(filename);) {
            if (null == is) {
                throw new IOException("Could not load resource " + filename + " from com.openexchange.pns.loader fragment.");
            }

            final KeyStore keystore;
            try {
                keystore = KeyStore.getInstance("PKCS12");
                keystore.load(is, password.toCharArray());
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                throw new IOException("Error reading certificate from  '" + filename + "'", e);
            }

            return new ApnOptions(keystore, password, production);
        }
    }
}
