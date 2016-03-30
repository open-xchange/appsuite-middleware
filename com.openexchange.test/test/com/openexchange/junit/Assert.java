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

package com.openexchange.junit;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link Assert}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Assert extends org.junit.Assert {

    protected Assert() {
        super();
    }

    public static <T> T assertSame(String message, T[] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array.length; j++) {
                assertEquals(message, array[i], array[j]);
            }
        }
        final T retval;
        if (0 == array.length) {
            retval = null;
        } else {
            retval = array[0];
        }
        return retval;
    }

    public static <T> T assertSame(String message, Collection<T> col) {
        // all objects must be compared with everyone.
        Iterator<T> iter1 = col.iterator();
        while (iter1.hasNext()) {
            T expected = iter1.next();
            Iterator<T> iter2 = col.iterator();
            while (iter2.hasNext()) {
                T actual = iter2.next();
                assertEquals(message, expected, actual);
            }
        }
        final T retval;
        if (col.isEmpty()) {
            retval = null;
        } else {
            retval = col.iterator().next();
        }
        return retval;
    }

    /**
     * Compare two bean style objects attributes. If given a list of field names only those fields will be compared,
     * otherwise all fields will be compared
     * @param message The message to display on failure
     * @param bean1 The expected values
     * @param bean2 The actual values
     * @param fields The fields to compare, if empty, compares all fields.
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static void assertEqualAttributes(String message, Object expected, Object actual, String...fields) throws Exception {
        if(! expected.getClass().isAssignableFrom(actual.getClass())) {
            fail(message+" The class "+actual.getClass()+" is not compatible with "+expected.getClass());
        }
        BeanInfo beanInfo = Introspector.getBeanInfo(expected.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        boolean all = fields.length == 0;
        Set<String> fieldsToCompare = new HashSet<String>(Arrays.asList(fields));

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propName = propertyDescriptor.getName();
            if(all || fieldsToCompare.contains(propName)) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if(readMethod == null && !all) {
                    fail("Can't read property "+propName);
                }
                if(readMethod == null) {
                    continue;
                }
                Object expectedValue = readMethod.invoke(expected);
                Object actualValue = readMethod.invoke(actual); // Hopefully I can reuse a Method object in compatible class definitions.
                assertEquals(message+" Attribute: "+propName, expectedValue, actualValue);
            }
        }
    }

    public static void assertEqualAttributes(Object expected, Object actual, String...fields) throws Exception {
        assertEqualAttributes("Attributes mismatch", expected, actual, fields);
    }
}
