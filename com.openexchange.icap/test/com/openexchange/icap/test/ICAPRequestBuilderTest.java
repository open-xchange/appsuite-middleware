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

package com.openexchange.icap.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.icap.ICAPCommons;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.header.ICAPRequestHeader;
import com.openexchange.icap.test.util.AssertUtil;
import com.openexchange.icap.test.util.ICAPTestProperties;

/**
 * {@link ICAPRequestBuilderTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPRequestBuilderTest {

    private Map<String, String> expected;

    /**
     * Initialises a new {@link ICAPRequestBuilderTest}.
     */
    public ICAPRequestBuilderTest() {
        super();
    }

    /**
     * Sets up the test
     */
    @Before
    public void setUp() {
        expected = new HashMap<>(4);
        expected.put(ICAPRequestHeader.HOST, ICAPTestProperties.ICAP_SERVER_ADDRESS);
        expected.put(ICAPRequestHeader.USER_AGENT, ICAPCommons.USER_AGENT);
    }

    /**
     * Tests the mandatory request parameters
     */
    @Test
    public void testMandatory() {
        ICAPRequest request = new ICAPRequest.Builder().withServer(ICAPTestProperties.ICAP_SERVER_ADDRESS).build();
        assertEquals("Server does not match", request.getServer(), ICAPTestProperties.ICAP_SERVER_ADDRESS);
        AssertUtil.assertICAPHeaders(expected, request);
    }

    /**
     * Tests with legitimate port other than the default {@link #ICAPTestProperties.ICAP_PORT}
     */
    @Test
    public void testLegitimatePort() {
        ICAPRequest request = new ICAPRequest.Builder().withServer(ICAPTestProperties.ICAP_SERVER_ADDRESS).withPort(1337).build();
        assertNotEquals("Port does not match", request.getPort(), ICAPTestProperties.ICAP_SERVER_PORT);
        assertEquals("Port does not match", request.getPort(), 1337);
    }

    /**
     * Tests out of range port
     */
    @Test
    public void testPortOutofRange() {
        try {
            new ICAPRequest.Builder().withServer(ICAPTestProperties.ICAP_SERVER_ADDRESS).withPort(123456).build();
            fail("IllegalArgumentException was expected, port exceeds the 65535 upper bound.");
        } catch (IllegalArgumentException e) {
            //ignore
        }
        try {
            new ICAPRequest.Builder().withServer(ICAPTestProperties.ICAP_SERVER_ADDRESS).withPort(-1).build();
            fail("IllegalArgumentException was expected, port exceeds the 0 lower bound.");
        } catch (IllegalArgumentException e) {
            //ignore
        }
    }

    /**
     * Tests service empty or <code>null</code>
     */
    @Test
    public void testWithEmptyOrNullService() {
        try {
            new ICAPRequest.Builder().withServer(ICAPTestProperties.ICAP_SERVER_ADDRESS).withService("").build();
            fail("IllegalArgumentException was expected, service cannot be empty.");
        } catch (IllegalArgumentException e) {
            //ignore
        }
        try {
            new ICAPRequest.Builder().withServer(ICAPTestProperties.ICAP_SERVER_ADDRESS).withService(null).build();
            fail("IllegalArgumentException was expected, service cannot be 'null'.");
        } catch (IllegalArgumentException e) {
            //ignore
        }
    }

    /**
     * Tests with <code>null</code> body.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullBody() {
        new ICAPRequest.Builder().withServer(ICAPTestProperties.ICAP_SERVER_ADDRESS).withBody(null).build();
    }

    /**
     * Tests with empty or <code>null</code> server.
     */
    @Test
    public void testWithEmptyRrNullServer() {
        try {
            new ICAPRequest.Builder().withServer(ICAPTestProperties.ICAP_SERVER_ADDRESS).withServer("").build();
            fail("IllegalArgumentException was expected, server cannot be empty.");
        } catch (IllegalArgumentException e) {
            //ignore
        }
        try {
            new ICAPRequest.Builder().withServer(ICAPTestProperties.ICAP_SERVER_ADDRESS).withServer(null).build();
            fail("IllegalArgumentException was expected, server cannot be 'null'.");
        } catch (IllegalArgumentException e) {
            //ignore
        }
    }
}
