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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.AssertionFailedError;
import org.junit.Test;

/**
 * {@link UserAttributeDiffTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class UserAttributeDiffTest {

    public UserAttributeDiffTest() {
        super();
    }

    @Test
    public void testEmpty() {
        Map<String, Set<String>> oldAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> newAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> added = new HashMap<String, Set<String>>();
        Map<String, Set<String>> removed = new HashMap<String, Set<String>>();
        Map<String, Set<String[]>> changed = new HashMap<String, Set<String[]>>();
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertTrue(oldAttributes.isEmpty());
        assertTrue(newAttributes.isEmpty());
        assertValues(added, N(), MV());
        assertValues(removed, N(), MV());
        assertTrue(changed.isEmpty());
    }

    @Test
    public void testAddedAttribute() {
        Map<String, Set<String>> oldAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> newAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> added = new HashMap<String, Set<String>>();
        Map<String, Set<String>> removed = new HashMap<String, Set<String>>();
        Map<String, Set<String[]>> changed = new HashMap<String, Set<String[]>>();
        oldAttributes.put("alias", V("marcus.klein@premium", "mk@premium"));
        newAttributes.putAll(oldAttributes);
        newAttributes.put("newKey", V("newValue"));
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertValues(added, N("newKey"), MV(V("newValue")));
        assertValues(removed, N(), MV());
        assertTrue(changed.isEmpty());
    }

    @Test
    public void testChangedAttribute() {
        Map<String, Set<String>> oldAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> newAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> added = new HashMap<String, Set<String>>();
        Map<String, Set<String>> removed = new HashMap<String, Set<String>>();
        Map<String, Set<String[]>> changed = new HashMap<String, Set<String[]>>();
        oldAttributes.put("alias", V("marcus.klein@premium", "mk@premium"));
        String expectedOldValue = Long.toString(System.currentTimeMillis());
        oldAttributes.put("client:testClient", V(expectedOldValue));
        newAttributes.putAll(oldAttributes);
        String expectedNewValue = Long.toString(System.currentTimeMillis()+1);
        newAttributes.put("client:testClient", V(expectedNewValue));
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertValues(added, N(), MV());
        assertValues(removed, N(), MV());
        assertChanges(changed, N("client:testClient"), MC(S(C(expectedOldValue, expectedNewValue))));
    }

    @Test
    public void testMultipleChangedValue() {
        Map<String, Set<String>> oldAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> newAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> added = new HashMap<String, Set<String>>();
        Map<String, Set<String>> removed = new HashMap<String, Set<String>>();
        Map<String, Set<String[]>> changed = new HashMap<String, Set<String[]>>();
        oldAttributes.put("alias", V("marcus.klein@premium", "mk@premium", "ma@premium"));
        newAttributes.put("alias", V("marcus.klein@premium", "mc@premium", "mr@premium"));
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertValues(added, N(), MV());
        assertValues(removed, N(), MV());
        // We don't know in which order the diff algorithm will generate the change of values. So test both ones.
        try {
            assertChanges(changed, N("alias"), MC(S(C("mk@premium", "mr@premium"), C("ma@premium", "mc@premium"))));
        } catch (AssertionFailedError e) {
            assertChanges(changed, N("alias"), MC(S(C("mk@premium", "mc@premium"), C("ma@premium", "mr@premium"))));
        }
    }

    @Test
    public void testFakeMultipleChangedValue() {
        Map<String, Set<String>> oldAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> newAttributes = new HashMap<String, Set<String>>();
        Map<String, Set<String>> added = new HashMap<String, Set<String>>();
        Map<String, Set<String>> removed = new HashMap<String, Set<String>>();
        Map<String, Set<String[]>> changed = new HashMap<String, Set<String[]>>();
        oldAttributes.put("alias", V("marcus.klein@premium", "mk@premium", "ma@premium"));
        newAttributes.put("alias", V("marcus.klein@premium", "ma@premium", "mr@premium"));
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertValues(added, N(), MV());
        assertValues(removed, N(), MV());
        assertChanges(changed, N("alias"), MC(S(C("mk@premium", "mr@premium"))));
    }

    private static void assertValues(Map<String, Set<String>> attributes, String[] names, Set<String>[] values) {
        assertEquals(names.length, attributes.size());
        for (int i = 0; i < names.length; i++) {
            assertTrue(attributes.containsKey(names[i]));
            assertEquals(attributes.get(names[i]), values[i]);
        }
    }

    private void assertChanges(Map<String, Set<String[]>> changeSets, String[] names, Set<String[]>[] changedValues) {
        assertEquals(names.length, changeSets.size());
        for (int i = 0; i < names.length; i++) {
            assertTrue(changeSets.containsKey(names[i]));
            Set<String[]> actual = changeSets.get(names[i]);
            Set<String[]> expected = changedValues[i];
            assertEquals(expected.size(), actual.size());
            for (String[] expectedChange : expected) {
                boolean found = false;
                for (String[] actualChange : actual) {
                    if (expectedChange[0].equals(actualChange[0]) && expectedChange[1].equals(actualChange[1])) {
                        found = true;
                    }
                }
                assertTrue(found);
            }
        }
    }

    private static final Set<String> V(String... values) {
        Set<String> retval = new HashSet<String>();
        for (String value : values) {
            retval.add(value);
        }
        return retval;
    }

    private static final String[] C(String oldValue, String newValue) {
        return new String[] { oldValue, newValue };
    }

    private static final Set<String[]> S(String[]... changes) {
        Set<String[]> retval = new HashSet<String[]>();
        for (String[] change : changes) {
            retval.add(change);
        }
        return retval;
    }

    private static final Set<String[]>[] MC(Set<String[]>... changeSets) {
        return changeSets;
    }

    private static final String[] N(String... keys) {
        return keys;
    }

    private static final Set<String>[] MV(Set<String>... valueSets) {
        return valueSets;
    }
}
