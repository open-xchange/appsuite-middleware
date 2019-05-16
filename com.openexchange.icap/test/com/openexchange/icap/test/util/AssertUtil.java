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

package com.openexchange.icap.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;

/**
 * {@link AssertUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public final class AssertUtil {

    /**
     * Asserts that the <code>expected</code> {@link ICAPResponse} is equal the <code>actual</code> one.
     * The status line, status code, ICAP headers, as well as the encapsulated status line (if any),
     * the encapsulated status code (if any) and the encapsulated headers (if any) are also asserted.
     * 
     * @param expected The expected {@link ICAPResponse}
     * @param actual The actual {@link ICAPResponse}
     */
    public static void assertICAPResponse(ICAPResponse expected, ICAPResponse actual) {
        assertEquals("The status line does not match", expected.getStatusLine(), actual.getStatusLine());
        assertEquals("The status code does not match", expected.getStatusCode(), actual.getStatusCode());
        assertHeaders(expected.getHeaders(), actual.getHeaders());
        assertEquals("The encapsulated status code does not match", expected.getEncapsulatedStatusLine(), actual.getEncapsulatedStatusLine());
        assertEquals("The encapsulated status code does not match", expected.getEncapsulatedStatusCode(), actual.getEncapsulatedStatusCode());
        assertHeaders(expected.getEncapsulatedHeaders(), actual.getEncapsulatedHeaders());
        assertEquals("The encapsulated headers do not match", expected.getEncapsulatedHeaders(), actual.getEncapsulatedHeaders());
    }

    /**
     * Asserts that the specified {@link ICAPRequest} contains at least the <code>expected</code> headers.
     * 
     * @param expected The expected headers of the {@link ICAPRequest}
     * @param actual The actual {@link ICAPRequest}
     */
    public static void assertICAPHeaders(Map<String, String> expected, ICAPRequest actual) {
        assertHeaders(expected, actual.getHeaders());
    }

    /**
     * Asserts that the <code>expected</code> headers are equal to the <code>actual</code> ones.
     * 
     * @param expected The expected headers
     * @param actual The actual headers
     */
    private static void assertHeaders(Map<String, String> expected, Map<String, String> actual) {
        assertEquals("Different amount of headers detected", expected.size(), actual.size());
        for (Entry<String, String> entry : expected.entrySet()) {
            assertTrue("Expected header '" + entry.getKey() + "' not found in request", actual.containsKey(entry.getKey()));
            assertEquals("Expected value for header '" + entry.getKey() + "' does not match", entry.getValue(), actual.get(entry.getKey()));
        }
    }
}
