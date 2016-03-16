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

import java.util.Set;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.hazelcast.core.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.hazelcast.directory.HazelcastResourceDirectory;
import com.openexchange.realtime.hazelcast.serialization.PortableContextPredicate;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableResource;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.packet.ID;

/**
 * {@link GlobalRealtimeCleanupManagement}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class GlobalRealtimeCleanupManagement extends ManagementObject<GlobalRealtimeCleanupMBean> implements GlobalRealtimeCleanupMBean {

    private ObjectName objectName = null;

    private final GlobalRealtimeCleanup globalRealtimeCleanup;

    private final HazelcastResourceDirectory hazelcastResourceDirectory;

    /**
     * Initializes a new {@link RealtimeConfigManagement}.
     * 
     * @param mbeanInterface
     * @param isMxBean
     * @throws IllegalArgumentException if the String is missing
     */
    public GlobalRealtimeCleanupManagement(GlobalRealtimeCleanup globalRealtimeCleanup, HazelcastResourceDirectory hazelcastResourceDirectory) {
        super(GlobalRealtimeCleanupMBean.class);
        this.globalRealtimeCleanup = globalRealtimeCleanup;
        this.hazelcastResourceDirectory = hazelcastResourceDirectory;
    }

    @Override
    public ObjectName getObjectName() {
        if (objectName == null) {
            String cleanupName = "GlobalRealtimeCleanup";
            try {
                objectName = new ObjectName("com.openexchange.realtime", "name", cleanupName);
            } catch (MalformedObjectNameException e) {
                // can't happen: valid domain and no missing parameters
            } catch (NullPointerException e) {
                // can't happen: valid domain and no missing parameters
            }
        }
        return objectName;
    }

    @Override
    protected String getDescription(MBeanInfo info) {
        return "MBean to trigger global realtime cleanups";
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        MBeanOperationInfo[] operations = new MBeanOperationInfo[3];
        MBeanParameterInfo[] cleanIdParameterInfo = new MBeanParameterInfo[1];
        cleanIdParameterInfo[0] = new MBeanParameterInfo("id", "string", "the id");
        operations[0] = getCleanIdOperationInfo();
        operations[1] = getCleanContextOperationInfo();
        operations[2] = getCleanAllOperationInfo();
        MBeanInfo mBeanInfo = new MBeanInfo(GlobalRealtimeCleanupManagement.class.getName(), "MBean to trigger global realtime cleanups based on IDs found in the HazelcastResourceDirectory", null, null, operations, null);
        return mBeanInfo;
    }

    @Override
    public void cleanId(String input) {
        ID idToClean = null;
        idToClean = new ID(input);
        globalRealtimeCleanup.cleanForId(idToClean);
    }

    private MBeanOperationInfo getCleanIdOperationInfo() {
        MBeanParameterInfo[] parameterInfo = new MBeanParameterInfo[1];
        parameterInfo[0] = new MBeanParameterInfo("id", String.class.getName(), "The String representation of the ID to clean. Example: x://93@1/664bbb2d-560f-4a85-9587-6e88dc84f32a, synthetic.office://operations@1/28176.217492");
        return new MBeanOperationInfo("cleanId", "Clean up states that were kept for the given id.", parameterInfo, "void", MBeanOperationInfo.ACTION);
    }

    @Override
    public void cleanContext(String contextId) throws Exception {
        try {
            IMap<PortableID,PortableResource> resourceMapping = hazelcastResourceDirectory.getResourceMapping();
            Set<PortableID> keySet = resourceMapping.keySet(new PortableContextPredicate(contextId));
            for (PortableID id : keySet) {
                globalRealtimeCleanup.cleanForId(id);
            }
        } catch (OXException oxe) {
            throw new Exception(oxe.getLogMessage());
        }
    }

    private MBeanOperationInfo getCleanContextOperationInfo() {
        MBeanParameterInfo[] parameterInfo = new MBeanParameterInfo[1];
        parameterInfo[0] = new MBeanParameterInfo("contextId", String.class.getName(), "The String representation of the ID of the context to clean. Example: 1, internal");
        return new MBeanOperationInfo("cleanContext", "Clean up states that were kept for a single context.", parameterInfo, "void", MBeanOperationInfo.ACTION);
    }

    @Override
    public void cleanAll() throws Exception {
        try {
            IMap<PortableID, PortableResource> resourceMapping = hazelcastResourceDirectory.getResourceMapping();
            Set<PortableID> keySet = resourceMapping.keySet();
            for (PortableID id : keySet) {
                globalRealtimeCleanup.cleanForId(id);
            }
        } catch (OXException oxe) {
            throw new Exception(oxe.getLogMessage());
        }
    }

    private MBeanOperationInfo getCleanAllOperationInfo() {
        MBeanParameterInfo[] parameterInfo = new MBeanParameterInfo[1];
        parameterInfo[0] = new MBeanParameterInfo("contextId", String.class.getName(), "The String representation of the ID of the context to clean. Example: 1, internal");
        return new MBeanOperationInfo("cleanAll", "Clean up states that were kept for all contexts", null, "void", MBeanOperationInfo.ACTION);
    }

}
