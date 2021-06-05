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
