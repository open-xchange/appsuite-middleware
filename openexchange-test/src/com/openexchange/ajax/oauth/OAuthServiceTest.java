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

package com.openexchange.ajax.oauth;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractTestEnvironment;
import com.openexchange.ajax.framework.config.util.ChangePropertiesRequest;
import com.openexchange.ajax.oauth.actions.AllOAuthServicesRequest;
import com.openexchange.ajax.oauth.actions.GetOAuthServiceRequest;
import com.openexchange.ajax.oauth.actions.OAuthServicesResponse;
import com.openexchange.ajax.oauth.types.OAuthService;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.test.pool.TestContext;
import com.openexchange.test.common.test.pool.TestContextPool;

/**
 * Instances of com.openexchange.oauth.OAuthServiceMetaData should be invisible if their according
 * enable-property is set to 'false'. This property is visible via ConfigCascade. We test the implementation
 * 'com.openexchange.oauth.testservice' here. The service is enabled server-wide but will be disabled for client2
 * at user-scope.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class OAuthServiceTest extends AbstractTestEnvironment {

    private static final String TESTSERVICE = "com.openexchange.oauth.testservice";

    private TestContext testContext;
    private AJAXClient client1;
    private AJAXClient client2;

    @Before
    public void setUp() throws Exception {
        testContext = TestContextPool.acquireContext(getClass().getCanonicalName());
        client1 = testContext.acquireUser().getAjaxClient();
        client2 = testContext.acquireUser().getAjaxClient();

        Map<String, String> properties = Collections.singletonMap("com.openechange.oauth.testservice.enabled", "false");
        ChangePropertiesRequest changePropertiesRequest = new ChangePropertiesRequest(properties, "user", null);
        client2.execute(changePropertiesRequest);
    }

    @After
    public void tearDown() throws Exception {
        TestContextPool.backContext(testContext);
    }

    @Test
    public void testGetAllServices() throws OXException, IOException, JSONException {
        OAuthServicesResponse response = client1.execute(new AllOAuthServicesRequest());
        List<OAuthService> services = response.getServices();
        boolean found = false;
        for (OAuthService service : services) {
            if (TESTSERVICE.equals(service.getId())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue("Service is missing: '" + TESTSERVICE + "'", found);
    }

    @Test
    public void testGetTestService() throws OXException, IOException, JSONException {
        OAuthServicesResponse response = client1.execute(new GetOAuthServiceRequest(TESTSERVICE));
        List<OAuthService> services = response.getServices();
        Assert.assertEquals("Get response should contain exactly one service", 1, services.size());
        OAuthService service = services.get(0);
        Assert.assertEquals("Service is missing: '" + TESTSERVICE + "'", TESTSERVICE, service.getId());
    }

    @Test
    public void testGetAllServicesWithoutPermission() throws Exception {
        OAuthServicesResponse response = client2.execute(new AllOAuthServicesRequest());
        List<OAuthService> services = response.getServices();
        boolean found = false;
        for (OAuthService service : services) {
            if (TESTSERVICE.equals(service.getId())) {
                found = true;
                break;
            }
        }
        Assert.assertFalse("Service is present without permission: '" + TESTSERVICE + "'", found);
    }
}
