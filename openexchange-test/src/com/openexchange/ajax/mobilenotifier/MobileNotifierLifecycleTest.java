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

package com.openexchange.ajax.mobilenotifier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mobilenotifier.actions.MobileNotifierSubscribeRequest;
import com.openexchange.ajax.mobilenotifier.actions.MobileNotifierUnsubscribeRequest;
import com.openexchange.ajax.mobilenotifier.actions.MobileNotifierUpdateTokenRequest;

/**
 * {@link MobileNotifierLifecycleTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifierLifecycleTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link MobileNotifierLifecycleTest}.
     * 
     * @param name
     */
    public MobileNotifierLifecycleTest() {
        super();
    }

    @Test
    public void testSubscriptionLifecycle() throws Exception {
        String startToken = "ADE9219010FD3DEAD9211384129";
        String serviceId = "gcm";
        String providerId = "mail";
        String newToken = "ACCCDDDDEEEAAA000111155";

        MobileNotifierSubscribeRequest msReq = new MobileNotifierSubscribeRequest(serviceId, providerId, startToken, true);
        AbstractAJAXResponse msResp = getClient().execute(msReq);
        assertNotNull(msResp);
        assertEmptyJson(msResp);

        MobileNotifierUpdateTokenRequest utReq = new MobileNotifierUpdateTokenRequest(serviceId, startToken, newToken, true);
        msResp = null;
        msResp = getClient().execute(utReq);
        assertNotNull(msResp);
        assertEmptyJson(msResp);

        MobileNotifierUnsubscribeRequest musReq = new MobileNotifierUnsubscribeRequest(serviceId, providerId, newToken, true);
        msResp = null;
        msResp = getClient().execute(musReq);
        assertNotNull(msResp);
        assertEmptyJson(msResp);

    }

    private void assertEmptyJson(AbstractAJAXResponse resp) {
        if (resp.getData() instanceof JSONObject) {
            JSONObject jObj = (JSONObject) resp.getData();
            assertTrue("Returning JSON data is not empty: " + jObj.toString(), jObj.isEmpty());
        } else {
            assertFalse("Returning data is not a json response", true);
        }

    }
}
