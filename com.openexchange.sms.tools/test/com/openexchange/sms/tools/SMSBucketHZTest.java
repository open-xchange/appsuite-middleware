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

package com.openexchange.sms.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sms.tools.internal.SMSBucket;
import com.openexchange.sms.tools.internal.SMSBucketServiceImpl;
import com.openexchange.sms.tools.osgi.Services;

/**
 * {@link SMSBucketHZTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
@RunWith(PowerMockRunner.class)
public class SMSBucketHZTest {

    @Mock
    private ConfigViewFactory factory;

    @Mock
    private ConfigView view;

    private static HazelcastInstance hz1;

    private static HazelcastInstance hz2;

    private static SMSBucketService smsBucketService;

    private static Session fake;

    @BeforeClass
    public static void initHazelcast() throws Exception {
        Config config = new Config();
        config.getSerializationConfig().addPortableFactory(CustomPortable.FACTORY_ID, new PortableFactory() {

            @Override
            public Portable create(int classId) {
                return new SMSBucket();
            }
        });
        // defaults from samlAuthnRequestInfos.properties
        MapConfig mapConfig = new MapConfig("smsBucketHZ");
        mapConfig.setBackupCount(1);
        mapConfig.setAsyncBackupCount(0);
        mapConfig.setReadBackupData(false);
        mapConfig.setMaxIdleSeconds(120000);
        config.addMapConfig(mapConfig);
        hz1 = Hazelcast.newHazelcastInstance(config);
        hz2 = Hazelcast.newHazelcastInstance(config);
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
    public void initSMSBucketService() throws OXException {

        Services.setServiceLookup(new ServiceLookup() {

            @SuppressWarnings("unchecked")
            @Override
            public <S> S getService(Class<? extends S> clazz) {
                return (S) factory;
            }

            @Override
            public <S> S getOptionalService(Class<? extends S> clazz) {
                return null;
            }
        });
        //        PowerMockito.when(Services.getService(ConfigViewFactory.class)).thenReturn(factory);
        PowerMockito.when(factory.getView(Matchers.anyInt(), Matchers.anyInt())).thenReturn(view);
        PowerMockito.when(view.get(SMSConstants.SMS_USER_LIMIT_ENABLED, boolean.class)).thenReturn(true);
        PowerMockito.when(view.get(SMSConstants.SMS_USER_LIMIT_REFRESH_INTERVAL, String.class)).thenReturn("2");
        PowerMockito.when(view.get(SMSConstants.SMS_USER_LIMIT_PROPERTY, String.class)).thenReturn("3");
        fake = PowerMockito.mock(Session.class, new Answer<Integer>() {

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return 1;
            }
        });

        smsBucketService = new SMSBucketServiceImpl(hz1);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSMSBucket() throws OXException {

        IMap<String, SMSBucket> map = hz2.getMap("SMS_Bucket");
        for (int x = 3; x > 0; x--) {
            assertEquals("Unexpected amount", x, smsBucketService.getSMSToken(fake));
            assertEquals("Map in second hazelcast instace has a wrong size", 1, map.size());
            assertTrue("Map in second hazelcast instace dont have a SMSBucket for the given user", map.containsKey("1/1"));
            if (x != 1) {
                assertEquals("", x - 1, map.get("1/1").removeToken(10));
            } else {
                assertEquals("", -1, map.get("1/1").removeToken(10));
            }
        }
        thrown.expect(OXException.class);
        thrown.expectMessage(CoreMatchers.containsString("You have exceeded the maximum number of sms allowed. Please try again after"));
        smsBucketService.getSMSToken(fake);
    }

}
