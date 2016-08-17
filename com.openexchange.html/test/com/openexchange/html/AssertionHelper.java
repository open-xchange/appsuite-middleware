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

package com.openexchange.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Queue;
import org.apache.commons.lang.StringUtils;
import com.openexchange.java.Strings;

/**
 * {@link AssertionHelper}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class AssertionHelper {

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
        String sanitized = service.sanitize(html, null, false, null, null);
        if (!Strings.isEmpty(sanitized)) {
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
