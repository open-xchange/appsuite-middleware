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
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.group.DistributedGroupManager;
import com.openexchange.realtime.group.InactivityNotice;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.impl.GlobalMessageDispatcherImpl;
import com.openexchange.realtime.hazelcast.management.DistributedGroupManagerMBean;
import com.openexchange.realtime.hazelcast.management.DistributedGroupManagerManagement;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.Duration;
import com.openexchange.realtime.util.IDMap;


/**
 * {@link DistributedGroupManager} - Allows acces to the distributed group infos stored in Hazelcast.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class DistributedGroupManagerImpl implements ManagementAware<DistributedGroupManagerMBean>, DistributedGroupManager, RealtimeJanitor {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedGroupManagerImpl.class);
    private final MessageDispatcher globalMessageDispatcher;
    private final String client_map;
    private final String group_map;
    private final IDMap<Duration> inactivityMap;
    private final DistributedGroupManagerManagement managementObject;

    /**
     * Initializes a new {@link DistributedGroupManagerImpl}.
     * @param globalDispatcher
     */
    public DistributedGroupManagerImpl(GlobalMessageDispatcherImpl globalMessageDispatcher, String client_map, String group_map) {
        this.globalMessageDispatcher = globalMessageDispatcher;
        this.client_map=client_map;
        this.group_map=group_map;
        this.inactivityMap = new IDMap<Duration>(true);
        this.managementObject = new DistributedGroupManagerManagement(client_map);
        
    }

    public boolean add(ID client, ID group) throws OXException {
        return getClientToGroupsMapping().put(client, group);
    }

    public Collection<ID> remove(ID client) throws OXException {
        return getClientToGroupsMapping().remove(client);
    }

    public boolean remove(ID client, ID group) throws OXException {
        return getClientToGroupsMapping().remove(client, group);
    }

    public Set<ID> getGroups(ID id) throws OXException {
        return new HashSet<ID>(getClientToGroupsMapping().get(id));
    }

    public Set<ID> getMembers(ID id) throws OXException {
        throw new UnsupportedOperationException("Not implemented, yet.");
    }

    public void setInactivity(ID id, Duration duration) throws OXException {
        Validate.notNull(id, "ID must not be null");
        Validate.notNull(duration, "Duration must not be null");
        Duration old = inactivityMap.put(id, duration);
        if(!duration.equals(old)) {
            for(ID group : getGroups(id)) {
                try {
                    LOG.debug("Informing GroupDispatcher {} about inactivity of client {} with Duration of {}.", group, id, duration);
                    globalMessageDispatcher.send(new InactivityNotice(group, id, duration));
                } catch (OXException e) {
                    LOG.error("Unable to inform GroupDispatcher {} about inactivity of client {} with Duration of {}.", group, id, duration, e);
                }
            }
        }
    }

    @Override
    public void cleanupForId(ID id) {
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
    private MultiMap<ID, ID> getClientToGroupsMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(client_map);
    }

    /**
     * Get mappings of one group to many members
     * 
     * @return A @ MultiMap} of one group to many members
     */
    private MultiMap<ID, ID> getGroupToMembersMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(group_map);
    }

    @Override
    public ManagementObject<DistributedGroupManagerMBean> getManagementObject() {
        return managementObject;
    }

}
