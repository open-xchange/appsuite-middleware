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

package com.openexchange.contact.storage.rdb.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.openexchange.java.SimpleTokenizer;

/**
 * {@link AutoCompleteAdapterTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.8.0
 */
public class AutoCompleteAdapterTest {

    @Test
    public void testDetectWildcard() throws Exception {
        String query = "hund wu?st hallo* ot*to d?d?* * hier";
        assertPatterns(query, "%");
    }

    @Test
    public void testExcludeDuplicatePatterns() throws Exception {
        String query = "hund wurst wurst hund hund hund wurst hund";
        assertPatterns(query, "hund%", "wurst%");
    }

    @Test
    public void testExcludeRedundantPatterns_1() throws Exception {
        String query = "hund hundewurst hu hu husten hustenanfall";
        assertPatterns(query, "hu%");
    }

    @Test
    public void testExcludeRedundantPatterns_2() throws Exception {
        String query = "hu*nd hundewurst hun husten hustenanfall";
        assertPatterns(query, "hu%nd%", "hun%", "husten%");
    }

    @Test
    public void testShrinkWildcards_1() throws Exception {
        String query = "*********";
        assertPatterns(query, "%");
    }

    @Test
    public void testShrinkWildcards_2() throws Exception {
        String query = "hu**n*****d*********";
        assertPatterns(query, "hu%n%d%");
    }

    @Test
    public void testShrinkWildcards_() throws Exception {
        String query = "hu**n**\\***d*********";
        assertPatterns(query, "hu%n%\\%%d%");
    }

    private static void assertPatterns(String query, String...expectedPatterns) throws Exception {
        List<String> patterns = AutocompleteAdapter.preparePatterns(SimpleTokenizer.tokenize(query));
        if (null == expectedPatterns || 0 == expectedPatterns.length) {
            assertTrue(null == patterns || 0 == patterns.size());
        } else {
            assertEquals(expectedPatterns.length, patterns.size());
            for (int i = 0; i < expectedPatterns.length; i++) {
                assertEquals(expectedPatterns[i], patterns.get(i));
            }
        }
    }

}
