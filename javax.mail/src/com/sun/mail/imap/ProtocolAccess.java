/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.sun.mail.imap;

import java.net.InetAddress;
import java.util.Properties;
import com.sun.mail.iap.Protocol;

/**
 * {@link ProtocolAccess} - Provides information about (possibly authenticated user) and access to low-level IMAP protocol.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public abstract class ProtocolAccess {

    /**
     * Gets the access instance for specified connected protocol.
     *
     * @param protocol The connected protocol
     * @return The access
     */
    public static ProtocolAccess instanceFor(Protocol protocol) {
        return new ConnectedProtocolAccess(protocol);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the protocol instance provided that this instance wraps an already connected IMAP protocol.
     *
     * @return The protocol
     * @throws IllegalStateException If this instance does not wrap an already connected IMAP protocol
     */
    public abstract Protocol getProtocol();

    /**
     * Gets the protocol properties.
     *
     * @return The properties
     */
    public abstract Properties getProps();

    /**
     * Gets the host
     *
     * @return The host
     */
    public abstract String getHost();

    /**
     * Gets the port
     *
     * @return The port
     */
    public abstract int getPort();

    /**
     * Gets the user
     *
     * @return The user
     */
    public abstract String getUser();

    /**
     * Gets the address of the socket connection.
     *
     * @return The address
     */
    public abstract InetAddress getInetAddress();

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
    }

    private static final class UnconnectedProtocolAccess extends ProtocolAccess {

        private final InetAddress address;
        private final String user;
        private final int port;
        private final String host;
        private final Properties props;

        UnconnectedProtocolAccess(String user, int port, String host, InetAddress address, Properties props) {
            super();
            this.address = address;
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
            return address;
        }
    }
}
