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

package com.openexchange.pluginsloaded.mbean.osgi;

import org.slf4j.Logger;
import com.openexchange.management.ManagementService;
import com.openexchange.management.osgi.HousekeepingManagementTracker;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pluginsloaded.PluginsLoadedService;
import com.openexchange.pluginsloaded.mbean.PluginsLoadedMBean;
import com.openexchange.pluginsloaded.mbean.impl.PluginsLoadedMBeanImpl;


/**
 * {@link PluginsLoadedActivator}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.8.4
 */
public class PluginsLoadedActivator extends HousekeepingActivator {

    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PluginsLoadedActivator.class);

    public PluginsLoadedActivator(){
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { PluginsLoadedService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        PluginsLoadedService pluginsLoadedService = getService(PluginsLoadedService.class);
        track(ManagementService.class, new HousekeepingManagementTracker(   context, 
                                                                            PluginsLoadedMBean.class.getName(), 
                                                                            PluginsLoadedMBean.DOMAIN, 
                                                                            new PluginsLoadedMBeanImpl(pluginsLoadedService)));
        openTrackers();
    }

}
