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

package com.openexchange.realtime.hazelcast.serialization.directory;

import java.io.IOException;
import java.util.Date;
import org.apache.commons.lang.Validate;
import com.hazelcast.core.Member;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.RoutingInfo;
import com.openexchange.realtime.hazelcast.serialization.packet.PortablePresence;

/**
 * {@link PortableResource} Hazelcast specific {@link Portable}{@link Resource} implementation. Can be initialized from an existing
 * DefaultResource.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PortableResource extends DefaultResource implements CustomPortable {

    public static final int CLASS_ID = 10;

    private final static String FIELD_PRESENCE = "presence";

    private final static String FIELD_TIMESTAMP = "timestamp";

    private final static String FIELD_ROUTINGINFO = "routinginfo";

    public static ClassDefinition CLASS_DEFINITION = null;

    static {
        CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
        .addPortableField(FIELD_PRESENCE, PortablePresence.CLASS_DEFINITION)
        .addLongField(FIELD_TIMESTAMP)
        .addPortableField(FIELD_ROUTINGINFO, PortableRoutingInfo.CLASS_DEFINITION)
        .build();
    }

    private RoutingInfo routingInfo;

    /**
     * Initializes a new {@link PortableResource} without Presence but with the current time.
     */
    protected PortableResource() {
        super();
    }

    /**
     * Initializes a new {@link PortableResource} based on a new DefaultResource and the given member identifying the cluster node.
     * 
     * @param member The member identifying the cluster node
     */
    public PortableResource(Member member) {
        this(new DefaultResource(), member);
    }

    /**
     * Initializes a new {@link PortableResource} based on another Resource instance and the given member identifying the cluster node.
     * 
     * @param resource The other resource
     * @param member The member identifying the cluster node
     */
    public PortableResource(Resource resource, Member member) {
        Validate.notNull(resource, "Mandatory argument missing: resource");
        Validate.notNull(member, "Mandatory argument missing: member");
        this.presence = resource.getPresence();
        this.timestamp = resource.getTimestamp();
        this.routingInfo = new RoutingInfo(member.getSocketAddress(), member.getUuid());
    }

    @Override
    public RoutingInfo getRoutingInfo() {
        return routingInfo;
    }

    @Override
    public void setRoutingInfo(RoutingInfo routingInfo) {
        this.routingInfo = routingInfo;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writePortable(FIELD_PRESENCE, presence == null ? null : new PortablePresence(presence));
        writer.writeLong(FIELD_TIMESTAMP, timestamp.getTime());
        writer.writePortable(FIELD_ROUTINGINFO, new PortableRoutingInfo(routingInfo));
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        PortablePresence portablePresence = reader.readPortable(FIELD_PRESENCE);
        if (portablePresence != null) {
            presence = portablePresence.getPresence();
        }
        timestamp = new Date(reader.readLong(FIELD_TIMESTAMP));
        //http://bugs.java.com/view_bug.do?bug_id=6302954
        routingInfo = reader.<PortableRoutingInfo>readPortable(FIELD_ROUTINGINFO);
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((routingInfo == null) ? 0 : routingInfo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof PortableResource))
            return false;
        PortableResource other = (PortableResource) obj;
        if (routingInfo == null) {
            if (other.routingInfo != null)
                return false;
        } else if (!routingInfo.equals(other.routingInfo))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortableResource [routingInfo=" + routingInfo + ", presence=" + presence + ", timestamp=" + timestamp + "]";
    }

}
