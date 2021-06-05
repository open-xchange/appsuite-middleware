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

package com.openexchange.ajax.user;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.user.actions.GetAttributeRequest;
import com.openexchange.ajax.user.actions.GetAttributeResponse;
import com.openexchange.ajax.user.actions.SetAttributeRequest;
import com.openexchange.ajax.user.actions.SetAttributeResponse;
import com.openexchange.test.common.tools.RandomString;

/**
 * {@link Bug26431Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug26431Test extends AbstractAJAXSession {

    private static final String ATTRIBUTE_NAME = "testForBug26431";

    private AJAXClient client;
    private int userId;

    public Bug26431Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId = client.getValues().getUserId();
    }

    @Test
    public void testOmittedValue() throws Exception {
        String value = RandomString.generateChars(64);
        SetAttributeResponse response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, value, false));
        assertTrue("Setting a test attribute failed.", response.isSuccess());
        response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, null, false));
        assertTrue("Removing the attribute failed.", response.isSuccess());
        GetAttributeResponse response2 = client.execute(new GetAttributeRequest(userId, ATTRIBUTE_NAME));
        assertNull("Removing the attribute failed.", response2.getValue());
    }

    @Test
    public void testNullValue() throws Exception {
        String value = RandomString.generateChars(64);
        SetAttributeResponse response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, value, false));
        assertTrue("Setting a test attribute failed.", response.isSuccess());
        response = client.execute(new SetAttributeRequest(userId, ATTRIBUTE_NAME, JSONObject.NULL, false));
        assertTrue("Removing the attribute failed.", response.isSuccess());
        GetAttributeResponse response2 = client.execute(new GetAttributeRequest(userId, ATTRIBUTE_NAME));
        assertNull("Removing the attribute failed.", response2.getValue());
    }
}
