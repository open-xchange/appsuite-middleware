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
 *     Copyright (C) 2018-2020 OX Software GmbH
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
