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

package com.openexchange.realtime.hazelcast.group;

import java.util.Collection;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementAware;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.cleanup.AbstractRealtimeJanitor;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.group.DistributedGroupManager;
import com.openexchange.realtime.group.InactivityNotice;
import com.openexchange.realtime.group.NotMember;
import com.openexchange.realtime.group.SelectorChoice;
import com.openexchange.realtime.group.commands.LeaveCommand;
import com.openexchange.realtime.group.commands.LeaveStanza;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.management.DistributedGroupManagerMBean;
import com.openexchange.realtime.hazelcast.management.DistributedGroupManagerManagement;
import com.openexchange.realtime.hazelcast.osgi.Services;
import com.openexchange.realtime.hazelcast.serialization.group.PortableSelectorChoice;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
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
public class DistributedGroupManagerImpl extends AbstractRealtimeJanitor implements ManagementAware<DistributedGroupManagerMBean>, DistributedGroupManager {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedGroupManagerImpl.class);
    private final MessageDispatcher messageDispatcher;
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
    public DistributedGroupManagerImpl(MessageDispatcher messageDispatcher, String client_map, String group_map) {
        this.messageDispatcher = messageDispatcher;
        this.client_map=client_map;
        this.group_map=group_map;
        this.inactivityMap = new IDMap<Duration>(true);
        this.managementObject = new DistributedGroupManagerManagement(client_map, group_map);
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

    @Override
    public ManagementObject<DistributedGroupManagerMBean> getManagementObject() {
        return managementObject;
    }

    @Override
    public boolean addChoice(SelectorChoice selectorChoice) throws OXException {
        Validate.notNull(selectorChoice, "Stamped group must not be null");

        PortableID portableCID = new PortableID(selectorChoice.getClient());
        PortableID portableGID = new PortableID(selectorChoice.getGroup());
        PortableSelectorChoice portableSelectorChoice = new PortableSelectorChoice(selectorChoice);

        boolean putClientToGroup = getClientToGroupsMapping().put(portableCID, portableSelectorChoice);
        boolean putGroupToMember = getGroupToMembersMapping().put(portableGID, portableSelectorChoice);

        return putGroupToMember && putClientToGroup;
    }

    @Override
    public boolean removeChoice(SelectorChoice selectorChoice) throws OXException {
        Validate.notNull(selectorChoice, "SelectorChoice must not be null");

        boolean wasClientToGroupMappingRemoved = removeClientToSelectorChoice(selectorChoice);
        boolean wasGroupToClientMappingRemoved = removeGroupToSelectorChoice(selectorChoice);

        return wasClientToGroupMappingRemoved && wasGroupToClientMappingRemoved;
    }

    @Override
    public boolean removeClientToSelectorChoice(SelectorChoice selectorChoice) throws OXException {
        MultiMap<PortableID,PortableSelectorChoice> clientToGroupsMapping = getClientToGroupsMapping();

        boolean clientAssociationRemoved = clientToGroupsMapping.remove(
            new PortableID(selectorChoice.getClient()),
            new PortableSelectorChoice(selectorChoice));

        return clientAssociationRemoved;
    }

    @Override
    public boolean removeGroupToSelectorChoice(SelectorChoice selectorChoice)  throws OXException {
        MultiMap<PortableID, PortableSelectorChoice> groupToMembersMapping = getGroupToMembersMapping();

        boolean groupAssociationRemoved = groupToMembersMapping.remove(new PortableID(selectorChoice.getGroup()), new PortableSelectorChoice(
            selectorChoice));

        return groupAssociationRemoved;
    }

    @Override
    public Collection<? extends SelectorChoice> removeClient(ID client) throws OXException {
        Collection<PortableSelectorChoice> removedChoices = removeClient(client, true);
        return removedChoices;
    }

    @Override
    public Collection<? extends SelectorChoice> removeGroup(ID group) throws OXException {
        //first remove all mappings
        Collection<PortableSelectorChoice> removedChoices = removeGroup(group, false);
        for (PortableSelectorChoice portableSelectorChoice : removedChoices) {
            removeClientToSelectorChoice(portableSelectorChoice);
        }
        //then inform all members so they can rejoin
        sendNotMember(removedChoices);
        return removedChoices;
    }

    @Override
    public Collection<? extends SelectorChoice> getGroups(ID client) throws OXException {
        Validate.notNull(client, "Client ID must not be null");
        Collection<PortableSelectorChoice> choices = getClientToGroupsMapping().get(new PortableID(client));
        return choices;
    }

    @Override
    public Collection<? extends SelectorChoice> getMembers(ID group) throws OXException {
        Validate.notNull(group, "Group ID must not be null");
        Collection<PortableSelectorChoice> selectorChoices = getGroupToMembersMapping().get(new PortableID(group));
        return selectorChoices;
    }

    @Override
    public void setInactivity(ID client, Duration duration) throws OXException {
        Validate.notNull(client, "Client ID must not be null");
        Validate.notNull(duration, "Duration must not be null");
        Duration old = inactivityMap.put(client, duration);
        if (!duration.equals(old)) {
            ThreadPoolService threadPoolService = Services.getService(ThreadPoolService.class);
            for (SelectorChoice selectorChoice : getGroups(client)) {
                try {
                    ID group = selectorChoice.getGroup();
                    LOG.debug("Informing GroupDispatcher {} about inactivity of client {} with Duration of {}.", group, client, duration);
                    InactivityNotice inactivityNotice = new InactivityNotice(group, client, duration);
                    threadPoolService.submit(new SendStanzaTask(messageDispatcher, inactivityNotice));
                } catch (Exception e) {
                    LOG.error(
                        "Unable to inform GroupDispatcher {} about inactivity of client {} with Duration of {}.",
                        selectorChoice.getGroup(),
                        client,
                        duration,
                        e);
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
        if (id != null) {
            LOG.debug("Cleanup for synthetic id {}", id);
            try {
                // For now groups are the only synthetic resource
                removeGroup(id);
            } catch (OXException oxe) {
                LOG.error("Error while cleaning for ID {}", id, oxe);
            }
        }
    }

    private void cleanupForClientId(ID id) {
        try {
            Collection<? extends SelectorChoice> removedChoices = removeClient(id);
            inactivityMap.remove(id);
            LOG.debug("Cleanup for ID: {}. Removed mappings: {}", id, removedChoices);
        } catch (Exception e) {
            LOG.error("Error while cleaning for ID {}", id, e);
        }
    }

    /**
     * Get mapping of one client to many groups
     *
     * @return A {@link MultiMap} of one client to many groups
     */
    private MultiMap<PortableID, PortableSelectorChoice> getClientToGroupsMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(client_map);
    }

    /**
     * Get mappings of one group to many members
     *
     * @return A @ MultiMap} of one group to many members
     */
    private MultiMap<PortableID, PortableSelectorChoice> getGroupToMembersMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(group_map);
    }

    /**
     * Remove all client -> group mappings for a given client ID. Additionally allows to send a LeaveCommand to all GroupDispatchers that
     * hold the client as member and thus remove the group -> client mapping.
     *
     * @param client The client to remove
     * @param sendLeave If a LeaveCommand should be sent to all GrpupDispatchers that the client was a member of
     * @return A Collection of groups IDs that the client was member of
     * @throws OXException
     */
    private Collection<PortableSelectorChoice> removeClient(ID client, boolean sendLeave) throws OXException {
        Validate.notNull(client, "Client ID must not be null");
        Collection<PortableSelectorChoice> removedChoices = getClientToGroupsMapping().remove(new PortableID(client));
        //This will automatically remove the group -> member association for this client
        if(sendLeave) {
            sendLeave(removedChoices);
        }
        return removedChoices;
    }

    /**
     * Remove all group -> client mappings for a given group ID. Additionally allows to send a {@link NotMember} to all members of the group
     * being removed.
     *
     * @param group The group to remove
     * @param sendNotMember If a {@link NotMember} should be sent to all members of the group being removed
     * @return A Collection selectorchoices representing the former members
     * @throws OXException
     */
    private Collection<PortableSelectorChoice> removeGroup(ID group, boolean sendNotMember) throws OXException {
        Validate.notNull(group, "Group ID must not be null");
        Collection<PortableSelectorChoice> removedChoices = getGroupToMembersMapping().remove(new PortableID(group));
        if(sendNotMember) {
            sendNotMember(removedChoices);
        }
        return removedChoices;
    }

    /**
     * Send a @{@link Stanza} containing a {@link LeaveCommand} from a client to a {@link Collection} of groups.
     *
     * @param client The client that leaves a group.
     * @param groups The groups that the client should leave.
     */
    private void sendLeave(Collection<PortableSelectorChoice> selectorChoices) {
        ThreadPoolService threadPoolService = Services.getService(ThreadPoolService.class);
        for (SelectorChoice selectorChoice : selectorChoices) {
            ID client = selectorChoice.getClient();
            ID group = selectorChoice.getGroup();
            try {
                LOG.debug("Sending leave on behalf of {} to {}.", client, group);
                LeaveStanza leaveStanza = new LeaveStanza(client, group);
                threadPoolService.submit(new SendStanzaTask(messageDispatcher, leaveStanza));
            } catch (Exception e) {
                LOG.error("Unable to remove client {} from group {}.", client, group, e);
            }
        }
    }

    /**
     * Send a @{@link Stanza} containing a {@link NotMember} from a group to a {@link Collection} of clients.
     *
     * @param selectorChoices The {@link SelectorChoice}s containing the details needed to send the {@link NotMember}.
     */
    private void sendNotMember(Collection<PortableSelectorChoice> selectorChoices) {
        ThreadPoolService threadPoolService = Services.getService(ThreadPoolService.class);
        for (PortableSelectorChoice selectorChoice : selectorChoices) {
            ID client = selectorChoice.getClient();
            ID group = selectorChoice.getGroup();
            String selector = selectorChoice.getSelector();
            try {
                LOG.debug("Sending NotMember to {} for group {}.", client, group);
                Stanza notMember = new NotMember(group, client, selector);
                threadPoolService.submit(new SendStanzaTask(messageDispatcher, notMember));
            } catch (Exception e) {
                LOG.error("Unable to remove client {} from group {}.", client, group, e);
            }
        }
    }

}
