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

package com.openexchange.realtime.hazelcast.cleanup;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementAware;
import com.openexchange.management.ManagementObject;
import com.openexchange.osgi.ShutDownRuntimeException;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanup;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.directory.HazelcastResourceDirectory;
import com.openexchange.realtime.hazelcast.management.GlobalRealtimeCleanupMBean;
import com.openexchange.realtime.hazelcast.management.GlobalRealtimeCleanupManagement;
import com.openexchange.realtime.hazelcast.osgi.Services;
import com.openexchange.realtime.hazelcast.serialization.cleanup.PortableCleanupDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link GlobalRealtimeCleanupImpl}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class GlobalRealtimeCleanupImpl implements GlobalRealtimeCleanup, ManagementAware<GlobalRealtimeCleanupMBean> {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalRealtimeCleanupImpl.class);
    private final HazelcastResourceDirectory hazelcastResourceDirectory;
    private final GlobalRealtimeCleanupManagement management;

    /**
     * Initializes a new {@link GlobalRealtimeCleanupImpl}.
     * @param hazelcastResourceDirectory
     */
    public GlobalRealtimeCleanupImpl(HazelcastResourceDirectory hazelcastResourceDirectory) {
        super();
        this.hazelcastResourceDirectory = hazelcastResourceDirectory;
        management = new GlobalRealtimeCleanupManagement(this, hazelcastResourceDirectory);
    }

    @Override
    public void cleanForId(ID id) {
        LOG.debug("Starting global realtime cleanup for ID: {}", id);

        // Do the local cleanup via a simple service call
        LocalRealtimeCleanup localRealtimeCleanup;
        try {
            localRealtimeCleanup = Services.optService(LocalRealtimeCleanup.class);
            if (localRealtimeCleanup == null) {
                LOG.error("Unable to start local cleanup.", RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(LocalRealtimeCleanup.class));
            } else {
                localRealtimeCleanup.cleanForId(id);
            }
        } catch (ShutDownRuntimeException shutDown) {
            // Shutting down
            LOG.debug("Unable to start local cleanup due to shut-down.", shutDown);
        }

        // Remove from directory after the RealtimeJanitors ran so we don't end up with an unreachable directory entry
        try {
            Collection<ID> removeFromResourceDirectory = removeFromResourceDirectory(id);
            if (removeFromResourceDirectory.isEmpty()) {
                LOG.debug("Unable to remove {} from ResourceDirectory.", id);
            }
        } catch (OXException oxe) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(oxe)) {
                // Hazelcast no more available (either absent or already in "not active" state)
                LOG.debug("Unable to remove {} from ResourceDirectory as Hazelcast is already shut-down.", id, oxe);
            } else {
                LOG.error("Unable to remove {} from ResourceDirectory.", id, oxe);
            }
        }

        // Remote cleanup via distributed MultiTask to remaining members of the cluster
        try {
            HazelcastInstance hazelcastInstance = HazelcastAccess.optHazelcastInstance();
            if (null != hazelcastInstance) {
                Member localMember = HazelcastAccess.getLocalMember();
                Set<Member> clusterMembers = new HashSet<Member>(hazelcastInstance.getCluster().getMembers());
                if(!clusterMembers.remove(localMember)) {
                    LOG.warn("Couldn't remove local member from cluster members.");
                }
                if(!clusterMembers.isEmpty()) {
                    hazelcastInstance.getExecutorService("default").submitToMembers(new PortableCleanupDispatcher(id), clusterMembers);
                } else {
                    LOG.debug("No other cluster members besides the local member. No further clean up necessary.");
                }
            }
        } catch (HazelcastInstanceNotActiveException e) {
            // Hazelcast no more available (either absent or already in "not active" state)
            LOG.debug("Failed to issue remote cleanup for {} as Hazelcast is already shut-down.", id, e);
        } catch (Exception e) {
            LOG.error("Failed to issue remote cleanup for {}.", id, e);
        }
    }

    @Override
    public void cleanForId(ID id, long timestamp) {
        try {
            IDMap<Resource> idMap = hazelcastResourceDirectory.get(id);
            Set<ID> validIDs = new HashSet<ID>();
            Iterator<Entry<ID, Resource>> mapIter = idMap.entrySet().iterator();
            while (mapIter.hasNext()) {
                Entry<ID, Resource> next = mapIter.next();
                if (next.getValue().getTimestamp().getTime() <= timestamp) {
                    validIDs.add(next.getKey());
                }
            }
            hazelcastResourceDirectory.remove(validIDs);
            for (ID idToClean : validIDs) {
                doCleanupForId(idToClean);
            }
        } catch (OXException oxe) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(oxe)) {
                LOG.debug("Failed to clean for ID {} as Hazelcast is already shut-down", id, oxe);
            } else {
                LOG.error("Failed to clean for ID {}", id, oxe);
            }
        }
    }

    @Override
    public Collection<ID> removeFromResourceDirectory(ID id) throws OXException {
        try {
            return hazelcastResourceDirectory.remove(id).keySet();
        } catch (OXException oxe) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(oxe)) {
                LOG.debug("Failed to remove from resource directory for ID {} as Hazelcast is already shut-down", id, oxe);
            } else {
                LOG.error("Failed to remove from resource directory for ID {}", id, oxe);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<ID> removeFromResourceDirectory(Collection<ID> ids) throws OXException {
        try {
            return hazelcastResourceDirectory.remove(ids).keySet();
        } catch (OXException oxe) {
            if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(oxe)) {
                LOG.debug("Failed to remove from resource directory for IDs {} as Hazelcast is already shut-down", ids, oxe);
            } else {
                LOG.error("Failed to remove from resource directory for IDs {}", ids, oxe);
            }
        }
        return Collections.emptyList();
    }

    private void doCleanupForId(ID id) {
        // Do the local cleanup via a simple service call
        LocalRealtimeCleanup localRealtimeCleanup = Services.optService(LocalRealtimeCleanup.class);
        if (localRealtimeCleanup == null) {
            LOG.error(
                "Unable to start local cleanup. Shutting down?",
                RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(LocalRealtimeCleanup.class));
        } else {
            localRealtimeCleanup.cleanForId(id);
        }

        // Remote cleanup via distributed MultiTask to remaining members of the cluster
        HazelcastInstance hazelcastInstance;
        try {
            hazelcastInstance = HazelcastAccess.getHazelcastInstance();
            Member localMember = HazelcastAccess.getLocalMember();
            Set<Member> clusterMembers = new HashSet<Member>(hazelcastInstance.getCluster().getMembers());
            if (!clusterMembers.remove(localMember)) {
                LOG.warn("Couldn't remove local member from cluster members.");
            }
            if (!clusterMembers.isEmpty()) {
                hazelcastInstance.getExecutorService("default").submitToMembers(new PortableCleanupDispatcher(id), clusterMembers);
            } else {
                LOG.debug("No other cluster members besides the local member. No further clean up necessary.");
            }
        } catch (Exception e) {
            LOG.error("Failed to issue remote cleanup for {}.", id, e);
        }
    }

    @Override
    public ManagementObject<GlobalRealtimeCleanupMBean> getManagementObject() {
        return management;
    }

}
