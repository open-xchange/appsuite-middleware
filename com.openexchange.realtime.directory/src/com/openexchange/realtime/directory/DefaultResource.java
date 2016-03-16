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

import java.util.Date;
import com.openexchange.realtime.packet.Presence;

/**
 * {@link DefaultResource} {@link Resource} implementation that doesn't carry any routing information, yet. Routing information is
 * automatically added by a transport specific Resource implmentation e.g. when adding a DefaultResource to a HazelcastResourceDirectory.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class DefaultResource implements Resource {

    protected Presence presence;

    protected Date timestamp;

    /**
     * Initializes a new {@link DefaultResource} without associated {@link Presence}
     */
    public DefaultResource() {
        this(null);
    }

    /**
     * Initializes a new {@link AbstractResource}.
     * 
     * @param state The presence state
     * @param timestamp The timestamp
     */
    public DefaultResource(Presence presence) {
        this(presence, new Date());
    }

    /**
     * Initializes a new {@link DefaultResource}.
     * 
     * @param presence The presence state
     * @param timestamp The timestamp
     */
    public DefaultResource(Presence presence, Date timestamp) {
        this.presence = presence;
        this.timestamp = timestamp;
    }

    /**
     * Gets the presence
     * 
     * @return The presence
     */
    public Presence getPresence() {
        return presence;
    }

    /**
     * Sets the presence
     * 
     * @param presence The presence to set
     */
    public void setPresence(Presence presence) {
        this.presence = presence;
    }

    /**
     * Gets the timestamp
     * 
     * @return The timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp
     * 
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public RoutingInfo getRoutingInfo() {
        return null;
    }

    @Override
    public void setRoutingInfo(RoutingInfo routingInfo) {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((presence == null) ? 0 : presence.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof DefaultResource))
            return false;
        DefaultResource other = (DefaultResource) obj;
        if (presence == null) {
            if (other.presence != null)
                return false;
        } else if (!presence.equals(other.presence))
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DefaultResource [presence=" + presence + ", timestamp=" + timestamp + "]";
    }

}
