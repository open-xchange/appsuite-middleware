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

    /* Keep track of SequencePrincalpal(ID) to thresholds(sequence number of last seen stanza) */
    private ConcurrentHashMap<ID, AtomicLong> sequenceNumbers = new ConcurrentHashMap<ID, AtomicLong>();

    private ConcurrentHashMap<ID, List<Stanza>> inboxes = new ConcurrentHashMap<ID, List<Stanza>>();

    public void handle(Stanza stanza, ID recipient) throws OXException {
        /* Stanza didn't carry a valid Sequencenumber, just handle it without pestering the gate and return */ 
        if (stanza.getSequenceNumber() == -1) {
            handleInternal(stanza, recipient);
            return;
        }
        try {
            stanza.getSequencePrincipal().lock("gate");
            AtomicLong threshhold = sequenceNumbers.get(stanza.getSequencePrincipal());
            /* We haven't recorded a threshold for this principal, yet */
            if (threshhold == null) {
                threshhold = new AtomicLong(stanza.getSequenceNumber());
                AtomicLong meantime = sequenceNumbers.putIfAbsent(stanza.getSequencePrincipal(), threshhold);
                /*
                 * Add eventhandler to clean up the traces we left in the gate when the the principal receives the dispose event, e.g when
                 * all members left the GroupDispatcher(SequencePrincipal)
                 */
                if (meantime == null) {
                    stanza.getSequencePrincipal().on("dispose", new IDEventHandler() {

                        @Override
                        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
                            sequenceNumbers.remove(id);
                            inboxes.remove(id);
                        }
                    });
                } else {
                    threshhold = meantime;
                }
            }
            /* Best case, we found the follow up Stanza */
            if (stanza.getSequenceNumber() == 0) {
                threshhold.set(0);
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stanza Gate: " + stanza.getSequencePrincipal()+":"+stanza.getSequenceNumber() + ":" + threshhold);
            }
            if (threshhold.compareAndSet(stanza.getSequenceNumber(), stanza.getSequenceNumber() + 1)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Best case, Threshold: " + threshhold.get());
                }
                handleInternal(stanza, recipient);
                /* Drain Stanzas accumulated while waiting for the missing SequenceNumber */
                List<Stanza> stanzas = inboxes.remove(stanza.getSequencePrincipal());

                if (stanzas == null || stanzas.isEmpty()) {
                    return;
                }
                Collections.sort(stanzas, new Comparator<Stanza>() {

                    public int compare(Stanza arg0, Stanza arg1) {
                        return (int) (arg0.getSequenceNumber() - arg1.getSequenceNumber());
                    }

                });
                for (Stanza s : stanzas) {
                    handle(s, s.getTo());
                }

                /* Stanzas got out of sync, enqueue until we receive the Stanza matching threshold */
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Stanzas not in sequence, Threshold: " + threshhold.get() + " SequenceNumber: " + stanza.getSequenceNumber());
                }
                List<Stanza> inbox = inboxes.get(stanza.getSequencePrincipal());
                if (inbox == null) {
                    inbox = Collections.synchronizedList(new ArrayList<Stanza>());
                    List<Stanza> oldList = inboxes.putIfAbsent(stanza.getSequencePrincipal(), inbox);
                    inbox = (oldList != null) ? oldList : inbox;
                }
                inbox.add(stanza);
            }
        } finally {
            stanza.getSequencePrincipal().unlock("gate");
        }

    }

    public abstract void handleInternal(Stanza stanza, ID recipient) throws OXException;

}
