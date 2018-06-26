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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.User;

/**
 * {@link AssertUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class AssertUtil {

    /*** Asserts for mandatory fields ***/

    /**
     * Asserts that the <code>expected</code> user is equal to the <code>actual</code>.
     * Only the mandatory fields are asserted.
     * 
     * @param expected The expected {@link User}
     * @param actual The actual {@link User}
     */
    public static void assertUserEquals(User expected, User actual) {
        assertEquals("Name should match", expected.getName(), actual.getName());
        assertEquals("Display name should match", expected.getDisplay_name(), actual.getDisplay_name());
        assertEquals("Given name should match", expected.getGiven_name(), actual.getGiven_name());
        assertEquals("Surname should match", expected.getSur_name(), actual.getSur_name());
        assertEquals("Primary E-Mail should match", expected.getPrimaryEmail(), actual.getPrimaryEmail());
        assertEquals("E-Ma0il #1 should match", expected.getEmail1(), actual.getEmail1());
    }

    /**
     * Asserts that the groups are equal
     * 
     * @param expected The expected {@link Group}
     * @param actual The actual {@link Group}
     */
    public static void assertGroupEquals(Group expected, Group actual) {
        assertEquals("Display name should match", expected.getDisplayname(), actual.getDisplayname());
        assertEquals("Name should match", expected.getName(), actual.getName());
    }

    /**
     * Asserts that the expected {@link Resource} is equal the actula {@link Resource}
     * 
     * @param expected The expected {@link Resource}
     * @param actual The actual {@link Resource}
     */
    public static void assertResourceEquals(Resource expected, Resource actual) {
        assertEquals("Display name should match", expected.getDisplayname(), actual.getDisplayname());
        assertEquals("Name should match", expected.getName(), actual.getName());
        assertEquals("E-Mail should match", expected.getEmail(), actual.getEmail());
    }

    /**
     * Compares two user arrays by retrieving all the IDs they contain
     * an checking if they match. Ignores duplicate entries, ignores
     * users without an ID at all.
     * 
     * @param arr1 the first array
     * @param arr2 the second array
     */
    public static void assertIDsAreEqual(User[] arr1, User[] arr2) {
        Set<Integer> set1 = new HashSet<Integer>();
        for (User element : arr1) {
            set1.add(element.getId());
        }
        Set<Integer> set2 = new HashSet<Integer>();
        for (int i = 0; i < arr1.length; i++) {
            set2.add(arr2[i].getId());
        }

        assertEquals("Both arrays should return the same IDs", set1, set2);
    }
}
