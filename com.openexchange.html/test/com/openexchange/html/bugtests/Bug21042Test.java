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

import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug21042Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug21042Test extends AbstractSanitizing {

     @Test
     public void testPrettyPrinter() {
        StringBuilder htmlContentBuilder = new StringBuilder();
        htmlContentBuilder.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        htmlContentBuilder.append(" <head>\n");
        htmlContentBuilder.append("    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"/>\n");
        htmlContentBuilder.append(" </head>\n");
        htmlContentBuilder.append(" <body>\n");
        htmlContentBuilder.append("  <p style=\"margin: 0pt;\">\n");
        htmlContentBuilder.append("   <span>\n");
        htmlContentBuilder.append("    Lorem<span style=\"font-size: 18pt;\"><strong>ipsum</strong></span>dolor\n");
        htmlContentBuilder.append("    <span></span>\n");
        htmlContentBuilder.append("   </span>\n");
        htmlContentBuilder.append("  </p>\n");
        htmlContentBuilder.append("  <p style=\"margin: 0px; \"></p>\n");
        htmlContentBuilder.append("  <p style=\"margin: 0px; \"></p>\n");
        htmlContentBuilder.append(" </body>\n");
        htmlContentBuilder.append("</html>");
        String htmlContent = htmlContentBuilder.toString();
        String actual = getHtmlService().html2text(htmlContent, false);
        System.out.println(actual);
    }

}
