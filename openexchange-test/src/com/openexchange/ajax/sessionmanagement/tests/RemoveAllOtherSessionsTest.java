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

package com.openexchange.ajax.sessionmanagement.tests;

import java.io.IOException;
import java.util.Collection;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.sessionmanagement.AbstractSessionManagementTest;
import com.openexchange.ajax.sessionmanagement.actions.GetSessionsRequest;
import com.openexchange.ajax.sessionmanagement.actions.GetSessionsResponse;
import com.openexchange.ajax.sessionmanagement.actions.RemoveAllOtherSessionsRequest;
import com.openexchange.ajax.sessionmanagement.actions.RemoveAllOtherSessionsResponse;
import com.openexchange.exception.OXException;
import com.openexchange.session.management.ManagedSession;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * {@link RemoveAllOtherSessionsTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class RemoveAllOtherSessionsTest extends AbstractSessionManagementTest {

    private static final String BLACKLISTED_CLIENT = "randomClientForTesting";

    @SuppressWarnings("unused")
    private AJAXClient testClient3;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testClient3 = new AJAXClient(testUser2, "anotherClient");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        // Logout only client2. Client 1 & 3 are being removed by the request and can not logout
        saveLogout(testClient2);
    }

    @Test
    public void testRemoveAllOtherSessions() throws Exception {
        String sessionId = testClient2.getSession().getId();
        RemoveAllOtherSessionsRequest request = new RemoveAllOtherSessionsRequest(sessionId);
        RemoveAllOtherSessionsResponse response = testClient2.execute(request);

        Assert.assertFalse(response.hasError());

        GetSessionsRequest getRequest = new GetSessionsRequest();
        GetSessionsResponse getResponse = testClient2.execute(getRequest);
        Assert.assertFalse(getResponse.hasError());
        Collection<ManagedSession> sessions = getResponse.getSessions();
        Assert.assertThat("Not all clients has been removed", 1, is(sessions.size()));

        for (ManagedSession session : sessions) {
            Assert.assertThat("Wrong client is still loged in", sessionId, is(session.getSessionId()));
        }

    }

    @Test
    public void testRemoveAllOtherSessionsWithoutBlacklisted() throws Exception {
        AJAXClient blackListed = null;
        try {
            blackListed = new AJAXClient(testUser2, BLACKLISTED_CLIENT);

            String sessionId = testClient2.getSession().getId();
            RemoveAllOtherSessionsRequest request = new RemoveAllOtherSessionsRequest(sessionId);
            RemoveAllOtherSessionsResponse response = testClient2.execute(request);

            Assert.assertFalse(response.hasError());

            GetSessionsRequest getRequest = new GetSessionsRequest();
            GetSessionsResponse getResponse = testClient2.execute(getRequest);
            Assert.assertFalse(getResponse.hasError());
            Collection<ManagedSession> sessions = getResponse.getSessions();
            Assert.assertThat("Not the excpected count of active clients", 2, is(sessions.size()));

            for (ManagedSession session : sessions) {
                if (sessionId.equals(session.getSessionId()) || blackListed.getSession().getId().equals(session.getSessionId())) {
                    continue;
                }
                fail("Wrong session still logged in");
            }

        } finally {
            saveLogout(blackListed);
        }

    }

    private void saveLogout(AJAXClient client) throws OXException, IOException, JSONException {
        // Try logout blacklisted
        if (null != client && null != client.getSession() && null != client.getSession().getId()) {
            client.logout();
        }
    }
}
