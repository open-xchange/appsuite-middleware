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

package com.openexchange.pluginsloaded.mbean.impl;

import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import com.openexchange.management.AnnotatedStandardMBean;
import com.openexchange.pluginsloaded.PluginsLoadedService;
import com.openexchange.pluginsloaded.mbean.PluginsLoadedMBean;

/**
 * {@link PluginsLoadedMBeanImpl}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.8.4
 */
public class PluginsLoadedMBeanImpl extends AnnotatedStandardMBean implements PluginsLoadedMBean{

    private final PluginsLoadedService pluginsLoadedService;    
    
    /**
     * Initializes a new {@link PluginsLoadedMBeanImpl}.
     * @param pluginsLoadedService, represents the service to check the status of the loaded plug-ins
     * @throws NotCompliantMBeanException
     */
    public PluginsLoadedMBeanImpl(PluginsLoadedService pluginsLoadedService) throws NotCompliantMBeanException {
        super("MBean for Pluginsloaded", PluginsLoadedMBean.class);
        this.pluginsLoadedService = pluginsLoadedService;
    }

    @Override
    public boolean allPluginsLoaded() throws MBeanException {        
        return pluginsLoadedService.allPluginsloaded();
    }

}
