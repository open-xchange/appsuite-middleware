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

package com.openexchange.ajax.ipcheck.osgi;

import com.openexchange.ajax.ipcheck.IPCheckService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link IPCheckActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class IPCheckActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link IPCheckActivator}.
     */
    public IPCheckActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        IPCheckServiceImpl.initialize(getService(ConfigurationService.class));

        {
            ForcedReloadable reloadable = new ForcedReloadable() {

                @Override
                public void reloadConfiguration(ConfigurationService configService) {
                    IPCheckServiceImpl.invalidateCaches();
                }

            };
            registerService(ForcedReloadable.class, reloadable);
        }

        IPCheckServiceImpl service = new IPCheckServiceImpl(context, this);
        rememberTracker(service);
        openTrackers();
        registerService(IPCheckService.class, service);
    }

}
