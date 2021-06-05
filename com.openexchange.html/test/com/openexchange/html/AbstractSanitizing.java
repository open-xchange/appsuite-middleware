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
import org.junit.Before;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.osgi.HTMLServiceActivator;
import com.openexchange.java.Strings;


/**
 * {@link AbstractSanitizing}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public abstract class AbstractSanitizing {

    private HtmlService service;

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
    public void tearDown() {
        service = null;
    }

    protected HtmlService getHtmlService() {
        return service;
    }

    protected String trimLines(String str) {
        return trim(str, true, false);
    }

    protected String trimLinesAndDropLineBreaks(String str) {
        return trim(str, true, true);
    }

    private String trim(String str, boolean trimLines, boolean dropLineBreaks) {
        StringBuilder sb = new StringBuilder(str.length());
        for (String line : Strings.splitByCRLF(str)) {
            if (trimLines) {
                line = line.trim();
            }
            if (line.length() > 0) {
                sb.append(line);
                if (!dropLineBreaks) {
                    sb.append('\n');
                }
            }
        }
        return sb.toString().trim();
    }
}
