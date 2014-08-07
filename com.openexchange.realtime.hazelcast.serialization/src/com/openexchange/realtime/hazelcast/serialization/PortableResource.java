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

package com.openexchange.realtime.hazelcast.serialization;

import java.io.IOException;
import java.util.Date;
import org.apache.commons.lang.Validate;
import com.hazelcast.core.Member;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.RoutingInfo;

/**
 * {@link PortableResource} Hazelcast specific {@link Portable}{@link Resource} implementation. Can be initialized from an existing
 * DefaultResource.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PortableResource extends DefaultResource implements CustomPortable {

    public static final int CLASS_ID = 10;

    private final static String PRESENCE = "presence";

    private final static String TIMESTAMP = "timestamp";

    private final static String ROUTINGINFO = "routinginfo";

    // routingInfo from Hazelcast member
    private RoutingInfo routingInfo;

    protected PortableResource() {
        // creates a resource without presence but with timestamp
        super();
    }

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
        if(presence != null) {
            writer.writePortable(PRESENCE, new PortablePresence(presence));
        }
        writer.writeLong(TIMESTAMP, timestamp.getTime());
        if(routingInfo!=null) {
            writer.writePortable(ROUTINGINFO, new PortableRoutingInfo(routingInfo));
        }
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        PortablePresence portablePresence = reader.readPortable(PRESENCE);
        presence = portablePresence.getPresence();
        timestamp = new Date(reader.readLong(TIMESTAMP));
        routingInfo = reader.readPortable(ROUTINGINFO);
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
