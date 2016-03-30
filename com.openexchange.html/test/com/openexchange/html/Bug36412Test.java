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

    @Test
    public void testKeepUnicode() {
        String content = "              <tr>\n" +
            "                            <td border=\"1\" class=\"webseminare\"\n" +
            "                              font-size:14px;=\"\" line-height:=\"\"\n" +
            "                              18px;\"=\"\" height=\"39\" valign=\"middle\"\n" +
            "                              align=\"center\" bgcolor=\"#346897\">Web Seminare</td>\n" +
            "                          </tr>";
        String test = service.sanitize(content, null, true, null, null);

        Assert.assertTrue("Unexpected return value.", test.indexOf("<td class=\"webseminare\" height=\"39\" valign=\"middle\" align=\"center\" bgcolor=\"#346897\">") > 0);
        Assert.assertTrue("Unexpected return value.", test.indexOf("Web Seminare") > 0);
    }
}
