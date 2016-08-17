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

package com.openexchange.realtime.hazelcast.serialization.util;

import static org.junit.Assert.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.DynamicPortableFactory;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.hazelcast.serialization.DynamicPortableFactoryImpl;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableIDFactory;
import com.openexchange.realtime.util.IDMap;


/**
 * {@link PortableIDToOXExceptionMapTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.2
 */
public class PortableIDToOXExceptionMapTest {

    private HazelcastInstance hzInstance1, hzInstance2;
    private PortableID user1, user2;

    private HazelcastInstance createHzInstance() {
        Config config = new Config();
        DynamicPortableFactoryImpl dynamicPortableFactory = new DynamicPortableFactoryImpl();
        dynamicPortableFactory.register(new PortableIDFactory());
        dynamicPortableFactory.register(new PortableIDToOXExceptionMapFactory());
        dynamicPortableFactory.register(new TestPortableStanzaDispatcherFactory());
        dynamicPortableFactory.register(new PortableIDToOXExceptionMapEntryFactory());
        config.getSerializationConfig().addPortableFactory(DynamicPortableFactory.FACTORY_ID, dynamicPortableFactory);
        for(ClassDefinition cd : dynamicPortableFactory.getClassDefinitions()) {
            config.getSerializationConfig().addClassDefinition(cd);
        }
        return Hazelcast.newHazelcastInstance(config);
    }

    @Before
    public void setUp() throws Exception {
        hzInstance1 = createHzInstance();
        hzInstance2 = createHzInstance();
        user1= new PortableID("user1@context1");
        user2 = new PortableID("user2@context1");
    }

    @After
    public void tearDown() {
        hzInstance1.shutdown();
        hzInstance2.shutdown();
    }

    /**
     * Test serialization of {@link PortableIDToOXExceptionMap}s by returning empty and filled maps as return values from a different
     * hazelcast node via the distributed executor service.
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testIDMapDeserialization() throws InterruptedException, ExecutionException {
        IExecutorService hzExecutorService = hzInstance1.getExecutorService("default");

        Future<IDMap<OXException>> portableDispatcherFuture = null;
        IDMap<OXException> exceptionMap = null;

        portableDispatcherFuture = hzExecutorService.submitToMember(new TestPortableStanzaDispatcher(), hzInstance2.getCluster().getLocalMember());
        exceptionMap = portableDispatcherFuture.get();
        assertEquals(0, exceptionMap.size());

        portableDispatcherFuture = hzExecutorService.submitToMember(new TestPortableStanzaDispatcher(true, user1), hzInstance2.getCluster().getLocalMember());
        exceptionMap = portableDispatcherFuture.get();
        assertEquals(1, exceptionMap.size());
        assertNotNull(exceptionMap.get(user1));

        portableDispatcherFuture = hzExecutorService.submitToMember(new TestPortableStanzaDispatcher(true, user1, user2), hzInstance2.getCluster().getLocalMember());
        exceptionMap = portableDispatcherFuture.get();
        assertEquals(2, exceptionMap.size());
        assertNotNull(exceptionMap.get(user1));
        assertTrue(exceptionMap.get(user1) instanceof RealtimeException);
        assertNotNull(exceptionMap.get(user2));
        assertTrue(exceptionMap.get(user2) instanceof RealtimeException);
    }

}
