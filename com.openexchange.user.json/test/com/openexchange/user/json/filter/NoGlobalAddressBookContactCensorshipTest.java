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

package com.openexchange.user.json.filter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.groupware.container.Contact;
import com.openexchange.user.json.field.UserField;


/**
 * {@link NoGlobalAddressBookContactCensorshipTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NoGlobalAddressBookContactCensorshipTest extends TestCase{


    public void testCensorsData() throws Exception {
        final Contact contact = getFilledContact();

        final ContactCensorship censorship = new NoGlobalAdressBookContactCensorship();

        censorship.censor(contact);

        for(final UserField field : UserField.ALL_FIELDS) {
            if(UserField.isUserOnlyField(field.getColumn())){
                continue;
            }
            if(UserField.UNPROTECTED_FIELDS.contains(field)) {
                assertTrue("Should have remained untouched: "+field, contact.contains(field.getColumn()));
            } else {
                assertFalse("Should have been hidden: "+field, contact.contains(field.getColumn()));
            }
        }

    }

    private Contact getFilledContact() throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Contact contact = new Contact();
        final BeanInfo beanInfo = Introspector.getBeanInfo(Contact.class);

        final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final Method writeMethod = propertyDescriptor.getWriteMethod();
            if(writeMethod != null) {
                final Object value = DEFAULT_VALUES.get(writeMethod.getParameterTypes()[0]);
                if(value != null) {
                    writeMethod.invoke(contact, value);
                } else {
                    System.err.println("No value found for setter: "+writeMethod);
                }
            }
        }

        return contact;
    }

    private static final Map <Class, Object> DEFAULT_VALUES = new HashMap<Class, Object>() {{
        put(String.class, "Some String");
        put(Date.class, new Date());
        put(Integer.class, 12);
        put(int.class, 12);
        put(byte[].class, new byte[0]);
        put(Boolean.class, true);
        put(boolean.class, true);
    }};

}
