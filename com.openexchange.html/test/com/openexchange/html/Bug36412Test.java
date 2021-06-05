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

import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.osgi.HTMLServiceActivator;

/**
 * {@link Bug36412Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug36412Test {

    private HtmlService service;

    public Bug36412Test() {
        super();
    }

    @Before
    public void setUp() {
        Object[] maps = HTMLServiceActivator.getDefaultHTMLEntityMaps();

        @SuppressWarnings("unchecked") final Map<String, Character> htmlEntityMap = (Map<String, Character>) maps[1];
        @SuppressWarnings("unchecked") final Map<Character, String> htmlCharMap = (Map<Character, String>) maps[0];

        htmlEntityMap.put("apos", Character.valueOf('\''));

        service = new HtmlServiceImpl(htmlCharMap, htmlEntityMap);
    }

    @After
    public void tearDown() {
        service = null;
    }

    @Test
    public void testKeepUnicode() throws Exception {
       String content = "              <table><tr>\n" +
           "                            <td border=\"1\" class=\"webseminare\"\n" +
           "                              font-size:14px;=\"\" line-height:=\"\"\n" +
           "                              18px;\"=\"\" height=\"39\" valign=\"middle\"\n" +
           "                              align=\"center\" bgcolor=\"#346897\">Web Seminare</td>\n" +
           "                          </tr></table>";
       String test = service.sanitize(content, null, true, null, null);
       Assert.assertTrue("Unexpected return value.", test.indexOf("<td class=\"webseminare\" height=\"39\" valign=\"middle\" align=\"center\" bgcolor=\"#346897\">") > 0);
       Assert.assertTrue("Unexpected return value.", test.indexOf("Web Seminare") > 0);
   }

}
