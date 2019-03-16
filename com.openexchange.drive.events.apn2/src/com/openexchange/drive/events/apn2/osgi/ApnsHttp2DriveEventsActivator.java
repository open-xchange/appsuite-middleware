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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.drive.events.apn2.osgi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.apn2.ApnsHttp2Options;
import com.openexchange.drive.events.apn2.ApnsHttp2Options.AuthType;
import com.openexchange.drive.events.apn2.ApnsHttp2OptionsProvider;
import com.openexchange.drive.events.apn2.internal.DriveEventsAPN2IOSProperty;
import com.openexchange.drive.events.apn2.internal.IOSApnsHttp2DriveEventPublisher;
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

        AuthType authType;
        try {
            String authTypeString = optProperty(properties, DriveEventsAPN2IOSProperty.authtype);
            if (Strings.isEmpty(authTypeString)) {
                LOG.debug("Apn2 is not configured in the given properties object. Fallback to null.");
                return null;
            }
            authType = AuthType.authTypeFor(authTypeString);
            if (AuthType.CERTIFICATE.equals(authType)) {
                /*
                 * get certificate options
                 */
                String keystoreName = getProperty(properties, DriveEventsAPN2IOSProperty.keystore);
                if (Strings.isEmpty(keystoreName)) {
                    return null;
                }

                String topic = getProperty(properties, DriveEventsAPN2IOSProperty.topic);
                String password = getProperty(properties, DriveEventsAPN2IOSProperty.password);
                boolean production = Boolean.parseBoolean(getProperty(properties, DriveEventsAPN2IOSProperty.production));

                // Check file path validity
                File keystoreFile = new File(keystoreName);
                if (keystoreFile.exists()) {
                    return new ApnsHttp2Options(keystoreFile, password, production, topic);
                }

                // Assume keystore file is given as resource identifier
                try {
                    byte[] keystoreBytes = Streams.stream2bytes(service.loadResource(keystoreName));
                    if(keystoreBytes.length == 0) {
                        return null;
                    }
                    return new ApnsHttp2Options(keystoreBytes, password, production, topic);
                } catch (IOException e) {
                    LOG.warn("Failed to load keystore from resource {}", keystoreName, e);
                }
            }
            if (AuthType.JWT.equals(authType)) {
                /*
                 * get jwt options
                 */
                String privateKeyFileName = getProperty(properties, DriveEventsAPN2IOSProperty.privatekey);
                if (Strings.isEmpty(privateKeyFileName)) {
                    return null;
                }

                try {
                    byte[] privateKey;
                    File privateKeyFile = new File(privateKeyFileName);
                    if (privateKeyFile.exists()) {
                        privateKey = Files.readAllBytes(privateKeyFile.toPath());
                    } else {
                        // Assume private key file is given as resource identifier
                        privateKey = Streams.stream2bytes(service.loadResource(privateKeyFileName));
                        if(privateKey.length == 0) {
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
        } catch (OXException e1) {
            // nothing to do
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
        String result = optProperty(properties, prop);
        if (result == null) {
            // This should never happen as long as the shipped fragment contains a proper properties file
            LOG.error("Missing required property from fragment: {}", prop.getFQPropertyName());
            throw OXException.general("Missing property: " + prop.getFQPropertyName());
        }
        return result;
    }

    /**
     * Optionally gets the given property from the {@link Properties} object.
     *
     * @param properties The {@link Properties} object
     * @param prop The {@link Property} to return
     * @return The string value of the property or null
     */
    private String optProperty(Properties properties, Property prop) {
        return properties.getProperty(prop.getFQPropertyName());
    }

}
