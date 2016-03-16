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

package com.openexchange.config.cascade.context.matching;

import java.util.Arrays;
import java.util.HashSet;
import junit.framework.TestCase;

/**
 * {@link ContextSetTermParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContextSetTermParserTest extends TestCase {

    private final ContextSetTermParser parser = new ContextSetTermParser();

    public void testParseSingleTag() {
        String term = "green";
        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "fluffy", "vegetarian");

    }

    public void testAnd() {
        String term = "fluffy & vegetarian";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "scaly", "vegetarian");

    }

    public void testThreeAnds() {
        String term ="fluffy & vegetarian & green";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "fluffy", "vegetarian");

    }

    public void testOr() {
        String term ="green | blue";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertMatches(term, "blue", "fluffy", "breatharian");
        assertNoMatch(term, "red", "fluffy", "breatharian");

    }

    public void testNot() {
        String term ="!blue";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "fluffy", "breatharian");

    }

    public void testBrackets() {
        String term ="(blue | green) & !breatharian";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertNoMatch(term, "blue", "fluffy", "breatharian");
        assertMatches(term, "blue", "fluffy", "omnivore");
    }

    public void testBrackets2() {
        String term ="blue | (green & !breatharian)";

        assertMatches(term, "green", "fluffy", "vegetarian");
        assertMatches(term, "blue", "fluffy", "breatharian");
        assertNoMatch(term, "green", "fluffy", "breatharian");
    }

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
