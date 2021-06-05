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
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug21118Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug21118Test extends AbstractSanitizing {

     @Test
     public void testEmptyLines() {
        StringBuilder htmlContentBuilder = new StringBuilder();
        htmlContentBuilder.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">");
        htmlContentBuilder.append(" <head>");
        htmlContentBuilder.append("    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"/>");
        htmlContentBuilder.append(" </head>");
        htmlContentBuilder.append(" <body>");
        htmlContentBuilder.append("  <p style=\"margin: 0pt;\">");
        htmlContentBuilder.append("   <span>Lorem ipsum,</span>");
        htmlContentBuilder.append("  </p>");
        htmlContentBuilder.append("  <p style=\"margin: 0px;\">&#160;</p>");
        htmlContentBuilder.append("  <p style=\"margin: 0px;\">dolor sit amet, consectetur adipiscing elit.</p>");
        htmlContentBuilder.append("  <p style=\"margin: 0px;\">&#160;</p>");
        htmlContentBuilder.append("  <p style=\"margin: 0px;\">Integer pretium luctus nibh, et interdum lacus ullamcorper vel.</p>");
        htmlContentBuilder.append("  <p style=\"margin: 0px;\">&#160;</p>");
        htmlContentBuilder.append("  <p style=\"margin: 0px;\">Sed</p>");
        htmlContentBuilder.append("  <p style=\"margin: 0px; \">");
        htmlContentBuilder.append("   <span>");
        htmlContentBuilder.append("    <span></span>");
        htmlContentBuilder.append("   </span>");
        htmlContentBuilder.append("  </p>");
        htmlContentBuilder.append(" </body>");
        htmlContentBuilder.append("</html>");
        String htmlContent = htmlContentBuilder.toString();
        String actual = getHtmlService().html2text(htmlContent, false);
        String[] lines = actual.split("\r\n");
        int lineNumber = 1;
        for (String line : lines) {
            // Every even line should be empty
            if (lineNumber++ % 2 == 0) {
                assertEquals("No empty line", " ", line);
            }
        }

        StringBuilder expectedBuilder = new StringBuilder();
        expectedBuilder.append("Lorem ipsum,\r\n \r\n");
        expectedBuilder.append("dolor sit amet, consectetur adipiscing elit.\r\n \r\n");
        expectedBuilder.append("Integer pretium luctus nibh, et interdum lacus ullamcorper vel.\r\n \r\n");
        expectedBuilder.append("Sed");
        String expected = expectedBuilder.toString();
        assertEquals("Unexpected value", expected, actual);
    }
}
