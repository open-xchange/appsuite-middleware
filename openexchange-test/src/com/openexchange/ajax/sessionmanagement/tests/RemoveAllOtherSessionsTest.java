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

package com.openexchange.ajax.sessionmanagement.tests;

import static org.hamcrest.Matchers.is;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.sessionmanagement.AbstractSessionManagementTest;
import com.openexchange.testing.httpclient.models.AllSessionsResponse;
import com.openexchange.testing.httpclient.models.SessionManagementData;

/**
 * {@link RemoveAllOtherSessionsTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class RemoveAllOtherSessionsTest extends AbstractSessionManagementTest {

    @Test
    public void testRemoveAllOtherSessions() throws Exception {
        String sessionId = getSessionId();

        getApi().clear();
        AllSessionsResponse response = getApi().all();
        Collection<SessionManagementData> sessions = response.getData();
        Assert.assertThat("Not all clients has been removed", new Integer(1), is(Integer.valueOf(sessions.size())));

        for (SessionManagementData session : sessions) {
            Assert.assertThat("Wrong client is still loged in", sessionId, is(session.getSessionId()));
        }

    }

    @Test
    public void testRemoveAllOtherSessionsWithoutBlacklisted() throws Exception {
        // Third client
        String sessionId = getSessionId();

        AllSessionsResponse response = getApi().all();
        Collection<SessionManagementData> sessions = response.getData();

        // Should not see blacklisted client
        Assert.assertThat("Blacklisted client is visible!", Integer.valueOf(sessions.size()), is(Integer.valueOf(2)));

        getApi().clear();

        response = getApi().all();
        sessions = response.getData();

        // Should not see blacklisted client, client2 should have been loged out
        Assert.assertThat("Not all clients has been removed", Integer.valueOf(sessions.size()), is(Integer.valueOf(1)));

        for (SessionManagementData session : sessions) {
            Assert.assertThat("Wrong client is still loged in", sessionId, is(session.getSessionId()));
        }
    }
}
