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

package com.openexchange.rest;

import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.ProvisioningSetup;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.java.Charsets;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
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

    @Override
    public void setUp() throws Exception {
        ProvisioningSetup.init();

        testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());
        Assert.assertNotNull("Unable to retrieve a context!", testContext);
        testUser = testContext.acquireUser();
        testUser2 = testContext.acquireUser();
        ajaxClient1 = new AJAXClient(testUser);
        ajaxClient2 = new AJAXClient(testUser2);
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
            if (ajaxClient1 != null) {
                // Client can be null if setUp() fails
                ajaxClient1.logout();
                ajaxClient1 = null;
            }
            if (ajaxClient2 != null) {
                // Client can be null if setUp() fails
                ajaxClient2.logout();
                ajaxClient2 = null;
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AbstractRestTest.class).error("Unable to correctly tear down test setup.", e);
        } finally {
            TestContextPool.backContext(testContext);
            super.tearDown();
        }
    }
}
