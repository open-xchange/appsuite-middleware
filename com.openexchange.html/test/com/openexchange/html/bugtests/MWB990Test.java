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

package com.openexchange.html.bugtests;

import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.google.common.collect.ImmutableList;
import com.openexchange.html.HtmlSanitizeResult;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.internal.WhitelistedSchemes;
import com.openexchange.java.Strings;

/**
 * {@link MWB990Test}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WhitelistedSchemes.class)
public class MWB990Test {

    private HtmlServiceImpl htmlServiceImpl;

    @Before
    public void setUp() {
        htmlServiceImpl = new HtmlServiceImpl(new HashMap<Character, String>(), new HashMap<String, Character>());
        PowerMockito.mockStatic(WhitelistedSchemes.class);
    }

    /**
     * Test MWB-990. URLs containing whitespace bypassed the scheme tests.
     * <p>
     * <code>tel</code> is not part of the standard configuration, so it should be sanitized
     *
     * @throws Exception In case test fails
     */
    @Test
    public void testMWB990_standard_configuration() throws Exception {
        PowerMockito.when(WhitelistedSchemes.getWhitelistedSchemes()).thenReturn(ImmutableList.of("http", "https", "ftp", "ftps", "mailto"));

        String snippet = "<div class=\"default-style\">This is a signature with a <a href=\"tel: 0123456789\">telephone number</a> embeded</div>";
        HtmlSanitizeResult test = htmlServiceImpl.sanitize(snippet, null, false, null, null, 0);
        String content = unfoldAndTrim(test.getContent());
        // tel URL should be removed 
        Assert.assertTrue(content.contains("<a>telephone number</a>"));
        Assert.assertFalse(content.contains("tel:"));
    }

    /**
     * Test MWB-990. URLs containing whitespace bypassed the scheme tests.
     * <p>
     * <code>tel</code> is added to allowed schemes, should not be sanitized
     *
     * @throws Exception In case test fails
     */
    @Test
    public void testMWB990_tel_configured() throws Exception {
        PowerMockito.when(WhitelistedSchemes.getWhitelistedSchemes()).thenReturn(ImmutableList.of("http", "https", "ftp", "ftps", "mailto", "tel"));

        String snippet = "<div class=\"default-style\">This is a signature with a <a href=\"tel: 0123456789\">telephone number</a> embeded</div>";
        HtmlSanitizeResult test = htmlServiceImpl.sanitize(snippet, null, false, null, null, 0);
        String content = unfoldAndTrim(test.getContent());
        // Note the removed whitespace in the URL
        Assert.assertTrue(content.contains("<a href=\"tel:0123456789\">telephone number</a>"));
    }

    private String unfoldAndTrim(String str) {
        String[] lines = Strings.splitByCRLF(str);
        StringBuilder sb = new StringBuilder(str.length());
        for (String line : lines) {
            sb.append(line.trim());
        }
        return sb.toString();
    }

}
