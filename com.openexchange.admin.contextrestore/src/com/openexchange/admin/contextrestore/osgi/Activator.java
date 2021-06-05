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

package com.openexchange.admin.contextrestore.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore;
import com.openexchange.admin.daemons.AdminDaemonService;
import com.openexchange.admin.plugin.hosting.rmi.impl.OXContext;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link Activator} - The activator for <b><code>com.openexchange.admin.contextrestore</code></b> bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Activator extends HousekeepingActivator {

    private static final AtomicReference<OXContextInterface> OXContextInterfaceReference = new AtomicReference<OXContextInterface>();

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Activator.class);
        try {
            AdminCache.compareAndSetBundleContext(null, this.context);
            final ConfigurationService service = getService(ConfigurationService.class);
            // Parse ConfigDB name from "writeUrl" property in file configdb.properties
            {
                final String jdbcUrl = service.getProperty("writeUrl", "jdbc:mysql://localhost/configdb");
                int pos = jdbcUrl.indexOf("://");
                pos = jdbcUrl.indexOf('/', pos > 0 ? pos + 3 : 0);
                if (pos > 0) {
                    OXContextRestore.setConfigDbName(jdbcUrl.substring(pos + 1));
                }
            }
            // Continue
            AdminCache.compareAndSetConfigurationService(null, service);
            OXContextInterfaceReference.set(new OXContext());
            // Register service
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put("RMI_NAME", OXContextRestore.RMI_NAME);
            registerService(Remote.class, new OXContextRestore(), serviceProperties);
            log.info("RMI Interface for context restore registered.");
        } catch (StorageException e) {
            log.error("Error while creating instance for OXContextRestoreInterface interface", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        OXContextInterfaceReference.set(null);
    }

    /**
     * Gets the {@link OXContextInterface} instance.
     *
     * @return The {@link OXContextInterface} instance or <code>null</code>
     */
    public static OXContextInterface getContextInterface() {
        return OXContextInterfaceReference.get();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, AdminDaemonService.class };
    }

}
