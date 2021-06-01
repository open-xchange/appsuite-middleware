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

package com.openexchange.ajax.user.me;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.testing.httpclient.models.CurrentUserData;
import com.openexchange.testing.httpclient.models.CurrentUserResponse;
import com.openexchange.testing.httpclient.modules.UserMeApi;


/**
 * {@link MeTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class MeTest extends AbstractAPIClientSession {

    /**
     * Tests that the following response data is orderly returned:
     *
     * <pre>
     * {
     *   "data": {
     *     "context_id": 1,
     *     "user_id": 3,
     *     "context_admin": 2,
     *     "login_name": "peter",
     *     "display_name": "Peter",
     *     "mail_login": "peter@example.com"
     *     "email_address": "peter@example.com",
     *     "email_aliases": [
     *       "peter@example.com"
     *     ],
     *   },
     *   "timestamp": 1583243886037
     * }
     * </pre>
     */
    @Test
    public void testGet() throws Exception {
        UserMeApi api = new UserMeApi(getApiClient());
        CurrentUserResponse response = api.getCurrentUser();
        CurrentUserData me = response.getData();
        assertEquals("Missing or wrong user_id", testUser.getUserId(), me.getUserId().intValue());
        assertEquals("Missing or wrong context_id", testContext.getId(), me.getContextId().intValue());
        assertNotNull("Missing context_admin", me.getContextAdmin());
        assertNotNull("Missing login_name", me.getLoginName());
        assertNotNull("Missing display_name", me.getDisplayName());
        assertNotNull("Missing mail_login", me.getMailLogin());
        assertEquals("Missing or wrong email_address", testUser.getLogin(), me.getEmailAddress());
        assertNotNull("Missing email_aliases", me.getEmailAliases());
        assertTrue("Missing primary address in email_aliases", me.getEmailAliases().stream().anyMatch(a -> {
            try {
                return a.equals(testUser.getLogin());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }));
    }

}
