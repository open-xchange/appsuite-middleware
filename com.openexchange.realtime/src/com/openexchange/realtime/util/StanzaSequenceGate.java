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

package com.openexchange.realtime.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementAware;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.cleanup.AbstractRealtimeJanitor;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.management.StanzaSequenceGateMBean;
import com.openexchange.realtime.management.StanzaSequenceGateManagement;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;

/**
 * A {@link StanzaSequenceGate} ensures that stanzas are handled in a well defined order. Sequence numbers are expected to always rise by
 * one (1, 2, 3, 4....). If a sequence number is skipped the stanza sequence gate will hold back handling the stanza until the missing
 * stanza arrives, and then handle all held back stanzas in turn. If a stanza contains no sequence number (-1) then it will be handled
 * immediately
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class StanzaSequenceGate extends AbstractRealtimeJanitor implements ManagementAware<StanzaSequenceGateMBean> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(StanzaSequenceGate.class);

    /* Max. number of stanzas that will be buffered if one stanza is missing in the sequence */
    protected static final int BUFFER_SIZE = 20;

    /* Keep track of SequencePrincalpal(ID) to thresholds(sequence number of last seen stanza) */
    protected ConcurrentHashMap<ID, AtomicLong> sequenceNumbers = new ConcurrentHashMap<ID, AtomicLong>();

    protected ConcurrentHashMap<ID, List<StanzaWithCustomAction>> inboxes = new ConcurrentHashMap<ID, List<StanzaWithCustomAction>>();

    private final String name;
    private final String lockScope;
    private final StanzaSequenceGateManagement managementObject;

    /**
     * Initializes a new {@link StanzaSequenceGate}.
     *
     * @param name The unique name for this gate
     */
    protected StanzaSequenceGate(String name) {
        super();
        this.name = name;
        this.lockScope = new StringBuilder("gate-").append(name).toString();
        this.managementObject = new StanzaSequenceGateManagement(name);
        initManagementObject(managementObject);
    }

    private void initManagementObject(StanzaSequenceGateManagement managementObject) {
        managementObject.setBufferSize(BUFFER_SIZE);
        managementObject.setNumberOfInboxes(inboxes.size());
    }

    @Override
    public ManagementObject<StanzaSequenceGateMBean> getManagementObject() {
        return managementObject;
    }


    /**
     * Ensures a correct order of sequence numbers of incoming Stanzas.
     *
     * <ul>
     *   <li>Stanzas that don't carry a sequence number are directly handed over to the specific
     *   {@link StanzaSequenceGate#handleInternal(Stanza, ID)} implementation of the components that makes use of this StanzaSequenceGate
     *   and have to adapt the logic of the Stanza handling of the received Stanzas.</li>
     *   <li>Stanzas that carry a sequence number are enqueued until a correct sequence is established.
     *     <ul>
     *       <li>Stanzas that already passed this gate are discarded and an acknowledgement is sent again for the discarded stanza so the client
     *       finally stops resending.</li>
     *       <li>If the buffer for creating a valid sequence is full Stanzas are discarded.</li>
     *     </ul>
     * </ul>
     *
     * @param stanza the incoming stanza
     * @param recipient the recipient of the incoming Stanza
     * @return If <code>false</code> an ACK shouldn't be sent to the client as the Stanza wasn't handled properly
     * @throws RealtimeException if the user of this gate couldn't handle the stanza internally
     * @throws RealtimeException with code 1006 if an invalid sequence number was detected. The component using this gate has to ensure this
     *             exception reaches the client
     */
    public boolean handle(Stanza stanza, ID recipient) throws RealtimeException {
        return handle(stanza, recipient, null);
    }

    /**
     * Ensures a correct order of sequence numbers of incoming Stanzas.
     *
     * <ul>
     *   <li>Stanzas that don't carry a sequence number are directly handed over to the specific
     *   {@link StanzaSequenceGate#handleInternal(Stanza, ID)} implementation of the components that makes use of this StanzaSequenceGate
     *   and have to adapt the logic of the Stanza handling of the received Stanzas.</li>
     *   <li>Stanzas that carry a sequence number are enqueued until a correct sequence is established.
     *     <ul>
     *       <li>Stanzas that already passed this gate are discarded and an acknowledgement is sent again for the discarded stanza so the client
     *       finally stops resending.</li>
     *       <li>If the buffer for creating a valid sequence is full Stanzas are discarded.</li>
     *     </ul>
     * </ul>
     *
     * @param stanza the incoming stanza
     * @param recipient the recipient of the incoming Stanza
     * @param customAction a custom action that (if not null) should be used instead of the using component's handleInternal.
     * @return If <code>false</code> an ACK shouldn't be sent to the client as the Stanza wasn't handled properly
     * @throws RealtimeException if the user of this gate couldn't handle the stanza internally
     * @throws RealtimeException with code 1006 if an invalid sequence number was detected. The component using this gate has to ensure this
     *             exception reaches the client
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

        ID sequencePrincipal = stanza.getSequencePrincipal();
        sequencePrincipal.lock(lockScope);
        try {
            AtomicLong threshold = sequenceNumbers.get(stanza.getSequencePrincipal());
            /* We haven't recorded a threshold (upper bound of last known sequence number) for this principal, yet so we'll add one */
            if (threshold == null) {
                threshold = new AtomicLong(0);
                AtomicLong meantime = sequenceNumbers.putIfAbsent(stanza.getSequencePrincipal(), threshold);
                if(meantime != null) {
                    LOG.debug("Found another number: {} in the meantime for the SequencePrincipal: {}", meantime, stanza.getSequencePrincipal());
                    threshold = meantime;
                }
            }
            LOG.debug("Stanza Gate ({}) : {}:{}:{}", name, stanza.getSequencePrincipal(), stanza.getSequenceNumber(), threshold);
            if (stanza.getSequenceNumber() == -1) {
                threshold.set(-1);
            }
            if (threshold.compareAndSet(stanza.getSequenceNumber(), stanza.getSequenceNumber() + 1)) {
                LOG.debug("Best case, Threshold: {}", threshold.get());
                notifyManagementSequenceNumbers();
                stanza.trace("Passing gate " + name);
                if (customAction != null) {
                    customAction.handle(stanza, recipient);
                } else {
                    //TODO: for 7.4 this shouldn't throw Exceptions at all. The class implementing the method has to handle it
                    try {
                        handleInternal(stanza, recipient);
                    } catch (RealtimeException x) {
                        throw x;
                    } catch (Exception ex) {
                        throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(ex, ex.getMessage());
                    }
                }

                /* Drain Stanzas accumulated while waiting for the missing SequenceNumber */
                List<StanzaWithCustomAction> stanzas = inboxes.remove(stanza.getSequencePrincipal());
                notifyManagementInboxes();
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
            } else { // We didn't hit the best case, either the Stanza was already received or the sequence number is too high
                LOG.debug(String.format("Expected sequence %d but got %d", threshold.get(), stanza.getSequenceNumber()));
                /* Stanzas got out of sync, enqueue until we receive the Stanza matching threshold */
                if (threshold.get() > stanza.getSequenceNumber()) {
                    stanza.trace("Discarded as this sequence number has already successfully passed this gate: " + stanza.getSequenceNumber());
                    LOG.debug("Discarded as this sequence number has already successfully passed this gate: {}, {}", stanza.getSequenceNumber(), stanza);
                    return true;
                }

                List<StanzaWithCustomAction> inbox = inboxes.get(stanza.getSequencePrincipal());
                if (inbox == null) {
                    inbox = Collections.synchronizedList(new ArrayList<StanzaWithCustomAction>());
                    List<StanzaWithCustomAction> oldList = inboxes.putIfAbsent(stanza.getSequencePrincipal(), inbox);
                    inbox = (oldList != null) ? oldList : inbox;
                    notifyManagementNumberOfInboxes();
                }

                if (inbox.size() < BUFFER_SIZE) {
                    //We see no reason to buffer if the gap in the sequence numbers is too big. instruct the client to reset the sequence
                    if(stanza.getSequenceNumber() > threshold.get() + BUFFER_SIZE) {
                        stanza.trace("Threshold == 0 and stanza not in sequence, instructing client to reset sequence.");
                        LOG.debug("Threshold == 0 and stanza not in sequence, instructing client to reset sequence.");
                        throw RealtimeExceptionCodes.SEQUENCE_INVALID.create();
                    }
                    //Try to buffer up a valid sequence of Stanzas
                    boolean alreadyContained = false;
                    for (StanzaWithCustomAction stanzaWithCustomAction : inbox) {
                        if(stanzaWithCustomAction.sequenceNumber == stanza.getSequenceNumber()) {
                            alreadyContained = true;
                            break;
                        }
                    }
                    if(!alreadyContained) {
                        stanza.trace("Not in sequence, enqueing");
                            LOG.debug("Stanzas not in sequence, Threshold: {} SequenceNumber: {}", threshold.get(), stanza.getSequenceNumber());
                        inbox.add(new StanzaWithCustomAction(stanza, customAction));
                        notifyManagementInboxes();
                        return true;
                    } else {
                        stanza.trace("Not in sequence but already enqueued, discarding.");
                            LOG.debug("Stanzas not in sequence, Threshold: {} SequenceNumber: {} but already buffered, discarding.", threshold.get(), stanza.getSequenceNumber());
                        return true;
                    }
                } else {
                    stanza.trace("Buffer full, instructing client to reset sequence");
                        LOG.debug("Instructing client to reset sequence because stanza's not in sequence, but buffer is full. Threshold: {} SequenceNumber: {}", threshold.get(), stanza.getSequenceNumber());
                    throw RealtimeExceptionCodes.SEQUENCE_INVALID.create();
                }
            }
        } finally {
            sequencePrincipal.unlock(lockScope);
        }

    }

    /**
     * Resets the current threshold for the given ID and empties the buffer of Stanzas with now incorrect sequence numbers
     * @param constructedId The ID for that we want to reset the threshold
     * @param newSequence the new sequence number to use
     * @throws RealtimeException
     */
    public void resetThreshold(ID constructedId, long newSequence) throws RealtimeException {
        constructedId.lock(lockScope);
        try {
            List<StanzaWithCustomAction> list = inboxes.get(constructedId);
            if (list != null) {
                list.clear();
            }
            sequenceNumbers.put(constructedId, new AtomicLong(newSequence));
            notifyManagementSequenceNumbers();
        } finally {
            constructedId.unlock(lockScope);
        }
    }


    public void freeResourcesFor(ID sequencePrincipal) {
        LOG.debug("Freeing Ressources for SequencePrincipal: {}", sequencePrincipal);
        sequenceNumbers.remove(sequencePrincipal);
        inboxes.remove(sequencePrincipal);
        notifyManagementSequenceNumbers();
        notifyManagementInboxes();
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

        public long sequenceNumber=0;
        public Stanza stanza;

        public CustomGateAction action;

        public StanzaWithCustomAction(Stanza stanza, CustomGateAction action) {
            super();
            this.stanza = stanza;
            this.action = action;
            this.sequenceNumber=stanza.getSequenceNumber();
        }

    }

    @Override
    public void cleanupForId(ID id) {
        LOG.debug("StanzasequenceGate-{}: Cleanup for ID: {}", name, id);
        freeResourcesFor(id);
    }

    // Management calls
    //======================================================================================================================================

    private void notifyManagementNumberOfInboxes() {
        managementObject.setNumberOfInboxes(inboxes.size());
    }

    private void notifyManagementSequenceNumbers() {
        HashMap<String, Long> basicSequenceNumbers = new HashMap<String, Long>(sequenceNumbers.size());
        for (Entry<ID, AtomicLong> entry : sequenceNumbers.entrySet()) {
            basicSequenceNumbers.put(entry.getKey().toString(), entry.getValue().get());
        }
        managementObject.setSequenceNumbers(basicSequenceNumbers);
    }

    private void notifyManagementInboxes() {
        notifyManagementNumberOfInboxes();

        Map<String, List<Long>> clientSequenceMap = new HashMap<String, List<Long>>();
        for (Entry<ID, List<StanzaWithCustomAction>> entry : inboxes.entrySet()) {
            String client = entry.getKey().toString();
            List<StanzaWithCustomAction> clientStanzas = entry.getValue();
            List<Long> sequenceListPerClient = new ArrayList<Long>(clientStanzas.size());
            for (StanzaWithCustomAction stanzaWithCustomAction : clientStanzas) {
                sequenceListPerClient.add(stanzaWithCustomAction.sequenceNumber);
            }
            clientSequenceMap.put(client, sequenceListPerClient);
        }
        managementObject.setInboxes(clientSequenceMap);
    }

}
