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

package com.openexchange.ajax.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.MessagingAccountData;
import com.openexchange.testing.httpclient.models.MessagingAccountUpdateResponse;
import com.openexchange.testing.httpclient.modules.MessagingApi;

/**
 * {@link RssMessagingBlacklistTest} checks whether the rss blacklist is properly working for messaging accounts
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class RssMessagingBlacklistTest extends AbstractAPIClientSession {

    private MessagingApi messagingApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        messagingApi = new MessagingApi(getApiClient());
    }

    @Test
    public void testLocalURI() throws JSONException, ApiException {
        JSONObject config = new JSONObject();
        config.put("url", "http://localhost:22/");
        MessagingAccountData data = new MessagingAccountData();
        data.setMessagingService("com.openexchange.messaging.rss");
        data.setDisplayName(RssMessagingBlacklistTest.class.getSimpleName() + "_" + System.currentTimeMillis());

        data.setConfiguration(config);
        MessagingAccountUpdateResponse resp = messagingApi.createMessagingAccount(data);
        try {
            assertNotNull(resp.getError());
            assertEquals("Wrong exception: " + resp.getErrorDesc(), MessagingExceptionCodes.INVALID_ACCOUNT_CONFIGURATION.create().getErrorCode(), resp.getCode());
        } catch (AssertionError e) {
            if (resp.getData() != null) {
                messagingApi.deleteMessagingAccount("com.openexchange.messaging.rss", resp.getData());
            }
            throw e;
        }
    }
}
