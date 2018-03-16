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

    // Blacklist is set in cook books
    private static final String BLACKLISTED_CLIENT = "randomClientForTesting";

    @Test
    public void testRemoveAllOtherSessions() throws Exception {
        String sessionId = apiClient.getSession();

        getApi().clear(sessionId);
        AllSessionsResponse response = getApi().all(sessionId);
        Collection<SessionManagementData> sessions = response.getData();
        Assert.assertThat("Not all clients has been removed", new Integer(1), is(Integer.valueOf(sessions.size())));

        for (SessionManagementData session : sessions) {
            Assert.assertThat("Wrong client is still loged in", sessionId, is(session.getSessionId()));
        }

    }

    @Test
    public void testRemoveAllOtherSessionsWithoutBlacklisted() throws Exception {
        // Third client
        String sessionId = apiClient.getSession();

        AllSessionsResponse response = getApi().all(sessionId);
        Collection<SessionManagementData> sessions = response.getData();

        // Should not see blacklisted client
        Assert.assertThat("Blacklisted client is visible!", Integer.valueOf(sessions.size()), is(Integer.valueOf(2)));

        getApi().clear(sessionId);

        response = getApi().all(sessionId);
        sessions = response.getData();

        // Should not see blacklisted client, client2 should have been loged out
        Assert.assertThat("Not all clients has been removed", Integer.valueOf(sessions.size()), is(Integer.valueOf(1)));

        for (SessionManagementData session : sessions) {
            Assert.assertThat("Wrong client is still loged in", sessionId, is(session.getSessionId()));
        }
    }
}
