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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.junit.Before;
import org.junit.Test;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
import com.openexchange.realtime.directory.RoutingInfo;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.serialization.DynamicPortableFactoryImpl;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableRoutingInfo;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableRoutingInfoFactory;


/**
 * {@link PortableRoutingInfoTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PortableRoutingInfoTest {

    private static HazelcastInstance hzInstance;
    private static final String ROUTING_INFO_MAP = "routingInfoMap";

    @Before
    public void setUp() {
        Config config = new Config();
        DynamicPortableFactoryImpl dynamicPortableFactory = new DynamicPortableFactoryImpl();
        dynamicPortableFactory.register(new PortableRoutingInfoFactory());
        config.getSerializationConfig().addPortableFactory(DynamicPortableFactory.FACTORY_ID, dynamicPortableFactory);
        hzInstance = Hazelcast.newHazelcastInstance(config);
        HazelcastAccess.setHazelcastInstance(hzInstance);
    }

    @Test
    public void testRoundTrip() throws Exception {
        IMap<Integer, PortableRoutingInfo> routingMap = hzInstance.getMap(ROUTING_INFO_MAP);
        InetAddress inet4Address = InetAddress.getByName("10.0.0.0");
        InetAddress inet6Address = InetAddress.getByName("1080:0:0:0:8:800:200C:417A");
        RoutingInfo routingInfo4 = new RoutingInfo(new InetSocketAddress(inet4Address, 4), "4");
        PortableRoutingInfo portableRoutingInfo4 = new PortableRoutingInfo(routingInfo4);
        routingMap.put(4, portableRoutingInfo4);

        RoutingInfo routingInfo6 = new RoutingInfo(new InetSocketAddress(inet6Address, 6), "6");
        PortableRoutingInfo portableRoutingInfo6 = new PortableRoutingInfo(routingInfo6);
        routingMap.put(6, portableRoutingInfo6);

        PortableRoutingInfo deserializedPortableRoutingInfo4 = routingMap.get(4);
        assertEquals(portableRoutingInfo4, deserializedPortableRoutingInfo4);

        PortableRoutingInfo deserializedPortableRoutingInfo6 = routingMap.get(6);
        assertEquals(portableRoutingInfo6, deserializedPortableRoutingInfo6);
    }

}
