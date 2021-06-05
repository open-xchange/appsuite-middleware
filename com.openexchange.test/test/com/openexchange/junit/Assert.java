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
        if (! expected.getClass().isAssignableFrom(actual.getClass())) {
            fail(message+" The class "+actual.getClass()+" is not compatible with "+expected.getClass());
        }
        BeanInfo beanInfo = Introspector.getBeanInfo(expected.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        boolean all = fields.length == 0;
        Set<String> fieldsToCompare = new HashSet<String>(Arrays.asList(fields));

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propName = propertyDescriptor.getName();
            if (all || fieldsToCompare.contains(propName)) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod == null && !all) {
                    fail("Can't read property "+propName);
                }
                if (readMethod == null) {
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
