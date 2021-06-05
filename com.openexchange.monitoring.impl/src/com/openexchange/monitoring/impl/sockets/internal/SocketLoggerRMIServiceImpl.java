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

package com.openexchange.monitoring.impl.sockets.internal;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.monitoring.sockets.SocketLoggerRMIService;
import com.openexchange.monitoring.sockets.SocketLoggerRegistryService;
import com.openexchange.monitoring.sockets.exceptions.BlackListException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SocketLoggerRMIServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class SocketLoggerRMIServiceImpl implements SocketLoggerRMIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketLoggerRMIServiceImpl.class);

    private final ServiceLookup services;

    /**
     * Initialises a new {@link SocketLoggerRMIServiceImpl}.
     */
    public SocketLoggerRMIServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void unregisterLoggerFor(String name) throws RemoteException {
        LOGGER.info("Remote invocation: Unregister logger '{}' for socket logging", name);
        SocketLoggerRegistryService registryService = services.getOptionalService(SocketLoggerRegistryService.class);
        if (registryService == null) {
            return;
        }
        registryService.unregisterLoggerFor(name);
    }

    @Override
    public void registerLoggerFor(String name) throws RemoteException {
        LOGGER.info("Remote invocation: Register logger '{}' for socket logging", name);
        SocketLoggerRegistryService registryService = services.getOptionalService(SocketLoggerRegistryService.class);
        if (registryService == null) {
            return;
        }
        try {
            registryService.registerLoggerFor(name);
        } catch (BlackListException e) {
            throw new RemoteException("", e);
        }
    }

    @Override
    public Set<String> getRegisteredLoggers() throws RemoteException {
        LOGGER.info("Remote invocation: List registered loggers");
        SocketLoggerRegistryService registryService = services.getOptionalService(SocketLoggerRegistryService.class);
        return (registryService == null) ? Collections.emptySet() : registryService.getAllLoggerNames();
    }

    @Override
    public Set<String> getBlacklistedLoggers() throws RemoteException {
        LOGGER.info("Remote invocation: List blacklisted loggers");
        SocketLoggerRegistryService registryService = services.getOptionalService(SocketLoggerRegistryService.class);
        return registryService.getBlackListedLoggerNames();
    }
}
