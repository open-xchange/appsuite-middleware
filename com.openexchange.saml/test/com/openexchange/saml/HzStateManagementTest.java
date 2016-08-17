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
    public static void initHazelcast() throws Exception {
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
