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

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.openexchange.exception.OXException;
import com.openexchange.test.TestClassConfig;
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

    protected TestContext testContext;
    protected List<TestContext> testContextList;
    protected TestUser admin;
    protected TestUser testUser;
    protected TestUser testUser2;

    @Before
    public void setUp() throws Exception {
        ProvisioningSetup.init();
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
            if(testContextList != null) {
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
