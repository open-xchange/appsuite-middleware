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

package com.openexchange.realtime.group;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.cleanup.AbstractRealtimeJanitor;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.group.commands.LeaveCommand;
import com.openexchange.realtime.group.commands.LeaveStanza;
import com.openexchange.realtime.group.osgi.GroupServiceRegistry;
import com.openexchange.realtime.group.osgi.RealtimeJanitors;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.Duration;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link GroupManager} - Keeps a mapping from Clients to the groups they joined. Can be used to issue a proper leave {@link LeaveCommand}
 * on behalf of those clients once they timed out.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class GroupManager extends AbstractRealtimeJanitor implements GroupManagerService {


    private static final Logger LOG = LoggerFactory.getLogger(GroupManager.class);

    private Multimap<ID, ID> clientMap;
    private IDMap<Duration> inactivityMap;

    /**
     * Initializes a new {@link GroupManager}.
     */
    public GroupManager() {
        super();
        clientMap = Multimaps.synchronizedMultimap(HashMultimap.<ID, ID> create());
        inactivityMap = new IDMap<Duration>(true);
        RealtimeJanitors.getInstance().addJanitor(this);
    }

    /**
     * Adds a client <-> group mapping to this manager
     * 
     * @param client The {@link ID} of the client that joined a group.
     * @param group The {@link ID} of the group that client joined.
     * @return True if the mapping was added to the manager, false otherwise.
     */
    public boolean add(ID client, ID group) {
        return clientMap.put(client, group);
    }

    /**
     * Remove all client <-> group mappings for a given client {@link ID}. This will automatically issue a {@link LeaveCommand} for every
     * group that the client was member of.
     * 
     * @param client The client {@link ID}
     * @return A {@link Collection} of groups {@link ID}s that the client was member of
     */
    public Collection<ID> remove(ID client) {
        Collection<ID> groups = clientMap.removeAll(client);
        sendLeave(client, groups);
        return groups;
    }
    
    /**
     * Remove a single client <-> group mapping without sending a {@link LeaveCommand} on
     * behalf of the client to the group.
     * 
     * @param client The client {@link ID}.
     * @return true if the client <-> group mapping was removed from the manager, false otherwise.
     */
    public boolean remove(ID client, ID group) {
        return clientMap.remove(client, group);
    }

    /**
     * Send a @{@link Stanza} containing a {@link LeaveCommand} from a client to a {@link Collection} of groups.
     * 
     * @param client The client that leaves a group.
     * @param groups The groups that the client should leave.
     */
    private void sendLeave(ID client, Collection<ID> groups) {
        MessageDispatcher dispatcher = GroupServiceRegistry.getInstance().getService(MessageDispatcher.class);
        if (dispatcher == null) {
            LOG.error("Unable to send leave message.", ServiceExceptionCode.serviceUnavailable(MessageDispatcher.class));
            return;
        }
        for (ID group : groups) {
            try {
                LOG.debug("Sending leave on behalf of {} to {}.", client, group);
                dispatcher.send(new LeaveStanza(client, group));
            } catch (OXException e) {
                LOG.error("Unable to remove client {} from group {}.", client, group, e);
            }
        }
    }

    @Override
    public void cleanupForId(ID id) {
        Collection<ID> removed = remove(id);
        inactivityMap.remove(id);
        LOG.debug("Cleanup for ID: {}. Removed from groups: {}", id, removed);
    }

    
    public void setInactivity(ID id, Duration duration) {
        Validate.notNull(id, "ID must not be null");
        Validate.notNull(duration, "Duration must not be null");
        Duration old = inactivityMap.put(id, duration);
        if(!duration.equals(old)) {
            informGroupDispatchers(id, duration);
        }
    }
    
    /**
     * Get the Groups a client is member of.
     * @param id the client {@link ID}
     * @return the Groups a client is member of.
     */
    public Set<ID> getGroups(ID id) {
        return new HashSet<ID>(clientMap.get(id));
    }

    private void informGroupDispatchers(ID id, Duration duration) {
        MessageDispatcher dispatcher = GroupServiceRegistry.getInstance().getService(MessageDispatcher.class);
        if (dispatcher == null) {
            LOG.error("Unable to inform GroupDispatchers.", ServiceExceptionCode.serviceUnavailable(MessageDispatcher.class));
            return;
        }
        for(ID group : getGroups(id)) {
            try {
                LOG.debug("Informing GroupDispatcher {} about inactivity of client {} with Duration of {} seconds.", group, id, duration.getValueInS());
                dispatcher.send(new InactivityNotice(group, id, duration));
            } catch (OXException e) {
                LOG.error("Unable to inform GroupDispatcher {} about inactivity of client {} with Duration of {} seconds.", group, id, duration.getValueInS(), e);
            }
        }
    }

}
