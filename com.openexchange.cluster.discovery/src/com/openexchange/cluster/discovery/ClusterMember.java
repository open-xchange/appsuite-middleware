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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.cluster.discovery;

import java.net.InetAddress;

/**
 * {@link ClusterMember} - Represents a cluster member.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ClusterMember {

    /**
     * Creates a new {@link ClusterMember} from given IP address leaving port to <code>-1</code>.
     *
     * @param inetAddress The IP address
     * @return The {@link ClusterMember} instance
     */
    public static ClusterMember valueOf(final InetAddress inetAddress) {
        return new ClusterMember(inetAddress, -1);
    }

    /**
     * Creates a new {@link ClusterMember} from given IP address and port.
     *
     * @param inetAddress The IP address
     * @param port The optional port or <code>-1</code> if unknown
     * @return The {@link ClusterMember} instance
     */
    public static ClusterMember valueOf(final InetAddress inetAddress, final int port) {
        return new ClusterMember(inetAddress, port);
    }

    // ----------------------------------------------------------------------------- //

    private final InetAddress inetAddress;
    private final int port;
    private final int hash;

    /**
     * Initializes a new {@link ClusterMember}.
     *
     * @param inetAddress The IP address
     * @param port The optional port or <code>-1</code> if unknown
     */
    private ClusterMember(final InetAddress inetAddress, final int port) {
        super();
        this.inetAddress = inetAddress;
        this.port = port;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inetAddress == null) ? 0 : inetAddress.hashCode());
        result = prime * result + port;
        hash = result;
    }

    /**
     * Gets the IP address.
     *
     * @return The IP address
     */
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    /**
     * Gets the optional port.
     *
     * @return The port or <code>-1</code>
     */
    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClusterMember)) {
            return false;
        }
        final ClusterMember other = (ClusterMember) obj;
        if (inetAddress == null) {
            if (other.inetAddress != null) {
                return false;
            }
        } else if (!inetAddress.equals(other.inetAddress)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(48);
        builder.append("ClusterMember [");
        if (inetAddress != null) {
            builder.append("inetAddress=").append(inetAddress).append(", ");
        }
        builder.append("port=").append(port).append(']');
        return builder.toString();
    }

}
