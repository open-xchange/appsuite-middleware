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

package com.openexchange.health.impl.checks;

import com.openexchange.health.MWHealthCheck;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.impl.MWHealthCheckResponseImpl;
import com.openexchange.pluginsloaded.PluginsLoadedService;
import com.openexchange.server.ServiceLookup;


/**
 * {@link AllPluginsLoadedCheck}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class AllPluginsLoadedCheck implements MWHealthCheck {

    private final static String NAME = "allPluginsLoaded";
    private final static long TIMEOUT = 10000L;

    private final ServiceLookup services;

    public AllPluginsLoadedCheck(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public long getTimeout() {
        return TIMEOUT;
    }

    @Override
    public MWHealthCheckResponse call() {
        PluginsLoadedService service = services.getService(PluginsLoadedService.class);
        return new MWHealthCheckResponseImpl(NAME, null, service.allPluginsloaded());
    }

}
