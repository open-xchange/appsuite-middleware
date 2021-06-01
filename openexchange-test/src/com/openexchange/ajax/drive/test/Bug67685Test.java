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

package com.openexchange.ajax.drive.test;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.modules.DriveApi;

/**
 * {@link Bug67685Test}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.3
 */
public class Bug67685Test extends AbstractAPIClientSession {

    DriveApi api;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        api = new DriveApi(getApiClient());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().build();
    }

    @Test
    public void testDriveSubscribeActionGet_ModeInvalid_ErrorResponseWithoutException() throws OXException, IOException, JSONException, ApiException {
        String sessionId = getSessionId();
        Integer folderId = I(getClient().getValues().getPrivateTaskFolder());

        CommonResponse response = api.subscribePushEventsGetReq(sessionId, folderId.toString(), "gcm", "foobar", "invalid");
        assertEquals(2, response.getErrorParams().size());
        assertEquals("Invalid parameter \"mode\": invalid", response.getErrorStack().get(0));
    }

    @Test
    public void testDriveSubscribeActionGet_ModeValid_ErrorResponse() throws OXException, IOException, JSONException, ApiException {
        String sessionId = getSessionId();
        Integer folderId = I(getClient().getValues().getPrivateTaskFolder());

        CommonResponse response = api.subscribePushEventsGetReq(sessionId, folderId.toString(), "gcm", "foobar", "default");
        assertNull(response.getErrorId());
        assertEquals(0, response.getErrorParams().size());
    }
}
