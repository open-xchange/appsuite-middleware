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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.Component.EvictionPolicy;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.group.commands.LeaveCommand;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.util.ActionHandler;
import com.openexchange.server.ServiceLookup;

/**
 * A {@link GroupDispatcher} is a utility superclass for implmenting chat room like functionality. Clients can join and leave the chat room,
 * when the last user has left, the room closes itself and calls {@link #onDispose()} for cleanup Subclasses can send messages to
 * participants in the room via the handy {@link #relayToAll(Stanza, ID...)} method. Subclasses may pass in an ActionHandler to make use of
 * the introspection magic.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GroupDispatcher implements ComponentHandle {

    private static final org.apache.commons.logging.Log LOG = Log.loggerFor(GroupDispatcher.class);

    /**
     * The <code>ServiceLookup</code> reference.
     */
    public static final AtomicReference<ServiceLookup> SERVICE_REF = new AtomicReference<ServiceLookup>();

    /** The collection of IDs that might be concurrently accessed */
    private final Queue<ID> ids = new LinkedBlockingQueue<ID>();

    private final Map<ID, String> stamps = new HashMap<ID, String>();

    /** ID of the group */
    private final ID id;

    private long sequenceNumber = 0;

    private ActionHandler handler = null;

    /**
     * Initializes a new {@link GroupDispatcher}.
     *
     * @param id the ID of this group.
     */
    public GroupDispatcher(ID id) {
        this(id, null);
    }

    /**
     * Initializes a new {@link GroupDispatcher}.
     *
     * @param id The id of the group
     * @param handler An action handler for introspection
     */
    public GroupDispatcher(ID id, ActionHandler handler) {
        this.id = id;
        this.handler = handler;
        final Queue<ID> ids = this.ids;
        id.on("dispose", new IDEventHandler() {

            @Override
            public void handle(String event, ID id, Object source, Map<String, Object> properties) {
                try {
                    // Find any valid member identifier
                    ID memberId = null;
                    if (properties != null) {
                        memberId = (ID) properties.get("id");
                    }
                    if (null == memberId) {
                        memberId = ids.peek();
                    }
                    if (memberId == null) {
                        memberId = id;
                    }
                    onDispose(memberId != null ? memberId : id);
                } catch (OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
    }

    /**
     * Implements the {@link ComponentHandle} standard method. If it receives a group command (like join or leave) it is handled internally
     * otherwise processing is delegated to {@link #processStanza(Stanza)}
     *
     * @param stanza
     * @throws OXException
     */
    @Override
    public void process(Stanza stanza) throws OXException {
        stanza.trace("Arrived in group dispatcher: " + id);
        if (!handleGroupCommand(stanza)) {
            processStanza(stanza);
        }
        if (stanza.traceEnabled()) {
            // Send analysis back
            stanza.trace("------------- SENDING RECEIPT ----------------");
            Stanza stanza2 = stanza.newInstance();
            stanza2.setFrom(id);
            stanza2.setTo(stanza.getFrom());
            stanza2.setTracer(stanza.getTracer());
            stanza2.addLogMessages(stanza.getLogEntries());
            send(stanza2);
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
        if (handler == null || !handler.callMethod(this, stanza)) {
            defaultAction(stanza);
        }
    }

    private boolean handleGroupCommand(Stanza stanza) throws OXException {
        PayloadElement payload = stanza.getPayload();
        if (payload == null) {
            return true;
        }

        if (payload.getElementName().equals("ping") && payload.getNamespace().equals("group")) {
            return true; // Discard, this was just to reset the timeout
        }

        Object data = payload.getData();
        if (GroupCommand.class.isInstance(data)) {
            ((GroupCommand) data).perform(stanza, this);
            return true;
        }

        return false;
    }

    public void relayToAll(Stanza stanza, ID... excluded) throws OXException {
        relayToAll(stanza, null, excluded);
    }

    /**
     * Send a copy of the stanza to all members of this group, excluding the ones provided as the rest of the arguments.
     */
    public void relayToAll(Stanza stanza, Stanza inResponseTo, ID... excluded) throws OXException {
        MessageDispatcher dispatcher = SERVICE_REF.get().getService(MessageDispatcher.class);
        Set<ID> ex = new HashSet<ID>(Arrays.asList(excluded));
        // Iterate over snapshot
        for (ID id : new ArrayList<ID>(ids)) {
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
     * Relay this message to all except the original sender ("from") of the stanza.
     */
    public void relayToAllExceptSender(Stanza stanza) throws OXException {
        relayToAll(stanza, stanza.getFrom());
    }

    public void relayToAllExceptSender(Stanza stanza, Stanza inResponseTo) throws OXException {
        relayToAll(stanza, inResponseTo, stanza.getFrom());
    }

    /**
     * Deliver this stanza to its recipient. Delegates to the {@link MessageDispatcher}
     */
    public void send(Stanza stanza) throws OXException {
        stamp(stanza);
        MessageDispatcher dispatcher = SERVICE_REF.get().getService(MessageDispatcher.class);

        dispatcher.send(stanza);
    }

    /**
     * Add a member to this group. Can be invoked by sending the following message to this groups address. { element: "message", selector:
     * "mygroupSelector", to: "synthetic.componentName://roomID", session: "da86ae8fc93340d389c51a1d92d6e997" payloads: [ { namespace:
     * 'group', element: 'command', data: 'join' } ], } A selector provided in this stanza will be added to all stanzas sent by this group,
     * so clients can know the message was part of a given group.
     *
     * @param id The id of the client joining the the Group
     * @param stamp The selector used in the Stanza to join the group
     */
    public void join(ID id, String stamp) {
        if (ids.contains(id)) {
            return;
        }

        beforeJoin(id);

        if (!mayJoin(id)) {
            return;
        }
        boolean first = ids.isEmpty();

        ids.offer(id);

        if (LOG.isDebugEnabled()) {
            LOG.debug("joining:" + id.toString());
        }

        stamps.put(id, stamp);
        id.on("dispose", LEAVE);
        if (first) {
            firstJoined(id);
        }
        onJoin(id);
    }

    /**
     * Leave the group by sending this stanza: { element: "message", to: "synthetic.componentName://roomID", session:
     * "da86ae8fc93340d389c51a1d92d6e997" payloads: [ { namespace: 'group', element: 'command', data: 'leave' } ], }
     */
    public void leave(ID id) throws OXException {
        beforeLeave(id);

        if (LOG.isDebugEnabled()) {
            LOG.debug("leaving:" + id.toString());
        }

        id.off("dispose", LEAVE);
        ids.remove(id);
        stamps.remove(id);
        onLeave(id);
        if (ids.isEmpty()) {
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("id", id);
            this.id.trigger("dispose", this, properties);
        }
    }

    /**
     * Gets the selector with which this id joined
     */
    public String getStamp(ID id) {
        return stamps.get(id);
    }

    /**
     * Stamp a stanza with the selector for this recipient
     */
    public void stamp(Stanza s) {
        s.setSelector(getStamp(s.getTo()));
        s.setSequencePrincipal(id);
        s.setSequenceNumber(sequenceNumber);
        sequenceNumber++;
    }

    /**
     * Get a (snapshot) list of all members of this group
     */
    public List<ID> getIds() {
        return new ArrayList<ID>(ids);
    }

    /**
     * Get the id of this group
     */
    public ID getId() {
        return id;
    }

    /**
     * Determine whether an ID is a member of this group. Useful if you want to only accept messages for IDs that are members.
     */
    protected boolean isMember(ID id) {
        return ids.contains(id);
    }

    /**
     * Makes a copy of this stanza for a recipient. May be overridden.
     */
    protected Stanza copyFor(Stanza stanza, ID to) throws OXException {
        Stanza copy = stanza.newInstance();
        copy.setTo(to);
        copy.setFrom(stanza.getFrom());
        copyPayload(stanza, copy);

        return copy;
    }

    /**
     * Makes a copy of the payload in the stanza and puts it into the copy
     */
    protected void copyPayload(Stanza stanza, Stanza copy) throws OXException {
        List<PayloadTree> copyList = new ArrayList<PayloadTree>(stanza.getPayloads().size());
        for (PayloadTree tree : stanza.getPayloads()) {
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
    protected boolean mayJoin(ID id) {
        return true;
    }

    /**
     * Callback that is called before an ID joins the group. Override this to be notified of a member about to join the group.
     */
    protected void beforeJoin(ID id) {
        // Empty method
    }

    /**
     * Callback that is called after a new member has joined the group.
     */
    protected void onJoin(ID id) {
        // Empty method
    }

    /**
     * Callback for when the first used joined
     */
    protected void firstJoined(ID id) {

    }

    /**
     * Callback that is called before a member leaves the group
     */
    protected void beforeLeave(ID id) {
        // Empty method
    }

    /**
     * Callback that is called after a member left the group
     */
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
    protected void onDispose(ID id) throws OXException {
        // Empty method
    }

    /**
     * Called for a stanza if no other handler is found.
     */
    protected void defaultAction(Stanza stanza) {
        if (LOG.isErrorEnabled()) {
            LOG.error("Couldn't find matching handler for " + stanza.toString() + ". \nUse default");
        }
    }

    private final IDEventHandler LEAVE = new IDEventHandler() {

        @Override
        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
            try {
                leave(id);
            } catch (OXException e) {
                // Ignore
            }
        }
    };

    @Override
    public boolean shouldBeDoneInGlobalThread(Stanza stanza) {
        PayloadElement payload = stanza.getPayload();

        Object data = payload.getData();
        if (LeaveCommand.class.isInstance(data)) {
            return true;
        }

        return false;
    }

}
