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

package com.sun.mail.imap;

import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import com.sun.mail.iap.Protocol;
import com.sun.mail.util.ProtocolInfo;

/**
 * {@link ProtocolAccess} - Provides information about (possibly authenticated user) and access to low-level IMAP protocol.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public abstract class ProtocolAccess implements ProtocolInfo {

    /**
     * Gets the connected access instance for specified connected protocol.
     *
     * @param protocol The connected protocol
     * @return The connected access
     */
    public static ProtocolAccess instanceFor(Protocol protocol) {
        return new ConnectedProtocolAccess(protocol);
    }
    
    /**
     * Gets the unconnected access instance for specified arguments.
     * 
     * @param user The user
     * @param host The host
     * @param port The port
     * @param props The properties
     * @return The unconnected access
     */
    public static ProtocolAccess instanceFor(String user, String host, int port, Properties props) {
        return new UnconnectedProtocolAccess(user, port, host, props);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the protocol instance provided that this instance wraps an already connected IMAP protocol.
     *
     * @return The protocol
     * @throws IllegalStateException If this instance does not wrap an already connected IMAP protocol
     */
    public abstract Protocol getProtocol();

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final class ConnectedProtocolAccess extends ProtocolAccess {

        private final Protocol protocol;

        ConnectedProtocolAccess(Protocol protocol) {
            super();
            this.protocol = protocol;
        }

        @Override
        public Protocol getProtocol() {
            return protocol;
        }

        @Override
        public Properties getProps() {
            return protocol.getProps();
        }

        @Override
        public InetAddress getInetAddress() {
            return protocol.getInetAddress();
        }

        @Override
        public String getHost() {
            return protocol.getHost();
        }

        @Override
        public int getPort() {
            return protocol.getPort();
        }

        @Override
        public String getUser() {
            return protocol.getUser();
        }
        
        @Override
        public void setInetAddress(InetAddress address) {
            throw new IllegalStateException("Address cannot be set on a connected instance");
        }
    }

    private static final class UnconnectedProtocolAccess extends ProtocolAccess {

        private final AtomicReference<InetAddress> addressReference;
        private final String user;
        private final int port;
        private final String host;
        private final Properties props;

        UnconnectedProtocolAccess(String user, int port, String host, Properties props) {
            super();
            this.addressReference = new AtomicReference<>();
            this.user = user;
            this.port = port;
            this.host = host;
            this.props = props;
        }

        @Override
        public Protocol getProtocol() {
            throw new IllegalStateException("Unconnected");
        }
        @Override
        public Properties getProps() {
            return props;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public int getPort() {
            return port;
        }

        @Override
        public String getUser() {
            return user;
        }

        @Override
        public InetAddress getInetAddress() {
            return addressReference.get();
        }
        
        @Override
        public void setInetAddress(InetAddress address) {
            addressReference.set(address);
        }
    }
}
