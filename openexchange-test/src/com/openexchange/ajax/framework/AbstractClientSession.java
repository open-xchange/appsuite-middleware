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

package com.openexchange.ajax.framework;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.pool.TestContext;
import com.openexchange.test.common.test.pool.TestContextPool;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * {@link AbstractClientSession}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
@RunWith(ConcurrentTestRunner.class)
@Concurrent(count = 5)
public class AbstractClientSession extends AbstractTestEnvironment {

    protected TestContext testContext;
    protected List<TestContext> testContextList;
    protected TestUser admin;
    protected TestUser testUser;
    protected TestUser testUser2;

    @Before
    public void setUp() throws Exception {
        TestClassConfig testConfig = getTestConfig();
        testContextList = TestContextPool.acquireContext(this.getClass().getCanonicalName() + "." + name.getMethodName(), optContextConfig(), testConfig.getNumberOfContexts());
        testContext = testContextList.get(0);
        Assert.assertNotNull("Unable to retrieve a context!", testContext);

        for (TestContext ctx : testContextList) {
            ctx.configure(testConfig);
        }

        admin = testContext.getAdmin();
        testUser = testContext.acquireUser();
        testUser2 = testContext.acquireUser();
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (testContextList != null) {
                for (TestContext ctx : testContextList) {
                    ctx.cleanUp();
                }
            }
        } finally {
            TestContextPool.backContext(testContextList);
        }
    }

    /**
     * Gets an optional map containing configurations for the context
     *
     * @return The optional map
     */
    public Optional<Map<String, String>> optContextConfig() {
        return Optional.empty();
    }

    /**
     * Gets a test config which describes the environment for the test. Test should override this method to adjust this to their own needs.
     * Defaults to 1 context, 1 userPerContext and builds both clients
     *
     * @return The Testconfig
     */
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().build();
    }

    /**
     * Get the {@link AJAXClient} for the {@link #testUser}
     * <p>
     * Fails if client can't be get
     *
     * @return The client
     */
    protected AJAXClient getClient() {
        try {
            return testUser.getAjaxClient();
        } catch (OXException | IOException | JSONException e) {
            fail(e.getMessage());
        }
        return null;
    }

    /**
     * Gets the session for {@link #getClient()}
     *
     * @return The {@link AJAXSession}
     */
    protected AJAXSession getSession() {
        return getClient().getSession();
    }

}
