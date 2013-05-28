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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.hazelcast.directory;

import java.io.Serializable;
import java.util.Date;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.directory.AbstractResource;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.packet.Presence;

/**
 * HazelcastResource - Hazelcast specific {@link Resource} implementation. Can be initialized from an existing DefaultResource.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HazelcastResource extends AbstractResource implements Resource {

    private static final long serialVersionUID = -6363105433259505165L;

    // Hazelcast specific routingInfo
    private Member routingInfo;

    /**
     * Initializes a new {@link HazelcastResource} without a Presence state, with the current date for the resource's timestamp and the
     * routing infos of the local hazelcast member.
     * 
     * @throws OXException If the HazelcastResource can't determine the localMember of the Hazelcast cluster
     */
    public HazelcastResource() throws OXException {
        super();
        this.routingInfo = HazelcastAccess.getLocalMember();
    }

    /**
     * Initializes a new {@link HazelcastResource} with a Presence state, the current date for the resource's timestamp and the routing
     * infos of the local hazelcast member.
     * 
     * @param state The presence state
     * @throws OXException If the HazelcastResource can't determine the localMember of the Hazelcast cluster
     */
    public HazelcastResource(Presence presence) throws OXException {
        this(presence, new Date());
    }

    /**
     * Initializes a new {@link HazelcastResource} with a Presence state, a given timestamp and the routing infos of the local hazelcast
     * member.
     * 
     * @param state The presence state
     * @param timestamp The timestamp
     * @throws OXException If the HazelcastResource can't determine the localMember of the Hazelcast cluster
     */
    public HazelcastResource(Presence presence, Date timestamp) throws OXException {
        super(presence, timestamp);
        this.routingInfo = HazelcastAccess.getLocalMember();
    }

    /**
     * Initializes a new {@link HazelcastResource} from an existing {@link Resource}. During initialization it discards existing routing
     * information and replaces them with the proper Hazelcast Member.
     * 
     * @param defaultResource the existing {@link DefaultResource} must not be null
     * @throws OXException If the HazelcastResource can't determine the localMember of the Hazelcast cluster
     * @throws IllegalStateException If the mandatory parameter defaultResource is missing
     */
    public HazelcastResource(Resource resource) throws OXException {
        if (resource == null) {
            throw new IllegalArgumentException("Mandatory argument argument missing: defaultResource");
        }
        this.presence = resource.getPresence();
        this.timestamp = resource.getTimestamp();
        this.routingInfo = HazelcastAccess.getLocalMember();
    }

    @Override
    public Member getRoutingInfo() {
        return routingInfo;
    }

    @Override
    public void setRoutingInfo(Serializable routingInfo) {
        this.routingInfo = (Member)routingInfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((presence == null) ? 0 : presence.hashCode());
        result = prime * result + ((routingInfo == null) ? 0 : routingInfo.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof HazelcastResource))
            return false;
        HazelcastResource other = (HazelcastResource) obj;
        if (presence == null) {
            if (other.presence != null)
                return false;
        } else if (!presence.equals(other.presence))
            return false;
        if (routingInfo == null) {
            if (other.routingInfo != null)
                return false;
        } else if (!routingInfo.equals(other.routingInfo))
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
        if (routingInfo != null) {
            if (presence != null && presence.getFrom() != null) {
                return presence.getFrom().toString() + "@" + routingInfo.toString();
            }
            return routingInfo.toString();
        }
        return super.toString();
    }

}
