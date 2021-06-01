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

package com.openexchange.ajax.system;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.WhoAmIResponse;

/**
 * {@link WhoAmITest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.3
 */
public class WhoAmITest extends AbstractSystemTest {

    @Test
    public void testSystemWhoAmI_normalTest_ResponseAvailable() throws ApiException, OXException, IOException, JSONException {
        String sessionId = getSessionId();
        WhoAmIResponse response = api.whoami();
        assertNull(response.getData().getRandom());
        assertEquals(sessionId, response.getData().getSession());
        assertEquals(testUser.getUser(), response.getData().getUser());
        assertEquals(String.valueOf(testUser.getUserId()), response.getData().getUserId());
        assertEquals(I(getClient().getValues().getContextId()).toString(), response.getData().getContextId());
        assertNull(response.getData().getRequiresMultifactor());
        assertEquals(getClient().getValues().getLocale().toString(), response.getData().getLocale());

    }

    @Test
    public void testSystemWhoAmI_WrongSessionId_ResponseNull() throws ApiException {
        String old = api.getApiClient().getSession();
        try {
            api.getApiClient().setApiKey("abcdefghijklmnopqrxtuvwxyz");
            WhoAmIResponse response = api.whoami();
            assertNull(response.getData());
            assertTrue(response.getError().contains("session expired"));
        } finally {
            api.getApiClient().setApiKey(old);
        }
    }

}
