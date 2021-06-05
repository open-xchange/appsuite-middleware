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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import org.junit.Test;
import com.openexchange.ajax.sessionmanagement.AbstractSessionManagementTest;
import com.openexchange.testing.httpclient.models.AllSessionsResponse;
import com.openexchange.testing.httpclient.models.SessionManagementData;

/**
 * {@link GetSessionsTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class GetSessionsTest extends AbstractSessionManagementTest {

    @Test
    public void testGetSessions() throws Exception {
        AllSessionsResponse response = getApi().all();
        Collection<SessionManagementData> sessions = response.getData();
        assertEquals(2, sessions.size());
        for (SessionManagementData session : sessions) {
            String sessionId = session.getSessionId();
            assertTrue(sessionId.equals(getSessionId()) || sessionId.equals(apiClient2.getSession()));
        }
    }

}
