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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.RoutingInfo;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.serialization.DynamicPortableFactoryImpl;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableResource;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableResourceFactory;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableRoutingInfoFactory;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableIDFactory;
import com.openexchange.realtime.hazelcast.serialization.packet.PortablePresenceFactory;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDManager;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link HazelcastResourceDirectoryTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class HazelcastResourceDirectoryTest extends HazelcastResourceDirectory {

    private static final String ID_MAP_NAME = "ID_MAP";

    private static final String RESOURCE_MAP_NAME = "RESOURCE_MAP";

    @BeforeClass
    public static void setUp() {
        ID.ID_MANAGER_REF.set(new IDManager());
    }

    @AfterClass
    public static void tearDown() {
        ID.ID_MANAGER_REF.set(null);
    }

    public HazelcastResourceDirectoryTest() throws OXException {
        super(ID_MAP_NAME, RESOURCE_MAP_NAME);
    }

    private static ExecutorService executorService;
    private static Random random;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Config config = new Config();
        DynamicPortableFactoryImpl dynamicPortableFactory = new DynamicPortableFactoryImpl();
        dynamicPortableFactory.register(new PortableIDFactory());
        dynamicPortableFactory.register(new PortablePresenceFactory());
        dynamicPortableFactory.register(new PortableRoutingInfoFactory());
        dynamicPortableFactory.register(new PortableResourceFactory());
        config.getSerializationConfig().addPortableFactory(DynamicPortableFactory.FACTORY_ID, dynamicPortableFactory);
        for(ClassDefinition cd : dynamicPortableFactory.getClassDefinitions()) {
            config.getSerializationConfig().addClassDefinition(cd);
        }
        MapConfig mapConfig = new MapConfig(RESOURCE_MAP_NAME);
        mapConfig.setMaxIdleSeconds(1);
        config.addMapConfig(mapConfig);
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);
        HazelcastAccess.setHazelcastInstance(hazelcast);
        executorService = Executors.newFixedThreadPool(25);
        random = new Random();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        HazelcastInstance hazelcast = HazelcastAccess.getHazelcastInstance();
        hazelcast.getLifecycleService().shutdown();
        HazelcastAccess.setHazelcastInstance(null);
    }

    @After
    public void after() throws Exception {
        getIDMapping().destroy();
        getResourceMapping().destroy();
    }

    @Test
    public void testSetNewResource() throws Exception {
        ID concreteId = generateId();
        ID generalId = concreteId.toGeneralForm();
        Resource resource = generateResource(concreteId);

        Assert.assertEquals("Wrong return value", null, set(concreteId, resource));
        IDMap<Resource> resources = get(generalId);
        Assert.assertEquals("Wrong size", 1, resources.size());
        ID reloadedId = resources.keySet().iterator().next();
        Assert.assertEquals("Wrong id", reloadedId, concreteId);

        Resource reloaded = resources.get(concreteId);
        RoutingInfo routingInfo = reloaded.getRoutingInfo();
        Member localMember = HazelcastAccess.getLocalMember();
        Assert.assertEquals("Wrong UUID in reloaded routing info", localMember.getUuid(), routingInfo.getId());
        Assert.assertEquals("Wrong SocketAddress in reloaded routing info", localMember.getSocketAddress(), routingInfo.getSocketAddress());
    }

    @Test
    public void testResourceEviction() throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final ResourceMappingEntryAdapter listener = new ResourceMappingEntryAdapter() {
            @Override
            public void entryEvicted(EntryEvent<PortableID, PortableResource> event) {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        };

        String listenerID = getResourceMapping().addEntryListener(listener, false);
        try {
            ID concreteId = generateId();
            Resource resource = generateResource(concreteId);
            set(concreteId, resource);
            Assert.assertEquals("Resource directory empty", 1, getResourceMapping().size());
            Assert.assertEquals("ID map empty", 1, getIDMapping().size());
            barrier.await(60, TimeUnit.SECONDS);
            Assert.assertEquals("Resource directory not empty", 0, getResourceMapping().size());
            Assert.assertEquals("ID map not empty", 0, getIDMapping().size());
        } finally {
            getResourceMapping().removeEntryListener(listenerID);
        }
    }

    @Test
    public void testUpdateResource() throws Exception {
        //Generate and ad Presence in ResourceDirectory
        ID concreteId = generateId();
        ID generalId = concreteId.toGeneralForm();
        Resource resource = generateResource(concreteId);
        set(concreteId, resource);

        //Reload Resource from directory and assert that presence state hasn't changed
        Resource reloadedResource = get(generalId).entrySet().iterator().next().getValue();
        assertTrue("Wrong Presence in reloaded Resource", arePresencesEqual(resource.getPresence(), reloadedResource.getPresence()));
        Assert.assertEquals("Wrong Timestamp in reloaded Resource", resource.getTimestamp(), reloadedResource.getTimestamp());

        //Change state by adding a new resource with PresenceState offline
        DefaultResource changedResource = new DefaultResource(Presence.builder().from(concreteId).state(PresenceState.OFFLINE).build());
        Resource previous = set(concreteId, changedResource);
        Assert.assertNotNull(previous);
        reloadedResource = get(generalId).entrySet().iterator().next().getValue();
        assertTrue("Wrong Presence in reloaded Resource", arePresencesEqual(changedResource.getPresence(), reloadedResource.getPresence()));
        Assert.assertEquals("Wrong Timestamp in reloaded Resource", changedResource.getTimestamp(), reloadedResource.getTimestamp());
    }

    @Test
    public void testSetAndRemoveMultiple() throws Exception {
        ID id1 = generateId();
        ID id2 = generateId();
        ID id3 = generateId();
        Resource r1 = generateResource(id1);
        Resource r2 = generateResource(id2);
        Resource r3 = generateResource(id3);

        set(id1, r1);
        set(id2, r2);
        set(id3, r3);

        IDMap<Resource> idMap1 = get(id1);
        Assert.assertEquals("Wrong size", 1, idMap1.size());
        Resource reloadedResource1 = idMap1.entrySet().iterator().next().getValue();

        Assert.assertEquals("Wrong Timestamp in reloaded Resource", r1.getTimestamp(), reloadedResource1.getTimestamp());


        IDMap<Resource> idMap2 = get(id2);
        Assert.assertEquals("Wrong size", 1, idMap2.size());
        Resource reloadedResource2 = idMap2.entrySet().iterator().next().getValue();
        assertTrue("Wrong Presence in reloaded Resource", arePresencesEqual(r2.getPresence(), reloadedResource2.getPresence()));

        Assert.assertEquals("Wrong Timestamp in reloaded Resource", r2.getTimestamp(), reloadedResource2.getTimestamp());


        IDMap<Resource> idMap3 = get(id3);
        Assert.assertEquals("Wrong size", 1, idMap3.size());
        Resource reloadedResource3 = idMap3.entrySet().iterator().next().getValue();
        assertTrue("Wrong Presence in reloaded Resource", arePresencesEqual(r3.getPresence(), reloadedResource3.getPresence()));
        Assert.assertEquals("Wrong Timestamp in reloaded Resource", r3.getTimestamp(), reloadedResource3.getTimestamp());


        IDMap<Resource> all = get(id1.toGeneralForm());
        Assert.assertEquals("Wrong size", 3, all.size());
        Assert.assertTrue("Result did not contain id", all.containsKey(id1));
        Assert.assertTrue("Result did not contain id", all.containsKey(id2));
        Assert.assertTrue("Result did not contain id", all.containsKey(id3));

        IDMap<Resource> remove1 = remove(id1);
        Assert.assertEquals("Wrong size", 1, remove1.size());
        Resource removedResource1 = remove1.entrySet().iterator().next().getValue();
        assertTrue("Wrong Presence in reloaded Resource", arePresencesEqual(r1.getPresence(), removedResource1.getPresence()));
        Assert.assertEquals("Wrong Timestamp in reloaded Resource", r1.getTimestamp(), removedResource1.getTimestamp());

        Set<ID> toRemove = new HashSet<ID>();
        toRemove.add(id2);
        toRemove.add(id3);

        IDMap<Resource> remove2 = remove(toRemove);
        Assert.assertEquals("Wrong size", 2, remove2.size());

        MultiMap<PortableID,PortableID> idMapping = getIDMapping();
        IMap<PortableID,PortableResource> resources = getResourceMapping();
        Assert.assertEquals("Id mapping not empty", 0, idMapping.size());
        Assert.assertEquals("Resources not empty", 0, resources.size());
    }

    @Test
    public void testSetAndGetAndRemoveMultipleWithMixedIdTypes() throws Exception {
        ID id1 = generateId();
        ID id2 = generateId();
        ID id3 = generateId();
        ID id4 = new ID("ox", null, "another.user", "context", UUID.randomUUID().toString());
        ID id5 = new ID("ox", null, "even.another", "context", UUID.randomUUID().toString());
        Resource r1 = generateResource(id1);
        Resource r2 = generateResource(id2);
        Resource r3 = generateResource(id3);
        Resource r4 = generateResource(id4);
        Resource r5 = generateResource(id5);

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

        MultiMap<PortableID,PortableID> idMapping = getIDMapping();
        IMap<PortableID,PortableResource> resources = getResourceMapping();
        Assert.assertEquals("Id mapping not empty", 0, idMapping.size());
        Assert.assertEquals("Resources not empty", 0, resources.size());
    }

    /*
     * https://github.com/hazelcast/hazelcast/issues/441
     */
    @Test
    public void testTransactionsInSet() throws Exception {
        final ID testID = generateId();
        final Resource testResource = generateResource(testID);

        List<Callable<Integer>> callables = new ArrayList<Callable<Integer>>();

        for (int i = 0; i < 3; i++) {
            final int j = i;
            callables.add(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    Thread.sleep(random.nextInt(500));
                    testResource.getPresence().setMessage(String.valueOf(j));
                    set(testID, testResource);
                    return j;
                }
            }
            );
        }
        List<Future<Integer>> invokeAll = executorService.invokeAll(callables);
        for (Future<Integer> future : invokeAll) {
            System.out.println(future.get());
        }
    }

    private ID generateId() {
        return new ID("ox", "some.component", "some.body", "context", UUID.randomUUID().toString());
    }

    private Resource generateResource(ID id) {
        return new DefaultResource(Presence.builder().from(id).state(PresenceState.ONLINE).build());
    }

    private boolean arePresencesEqual(Presence p1, Presence p2) {
        assertNotNull(p1);
        assertNotNull(p2);
        assertEquals(p1.getFrom(), p2.getFrom());
        assertEquals(p1.getMessage(), p2.getMessage());
        assertEquals(p1.getPriority(), p2.getPriority());
        assertEquals(p1.getState(), p2.getState());
        assertEquals(p1.getType(), p2.getType());
        return true;
    }

}
