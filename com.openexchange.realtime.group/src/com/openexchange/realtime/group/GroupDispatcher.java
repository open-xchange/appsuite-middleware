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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.base.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.Component.EvictionPolicy;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.group.commands.LeaveCommand;
import com.openexchange.realtime.group.osgi.GroupServiceRegistry;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.ActionHandler;
import com.openexchange.realtime.util.Duration;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.server.ServiceExceptionCode;

/**
 * A {@link GroupDispatcher} is a utility superclass for implmenting chat room like functionality. Clients can join and leave the chat room,
 * when the last user has left, the room closes itself and calls {@link #onDispose()} for cleanup Subclasses can send messages to
 * participants in the room via the handy {@link #relayToAll(Stanza, ID...)} method. Subclasses may pass in an ActionHandler to make use of
 * the introspection magic.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */

public class GroupDispatcher implements ComponentHandle {

    /** The logger constant. */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GroupDispatcher.class);

    public static final AtomicReference<DistributedGroupManager> GROUPMANAGER_REF = new AtomicReference<DistributedGroupManager>();

    /** The collection of IDs that might be concurrently accessed */
    private final AtomicReference<Set<ID>> idsRef = new AtomicReference<Set<ID>>(Collections.<ID> emptySet());

    private final Map<ID, String> stamps = new ConcurrentHashMap<ID, String>();

    /** ID of the group */
    private final ID groupId;

    /** Sequence numer */
    private final AtomicLong sequenceNumber = new AtomicLong();

    /** Action handler */
    private final ActionHandler handler;

    private boolean isDisposed = false;

    /**
     * Initializes a new {@link GroupDispatcher}.
     *
     * @param id the ID of this group.
     */
    public GroupDispatcher(ID id) {
        this(id, null);
        LOG.info("Creating GroupDispatcher {} - {}", groupId, super.hashCode());
    }

    /**
     * Initializes a new {@link GroupDispatcher}.
     *
     * @param id The id of the group
     * @param handler An action handler for introspection
     */
    public GroupDispatcher(ID id, ActionHandler handler) {
        super();
        this.groupId = id;
        this.handler = handler;
    }

    /**
     * Implements the {@link ComponentHandle} standard method. If it receives a group command (like join or leave) it is handled internally
     * otherwise processing is delegated to {@link #processStanza(Stanza)}
     *
     * @param stanza
     */
    @Override
    public void process(Stanza stanza) {
        try {
            if (isDisposed) {
                LOG.debug("Discarding Stanza as GroupDispatcher {} is already disposed: {}", groupId, stanza);
                throw RealtimeExceptionCodes.GROUP_DISPOSED.create(groupId);
            }
            stanza.trace("Arrived in group dispatcher: " + groupId);
            if (!handleGroupCommand(stanza)) {
                processStanza(stanza);
            }
        } catch(RealtimeException re){
            handleException(stanza, re);
        } catch(Throwable t) {
            handleException(stanza, RealtimeExceptionCodes.STANZA_PROCESSING_FAILED.create(t, groupId, stanza.getFrom(), stanza.getTo()));
        }
    }

    /**
     * Handle the exception by logging it and informing the sender about it.
     * 
     * @param stanza The Stanza that caused and Exception
     * @param e The caused exception
     */
    private void handleException(Stanza stanza, RealtimeException exception) {
        LOG.error("", exception);
        ID sender = stanza.getFrom();
        stanza.setFrom(groupId);
        stanza.setError(exception);
        try {
            relayToID(stanza, sender);
        } catch (OXException e1) {
            LOG.error("Failed to inform sender about exception: {} because of {}", exception, e1);
        }
    }

    /**
     * Can be overidden by subclasses to implement a custom handling of non group commands. Defaults to using the ActionHandler to call
     * methods or calls {@link #defaultAction(Stanza)} if no suitable method
     *
     * @param stanza
     * @throws OXException
     */
    protected void processStanza(Stanza stanza) throws OXException {
        ID sender = stanza.getFrom();
        if (isWhitelisted(sender) || isMember(sender)) {
            if (handler == null || !handler.callMethod(this, stanza)) {
                defaultAction(stanza);
            }
        } else {
            LOG.error("Refusing to send to GroupDispatcher as sender {} is no member of the GroupDispatcher {}", stanza.getFrom(), groupId);
            send(new NotMember(groupId, sender, stanza.getSelector()));
        }
    }

    /**
     * Check if the sender is whitelisted iow. always allowed to send {@link Stanza}s to {@ GroupDispatcher}s although he isn't a member.
     * @param sender The sender of the {@link Stanza}
     * @return true if the user is always allowed to send e.g. is a synthetic component from an internal context
     */
    protected boolean isWhitelisted(ID sender) {
        if(
            sender != null
            && sender.isInternal()
          ) {
            return true;
        }
        return false;
    }

    private boolean handleGroupCommand(Stanza stanza) throws OXException {
        Optional<GroupCommand> groupCommand = stanza.getSinglePayload(new ElementPath("group", "command"), GroupCommand.class);

        if (!groupCommand.isPresent()) {
            return false;
        }

        groupCommand.get().perform(stanza, this);
        return true;
    }

    /**
     * Send a copy of a {@link Stanza} {@link Stanza} to all members of this group excluding a given set of users.
     *
     * @param stanza The stanza to send
     * @param inResponseTo The stanza we are responding to
     * @param excluded The {@link Set} of users to be excluded when sending
     * @throws OXException
     */
    public void relayToAll(Stanza stanza, ID... excluded) throws OXException {
        relayToAll(stanza, null, excluded);
    }

    /**
     * Send a copy of a {@link Stanza} as response to another {@link Stanza} to all members of this group excluding a given set of users. "As
     * response" means we add a special tracer and the log messages of the {@link Stanza} we are responding to.
     *
     * @param stanza The stanza to send
     * @param inResponseTo The stanza we are responding to
     * @param excluded The {@link Set} of users to be excluded when sending
     * @throws OXException
     */
    public void relayToAll(Stanza stanza, Stanza inResponseTo, ID... excluded) throws OXException {
        MessageDispatcher dispatcher = GroupServiceRegistry.getInstance().getService(MessageDispatcher.class);
        Set<ID> ex = new HashSet<ID>(Arrays.asList(excluded));
        // Iterate over snapshot
        for (ID id : idsRef.get()) {
            if (!ex.contains(id)) {
                // Send a copy of the stanza
                Stanza copy = copyFor(stanza, id);
                stamp(copy);
                if (inResponseTo != null) {
                    if (inResponseTo.getTracer() != null) {
                        copy.setTracer(inResponseTo.getTracer() + " response for " + id);
                        copy.addLogMessages(inResponseTo.getLogEntries());
                        copy.trace("---- Response ---");
                    }

                }
                dispatcher.send(copy);
            }
        }
    }

    /**
     * Send a copy of a {@link Stanza} to all members of this group excluding the client that sent the this {@link Stanza}.
     *
     * @param stanza
     * @throws OXException
     */
    public void relayToAllExceptSender(Stanza stanza) throws OXException {
        relayToAll(stanza, stanza.getFrom());
    }

    /**
     * Send a copy of a {@link Stanza} as response to another {@link Stanza} to all members of this group excluding the client that sent
     * this {@link Stanza}.
     *
     * @param stanza The stanza to send
     * @param inResponseTo The stanza we are responding to
     * @throws OXException
     */
    public void relayToAllExceptSender(Stanza stanza, Stanza inResponseTo) throws OXException {
        relayToAll(stanza, inResponseTo, stanza.getFrom());
    }

    /**
     * Send a {@link Stanza} to just one specific recipient
     *
     * @param stanza The {@link Stanza} to send
     * @param id The {@link ID} of the recipient
     * @throws OXException
     */
    public void relayToID(Stanza stanza, ID id) throws OXException {
        MessageDispatcher dispatcher = GroupServiceRegistry.getInstance().getService(MessageDispatcher.class);

        // Send a copy of the stanza
        Stanza copy = copyFor(stanza, id);
        stamp(copy);
        dispatcher.send(copy);
    }

    /**
     * Deliver this stanza to its recipient. Delegates to the {@link MessageDispatcher}
     */
    public void send(Stanza stanza) throws OXException {
        stamp(stanza);
        MessageDispatcher dispatcher = GroupServiceRegistry.getInstance().getService(MessageDispatcher.class);

        dispatcher.send(stanza);
    }

    /**
     * Add a member to this group. Can be invoked by sending the following message to this groups address.
     *
     * <pre>
     * {   element:     "message"
     *   , selector:    "mygroupSelector"
     *   , to:          "synthetic.componentName://roomID"
     *   , session:     "da86ae8fc93340d389c51a1d92d6e997"
     *   , payloads:    [ { namespace: 'group', element: 'command', data: 'join' } ]
     * }
     * </pre>
     *
     * A selector provided in this stanza will be added to all stanzas sent by this group, so clients can know the message was part of a
     * given group. Trying to join an already disposed GroupDispatcher will result in a RealtimeExceptionCodes.STANZA_RECIPIENT_UNAVAILABLE
     * Exception being thrown so the client can try to join again.
     *
     * @param id The id of the client joining the the Group
     * @param stamp The selector used in the Stanza to join the group
     * @throws OXException
     */
    public void join(ID id, String stamp, Stanza stanza) throws OXException {
        if(isDisposed) {
            throw RealtimeExceptionCodes.GROUP_DISPOSED.create(groupId);
        }
        if (idsRef.get().contains(id)) {
            throw RealtimeExceptionCodes.ALREADY_MEMBER.create(groupId);
        }

        beforeJoin(id, stanza);

        if (!mayJoin(id, stanza)) {
            LOG.info("{} is already a member of {}.", id, groupId);
            return;
        }

        // Perform a compare-and-set to atomically add
        boolean added = false;
        boolean first = false;
        Set<ID> expected;
        Set<ID> ids;
        do {
            expected = idsRef.get();
            ids = new LinkedHashSet<ID>(expected);

            first = ids.isEmpty();

            added = ids.add(id);
        } while (!idsRef.compareAndSet(expected, ids));

        LOG.debug("{} is joining {},", id, groupId);

        stamps.put(id, stamp);
        if (first) {
            firstJoined(id, stanza);
        }
        if (added) {
            DistributedGroupManager groupManager = GROUPMANAGER_REF.get();
            if(groupManager == null) {
                LOG.error("GroupManager reference unset.");
            } else {
                groupManager.addChoice(new SelectorChoice(id , groupId, stamp));
            }
            onJoin(id, stanza);
        }
    }

    /**
     * Leave the group by sending this stanza: { element: "message", to: "synthetic.componentName://roomID", session:
     * "da86ae8fc93340d389c51a1d92d6e997" payloads: [ { namespace: 'group', element: 'command', data: 'leave' } ], }
     */
    public void leave(ID id, Stanza stanza) throws OXException {
        //check if the sender is a member at all
        Set<ID> members = idsRef.get();
        if(!members.contains(id)) {
            if(members.isEmpty()) {
                dispose();
            }
            throw RealtimeExceptionCodes.NOT_A_MEMBER.create(id);
        }
        beforeLeave(id, stanza);

        LOG.debug("{} is leaving {}", id, groupId);

        // Perform a compare-and-set to atomically remove
        boolean removed = false;
        boolean empty = false;
        Set<ID> expected;
        Set<ID> ids;
        do {
            expected = idsRef.get();
            ids = new LinkedHashSet<ID>(expected);

            removed = ids.remove(id);
            empty = ids.isEmpty();
        } while (!idsRef.compareAndSet(expected, ids));


        if (removed) {
            DistributedGroupManager groupManager = GROUPMANAGER_REF.get();
            if (groupManager == null) {
                LOG.error("GroupManager reference unset.");
            } else {
                groupManager.removeChoice(new SelectorChoice(id, groupId, getStamp(id)));
            }
            onLeave(id, stanza);
        }

        stamps.remove(id);

        if (empty) {
            onDispose(id, stanza);
            isDisposed = true;
            doGlobalCleanup(groupId);
        }
    }

    /**
     * Gets the selector with which this id joined
     */
    public String getStamp(ID id) {
        return stamps.get(id);
    }

    /**
     * Stamp a stanza with the selector for this recipient. Furthermore this sets this GroupDispatcher as SequencePrincipal for this Stanza
     * and uses the GroupDispatchers current sequence number for sending this Stanza.
     */
    public void stamp(Stanza s) {
        if (s.getSelector() == null || s.getSelector().equals(Stanza.DEFAULT_SELECTOR)) {
            s.setSelector(getStamp(s.getTo()));
        }
        s.setSequencePrincipal(groupId);
        s.setSequenceNumber(sequenceNumber.getAndIncrement());
    }

    /**
     * Get a (snapshot) list of all members of this group
     */
    public List<ID> getIds() {
        return new ArrayList<ID>(idsRef.get());
    }

    /**
     * Get the id of this group
     */
    @Override
    public ID getId() {
        return groupId;
    }

    /**
     * Determine whether an ID is a member of this group. Useful if you want to only accept messages for IDs that are members.
     */
    public boolean isMember(ID id) {
        return idsRef.get().contains(id);
    }

    /**
     * Makes a copy of this stanza for a recipient. May be overridden.
     */
    protected Stanza copyFor(Stanza stanza, ID to) throws OXException {
        Stanza copy = stanza.newInstance();
        copy.setTo(to);
        copy.setFrom(stanza.getFrom());
        copy.setTracer(stanza.getTracer());
        copy.setSelector(stanza.getSelector());
        copyPayload(stanza, copy);

        return copy;
    }

    /**
     * Makes a copy of the payload in the stanza and puts it into the copy
     */
    protected void copyPayload(Stanza stanza, Stanza copy) throws OXException {
        List<PayloadTree> copyList = new ArrayList<PayloadTree>(stanza.getPayloadTrees().size());
        for (PayloadTree tree : stanza.getPayloadTrees()) {
            copyList.add(tree.internalClone());
        }
        copy.setPayloads(copyList);
    }

    /**
     * Subclasses can override this method to determine whether a potential participant is allowed to join this group.
     *
     * @param id The id to check the permission for.
     * @return true, if the participant may join this group, false otherwise
     * @see ID#toSession()
     */

    protected boolean mayJoin(ID id, Stanza stanza) {
        return mayJoin(id);
    }

    protected boolean mayJoin(ID id) {
        return true;
    }

    /**
     * Callback that is called before an ID joins the group. Override this to be notified of a member about to join the group.
     */
    protected void beforeJoin(ID id, Stanza stanza) {
        beforeJoin(id);
    }

    protected void beforeJoin(ID id) {
        // Empty method
    }



    /**
     * Callback that is called after a new member has joined the group.
     */
    protected void onJoin(ID id, Stanza stanza) {
        onJoin(id);
    }

    protected void onJoin(ID id) {
        // Empty method
    }

    /**
     * Callback for when the first user joined
     */
    protected void firstJoined(ID id, Stanza stanza) {
        firstJoined(id);
    }

    protected void firstJoined(ID id) {

    }

    /**
     * Callback that is called before a member leaves the group
     */
    protected void beforeLeave(ID id, Stanza stanza) {
        beforeLeave(id);
    }

    protected void beforeLeave(ID id) {
        // Empty method
    }

    /**
     * Callback that is called after a member left the group
     */
    protected void onLeave(ID id, Stanza stanza) {
        onLeave(id);
    }

    protected void onLeave(ID id) {
        // Empty method
    }

    /**
     * Called when the group is closed. This happens when the last member left the group, or the {@link EvictionPolicy} of the
     * {@link Component} that created this group decides it is time to close the group
     *
     * @param id
     * @throws OXException
     */
    protected void onDispose(ID id, Stanza stanza) throws OXException {
        try {
            onDispose(id);
        } catch (Exception e) {
            LOG.info("Caught exception during onDispose, trying to continue.", e);
        }
    }

    protected void onDispose(ID id) throws OXException {
        // Empty method
    }

    /**
     * Called for a stanza if no other handler is found.
     */
    protected void defaultAction(Stanza stanza) {
        LOG.warn("Couldn't find matching handler for {}. \nUse default", stanza);
    }

    /**
     * Handle notifications about inactivity durations of members.
     * Override this method to handle notifications in your GroupDispatcher specialization.
     * <code>
     * <pre>
     * Optional<ID> inactiveClient = stanza.getSinglePayload(new ElementPath("com.openexchange.realtime", "client"), ID.class);
     * Optional<Duration> inactivityDuration = stanza.getSinglePayload(new ElementPath("com.openexchange.realtime.client", "inactivity"), Duration.class);
     *
     * if (inactiveClient.isPresent() && inactivityDuration.isPresent()) {
     *      LOG.info("User {} was inactive for {} ", inactiveClient.get(), inactivityDuration.get());
     * }
     * </pre>
     * </code>
     *
     * @param stanza The Stanza containing the inactive client identified by {@link ElementPath} 'com.openexchange.realtime.client' and the {@link Duration} of inactivity identified by
     * 'com.openexchange.realtime.client.inactivity'.
     * @throws OXException
     */
    public void handleInactivityNotice(Stanza stanza) throws OXException {}

    @Override
    public boolean shouldBeDoneInGlobalThread(Stanza stanza) {
        PayloadElement payload = stanza.getPayload();

        Object data = payload.getData();
        if (LeaveCommand.class.isInstance(data)) {
            return true;
        }

        return false;
    }

    public Stanza getWelcomeMessage(ID onBehalfOf) {
        Stanza welcome = new Message();
        welcome.setTo(onBehalfOf);
        welcome.setFrom(getId());
        welcome.setSelector(getStamp(onBehalfOf));
        welcome.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
                        new PayloadElement("Welcome", "json", "group", "message")).build()));
        return welcome;
    }

    public Stanza getSignOffMessage(ID onBehalfOf) {
        Stanza goodbye = new Message();
        goodbye.setTo(onBehalfOf);
        goodbye.setFrom(getId());
        goodbye.setSelector(getStamp(onBehalfOf));
        goodbye.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
                        new PayloadElement("Goodbye", "json", "group", "message")).build()));
        return goodbye;
    }

    @Override
    public void dispose() {
          if (!isDisposed) {
              isDisposed = true;
              // Find any valid member identifier
              ID memberId = null;
              final Set<ID> ids = idsRef.get();
              memberId = ids.isEmpty() ? null : ids.iterator().next();
              if (memberId == null) {
                  LOG.info("No member left in GroupDispatcher {}, skipping onDispose", groupId);
              } else {
                  try {
                      onDispose(memberId);
                  } catch (Exception e) {
                      LOG.info("Caught exception during onDispose, trying to continue.", e);
                  }
              }
              idsRef.set(null);
              stamps.clear();
                /*
                 * Global Cleanup does several things for us:
                 * 1: Trigger cleanup of SyntheticChannel to create a new Instance when clients try to rejoin, see
                 *    SyntheticChannel#cleanupForId(ID id)
                 * 2: Send all current members a NotMember so they are forced to rejoin/reload the resource represented by this
                 *    GroupDispatcher
                 * 3: Clean groupId from ResourceDirectory so it has to be conjured again when a client tries to reload it
                 */
              doGlobalCleanup(groupId);
          }
    }

    /**
     * Do a global cleanup for a given ID
     *
     * @param id the ID
     */
    private void doGlobalCleanup(ID id) {
        GlobalRealtimeCleanup globalRealtimeCleanup = GroupServiceRegistry.getInstance().getService(GlobalRealtimeCleanup.class);
        if (globalRealtimeCleanup == null) {
            LOG.error(
                "Unable to initiate global cleanup for {} cleanup",
                id,
                ServiceExceptionCode.serviceUnavailable(GlobalRealtimeCleanup.class));
        } else {
            globalRealtimeCleanup.cleanForId(groupId);
        }
    }

}
