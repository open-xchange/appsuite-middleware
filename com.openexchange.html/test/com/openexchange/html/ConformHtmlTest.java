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

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link ConformHtmlTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConformHtmlTest extends AbstractSanitizing {
     @Test
     public void testConformHtml() throws Exception {
        String content = "<table><tr>\n" +
            "<td style=\"background-color:#FFFFFF; height:52px; width:100px;\">\n" +
            "<span style = \"font-size:48px; font-family: Veranda; font-weight: bold; color: #6666FF;\">OX</span>\n" +
            "</td><td align=\"center\" style=\"width:300px;\"><h1>${doc.translate.email.secure_email}</h1></td>\n" +
            "</tr>\n" +
            "</table>";

        String test = getHtmlService().getConformHTML(content, "us-ascii");

        Assert.assertTrue("Missing DOCTYPE declaration", test.startsWith("<!doctype html"));
        Assert.assertTrue("Missing <head> section.", test.indexOf("<head>") >= 0);
        Assert.assertTrue("Missing <meta> tag.", test.indexOf("<meta") >= 0);
        Assert.assertTrue("Missing <meta> tag.", test.indexOf('?') < 0);
    }

     @Test
     public void testConformHtml2() throws Exception {
        String content = "<p>Text before one empty line</p><p><br></p><p>Text after empty line.</p>";

        String test = getHtmlService().getConformHTML(content, "us-ascii");

        Assert.assertTrue("Unexpected HTML content", test.indexOf("<br>") > 0);
        Assert.assertTrue("Unexpected HTML content", test.indexOf("</br>") < 0);
    }

}
