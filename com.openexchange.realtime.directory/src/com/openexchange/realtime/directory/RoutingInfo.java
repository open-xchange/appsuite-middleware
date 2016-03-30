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

package com.openexchange.realtime.directory;

import java.net.InetSocketAddress;
import org.apache.commons.lang.Validate;

/**
 * {@link RoutingInfo} - Infos needed to distinguish and address different nodes, immutable.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class RoutingInfo {

    protected InetSocketAddress socketAddress;

    protected String id;

    /**
     * Initializes a new {@link RoutingInfo}.
     */
    protected RoutingInfo() {
        super();
    }

    /**
     * Initializes a new {@link RoutingInfo}.
     * 
     * @param routingInfo must not be null
     */
    public RoutingInfo(RoutingInfo routingInfo) {
        Validate.notNull(routingInfo, "Mandatory argument missing: routingInfo");
        this.socketAddress = routingInfo.socketAddress;
        this.id = routingInfo.id;

    }

    /**
     * Initializes a new {@link RoutingInfo}.
     * 
     * @param socketAddress The address of the routing info, must not be null
     * @param id The unique id of the routing info
     */
    public RoutingInfo(InetSocketAddress socketAddress, String id) {
        super();
        Validate.notNull(socketAddress, "Mandatory argument missing: socketAddress");
        this.socketAddress = socketAddress;
        this.id = id;
    }

    /**
     * Get the {@link InetSocketAddress} of this {@link RoutingInfo}
     * 
     * @return the {@link InetSocketAddress} of this {@link RoutingInfo}
     */
    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    /**
     * Get the id of this {@link RoutingInfo}
     * 
     * @return the id of this {@link RoutingInfo}, might be null if the node represented by this routing info doesn't provide an
     *         unique identifier.
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((socketAddress == null) ? 0 : socketAddress.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof RoutingInfo))
            return false;
        RoutingInfo other = (RoutingInfo) obj;
        if (socketAddress == null) {
            if (other.socketAddress != null)
                return false;
        } else if (!socketAddress.equals(other.socketAddress))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RoutingInfo [address=" + socketAddress + ", id=" + id + "]";
    }

}
