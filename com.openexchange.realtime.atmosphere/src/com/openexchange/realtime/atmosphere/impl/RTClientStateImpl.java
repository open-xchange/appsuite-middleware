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

package com.openexchange.realtime.atmosphere.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.realtime.atmosphere.protocol.RTClientState;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;


/**
 * The {@link RTClientStateImpl} encapsulates the state of a connected client.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTClientStateImpl implements RTClientState {
    
    private ID id;
    private Map<Long, EnqueuedStanza> resendBuffer = new HashMap<Long, EnqueuedStanza>();
    private List<Stanza> nonsequenceStanzas = new ArrayList<Stanza>();

    private Lock lock = new ReentrantLock();
    private long lastSeen;
    private long sequenceNumber = 0;
        
    public RTClientStateImpl(ID concreteID) {
        this.id = concreteID;
        touch();
    }

    @Override
    public void acknowledgementReceived(long sequenceNumber) {
        try {
            lock();
            resendBuffer.remove(sequenceNumber);
        } finally {
            unlock();
        }
    }
    
    @Override
    public void enqueue(Stanza stanza) {
        if (stanza.getSequenceNumber() != -1) {
            try {
                lock();
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
        try {
            lock();
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
        try {
            lock();
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
    public ID getId() {
        return id;
    }

    @Override
    public void lock() {
        lock.lock();
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
        return 120000 < timestamp - lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
}
