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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.util.IDMap;


/**
 * {@link HazelcastResourceDirectoryTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class HazelcastResourceDirectoryTest extends HazelcastResourceDirectory {
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        HazelcastInstance hazelcast = Hazelcast.getDefaultInstance();
        HazelcastAccess.setHazelcastInstance(hazelcast);
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        hazelcast.getLifecycleService().shutdown();
        HazelcastAccess.setHazelcastInstance(null);
    }
    
    @After
    public void after() throws Exception {
        getIDMapping().clear();
        getResourceMap().clear();
    }
    
    @Test
    public void testSetNewResource() throws Exception {
        ID concreteId = generateId();
        ID generalId = concreteId.toGeneralForm();
        Resource resource = generateResource();
        
        Assert.assertEquals("Wrong return value", null, set(concreteId, resource));
        IDMap<Resource> resources = get(generalId);
        Assert.assertEquals("Wrong size", 1, resources.size());
        ID reloadedId = resources.keySet().iterator().next();
        Assert.assertEquals("Wrong id", reloadedId, concreteId);
    }
    
    @Test
    public void testUpdateResource() throws Exception {
        ID concreteId = generateId();
        ID generalId = concreteId.toGeneralForm();
        Resource resource = generateResource();
        
        set(concreteId, resource);
        Resource reloadedResource = get(generalId).entrySet().iterator().next().getValue();
        Assert.assertEquals("Wrong presence state", resource.getPresenceState(), reloadedResource.getPresenceState());
        
        DefaultResource changedResource = new DefaultResource(PresenceState.OFFLINE);
        Resource previous = set(concreteId, changedResource);
        Assert.assertEquals("Wrong resource", resource, previous);
        
        reloadedResource = get(generalId).entrySet().iterator().next().getValue();
        Assert.assertEquals("Wrong resource", changedResource, reloadedResource);
    }
    
    @Test
    public void testSetAndRemoveMultiple() throws Exception {
        ID id1 = generateId();
        ID id2 = generateId();
        ID id3 = generateId();
        Resource r1 = generateResource();
        Resource r2 = generateResource();
        Resource r3 = generateResource();
        
        set(id1, r1);
        set(id2, r2);
        set(id3, r3);
        
        IDMap<Resource> idMap1 = get(id1);
        Assert.assertEquals("Wrong size", 1, idMap1.size());
        Assert.assertEquals("Wrong resource", r1, idMap1.entrySet().iterator().next().getValue());
        
        IDMap<Resource> idMap2 = get(id2);
        Assert.assertEquals("Wrong size", 1, idMap2.size());
        Assert.assertEquals("Wrong resource", r2, idMap2.entrySet().iterator().next().getValue());
        
        IDMap<Resource> idMap3 = get(id3);
        Assert.assertEquals("Wrong size", 1, idMap3.size());
        Assert.assertEquals("Wrong resource", r3, idMap3.entrySet().iterator().next().getValue());
        
        IDMap<Resource> all = get(id1.toGeneralForm());
        Assert.assertEquals("Wrong size", 3, all.size());
        Assert.assertTrue("Result did not contain id", all.containsKey(id1));
        Assert.assertTrue("Result did not contain id", all.containsKey(id2));
        Assert.assertTrue("Result did not contain id", all.containsKey(id3));
        
        IDMap<Resource> remove1 = remove(id1);
        Assert.assertEquals("Wrong size", 1, remove1.size());
        Assert.assertEquals("Wrong resource", r1, remove1.entrySet().iterator().next().getValue());
        
        Set<ID> toRemove = new HashSet<ID>();
        toRemove.add(id2);
        toRemove.add(id3);
        
        IDMap<Resource> remove2 = remove(toRemove);
        Assert.assertEquals("Wrong size", 2, remove2.size());
        
        MultiMap<ID,ID> idMapping = getIDMapping();
        IMap<ID, Resource> resources = getResourceMap();
        Assert.assertEquals("Id mapping not empty", 0, idMapping.size());
        Assert.assertEquals("Resources not empty", 0, resources.size());
    }
    
    @Test
    public void testSetAndGetAndRemoveMultipleWithMixedIdTypes() throws Exception {
        ID id1 = generateId();
        ID id2 = generateId();
        ID id3 = generateId();
        ID id4 = new ID("ox", "another.user", "context", UUID.randomUUID().toString());
        ID id5 = new ID("ox", "even.another", "context", UUID.randomUUID().toString());
        Resource r1 = generateResource();
        Resource r2 = generateResource();
        Resource r3 = generateResource();
        Resource r4 = generateResource();
        Resource r5 = generateResource();
        
        set(id1, r1);
        set(id2, r2);
        set(id3, r3);
        set(id4, r4);
        set(id5, r5);
        
        Set<ID> toGet = new HashSet<ID>();
        toGet.add(id1.toGeneralForm());
        toGet.add(id4);
        IDMap<Resource> idMap = get(toGet);
        Assert.assertEquals("Wrong size", 4, idMap.size());
        Assert.assertTrue("Result did not contain id", idMap.containsKey(id1));
        Assert.assertTrue("Result did not contain id", idMap.containsKey(id2));
        Assert.assertTrue("Result did not contain id", idMap.containsKey(id3));
        Assert.assertTrue("Result did not contain id", idMap.containsKey(id4));
        
        Set<ID> toRemove = new HashSet<ID>();
        toRemove.add(id1.toGeneralForm());
        toRemove.add(id5);
        IDMap<Resource> removed = remove(toRemove);
        Assert.assertEquals("Wrong size", 4, removed.size());
        Assert.assertTrue("Result did not contain id", removed.containsKey(id1));
        Assert.assertTrue("Result did not contain id", removed.containsKey(id2));
        Assert.assertTrue("Result did not contain id", removed.containsKey(id3));
        Assert.assertTrue("Result did not contain id", removed.containsKey(id5));
        
        IDMap<Resource> removed2 = remove(id4);
        Assert.assertEquals("Wrong size", 1, removed2.size());
        
        MultiMap<ID,ID> idMapping = getIDMapping();
        IMap<ID, Resource> resources = getResourceMap();
        Assert.assertEquals("Id mapping not empty", 0, idMapping.size());
        Assert.assertEquals("Resources not empty", 0, resources.size());
    }
    
    private ID generateId() {
        return new ID("ox", "some.body", "context", UUID.randomUUID().toString());
    }
    
    private Resource generateResource() {
        return new DefaultResource();
    }

}
