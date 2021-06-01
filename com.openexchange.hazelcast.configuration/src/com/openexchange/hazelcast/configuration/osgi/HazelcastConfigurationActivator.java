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


package com.openexchange.hazelcast.configuration.osgi;

import org.eclipse.osgi.framework.console.CommandProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.configuration.internal.AddNodeUtilCommandProvider;
import com.openexchange.hazelcast.configuration.internal.HazelcastConfigurationServiceImpl;
import com.openexchange.hazelcast.configuration.reloadable.HazelcastDnsNetworkJoinReloadable;
import com.openexchange.hazelcast.configuration.reloadable.HazelcastSSLReloadable;
import com.openexchange.hazelcast.configuration.reloadable.HazelcastStaticNetworkJoinReloadable;
import com.openexchange.hazelcast.dns.HazelcastDnsService;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link HazelcastConfigurationActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class HazelcastConfigurationActivator extends HousekeepingActivator {

    private HazelcastConfigurationServiceImpl configService;

    /**
     * Initializes a new {@link HazelcastConfigurationActivator}.
     */
    public HazelcastConfigurationActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, StringParser.class, DynamicPortableFactory.class, TimerService.class, HazelcastDnsService.class};
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Services.set(this);
        openTrackers();
        HazelcastConfigurationServiceImpl configService = new HazelcastConfigurationServiceImpl();
        this.configService = configService;
        registerService(HazelcastConfigurationService.class, configService);
        registerService(Reloadable.class, new HazelcastStaticNetworkJoinReloadable(configService));
        registerService(Reloadable.class, new HazelcastDnsNetworkJoinReloadable(configService));
        registerService(Reloadable.class, new HazelcastSSLReloadable(configService));
        registerService(CommandProvider.class, new AddNodeUtilCommandProvider(configService));
    }

    @Override
    public synchronized void stopBundle() throws Exception {
        super.stopBundle();
        HazelcastConfigurationServiceImpl configService = this.configService;
        if (configService != null) {
            this.configService = null;
            configService.shutDown();
        }
        Services.set(null);
    }

}
