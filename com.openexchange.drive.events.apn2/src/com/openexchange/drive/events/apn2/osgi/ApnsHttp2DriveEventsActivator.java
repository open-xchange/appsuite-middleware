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

package com.openexchange.drive.events.apn2.osgi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.apn2.internal.DriveEventsAPN2IOSProperty;
import com.openexchange.drive.events.apn2.internal.IOSApnsHttp2DriveEventPublisher;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options;
import com.openexchange.drive.events.apn2.util.ApnsHttp2OptionsProvider;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.exception.OXException;
import com.openexchange.fragment.properties.loader.FragmentPropertiesLoader;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link ApnsHttp2DriveEventsActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class ApnsHttp2DriveEventsActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnsHttp2DriveEventsActivator.class);

    /**
     * Initializes a new {@link ApnsHttp2DriveEventsActivator}.
     */
    public ApnsHttp2DriveEventsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DriveEventService.class, DriveSubscriptionStore.class, LeanConfigurationService.class, TimerService.class, ThreadPoolService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { ApnsHttp2OptionsProvider.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: {}", context.getBundle().getSymbolicName());
        track(FragmentPropertiesLoader.class, new SimpleRegistryListener<FragmentPropertiesLoader>() {

            private ApnsHttp2OptionsProvider provider;

            @Override
            public synchronized void added(ServiceReference<FragmentPropertiesLoader> ref, FragmentPropertiesLoader service) {
                Properties properties = service.load(DriveEventsAPN2IOSProperty.FRAGMENT_FILE_NAME);
                if (properties != null) {
                    ApnsHttp2Options option = createOption(properties, service);
                    if (option != null) {
                        provider = () -> option;
                        registerService(ApnsHttp2OptionsProvider.class, provider);
                    }
                }
            }

            @Override
            public synchronized void removed(ServiceReference<FragmentPropertiesLoader> ref, FragmentPropertiesLoader service) {
                if (provider != null) {
                    unregisterService(provider);
                }
            }
        });
        openTrackers();
        /*
         * register publisher
         */
        getServiceSafe(DriveEventService.class).registerPublisher(new IOSApnsHttp2DriveEventPublisher(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }

    @Override
    public <S> void registerService(Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    @Override
    public <S> void unregisterService(S service) {
        super.unregisterService(service);
    }

    /**
     * Creates a {@link ApnsHttp2Options} from the given {@link Properties} object
     *
     * @param properties The {@link Properties} object containing all required properties
     * @param service The {@link FragmentPropertiesLoader}
     * @return the {@link ApnsHttp2Options} or null if some properties are missing
     */
    protected ApnsHttp2Options createOption(Properties properties, FragmentPropertiesLoader service) {
        try {

            // Try with PKCS#8 private key first
            String privateKeyFileName = optProperty(properties, DriveEventsAPN2IOSProperty.privatekey);
            if (Strings.isNotEmpty(privateKeyFileName)) {
                try {
                    byte[] privateKey;
                    File privateKeyFile = new File(privateKeyFileName);
                    if (privateKeyFile.exists()) {
                        privateKey = Files.readAllBytes(privateKeyFile.toPath());
                    } else {
                        // Assume private key file is given as resource identifier
                        privateKey = Streams.stream2bytes(service.loadResource(privateKeyFileName));
                        if (privateKey.length == 0) {
                            return null;
                        }
                    }

                    String keyId = getProperty(properties, DriveEventsAPN2IOSProperty.keyid);
                    String teamId = getProperty(properties, DriveEventsAPN2IOSProperty.teamid);
                    String topic = getProperty(properties, DriveEventsAPN2IOSProperty.topic);
                    boolean production = Boolean.parseBoolean(getProperty(properties, DriveEventsAPN2IOSProperty.production));
                    return new ApnsHttp2Options(privateKey, keyId, teamId, production, topic);
                } catch (IOException e) {
                    LOG.error("Error instantiating APNS HTTP/2 options from {}", privateKeyFileName, e);
                    return null;
                }
            }

            // Use PKCS#12 keystore as fall-back
            String keystoreFileName = optProperty(properties, DriveEventsAPN2IOSProperty.keystore);
            if (Strings.isNotEmpty(keystoreFileName)) {
                try {
                    byte[] keystore;
                    File keystoreFile = new File(keystoreFileName);
                    if (keystoreFile.exists()) {
                        keystore = Files.readAllBytes(keystoreFile.toPath());
                    } else {
                        // Assume keystore file is given as resource identifier
                        keystore = Streams.stream2bytes(service.loadResource(keystoreFileName));
                        if (keystore.length == 0) {
                            return null;
                        }
                    }

                    String password = getProperty(properties, DriveEventsAPN2IOSProperty.password);
                    String topic = getProperty(properties, DriveEventsAPN2IOSProperty.topic);
                    boolean production = Boolean.parseBoolean(getProperty(properties, DriveEventsAPN2IOSProperty.production));
                    return new ApnsHttp2Options(keystore, password, production, topic);
                } catch (IOException e) {
                    LOG.error("Error instantiating APNS HTTP/2 options from {}", keystoreFileName, e);
                    return null;
                }
            }
        } catch (OXException e) {
            LOG.debug("Error while creating APNS HTTP/2 options", e);
        }
        return null;
    }



    /**
     * Get the given property from the {@link Properties} object
     *
     * @param properties The {@link Properties} object
     * @param prop The {@link Property} to return
     * @return The string value of the property
     * @throws OXException In case the property is missing
     */
    private String getProperty(Properties properties, Property prop) throws OXException {
        String result = properties.getProperty(prop.getFQPropertyName());
        if (result == null) {
            // This should never happen as long as the shipped fragment contains a proper properties file
            LOG.error("Missing required property from fragment: {}", prop.getFQPropertyName());
            throw OXException.general("Missing property: " + prop.getFQPropertyName());
        }
        return result;
    }

    /**
     * Get the given property from the {@link Properties} object or <code>null</code>
     *
     * @param properties The {@link Properties} object
     * @param prop The {@link Property} to return
     * @return The string value of the property or <code>null</code> in case property is missing
     */
    private String optProperty(Properties properties, Property prop) {
        String result = properties.getProperty(prop.getFQPropertyName());
        if (result == null) {
            // This should never happen as long as the shipped fragment contains a proper properties file
            LOG.debug("Missing required property from fragment: {}", prop.getFQPropertyName());
        }
        return result;
    }

}
