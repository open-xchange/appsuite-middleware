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

package com.openexchange.realtime.hazelcast.group;

import static com.openexchange.realtime.hazelcast.serialization.PortableID.pID;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementAware;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.group.DistributedGroupManager;
import com.openexchange.realtime.group.InactivityNotice;
import com.openexchange.realtime.group.StampedGroup;
import com.openexchange.realtime.group.commands.LeaveCommand;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.management.DistributedGroupManagerMBean;
import com.openexchange.realtime.hazelcast.management.DistributedGroupManagerManagement;
import com.openexchange.realtime.hazelcast.osgi.Services;
import com.openexchange.realtime.hazelcast.serialization.PortableID;
import com.openexchange.realtime.hazelcast.serialization.PortableStampedGroup;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.synthetic.SyntheticChannel;
import com.openexchange.realtime.util.Duration;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link DistributedGroupManager} - Allows acces to the distributed group infos stored in Hazelcast.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class DistributedGroupManagerImpl implements ManagementAware<DistributedGroupManagerMBean>, DistributedGroupManager, RealtimeJanitor {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedGroupManagerImpl.class);
    private final MessageDispatcher messageDispatcher;
    private GlobalRealtimeCleanup globalCleanup;
    private final String client_map;
    private final String group_map;
    private final IDMap<Duration> inactivityMap;
    private final DistributedGroupManagerManagement managementObject;
    private final DistributedGroupManagerCleaner cleaner;

    /**
     * Initializes a new {@link DistributedGroupManagerImpl}.
     * 
     * @param messageDispatcher The MessageDispatcher 
     * @param globalCleanup The GlobalRealtimeCleanup
     * @param client_map The name of the client map
     * @param group_map The name of group map
     */
    public DistributedGroupManagerImpl(MessageDispatcher messageDispatcher, GlobalRealtimeCleanup globalCleanup, String client_map, String group_map) {
        this.messageDispatcher = messageDispatcher;
        this.globalCleanup = globalCleanup;
        this.client_map=client_map;
        this.group_map=group_map;
        this.inactivityMap = new IDMap<Duration>(true);
        this.managementObject = new DistributedGroupManagerManagement(client_map);
        this.cleaner = new DistributedGroupManagerCleaner(this);
    }

    /**
     * Gets the DistributedGroupManagerCleaner that reacts to eviction.
     *
     * @return The cleaner
     */
    public DistributedGroupManagerCleaner getCleaner() {
        return cleaner;
    }

    public boolean add(ID client, StampedGroup stampedGroup) throws OXException {
        Validate.notNull(client, "Client must not be null");
        Validate.notNull(stampedGroup, "Stamped group must not be null");
        return getClientToGroupsMapping().put(pID(client), new PortableStampedGroup(stampedGroup));
    }

    public Collection<ID> remove(ID client) throws OXException {
        Validate.notNull(client, "ID must not be null");
        Collection<PortableStampedGroup> removedGroups = getClientToGroupsMapping().remove(pID(client));
        Set<ID> groups = removeStamp(removedGroups);
        LOG.debug("Removed client to group association {} <-> {}", client, groups, !groups.isEmpty());
        sendLeave(client, groups);
        return groups;
    }

    /**
     * Send a @{@link Stanza} containing a {@link LeaveCommand} from a client to a {@link Collection} of groups.
     * 
     * @param client The client that leaves a group.
     * @param groups The groups that the client should leave.
     */
    private void sendLeave(ID client, Collection<ID> groups) {
        Validate.notNull(client, "Client must not be null");
        Validate.notNull(groups, "Groups must not be null");
        ThreadPoolService threadPoolService = Services.getService(ThreadPoolService.class);
        for (ID group : groups) {
            try {
                LOG.debug("Sending leave on behalf of {} to {}.", client, group);
                threadPoolService.submit(new SendLeaveTask(messageDispatcher, client, group));
            } catch (Exception e) {
                LOG.error("Unable to remove client {} from group {}.", client, group, e);
            }
        }
    }

    public boolean remove(ID client, StampedGroup stampedGroup) throws OXException {
        Validate.notNull(client, "Client must not be null");
        Validate.notNull(stampedGroup, "StampedGroup must not be null");
        boolean removed = getClientToGroupsMapping().remove(pID(client), new PortableStampedGroup(stampedGroup));
        LOG.info("Removed client to group association {} <-> {}: {}", client, stampedGroup.getGroupID(), removed);
        return removed;
    }

    public Set<ID> getGroups(ID id) throws OXException {
        Validate.notNull(id, "ID must not be null");
        Collection<PortableStampedGroup> stampedGroups = getClientToGroupsMapping().get(pID(id));
        HashSet<ID> groups = new HashSet<ID>(stampedGroups.size());
        for (PortableStampedGroup portableStampedGroup : stampedGroups) {
            groups.add(portableStampedGroup.getGroupID());
        }
        return groups;
    }

    public Set<ID> getMembers(ID id) throws OXException {
        Validate.notNull(id, "ID must not be null");
        HashSet<ID> members = new HashSet<ID>(getGroupToMembersMapping().get(pID(id)));
        return members;
    }

    public void setInactivity(ID id, Duration duration) throws OXException {
        Validate.notNull(id, "ID must not be null");
        Validate.notNull(duration, "Duration must not be null");
        Duration old = inactivityMap.put(id, duration);
        if(!duration.equals(old)) {
            for(ID group : getGroups(id)) {
                try {
                    LOG.debug("Informing GroupDispatcher {} about inactivity of client {} with Duration of {}.", group, id, duration);
                    messageDispatcher.send(new InactivityNotice(group, id, duration));
                } catch (OXException e) {
                    LOG.error("Unable to inform GroupDispatcher {} about inactivity of client {} with Duration of {}.", group, id, duration, e);
                }
            }
        }
    }

    @Override
    public void cleanupForId(ID id) {
        if(SyntheticChannel.PROTOCOL.equals(id.getProtocol())) {
            cleanupForSyntheticId(id);
        } else {
            cleanupForClientId(id);
        }
    }

    private void cleanupForSyntheticId(ID id) {
        /*
         * - Find clients that are member of this group
         * - Remove every client <-> group mapping
         * - Inform client by sending a NotMember Exception 
         */
        LOG.debug("Cleanup for synthetic id {}", id);
        try {
            Set<ID> members = getMembers(id);
            //find matching client <-> stampedgroup mappings
            getClientToGroupsMapping().
        } catch (OXException e) {
            LOG.error("Error while cleaning for ID {}", id, e);
        }
    }

    private void cleanupForClientId(ID id) {
        try {
            Collection<ID> removed = remove(id);
            inactivityMap.remove(id);
            LOG.debug("Cleanup for ID: {}. Removed from groups: {}", id, removed);
        } catch (Exception e) {
            LOG.error("Error while cleaning for ID {}", id, e);
        }
    }

    /**
     * Get mapping of one client to many groups
     * 
     * @return A {@link MultiMap} of one client to many groups
     */
    private MultiMap<ID, PortableStampedGroup> getClientToGroupsMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(client_map);
    }

    /**
     * Get mappings of one group to many members
     * 
     * @return A @ MultiMap} of one group to many members
     */
    private MultiMap<PortableID, PortableID> getGroupToMembersMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(group_map);
    }

    @Override
    public ManagementObject<DistributedGroupManagerMBean> getManagementObject() {
        return managementObject;
    }

    private Set<ID> removeStamp(Collection<? extends StampedGroup> stampedGroups) {
        Set<ID> groups = new HashSet<ID>(stampedGroups.size());
        for (StampedGroup stampedGroup : stampedGroups) {
            groups.add(stampedGroup.getGroupID());
        }
        return groups;
    }

}
