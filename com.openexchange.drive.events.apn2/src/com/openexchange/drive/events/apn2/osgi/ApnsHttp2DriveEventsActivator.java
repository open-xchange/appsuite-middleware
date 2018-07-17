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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.apn2.ApnsHttp2Options;
import com.openexchange.drive.events.apn2.ApnsHttp2Options.AuthType;
import com.openexchange.drive.events.apn2.DefaultIOSApnsHttp2OptionsProvider;
import com.openexchange.drive.events.apn2.IOSApnsHttp2OptionsProvider;
import com.openexchange.drive.events.apn2.internal.ApnsHttp2DriveEventPublisher;
import com.openexchange.drive.events.apn2.internal.IOSApnsHttp2DriveEventPublisher;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
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
        return new Class<?>[] { DriveEventService.class, DriveSubscriptionStore.class, ConfigurationService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.drive.events.apn2");

        ConfigurationService configService = getService(ConfigurationService.class);
        DriveEventService eventService = getService(DriveEventService.class);

        /*
         * iOS
         */
        if (configService.getBoolProperty("com.openexchange.drive.events.apn2.ios.enabled", false)) {
            /*
             * register APN certificate provider for iOS if specified via config file (with a low ranking)
             */
            ApnsHttp2Options options = getOptions(configService, "com.openexchange.drive.events.apn2.ios.");
            if (null != options) {
                registerService(IOSApnsHttp2OptionsProvider.class, new DefaultIOSApnsHttp2OptionsProvider(options), 1);
                LOG.info("Successfully registered APNS HTTP/2 options provider for iOS.");
            } else {
                LOG.info("No default APNS HTTP/2 options configured for iOS in \"Push\" section in file 'drive.properties', skipping registration for default iOS options provider.");
            }
            /*
             * register publisher
             */
            ApnsHttp2DriveEventPublisher publisher = new IOSApnsHttp2DriveEventPublisher(this);
            eventService.registerPublisher(publisher);
        } else {
            LOG.info("Drive events for iOS clients via APNS HTTP/2 are disabled, skipping publisher registration.");
        }
    }

    private ApnsHttp2Options getOptions(ConfigurationService configService, String prefix) throws Exception {
        // Auth type
        AuthType authType = AuthType.authTypeFor(configService.getProperty(prefix + "authtype"));
        if (null == authType) {
            LOG.info("Missing or invalid authentication type in APNS HTTP/2 options for drive events. Assuming {} instead.", AuthType.CERTIFICATE.name());
            authType = AuthType.CERTIFICATE;
        }

        ApnsHttp2Options apnsHttp2Options;
        if (authType == AuthType.CERTIFICATE) {
            // Keystore name
            String keystoreName = configService.getProperty(prefix + "keystore");
            if (Strings.isEmpty(keystoreName)) {
                LOG.info("Missing \"keystore\" APNS HTTP/2 option for drive events. Ignoring APNS HTTP/2 configuration for drive events.");
                return null;
            }

            // Topic
            String topic = configService.getProperty(prefix + "topic");
            if (null == topic) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prefix + "topic");
            }

            // Proceed if enabled for associated client
            String password = configService.getProperty(prefix + "password");
            if (null == password) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prefix + "password");
            }

            boolean production = configService.getBoolProperty(prefix + "production", true);
            apnsHttp2Options = createOptions(keystoreName, password, production, topic);
        } else if (authType == AuthType.JWT) {
            String privateKeyFile = configService.getProperty(prefix + "privatekey");
            if (null == privateKeyFile) {
                LOG.info("Missing \"privatekey\" APNS HTTP/2 option for drive events. Ignoring APNS HTTP/2 configuration for drive events.");
                return null;
            }

            String keyId = configService.getProperty(prefix + "keyid");
            if (null == keyId) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prefix + "keyid");
            }

            String teamId = configService.getProperty(prefix + "teamid");
            if (null == teamId) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prefix + "teamid");
            }

            // Topic
            String topic = configService.getProperty(prefix + "topic");
            if (null == topic) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prefix + "topic");
            }

            boolean production = configService.getBoolProperty(prefix + "production", true);
            apnsHttp2Options = createOptions(privateKeyFile, keyId, teamId, production, topic);
        } else {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prefix + "authtype");
        }

        LOG.info("Parsed APNS HTTP/2 options for drive events.");
        return apnsHttp2Options;
    }

    private ApnsHttp2Options createOptions(String resourceName, String password, boolean production, String topic) {
        return new ApnsHttp2Options(new File(resourceName), password, production, topic);
    }

    private ApnsHttp2Options createOptions(String privateKeyFile, String keyId, String teamId, boolean production, String topic) throws Exception{
        StringBuilder sPrivateKey = null;
        {
            InputStream resourceStream = null;
            BufferedReader reader = null;
            try {
                resourceStream = new FileInputStream(new File(privateKeyFile));
                reader = new BufferedReader(new InputStreamReader(resourceStream, Charsets.ISO_8859_1));
                sPrivateKey = new StringBuilder(2048);
                for (String line; (line = reader.readLine()) != null;) {
                    if (!line.startsWith("-----BEGIN") && !line.startsWith("-----END")) {
                        sPrivateKey.append(line);
                    }
                }
            } finally {
                Streams.close(resourceStream);
            }
        }
        return new ApnsHttp2Options(sPrivateKey.toString(), keyId, teamId, production, topic);
    }

}
