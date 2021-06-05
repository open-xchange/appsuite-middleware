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

package com.openexchange.health.impl.osgi;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.health.MWHealthCheck;
import com.openexchange.health.MWHealthCheckService;
import com.openexchange.health.impl.HealthCheckResponseProviderImpl;
import com.openexchange.health.impl.MWHealthCheckServiceImpl;
import com.openexchange.health.impl.checks.AllPluginsLoadedCheck;
import com.openexchange.health.impl.checks.ConfigDBCheck;
import com.openexchange.health.impl.checks.HazelcastCheck;
import com.openexchange.health.impl.checks.JVMHeapCheck;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pluginsloaded.PluginsLoadedService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link MWHealthCheckActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class MWHealthCheckActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ThreadPoolService.class, PluginsLoadedService.class, LeanConfigurationService.class,
            DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        MWHealthCheckServiceImpl service = new MWHealthCheckServiceImpl(this);
        registerService(MWHealthCheckService.class, service);

        registerService(MWHealthCheck.class, new AllPluginsLoadedCheck(this));
        registerService(MWHealthCheck.class, new ConfigDBCheck(this));
        registerService(MWHealthCheck.class, new HazelcastCheck(this));
        registerService(MWHealthCheck.class, new JVMHeapCheck());

        HealthCheckResponse.setResponseProvider(new HealthCheckResponseProviderImpl());

        track(MWHealthCheck.class, new MWHealthCheckTracker(context, service));
        track(HealthCheck.class, new HealthCheckTracker(context, service));
        track(HazelcastInstance.class);
        openTrackers();
    }

}
