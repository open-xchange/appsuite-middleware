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

package com.openexchange.pluginsloaded.impl.osgi;


import org.slf4j.Logger;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pluginsloaded.PluginsLoadedService;
import com.openexchange.pluginsloaded.impl.PluginsLoadedServiceImpl;

/**
 * {@link PluginsLoadedServiceImplActivator}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.8.4
 */
public class PluginsLoadedServiceImplActivator extends HousekeepingActivator {
    
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PluginsLoadedServiceImplActivator.class);
    
    public PluginsLoadedServiceImplActivator(){
        super();
    }      

    @Override
    protected void startBundle() throws Exception {
        registerService(PluginsLoadedService.class, new PluginsLoadedServiceImpl(context), null);   
        LOGGER.info("Successfully started PluginsLoadedService");
    }    
    
    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();        
        LOGGER.info("Successfully stopped PluginsLoadedService");
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }
}
