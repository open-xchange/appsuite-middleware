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

package com.openexchange.realtime.hazelcast.directory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Optional;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.hazelcast.query.Predicate;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementAware;
import com.openexchange.management.ManagementObject;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.realtime.cleanup.AbstractRealtimeJanitor;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.DefaultResourceDirectory;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.RoutingInfo;
import com.openexchange.realtime.group.DistributedGroupManager;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl;
import com.openexchange.realtime.hazelcast.management.HazelcastResourceDirectoryMBean;
import com.openexchange.realtime.hazelcast.management.HazelcastResourceDirectoryManagement;
import com.openexchange.realtime.hazelcast.osgi.Services;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableMemberPredicate;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableResource;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.timer.TimerService;

/**
 * {@link HazelcastResourceDirectory} - Keeps mappings of general {@link ID}s to full {@link ID}s and full {@link ID}s to {@link Resource}s.
 * New {@link ID}s and {@link DefaultResource}s that are added to this directory are automatically converted to {@link PortableID}s and
 * {@link PortableResource}s and extended with {@link RoutingInfo}s based on the local Hazelcast {@link Member}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HazelcastResourceDirectory extends DefaultResourceDirectory implements ManagementAware<HazelcastResourceDirectoryMBean>, RealtimeJanitor {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HazelcastResourceDirectory.class);

    private static final Object PRESENT = new Object();

    /** Mapping of general IDs to full IDs e.g marc.arens@premium <-> ox://marc.arens@premium/random. */
    private final String id_map;

    /** Mapping of full IDs to the Resource e.g. ox://marc.arens@premium/random <-> ResourceMap */
    private final String resource_map;

    private final HazelcastResourceDirectoryManagement managementObject;

    /** The reference to associated {@code DistributedGroupManager} instance */
    final AtomicReference<DistributedGroupManager> distributedGroupManagerRef;

    /**
     * Initializes a new {@link HazelcastResourceDirectory}.
     *
     * @param id_map The name of the mapping of general IDs to full IDs e.g marc.arens@premium <-> ox://marc.arens@premium/random
     * @param resource_map The name of the mapping of full IDs to the Resource e.g. ox://marc.arens@premium/random <-> ResourceMap
     * @throws OXException
     */
    public HazelcastResourceDirectory(String id_map, String resource_map) throws OXException {
        super();
        final AtomicReference<DistributedGroupManager> distributedGroupManagerRef = new AtomicReference<DistributedGroupManager>();
        this.distributedGroupManagerRef = distributedGroupManagerRef;
        this.id_map = id_map;
        this.resource_map = resource_map;
        this.managementObject = new HazelcastResourceDirectoryManagement(this);
        addResourceMappingEntryListener(new ResourceMappingEntryAdapter() {

            @Override
            public void entryEvicted(EntryEvent<PortableID, PortableResource> event) {
                PortableID id = event.getKey();
                Object source = event.getSource();
                Member member = event.getMember();

                DistributedGroupManager distributedGroupManager = distributedGroupManagerRef.get();
                if (null != distributedGroupManager) {
                    try {
                        if (null != distributedGroupManager.getMembers(id)) {
                            // Detected preliminary eviction attempt
                            Throwable trace = new Throwable("tracked thread");
                            LOG.warn("Source {} on Member: {} fired eviction event for '{}'. ID is still in use by associated {}.", source, member, id, DistributedGroupManager.class.getSimpleName(), trace);
                        }
                    } catch (Exception e) {
                        // Ignore...
                    }
                }

                try {
                    if (getIDMapping().remove(id.toGeneralForm(), id)) {
                        LOG.debug("Source {} on Member: {} fired eviction event. Removing mapping for '{}' due to eviction of associated resource.", source, member, id);
                    }
                } catch (OXException e) {
                    LOG.warn("Could not handle eviction for id '{}'", id, e);
                }
            }

            @Override
            public void entryRemoved(EntryEvent<PortableID, PortableResource> event) {
                PortableID id = event.getKey();
                Object source = event.getSource();
                Member member = event.getMember();
                Throwable trace = new Throwable("tracked thread");
                LOG.debug("Source {} on Member: {} fired removal event for '{}'.", source, member, id, trace);
            }
        }, false);
        startRefreshTimer();
    }

    /**
     * Applies the specified {@link DistributedGroupManager} instance to this directory
     *
     * @param distributedGroupManager The {@code DistributedGroupManager} instance to apply
     * @return The cleaner registration identifier
     * @throws OXException If {@link DistributedGroupManager} instance cannot be applied
     */
    public String applyDistributedGroupManager(DistributedGroupManagerImpl distributedGroupManager) throws OXException {
        this.distributedGroupManagerRef.set(distributedGroupManager);
        return addResourceMappingEntryListener(distributedGroupManager.getCleaner(), true);
    }

    @Override
    public ManagementObject<HazelcastResourceDirectoryMBean> getManagementObject() {
        return managementObject;
    }

    @Override
    public IDMap<Resource> get(ID id) throws OXException {
        IDMap<Resource> foundResources = new IDMap<Resource>();
        PortableID currentPortableId = new PortableID(id);

        if (currentPortableId.isGeneralForm()) {
            MultiMap<PortableID,PortableID> idMapping = getIDMapping();
            Collection<PortableID> concreteIds = idMapping.get(currentPortableId);
            if (concreteIds != null && !concreteIds.isEmpty()) {
                IMap<PortableID,PortableResource> allResources = getResourceMapping();
                Map<PortableID, PortableResource> resources = allResources.getAll(new HashSet<PortableID>(concreteIds));
                if (resources != null) {
                    for (Entry<PortableID, PortableResource> entry : resources.entrySet()) {
                        PortableID foundId = entry.getKey();
                        PortableResource foundResource = entry.getValue();
                        foundResources.put(foundId, foundResource);
                    }
                }
            }
        } else {
            IMap<PortableID,PortableResource> allResources = getResourceMapping();
            PortableResource portableResource = allResources.get(currentPortableId);
            if (portableResource != null) {
                foundResources.put(currentPortableId, portableResource);
            } else {
                // no matching resource for requested id, can we create one on demand?
                portableResource = conjureResource(currentPortableId);
                if (portableResource != null) {
                    foundResources.put(currentPortableId, portableResource);
                }
            }
        }

        return foundResources;
    }

    @Override
    public IDMap<Resource> get(Collection<ID> ids) throws OXException {
        IDMap<Resource> foundResources = new IDMap<Resource>();
        Set<PortableID> generalIds = new HashSet<PortableID>();
        Set<PortableID> conreteIds = new HashSet<PortableID>();
        for (ID id : ids) {
            if (id.isGeneralForm()) {
                generalIds.add(new PortableID(id));
            } else {
                conreteIds.add(new PortableID(id));
            }
        }

        if (!conreteIds.isEmpty()) {
            IMap<PortableID,PortableResource> allResources = getResourceMapping();
            Map<PortableID, PortableResource> matchingPortableResources = allResources.getAll(conreteIds);
            if (matchingPortableResources != null) {
                for (Entry<PortableID, PortableResource> entry : matchingPortableResources.entrySet()) {
                    ID foundID = entry.getKey();
                    PortableResource foundResource = entry.getValue();
                    foundResources.put(foundID, foundResource);
                }
                // Remove all found Resources so we can try to conjure the rest
                conreteIds.removeAll(foundResources.keySet());
                for (PortableID id : conreteIds) {
                    PortableResource resource = conjureResource(id);
                    if (resource != null) {
                        foundResources.put(id, resource);
                    }
                }
            }
        }

        if (!generalIds.isEmpty()) {
            for (PortableID id : generalIds) {
                MultiMap<PortableID,PortableID> idMapping = getIDMapping();
                Collection<PortableID> foundConcreteIds = idMapping.get(id);
                if (foundConcreteIds != null && !foundConcreteIds.isEmpty()) {
                    IMap<PortableID,PortableResource> resourceMapping = getResourceMapping();
                    Map<PortableID, PortableResource> foundResourcesForConcreteIDs = resourceMapping.getAll(new HashSet<PortableID>(foundConcreteIds));
                    if (foundResourcesForConcreteIDs != null) {
                        for (Entry<PortableID, PortableResource> entry : foundResourcesForConcreteIDs.entrySet()) {
                            PortableID foundID = entry.getKey();
                            PortableResource foundResource = entry.getValue();
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
        LOG.debug("Removing IDs from HazelcastResourceDirectory: {}", ids);
        IDMap<Resource> removedResources = new IDMap<Resource>();
        Set<PortableID> generalIds = new HashSet<PortableID>();
        Set<PortableID> concreteIds = new HashSet<PortableID>();
        for (ID id : ids) {
            if (id.isGeneralForm()) {
                generalIds.add(new PortableID(id));
            } else {
                concreteIds.add(new PortableID(id));
            }
        }

        try {
            MultiMap<PortableID,PortableID> idMapping = getIDMapping();
            IMap<PortableID,PortableResource> allResources = getResourceMapping();
            if (!concreteIds.isEmpty()) {
                for (PortableID id : concreteIds) {
                    idMapping.remove(id.toGeneralForm(), id);
                    PortableResource removedResource = allResources.remove(id);
                    if(removedResource != null) {
                        removedResources.put(id, removedResource);
                    }
                }
            }

            if (!generalIds.isEmpty()) {
                for (PortableID generalId : generalIds) {
                    Collection<PortableID> foundConcreteIds = idMapping.remove(generalId);
                    for (PortableID concreteId : foundConcreteIds) {
                        PortableResource removedResource = allResources.remove(concreteId);
                        if(removedResource != null) {
                            removedResources.put(concreteId, removedResource);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            throw new OXException(t);
        }
        LOG.debug("Removed Resource(s) from HazelcastResourceDirectory: {}", removedResources);
        return removedResources;
    }

    @Override
    protected IDMap<Resource> doRemove(ID id) throws OXException {
        LOG.debug("Removing ID from HazelcastResourceDirectory: {}", id);
        PortableID currentPortableId = new PortableID(id);
        IDMap<Resource> removedResources = new IDMap<Resource>();
        try {
            MultiMap<PortableID,PortableID> idMapping = getIDMapping();
            IMap<PortableID,PortableResource> allResources = getResourceMapping();
            if (currentPortableId.isGeneralForm()) {
                Collection<PortableID> removedConcreteIds = idMapping.remove(currentPortableId);
                if (removedConcreteIds != null) {
                    for (PortableID concreteId : removedConcreteIds) {
                        PortableResource removedResource = allResources.remove(concreteId);
                        if (removedResource != null) {
                            removedResources.put(concreteId, removedResource);
                        }
                    }
                }
            } else {
                idMapping.remove(currentPortableId.toGeneralForm(), currentPortableId);
                PortableResource removedResource = allResources.remove(currentPortableId);
                if (removedResource != null) {
                    removedResources.put(currentPortableId, removedResource);
                }
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            throw new OXException(t);
        }
        LOG.debug("Removed Resource(s) from HazelcastResourceDirectory: {}", removedResources);
        return removedResources;
    }

    /*
     * Automatically converts incoming Resources to HazelcastResources (which extends them with hazelcast specific routing infos) and
     * infcoming IDs to PortableIDs before adding them to the hazelcast data structures.
     */
    @Override
    protected PortableResource doSet(ID id, Resource resource, boolean overwrite) throws OXException {
        PortableResource currentPortableResource = new PortableResource(resource, HazelcastAccess.getLocalMember());
        PortableID currentPortableID = new PortableID(id);

        PortableResource previousPortableResource = null;
        try {
            MultiMap<PortableID,PortableID> idMapping = getIDMapping();
            IMap<PortableID,PortableResource> resourceMapping = getResourceMapping();
            idMapping.put(currentPortableID.toGeneralForm(), currentPortableID);

            // don't overwrite exisiting Presence Data
            if (currentPortableResource.getPresence() == null) {
                // current resource doesn't provide presence infos, might be a DefaultResource / idle reconnect
                previousPortableResource = resourceMapping.get(currentPortableID);
                if(previousPortableResource != null && previousPortableResource.getPresence() != null) {
                    // but the previous resource provides a presence
                    currentPortableResource.setPresence(previousPortableResource.getPresence());
                    resourceMapping.put(currentPortableID, currentPortableResource);
                } else {
                    // neither current nor previous resource provide presence infos
                    if(overwrite) {
                        previousPortableResource = resourceMapping.put(currentPortableID, currentPortableResource);
                    } else {
                        previousPortableResource = resourceMapping.putIfAbsent(currentPortableID, currentPortableResource);
                    }
                }
            } else {
                // current resource provides Presence data
                if(overwrite) {
                    previousPortableResource = resourceMapping.put(currentPortableID, currentPortableResource);
                } else {
                    previousPortableResource = resourceMapping.putIfAbsent(currentPortableID, currentPortableResource);
                }
            }
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            throw new OXException(t);
        }
        return previousPortableResource;
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
     * Try to create a new Resource for the given ID and persist it in the ResourceDirectory. One place where this is used is during
     * creation of GroupDispatchers.
     *
     * @param id The ID used to reach a Resource
     * @return null if the HazelcastResource couldn't be created, otherwise the new Resource
     * @throws OXException
     */
    private PortableResource conjureResource(PortableID id) throws OXException {
        if (!conjure(id)) {
            return null;
        }

        PortableResource res = new PortableResource(HazelcastAccess.getLocalMember());
        PortableResource meantime = setIfAbsent(id, res);
        if (meantime == null) {
            return res;
        }
        return meantime;
    }

    /*
     * Hazelcast specific setIfAbsent that returns a proper PortableResource
     */
    @Override
    public PortableResource setIfAbsent(ID id, Resource resource) throws OXException {
        PortableResource previousResource = doSet(id, resource, false);
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
    public MultiMap<PortableID, PortableID> getIDMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        try {
            return hazelcast.getMultiMap(id_map);
        } catch (HazelcastInstanceNotActiveException e) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(e, HazelcastInstance.class.getName());
        }
    }

    /**
     * Get the mapping of full IDs to the Resource e.g. ox://marc.arens@premium/random <-> Resource. The resource includes the
     * {@link RoutingInfo} needed to address clients identified by the {@link ID}
     *
     * @return the map used for mapping full IDs to ResourceMaps.
     * @throws OXException if the map couldn't be fetched from hazelcast
     */
    public IMap<PortableID, PortableResource> getResourceMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        try {
            return hazelcast.getMap(resource_map);
        } catch (HazelcastInstanceNotActiveException e) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(e, HazelcastInstance.class.getName());
        }
    }

    /**
     * Starts the timer that refreshes synthetic resources
     */
    protected void startRefreshTimer() {
        TimerService timerService = Services.optService(TimerService.class);
        if (null != timerService) {
            timerService.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    try {
                        DistributedGroupManager distributedGroupManager = distributedGroupManagerRef.get();
                        MultiMap<PortableID, PortableID> idMapping = getIDMapping();
                        IMap<PortableID, PortableResource> resourceMapping = getResourceMapping();
                        for (PortableID portableID : new LinkedHashSet<PortableID>(resourceMapping.localKeySet())) { // Copy local key set
                            if (null == distributedGroupManager) {
                                // No chance to check if active; just do it
                                touch(idMapping, resourceMapping, portableID);
                            } else {
                                if (null != distributedGroupManager.getMembers(portableID)) {
                                    // ID is still in use
                                    touch(idMapping, resourceMapping, portableID);
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error while touching IDs.", e);
                    }
                }

                private void touch(MultiMap<PortableID, PortableID> idMapping, IMap<PortableID, PortableResource> resourceMapping, PortableID portableID) {
                    Set<PortableID> idSet = new HashSet<PortableID>();
                    idSet.add(portableID);
                    Map<PortableID, PortableResource> portableResource = resourceMapping.getAll(idSet);
                    if ((portableResource == null) || (portableResource.isEmpty())) {
                        LOG.debug("Unable to touch ID; might have been removed in the meantime: {}", portableID);
                    } else {
                        LOG.debug("Touched ID: {}", portableID);
                    }
                }

            }, 1, 5, TimeUnit.MINUTES);
        }
    }

    @Override
    public void cleanupForId(ID id) {
        LOG.debug("Cleanup for ID: {}", id);
        try {
            IDMap<Resource> removed = remove(id);
            LOG.debug("Removed: {}", removed.entrySet());
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Add a {@link ResourceMappingEntryListener} to this ResourceDirectory.
     * @param entryListener the {@link ResourceMappingEntryListener} to add
     * @param includeValue should the value be included when informing the listener
     * @return the registration id that can be used to remove a previously added listener
     * @throws OXException
     */
    public String addResourceMappingEntryListener(ResourceMappingEntryListener entryListener, boolean includeValue) throws OXException {
        Optional<Predicate<PortableID, PortableResource>> predicate = entryListener.getPredicate();
        if (predicate.isPresent()) {
            return getResourceMapping().addEntryListener(entryListener, predicate.get(), includeValue);
        } else {
            return getResourceMapping().addEntryListener(entryListener, includeValue);
        }
    }

    /**
     * Remove a {@link ResourceMappingEntryListener} from this ResourceDirectory.
     * @param registrationId the registration id gained while adding the listener
     * @return true if removal of the listener succeeded, else false
     * @throws OXException
     */
    public boolean removeResourceMappingEntryListener(String registrationId) throws OXException {
        return getResourceMapping().removeEntryListener(registrationId);
    }

    /**
     * Find all Resources in this directory that are located on a given member node.
     *
     * @param member The cluster member
     * @return all Resources in this directory that are located on the given member node.
     * @throws OXException
     */
    public IDMap<Resource> getResourcesOfMember(Member member)throws OXException {
        IMap<PortableID, PortableResource> allResources = getResourceMapping();
        PortableMemberPredicate memberPredicate = new PortableMemberPredicate(member);
        Set<Entry<PortableID, PortableResource>> matchingResources = allResources.entrySet(memberPredicate);

        IDMap<Resource> foundIds = new IDMap<Resource>();
        Iterator<Entry<PortableID, PortableResource>> iterator = matchingResources.iterator();
        while(iterator.hasNext()) {
            try {
                Entry<PortableID, PortableResource> next = iterator.next();
                foundIds.put(next.getKey(), next.getValue());
            } catch (Exception e) {
                LOG.error("Couldn't add resource that was found for member {}", member, e);
            }
        }
        return foundIds;
    }

    @Override
    public Dictionary<String, Object> getServiceProperties() {
        return AbstractRealtimeJanitor.NO_PROPERTIES;
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private class ValidatingResourceMappingEntryListener implements ResourceMappingEntryListener {

        private final ResourceMappingEntryListener delegate;
        private final boolean reAdd;
        private final boolean delegateIfInvalid;

        ValidatingResourceMappingEntryListener(ResourceMappingEntryListener delegate, boolean reAdd, boolean delegateIfInvalid) {
            super();
            this.delegate = delegate;
            this.reAdd = reAdd;
            this.delegateIfInvalid = delegateIfInvalid;
        }

        @Override
        public void entryMerged(EntryEvent<PortableID, PortableResource> event) {
            delegate.entryMerged(event);
        }

        @Override
        public void mapCleared(MapEvent event) {
            delegate.mapCleared(event);
        }

        @Override
        public void mapEvicted(MapEvent event) {
            delegate.mapEvicted(event);
        }

        @Override
        public void entryAdded(EntryEvent<PortableID, PortableResource> event) {
            delegate.entryAdded(event);
        }

        @Override
        public void entryUpdated(EntryEvent<PortableID, PortableResource> event) {
            delegate.entryUpdated(event);
        }

        @Override
        public void entryRemoved(EntryEvent<PortableID, PortableResource> event) {
            delegate.entryRemoved(event);
        }

        @Override
        public void entryEvicted(EntryEvent<PortableID, PortableResource> event) {
            // Check for preliminary eviction attempt
            DistributedGroupManager distributedGroupManager = distributedGroupManagerRef.get();
            if (null != distributedGroupManager) {
                try {
                    PortableID id = event.getKey();
                    if (null != distributedGroupManager.getMembers(id)) {
                        // Detected preliminary eviction attempt
                        Object source = event.getSource();
                        Member member = event.getMember();
                        LOG.warn("Source {} on Member: {} fired preliminary eviction event for '{}'. ID is still in use by associated {}", source, member, id, DistributedGroupManager.class.getSimpleName());
                    }

                    if (reAdd) {
                        TimerService timerService = Services.optService(TimerService.class);
                        if (null != timerService) {
                            timerService.schedule(new EntryAdder(event), 250);
                        }
                    }

                    if (false == delegateIfInvalid) {
                        return;
                    }
                } catch (Exception e) {
                    // Ignore...
                }
            }

            // Delegate event
            delegate.entryEvicted(event);
        }

        @Override
        public Optional<Predicate<PortableID, PortableResource>> getPredicate() {
            return delegate.getPredicate();
        }
    }

    private class EntryAdder implements Runnable {

        private final EntryEvent<PortableID, PortableResource> event;

        EntryAdder(EntryEvent<PortableID, PortableResource> event) {
            super();
            this.event = event;
        }

        @Override
        public void run() {
            try {
                IMap<PortableID, PortableResource> resourceMapping = getResourceMapping();
                resourceMapping.put(event.getKey(), event.getValue());
            } catch (Exception e) {
                LOG.warn("Failed to add element", e);
            }
        }

    }

}
