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

package com.openexchange.realtime.hazelcast.cleanup;

import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.directory.HazelcastResource;
import com.openexchange.realtime.hazelcast.directory.HazelcastResourceDirectory;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.IDMap;


/**
 * {@link CleanupMemberShipListener} - Reacts to a member removal by cleaning up resources that have been located on that member.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class CleanupMemberShipListener implements MembershipListener {

    private String cleanupLockMapName;
    
    private HazelcastResourceDirectory directory;

    private GlobalRealtimeCleanup globalCleanup;

    private static final Logger LOG = LoggerFactory.getLogger(CleanupMemberShipListener.class);

    /**
     * Initializes a new {@link CleanupMemberShipListener}.
     * @param cleanupLockMapName The name of the map holding the cleanup locks
     * @param directory The HazelcastDirectory instance
     * @param globalCleanup The GlobalRealtimeCleanup instance
     */
    public CleanupMemberShipListener(String cleanupLockMapName, HazelcastResourceDirectory directory, GlobalRealtimeCleanup globalCleanup) {
        super();
        this.cleanupLockMapName = cleanupLockMapName;
        this.directory = directory;
        this.globalCleanup = globalCleanup;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        Member memberToClean = membershipEvent.getMember();
        String uuid = memberToClean.getUuid();
        try {
            IMap<String, CleanupStatus> cleanupMapping = getCleanupMapping();
            cleanupMapping.lock(uuid);
            try {
                CleanupStatus cleanupStatus = cleanupMapping.get(uuid);
                // is somebody already cleaning up for him?
                if (!cleanupStarted(cleanupStatus)) {
                    LOG.info("Starting cleanup for member {} with IP {}", memberToClean.getUuid(), memberToClean.getInetSocketAddress());
                    cleanupStatus = new CleanupStatus(HazelcastAccess.getLocalMember(), memberToClean);
                    //do actual cleanup
                    IDMap<HazelcastResource> resourcesOfMember = directory.getResourcesOfMember(memberToClean);
                    LOG.debug("Found the following resources to clean up: {}", resourcesOfMember);
                    for (Entry<ID, HazelcastResource> entry : resourcesOfMember.entrySet()) {
                        ID id = entry.getKey();
                        HazelcastResource hzResource = entry.getValue();
                        globalCleanup.cleanForId(id, hzResource.getTimestamp().getTime());
                    }
                    //update status and put to map
                    cleanupStatus.setCleaningFinishTime(System.currentTimeMillis());
                    cleanupMapping.put(uuid, cleanupStatus);
                    LOG.debug("CleanupMapping after cleanup: {}", cleanupMapping.entrySet());
                } else {
                    LOG.info(
                        "Cleanup was already started: {}", cleanupStatus);
                }
            } catch (Exception e) {
                LOG.error("Failed to start cleanup after member {} with IP {} left the cluster", uuid, memberToClean.getInetSocketAddress());
            } finally {
                cleanupMapping.unlock(uuid);
            }
        } catch (OXException oxe) {
            LOG.error("Failed to start cleanup after member {} with IP {} left the cluster", uuid, memberToClean.getInetSocketAddress());
        }
    }

    public IMap<String, CleanupStatus> getCleanupMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMap(cleanupLockMapName);
    }

    /**
     * Check the distributed memberCleanup map for a lock indicating that some other member has already started cleaning up.
     * 
     * @param member may be null
     * @return true if member is not null and 
     */
    private boolean cleanupStarted(CleanupStatus cleanupInfo) {
        if(cleanupInfo!= null) {
            //TODO: Additionally check how long ago the cleanup was finished and decide to return true or false depending on the duration
            return true;
        }
        return false;
    }

}
