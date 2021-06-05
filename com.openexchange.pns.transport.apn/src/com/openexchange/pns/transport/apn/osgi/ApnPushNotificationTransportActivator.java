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

package com.openexchange.pns.transport.apn.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.apn.DefaultApnOptionsProvider;
import com.openexchange.pns.transport.apn.internal.ApnPushNotificationTransport;
import com.openexchange.pns.transport.apns_http2.util.ApnOptions;
import com.openexchange.pns.transport.apns_http2.util.ApnOptions.AuthType;
import com.openexchange.pns.transport.apns_http2.util.ApnOptionsProvider;

/**
 * {@link ApnPushNotificationTransportActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ApnPushNotificationTransportActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnPushNotificationTransportActivator.class);

    private static final String CONFIGFILE_APNS_OPTIONS = "pns-apns-options.yml";

    private ServiceRegistration<ApnOptionsProvider> optionsProviderRegistration;
    private ApnPushNotificationTransport apnTransport;

    /**
     * Initializes a new {@link ApnPushNotificationTransportActivator}.
     */
    public ApnPushNotificationTransportActivator() {
        super();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            reinit(configService);
        } catch (Exception e) {
            LOG.error("Failed to re-initialize APNS transport", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().configFileNames(CONFIGFILE_APNS_OPTIONS).propertiesOfInterest("com.openexchange.pns.transport.apn.ios.enabled").build();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, PushSubscriptionRegistry.class, PushMessageGeneratorRegistry.class, ConfigViewFactory.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        reinit(getService(ConfigurationService.class));

        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                ApnPushNotificationTransport.invalidateEnabledCache();
            }

        });

        registerService(Reloadable.class, this);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        ApnPushNotificationTransport apnTransport = this.apnTransport;
        if (null != apnTransport) {
            apnTransport.close();
            this.apnTransport = null;
        }
        ServiceRegistration<ApnOptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
        if (null != optionsProviderRegistration) {
            optionsProviderRegistration.unregister();
            this.optionsProviderRegistration = null;
        }
        super.stopBundle();
    }

    private synchronized void reinit(ConfigurationService configService) throws Exception {
        ApnPushNotificationTransport apnTransport = this.apnTransport;
        if (null != apnTransport) {
            apnTransport.close();
            this.apnTransport = null;
        }

        ServiceRegistration<ApnOptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
        if (null != optionsProviderRegistration) {
            optionsProviderRegistration.unregister();
            this.optionsProviderRegistration = null;
        }

        Object yaml = configService.getYaml(CONFIGFILE_APNS_OPTIONS);
        if (null != yaml && Map.class.isInstance(yaml)) {
            Map<String, Object> map = (Map<String, Object>) yaml;
            if (!map.isEmpty()) {
                Map<String, ApnOptions> options = parseApnOptions(map);
                if (null != options && !options.isEmpty()) {
                    optionsProviderRegistration = context.registerService(ApnOptionsProvider.class, new DefaultApnOptionsProvider(options), withRanking(785));
                    this.optionsProviderRegistration = optionsProviderRegistration;
                }
            }
        }

        apnTransport = new ApnPushNotificationTransport(getService(PushSubscriptionRegistry.class), getService(PushMessageGeneratorRegistry.class), getService(ConfigViewFactory.class), context);
        apnTransport.open();
        this.apnTransport = apnTransport;
    }

    private Map<String, ApnOptions> parseApnOptions(Map<String, Object> yaml) throws Exception {
        Map<String, ApnOptions> options = new LinkedHashMap<String, ApnOptions>(yaml.size());
        for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            String client = entry.getKey();

            // Check for duplicate
            if (options.containsKey(client)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Duplicate APNS options specified for client: " + client);
            }

            // Check values map
            if (false == Map.class.isInstance(entry.getValue())) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Invalid APNS options configuration specified for client: " + client);
            }

            // Parse values map
            Map<String, Object> values = (Map<String, Object>) entry.getValue();

            // Enabled?
            Boolean enabled = getBooleanOption("enabled", Boolean.TRUE, values);
            if (false == enabled.booleanValue()) {
                LOG.info("APNS options for client {} is disabled.", client);
                continue;
            }

            // Auth type
            AuthType authType = AuthType.authTypeFor(getStringOption("authtype", values));
            if (null == authType) {
                // Missing or invalid auth type. Assume "certificate" as fall-back.
                LOG.debug("Missing or invalid authentication type in APNS HTTP/2 options for client {}. Using fall-back \"certificate\".", client);
                authType = AuthType.CERTIFICATE;
            }
            switch (authType) {
                case CERTIFICATE:
                    processCertificate(client, options, values);
                    break;
                case JWT:
                    processJWT(client, options, values);
                    break;
                default:
                    LOG.debug("Unsupported auth type {}", authType);
            }
        }
        return options;
    }

    /**
     * Process the {@link AuthType#CERTIFICATE}
     *
     * @param client The client
     * @param options the options
     * @param values The values
     * @return <code>true</code> if appropriate options have been initialized for given client; otherwise <code>false</code>
     */
    private static boolean processCertificate(String client, Map<String, ApnOptions> options, Map<String, Object> values) {
        String keystoreName = getStringOption("keystore", values);
        if (Strings.isEmpty(keystoreName)) {
            LOG.info("Missing \"keystore\" APNS option for client {}. Ignoring that client's configuration.", client);
            return false;
        }
        String password = getStringOption("password", values);
        if (Strings.isEmpty(password)) {
            LOG.info("Missing \"password\" APNS option for client {}. Ignoring that client's configuration.", client);
            return false;
        }
        String topic = getStringOption("topic", values);
        if (Strings.isEmpty(topic)) {
            LOG.info("Missing \"topic\" APNS option for client {}. Ignoring that client's configuration.", client);
            return false;
        }
        Boolean production = getBooleanOption("production", Boolean.TRUE, values);
        try {
            options.put(client, new ApnOptions(getResourceBytes(keystoreName), password, production.booleanValue(), topic, client));
            LOG.info("Parsed APNS options for client {}.", client);
            return true;
        } catch (Exception e) {
            LOG.warn("Failed to parse APNS options for client {}.", client, e);
        }
        return false;
    }

    /**
     * Process the {@link AuthType#JWT}
     *
     * @param client The client
     * @param options the options
     * @param values The values
     * @return <code>true</code> if appropriate options have been initialized for given client; otherwise <code>false</code>
     */
    private static boolean processJWT(String client, Map<String, ApnOptions> options, Map<String, Object> values) {
        String privateKeyName = getStringOption("privatekey", values);
        if (Strings.isEmpty(privateKeyName)) {
            LOG.info("Missing \"privatekey\" APNS option for client {}. Ignoring that client's configuration.", client);
            return false;
        }
        String keyId = getStringOption("keyid", values);
        if (Strings.isEmpty(keyId)) {
            LOG.info("Missing \"keyid\" APNS option for client {}. Ignoring that client's configuration.", client);
            return false;
        }
        String teamId = getStringOption("teamid", values);
        if (Strings.isEmpty(teamId)) {
            LOG.info("Missing \"teamid\" APNS option for client {}. Ignoring that client's configuration.", client);
            return false;
        }
        String topic = getStringOption("topic", values);
        if (Strings.isEmpty(topic)) {
            LOG.info("Missing \"topic\" APNS option for client {}. Ignoring that client's configuration.", client);
            return false;
        }
        Boolean production = getBooleanOption("production", Boolean.TRUE, values);
        try {
            options.put(client, new ApnOptions(getResourceBytes(privateKeyName), keyId, teamId, production.booleanValue(), topic, client));
            LOG.info("Parsed APNS options for client {}.", client);
            return true;
        } catch (Exception e) {
            LOG.warn("Failed to parse APNS options for client {}.", client, e);
        }
        return false;
    }

    private static Boolean getBooleanOption(String name, Boolean def, Map<String, Object> values) {
        Object object = values.get(name);
        if (object instanceof Boolean) {
            return (Boolean) object;
        }
        return null == object ? def : Boolean.valueOf(object.toString());
    }

    private static String getStringOption(String name, Map<String, Object> values) {
        Object object = values.get(name);
        if (null == object) {
            return null;
        }
        String str = object.toString();
        return Strings.isEmpty(str) ? null : str.trim();
    }

    private static byte[] getResourceBytes(String resourceName) throws IOException {
        try (InputStream resourceStream = new FileInputStream(new File(resourceName))) {
            return IOUtils.toByteArray(resourceStream);
        }
    }
}
