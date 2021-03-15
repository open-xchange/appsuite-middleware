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
 *    trademarks of the OX Software GmbH. group of companies.
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
