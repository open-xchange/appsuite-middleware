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
import org.junit.Before;
import org.junit.Test;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.serialization.DynamicPortableFactoryImpl;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableIDFactory;
import com.openexchange.realtime.hazelcast.serialization.packet.PortablePresence;
import com.openexchange.realtime.hazelcast.serialization.packet.PortablePresenceFactory;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;


/**
 * {@link PortablePresenceTest} - Test serialize -> deserialize roundtrip for PortablePresence instances.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PortablePresenceTest {

    private static HazelcastInstance hzInstance;
    private static final String PRESENCE_MAP = "presenceMap";
    private static ID user1, user2, user3;
    private static Presence presence1, presence2, presence3;

    @Before
    public void setUp() {
        Config config = new Config();
        DynamicPortableFactoryImpl dynamicPortableFactory = new DynamicPortableFactoryImpl();
        dynamicPortableFactory.register(new PortableIDFactory());
        dynamicPortableFactory.register(new PortablePresenceFactory());
        config.getSerializationConfig().addPortableFactory(DynamicPortableFactory.FACTORY_ID, dynamicPortableFactory);
        hzInstance = Hazelcast.newHazelcastInstance(config);
        HazelcastAccess.setHazelcastInstance(hzInstance);
        user1 = new ID("user1@context");
        user2 = new ID("user2@context");
        user3 = new ID("user3@context");
        presence1 = Presence.builder()
            .from(user1)
            .type(Presence.Type.NONE)
            .state(PresenceState.ONLINE)
            .message("Hey, i'm online")
            .priority((byte) 0)
            .build();
        presence2 = Presence.builder()
            .from(user2)
            .type(Presence.Type.NONE)
            .state(PresenceState.DO_NOT_DISTURB)
            .message("In a meeting")
            .priority((byte)1)
            .build();
        presence3 = Presence.builder()
            .from(user3)
            .type(Presence.Type.UNAVAILABLE)
            .state(PresenceState.OFFLINE)
            .message(null)
            .priority((byte)2)
            .build();
    }

    @Test
    public void testPortableRoundtrip() {
        IMap<PortableID, PortablePresence> presenceMap = hzInstance.getMap(PRESENCE_MAP);

        PortableID portbleUser1 = new PortableID(user1);
        PortablePresence portablePresence1 = new PortablePresence(presence1);
        presenceMap.put(portbleUser1, portablePresence1);
        PortablePresence deserializedPortablePresence1 = presenceMap.get(portbleUser1);
        assertEquals(portablePresence1, deserializedPortablePresence1);
        assertEquals(presence1, portablePresence1.getPresence());

        PortableID portableUser2 = new PortableID(user2);
        PortablePresence portablePresence2 = new PortablePresence(presence2);
        presenceMap.put(portableUser2, portablePresence2);
        PortablePresence deserialzedPortablePresence2 = presenceMap.get(portableUser2);
        assertEquals(portablePresence2, deserialzedPortablePresence2);
        assertEquals(presence2, deserialzedPortablePresence2.getPresence());

        PortableID portableUser3 = new PortableID(user3);
        PortablePresence portablePresence3 = new PortablePresence(presence3);
        presenceMap.put(portableUser3, portablePresence3);
        PortablePresence deserializedPortablePresence3 = presenceMap.get(portableUser3);
        assertEquals(portablePresence3, deserializedPortablePresence3);
        assertEquals(presence3, deserializedPortablePresence3.getPresence());
    }

}
