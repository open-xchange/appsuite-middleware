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

package com.openexchange.grizzly;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.simple.AbstractSimpleClientTest;
import com.openexchange.ajax.simple.SimpleResponse;

/**
 * {@link GetWithBodyTest} - Test that the body of HTTP methods that have no clear specification about transporting data within the body is
 * consumed completely.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class GetWithBodyTest extends AbstractSimpleClientTest {

    private JSONObject simplePayload;
    private final String key1 = "a";
    private final int value1 = 1;
    private final String key2 = "b";
    private final int value2 = 2;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        simplePayload = new JSONObject();
        simplePayload.put(key1, value1);
        simplePayload.put(key2, value2);
    }

    /**
     * Tests that the body of the method is consumed completely.
     *
     * @throws Exception
     */
    @Test
    public void testGetWithBody() throws Exception {
        as(USER1);
        SimpleResponse response = callGeneral("grizzlytest", "getWithBody", "body", simplePayload);
        Map<String, Object> objectData = response.getObjectData();
        Map<String, Object> payloadData = (Map<String, Object>) objectData.get("payload");
        Object responseValue1 = payloadData.get(key1);
        Object responseValue2 = payloadData.get(key2);
        assertEquals(responseValue1, I(value1));
        assertEquals(responseValue2, I(value2));
    }

}
