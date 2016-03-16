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

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link ConformHtmlTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConformHtmlTest extends AbstractSanitizing {
    @Test
    public void testConformHtml() {
        String content = "<table><tr>\n" +
            "<td style=\"background-color:#FFFFFF; height:52px; width:100px;\">\n" +
            "<span style = \"font-size:48px; font-family: Veranda; font-weight: bold; color: #6666FF;\">OX</span>\n" +
            "</td><td align=\"center\" style=\"width:300px;\"><h1>${doc.translate.email.secure_email}</h1></td>\n" +
            "</tr>\n" +
            "</table>";

        String test = getHtmlService().getConformHTML(content, "us-ascii");

        Assert.assertTrue("Missing DOCTYPE declaration", test.startsWith("<!DOCTYPE html"));
        Assert.assertTrue("Missing <head> section.", test.indexOf("<head>") >= 0);
        Assert.assertTrue("Missing <meta> tag.", test.indexOf("<meta") >= 0);
        Assert.assertTrue("Missing <meta> tag.", test.indexOf("?") < 0);
    }

    @Test
    public void testConformHtml2() {
        String content = "<p>Text before one empty line</p><p><br></p><p>Text after empty line.</p>";

        String test = getHtmlService().getConformHTML(content, "us-ascii");

        Assert.assertTrue("Unexpected HTML content", test.indexOf("<br>") > 0);
        Assert.assertTrue("Unexpected HTML content", test.indexOf("</br>") < 0);
    }

}
