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

package com.openexchange.ajax.framework;

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.openexchange.exception.OXException;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.test.tryagain.TryAgain;
import com.openexchange.test.tryagain.TryAgainTestRule;

/**
 * {@link AbstractClientSession}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
@RunWith(ConcurrentTestRunner.class)
@Concurrent(count = 5)
public class AbstractClientSession {

    /** The test name for context acquisition */
    @Rule
    public final TestName name = new TestName();

    /** Declare 'try again' rule as public field to allow {@link TryAgain}-annotation for tests */
    @Rule
    public final TryAgainTestRule tryAgainRule = new TryAgainTestRule();

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {

        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractClientSession.class);
    }

    private AJAXClient client;
    protected TestContext testContext;
    protected List<TestContext> testContextList;
    protected TestUser admin;
    protected TestUser testUser;

    protected Map<TestContext, List<TestUser>> users = new HashMap<>();
    protected Map<TestUser, AJAXClient> users2client = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        ProvisioningSetup.init();
        TestConfig testConfig = getTestConfig();
        testContextList = TestContextPool.acquireContext(this.getClass().getCanonicalName() + "." + name.getMethodName(), optContextConfig(), testConfig.numberOfContexts);
        testContext = testContextList.get(0);
        Assert.assertNotNull("Unable to retrieve a context!", testContext);

        for (TestContext ctx : testContextList) {
            users.put(ctx, new ArrayList<TestUser>(testConfig.numberOfusersPerContext));
            for (int x = testConfig.numberOfusersPerContext; x > 0; x--) {

                TestUser user = ctx.acquireUser();
                users.get(ctx).add(user);
                if (testUser == null) {
                    testUser = user;
                }

                if (testConfig.createAjaxClients) {
                    users2client.put(user, generateClient(user));
                    if (client == null) {
                        client = users2client.get(user);
                    }
                }
            }
        }

        admin = testContext.getAdmin();
    }

    /**
     * Gets an optional map containing configurations for the context
     *
     * @return The optional map
     */
    public Optional<Map<String, String>> optContextConfig() {
        return Optional.empty();
    }

    protected TestUser getUser(int x) {
        return getUser(testContext, x);
    }

    protected TestUser getUser(TestContext ctx, int x) {
        Assert.assertThat(I(x), allOf(is(greaterThanOrEqualTo(I(0))), is(lessThan(I(users.get(ctx).size())))));
        return users.get(ctx).get(x);
    }

    /**
     * Gets a test config which describes the environment for the test. Test should override this method to adjust this to their own needs.
     * Defaults to 1 context, 1 userPerContext and builds both clients
     *
     * @return The Testconfig
     */
    public TestConfig getTestConfig() {
        return TestConfig.builder().createAjaxClient().createApiClient().build();
    }

    @After
    public void tearDown() throws Exception {
        try {
            for (AJAXClient client : users2client.values()) {
                logoutClient(client, true);
            }
        } finally {
            TestContextPool.backContext(testContextList);
        }
    }

    protected final AJAXClient getClient() {
        assertNotNull("Missing ajax client. Please check test config", client);
        return client;
    }

    /**
     * Gets the client with the given number from the first context
     *
     * @param x the client number whereby the default client is 0 and additional clients start with 1 and so forth
     * @return The {@link AJAXClient}
     */
    protected final AJAXClient getClient(int x) {
        return getClient(testContext, x);
    }

    /**
     * Gets the client with the given number from the given context
     *
     * @param ctx The test context
     * @param x the client number whereby the default client is 0 and additional clients start with 1 and so forth
     * @return The {@link AJAXClient}
     */
    protected final AJAXClient getClient(TestContext ctx, int x) {
        Assert.assertThat(I(x), allOf(is(greaterThanOrEqualTo(I(0))), is(lessThan(I(users.get(ctx).size())))));
        return users2client.get(users.get(ctx).get(x));
    }

    public final AJAXSession getSession() {
        assertNotNull("Missing ajax client. Please check config", client);
        return client.getSession();
    }

    /**
     * Does a logout for the client. Errors won't be logged.
     * Example:
     * <p>
     * <code>
     * client = logoutClient(client);
     * </code>
     * </p>
     *
     * @param client to logout
     * @return <code>null</code> to prepare client for garbage collection
     */
    protected final AJAXClient logoutClient(AJAXClient client) {
        return logoutClient(client, false);
    }

    /**
     * Does a logout for the client.
     * Example:
     * <p>
     * <code>
     * client = logoutClient(client, true);
     * </code>
     * </p>
     *
     * @param client to logout
     * @param loggin Whether to log an error or not
     * @return <code>null</code> to prepare client for garbage collection
     */
    protected final AJAXClient logoutClient(AJAXClient client, boolean loggin) {
        try {
            if (client != null) {
                client.logout();
            }
        } catch (Exception e) {
            if (loggin) {
                LoggerHolder.LOG.error("Unable to correctly tear down test setup.", e);
            }
        }
        return null;
    }

    /**
     * Generates a new {@link AJAXClient}. Uses standard client identifier.
     * Generated client needs a <b>logout in tearDown()</b>
     *
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final AJAXClient generateDefaultClient() throws OXException {
        return generateClient(getClientId());
    }

    /**
     * Generates a new {@link AJAXClient}.
     * Generated client needs a <b>logout in tearDown()</b>
     *
     * @param client The client identifier to use when performing a login
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final AJAXClient generateClient(String client) throws OXException {
        return generateClient(client, testContext.acquireUser());
    }

    /**
     * Generates a new {@link AJAXClient} for the {@link TestUser}.
     * Generated client needs a <b>logout in tearDown()</b>
     *
     * @param user The {@link TestUser} to create a client for
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final AJAXClient generateClient(TestUser user) throws OXException {
        return generateClient(getClientId(), user);
    }

    /**
     * Gets the client identifier to use when performing a login
     *
     * @return The client identifier or <code>null</code> to use default one (<code>"com.openexchange.ajax.framework.AJAXClient"</code>)
     */
    protected String getClientId() {
        return null;
    }

    /**
     * Generates a new {@link AJAXClient} for the {@link TestUser}.
     * Generated client needs a <b>logout in tearDown()</b>
     *
     * @param client The client identifier to use when performing a login
     * @param user The {@link TestUser} to create a client for
     * @return The new {@link AJAXClient}
     * @throws OXException In case no client could be created
     */
    protected final AJAXClient generateClient(String client, TestUser user) throws OXException {
        if (null == user) {
            LoggerHolder.LOG.error("Can only create a client for an valid user");
            throw new OXException();
        }
        AJAXClient newClient;
        try {
            if (null == client || client.isEmpty()) {
                newClient = new AJAXClient(user);
            } else {
                newClient = new AJAXClient(user, client);
            }
        } catch (Exception e) {
            LoggerHolder.LOG.error("Could not generate new client for user {} in context {}.", user.getUser(), user.getContext(), e);
            throw new OXException(e);
        }
        return newClient;
    }

    /**
     * {@link TestConfig}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.5
     */
    public static class TestConfig {

        int numberOfContexts;
        int numberOfusersPerContext;
        boolean createAjaxClients;
        boolean createApiClients;

        /**
         * Initializes a new {@link TestConfig}.
         *
         * @param numberOfContexts
         * @param numberOfusersPerContext
         * @param createAjaxClients
         * @param createApiClients
         */
        public TestConfig(int numberOfContexts, int numberOfusersPerContext, boolean createAjaxClients, boolean createApiClients) {
            super();
            this.numberOfContexts = numberOfContexts;
            this.numberOfusersPerContext = numberOfusersPerContext;
            this.createAjaxClients = createAjaxClients;
            this.createApiClients = createApiClients;
        }

        public static TestConfigBuilder builder() {
            return new TestConfigBuilder();
        }

        /**
         *
         * {@link TestConfigBuilder}
         *
         * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
         * @since v7.10.5
         */
        public static class TestConfigBuilder {

            private int numberOfContexts = 1;
            private int numberOfusersPerContext = 1;
            private boolean createAjaxClients = false;
            private boolean createApiClients = false;

            /**
             * Initializes a new {@link TestConfig}.
             *
             * @param numberOfContexts
             * @param numberOfusersPerContext
             * @param createAjaxClients
             * @param createApiClients
             */
            public TestConfigBuilder() {
                // empty constructor
            }

            public TestConfigBuilder withContexts(int x) {
                numberOfContexts = x;
                return this;
            }

            public TestConfigBuilder withUserPerContext(int x) {
                numberOfusersPerContext = x;
                return this;
            }

            public TestConfigBuilder createAjaxClient() {
                createAjaxClients = true;
                return this;
            }

            public TestConfigBuilder createApiClient() {
                createApiClients = true;
                return this;
            }

            public TestConfig build() {
                return new TestConfig(numberOfContexts, numberOfusersPerContext, createAjaxClients, createApiClients);
            }

        }

    }

}
