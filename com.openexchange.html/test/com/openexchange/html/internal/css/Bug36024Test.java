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

package com.openexchange.html.internal.css;

import static org.junit.Assert.assertTrue;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.html.HtmlService;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.osgi.HTMLServiceActivator;

/**
 * {@link Bug36024Test}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug36024Test  {
    private HtmlService service;

    public Bug36024Test() {
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
     public void testPositionFixedReplacedByDisplayBlock() throws Exception {
        String content = "<a href=\"http://example.com/attack.html\" style=\"position: fixed; top: 0px; left: 0; width: 1000000px; height: 100000px; background-color: red;\"></a>";
        String sanitized = service.sanitize(content, null, true, null, null);
        assertTrue("CSS style not sanitized", (sanitized.indexOf("display: block") > -1));

        String content2 = "<a href=\"http://example.com/attack.html\" style=\"position     :   fixed; top: 0px; left: 0; width: 1000000px; height: 100000px; background-color: red;\"></a>";
        String sanitized2 = service.sanitize(content2, null, true, null, null);
        assertTrue("CSS style not sanitized", (sanitized2.indexOf("display: block") > -1));
    }
}
