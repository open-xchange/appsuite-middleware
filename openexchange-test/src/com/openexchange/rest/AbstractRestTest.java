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

package com.openexchange.rest;

import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.ProvisioningSetup;
import com.openexchange.java.Charsets;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.test.pool.TestContext;
import com.openexchange.test.common.test.pool.TestContextPool;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.testing.restclient.invoker.ApiClient;

@RunWith(ConcurrentTestRunner.class)
@Concurrent(count = 10)
public abstract class AbstractRestTest extends JerseyTest {

    private AJAXClient ajaxClient1;
    private AJAXClient ajaxClient2;
    protected TestContext testContext;
    protected TestUser admin;
    protected TestUser testUser;
    protected TestUser testUser2;

    private ApiClient restClient;
    private TestUser restUser;

    private Object protocol;

    private Object hostname;

    protected final ApiClient getRestClient() {
        return restClient;
    }

    protected final AJAXClient getAjaxClient() {
        return ajaxClient1;
    }

    protected final AJAXClient getAjaxClient2() {
        return ajaxClient2;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        ProvisioningSetup.init();
    }

    @AfterClass
    public static void afterClass() {
        ProvisioningSetup.down();
    }

    @Override
    public void setUp() throws Exception {
        testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());
        Assert.assertNotNull("Unable to retrieve a context!", testContext);
        testUser = testContext.acquireUser();
        testUser2 = testContext.acquireUser();
        ajaxClient1 = testUser.getAjaxClient();
        ajaxClient2 = testUser2.getAjaxClient();
        admin = testContext.getAdmin();

        restClient = new ApiClient();
        restClient.setBasePath(getBasePath());
        restUser = TestContextPool.getRestUser();
        restClient.setUsername(restUser.getUser());
        restClient.setPassword(restUser.getPassword());
        String authorizationHeaderValue = "Basic " + Base64.encodeBase64String((restUser.getUser() + ":" + restUser.getPassword()).getBytes(Charsets.UTF_8));
        restClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
    }

    protected String getBasePath() {
        if (hostname == null) {
            this.hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        }

        if (protocol == null) {
            this.protocol = AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL);
            if (this.protocol == null) {
                this.protocol = "http";
            }
        }
        return this.protocol + "://" + this.hostname + ":8009";
    }

    @Override
    public void tearDown() throws Exception {
        try {
            TestContextPool.backContext(testContext);
        } catch (Exception e) {
            LoggerFactory.getLogger(AbstractRestTest.class).error("Unable to correctly tear down test setup.", e);
        } finally {
            super.tearDown();
        }
    }
}
