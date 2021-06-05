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

package com.openexchange.test.fixtures;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Fixture<T> {

    private final T entry;
    private final String[] fields;
    private final Map<String, String> attributes;

    public Fixture(final T entry, final String[] fields, Map<String, String> attributes) {
        this.entry = entry;
        this.fields = fields;
        this.attributes = attributes;
    }

    public boolean matches(final T other) throws OXException {

        final Class<?> klass = entry.getClass();

        for (String field : fields) {
            try {
                final Method get = getMethod(field, klass);
                if (get == null) {
                    continue; // Skip fields we don't have access to.
                }
                final Object v1 = get.invoke(entry);
                final Object v2 = get.invoke(other);

                if (v1 == null && v2 != null) {
                    return false;
                }

                if (v1 != null && v2 == null) {
                    return false;
                }
                final Comparator<Object> comp = getComparator(field);
                if (comp != null && comp.compare(v1, v2) != 0) {
                    return false;
                }
                if (v1 == null || comp == null && !v1.equals(v2)) {
                    return false;
                }

            } catch (IllegalAccessException e) {
                throw new FixtureException(e);
            } catch (InvocationTargetException e) {
                throw new FixtureException(e);
            }
        }

        return true;
    }

    private Method getMethod(final String field, final Class<?> klass) {
        for (Method m : klass.getMethods()) {
            if (m.getName().equalsIgnoreCase(IntrospectionTools.getterName(field)) && m.getParameterTypes().length == 0) {
                return m;
            }
        }
        //throw new FixtureException("Don't know how to read "+field);
        return null;
    }

    public T getEntry() {
        return entry;
    }

    // Override me!
    public Comparator<Object> getComparator(@SuppressWarnings("unused") final String field) {
        return null;
    }

    public Object getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }
}
