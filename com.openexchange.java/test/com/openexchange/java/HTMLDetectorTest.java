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
