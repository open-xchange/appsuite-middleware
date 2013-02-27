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
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.presence.PresenceChangeListener;
import com.openexchange.realtime.presence.PresenceData;
import com.openexchange.realtime.presence.PresenceStatusService;
import com.openexchange.realtime.presence.hazelcast.osgi.RealtimeHazelcastPresenceActivator;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link HazelcastPresenceStatusServiceImpl} - Hazelcast based PresenceStatusService that is implemented via a distributed Map containing
 * <ID, PresenceData> pairs.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
// TODO: PresenceService must honor priority
public class HazelcastPresenceStatusServiceImpl implements PresenceStatusService {

    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(HazelcastPresenceStatusServiceImpl.class);

    private static final AtomicReference<HazelcastInstance> REFERENCE = new AtomicReference<HazelcastInstance>();

    /**
     * Sets specified {@link HazelcastInstance}.
     * 
     * @param hazelcast The {@link HazelcastInstance}
     */
    public static void setHazelcastInstance(HazelcastInstance hazelcast) {
        REFERENCE.set(hazelcast);
    }

    private enum PresenceChangeType {
        COMING_ONLINE, ONLINE_CHANGE, GOING_OFFLINE
    }

    private final CopyOnWriteArrayList<PresenceChangeListener> presenceChangeListeners;

    private final ResourceDirectory resourceDirectory;

    /**
     * Initializes a new {@link HazelcastPresenceStatusServiceImpl}.
     * 
     * @param statusMapName The name of the distributed map to use
     */
    public HazelcastPresenceStatusServiceImpl(ResourceDirectory resourceDirectory) {
        super();
        this.presenceChangeListeners = new CopyOnWriteArrayList<PresenceChangeListener>();
        this.resourceDirectory = resourceDirectory;
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
    public void changePresenceStatus(Presence stanza) throws OXException {
        if (stanza == null) {
            throw new IllegalStateException("Obligatory parameter stanza missing.");
        }
        /*
         * update presence status in map
         */
        PresenceData presence = new PresenceData(stanza.getState(), stanza.getMessage());
        PresenceData previousPresence = statusMap().put(key(stanza.getFrom()), presence);
        /*
         * determine presence change type
         */
        PresenceChangeType presenceChangeType;
        if (null == previousPresence || PresenceState.OFFLINE.equals(previousPresence.getState())) {
            presenceChangeType = PresenceChangeType.COMING_ONLINE;
        } else if (Type.UNAVAILABLE.equals(stanza.getType())) {
            presenceChangeType = PresenceChangeType.GOING_OFFLINE;
        } else {
            presenceChangeType = PresenceChangeType.ONLINE_CHANGE;
        }
        /*
         * notify listeners
         */
        for (PresenceChangeListener listener : presenceChangeListeners) {
            switch (presenceChangeType) {
            case COMING_ONLINE:
                listener.initialPresence(stanza);
                break;
            case ONLINE_CHANGE:
                listener.normalPresence(stanza);
                break;
            case GOING_OFFLINE:
                listener.finalPresence(stanza);
                break;
            }
        }
    }

    /*
     * http://xmpp.org/rfcs/rfc3921.html#presence-resp-probes
     */
    @Override
    public PresenceData getPresenceStatus(ID id) throws OXException {
        if (id == null) {
            throw new IllegalStateException("Obligatory parameter missing.");
        }
        PresenceData presenceData = statusMap().get(key(id));
        IDMap<Resource> idMap = resourceDirectory.get(id);
        int mapSize = idMap.size();
        
        if(mapSize == 0) {
            /*
             * If the contact has no available resources, the server MUST either (1) reply to the presence probe by sending to the user the
             * full content of the last presence stanza of type "unavailable" received by the server from the contact, or (2) not reply at all.
             */
            presenceData = PresenceData.OFFLINE;
        } else if (mapSize >=1) {
            /*
             * Else, if the contact has at least one available resource, the server MUST reply to the presence probe by sending to the user
             * the full content of the last presence stanza with no 'to' attribute received by the server from each of the contact's
             * available resources (again, subject to privacy lists in force for each session).
             */
            Entry<ID, Resource> mostRecentPresence = findMostRecentStatus(idMap.entrySet());
            Entry<ID, Resource> firstEntry = idMap.entrySet().iterator().next();
            Resource idResource = firstEntry.getValue();
            new PresenceData(idResource.getPresenceState(), idResource.getMessage());
        }
        return presenceData;
    }

    /**
     * Find the most recent status the client sent to the server.
     * @param idEntries a Set of Entrys containing ID and Resource which contans the status
     * @return the entry with the most recent Status or null if no entry can be found. 
     */
    private Entry<ID, Resource> findMostRecentStatus(Set<Entry<ID, Resource>> idEntries) {
        if(idEntries.isEmpty()) {
            return null;
        }
        //TODO: sort set and return first entry
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public IDMap<PresenceData> getPresenceStatus(Collection<ID> ids) throws OXException {
//        IDMap<PresenceData> results = new IDMap<PresenceData>();
//        for (Entry<String, PresenceData> entry : statusMap().getAll(keys(ids)).entrySet()) {
//            results.put(new ID(entry.getKey()), entry.getValue());
//        }
//        return results;
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Gets the 'rtPresence' map that maps IDs to their presence states, throwing appropriate exceptions if the map can't be accessed.
     * 
     * @return The 'sessions' map
     * @throws OXException
     */
    private IMap<String, PresenceData> statusMap() throws OXException {
//        try {
//            HazelcastInstance hazelcastInstance = REFERENCE.get();
//            if (null == hazelcastInstance || false == hazelcastInstance.getLifecycleService().isRunning()) {
//                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
//            }
//            return hazelcastInstance.getMap(statusMapName);
//        } catch (RuntimeException e) {
//            throw RealtimeExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
//        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Creates a key for the supplied ID as used by the distributed maps.
     * 
     * @param id The ID
     * @return The key
     */
    private static String key(ID id) {
        return id.toString();
    }

    /**
     * Creates a set of keys for the supplied IDs as used by the distributed maps.
     * 
     * @param ids The IDs
     * @return The keys
     */
    private static Set<String> keys(Collection<ID> ids) {
        Set<String> keys = new HashSet<String>();
        if (null != ids && 0 < ids.size()) {
            for (ID id : ids) {
                keys.add(key(id));
            }
        }
        return keys;
    }

}
