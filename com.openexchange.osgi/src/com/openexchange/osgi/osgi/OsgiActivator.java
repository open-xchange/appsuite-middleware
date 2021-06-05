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

package com.openexchange.osgi.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.console.ServiceStateLookup;
import com.openexchange.osgi.console.osgi.ConsoleActivator;
import com.openexchange.osgi.rmi.DeferredActivatorRMIServiceImpl;

/**
 * {@link OsgiActivator} - Activator for OSGi-Bundle
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class OsgiActivator extends HousekeepingActivator {

    private volatile ConsoleActivator consoleActivator;

    /**
     * Initializes a new {@link OsgiActivator}.
     */
    public OsgiActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OsgiActivator.class);
        logger.info("starting bundle: com.openexchange.osgi");
        try {
            registerService(ServiceStateLookup.class, DeferredActivator.getLookup());
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put("RMI_NAME", DeferredActivatorRMIServiceImpl.RMI_NAME);
            registerService(Remote.class, new DeferredActivatorRMIServiceImpl(), serviceProperties);
            openTrackers();
            final ConsoleActivator consoleActivator = new ConsoleActivator();
            consoleActivator.start(context);
            this.consoleActivator = consoleActivator;
        } catch (Exception e) {
            logger.error("OsgiActivator: start", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OsgiActivator.class);
        logger.info("stopping bundle: com.openexchange.osgi");
        try {
            final ConsoleActivator consoleActivator = this.consoleActivator;
            if (null != consoleActivator) {
                consoleActivator.stop(context);
                this.consoleActivator = null;
            }
            cleanUp();
        } catch (Exception e) {
            logger.error("OsgiActivator: stop", e);
            throw e;
        }
    }
}
