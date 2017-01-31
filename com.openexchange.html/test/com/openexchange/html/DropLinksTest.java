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
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link DropLinksTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DropLinksTest extends AbstractSanitizing {

    public DropLinksTest() {
        super();
    }

    @Test
    public void testDroppedLinks1() throws Exception {
        String content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "</head><body>\n" +
            "\n" +
            "<img src=\"planets.gif\" width=\"145\" height=\"126\" alt=\"Planets\"\n" +
            "usemap=\"#planetmap\">\n" +
            "\n" +
            "<map name=\"planetmap\">\n" +
            "  <area shape=\"rect\" coords=\"0,0,82,126\" href=\"sun.htm\" alt=\"Sun\">\n" +
            "  <area shape=\"circle\" coords=\"90,58,3\" href=\"mercur.htm\" alt=\"Mercury\">\n" +
            "  <area shape=\"circle\" coords=\"124,58,8\" href=\"venus.htm\" alt=\"Venus\">\n" +
            "</map>" +
            "\n" +
            "</body></html>";

        HtmlService service = getHtmlService();
        HtmlSanitizeResult result = service.sanitize(content, HtmlSanitizeOptions.builder().setSuppressLinks(true).build());

        content = result.getContent();
        Assert.assertTrue("", content.indexOf("sun.htm") < 0);
        Assert.assertTrue("", content.indexOf("mercur.htm") < 0);
        Assert.assertTrue("", content.indexOf("venus.htm") < 0);
    }

    @Test
    public void testDroppedLinks2() throws Exception {
        String content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "</head><body>\n" +
            "\n" +
            "<p>Here is the <a href=\"http://unsecure.com/klick.me\">link</a>" +
            "\n" +
            "</body></html>";

        HtmlService service = getHtmlService();
        HtmlSanitizeResult result = service.sanitize(content, HtmlSanitizeOptions.builder().setSuppressLinks(true).build());

        Assert.assertTrue("", result.getContent().indexOf("http://unsecure.com/klick.me") < 0);
    }

    @Test
    public void testNoDroppedLinks() throws Exception {
        String content = "<!DOCTYPE html>\n" +
            "<html><head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "</head><body>\n" +
            "\n" +
            "<p>Here is the <a href=\"http://unsecure.com/klick.me\">link</a>" +
            "\n" +
            "</body></html>";

        HtmlService service = getHtmlService();
        HtmlSanitizeResult result = service.sanitize(content, HtmlSanitizeOptions.builder().build());

        Assert.assertTrue("", result.getContent().indexOf("http://unsecure.com/klick.me") >= 0);
    }

}
