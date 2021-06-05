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

package com.openexchange.groupware.contact;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * {@link ParsedDisplayNameTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ParsedDisplayNameTest {
    /**
     * Initializes a new {@link ParsedDisplayNameTest}.
     */
    public ParsedDisplayNameTest() {
        super();
    }

         @Test
     public void testBasicParsing() {
        assertEquals("heinz", new ParsedDisplayName("heinz otto").getGivenName());
        assertEquals("otto", new ParsedDisplayName("heinz otto").getSurName());
        assertEquals("heinz", new ParsedDisplayName("otto, heinz").getGivenName());
        assertEquals("otto", new ParsedDisplayName("otto, heinz").getSurName());
        assertEquals("heinz", new ParsedDisplayName("otto,heinz").getGivenName());
        assertEquals("otto", new ParsedDisplayName("otto,heinz").getSurName());
    }

         @Test
     public void testPrefixTrimming() {
        assertEquals("heinz", new ParsedDisplayName("\"heinz otto").getGivenName());
        assertEquals("otto", new ParsedDisplayName("\"heinz otto").getSurName());
        assertEquals("heinz", new ParsedDisplayName(" heinz otto").getGivenName());
        assertEquals("otto", new ParsedDisplayName(" heinz otto").getSurName());
        assertEquals("heinz", new ParsedDisplayName("\theinz otto").getGivenName());
        assertEquals("otto", new ParsedDisplayName("\theinz otto").getSurName());
        assertEquals("heinz", new ParsedDisplayName("'heinz otto").getGivenName());
        assertEquals("otto", new ParsedDisplayName("'heinz otto").getSurName());
        assertEquals("heinz", new ParsedDisplayName("<heinz otto").getGivenName());
        assertEquals("otto", new ParsedDisplayName("<heinz otto").getSurName());
    }

         @Test
     public void testSuffixTrimming() {
        assertEquals("heinz", new ParsedDisplayName("heinz otto\"").getGivenName());
        assertEquals("otto", new ParsedDisplayName("heinz otto\"").getSurName());
        assertEquals("heinz", new ParsedDisplayName("heinz otto ").getGivenName());
        assertEquals("otto", new ParsedDisplayName("heinz otto ").getSurName());
        assertEquals("heinz", new ParsedDisplayName("heinz otto\t").getGivenName());
        assertEquals("otto", new ParsedDisplayName("heinz otto\t").getSurName());
        assertEquals("heinz", new ParsedDisplayName("heinz otto'").getGivenName());
        assertEquals("otto", new ParsedDisplayName("heinz otto'").getSurName());
        assertEquals("heinz", new ParsedDisplayName("heinz otto>").getGivenName());
        assertEquals("otto", new ParsedDisplayName("heinz otto>").getSurName());
    }

         @Test
     public void testLongerNames() {
        assertEquals("heinz horst albrecht", new ParsedDisplayName("heinz horst albrecht otto").getGivenName());
        assertEquals("otto", new ParsedDisplayName("heinz horst albrecht otto").getSurName());
        assertEquals("heinz horst albrecht", new ParsedDisplayName("otto, albrecht, horst, heinz").getGivenName());
        assertEquals("otto", new ParsedDisplayName("otto, albrecht, horst, heinz").getSurName());
    }

         @Test
     public void testShortNames() {
        assertEquals("heinz", new ParsedDisplayName("heinz").getGivenName());
    }

}
