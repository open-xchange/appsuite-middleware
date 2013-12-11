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

package com.openexchange.realtime.hazelcast.directory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.Transaction;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentSet;
import com.openexchange.management.ManagementAware;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.directory.DefaultResourceDirectory;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.hazelcast.Services;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.management.HazelcastResourceDirectoryMBean;
import com.openexchange.realtime.hazelcast.management.HazelcastResourceDirectoryManagement;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.timer.TimerService;

/**
 * {@link HazelcastResourceDirectory} - Keeps mappings of general {@link ID}s to full {@link ID}s and full {@link ID}s to {@link Resource}.
 * New DefaultResources that are added to this directory are automatically converted to HazelcastResources and extended with the local
 * Hazelcast Member as routing info.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HazelcastResourceDirectory extends DefaultResourceDirectory implements ManagementAware<HazelcastResourceDirectoryMBean> {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastResourceDirectory.class);

    /** Mapping of general IDs to full IDs e.g marc.arens@premium <-> ox://marc.arens@premuim/random. */
    private final String id_map;

    /** Mapping of full IDs to the Resource e.g. ox://marc.arens@premuim/random <-> ResourceMap */
    private final String resource_map;

    private final HazelcastResourceDirectoryManagement managementObject;

    /** Keep track of synthetic ids */
    private final Set<ID> syntheticIDs;

    /**
     * Initializes a new {@link HazelcastResourceDirectory}.
     * 
     * @param id_map the name of the apping of general IDs to full IDs e.g marc.arens@premium <-> ox://marc.arens@premuim/random
     * @param resource_map the name of the mapping of full IDs to the Resource e.g. ox://marc.arens@premuim/random <-> ResourceMap
     * @throws OXException
     */
    public HazelcastResourceDirectory(String id_map, String resource_map) throws OXException {
        super();
        this.id_map = id_map;
        this.resource_map = resource_map;
        this.managementObject = new HazelcastResourceDirectoryManagement(this);
        syntheticIDs = new ConcurrentSet<ID>();
        getResourceMapping().addEntryListener(new EntryListener<String, Map<String, Serializable>>() {

            @Override
            public void entryUpdated(EntryEvent<String, Map<String, Serializable>> event) { /* nothing */
            }

            @Override
            public void entryRemoved(EntryEvent<String, Map<String, Serializable>> event) {
                ID id = new ID(event.getKey());
                boolean removed = syntheticIDs.remove(id);
                if (LOG.isDebugEnabled()) {
                    if (removed) {
                        LOG.debug("Removed id from refresh list: {}", id);
                    }
                }
            }

            @Override
            public void entryAdded(EntryEvent<String, Map<String, Serializable>> event) { /* nothing */
            }

            @Override
            public void entryEvicted(EntryEvent<String, Map<String, Serializable>> event) {
                String id = event.getKey();
                Object source = event.getSource();
                Member member = event.getMember();
                try {
                    if (getIDMapping().remove(new ID(id).toGeneralForm().toString(), id)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Source {} on Member: {} fired event. Removing mapping for '{}' due to eviction of according resource.", source, member, id);
                        }
                    }
                } catch (OXException e) {
                    LOG.warn("Could not handle eviction for id '{}'", id, e);
                }
                boolean removed = syntheticIDs.remove(new ID(id));
                if (LOG.isDebugEnabled()) {
                    if (removed) {
                        LOG.debug("Removed id from refresh list: {}", id);
                    }
                }
            }
        }, false);
        startRefreshTimer();
    }

    @Override
    public ManagementObject<HazelcastResourceDirectoryMBean> getManagementObject() {
        return managementObject;
    }

    @Override
    public IDMap<Resource> get(ID id) throws OXException {
        IDMap<Resource> foundResources = new IDMap<Resource>();
        if (id.isGeneralForm()) {
            MultiMap<String, String> idMapping = getIDMapping();
            Collection<String> concreteIds = idMapping.get(id.toString());
            if (concreteIds != null && !concreteIds.isEmpty()) {
                IMap<String, Map<String, Serializable>> allResources = getResourceMapping();
                Map<String, Map<String, Serializable>> resources = allResources.getAll(new HashSet<String>(concreteIds));
                if (resources != null) {
                    for (Entry<String, Map<String, Serializable>> entry : resources.entrySet()) {
                        ID foundId = new ID(entry.getKey());
                        HazelcastResource foundResource = HazelcastResourceWrapper.unwrap(entry.getValue());
                        foundResources.put(foundId, foundResource);
                    }
                }
            }
        } else {
            IMap<String, Map<String, Serializable>> allResources = getResourceMapping();
            Map<String, Serializable> resourceMap = allResources.get(id.toString());
            HazelcastResource resource = HazelcastResourceWrapper.unwrap(resourceMap);
            if (resource != null) {
                foundResources.put(id, resource);
            } else {
                resource = conjureResource(id);
                if (resource != null) {
                    foundResources.put(id, resource);
                }
            }
        }

        return foundResources;
    }

    @Override
    public IDMap<Resource> get(Collection<ID> ids) throws OXException {
        IDMap<Resource> foundResources = new IDMap<Resource>();
        Set<ID> generalIds = new HashSet<ID>();
        Set<ID> resourceIds = new HashSet<ID>();
        for (ID id : ids) {
            if (id.isGeneralForm()) {
                generalIds.add(id);
            } else {
                resourceIds.add(id);
            }
        }

        if (!resourceIds.isEmpty()) {
            IMap<String, Map<String, Serializable>> allResources = getResourceMapping();
            Map<String, Map<String, Serializable>> matchingResources = allResources.getAll(IDWrapper.idsToStringSet(resourceIds));
            if (matchingResources != null) {
                for (Entry<String, Map<String, Serializable>> entry : matchingResources.entrySet()) {
                    ID foundID = new ID(entry.getKey());
                    HazelcastResource foundResource = HazelcastResourceWrapper.unwrap(entry.getValue());
                    foundResources.put(foundID, foundResource);
                }
                // Remove all found Resources so we can try to conjure the rest
                resourceIds.removeAll(foundResources.keySet());
                for (ID id : resourceIds) {
                    HazelcastResource resource = conjureResource(id);
                    if (resource != null) {
                        foundResources.put(id, resource);
                    }
                }
            }
        }

        if (!generalIds.isEmpty()) {
            for (ID id : generalIds) {
                MultiMap<String, String> idMapping = getIDMapping();
                Collection<String> concreteIds = idMapping.get(id.toString());
                if (concreteIds != null && !concreteIds.isEmpty()) {
                    IMap<String, Map<String, Serializable>> allResources = getResourceMapping();
                    Map<String, Map<String, Serializable>> resources = allResources.getAll(new HashSet<String>(concreteIds));
                    if (resources != null) {
                        for (Entry<String, Map<String, Serializable>> entry : resources.entrySet()) {
                            ID foundID = new ID(entry.getKey());
                            HazelcastResource foundResource = HazelcastResourceWrapper.unwrap(entry.getValue());
                            foundResources.put(foundID, foundResource);
                        }
                    }
                }
            }
        }

        return foundResources;
    }

    @Override
    protected IDMap<Resource> doRemove(Collection<ID> ids) throws OXException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing IDs from HazelcastResourceDirectory: {}", ids);
        }
        IDMap<Resource> removedResources = new IDMap<Resource>();
        Set<ID> generalIds = new HashSet<ID>();
        Set<ID> resourceIds = new HashSet<ID>();
        for (ID id : ids) {
            if (id.isGeneralForm()) {
                generalIds.add(id);
            } else {
                resourceIds.add(id);
            }
        }

        MultiMap<String, String> idMapping = getIDMapping();
        IMap<String, Map<String, Serializable>> allResources = getResourceMapping();
        Transaction tx = newTransaction();
        tx.begin();
        try {
            if (!resourceIds.isEmpty()) {
                for (ID id : resourceIds) {
                    idMapping.remove(id.toGeneralForm().toString(), id.toString());
                    Map<String, Serializable> resourceMap = allResources.remove(id.toString());
                    if (resourceMap != null) {
                        HazelcastResource removedResource = HazelcastResourceWrapper.unwrap(resourceMap);
                        removedResources.put(id, removedResource);
                    }
                }
            }

            if (!generalIds.isEmpty()) {
                for (ID id : generalIds) {
                    Collection<String> toRemove = idMapping.remove(id.toString());
                    for (String concreteId : toRemove) {
                        Map<String, Serializable> resourceMap = allResources.remove(concreteId);
                        if (resourceMap != null) {
                            HazelcastResource removedResource = HazelcastResourceWrapper.unwrap(resourceMap);
                            removedResources.put(new ID(concreteId), removedResource);
                        }
                    }
                }
            }

            tx.commit();
        } catch (Throwable t) {
            tx.rollback();
            throw new OXException(t);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removed Resource(s) from HazelcastResourceDirectory: {}", removedResources);
        }
        return removedResources;
    }

    @Override
    protected IDMap<Resource> doRemove(ID id) throws OXException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing ID from HazelcastResourceDirectory: {}", id);
        }
        IDMap<Resource> removedResources = new IDMap<Resource>();
        MultiMap<String, String> idMapping = getIDMapping();
        IMap<String, Map<String, Serializable>> allResources = getResourceMapping();
        if (id.isGeneralForm()) {
            Transaction tx = newTransaction();
            tx.begin();
            try {
                Collection<String> toRemove = idMapping.remove(id.toString());
                if (toRemove != null) {
                    for (String concreteId : toRemove) {
                        Map<String, Serializable> resourceMap = allResources.remove(concreteId);
                        if (resourceMap != null) {
                            HazelcastResource removedResource = HazelcastResourceWrapper.unwrap(resourceMap);
                            removedResources.put(new ID(concreteId), removedResource);
                        }
                    }
                }

                tx.commit();
            } catch (Throwable t) {
                tx.rollback();
                throw new OXException(t);
            }
        } else {
            Transaction tx = newTransaction();
            tx.begin();
            try {
                idMapping.remove(id.toGeneralForm().toString(), id.toString());
                Map<String, Serializable> resourceMap = allResources.remove(id.toString());
                if (resourceMap != null) {
                    HazelcastResource removedResource = HazelcastResourceWrapper.unwrap(resourceMap);
                    removedResources.put(id, removedResource);
                }
                tx.commit();
            } catch (Throwable t) {
                tx.rollback();
                throw new OXException(t);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removed Resource(s) from HazelcastResourceDirectory: {}", removedResources);
        }
        return removedResources;
    }

    /*
     * Automatically converts incoming Resources to HazelcastResources (which extends them with hazelcast specific routing infos) before
     * adding them to the hazelcast data structures.
     */
    @Override
    protected HazelcastResource doSet(ID id, Resource resource, boolean overwrite) throws OXException {
        HazelcastResource hazelcastResource = new HazelcastResource(resource);
        id.on(ID.Events.REFRESH, TOUCH_ID);
        id.on(ID.Events.DISPOSE, CLEAN_UP);

        MultiMap<String, String> idMapping = getIDMapping();
        IMap<String, Map<String, Serializable>> allResources = getResourceMapping();
        HazelcastResource previousResource = null;
        Transaction tx = newTransaction();
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Starting transaction: %1$s for resource: %2$s", tx, hazelcastResource));
        }
        tx.begin();
        try {
            idMapping.put(id.toGeneralForm().toString(), id.toString());

            // don't overwrite exisiting Presence Data
            if (hazelcastResource.getPresence() == null) { // a DefaultResource / idle reconnect
                Map<String, Serializable> previousResourceMap = allResources.get(id.toString());
                if (previousResourceMap != null) {
                    previousResource = HazelcastResourceWrapper.unwrap(previousResourceMap);
                }
                if (previousResource != null && previousResource.getPresence() != null) {
                    hazelcastResource.setPresence(previousResource.getPresence());
                    allResources.set(id.toString(), HazelcastResourceWrapper.wrap(hazelcastResource), 0, TimeUnit.SECONDS);
                } else {
                    if (overwrite) {
                        previousResourceMap = allResources.put(id.toString(), HazelcastResourceWrapper.wrap(hazelcastResource));
                        previousResource = HazelcastResourceWrapper.unwrap(previousResourceMap);
                    } else {
                        previousResourceMap = allResources.putIfAbsent(id.toString(), HazelcastResourceWrapper.wrap(hazelcastResource));
                        previousResource = HazelcastResourceWrapper.unwrap(previousResourceMap);
                    }
                }
            } else { // a Resource with Presence data
                Map<String, Serializable> previousResourceMap = null;
                if (overwrite) {
                    previousResourceMap = allResources.put(id.toString(), HazelcastResourceWrapper.wrap(hazelcastResource));
                } else {
                    previousResourceMap = allResources.putIfAbsent(id.toString(), HazelcastResourceWrapper.wrap(hazelcastResource));
                }
                previousResource = HazelcastResourceWrapper.unwrap(previousResourceMap);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Committing transaction %1$s for resource %2$s", tx, hazelcastResource));
            }
            tx.commit();
        } catch (Throwable t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Rolling back transaction: %1$s for resource: %2$s", tx, hazelcastResource));
            }
            tx.rollback();
            throw new OXException(t);
        }
        return previousResource;
        // return resource

    }

    @Override
    public Presence getPresence(ID id) throws OXException {
        Resource selectedResource = null;

        IDMap<Resource> resources = get(id.toGeneralForm());
        for (Resource candidateResource : resources.values()) {
            Presence candidatePresence = candidateResource.getPresence();
            if (candidatePresence == null || candidatePresence.getPriority() < 0) {
                continue;
            }
            if ((selectedResource == null) || (selectedResource.getTimestamp().compareTo(candidateResource.getTimestamp()) < 0)) {
                selectedResource = candidateResource;
            }
        }

        return selectedResource == null ? null : selectedResource.getPresence();
    }

    /**
     * Try to create a new HazelcastResource for the given ID. One place where this is used is during creation of GroupDispatchers.
     * 
     * @param id The ID used to reach a Resource
     * @return null if the HazelcastResource couldn't be created, otherwise the new Resource
     * @throws OXException
     */
    private HazelcastResource conjureResource(ID id) throws OXException {
        if (!conjure(id)) {
            return null;
        }
        syntheticIDs.add(id);
        HazelcastResource res = new HazelcastResource();
        HazelcastResource meantime = setIfAbsent(id, res);
        if (meantime == null) {
            return res;
        }
        return meantime;
    }

    /*
     * Hazelcast specific setIfAbsent that returns a proper HazelcastResource
     */
    @Override
    public HazelcastResource setIfAbsent(ID id, Resource resource) throws OXException {
        HazelcastResource previousResource = doSet(id, resource, false);
        if (null == previousResource) {
            notifyAdded(id, resource);
        }
        return previousResource;
    }

    /**
     * Get the mapping of general IDs to full IDs e.g. marc.arens@premium <-> ox://marc.arens@premium/random.
     * 
     * @return the map used for mapping general IDs to full IDs.
     * @throws OXException if the HazelcastInstance is missing.
     */
    public MultiMap<String, String> getIDMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(id_map);
    }

    /**
     * Get the mapping of full IDs to the Resource e.g. ox://marc.arens@premuim/random <-> ResourceMap.
     * 
     * @return the map used for mapping full IDs to ResourceMaps.
     * @throws OXException if the map couldn't be fetched from hazelcast
     */
    public IMap<String, Map<String, Serializable>> getResourceMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMap(resource_map);
    }

    /**
     * Creates a new transaction.
     *
     * @return The newly created transaction
     * @throws OXException If an error occurs while creating the transaction
     */
    protected static Transaction newTransaction() throws OXException {
        /*
         * HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance(); return hazelcast.getTransaction();
         */
        return new Transaction() {

            @Override
            public void rollback() throws IllegalStateException {
                /* nothing */
            }

            @Override
            public int getStatus() {
                return 0;
            }

            @Override
            public void commit() throws IllegalStateException {
                /* nothing */
            }

            @Override
            public void begin() throws IllegalStateException {
                /* nothing */
            }
        };
    }

    /**
     * Starts the timer that refreshes synthetic resources
     */
    protected void startRefreshTimer() {
        Services.getService(TimerService.class).scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                for (ID syntheticID : syntheticIDs) {
                    syntheticID.trigger(ID.Events.REFRESH, this);
                }
            }

        }, 1, 15, TimeUnit.MINUTES);
    }

    /**
     * Remove the entries we track for this id from the IDMapping and the ResourceMapping
     */
    private final IDEventHandler CLEAN_UP = new IDEventHandler() {

        @Override
        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
            if (source != HazelcastResourceDirectory.this) {
                try {
                    IDMap<Resource> removed = removeWithoutDisposeEvent(id);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Removed: {}", removed.entrySet());
                    }
                } catch (OXException e) {
                    LOG.error("", e);
                }
            }
        }

    };

    /**
     * Touch the infos we track for a given ID so they don't get automatically removed by Hazelcast's eviction policy as long as it's in
     * active use.
     */
    private final IDEventHandler TOUCH_ID = new IDEventHandler() {

        @Override
        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
            try {
                /*
                 * This performs a set on map entries to prevent eviction
                 */
                getIDMapping().get(id.toGeneralForm().toString());
                Map<String, Serializable> resourceWrap = getResourceMapping().get(id.toString());
                if (resourceWrap == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unable to refresh id, might have been removed in the meantime: {}", id);
                    }
                    syntheticIDs.remove(id);
                } else {
                    resourceWrap.put(HazelcastResourceWrapper.eviction_timestamp, System.currentTimeMillis());
                    Map<String, Serializable> put = getResourceMapping().put(id.toString(), resourceWrap);
                    if (put == null) {
                        LOG.warn("There was no previous entry associated with id: {}when refreshing the directory entry via write.", id);
                    } else {
                        LOG.debug("Refreshed id: {} with resource: {}", id, resourceWrap);
                    }
                }
            } catch (OXException e) {
                LOG.error(e.getMessage());
            }
        }

    };

}
