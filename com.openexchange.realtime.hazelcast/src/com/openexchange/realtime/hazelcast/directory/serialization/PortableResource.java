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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.realtime.hazelcast.directory.serialization;

import java.io.IOException;
import java.util.Date;
import com.hazelcast.core.Member;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.packet.Presence;


/**
 * {@link PortableResource}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class PortableResource extends AbstractCustomPortable implements Resource {

    /** The unique portable class ID of the {@link PortableResource} */
    public static final int CLASS_ID = 5;
    
    private PortablePresence presence;
    private Date timestamp;
    private Member routingInfo;

    public PortableResource() throws OXException {
        this.timestamp = new Date();
        this.routingInfo = HazelcastAccess.getLocalMember();
    }

    /**
     * Initializes a new {@link PortableResource} based on another Resource.
     * 
     * @param resource the resource to use as basis
     * @throws OXException
     */
    public PortableResource(Resource resource) throws OXException {
        if(resource.getPresence() != null) {
            this.presence = new PortablePresence(resource.getPresence());
        }
        this.timestamp = resource.getTimestamp();
        this.routingInfo = HazelcastAccess.getLocalMember();
    }

    /**
     * Gets the presence
     *
     * @return The presence
     */
    public PortablePresence getPresence() {
        return presence;
    }

    
    /**
     * Sets the presence
     *
     * @param presence The presence to set
     */
    public void setPresence(PortablePresence presence) {
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

    
    /**
     * Gets the routingInfo
     *
     * @return The routingInfo
     */
    public Member getRoutingInfo() {
        return routingInfo;
    }

    
    /**
     * Sets the routingInfo
     *
     * @param routingInfo The routingInfo to set
     */
    public void setRoutingInfo(Member routingInfo) {
        this.routingInfo = routingInfo;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        if(presence != null) {
            writer.writePortable("presence", presence);
        }
        writer.writeLong("timestamp", timestamp.getTime());
        routingInfo.writeData(writer.getRawDataOutput());
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        if(reader.getFieldNames().contains("presence")) {
            presence  = reader.readPortable("presence");
        }
        long time = reader.readLong("timestamp");
        timestamp = new Date(time);
        routingInfo.readData(reader.getRawDataInput());
    }

    @Override
    public void setPresence(Presence presence) {
        this.presence = new PortablePresence(presence);
    }

    @Override
    public void setRoutingInfo(Object routingInfo) {
        this.routingInfo = (Member)routingInfo;
    }

}
