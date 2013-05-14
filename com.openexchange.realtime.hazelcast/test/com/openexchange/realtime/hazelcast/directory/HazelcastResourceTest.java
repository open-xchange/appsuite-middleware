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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.net.UnknownHostException;
import java.util.Date;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleService;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.directory.mock.HazelcastMemberMock;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;

/**
 * {@link HazelcastResourceTest}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HazelcastResourceTest {

    private ID marensID;

    private Presence onlinePresence;

    private Date currentDate;

    private Date epoch;

    private Date afterEpoch;

    private DefaultResource defaultResource;

    private static HazelcastMemberMock hazelcastMemberMock;

    private HazelcastResource differingHazelcastResource1;

    private HazelcastResource differingHazelcastResource2;

    private HazelcastResource matchingHazelcastResource1;

    private HazelcastResource matchingHazelcastResource2;

    /*
     * Mock for the getLocalMember call in HazelcastAccess (getHazelcastInstance().getCluster().getLocalMember()) needed for
     * HazelcastResource testing.
     */
    @BeforeClass
    public static void mockHazelcast() throws UnknownHostException {
        hazelcastMemberMock = new HazelcastMemberMock();

        Cluster hazelcastClusterMock = mock(Cluster.class);
        when(hazelcastClusterMock.getLocalMember()).thenReturn(hazelcastMemberMock);

        LifecycleService hazelcastLifecycleServiceMock = mock(LifecycleService.class);
        when(hazelcastLifecycleServiceMock.isRunning()).thenReturn(true);

        HazelcastInstance hazelcastInstanceMock = mock(HazelcastInstance.class);
        when(hazelcastInstanceMock.getCluster()).thenReturn(hazelcastClusterMock);
        when(hazelcastInstanceMock.getLifecycleService()).thenReturn(hazelcastLifecycleServiceMock);

        HazelcastAccess.setHazelcastInstance(hazelcastInstanceMock);
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        marensID = new ID("ox", "marens", "premium", "desktop");
        onlinePresence = Presence.builder().from(marensID).state(PresenceState.ONLINE).message("Hello World.").build();
        epoch = new Date(0);
        afterEpoch = new Date(1);
        differingHazelcastResource1 = new HazelcastResource(onlinePresence, epoch);
        differingHazelcastResource2 = new HazelcastResource(onlinePresence, afterEpoch);
        matchingHazelcastResource1 = new HazelcastResource(onlinePresence, currentDate);
        matchingHazelcastResource2 = new HazelcastResource(onlinePresence, currentDate);
        defaultResource = new DefaultResource(onlinePresence, currentDate);
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.directory.HazelcastResource#HazelcastResource()}.
     * 
     * @throws OXException
     */
    @Test
    public void testHazelcastResource() throws OXException {
        HazelcastResource hazelcastResource = new HazelcastResource();
        assertNull(hazelcastResource.getPresence());
        assertEquals(hazelcastMemberMock, hazelcastResource.getRoutingInfo());
        assertNotNull(hazelcastResource.getTimestamp());
    }

    /**
     * Test method for
     * {@link com.openexchange.realtime.hazelcast.directory.HazelcastResource#HazelcastResource(com.openexchange.realtime.packet.Presence)}.
     * 
     * @throws OXException
     */
    @Test
    public void testHazelcastResourcePresence() throws OXException {
        HazelcastResource hazelcastResource = new HazelcastResource(onlinePresence);
        assertEquals(onlinePresence, hazelcastResource.getPresence());
        assertNotNull(hazelcastResource.getTimestamp());
        assertEquals(hazelcastMemberMock, hazelcastResource.getRoutingInfo());
    }

    /**
     * Test method for
     * {@link com.openexchange.realtime.hazelcast.directory.HazelcastResource#HazelcastResource(com.openexchange.realtime.packet.Presence, java.util.Date)}
     * .
     * 
     * @throws OXException
     */
    @Test
    public void testHazelcastResourcePresenceDate() throws OXException {
        HazelcastResource hazelcastResource = new HazelcastResource(onlinePresence, currentDate);
        assertEquals(onlinePresence, hazelcastResource.getPresence());
        assertEquals(currentDate, hazelcastResource.getTimestamp());
        assertEquals(hazelcastMemberMock, hazelcastResource.getRoutingInfo());
    }

    /**
     * Test method for
     * {@link com.openexchange.realtime.hazelcast.directory.HazelcastResource#HazelcastResource(com.openexchange.realtime.directory.Resource)}
     * The constructor takes the Presence and Timestamp infos from the {@link DefaultResource} and adds the local hazelcast member as
     * routing infos.
     * 
     * @throws OXException
     */
    @Test
    public void testHazelcastResourceResource() throws OXException {
        HazelcastResource hazelcastResource = new HazelcastResource(defaultResource);
        assertEquals(defaultResource.getPresence(), hazelcastResource.getPresence());
        assertEquals(defaultResource.getTimestamp(), hazelcastResource.getTimestamp());
        assertEquals(hazelcastMemberMock, hazelcastResource.getRoutingInfo());
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.directory.HazelcastResource#getRoutingInfo()} and
     * {@link com.openexchange.realtime.hazelcast.directory.HazelcastResource#setRoutingInfo()}.
     * 
     * @throws OXException
     */
    @Test
    public void testGetSetRoutingInfo() throws OXException {
        HazelcastResource hazelcastResource = new HazelcastResource();
        assertNotNull(hazelcastResource.getRoutingInfo());
        hazelcastResource.setRoutingInfo(null);
        assertNull(hazelcastResource.getRoutingInfo());
        hazelcastResource.setRoutingInfo(hazelcastMemberMock);
        assertEquals(hazelcastMemberMock, hazelcastResource.getRoutingInfo());
    }

    /**
     * Test method for {@link com.openexchange.realtime.directory.AbstractResource#getPresence(com.openexchange.realtime.packet.Presence)}
     * and {@link com.openexchange.realtime.directory.AbstractResource#setPresence()}.
     * 
     * @throws OXException
     */
    @Test
    public void testGetSetPresence() throws OXException {
        HazelcastResource hazelcastResource = new HazelcastResource(onlinePresence);
        assertNotNull(hazelcastResource.getPresence());
        hazelcastResource.setPresence(null);
        assertNull(hazelcastResource.getPresence());
        hazelcastResource.setPresence(onlinePresence);
        assertEquals(onlinePresence, hazelcastResource.getPresence());
    }

    /**
     * Test method for {@link com.openexchange.realtime.directory.AbstractResource#setTimestamp(java.util.Date)} and
     * {@link com.openexchange.realtime.directory.AbstractResource#getTimestamp()}.
     * 
     * @throws OXException
     */
    @Test
    public void testGetSetTimestamp() throws OXException {
        HazelcastResource hazelcastResource = new HazelcastResource(onlinePresence, currentDate);
        assertEquals(currentDate, hazelcastResource.getTimestamp());
        Date newDate = new Date();
        hazelcastResource.setTimestamp(newDate);
        assertEquals(newDate, hazelcastResource.getTimestamp());
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.directory.HazelcastResource#equals(java.lang.Object)}.
     * 
     * @throws OXException
     * @throws InterruptedException
     */
    @Test
    public void testEqualsObject() throws OXException, InterruptedException {
        assertFalse(differingHazelcastResource1.equals(differingHazelcastResource2));
        assertTrue(matchingHazelcastResource1.equals(matchingHazelcastResource2));
    }

    /**
     * Test method for {@link com.openexchange.realtime.hazelcast.directory.HazelcastResource#hashCode()}.
     * 
     * @throws OXException
     */
    @Test
    public void testHashCode() throws OXException {
        assertFalse(differingHazelcastResource1.hashCode() == (differingHazelcastResource2.hashCode()));
        assertTrue(matchingHazelcastResource1.hashCode() == matchingHazelcastResource2.hashCode());
    }

}
