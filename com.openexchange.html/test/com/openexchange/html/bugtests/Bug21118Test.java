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
