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

package com.openexchange.html.vulntests;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.ImmutableMap;
import com.openexchange.html.HtmlSanitizeResult;
import com.openexchange.html.internal.HtmlServiceImpl;

/**
 * {@link MWB1116VulTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class MWB1116VulTest {

    private HtmlServiceImpl htmlServiceImpl;

    /**
     * Initializes a new {@link MWB1116VulTest}.
     */
    public MWB1116VulTest() {
        super();
    }

    @Before
    public void setUp() {
        Map<Character, String> htmlCharMap = ImmutableMap.of(Character.valueOf('<'), "lt", Character.valueOf('>'), "gt");
        htmlServiceImpl = new HtmlServiceImpl(htmlCharMap, new HashMap<String, Character>());
    }

    @Test
    public void testMWB1116() {
        String plainText = "Important Message!\n"
            + "<!--anchor-5fd15ca8-a027-4b14-93ea-35de1747419e: <img src=\"\" onerror=\"alert('XSS');\">-->";

        HtmlSanitizeResult htmlFormat = htmlServiceImpl.htmlFormat(plainText, true, "anchor-5fd15ca8-a027-4b14-93ea-35de1747419e:", 11111);
        String content = htmlFormat.getContent();
        Assert.assertTrue(content.indexOf("<img src=\"\" onerror=\"alert('XSS');\">") >= 0);

        htmlFormat = htmlServiceImpl.htmlFormat(plainText, true, "anchor-538e7835-4fe9-4873-8719-2b08743af6c1:", 11111);
        content = htmlFormat.getContent();
        Assert.assertTrue(content.indexOf("<img src=\"\" onerror=\"alert('XSS');\">") < 0);


    }

}
