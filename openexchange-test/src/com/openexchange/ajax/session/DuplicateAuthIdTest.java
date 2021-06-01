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

package com.openexchange.ajax.session;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractTestEnvironment;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.exception.OXException;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.test.common.test.pool.TestContext;
import com.openexchange.test.common.test.pool.TestContextPool;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * Checks if the server detects correctly a duplicate used authId.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DuplicateAuthIdTest extends AbstractTestEnvironment {

    private String sameAuthId;

    private AJAXClient client1;

    private String login1;

    private String password1;

    private AJAXSession session2;

    private AJAXClient client2;

    private String login2;

    private String password2;

    private TestContext testContext;

    @Before
    public void setUp() throws Exception {
        testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());
        TestUser testUser = testContext.acquireUser();

        sameAuthId = LoginTools.generateAuthId();
        final AJAXSession session1 = new AJAXSession();
        client1 = new AJAXClient(session1, false); // explicit logout in tearDown
        login1 = testUser.getLogin();
        password1 = testUser.getPassword();
        LoginResponse response = client1.execute(new LoginRequest(login1, password1, sameAuthId, LoginTest.class.getName(), "6.15.0"));

        session1.setId(response.getSessionId());
        session2 = new AJAXSession();
        session2.getConversation().putCookie("JSESSIONID", session1.getConversation().getCookieValue("JSESSIONID"));
        client2 = new AJAXClient(session2, false); // explicit logout in test method

        TestUser testUser2 = testContext.acquireUser();
        login2 = testUser2.getLogin();
        password2 = testUser2.getPassword();
    }

    @After
    public void tearDown() throws Exception {
        TestContextPool.backContext(testContext);
    }

    @Test
    public void testDuplicateAuthId() throws Throwable {
        LoginResponse response = client2.execute(new LoginRequest(login2, password2, sameAuthId, LoginTest.class.getName(), "6.15.0", false));
        if (!response.hasError()) {
            session2.setId(response.getSessionId());
            client2.logout();
            fail("Duplicate authId not detected.");
        } else {
            OXException e = response.getException();
            OXException se = SessionExceptionCodes.DUPLICATE_AUTHID.create(login1, login2);
            assertTrue("Found wrong exception.", se.similarTo(e));
        }
    }
}
