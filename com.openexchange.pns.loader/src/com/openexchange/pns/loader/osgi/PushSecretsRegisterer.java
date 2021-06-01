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

package com.openexchange.pns.loader.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import com.openexchange.java.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.pns.loader.ClientIdentifierProvider;
import com.openexchange.pns.transport.apn.DefaultApnOptionsProvider;
import com.openexchange.pns.transport.apns_http2.DefaultApnsHttp2OptionsProvider;
import com.openexchange.pns.transport.apns_http2.util.ApnOptions;
import com.openexchange.pns.transport.apns_http2.util.ApnOptionsProvider;
import com.openexchange.pns.transport.apns_http2.util.ApnsHttp2Options;
import com.openexchange.pns.transport.apns_http2.util.ApnsHttp2OptionsProvider;
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

    private static final String TOPIC_IOS_VANILLA_MAILAPP = "com.openxchange.mobile.mailapp2";

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
        loadAndRegisterApnsHttp2Options(client, props);

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
            registration = context.registerService(ApnOptionsProvider.class, apnOptionsProvider, withRanking(1000));
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
            registration = context.registerService(GcmOptionsProvider.class, gcmOptionsProvider, withRanking(1000));
            rememberRegistration(client, registration);
            registration = null; // Everything went fine
            LOG.info("Loaded GCM secret key for client identifier {}", client);
        } finally {
            if (null != registration) {
                registration.unregister();
            }
        }
    }

    private void loadAndRegisterApnsHttp2Options(String client, Properties props) {
        ServiceRegistration<?> registration = null;
        try {
            ApnsHttp2Options apnsHttp2Options = loadApnsHttp2OptionsFromFragment(client, props);
            if (null == apnsHttp2Options) {
                // Property keystore is missing and we assume that no APN certificate is wanted.
                return;
            }

            DefaultApnsHttp2OptionsProvider apnsHttp2OptionsProvider = new DefaultApnsHttp2OptionsProvider(Collections.singletonMap(client, apnsHttp2Options));
            registration = context.registerService(ApnsHttp2OptionsProvider.class, apnsHttp2OptionsProvider, withRanking(1000));
            rememberRegistration(client, registration);
            registration = null; // Everything went fine
            LOG.info("Loaded APNs HTTP/2 push configuration for client identifier {}", client);
        } catch (IOException e) {
            LOG.error("Failed to load APNs HTTP/2 configuration for push client {}", client, e);
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
        String privateKeyFile = props.getProperty("apnPrivatekeyFile");
        if (Strings.isNotEmpty(privateKeyFile)) {
            String keyId = props.getProperty("apnKeyId");
            String teamId = props.getProperty("apnTeamId");
            if (null == keyId) {
                throw new IOException(client + ".properties is missing property keyId.");
            }
            if (null == teamId) {
                throw new IOException(client + ".properties is missing property teamId.");
            }
            boolean production = Boolean.parseBoolean(props.getProperty("apnProduction", Boolean.TRUE.toString()));
            try (InputStream is = PushSecretsRegisterer.class.getClassLoader().getResourceAsStream(privateKeyFile);) {
                if (null == is) {
                    throw new IOException("Could not load resource " + privateKeyFile + " from com.openexchange.pns.loader fragment.");
                }
                return new ApnOptions(IOUtils.toByteArray(is), keyId, teamId, production, TOPIC_IOS_VANILLA_MAILAPP, client);
            }
        }
        
        String filename = props.getProperty("apnKeystoreFile");
        if (Strings.isNotEmpty(filename)) {

            String password = props.getProperty("apnPassword");
            if (null == password) {
                throw new IOException(client + ".properties is missing property password for keystore password.");
            }

            boolean production = Boolean.parseBoolean(props.getProperty("apnProduction", Boolean.TRUE.toString()));
            try (InputStream is = PushSecretsRegisterer.class.getClassLoader().getResourceAsStream(filename);) {
                if (null == is) {
                    throw new IOException("Could not load resource " + filename + " from com.openexchange.pns.loader fragment.");
                }
                return new ApnOptions(IOUtils.toByteArray(is), password, production, TOPIC_IOS_VANILLA_MAILAPP, client);
            }
        }
        return null;
    }

    private static ApnsHttp2Options loadApnsHttp2OptionsFromFragment(String client, Properties props) throws IOException {
        String privateKeyFile = props.getProperty("apnsHttp2PrivatekeyFile");
        if (Strings.isNotEmpty(privateKeyFile)) {
            String keyId = props.getProperty("apnsHttp2KeyId");
            String teamId = props.getProperty("apnsHttp2TeamId");
            if (null == keyId) {
                throw new IOException(client + ".properties is missing property keyId.");
            }
            if (null == teamId) {
                throw new IOException(client + ".properties is missing property teamId.");
            }
            boolean production = Boolean.parseBoolean(props.getProperty("apnsHttp2Production", Boolean.TRUE.toString()));
            try (InputStream is = PushSecretsRegisterer.class.getClassLoader().getResourceAsStream(privateKeyFile);) {
                if (null == is) {
                    throw new IOException("Could not load resource " + privateKeyFile + " from com.openexchange.pns.loader fragment.");
                }
                return new ApnsHttp2Options(client, IOUtils.toByteArray(is), keyId, teamId, production, TOPIC_IOS_VANILLA_MAILAPP);
            }
        }
        
        String filename = props.getProperty("apnsHttp2KeystoreFile");
        if (Strings.isNotEmpty(filename)) {

            String password = props.getProperty("apnsHttp2Password");
            if (null == password) {
                throw new IOException(client + ".properties is missing property password for keystore password.");
            }

            boolean production = Boolean.parseBoolean(props.getProperty("apnsHttp2Production", Boolean.TRUE.toString()));
            try (InputStream is = PushSecretsRegisterer.class.getClassLoader().getResourceAsStream(filename);) {
                if (null == is) {
                    throw new IOException("Could not load resource " + filename + " from com.openexchange.pns.loader fragment.");
                }
                return new ApnsHttp2Options(client, IOUtils.toByteArray(is), password, production, TOPIC_IOS_VANILLA_MAILAPP);
            }
        }
        return null;
    }
}
