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
 * {@link Bug36275Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug36275Test {

    private HtmlService service;

    public Bug36275Test() {
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
        String content = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
            "</head><body>\n" +
            "<p>\uff0c \u3002 \uff08 \uff09 \uff1b \uff1a</p>\n" +
            "</body></html>";
        String test = service.sanitize(content, null, true, null, null);

        Assert.assertTrue("Unexpected retur value.", test.indexOf('\uff0c') > 0);
        Assert.assertTrue("Unexpected retur value.", test.indexOf('\u3002') > 0);
        Assert.assertTrue("Unexpected retur value.", test.indexOf('\uff08') > 0);
        Assert.assertTrue("Unexpected retur value.", test.indexOf('\uff09') > 0);
        Assert.assertTrue("Unexpected retur value.", test.indexOf('\uff1b') > 0);
        Assert.assertTrue("Unexpected retur value.", test.indexOf('\uff1a') > 0);
    }
}
