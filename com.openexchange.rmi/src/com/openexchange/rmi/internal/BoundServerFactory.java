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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

/**
 * {@link BoundServerFactory} - The RMI server socket factory accepting connections only for any address resolvable from a given host name.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class BoundServerFactory implements RMIServerSocketFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BoundServerFactory.class);

    private final String hostname;

    /**
     * Initializes a new {@link BoundServerFactory}.
     *
     * @param hostname The host name
     */
    public BoundServerFactory(String hostname) {
        super();
        this.hostname = hostname;
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        LOG.info("RMI server socket will only accept connect requests to the addresses of {}!", hostname);
        return new ServerSocket(port, 0, InetAddress.getByName(hostname));
    }
}
