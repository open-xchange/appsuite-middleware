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

package com.openexchange.rmi.internal;

import java.lang.reflect.Field;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.rmi.exceptions.RMIExceptionCodes;

/**
 * {@link RMIUtility}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RMIUtility {

    /**
     * Initializes a new {@link RMIUtility}.
     */
    private RMIUtility() {
        super();
    }

    /**
     * Creates a new <code>java.rmi.registry.Registry</code> instance.
     *
     * @param configService The configuration service to utilize
     * @return The <code>java.rmi.registry.Registry</code> instance
     * @throws OXException If initialization fails
     */
    public static Registry createRegistry(ConfigurationService configService) throws OXException {
        try {
            int port = configService.getIntProperty("com.openexchange.rmi.port", 1099);
            String hostname = configService.getProperty("com.openexchange.rmi.host", "localhost").trim();
            System.setProperty("java.rmi.server.useCodebaseOnly", "true");
            return LocateRegistry.createRegistry(port, RMISocketFactory.getDefaultSocketFactory(), createServerSocketFactoryFor(hostname));
        } catch (RemoteException e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RMIUtility.class);
            logger.error("", e);
            throw RMIExceptionCodes.RMI_CREATE_REGISTRY_FAILED.create(e);
        }
    }

    private static RMIServerSocketFactory createServerSocketFactoryFor(String hostname) {
        return "0".equals(hostname) ? UnboundServerFactory.getInstance() : new BoundServerFactory(hostname);
    }

    /**
     * Looks up the appropriate name to associate with the remote reference
     *
     * @param reference The service reference for the remote reference
     * @param r The remote reference
     * @return The name to associate with the remote reference
     */
    public static String findRMIName(ServiceReference<Remote> reference, Remote r) {
        // Check for "RMIName"/"RMI_NAME" service property
        {
            Object name = reference.getProperty("RMI_NAME");
            if (name != null) {
                return (String) name;
            }
            name = reference.getProperty("RMIName");
            if (name != null) {
                return (String) name;
            }
        }

        // Look-up by Java Reflection
        try {
            Field field = r.getClass().getField("RMI_NAME");
            return (String) field.get(r);
        } catch (@SuppressWarnings("unused") Exception e) {
            return r.getClass().getSimpleName();
        }
    }

}
