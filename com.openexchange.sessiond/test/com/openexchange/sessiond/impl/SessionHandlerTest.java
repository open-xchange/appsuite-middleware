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

package com.openexchange.sessiond.impl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.service.event.EventAdmin;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.serialization.PortableSessionFilterApplier;
import com.openexchange.threadpool.SimFactory;


/**
 * {@link SessionHandlerTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SessionHandlerTest {

    private static final String[] PROP_NAMES = new String[] { "com.openexchange.remote.property1", "com.openexchange.remote.property2", "com.openexchange.remote.property3" };

    private static HazelcastInstance hz1;

    private static HazelcastInstance hz2;

    @BeforeClass
    public static void initHazelcast() throws Exception {
        Config config = new Config();
        config.getSerializationConfig().addPortableFactory(CustomPortable.FACTORY_ID, new PortableFactory() {
            @Override
            public Portable create(int classId) {
                return new PortableSessionFilterApplier();
            }
        });
        hz1 = Hazelcast.newHazelcastInstance(config);
        hz2 = Hazelcast.newHazelcastInstance(config);

        ServiceRegistry serviceLookup = new ServiceRegistry();
        serviceLookup.addService(EventAdmin.class, Mockito.mock(EventAdmin.class));
        serviceLookup.addService(HazelcastInstance.class, hz1);
        com.openexchange.sessiond.osgi.Services.setServiceLookup(serviceLookup);
    }

    @AfterClass
    public static void shutdownHazelcast() {
        if (hz1 != null) {
            hz1.shutdown();
        }
        if (hz2 != null) {
            hz2.shutdown();
        }
    }

    @Before
    public void initSessionHandler() throws Exception {
        SessionHandler.init(new SessiondConfigInterface() {
            @Override
            public boolean isAutoLogin() {
                return false;
            }

            @Override
            public boolean isAsyncPutToSessionStorage() {
                return false;
            }

            @Override
            public long getSessionContainerTimeout() {
                return 1000l;
            }

            @Override
            public long getLongTermSessionContainerTimeout() {
                return 10000l;
            }

            @Override
            public List<String> getRemoteParameterNames() {
                return Arrays.asList(PROP_NAMES);
            }

            @Override
            public long getRandomTokenTimeout() {
                return 100l;
            }

            @Override
            public String getObfuscationKey() {
                return "12345";
            }

            @Override
            public int getNumberOfSessionContainers() {
                return 2;
            }

            @Override
            public int getNumberOfLongTermSessionContainers() {
                return 2;
            }

            @Override
            public int getMaxSessionsPerUser() {
                return 0;
            }

            @Override
            public int getMaxSessionsPerClient() {
                return 0;
            }

            @Override
            public int getMaxSessions() {
                return 0;
            }

            @Override
            public long getLongLifeTime() {
                return 0;
            }

            @Override
            public long getLifeTime() {
                return 0;
            }
        });
    }

    @Before
    public void initServices() throws Exception {
        SessionHandler.addTimerService(SimFactory.newTimerService());
        SessionHandler.addThreadPoolService(SimFactory.newThreadPoolService());
    }

    @After
    public void closeSessionHandler() {
        SessionHandler.close();
    }

    @Test
    public void testSessionRotation() throws Exception {
        SessionImpl session = addSession();
        Assert.assertNotNull(SessionHandler.getSession(session.getSessionID(), false));
        Thread.sleep(SessionHandler.config.getNumberOfSessionContainers() * SessionHandler.config.getLifeTime() + SessionHandler.config.getNumberOfLongTermSessionContainers() * SessionHandler.config.getLongLifeTime() + 2000);
        Assert.assertNull(SessionHandler.getSession(session.getSessionID(), false));
    }

    @Test
    public void testFindLocalSessions() throws Exception {
        String v1 = "thevalue";
        String v2 = "othervalue";
        SessionImpl s1 = addSession(v1);
        SessionImpl s2 = addSession(v2);
        addSession();
        Assert.assertEquals(3, SessionHandler.getSessions().size());
        List<String> sessions = SessionHandler.findLocalSessions(SessionFilter.create("(" + PROP_NAMES[0] + "=" + v1 + ")"));
        Assert.assertEquals(1, sessions.size());
        Assert.assertEquals(s1.getSessionID(), sessions.get(0));

        sessions = SessionHandler.findLocalSessions(SessionFilter.create("(|(" + PROP_NAMES[0] + "=" + v1 + ")(" + PROP_NAMES[0] + "=" + v2 + "))"));
        Assert.assertEquals(2, sessions.size());
        Assert.assertTrue(sessions.contains(s1.getSessionID()) && sessions.contains(s2.getSessionID()));
    }

    @Test
    public void testRemoveLocalSessions() throws Exception {
        String v1 = "thevalue";
        String v2 = "othervalue";
        SessionImpl s1 = addSession(v1);
        SessionImpl s2 = addSession(v2);
        addSession();
        Assert.assertEquals(3, SessionHandler.getSessions().size());
        List<String> sessions = SessionHandler.removeLocalSessions(SessionFilter.create("(" + PROP_NAMES[0] + "=" + v1 + ")"));
        Assert.assertEquals(1, sessions.size());
        Assert.assertEquals(s1.getSessionID(), sessions.get(0));
        Assert.assertEquals(2, SessionHandler.getSessions().size());

        sessions = SessionHandler.removeLocalSessions(SessionFilter.create("(|(" + PROP_NAMES[0] + "=" + v1 + ")(" + PROP_NAMES[0] + "=" + v2 + "))"));
        Assert.assertEquals(1, sessions.size());
        Assert.assertEquals(1, SessionHandler.getSessions().size());
        Assert.assertEquals(s2.getSessionID(), sessions.get(0));
    }

    @Test
    public void testFindRemoteSessions() throws Exception {
        String v1 = "thevalue";
        String v2 = "othervalue";
        SessionImpl s1 = addSession(v1);
        SessionImpl s2 = addSession(v2);
        addSession();
        Assert.assertEquals(3, SessionHandler.getSessions().size());
        List<String> sessions = SessionHandler.findRemoteSessions(SessionFilter.create("(" + PROP_NAMES[0] + "=" + v1 + ")"));
        Assert.assertEquals(1, sessions.size());
        Assert.assertEquals(s1.getSessionID(), sessions.get(0));

        sessions = SessionHandler.findRemoteSessions(SessionFilter.create("(|(" + PROP_NAMES[0] + "=" + v1 + ")(" + PROP_NAMES[0] + "=" + v2 + "))"));
        Assert.assertEquals(2, sessions.size());
        Assert.assertTrue(sessions.contains(s1.getSessionID()) && sessions.contains(s2.getSessionID()));
    }

    private static SessionImpl addSession() throws OXException {
        return addSession((String[]) null);
    }

    private static SessionImpl addSession(final String... props) throws OXException {
        if (props == null) {
            return SessionHandler.addSession(1, "user", "secret", 1, "", "user", UUID.randomUUID().toString(), "5433", "TestClient", null, false, null);
        }

        return SessionHandler.addSession(1, "user", "secret", 1, "", "user", UUID.randomUUID().toString(), "5433", "TestClient", null, false, new SessionEnhancement() {
            @Override
            public void enhanceSession(Session session) {
                for (int i = 0; i < props.length; i++) {
                    session.setParameter(PROP_NAMES[i], props[i]);
                }
            }
        });
    }

}
