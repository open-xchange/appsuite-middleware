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

package com.openexchange.global.tools.id;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link IDManglerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IDManglerTest {
         @Test
     public void testRoundtrip() {
        String id = IDMangler.mangle("com.openexchange.some.service", "someFolder", "someId");
        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("com.openexchange.some.service", "someFolder", "someId"), unmangled);

    }

         @Test
     public void testSlashes() {
        String id = IDMangler.mangle("com.openexchange.some.service", "someFolder/folder/with/slashes", "someId");
        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("com.openexchange.some.service", "someFolder/folder/with/slashes", "someId"), unmangled);
    }

         @Test
     public void testSpecialCharacters() {
        String id = IDMangler.mangle(
            "com.openexchange.some.service",
            "someFolder/folder/with/slashes and whitespace and f\u00ac\u00df)($nny ch$\u00ac\u00df\u00ac\u00df$\u221a\u00f2\u221a\u00e8\u221a\u00e8\u221a\u00e7r\u00ac\u00df$ter$",
            "someId");
        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList(
            "com.openexchange.some.service",
            "someFolder/folder/with/slashes and whitespace and f\u00ac\u00df)($nny ch$\u00ac\u00df\u00ac\u00df$\u221a\u00f2\u221a\u00e8\u221a\u00e8\u221a\u00e7r\u00ac\u00df$ter$",
            "someId"), unmangled);
    }

         @Test
     public void testBrackets() {
        String id = IDMangler.mangle("some", "com[ponents", "wi]]th", "brackets");

        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("some", "com[ponents", "wi]]th", "brackets"), unmangled);
    }

         @Test
     public void testBackslash() {
        String id = IDMangler.mangle("something\\with", "neato\\backslashes");
        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("something\\with", "neato\\backslashes"), unmangled);
    }

         @Test
     public void testContainsPrimaryDelim() {
        List<String> unmangled = IDMangler.unmangle("some:/partial:id://component");
        assertEquals(Arrays.asList("some:/partial:id", "component"), unmangled);
    }

         @Test
     public void testContainsFragmentsOfPrimaryDelim() {
        String id = IDMangler.mangle("some:/service:this is", "someFolder", "someId");
        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("some:/service:this is", "someFolder", "someId"), unmangled);
    }

         @Test
     public void testInfostoreFolderAndId() {
        List<String> unmangled = IDMangler.unmangle("123/456");

        assertEquals("Unexpected size", 2, unmangled.size());

        assertEquals("123", unmangled.get(0));
        assertEquals("456", unmangled.get(1));
    }

}
