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

package com.openexchange.saml;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.impl.hz.HzStateManagement;
import com.openexchange.saml.impl.hz.PortableAuthnRequestInfo;
import com.openexchange.saml.impl.hz.PortableLogoutRequestInfo;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.DefaultAuthnRequestInfo;


/**
 * {@link HzStateManagementTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class HzStateManagementTest {

    private static HazelcastInstance hz1;

    private static HazelcastInstance hz2;

    private HzStateManagement stateManagement;

    @BeforeClass
    public static void initHazelcast() {
        Config config = new Config();
        config.getSerializationConfig().addPortableFactory(CustomPortable.FACTORY_ID, new PortableFactory() {
            @Override
            public Portable create(int classId) {
                if (classId == CustomPortable.PORTABLE_SAML_AUTHN_REQUEST_INFO) {
                    return new PortableAuthnRequestInfo();
                }

                return new PortableLogoutRequestInfo();
            }
        });
        // defaults from samlAuthnRequestInfos.properties
        MapConfig mapConfig = new MapConfig("samlAuthnRequestInfos-1");
        mapConfig.setBackupCount(1);
        mapConfig.setAsyncBackupCount(0);
        mapConfig.setReadBackupData(true);
        mapConfig.setMaxIdleSeconds(3600);
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
    public void initStateManagement() {
        stateManagement = new HzStateManagement(hz1);
    }

     @Test
     public void testAuthRequestInfoPortable() throws Exception {
        String requestId = UUIDs.getUnformattedString(UUID.randomUUID());
        String domainName = "example.com";
        String loginPath = "/appsuite/";
        String clientID = "open-xchange-appsuite";
        DefaultAuthnRequestInfo requestInfo = new DefaultAuthnRequestInfo();
        requestInfo.setRequestId(requestId);
        requestInfo.setDomainName(domainName);
        requestInfo.setLoginPath(loginPath);
        requestInfo.setClientID(clientID);
        String state = stateManagement.addAuthnRequestInfo(requestInfo, 1, TimeUnit.MINUTES);

        // reload and check values
        AuthnRequestInfo reloaded = stateManagement.removeAuthnRequestInfo(state);
        Assert.assertNotNull(reloaded);
        Assert.assertEquals(requestId, reloaded.getRequestId());
        Assert.assertEquals(domainName, reloaded.getDomainName());
        Assert.assertEquals(loginPath, reloaded.getLoginPath());
        Assert.assertEquals(clientID, reloaded.getClientID());

        // assert removed
        reloaded = stateManagement.removeAuthnRequestInfo(state);
        Assert.assertNull(reloaded);
    }

}
