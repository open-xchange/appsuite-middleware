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

package com.openexchange.pns.mobile.api.facade.osgi;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.DefaultInterests.Builder;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMessageGenerator;
import com.openexchange.pns.mobile.api.facade.ClientConfig;
import com.openexchange.pns.mobile.api.facade.MobileApiFacadeMessageGenerator;
import com.openexchange.pns.mobile.api.facade.MobileApiFacadePushConfiguration;

/**
 * {@link MobileApiFacadePushStuffActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class MobileApiFacadePushStuffActivator extends HousekeepingActivator implements Reloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobileApiFacadePushStuffActivator.class);

    private List<ServiceRegistration<PushMessageGenerator>> registrations;

    /**
     * Initializes a new {@link MobileApiFacadePushStuffActivator}.
     */
    public MobileApiFacadePushStuffActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(Reloadable.class, this);
        reinit(getService(ConfigurationService.class), getService(ConfigViewFactory.class));
    }

    private synchronized void reinit(ConfigurationService configService, ConfigViewFactory viewFactory) throws Exception {
        List<ServiceRegistration<PushMessageGenerator>> registrations = this.registrations;
        if (null != registrations) {
            this.registrations = null;
            for (ServiceRegistration<PushMessageGenerator> registration : registrations) {
                registration.unregister();
            }
            registrations = null;
        }

        registrations = new LinkedList<>();

        // Add generic one
        registrations.add(context.registerService(PushMessageGenerator.class, new MobileApiFacadeMessageGenerator(ClientConfig.GENERIC_CLIENT_CONFIG, viewFactory), null));
        LOG.info("Registered PNS Mobile API Facade configuration for client {}", ClientConfig.GENERIC_CLIENT_CONFIG.getClientId());

        // Parse brand-specific ones
        Object yaml = configService.getYaml(MobileApiFacadePushConfiguration.CONFIG_CLIENT_IDS_YAML);
        if (null != yaml && Map.class.isInstance(yaml)) {
            Map<String, Object> map = (Map<String, Object>) yaml;
            if (!map.isEmpty()) {
                Map<String, ClientConfig> configs = parseClientConfigs(map);
                if (null != configs && !configs.isEmpty()) {
                    for (ClientConfig config : configs.values()) {
                        if (config.isEnabled()) {
                            registrations.add(context.registerService(PushMessageGenerator.class, new MobileApiFacadeMessageGenerator(config, viewFactory), null));
                            LOG.info("Registered PNS Mobile API Facade configuration for client {}", config.getClientId());
                        } else {
                            LOG.info("PNS Mobile API Facade configuration for client {} is disabled", config.getClientId());
                        }
                    }
                }
            }
        }
        this.registrations = registrations;
    }

    private Map<String, ClientConfig> parseClientConfigs(Map<String, Object> yaml) throws Exception {
        Map<String, ClientConfig> configs = new LinkedHashMap<>(yaml.size());
        for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            String client = entry.getKey();

            // Check for duplicate
            if (configs.containsKey(client)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Duplicate PNS Mobile API Facade configurations specified for client: " + client);
            }

            // Check values map
            if (false == Map.class.isInstance(entry.getValue())) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Invalid PNS Mobile API Facade configuration specified for client: " + client);
            }

            // Parse values map
            Map<String, Object> values = (Map<String, Object>) entry.getValue();
            ClientConfig.Builder builder = ClientConfig.builder(client);

            // Enabled?
            Boolean enabled = getBooleanOption("enabled", Boolean.TRUE, values);
            builder.enabled(enabled.booleanValue());

            // Description
            String description = getStringOption("description", values);
            builder.description(description);

            configs.put(client, builder.build());
        }
        return configs;
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

    // ----------------------------------------------------------------------------------------------------------

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        MobileApiFacadePushConfiguration.invalidateCache();
        try {
            reinit(configService, getService(ConfigViewFactory.class));
        } catch (Exception e) {
            LOG.error("Failed to re-initialize PNS Mobile API Facade configurations", e);
        }
    }

    @Override
    public Interests getInterests() {
        Builder builder = DefaultInterests.builder();
        builder.propertiesOfInterest(
            MobileApiFacadePushConfiguration.CONFIG_APN_BADGE_ENABLED,
            MobileApiFacadePushConfiguration.CONFIG_APN_SOUND_ENABLED,
            MobileApiFacadePushConfiguration.CONFIG_APN_SOUND_FILENAME
        );
        builder.configFileNames(MobileApiFacadePushConfiguration.CONFIG_CLIENT_IDS_YAML);
        return builder.build();
    }

}
