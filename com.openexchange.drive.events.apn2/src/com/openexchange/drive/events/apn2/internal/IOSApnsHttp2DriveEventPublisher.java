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

package com.openexchange.drive.events.apn2.internal;

import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options;
import com.openexchange.drive.events.apn2.util.ApnsHttp2OptionsProvider;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options.AuthType;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link IOSApnsHttp2DriveEventPublisher}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class IOSApnsHttp2DriveEventPublisher extends ApnsHttp2DriveEventPublisher {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IOSApnsHttp2DriveEventPublisher.class);

    /**
     * Initializes a new {@link IOSApnsHttp2DriveEventPublisher}.
     *
     * @param services The tracked OSGi services
     */
    public IOSApnsHttp2DriveEventPublisher(ServiceLookup services) {
        super(services);
    }

    @Override
    protected String getServiceID() {
        return "apn2";
    }

    @Override
    protected ApnsHttp2Options getOptions(int contextId, int userId) throws OXException {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        if (null == configService) {
            throw ServiceExceptionCode.absentService(LeanConfigurationService.class);
        }

        // Check if push via APNs HTTP/2 is enabled
        if (false == configService.getBooleanProperty(userId, contextId, DriveEventsAPN2IOSProperty.enabled)) {
            LOG.trace("Push via {} is disabled for user {} in context {}.", getServiceID(), I(userId), I(contextId));
            return null;
        }

        // Determine APNs HTTP/2 options by auth type
        ApnsHttp2Options options = null;
        AuthType authType = requireAuthType(contextId, userId, configService);
        switch (authType) {
            case CERTIFICATE:
                /*
                 * Get certificate options via config cascade
                 */
                String keystoreName = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.keystore);
                if (Strings.isEmpty(keystoreName)) {
                    options = getFallbackOptions(contextId, userId);
                    if (null == options) {
                        LOG.info("Missing \"keystore\" APNS HTTP/2 option for drive events for context {}. Ignoring APNS HTTP/2 configuration for drive events.", I(contextId));
                    }
                } else {
                    LOG.trace("Using configured certificate options for push via {} for user {} in context {}.", getServiceID(), I(userId), I(contextId));
                    String topic = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.topic);
                    String password = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.password);
                    boolean production = configService.getBooleanProperty(userId, contextId, DriveEventsAPN2IOSProperty.production);
                    options = new ApnsHttp2Options(new File(keystoreName), password, production, topic);
                }
                break;
            case JWT:
                /*
                 * Get JWT options via config cascade
                 */
                String privateKeyFile = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.privatekey);
                if (Strings.isEmpty(privateKeyFile)) {
                    options = getFallbackOptions(contextId, userId);
                    if (null == options) {
                        LOG.info("Missing \"privatekey\" APNS HTTP/2 option for drive events for context {}. Ignoring APNS HTTP/2 configuration for drive events.", I(contextId));
                    }
                } else {
                    LOG.trace("Using configured JWT options for push via {} for user {} in context {}.", getServiceID(), I(userId), I(contextId));

                    String keyId = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.keyid);
                    checkNotEmpty(keyId, DriveEventsAPN2IOSProperty.keyid);

                    String teamId = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.teamid);
                    checkNotEmpty(teamId, DriveEventsAPN2IOSProperty.teamid);

                    String topic = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.topic);
                    checkNotEmpty(topic, DriveEventsAPN2IOSProperty.topic);

                    boolean production = configService.getBooleanProperty(userId, contextId, DriveEventsAPN2IOSProperty.production);
                    try {
                        options = new ApnsHttp2Options(Files.readAllBytes(new File(privateKeyFile).toPath()), keyId, teamId, production, topic);
                    } catch (IOException e) {
                        LOG.error("Error instantiating APNS HTTP/2 options from {}", privateKeyFile, e);
                        return null;
                    }
                }
                break;
        }
        /*
         * check for a registered APNs options provider as fallback, otherwise
         */
        ApnsHttp2OptionsProvider provider = services.getOptionalService(ApnsHttp2OptionsProvider.class);
        if (null != provider) {
            LOG.trace("Using fallback certificate provider for push via {} for user {} in context {}.", getServiceID(), I(userId), I(contextId));
            return provider.getOptions();
        }
        LOG.trace("No configuration for push via {} found for user {} in context {}.", getServiceID(), I(userId), I(contextId));
        return options;
    }

    private static AuthType requireAuthType(int contextId, int userId, LeanConfigurationService configService) throws OXException {
        String sAuthType = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.authtype);
        checkNotEmpty(sAuthType, DriveEventsAPN2IOSProperty.authtype);

        AuthType authType = AuthType.authTypeFor(sAuthType);
        if (authType == null) {
            String propertyName = DriveEventsAPN2IOSProperty.authtype.getFQPropertyName();
            LOG.error("Unsupported value for property: {}", propertyName);
            throw OXException.general("No such auth type: " + sAuthType);
        }
        return authType;
    }

    private static void checkNotEmpty(String value, Property prop) throws OXException {
        if (Strings.isEmpty(value)) {
            String propertyName = prop.getFQPropertyName();
            LOG.error("Missing required property: {}", propertyName);
            throw OXException.general("Missing property: " + propertyName);
        }
    }

    private ApnsHttp2Options getFallbackOptions(int contextId, int userId) {
        /*
         * Try to get options via registered options provider as fallback
         */
        ApnsHttp2OptionsProvider optionsProvider = services.getService(ApnsHttp2OptionsProvider.class);
        if (null != optionsProvider) {
            LOG.trace("Using registered fallback options push via {} for user {} in context {}.", getServiceID(), I(userId), I(contextId));
            return optionsProvider.getOptions();
        }
        LOG.trace("No valid options available for push via {} for user {} in context {}.", getServiceID(), I(userId), I(contextId));
        return null;
    }

}
