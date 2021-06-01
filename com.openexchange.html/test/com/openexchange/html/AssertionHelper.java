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

package com.openexchange.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Queue;
import org.apache.commons.lang.StringUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link AssertionHelper}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class AssertionHelper {

    public static void assertSanitizedEquals(HtmlService service, String html, String expected, boolean ignoreCase) {
        try {
            String sanitized = service.sanitize(html, null, false, null, null);
            if (ignoreCase) {
                if (Strings.isNotEmpty(sanitized)) {
                    sanitized = sanitized.toLowerCase();
                }
                assertEquals("Unexpected HTML sanitize result", expected.toLowerCase(), sanitized);
            }
            assertEquals("Unexpected HTML sanitize result", expected, sanitized);
        } catch (OXException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    public static void assertSanitizedDoesNotContain(HtmlService service, String html, String... mailiciousParams) {
        assertSanitized(service, html, mailiciousParams, AssertExpression.NOT_CONTAINED);
    }

    public static void assertSanitizedEmpty(HtmlService service, String html) {
        assertSanitized(service, html, (String) null, AssertExpression.EMPTY);
    }

    public static void assertSanitized(HtmlService service, String html, String maliciousParam, AssertExpression ae) {
        assertSanitized(service, html, null == maliciousParam ? null : new String[] { maliciousParam }, ae);
    }

    public static void assertSanitized(HtmlService service, String html, String[] maliciousParams, AssertExpression ae) {
        try {
            String sanitized = service.sanitize(html, null, false, null, null);
            if (Strings.isNotEmpty(sanitized)) {
                sanitized = sanitized.toLowerCase();
            }
            if (AssertExpression.NOT_CONTAINED.equals(ae)) {
                if (null != maliciousParams) {
                    for (String maliciousParam : maliciousParams) {
                        assertFalse("sanitized output: " + sanitized + " contains " + maliciousParam, StringUtils.containsIgnoreCase(sanitized, maliciousParam));
                    }
                }
            } else if (AssertExpression.EMPTY.equals(ae)) {
                assertTrue("expected html: " + html + " after sanitizing to be empty but contains " + sanitized, Strings.isEmpty(sanitized));
            }
        } catch (OXException e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    public static void assertBlockingQuote(final Queue<String> quotedText, String[] quotedLines) {
        int line = 0;
        while (!quotedText.isEmpty()) {
            String qt = quotedText.poll();
            for (int i = line; i < quotedLines.length; i++) {
                if (quotedLines[i].contains(qt)) {
                    assertTrue("The HTML <blockquote> tag is not properly converted to '>'", quotedLines[i].startsWith(">"));
                    line = i;
                    break;
                }
            }
        }
    }

    public static void assertTag(String tag, String actual, boolean closing) {
        assertTrue(tag + " is missing", actual.contains(tag));
        if (closing) {
            String closingTag = tag.substring(1);
            closingTag = "</" + closingTag;
            assertTag(closingTag, actual, false);
        }
    }
}
