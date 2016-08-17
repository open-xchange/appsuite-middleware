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

package com.openexchange.realtime.json.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.openexchange.java.util.UUIDs;
import com.openexchange.realtime.json.actions.SendAction;
import com.openexchange.realtime.json.protocol.RTClientState;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.Duration;
import com.openexchange.realtime.util.OwnerAwareReentrantLock;


/**
 * The {@link RTClientStateImpl} encapsulates the state of a connected client by keeping track of the sequenced and unsequenced Stanzas that
 * still have to be delivered to the client.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RTClientStateImpl implements RTClientState {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SendAction.class);

    private final ID id;
    private final Map<Long, EnqueuedStanza> resendBuffer = new HashMap<Long, EnqueuedStanza>();
    private final List<Stanza> nonsequenceStanzas = new ArrayList<Stanza>();

    private final OwnerAwareReentrantLock lock = new OwnerAwareReentrantLock();
    private long lastSeen;
    private long sequenceNumber = 0;

    public RTClientStateImpl(ID concreteID) {
        this.id = concreteID;
        touch();
    }

    @Override
    public void acknowledgementReceived(long sequenceNumber) {
        lock();
        try {
            resendBuffer.remove(sequenceNumber);
        } finally {
            unlock();
        }
    }

    @Override
    public void enqueue(Stanza stanza) {
        if (stanza.getId() == null || stanza.getId().trim().equals("")) {
        	stanza.setId(UUIDs.getUnformattedStringFromRandom());
        }
    	stanza.trace("Using ID: " + stanza.getId());
        if (stanza.getSequenceNumber() != -1) {
            lock();
            try {
                stanza.setSequenceNumber(sequenceNumber);
                stanza.trace("RTClientState recasting stanza to sequence number "+ sequenceNumber + " for delivery");
                sequenceNumber++;
            } finally {
                unlock();
            }
            stanza.trace("add to resend buffer");
            resendBuffer.put(stanza.getSequenceNumber(), new EnqueuedStanza(stanza));
        } else {
            stanza.trace("add to nonsequenceStanza list");
            nonsequenceStanzas.add(stanza);
        }
    }

    public Map<Long, EnqueuedStanza> getResendBuffer() {
        return resendBuffer;
    }

    public List<Stanza> getNonsequenceStanzas() {
        return nonsequenceStanzas;
    }

    @Override
    public List<Stanza> getStanzasToSend() {
        lock();
        try {
            List<Stanza> list = new ArrayList<Stanza>(resendBuffer.size() + nonsequenceStanzas.size());
            for(EnqueuedStanza es: resendBuffer.values()) {
                list.add(es.stanza);
            }
            list.addAll(nonsequenceStanzas);

            Collections.sort(list, new Comparator<Stanza>() {

                @Override
                public int compare(Stanza s1, Stanza s2) {
                    return (int) (s1.getSequenceNumber() - s2.getSequenceNumber());
                }

            });

            return list;
        } finally {
            unlock();
        }
    }

    @Override
    public void purge() {
        lock();
        try {
            for (Stanza s : nonsequenceStanzas) {
                s.trace("Stanza could not be delivered");
            }
            nonsequenceStanzas.clear();
            List<Long> toRemove = new ArrayList<Long>(resendBuffer.size());
            for(EnqueuedStanza es: resendBuffer.values()) {
                if (!es.incCounter()) {
                    toRemove.add(es.sequenceNumber);
                    es.stanza.trace("TTL reached, this stanza will be lost!");
                }
            }
            for (Long seq : toRemove) {
                resendBuffer.remove(seq);
            }
        } finally {
            unlock();
        }
    }

    @Override
    public void reset() {
        LOG.debug("Removing all sequenced and unsequenced Stanzas.");
        lock();
        try {
            nonsequenceStanzas.clear();
            resendBuffer.clear();
        } finally {
            unlock();
        }
    }

    @Override
    public ID getId() {
        return id;
    }

    @Override
    public Thread getOwner() {
        return lock.getOwner();
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return lock.tryLock(timeout, unit);
    }

    @Override
    public void lock() {
        lock.lock();

//        boolean lockAcquired = false;
//        long waitedSeconds = 0L;
//        while (!lockAcquired) {
//            try {
//                lockAcquired = lock.tryLock(5, TimeUnit.SECONDS);
//            } catch (InterruptedException x) {
//                Thread.currentThread().interrupt();
//                return;
//            }
//
//            if (!lockAcquired) {
//                Thread owner = lock.getOwner();
//                if (null != owner) {
//                    waitedSeconds += 5;
//                    FastThrowable t = new FastThrowable("Trace from thread " + owner.getName());
//                    t.setStackTrace(owner.getStackTrace());
//                    LOG.warn("Thread {} failed to get lock for client state {} after {}sec. Lock still acquired by thread {}:", Thread.currentThread().getName(), id, waitedSeconds, owner.getName(), t);
//                }
//            }
//        }
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public void touch() {
        lastSeen = System.currentTimeMillis();
    }

    @Override
    public long getLastSeen() {
        return lastSeen;
    }

    @Override
    public boolean isTimedOut(long timestamp) {
        return 30 * 60 * 1000 < (timestamp - lastSeen);
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public Duration getInactivityDuration() {
        long diff = System.currentTimeMillis() - this.lastSeen;
        diff = diff < 0 ? 0 : diff;
        return Duration.roundDownTo(diff, MILLISECONDS);
    }

}
