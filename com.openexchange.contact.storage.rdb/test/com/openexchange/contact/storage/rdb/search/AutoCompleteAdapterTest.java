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
    public void testDetectWildcard() {
        String query = "hund wu?st hallo* ot*to d?d?* * hier";
        assertPatterns(query, "%");
    }

    @Test
    public void testExcludeDuplicatePatterns() {
        String query = "hund wurst wurst hund hund hund wurst hund";
        assertPatterns(query, "hund%", "wurst%");
    }

    @Test
    public void testExcludeRedundantPatterns_1() {
        String query = "hund hundewurst hu hu husten hustenanfall";
        assertPatterns(query, "hu%");
    }

    @Test
    public void testExcludeRedundantPatterns_2() {
        String query = "hu*nd hundewurst hun husten hustenanfall";
        assertPatterns(query, "hu%nd%", "hun%", "husten%");
    }

    @Test
    public void testShrinkWildcards_1() {
        String query = "*********";
        assertPatterns(query, "%");
    }

    @Test
    public void testShrinkWildcards_2() {
        String query = "hu**n*****d*********";
        assertPatterns(query, "hu%n%d%");
    }

    @Test
    public void testShrinkWildcards_() {
        String query = "hu**n**\\***d*********";
        assertPatterns(query, "hu%n%\\%%d%");
    }

    private static void assertPatterns(String query, String... expectedPatterns) {
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
