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

package com.openexchange.realtime.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.realtime.packet.Stanza;

/**
 * A {@link StanzaSequenceGate} ensures that stanzas are handled in a well defined order. Sequence numbers are expected to always rise by
 * one (1, 2, 3, 4....). If a sequence number is skipped the stanza sequence gate will hold back handling the stanza until the missing
 * stanza arrives, and then handle all held back stanzas in turn. If a stanza contains no sequence number (-1) then it will be handled
 * immediately
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class StanzaSequenceGate {

    private static org.apache.commons.logging.Log LOG = Log.loggerFor(StanzaSequenceGate.class);

    /* Max. number of stanzas that will be buffered if one stanza is missing in the sequence */
    protected static final int BUFFER_SIZE = 20;

    /* Keep track of SequencePrincalpal(ID) to thresholds(sequence number of last seen stanza) */
    protected ConcurrentHashMap<ID, AtomicLong> sequenceNumbers = new ConcurrentHashMap<ID, AtomicLong>();

    protected ConcurrentHashMap<ID, List<StanzaWithCustomAction>> inboxes = new ConcurrentHashMap<ID, List<StanzaWithCustomAction>>();

    private final String name;

    public StanzaSequenceGate(String name) {
        this.name = name;
    }

    /**
     * Ensures a correct order of sequence numbers of incoming Stanzas. <li>Stanzas that don't carry a sequence number are directly handed
     * over to handleInternal of the components that makes use of this StanzaSequenceGate and have to adapt the logic of the Stanza handling
     * of the received Stanzas.</li> <li>Stanzas that carry a sequence number are enqueued until a correct sequence is established. Stanza
     * that already passed this gate are discarded. If the buffer for creating a valid sequence is full Stanzas are discarded.</li> </p>
     * 
     * @param stanza the incoming stanza
     * @param recipient the recipient of the incoming Stanza
     * @return If <code>false</code> is returned the handled stanza did either not expect an ACK or was not stored because of a full message
     *         buffer for the sequence principal.
     * @throws RealtimeException with code 1006 if an invalid sequence number was detected. The component using this gate has to ensure this
     *             exception reaches the client.
     */
    public boolean handle(Stanza stanza, ID recipient) throws RealtimeException {
        return handle(stanza, recipient, null);
    }

    /**
     * Ensures a correct order of sequence numbers of incoming Stanzas. <li>Stanzas that don't carry a sequence number are directly handed
     * over to handleInternal of the components that makes use of this StanzaSequenceGate and have to adapt the logic of the Stanza handling
     * of the received Stanzas.</li> <li>Stanzas that carry a sequence number are enqueued until a correct sequence is established. Stanza
     * that already passed this gate are discarded. If the buffer for creating a valid sequence is full Stanzas are discarded.</li> </p>
     * 
     * @param stanza the incoming stanza
     * @param recipient the recipient of the incoming Stanza
     * @param customAction a custom action that (if not null) should be used instead of the using component's handleInternal.
     * @return If <code>false</code> is returned the handled stanza did either not expect an ACK or was not stored because of a full message
     *         buffer for the sequence principal.
     * @throws RealtimeException with code 1006 if an invalid sequence number was detected. The component using this gate has to ensure this
     *             exception reaches the client.
     */
    public boolean handle(Stanza stanza, ID recipient, CustomGateAction customAction) throws RealtimeException {
        /* Stanza didn't carry a valid Sequencenumber, just handle it without pestering the gate and return */
        if (stanza.getSequenceNumber() == -1) {
            stanza.trace("Stanza Gate: (" + name + ") : No sequence number, so let it pass");
            if (customAction != null) {
                customAction.handle(stanza, recipient);
            } else {
                //TODO: for 7.4 this shouldn't throw Exceptions at all. The class implementing the method has to handle it
                try {
                    handleInternal(stanza, recipient);
                } catch (Exception ex) {
                    throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(ex, ex.getMessage());
                }
            }
            return false;
        }

        try {
            stanza.getSequencePrincipal().lock("gate");
            AtomicLong threshold = sequenceNumbers.get(stanza.getSequencePrincipal());
            /* We haven't recorded a threshold (upper bound of last known sequence number) for this principal, yet so we'll add one */
            if (threshold == null) {
                threshold = new AtomicLong(0);
                AtomicLong meantime = sequenceNumbers.putIfAbsent(stanza.getSequencePrincipal(), threshold);
                /*
                 * Add eventhandler to clean up the traces we left in the gate when the the principal receives the dispose event, e.g when
                 * all members left the GroupDispatcher(SequencePrincipal)
                 */
                if (meantime == null) {
                    stanza.getSequencePrincipal().on(ID.Events.DISPOSE, new IDEventHandler() {

                        @Override
                        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
                            freeRessourcesFor(id);
                        }
                    });
                } else {
                    threshold = meantime;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stanza Gate (" + name + ") : " + stanza.getSequencePrincipal() + ":" + stanza.getSequenceNumber() + ":" + threshold);
            }
            if (threshold.compareAndSet(stanza.getSequenceNumber(), stanza.getSequenceNumber() + 1)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Best case, Threshold: " + threshold.get());
                }
                stanza.trace("Passing gate " + name);
                if (customAction != null) {
                    customAction.handle(stanza, recipient);
                } else {
                    //TODO: for 7.4 this shouldn't throw Exceptions at all. The class implementing the method has to handle it
                    try {
                        handleInternal(stanza, recipient);
                    } catch (Exception ex) {
                        throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(ex, ex.getMessage());
                    }
                }

                /* Drain Stanzas accumulated while waiting for the missing SequenceNumber */
                List<StanzaWithCustomAction> stanzas = inboxes.remove(stanza.getSequencePrincipal());
                if (stanzas == null || stanzas.isEmpty()) {
                    return true;
                }

                Collections.sort(stanzas, new Comparator<StanzaWithCustomAction>() {

                    @Override
                    public int compare(StanzaWithCustomAction arg0, StanzaWithCustomAction arg1) {
                        return (int) (arg0.stanza.getSequenceNumber() - arg1.stanza.getSequenceNumber());
                    }
                });

                /*
                 * There still might be some gaps in the list of accumulated stanzas. Therefore the recursive calling of handle will assure
                 * that affected stanzas are cached again.
                 */
                for (StanzaWithCustomAction s : stanzas) {
                    s.stanza.trace("Handle preserved (" + name + ")");
                    handle(s.stanza, s.stanza.getTo(), s.action);
                }

                return true;
            } else {
                /* Stanzas got out of sync, enqueue until we receive the Stanza matching threshold */
                if (threshold.get() > stanza.getSequenceNumber()) {
                    // Discard as this stanza already passed the gate once
                    stanza.trace("Discarded as this sequence number has already successfully passed this gate: " + stanza.getSequenceNumber());
                    LOG.debug("Discarded as this sequence number has already successfully passed this gate: " + stanza.getSequenceNumber());
                    return true;
                }

                List<StanzaWithCustomAction> inbox = inboxes.get(stanza.getSequencePrincipal());
                if (inbox == null) {
                    inbox = Collections.synchronizedList(new ArrayList<StanzaWithCustomAction>());
                    List<StanzaWithCustomAction> oldList = inboxes.putIfAbsent(stanza.getSequencePrincipal(), inbox);
                    inbox = (oldList != null) ? oldList : inbox;
                }

                if (inbox.size() < BUFFER_SIZE) {
                    if(threshold.get() == 0 && stanza.getSequenceNumber() > 10) {
                        stanza.trace("Threshold == 0 and stanza not in sequence, instructing client to reset sequence.");
                        // Abort by throwing exception
                        throw RealtimeExceptionCodes.SEQUENCE_INVALID.create();
                    }
                    stanza.trace("Not in sequence, enqueing");
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Stanzas not in sequence, Threshold: " + threshold.get() + " SequenceNumber: " + stanza.getSequenceNumber());
                    }

                    inbox.add(new StanzaWithCustomAction(stanza, customAction));
                    return true;
                }

                stanza.trace("Buffer full, discarding");
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Discarding. Stanzas not in sequence, but buffer is full. Threshold: " + threshold.get() + " SequenceNumber: " + stanza.getSequenceNumber());
                }
                return false;
            }
        } finally {
            stanza.getSequencePrincipal().unlock("gate");
        }

    }

    public void freeRessourcesFor(ID sequencePrincipal) {
        sequenceNumbers.remove(sequencePrincipal);
        inboxes.remove(sequencePrincipal);
    }

    /**
     * Procedure to handle incoming Stanzas. Has to be implemented by the Components that want to make use of a StanzaSequenceGate and have
     * to adapt the logic of the Stanza handling after the gateway enforced the Sequence of the received Stanzas.
     *
     * @param stanza The incoming Stanza
     * @param recipient The recipient of the incoming Stanza
     * @throws OXException If the Stanza couldn't be handled
     */
    public abstract void handleInternal(Stanza stanza, ID recipient) throws OXException;

    protected final class StanzaWithCustomAction {

        public Stanza stanza;

        public CustomGateAction action;

        public StanzaWithCustomAction(Stanza stanza, CustomGateAction action) {
            super();
            this.stanza = stanza;
            this.action = action;
        }

    }

}
