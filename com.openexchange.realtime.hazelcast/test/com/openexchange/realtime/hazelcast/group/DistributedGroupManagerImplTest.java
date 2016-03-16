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

package com.openexchange.realtime.hazelcast.group;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.jayway.awaitility.Awaitility;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.group.SelectorChoice;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.group.helper.MessageDispatcherMock;
import com.openexchange.realtime.hazelcast.group.helper.SimServiceLookup;
import com.openexchange.realtime.hazelcast.osgi.Services;
import com.openexchange.realtime.hazelcast.serialization.DynamicPortableFactoryImpl;
import com.openexchange.realtime.hazelcast.serialization.group.PortableSelectorChoiceFactory;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableIDFactory;
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
    private static SelectorChoice user1Group1, user1Group2, user1Group3, user2Group1, user2Group2, user2Group3, user3Group1, user3Group2, user3Group3;
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
        user1Group1 = new SelectorChoice(user1, group1, "user1Group1");
        user1Group2 = new SelectorChoice(user1, group2, "user1Group2");
        user1Group3 = new SelectorChoice(user1, group3, "user1Group3");
        user2Group1 = new SelectorChoice(user2, group1, "user2Group1");
        user2Group2 = new SelectorChoice(user2, group2, "user2Group2");
        user2Group3 = new SelectorChoice(user2, group3, "user2Group3");
        user3Group1 = new SelectorChoice(user3, group1, "user3Group1");
        user3Group2 = new SelectorChoice(user3, group2, "user3Group2");
        user3Group3 = new SelectorChoice(user3, group3, "user3Group3");
        Config config = new Config();
        DynamicPortableFactoryImpl dynamicPortableFactory = new DynamicPortableFactoryImpl();
        dynamicPortableFactory.register(new PortableIDFactory());
        dynamicPortableFactory.register(new PortableSelectorChoiceFactory());
        config.getSerializationConfig().addPortableFactory(DynamicPortableFactory.FACTORY_ID, dynamicPortableFactory);
        hzInstance = Hazelcast.newHazelcastInstance(config);
        HazelcastAccess.setHazelcastInstance(hzInstance);
    }

    @Before
    public void setUp() {
        sentStanzas = Multimaps.synchronizedMultimap(HashMultimap.<ID, Stanza>create());
        messageDispatcher  = new MessageDispatcherMock(sentStanzas);
        groupManager = new DistributedGroupManagerImpl(messageDispatcher, CLIENT_MAP, GROUP_MAP);
        Services.setServiceLookup(new SimServiceLookup());
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#add(com.openexchange.realtime.packet.ID, com.openexchange.realtime.packet.ID)}.
     * @throws OXException
     */
    @Test
    public void testAdd() throws Exception {
        fillGroupManager();
        Collection<? extends SelectorChoice> groupsForUser1 = groupManager.getGroups(user1);
        Collection<? extends SelectorChoice> groupsForUser2 = groupManager.getGroups(user2);
        Collection<? extends SelectorChoice> groupsForUser3 = groupManager.getGroups(user3);
        assertEquals(3, groupsForUser1.size());
        assertEquals(2, groupsForUser2.size());
        assertEquals(1, groupsForUser3.size());
        assertEquals(0, getDurationMap().size());
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#removeClient(com.openexchange.realtime.packet.ID)}.
     * @throws Exception
     */
    @Test
    public void testRemoveID() throws Exception {
        fillGroupManager();
        Collection<? extends SelectorChoice> removedGroupsForUser1 = groupManager.removeClient(user1);
        assertEquals(3, removedGroupsForUser1.size());
        assertTrue(removedGroupsForUser1.containsAll(Arrays.asList(user1Group1, user1Group2, user1Group3)));
        Collection<? extends SelectorChoice> groupsForUser1 = groupManager.getGroups(user1);
        assertEquals(0, groupsForUser1.size());

        Collection<? extends SelectorChoice> removedGroupsForUser2 = groupManager.removeClient(user2);
        assertEquals(2, removedGroupsForUser2.size());
        assertTrue(removedGroupsForUser2.containsAll(Arrays.asList(user2Group2, user2Group3)));
        Collection<? extends SelectorChoice> groupsForUser2 = groupManager.getGroups(user2);
        assertEquals(0, groupsForUser2.size());

        Collection<? extends SelectorChoice> removedGroupsForUser3 = groupManager.removeClient(user3);
        assertEquals(1, removedGroupsForUser3.size());
        assertTrue(removedGroupsForUser3.contains(user3Group3));
        Collection<? extends SelectorChoice> groupsForUser3 = groupManager.getGroups(user3);
        assertEquals(0, groupsForUser3.size());
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#remove(com.openexchange.realtime.packet.ID, com.openexchange.realtime.packet.ID)}.
     * @throws Exception
     */
    @Test
    public void testRemoveIDID() throws Exception {
        fillGroupManager();
        Collection<? extends SelectorChoice> groups = groupManager.getGroups(user1);
        assertEquals(3, groups.size());
        boolean removed = groupManager.removeChoice(user1Group3);
        assertTrue(removed);
        groups = groupManager.getGroups(user1);
        assertEquals(2, groups.size());
        assertTrue(groups.containsAll(Arrays.asList(user1Group1, user1Group2)));
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#getGroups(com.openexchange.realtime.packet.ID)}.
     * @throws Exception
     */
    @Test
    public void testGetGroups() throws Exception {
        fillGroupManager();
        Collection<? extends SelectorChoice> groups = groupManager.getGroups(user1);
        assertEquals(3, groups.size());
        groups = groupManager.getGroups(user2);
        assertEquals(2, groups.size());
        groups = groupManager.getGroups(user3);
        assertEquals(1, groups.size());

        groupManager.removeClient(user1);
        groups = groupManager.getGroups(user1);
        assertEquals(0, groups.size());
        groups = groupManager.getGroups(user2);
        assertEquals(2, groups.size());
        groups = groupManager.getGroups(user3);
        assertEquals(1, groups.size());

        groupManager.removeClient(user2);
        groups = groupManager.getGroups(user1);
        assertEquals(0, groups.size());
        groups = groupManager.getGroups(user2);
        assertEquals(0, groups.size());
        groups = groupManager.getGroups(user3);
        assertEquals(1, groups.size());

        groupManager.removeClient(user3);
        groups = groupManager.getGroups(user1);
        assertEquals(0, groups.size());
        groups = groupManager.getGroups(user2);
        assertEquals(0, groups.size());
        groups = groupManager.getGroups(user3);
        assertEquals(0, groups.size());
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#getMembers(com.openexchange.realtime.packet.ID)}.
     *
     * Only tests if the leave command was sent to the GroupDispatcher as no actual Message and GroupDispatchers are used in this test.
     *
     * @throws Exception
     */
    @Test
    public void testGetMembers() throws Exception {
        fillGroupManager();

        Collection<? extends SelectorChoice> members = groupManager.getMembers(group1);
        assertEquals(1, members.size());
        members = groupManager.getMembers(group2);
        assertEquals(2, members.size());
        members = groupManager.getMembers(group3);
        assertEquals(3, members.size());
        LeaveCommandMatcher leaveMatcher = new LeaveCommandMatcher();

        groupManager.removeClient(user1);
        Awaitility.await().atMost(com.jayway.awaitility.Duration.FIVE_SECONDS).until(numStanzasReceived(), greaterThanOrEqualTo(3));
        Set<Stanza> stanzas = getStanzas(user1, group1);
        assertEquals(stanzas.size(), 1);
        assertThat(stanzas.iterator().next().getPayload(), leaveMatcher);
        stanzas = getStanzas(user1, group2);
        assertEquals(stanzas.size(), 1);
        assertThat(stanzas.iterator().next().getPayload(), leaveMatcher);
        stanzas = getStanzas(user1, group3);
        assertEquals(stanzas.size(), 1);
        assertThat(stanzas.iterator().next().getPayload(), leaveMatcher);

        groupManager.removeClient(user2);
        Awaitility.await().atMost(com.jayway.awaitility.Duration.FIVE_SECONDS).until(numStanzasReceived(), greaterThanOrEqualTo(5));
        stanzas = getStanzas(user2, group1);
        assertEquals(stanzas.size(), 0);
        stanzas = getStanzas(user2, group2);
        assertEquals(stanzas.size(), 1);
        assertThat(stanzas.iterator().next().getPayload(), leaveMatcher);
        stanzas = getStanzas(user2, group3);
        assertEquals(stanzas.size(), 1);
        assertThat(stanzas.iterator().next().getPayload(), leaveMatcher);

        groupManager.removeClient(user3);
        Awaitility.await().atMost(com.jayway.awaitility.Duration.FIVE_SECONDS).until(numStanzasReceived(), greaterThanOrEqualTo(6));
        stanzas = getStanzas(user3, group1);
        assertEquals(stanzas.size(), 0);
        stanzas = getStanzas(user3, group2);
        assertEquals(stanzas.size(), 0);
        stanzas = getStanzas(user3, group3);
        assertEquals(stanzas.size(), 1);
        assertThat(stanzas.iterator().next().getPayload(), leaveMatcher);
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
        Awaitility.await().atMost(com.jayway.awaitility.Duration.FIVE_SECONDS).until(numStanzasReceived(), equalTo(3));
        Collection<Stanza> collection1 = sentStanzas.removeAll(group1);
        Collection<Stanza> collection2 = sentStanzas.removeAll(group2);
        Collection<Stanza> collection3 = sentStanzas.removeAll(group3);
        assertEquals(1, collection1.size());
        assertEquals(1, collection2.size());
        assertEquals(1, collection3.size());
        assertTrue(sentStanzas.isEmpty());
    }

    private Callable<Integer> numStanzasReceived() {
        return new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return sentStanzas.size();
            }
        };
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
        LeaveCommandMatcher leaveMatcher = new LeaveCommandMatcher();
        // The groupManager.cleanupForId(user1); sent a leave on behalf of user1 to all previously joined groups
        for (Stanza stanza : getStanzas(user1, group1, group2, group3)) {
            assertThat(stanza.getPayload(),leaveMatcher);
        }
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.group.DistributedGroupManagerImpl#cleanupForId(com.openexchange.realtime.packet.ID)}.
     * @throws Exception
     */
    @Test
    public void testCleanupForSyntheticId() throws Exception {
        fillGroupManager();
        //Remove the group
        groupManager.removeGroup(group3);
        assertTrue(groupManager.getMembers(group3).isEmpty());
        NotMemberMatcher notMemberMatcher = new NotMemberMatcher();
        Awaitility.await().atMost(com.jayway.awaitility.Duration.TEN_SECONDS).until(numStanzasReceived(), equalTo(3));
        // .. and assert that all previous members are informed about the removal of the group
        Set<Stanza> stanzas = getStanzas(group3, user1);
        assertEquals(stanzas.size(), 1);
        assertThat(stanzas.iterator().next().getPayload(), notMemberMatcher);
        stanzas = getStanzas(group3, user2);
        assertEquals(stanzas.size(), 1);
        assertThat(stanzas.iterator().next().getPayload(), notMemberMatcher);
        stanzas = getStanzas(group3, user3);
        assertEquals(stanzas.size(), 1);
        assertThat(stanzas.iterator().next().getPayload(), notMemberMatcher);
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

    /**
     * Adds the following mappings
     * <ul>
     * <li>user1 -> {group1, group2, group3}</li>
     * <li>user2 -> {group2, group3}</li>
     * <li>user3 -> {group3}</li>
     * </ul>
     * <ul>
     * <li>group1 -> {user1}</li>
     * <li>group2 -> {user1, user2}</li>
     * <li>group3 -> {user1, user2, user3}</li>
     * </ul>
     * @throws Exception
     */
    private void fillGroupManager() throws Exception {
        groupManager.addChoice(user1Group1);
        groupManager.addChoice(user1Group2);
        groupManager.addChoice(user2Group2);
        groupManager.addChoice(user1Group3);
        groupManager.addChoice(user2Group3);
        groupManager.addChoice(user3Group3);
    }

    /**
     * Get Stanzas sent from ID to ID
     * @param from
     * @param to
     * @return Stanzas sent from ID to ID
     */
    private Set<Stanza> getStanzas(ID from, ID... to) {
        List<ID> recipients = Arrays.asList(to);
        Set<Stanza> stanzas = new HashSet<Stanza>();
        Iterator<Stanza> iterator = sentStanzas.values().iterator();
        while(iterator.hasNext()) {
            Stanza next = iterator.next();
            if(from.equals(next.getFrom()) && recipients.contains(next.getTo())) {
                stanzas.add(next);
            }
        }
        return stanzas;
    }

}
