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

package com.openexchange.realtime.hazelcast.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.handle.StanzaStorage;
import com.openexchange.realtime.handle.TimedStanza;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.packet.ID;


/**
 * {@link HazelcastStanzaStorage}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class HazelcastStanzaStorage implements StanzaStorage {
    
    // TODO: configure explicitly with eviction time etc.
    private static final String STANZA_MAP = "rtStanzaStorage-0";
    

    @Override
    public void pushStanza(ID forID, TimedStanza stanza) throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        MultiMap<ID, TimedStanza> stanzaMap = hazelcast.getMultiMap(STANZA_MAP);
        stanza.makeSerializable();
        stanzaMap.put(forID, stanza);
    }

    @Override
    public List<TimedStanza> popStanzas(ID forID) throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        MultiMap<ID, TimedStanza> stanzaMap = hazelcast.getMultiMap(STANZA_MAP);
        Collection<TimedStanza> stanzas = stanzaMap.remove(forID);
        if (stanzas == null || stanzas.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<TimedStanza> sortedStanzas = new ArrayList<TimedStanza>(stanzas);
        Collections.sort(sortedStanzas, new Comparator<TimedStanza>() {
            @Override
            public int compare(TimedStanza o1, TimedStanza o2) {
                long t1 = o1.getTimestamp();
                long t2 = o2.getTimestamp();
                
                if (t1 == t2) {
                    return 0;
                } else if (t1 < t2) {
                    return -1;
                }

                return 1;
            }
        });
        for (TimedStanza timedStanza : sortedStanzas) {
            timedStanza.makeInternal();
        }
        return sortedStanzas;
    }

}
