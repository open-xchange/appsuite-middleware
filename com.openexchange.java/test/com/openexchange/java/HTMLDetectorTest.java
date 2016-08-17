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

package com.openexchange.java;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;


/**
 * {@link HTMLDetectorTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HTMLDetectorTest {

    /**
     * Initializes a new {@link HTMLDetectorTest}.
     */
    public HTMLDetectorTest() {
        super();
    }

    @Test
    public final void testDetectJSEventHandler() {
        try {
            final byte[] svgImage = ("<svg onload=\"alert(document.domain)\" xmlns=\"http://www.w3.org/2000/svg\"\n" +
                "        xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                "        xmlns:ev=\"http://www.w3.org/2001/xml-events\"\n" +
                "        version=\"1.1\" baseProfile=\"full\"\n" +
                "        width=\"700px\" height=\"400px\" viewBox=\"0 0 700 400\">\n" +
                "    <text x=\"20\" y=\"40\">oha!</text>\n" +
                "</svg>").getBytes();

            boolean containsHTMLTags = HTMLDetector.containsHTMLTags(new ByteArrayInputStream(svgImage), false);

            org.junit.Assert.assertTrue("HTMLDetector should have found \"onload\" JavaScript event handler.", containsHTMLTags);
        } catch (IOException e) {
            e.printStackTrace();
            org.junit.Assert.fail(e.getMessage());
        }

    }

    @Test
    public final void testDetectClosingHTMLTags_Bug33577() {
        final String htmlContent = ("<td>My Name</td></tr>" +
            "</tbody></table>" +
            "</body>" +
            "</html>");

        boolean containsHTMLTags = HTMLDetector.containsHTMLTags(htmlContent, true);

        org.junit.Assert.assertTrue("HTMLDetector should have found html.", containsHTMLTags);
    }

    @Test
    public final void testDetectStartingScriptTag_Bug44584() {
        try {
            final byte[] svgImage = ("<svg\n" +
                "   xmlns:svg=\"http://www.w3.org/2000/svg\"\n" +
                "   xmlns=\"http://www.w3.org/2000/svg\"\n" +
                "   xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                "   version=\"1.0\"\n" +
                "   x=\"0\"\n" +
                "   y=\"0\"\n" +
                "   width=\"194\"\n" +
                "   height=\"200\"\n" +
                "   id=\"Face\">\n" +
                "\n" +
                "   <script type=\"text/ecmascript\">\n" +
                "  <![CDATA[\n" +
                " alert(\"XSS Vulnerability\");\n" +
                "  ]]>\n" +
                " </script>\n" +
                "</svg>").getBytes();

            boolean containsHTMLTags = HTMLDetector.containsHTMLTags(new ByteArrayInputStream(svgImage), false);

            org.junit.Assert.assertTrue("HTMLDetector should have found \"onload\" JavaScript event handler.", containsHTMLTags);
        } catch (IOException e) {
            e.printStackTrace();
            org.junit.Assert.fail(e.getMessage());
        }
    }
}
