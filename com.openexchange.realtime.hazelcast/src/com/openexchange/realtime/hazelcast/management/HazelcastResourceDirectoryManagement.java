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

package com.openexchange.realtime.hazelcast.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.hazelcast.directory.HazelcastResourceDirectory;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableResource;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;


/**
 * {@link HazelcastResourceDirectoryManagement}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HazelcastResourceDirectoryManagement extends ManagementObject<HazelcastResourceDirectoryMBean> implements HazelcastResourceDirectoryMBean {

    private ObjectName objectName = null;
    private final HazelcastResourceDirectory resourceDirectory;

    /**
     * Initializes a new {@link RealtimeConfigManagement}.
     * @param mbeanInterface
     * @param isMxBean
     * @throws IllegalArgumentException if the String is missing
     */
    public HazelcastResourceDirectoryManagement(HazelcastResourceDirectory resourceDirectory) {
        super(HazelcastResourceDirectoryMBean.class);
        this.resourceDirectory = resourceDirectory;
    }

    @Override
    public ObjectName getObjectName() {
        if (objectName == null) {
            String directoryName = "HazelcastResourceDirectory";
            try {
                objectName = new ObjectName("com.openexchange.realtime", "name", directoryName);
            } catch (MalformedObjectNameException e) {
                // can't happen: valid domain and no missing parameters
            } catch (NullPointerException e) {
                // can't happen: valid domain and no missing parameters
            }
        }
        return objectName;
    }

    /**
     * Get the mapping of general IDs to full IDs e.g. marc.arens@premium <-> ox://marc.arens@premium/random.
     *
     * @return the map used for mapping general IDs to full IDs.
     * @throws OXException if the HazelcastInstance is missing.
     */
    @Override
    public Map<String, List<String>> getIDMapping() throws OXException {
        MultiMap<PortableID,PortableID> idMapping = resourceDirectory.getIDMapping();
        Set<PortableID> generalIds = idMapping.keySet();
        Map<String, List<String>> jmxMap = new HashMap<String, List<String>>(generalIds.size());
        for (PortableID generalId : generalIds) {
            Collection<PortableID> concreteIds = idMapping.get(generalId);
            ArrayList<String> concreteIdRepresentations = new ArrayList<String>(concreteIds.size());
            for (PortableID concreteId : concreteIds) {
                concreteIdRepresentations.add(concreteId.toString());
            }
            jmxMap.put(generalId.toString(), concreteIdRepresentations);
        }
        return jmxMap;
    }

    /**
     * Get the mapping of full IDs to the Resource e.g. ox://marc.arens@premuim/random <-> ResourceMap.
     *
     * @return the map used for mapping full IDs to ResourceMaps.
     * @throws OXException if the map couldn't be fetched from hazelcast
     */
    @Override
    public Map<String, String> getResourceMapping() throws OXException {
        IMap<PortableID,PortableResource> resourceMapping = resourceDirectory.getResourceMapping();
        Map<String,String> jmxMap = new HashMap<String,String>(resourceMapping.size());
        for (Entry<PortableID, PortableResource> entry : resourceMapping.entrySet()) {
            PortableID concreteID = entry.getKey();
            PortableResource resource = entry.getValue();
            jmxMap.put(concreteID.toString(), resource.toString());
        }
        return jmxMap;
    }


}
