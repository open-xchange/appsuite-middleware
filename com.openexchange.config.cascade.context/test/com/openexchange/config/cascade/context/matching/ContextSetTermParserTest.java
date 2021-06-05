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

package com.openexchange.config.cascade.context.matching;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

/**
 * {@link ContextSetTermParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContextSetTermParserTest {
    private final ContextSetTermParser parser = new ContextSetTermParser();

         @Test
     public void testParseSingleTag() {
        String term = "green";
        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "fluffy", "vegetarian");

    }

         @Test
     public void testAnd() {
        String term = "fluffy & vegetarian";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "scaly", "vegetarian");

    }

         @Test
     public void testThreeAnds() {
        String term ="fluffy & vegetarian & green";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "fluffy", "vegetarian");

    }

         @Test
     public void testOr() {
        String term ="green | blue";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertMatches(term, "blue", "fluffy", "breatharian");
        assertNoMatch(term, "red", "fluffy", "breatharian");

    }

         @Test
     public void testNot() {
        String term ="!blue";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "fluffy", "breatharian");

    }

         @Test
     public void testBrackets() {
        String term ="(blue | green) & !breatharian";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "fluffy", "breatharian");
        assertMatches(term, "blue", "fluffy", "omnivore");
    }

         @Test
     public void testBrackets2() {
        String term ="blue | (green & !breatharian)";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertMatches(term, "blue", "fluffy", "breatharian");
        assertNoMatch(term, "green", "fluffy", "breatharian");
    }

         @Test
     public void testComplex() {
        String term ="!(blue | green) & (breatharian | ( fluffy & vegetarian ))";

        assertNoMatch(term, "green", "fluffy", "vegetarian");
        assertMatches(term, "red", "fluffy", "breatharian");
        assertNoMatch(term, "blue", "soft", "breatharian");
        assertMatches(term, "red", "fluffy", "vegetarian");
        assertNoMatch(term, "red", "soft", "vegetarian");

    }

    public void assertMatches(String term, String... tags) {
        ContextSetTerm t = parser.parse(term);
        assertTrue(t.matches(new HashSet<String>(Arrays.asList(tags))));
    }

    public void assertNoMatch(String term, String... tags) {
        ContextSetTerm t = parser.parse(term);
        assertFalse(t.matches(new HashSet<String>(Arrays.asList(tags))));
    }

}
