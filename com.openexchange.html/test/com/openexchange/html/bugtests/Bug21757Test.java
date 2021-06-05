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

package com.openexchange.html.bugtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug21757Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug21757Test extends AbstractSanitizing {

     @Test
     public void testEnsureBodyTagPresence() throws Exception {
        String htmlContent = "<center>Lorem Ipsum Dolor<table<tr><td>cell</td></tr></table></center>";
        String actual = trimLines(getHtmlService().getConformHTML(htmlContent, "UTF-8"));

        String expected = "<!doctype html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta charset=\"UTF-8\">\n" +
            "</head>\n" +
            "<body>\n" +
            "<center>\n" +
            "Lorem Ipsum Dolor\n" +
            "<table>\n" +
            "<tbody>\n" +
            "<tr>\n" +
            "<td>cell</td>\n" +
            "</tr>\n" +
            "</tbody>\n" +
            "</table>\n" +
            "</center>\n" +
            "</body>\n" +
            "</html>";

        assertTrue("The opening <body> tag is missing", actual.contains("<body>"));
        assertTrue("The closing </body> tag is missing", actual.contains("</body>"));
        assertEquals("Unexpected value", expected, actual);
    }
}
