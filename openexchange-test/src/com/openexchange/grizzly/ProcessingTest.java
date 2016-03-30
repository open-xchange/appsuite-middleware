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

package com.openexchange.grizzly;

import java.util.Map;
import org.json.JSONObject;
import org.junit.Before;
import com.openexchange.ajax.simple.AbstractSimpleClientTest;
import com.openexchange.ajax.simple.SimpleResponse;


/**
 * {@link ProcessingTest} - Simple test that exceeds the default apache timeout of 100s and thus triggers the http processing ping
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ProcessingTest extends AbstractSimpleClientTest {

    private final int TIMEOUT = 120;
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
     * Tests that the connection isn't closed by apache and we receive bac the payload we initially provided. 
     * @throws Exception
     */
    public void testSimpleProcessing() throws Exception {
        as(USER1);
        SimpleResponse response = callGeneral("grizzlytest", "processing", "timeout", TIMEOUT, "payload", simplePayload );
        Map<String, Object> objectData = response.getObjectData();
        Map<String, Object> payloadData = (Map<String, Object>) objectData.get("payload");
        Object responseValue1 = payloadData.get(key1);
        Object responseValue2 = payloadData.get(key2);
        assertEquals(responseValue1, value1);
        assertEquals(responseValue2, value2);
    }

}
