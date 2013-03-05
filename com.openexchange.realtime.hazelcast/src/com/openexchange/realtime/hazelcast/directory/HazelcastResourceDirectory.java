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
import java.util.Set;
import org.apache.commons.logging.Log;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.Transaction;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.directory.*;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link HazelcastResourceDirectory}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class HazelcastResourceDirectory extends DefaultResourceDirectory {
    
    private static final Log LOG = LogFactory.getLog(HazelcastResourceDirectory.class);

    private static final String ID_MAP = "rtIDMapping-0";

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
        } catch (Exception e) {
            tx.rollback();
            throw new OXException(e);
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
            } catch (Exception e) {
                tx.rollback();
                throw new OXException(e);
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
            } catch (Exception e) {
                tx.rollback();
                throw new OXException(e);
            }
        }

        return removedResources;
    }

    @Override
    protected Resource doSet(ID id, Resource data) throws OXException {
        data.setRoutingInfo(getLocalMember());
        data.setTimestamp(new Date());

        MultiMap<ID, ID> idMapping = getIDMapping();
        IMap<ID, Resource> allResources = getResourceMap();
        Resource previous = null;
        Transaction tx = newTransaction();
        tx.begin();
        try {
            idMapping.put(id.toGeneralForm(), id);
            previous = allResources.put(id, data);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new OXException(e);
        }

        return previous;
    }
    
    private Resource conjureResource(ID id) throws OXException {
        if (conjure(id)) {
            Resource res = new DefaultResource();
            set(id, res);
            id.on("dispose", CLEAN_UP);
            return res;
        } else {
            return null;
        }
    }

    protected static MultiMap<ID, ID> getIDMapping() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMultiMap(ID_MAP);
    }

    protected static IMap<ID, Resource> getResourceMap() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getMap(RESOURCE_MAP);
    }

    protected static Transaction newTransaction() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getTransaction();
    }

    protected static Member getLocalMember() throws OXException {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        return hazelcast.getCluster().getLocalMember();
    }
    
    private IDEventHandler CLEAN_UP = new IDEventHandler() {
        
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
