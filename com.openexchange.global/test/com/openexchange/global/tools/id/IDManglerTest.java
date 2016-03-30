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

package com.openexchange.global.tools.id;


import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link IDManglerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IDManglerTest extends TestCase {

    public void testRoundtrip() {
        String id = IDMangler.mangle("com.openexchange.some.service", "someFolder", "someId");
        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("com.openexchange.some.service", "someFolder", "someId"), unmangled);

    }

    public void testSlashes() {
        String id = IDMangler.mangle("com.openexchange.some.service", "someFolder/folder/with/slashes", "someId");
        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("com.openexchange.some.service", "someFolder/folder/with/slashes", "someId"), unmangled);
    }

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

    public void testBrackets() {
        String id = IDMangler.mangle("some", "com[ponents", "wi]]th", "brackets");

        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("some", "com[ponents", "wi]]th", "brackets"), unmangled);
    }

    public void testBackslash() {
        String id = IDMangler.mangle("something\\with", "neato\\backslashes");
        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("something\\with", "neato\\backslashes"), unmangled);
    }

    public void testContainsPrimaryDelim() {
        List<String> unmangled = IDMangler.unmangle("some:/partial:id://component");
        assertEquals(Arrays.asList("some:/partial:id", "component"), unmangled);
    }

    public void testContainsFragmentsOfPrimaryDelim() {
        String id = IDMangler.mangle("some:/service:this is", "someFolder", "someId");
        assertNotNull(id);

        List<String> unmangled = IDMangler.unmangle(id);

        assertEquals(Arrays.asList("some:/service:this is", "someFolder", "someId"), unmangled);
    }

    public void testInfostoreFolderAndId() {
        List<String> unmangled = IDMangler.unmangle("123/456");

        assertEquals("Unexpected size", 2, unmangled.size());

        assertEquals("123", unmangled.get(0));
        assertEquals("456", unmangled.get(1));
    }

}
