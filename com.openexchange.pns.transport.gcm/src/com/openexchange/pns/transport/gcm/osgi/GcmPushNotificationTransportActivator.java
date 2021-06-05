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

package com.openexchange.pns.transport.gcm.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import java.util.LinkedHashMap;
import java.util.Map;
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
import com.openexchange.pns.transport.gcm.DefaultGcmOptionsProvider;
import com.openexchange.pns.transport.gcm.GcmOptions;
import com.openexchange.pns.transport.gcm.GcmOptionsProvider;
import com.openexchange.pns.transport.gcm.internal.GcmPushNotificationTransport;


/**
 * {@link GcmPushNotificationTransportActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class GcmPushNotificationTransportActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GcmPushNotificationTransportActivator.class);

    private static final String CONFIGFILE_GCM_OPTIONS = "pns-gcm-options.yml";

    private ServiceRegistration<GcmOptionsProvider> optionsProviderRegistration;
    private GcmPushNotificationTransport gcmTransport;

    /**
     * Initializes a new {@link ApnPushNotificationTransportActivator}.
     */
    public GcmPushNotificationTransportActivator() {
        super();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            reinit(configService);
        } catch (Exception e) {
            LOG.error("Failed to re-initialize GCM transport", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder()
            .configFileNames(CONFIGFILE_GCM_OPTIONS)
            .propertiesOfInterest("com.openexchange.pns.transport.gcm.enabled")
            .build();
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
                GcmPushNotificationTransport.invalidateEnabledCache();
            }

        });

        registerService(Reloadable.class, this);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        GcmPushNotificationTransport gcmTransport = this.gcmTransport;
        if (null != gcmTransport) {
            gcmTransport.close();
            this.gcmTransport = null;
        }
        ServiceRegistration<GcmOptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
        if (null != optionsProviderRegistration) {
            optionsProviderRegistration.unregister();
            this.optionsProviderRegistration = null;
        }
        super.stopBundle();
    }

    private synchronized void reinit(ConfigurationService configService) throws Exception {
        GcmPushNotificationTransport gcmTransport = this.gcmTransport;
        if (null != gcmTransport) {
            gcmTransport.close();
            this.gcmTransport = null;
        }

        ServiceRegistration<GcmOptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
        if (null != optionsProviderRegistration) {
            optionsProviderRegistration.unregister();
            this.optionsProviderRegistration = null;
        }

        Object yaml = configService.getYaml(CONFIGFILE_GCM_OPTIONS);
        if (null != yaml && Map.class.isInstance(yaml)) {
            Map<String, Object> map = (Map<String, Object>) yaml;
            if (!map.isEmpty()) {
                Map<String, GcmOptions> options = parseGcmOptions(map);
                if (null != options && !options.isEmpty()) {
                    optionsProviderRegistration = context.registerService(GcmOptionsProvider.class, new DefaultGcmOptionsProvider(options), withRanking(785));
                    this.optionsProviderRegistration = optionsProviderRegistration;
                }
            }
        }

        gcmTransport = new GcmPushNotificationTransport(getService(PushSubscriptionRegistry.class), getService(PushMessageGeneratorRegistry.class), getService(ConfigViewFactory.class), context);
        gcmTransport.open();
        this.gcmTransport = gcmTransport;
    }

    private Map<String, GcmOptions> parseGcmOptions(Map<String, Object> yaml) throws Exception {
        Map<String, GcmOptions> options = new LinkedHashMap<String, GcmOptions>(yaml.size());
        for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            String client = entry.getKey();

            // Check for duplicate
            if (options.containsKey(client)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Duplicate GCM options specified for client: " + client);
            }

            // Check values map
            if (false == Map.class.isInstance(entry.getValue())) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Invalid GCM options configuration specified for client: " + client);
            }

            // Parse values map
            Map<String, Object> values = (Map<String, Object>) entry.getValue();

            // Enabled?
            Boolean enabled = getBooleanOption("enabled", Boolean.TRUE, values);
            if (enabled.booleanValue()) {
                // Key
                String key = getStringOption("key", values);
                if (null == key) {
                    LOG.info("Missing \"key\" GCM option for client {}. Ignoring that client's configuration.", client);
                } else {
                    GcmOptions gcmOptions = new GcmOptions(key);
                    options.put(client, gcmOptions);
                    LOG.info("Parsed GCM options for client {}.", client);
                }
            } else {
                LOG.info("GCM options for client {} is disabled.", client);
            }
        }
        return options;
    }

    private Boolean getBooleanOption(String name, Boolean def, Map<String, Object> values) {
        Object object = values.get(name);
        if (object instanceof Boolean) {
            return (Boolean) object;
        }
        return null == object ? def : Boolean.valueOf(object.toString());
    }

    private String getStringOption(String name, Map<String, Object> values) {
        Object object = values.get(name);
        if (null == object) {
            return null;
        }
        String str = object.toString();
        return Strings.isEmpty(str) ? null : str.trim();
    }

}
