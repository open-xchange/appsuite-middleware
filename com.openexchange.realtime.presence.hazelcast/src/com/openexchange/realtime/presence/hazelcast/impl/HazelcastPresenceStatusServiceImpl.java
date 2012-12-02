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

package com.openexchange.realtime.presence.hazelcast.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.presence.PresenceChangeListener;
import com.openexchange.realtime.presence.PresenceData;
import com.openexchange.realtime.presence.PresenceStatusService;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link HazelcastPresenceStatusServiceImpl} - Hazelcast based PresenceStatusService that is implemented via a distributed Map containing
 * <ID, PresenceData> pairs.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
// TODO: PresenceService must honor priority
public class HazelcastPresenceStatusServiceImpl implements PresenceStatusService {

    private final ConcurrentMap<ID, PresenceData> statusMap;

    private final CopyOnWriteArrayList<PresenceChangeListener> presenceChangeListeners;

    private enum PresenceChangeType {
        COMING_ONLINE, ONLINE_CHANGE, GOING_OFFLINE
    }

    /**
     * Initializes a new {@link HazelcastPresenceStatusServiceImpl}.
     * 
     * @param hazelcastInstance The haszelcastInstance of this server which is used to get the presenceStatus Map distributed in the cluster
     */
    public HazelcastPresenceStatusServiceImpl(HazelcastInstance hazelcastInstance) {
        this.statusMap = hazelcastInstance.getMap("com.openexchange.realtime.presence.hazelcast.statusMap");
        this.presenceChangeListeners = new CopyOnWriteArrayList<PresenceChangeListener>();
    }

    @Override
    public void registerPresenceChangeListener(PresenceChangeListener presenceChangeListener) {
        presenceChangeListeners.add(presenceChangeListener);
    }

    @Override
    public void unregisterPresenceChangeListener(PresenceChangeListener presenceChangeListener) {
        presenceChangeListeners.remove(presenceChangeListener);
    }

    @Override
    public void changePresenceStatus(Presence stanza, ServerSession serverSession) {
        if (stanza == null || serverSession == null) {
            throw new IllegalStateException("Obligatory parameter missing.");
        }
        // check type before setting new status
        PresenceChangeType presenceChangeType = checkPresenceChangeType(stanza);

        ID from = stanza.getFrom();
        PresenceData presenceData = new PresenceData(stanza.getState(), stanza.getMessage());
        statusMap.put(from, presenceData);

        for (PresenceChangeListener listener : presenceChangeListeners) {
            switch (presenceChangeType) {
            case COMING_ONLINE:
                listener.initialPresence(stanza, serverSession);
                break;
            case ONLINE_CHANGE:
                listener.normalPresence(stanza, serverSession);
                break;
            case GOING_OFFLINE:
                listener.finalPresence(stanza, serverSession);
                break;
            }
        }

    }

    @Override
    public PresenceData getPresenceStatus(ID id) {
        if (id == null) {
            throw new IllegalStateException("Obligatory parameter missing.");
        }
        PresenceData presenceData = statusMap.get(id);
        // id wasn't seen yet, no status saved, show as offline
        if (presenceData == null) {
            presenceData = PresenceData.OFFLINE;
        }
        return presenceData;
    }

    @Override
    public IDMap<PresenceData> getPresenceStatus(Collection<ID> ids) {
        IDMap<PresenceData> results = new IDMap<PresenceData>();
        for (ID id : ids) {
            results.put(id, getPresenceStatus(id));
        }
        return results;
    }

    private PresenceChangeType checkPresenceChangeType(Presence stanza) {
        if (isInitialPresence(stanza)) {
            return PresenceChangeType.COMING_ONLINE;
        } else if (isFinalPresence(stanza)) {
            return PresenceChangeType.GOING_OFFLINE;
        } else {
            return PresenceChangeType.ONLINE_CHANGE;
        }
    }

    /**
     * Are we dealing with an initial Presence Stanza iow. was the client offline before?
     * 
     * @param stanza The incoming Presence Stanza that has to be insepcted
     * @return true if the client is sending an initial Presence, false otherwise
     * @throws OXException If the AtmospherePresenceService can't be queried
     */
    private boolean isInitialPresence(Presence stanza) {
        boolean isInitial = false;
        PresenceData presenceData = getPresenceStatus(stanza.getFrom());
        if (PresenceState.OFFLINE.equals(presenceData.getState())) {
            isInitial = true;
        }
        return isInitial;
    }

    /**
     * Are we dealing with a final Presence Stanza iow. is the client going offline?
     * 
     * @param stanza The incoming Presence Stanza that has to be inspected
     * @return true if the client is sending a final Presence, false otherwise
     */
    private boolean isFinalPresence(Presence stanza) {
        boolean isFinal = false;
        Type type = stanza.getType();
        if (Type.UNAVAILABLE.equals(type)) {
            isFinal = true;
        }
        return isFinal;
    }

}
