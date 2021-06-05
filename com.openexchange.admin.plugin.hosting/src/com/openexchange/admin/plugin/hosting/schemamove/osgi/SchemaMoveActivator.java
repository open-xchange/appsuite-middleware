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

package com.openexchange.admin.plugin.hosting.schemamove.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.daemons.AdminDaemonService;
import com.openexchange.admin.plugin.hosting.schemamove.SchemaMoveService;
import com.openexchange.admin.plugin.hosting.schemamove.internal.SchemaMoveImpl;
import com.openexchange.admin.plugin.hosting.schemamove.internal.SchemaMoveRemoteImpl;
import com.openexchange.admin.plugin.hosting.schemamove.mbean.SchemaMoveRemote;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link SchemaMoveActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SchemaMoveActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link SchemaMoveActivator}.
     */
    public SchemaMoveActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ ConfigurationService.class, AdminDaemonService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Logger logger = LoggerFactory.getLogger(SchemaMoveActivator.class);

        SchemaMoveImpl schemaMoveImpl = new SchemaMoveImpl();
        registerService(SchemaMoveService.class, schemaMoveImpl);

        // Register RMI
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put("RMI_NAME", SchemaMoveRemote.RMI_NAME);
        registerService(Remote.class, new SchemaMoveRemoteImpl(schemaMoveImpl), serviceProperties);

        logger.info("Successfully started bundle {}", context.getBundle().getSymbolicName());
    }

}
