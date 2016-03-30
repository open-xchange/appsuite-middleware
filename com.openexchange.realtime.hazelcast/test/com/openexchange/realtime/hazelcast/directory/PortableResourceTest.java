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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.RoutingInfo;
import com.openexchange.realtime.hazelcast.serialization.DynamicPortableFactoryImpl;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableResource;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableResourceFactory;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableRoutingInfoFactory;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableIDFactory;
import com.openexchange.realtime.hazelcast.serialization.packet.PortablePresenceFactory;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;

/**
 * {@link PortableResourceTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PortableResourceTest {

    private static HazelcastInstance hzInstance;

    private Member localMember;

    private static final String RESOURCE_MAP = "resourceMap";

    private ID marensID;

    private PortableID portableMarensID;

    private Presence onlinePresence;

    private Date currentDate;

    private Date epoch;

    private Date afterEpoch;

    private DefaultResource defaultResource;

    private PortableResource differingPortableResource1;

    private PortableResource differingPortableResource2;

    private PortableResource matchingPortableResource1;

    private PortableResource matchingPortableResource2;

    @Before
    public void setUp() throws Exception {
        marensID = new ID("ox", "marens", "premium", "desktop");
        portableMarensID = new PortableID(marensID);
        onlinePresence = Presence.builder().from(marensID).state(PresenceState.ONLINE).message("Hello World.").build();
        currentDate = new Date();
        epoch = new Date(0);
        afterEpoch = new Date(1);
        defaultResource = new DefaultResource(onlinePresence, currentDate);

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
        hzInstance = Hazelcast.newHazelcastInstance(config);
        localMember = hzInstance.getCluster().getLocalMember();

        differingPortableResource1 = new PortableResource(new DefaultResource(onlinePresence, epoch), localMember);
        differingPortableResource2 = new PortableResource(new DefaultResource(onlinePresence, afterEpoch), localMember);
        matchingPortableResource1 = new PortableResource(new DefaultResource(onlinePresence, currentDate), localMember);
        matchingPortableResource2 = new PortableResource(new DefaultResource(onlinePresence, currentDate), localMember);
    }

    @Test
    public void testPortableHazelcastResource() throws OXException {
        PortableResource portableResource = new PortableResource(localMember);
        assertNull(portableResource.getPresence());
        RoutingInfo routingInfo = portableResource.getRoutingInfo();
        assertEquals(localMember.getSocketAddress(), routingInfo.getSocketAddress());
        assertEquals(localMember.getUuid(), routingInfo.getId());
        assertNotNull(portableResource.getTimestamp());
        IMap<PortableID, PortableResource> resourceMap = hzInstance.getMap(RESOURCE_MAP);
        resourceMap.put(portableMarensID, portableResource);
        PortableResource deserializedPortableResource = resourceMap.get(portableMarensID);
        assertEquals(portableResource, deserializedPortableResource);
    }

    @Test
    public void testPortableHazelcastResourcePresence() throws OXException {
        PortableResource portableResource = new PortableResource(defaultResource, localMember);
        assertEquals(onlinePresence, portableResource.getPresence());
        RoutingInfo routingInfo = portableResource.getRoutingInfo();
        assertEquals(localMember.getSocketAddress(), routingInfo.getSocketAddress());
        assertEquals(localMember.getUuid(), routingInfo.getId());
        assertNotNull(portableResource.getTimestamp());
        IMap<PortableID, PortableResource> resourceMap = hzInstance.getMap(RESOURCE_MAP);
        resourceMap.put(portableMarensID, portableResource);
        PortableResource deserializedPortableResource = resourceMap.get(portableMarensID);
        assertEquals(portableResource, deserializedPortableResource);

    }

    @Test
    public void testEqualsObject() throws OXException, InterruptedException {
        assertFalse(differingPortableResource1.equals(differingPortableResource2));
        assertTrue(matchingPortableResource1.equals(matchingPortableResource2));
    }

    @Test
    public void testHashCode() throws OXException {
        assertFalse(differingPortableResource1.hashCode() == (differingPortableResource2.hashCode()));
        assertTrue(matchingPortableResource1.hashCode() == matchingPortableResource2.hashCode());
    }

}
