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

    public Fixture(final T entry, final String[] fields, Map attributes) {
        this.entry = entry;
        this.fields = fields;
        this.attributes = attributes;
    }

    public boolean matches(final T other) throws OXException {

    	final Class<?> klass = entry.getClass();

        for(String field : fields) {
            try {
                final Method get = getMethod(field, klass);
                if(get == null) {
                    continue; // Skip fields we don't have access to.
                }
                final Object v1 = get.invoke(entry);
                final Object v2 = get.invoke(other);

                if(v1 == null && v2 != null) {
                    return false;
                }

                if(v1 != null && v2 == null) {
                    return false;
                }
                final Comparator<Object> comp = getComparator(field);
                if(comp != null && comp.compare(v1, v2) != 0) {
                    return false;
                }
                if(comp == null && !v1.equals(v2)) {
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

    private Method getMethod(final String field, final Class<?> klass) throws OXException {
        for(Method m : klass.getMethods()) {
            if(m.getName().equalsIgnoreCase(IntrospectionTools.getterName(field)) && m.getParameterTypes().length == 0) {
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
    public Comparator<Object> getComparator(final String field) {
    	return null;
    }

    public Object getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }
}
