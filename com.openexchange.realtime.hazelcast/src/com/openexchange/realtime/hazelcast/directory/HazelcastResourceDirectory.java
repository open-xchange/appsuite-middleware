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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.Transaction;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.directory.*;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link HazelcastResourceDirectory} - Keeps mappings of general {@link ID}s to full {@link ID}s and full {@link ID}s to {@link Resource}.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class HazelcastResourceDirectory extends DefaultResourceDirectory {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HazelcastResourceDirectory.class);

    /** Mapping of general IDs to full IDs e.q marc.arens@premium <-> ox://marc.arens@premuim/random. */
    private static final String ID_MAP = "rtIDMapping-0";

    /** Mapping of full IDs to the Resource e.g. ox://marc.arens@premuim/random <-> Resource */
    private static final String RESOURCE_MAP = "rtResourceDirectory-0";

    @Override
    public IDMap<Resource> get(ID id) throws OXException {
        IDMap<Resource> foundResources = new IDMap<Resource>();
        if (id.isGeneralForm()) {
            MultiMap<ID, ID> idMapping = getIDMapping();
            Collection<ID> concreteIds = idMapping.get(id);
            if (concreteIds != null && !concreteIds.isEmpty()) {
                IMap<ID, Resource> allResources = getResourceMap();
                Map<ID, Resource> resources = allResources.getAll(new HashSet<ID>(concreteIds));
                if (resources != null) {
                    for (Entry<ID, Resource> entry : resources.entrySet()) {
                        foundResources.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        } else {
            IMap<ID, Resource> allResources = getResourceMap();
            Resource resource = allResources.get(id);
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
            IMap<ID, Resource> allResources = getResourceMap();
            Map<ID, Resource> resources = allResources.getAll(resourceIds);
            if (resources != null) {
                for (Entry<ID, Resource> entry : resources.entrySet()) {
                    foundResources.put(entry.getKey(), entry.getValue());
                }
                resourceIds.removeAll(resources.keySet());
                for (ID id : resourceIds) {
                    Resource resource = conjureResource(id);
                    if (resource != null) {
                        foundResources.put(id, resource);
                    }
                }
            }
        }

        if (!generalIds.isEmpty()) {
            for (ID id : generalIds) {
                MultiMap<ID, ID> idMapping = getIDMapping();
                Collection<ID> concreteIds = idMapping.get(id);
                if (concreteIds != null && !concreteIds.isEmpty()) {
                    IMap<ID, Resource> allResources = getResourceMap();
                    Map<ID, Resource> resources = allResources.getAll(new HashSet<ID>(concreteIds));
                    if (resources != null) {
                        for (Entry<ID, Resource> entry : resources.entrySet()) {
                            foundResources.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        }

        return foundResources;
    }

    @Override
    protected IDMap<Resource> doRemove(Collection<ID> ids) throws OXException {
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

        MultiMap<ID, ID> idMapping = getIDMapping();
        IMap<ID, Resource> allResources = getResourceMap();
        Transaction tx = newTransaction();
        tx.begin();
        try {
            if (!resourceIds.isEmpty()) {
                for (ID id : resourceIds) {
                    idMapping.remove(id.toGeneralForm(), id);
                    Resource resource = allResources.remove(id);
                    if (resource != null) {
                        removedResources.put(id, resource);
                    }
                }
            }

            if (!generalIds.isEmpty()) {
                for (ID id : generalIds) {
                    Collection<ID> toRemove = idMapping.remove(id);
                    for (ID concreteId : toRemove) {
                        Resource resource = allResources.remove(concreteId);
                        if (resource != null) {
                            removedResources.put(concreteId, resource);
                        }
                    }
                }
            }

            tx.commit();
        } catch (Throwable t) {
            tx.rollback();
            throw new OXException(t);
        }

        return removedResources;
    }

    @Override
    protected IDMap<Resource> doRemove(ID id) throws OXException {
        IDMap<Resource> removedResources = new IDMap<Resource>();
        MultiMap<ID, ID> idMapping = getIDMapping();
        IMap<ID, Resource> allResources = getResourceMap();
        if (id.isGeneralForm()) {
            Transaction tx = newTransaction();
            tx.begin();
            try {
                Collection<ID> toRemove = idMapping.remove(id);
                if (toRemove != null) {
                    for (ID concreteId : toRemove) {
                        Resource resource = allResources.remove(concreteId);
                        if (resource != null) {
                            removedResources.put(concreteId, resource);
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
                idMapping.remove(id.toGeneralForm(), id);
                Resource resource = allResources.remove(id);
                if (resource != null) {
                    removedResources.put(id, resource);
                }

                tx.commit();
            } catch (Throwable t) {
                tx.rollback();
                throw new OXException(t);
            }
        }

        return removedResources;
    }

    @Override
    protected Resource doSet(ID id, Resource resource, boolean overwrite) throws OXException {
        resource.setRoutingInfo(getLocalMember());
        resource.setTimestamp(new Date());

        MultiMap<ID, ID> idMapping = getIDMapping();
        IMap<ID, Resource> allResources = getResourceMap();
        Resource previousResource = null;
        Transaction tx = newTransaction();
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format(
                "Starting transaction: %1$s for resource: %2$s",
                tx, resource));
        }
        tx.begin();
        try {
            idMapping.put(id.toGeneralForm(), id);

            // don't overwrite exisiting Presence Data
            if (resource.getPresence() == null) { // a DefaultResource / idle reconnect
                previousResource = allResources.get(id);
                if (previousResource != null && previousResource.getPresence() != null) {
                    resource.setPresence(previousResource.getPresence());
                    allResources.set(id, resource, 0, TimeUnit.SECONDS);
                } else {
                    if (overwrite) {
                        previousResource = allResources.put(id, resource);                        
                    } else {
                        previousResource = allResources.putIfAbsent(id, resource);                        
                    }
                }
            } else { // a Resource with Presence data
                if (overwrite) {
                    previousResource = allResources.put(id, resource);
                } else {
                    previousResource = allResources.putIfAbsent(id, resource);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                    "Committing transaction %1$s for resource %2$s",
                    tx, resource));
            }
            tx.commit();
        } catch (Throwable t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                    "Rolling back transaction: %1$s for resource: %2$s",
                    tx, resource));
            }
            tx.rollback();
            throw new OXException(t);
        }
        return previousResource;
    }

    @Override
    public Presence getPresence(ID id) throws OXException {
        Resource selectedResource = null;
        
        IDMap<Resource> resources = get(id.toGeneralForm());
        for (Resource candidateResource : resources.values()) {
            Presence candidatePresence = candidateResource.getPresence();
            if(candidatePresence == null || candidatePresence.getPriority() < 0) {
                continue;
            }
            if (selectedResource == null) {
                 selectedResource = candidateResource;
                 continue;
            } else {
                int comparisonResult = selectedResource.getTimestamp().compareTo(candidateResource.getTimestamp());
                if (comparisonResult < 0) {
                    selectedResource = candidateResource;
                }
            }
        }
        
        if(selectedResource == null) {
            return null;
        } else {
            return selectedResource.getPresence();
        }
        
    }
        
    private Resource conjureResource(ID id) throws OXException {
        if (conjure(id)) {
            Resource res = new DefaultResource();
            Resource meantime = setIfAbsent(id, res);
            id.on("dispose", CLEAN_UP);
            return (meantime == null) ? res : meantime;
        } else {
            return null;
        }
    }

    /**
     * Get the mapping of general IDs to full IDs e.g. marc.arens@premium <-> ox://marc.arens@premuim/random.
     * 
     * @return the map used for mapping general IDs to full IDs.
     * @throws OXException
     */
    protected static MultiMap<ID, ID> getIDMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(ID_MAP);
    }

    /**
     * Get the mapping of full IDs to the Resource e.g. ox://marc.arens@premuim/random <-> Resource.
     * 
     * @return the map used for mapping general IDs to full IDs.
     * @throws OXException
     */
    protected static IMap<ID, Resource> getResourceMap() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMap(RESOURCE_MAP);
    }

    protected static Transaction newTransaction() throws OXException {
        /*HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getTransaction();*/
        return new Transaction() {
            
            @Override
            public void rollback() throws IllegalStateException {
                
            }
            
            @Override
            public int getStatus() {
                return 0;
            }
            
            @Override
            public void commit() throws IllegalStateException {
                
            }
            
            @Override
            public void begin() throws IllegalStateException {
                
            }
        };
    }

    protected static Member getLocalMember() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getCluster().getLocalMember();
    }
    
    private final IDEventHandler CLEAN_UP = new IDEventHandler() {
        
        @Override
        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
            if (source != HazelcastResourceDirectory.this) {
                try {
                    removeWithoutDisposeEvent(id);
                } catch (OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        
        
    };

}
