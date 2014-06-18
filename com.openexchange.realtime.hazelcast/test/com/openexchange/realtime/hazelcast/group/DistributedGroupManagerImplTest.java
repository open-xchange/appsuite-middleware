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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.realtime.hazelcast.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.group.helper.MessageDispatcherMock;
import com.openexchange.realtime.hazelcast.group.helper.PortableFactoryAdapter;
import com.openexchange.realtime.hazelcast.serialization.PortableID;
import com.openexchange.realtime.hazelcast.serialization.PortableIDFactory;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.Duration;
import com.openexchange.realtime.util.IDMap;


/**
 * {@link DistributedGroupManagerImplTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class DistributedGroupManagerImplTest {

    private static String CLIENT_MAP = "client_map";
    private static String GROUP_MAP = "group_map";
    private static HazelcastInstance hzInstance;
    private static ID user1, user2, user3;
    private static ID group1, group2, group3;
    GlobalRealtimeCleanup grcMock;
    private static DistributedGroupManagerImpl groupManager;
    //Stanzas the messageDispatcher sent during the test
    private Multimap<ID, Stanza> sentStanzas;
    private MessageDispatcherMock messageDispatcher;

    @BeforeClass
    public static void setUpClass() throws Exception {
        user1 = new ID("user1@context");
        user2 = new ID("user2@context");
        user3 = new ID("user3@context");
        group1 = new ID("group1@synthetic");
        group2 = new ID("group2@synthetic");
        group3 = new ID("group3@synthetic");
        Config config = new Config();
        PortableFactoryAdapter portableIDFactory = new PortableFactoryAdapter(new PortableIDFactory());
        config.getSerializationConfig().addPortableFactory(PortableID.FACTORY_ID, portableIDFactory);
        hzInstance = Hazelcast.newHazelcastInstance(config);
        HazelcastAccess.setHazelcastInstance(hzInstance);
    }

    @Before
    public void setUp() {
        sentStanzas = HashMultimap.create();
        messageDispatcher  = new MessageDispatcherMock(sentStanzas);
        grcMock = mock(GlobalRealtimeCleanup.class);
        groupManager = new DistributedGroupManagerImpl(messageDispatcher, grcMock, CLIENT_MAP, GROUP_MAP);
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#add(com.openexchange.realtime.packet.ID, com.openexchange.realtime.packet.ID)}.
     * @throws OXException 
     */
    @Test
    public void testAdd() throws Exception {
        groupManager.add(user1, group1);
        groupManager.add(user1, group2);
        groupManager.add(user2, group2);
        groupManager.add(user1, group3);
        groupManager.add(user2, group3);
        groupManager.add(user3, group3);
        Set<ID> groupsForUser1 = groupManager.getGroups(user1);
        Set<ID> groupsForUser2 = groupManager.getGroups(user2);
        Set<ID> groupsForUser3 = groupManager.getGroups(user3);
        assertEquals(3, groupsForUser1.size());
        assertEquals(2, groupsForUser2.size());
        assertEquals(1, groupsForUser3.size());
        assertEquals(0, getDurationMap().size());
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#remove(com.openexchange.realtime.packet.ID)}.
     * @throws Exception 
     */
    @Test
    public void testRemoveID() throws Exception {
        fillGroupManager();
        Collection<ID> removedGroupsForUser1 = groupManager.remove(user1);
        assertEquals(3, removedGroupsForUser1.size());
        assertTrue(removedGroupsForUser1.containsAll(Arrays.asList(group1, group2, group3)));
        Set<ID> groupsForUser1 = groupManager.getGroups(user1);
        assertEquals(Collections.EMPTY_SET, groupsForUser1);

        Collection<ID> removedGroupsForUser2 = groupManager.remove(user2);
        assertEquals(2, removedGroupsForUser2.size());
        assertTrue(removedGroupsForUser2.containsAll(Arrays.asList(group2, group3)));
        Set<ID> groupsForUser2 = groupManager.getGroups(user2);
        assertEquals(Collections.EMPTY_SET, groupsForUser2);

        Collection<ID> removedGroupsForUser3 = groupManager.remove(user3);
        assertEquals(1, removedGroupsForUser3.size());
        assertTrue(removedGroupsForUser3.contains(group3));
        Set<ID> groupsForUser3 = groupManager.getGroups(user3);
        assertEquals(Collections.EMPTY_SET, groupsForUser3);
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#remove(com.openexchange.realtime.packet.ID, com.openexchange.realtime.packet.ID)}.
     * @throws Exception 
     */
    @Test
    public void testRemoveIDID() throws Exception {
        fillGroupManager();
        Set<ID> groups = groupManager.getGroups(user1);
        assertEquals(3, groups.size());
        boolean removed = groupManager.remove(user1, group3);
        assertTrue(removed);
        groups = groupManager.getGroups(user1);
        assertEquals(2, groups.size());
        assertTrue(groups.containsAll(Arrays.asList(group1, group2)));
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#getGroups(com.openexchange.realtime.packet.ID)}.
     */
    @Test @Ignore
    public void testGetGroups() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#getMembers(com.openexchange.realtime.packet.ID)}.
     */
    @Test @Ignore
    public void testGetMembers() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#setInactivity(com.openexchange.realtime.packet.ID, com.openexchange.realtime.util.Duration)}.
     * @throws Exception 
     */
    @Test
    public void testSetInactivity() throws Exception {
        fillGroupManager();
        groupManager.setInactivity(user1, Duration.TEN_SECONDS);
        IDMap<Duration> durationMap = getDurationMap();
        assertEquals(1, durationMap.size());
        assertEquals(3, sentStanzas.size());
        Collection<Stanza> collection1 = sentStanzas.removeAll(group1);
        Collection<Stanza> collection2 = sentStanzas.removeAll(group2);
        Collection<Stanza> collection3 = sentStanzas.removeAll(group3);
        assertEquals(1, collection1.size());
        assertEquals(1, collection2.size());
        assertEquals(1, collection3.size());
        assertTrue(sentStanzas.isEmpty());
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#cleanupForId(com.openexchange.realtime.packet.ID)}.
     * @throws Exception 
     */
    @Test
    public void testCleanupForId() throws Exception {
        fillGroupManager();
        groupManager.setInactivity(user1, Duration.TEN_SECONDS);
        groupManager.setInactivity(user2, Duration.FOUR_MINUTES);
        groupManager.setInactivity(user3, Duration.THIRTY_SECONDS);
        groupManager.cleanupForId(user1);
        IDMap<Duration> durationMap = getDurationMap();
        assertEquals(2, durationMap.size());
        assertNull(durationMap.get(user1));
        assertTrue(groupManager.getGroups(user1).isEmpty());
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#getManagementObject()}.
     */
    @Test
    public void testGetManagementObject() {
        assertNotNull(groupManager.getManagementObject());
    }

    // ===-Helpers-=========================================================================================================================

    private IDMap<Duration> getDurationMap() throws Exception {
        Field inactivityField = DistributedGroupManagerImpl.class.getDeclaredField("inactivityMap");
        inactivityField.setAccessible(true);
        IDMap<Duration> idMap = (IDMap<Duration>) inactivityField.get(groupManager);
        return idMap;
    }

    private void fillGroupManager() throws Exception {
        groupManager.add(user1, group1);
        groupManager.add(user1, group2);
        groupManager.add(user2, group2);
        groupManager.add(user1, group3);
        groupManager.add(user2, group3);
        groupManager.add(user3, group3);
    }

    private void setInactivities() throws Exception {
        groupManager.setInactivity(user1, Duration.TEN_SECONDS);
        groupManager.setInactivity(user2, Duration.THIRTY_SECONDS);
        groupManager.setInactivity(user3, Duration.ONE_MINUTE);
    }

}
