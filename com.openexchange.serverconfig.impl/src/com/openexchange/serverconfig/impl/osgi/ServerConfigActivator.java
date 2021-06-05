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

package com.openexchange.serverconfig.impl.osgi;

import java.util.Collections;
import java.util.List;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.serverconfig.ClientServerConfigFilter;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.serverconfig.ServerConfigServicesLookup;
import com.openexchange.serverconfig.impl.ServerConfigServiceImpl;
import com.openexchange.serverconfig.impl.values.Capabilities;
import com.openexchange.serverconfig.impl.values.ForcedHttpsValue;
import com.openexchange.serverconfig.impl.values.Hosts;
import com.openexchange.serverconfig.impl.values.Languages;
import com.openexchange.serverconfig.impl.values.Prefix;
import com.openexchange.serverconfig.impl.values.ServerVersion;
import com.openexchange.session.SessionHolder;
import com.openexchange.version.VersionService;

/**
 * {@link ServerConfigActivator}
 */
public class ServerConfigActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ServerConfigActivator}.
     */
    public ServerConfigActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, ConfigViewFactory.class, CapabilityService.class, VersionService.class };
    }

    @Override
    protected void startBundle() throws Exception {

        final NearRegistryServiceTracker<ComputedServerConfigValueService> computedValueTracker = new NearRegistryServiceTracker<ComputedServerConfigValueService>(
            context,
            ComputedServerConfigValueService.class);
        rememberTracker(computedValueTracker);

        final NearRegistryServiceTracker<ClientServerConfigFilter> configFilterTracker = new NearRegistryServiceTracker<ClientServerConfigFilter>(
            context,
            ClientServerConfigFilter.class
        );
        rememberTracker(configFilterTracker);
        trackService(DispatcherPrefixService.class);
        trackService(SessionHolder.class);
        openTrackers();

        ServerConfigServicesLookup serverConfigServicesLookup = new ServerConfigServicesLookup() {

            @Override
            public List<ComputedServerConfigValueService> getComputed() {
                return Collections.unmodifiableList(computedValueTracker.getServiceList());
            }

            @Override
            public List<ClientServerConfigFilter> getClientFilters() {
                return configFilterTracker.getServiceList();
            }

        };

        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                ServerConfigServiceImpl.invalidateCache();
            }

        });

        // Register the services that add computed values during creation of the server config
        registerService(ComputedServerConfigValueService.class, new ForcedHttpsValue(this));
        registerService(ComputedServerConfigValueService.class, new Hosts());
        registerService(ComputedServerConfigValueService.class, new Languages(this));
        registerService(ComputedServerConfigValueService.class, new ServerVersion(getServiceSafe(VersionService.class)));
        registerService(ComputedServerConfigValueService.class, new Capabilities(this));
        registerService(ComputedServerConfigValueService.class, new Prefix(this));

        // The actual config service
        ServerConfigServiceImpl serverConfigServiceImpl = new ServerConfigServiceImpl(this, serverConfigServicesLookup);
        registerService(ServerConfigService.class, serverConfigServiceImpl);
    }

}
