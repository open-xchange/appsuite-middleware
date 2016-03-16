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

package com.openexchange.groupware.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        Map<String, UserAttribute> oldAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> newAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> added = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> removed = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> changed = new HashMap<String, UserAttribute>();
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertTrue(oldAttributes.isEmpty());
        assertTrue(newAttributes.isEmpty());
        assertValues(added, N(), MV());
        assertValues(removed, N(), MV());
        assertTrue(changed.isEmpty());
    }

    @Test
    public void testAddedAttribute() {
        Map<String, UserAttribute> oldAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> newAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> added = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> removed = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> changed = new HashMap<String, UserAttribute>();
        oldAttributes.put("alias", V("alias", "marcus.klein@premium", "mk@premium"));
        newAttributes.putAll(oldAttributes);
        newAttributes.put("newKey", V("newKey", "newValue"));
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertValues(added, N("newKey"), MV(V("newKey", "newValue")));
        assertValues(removed, N(), MV());
        assertTrue(changed.isEmpty());
    }

    @Test
    public void testChangedAttribute() {
        Map<String, UserAttribute> oldAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> newAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> added = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> removed = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> changed = new HashMap<String, UserAttribute>();
        oldAttributes.put("alias", V("alias", "marcus.klein@premium", "mk@premium"));
        String expectedOldValue = Long.toString(System.currentTimeMillis());
        oldAttributes.put("client:testClient", V("client:testClient", expectedOldValue));
        newAttributes.putAll(oldAttributes);
        String expectedNewValue = Long.toString(System.currentTimeMillis()+1);
        newAttributes.put("client:testClient", V("client:testClient", expectedNewValue));
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertValues(added, N(), MV());
        assertValues(removed, N(), MV());
        assertChanges(changed, N("client:testClient"), MC(S(C(expectedOldValue, expectedNewValue))));
    }

    @Test
    public void testMultipleChangedValue() {
        Map<String, UserAttribute> oldAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> newAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> added = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> removed = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> changed = new HashMap<String, UserAttribute>();
        oldAttributes.put("alias", V("alias","marcus.klein@premium", "mk@premium", "ma@premium"));
        newAttributes.put("alias", V("alias","marcus.klein@premium", "mc@premium", "mr@premium"));
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertValues(added, N(), MV());
        assertValues(removed, N(), MV());
        // We don't know in which order the diff algorithm will generate the change of values. So test both ones.
        try {
            assertChanges(changed, N("alias"), MC(S(C("mk@premium", "mc@premium"), C("ma@premium", "mr@premium"))));
        } catch (AssertionError e) {
            assertChanges(changed, N("alias"), MC(S(C("mk@premium", "mr@premium"), C("ma@premium", "mc@premium"))));
        }
    }

    @Test
    public void testFakeMultipleChangedValue() {
        Map<String, UserAttribute> oldAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> newAttributes = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> added = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> removed = new HashMap<String, UserAttribute>();
        Map<String, UserAttribute> changed = new HashMap<String, UserAttribute>();
        oldAttributes.put("alias", V("alias","marcus.klein@premium", "mk@premium", "ma@premium"));
        newAttributes.put("alias", V("alias","marcus.klein@premium", "ma@premium", "mr@premium"));
        RdbUserStorage.calculateDifferences(oldAttributes, newAttributes, added, removed, changed);
        assertValues(added, N(), MV());
        assertValues(removed, N(), MV());
        assertChanges(changed, N("alias"), MC(S(C("mk@premium", "mr@premium"))));
    }

    private static void assertValues(Map<String, UserAttribute> added, String[] names, UserAttribute[] values) {
        assertEquals(names.length, added.size());
        for (int i = 0; i < names.length; i++) {
            assertTrue(added.containsKey(names[i]));
            assertEquals(added.get(names[i]), values[i]);
        }
    }

    private static void assertChanges(Map<String, UserAttribute> changed, String[] names, Set<AttributeValue>[] sets) {
        assertEquals(names.length, changed.size());
        for (int i = 0; i < names.length; i++) {
            assertTrue(changed.containsKey(names[i]));
            Set<AttributeValue> actual = changed.get(names[i]).getValues();
            Set<AttributeValue> expected = sets[i];
            assertEquals(expected.size(), actual.size());
            for (AttributeValue expectedChange : expected) {
                boolean found = false;
                for (AttributeValue actualChange : actual) {
                    if (expectedChange.getValue().equals(actualChange.getValue()) && expectedChange.getNewValue().equals(actualChange.getNewValue())) {
                        found = true;
                    }
                }
                assertTrue(found);
            }
        }
    }

    private static final UserAttribute V(String name, String... values) {
        UserAttribute retval = new UserAttribute(name);
        for (String value : values) {
            retval.addValue(value);
        }
        return retval;
    }

    private static final AttributeValue C(String oldValue, String newValue) {
        return new AttributeValue(oldValue, null, newValue);
    }

    private static final Set<AttributeValue> S(AttributeValue... changes) {
        Set<AttributeValue> retval = new HashSet<AttributeValue>();
        for (AttributeValue change : changes) {
            retval.add(change);
        }
        return retval;
    }

    private static final Set<AttributeValue>[] MC(Set<AttributeValue>... changeSets) {
        return changeSets;
    }

    private static final String[] N(String... keys) {
        return keys;
    }

    private static final UserAttribute[] MV(UserAttribute... valueSets) {
        return valueSets;
    }
}
