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
