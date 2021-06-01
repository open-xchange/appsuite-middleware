/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import com.openexchange.session.DefaultSessionAttributes;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.impl.usertype.UserTypeSessiondConfigInterface;
import com.openexchange.sessiond.impl.usertype.UserTypeSessiondConfigRegistry;
import com.openexchange.sessiond.impl.usertype.UserTypeSessiondConfigRegistry.UserType;
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

    @Mock
    private UserTypeSessiondConfigRegistry registry;

    private SessiondConfigInterface config;

    @BeforeClass
    public static void initHazelcast() {
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
        MockitoAnnotations.initMocks(this);

        UserTypeSessiondConfigInterface sessiondConfigInterface = new UserTypeSessiondConfigInterface() {

            @Override
            public int getMaxSessionsPerUserType() {
                return 0;
            }

            @Override
            public UserType getUserType() {
                return UserType.USER;
            }
        };
        Mockito.when(registry.getConfigFor(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(sessiondConfigInterface);
        config = new SessiondConfigInterface() {

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

            @Override
            public boolean isRemoveFromSessionStorageOnTimeout() {
                return true;
            }
        };
        SessionHandler.init(config, registry);
    }

    @Before
    public void initServices() {
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
        Thread.sleep(config.getNumberOfSessionContainers() * config.getLifeTime() + config.getNumberOfLongTermSessionContainers() * config.getLongLifeTime() + 2000);
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

    @Test
    public void testRemoteParameterRoundtrip() throws Exception {
        String v1 = UUID.randomUUID().toString();
        SessionImpl s1 = addSession(v1);
        Assert.assertEquals(1, SessionHandler.getSessions().size());
        SessionHandler.setSessionAttributes(s1, DefaultSessionAttributes.builder().withLocalIp("172.16.33.66").build());
        List<String> sessions = SessionHandler.findRemoteSessions(SessionFilter.create("(" + PROP_NAMES[0] + "=" + v1 + ")"));
        Assert.assertEquals(1, sessions.size());
        Assert.assertEquals(s1.getSessionID(), sessions.get(0));
    }

    private static SessionImpl addSession() throws OXException {
        return addSession((String[]) null);
    }

    private static SessionImpl addSession(final String... props) throws OXException {
        if (props == null) {
            return SessionHandler.addSession(1, "user", "secret", 1, "", "user", UUID.randomUUID().toString(), "5433", "TestClient", null, false, false, null, null, "default-user-agent");
        }

        return SessionHandler.addSession(1, "user", "secret", 1, "", "user", UUID.randomUUID().toString(), "5433", "TestClient", null, false, false, null, Arrays.asList(new SessionEnhancement() {


            @SuppressWarnings("synthetic-access")
            @Override
            public void enhanceSession(Session session) {
                for (int i = 0; i < props.length; i++) {
                    session.setParameter(PROP_NAMES[i], props[i]);
                }
            }
        }), "default-user-agent");
    }

}
