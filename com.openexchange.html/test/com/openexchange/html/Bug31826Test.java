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
 * {@link Bug31826Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug31826Test {

    private HtmlService service;

    public Bug31826Test() {
        super();
    }

    @Before
    public void setUp() {
        Object[] maps = HTMLServiceActivator.getDefaultHTMLEntityMaps();

        @SuppressWarnings("unchecked")
        final Map<String, Character> htmlEntityMap = (Map<String, Character>) maps[1];
        @SuppressWarnings("unchecked")
        final Map<Character, String> htmlCharMap = (Map<Character, String>) maps[0];

        htmlEntityMap.put("apos", Character.valueOf('\''));

        service = new HtmlServiceImpl(htmlCharMap, htmlEntityMap);
    }

    @After
    public void tearDown()
 {
        service = null;
    }

     @Test
     public void testKeepUnicode() throws Exception {
        String content = "dfg &hearts;&diams;&spades;&clubs;&copy;&reg;&trade; dfg";
        String test = service.sanitize(content, null, true, null, null);

        Assert.assertEquals("Unexpected return value.", "dfg \u2665\u2666\u2660\u2663\u00a9\u00ae\u2122 dfg", test);

        content = "\u2665\u2666\u2660\u2663\u00a9\u00ae\u2122 <>";
        test = service.htmlFormat(content, true, "--==--");

        Assert.assertEquals("Unexpected return value.", "\u2665\u2666\u2660\u2663\u00a9\u00ae\u2122 &lt;&gt;", test);
    }
}
