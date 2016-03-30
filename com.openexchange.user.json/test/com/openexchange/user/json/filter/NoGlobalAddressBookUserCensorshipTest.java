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

package com.openexchange.user.json.filter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.user.json.field.UserField;


/**
 * {@link NoGlobalAddressBookUserCensorshipTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NoGlobalAddressBookUserCensorshipTest extends TestCase {
    public void testCensorsData() throws Exception {
        User user = getFilledUser();

        final UserCensorship censorship = new NoGlobalAdressBookUserCensorship();

        user = censorship.censor(user);

        for(final UserField field : UserField.ALL_FIELDS) {
            if(!UserField.isUserOnlyField(field.getColumn())){
                continue;
            }
            final Object value = get(field, user);
            if(UserField.UNPROTECTED_FIELDS.contains(field)) {
                assertFalse("Should have remained untouched: "+field, value != null);
            } else {
                assertTrue("Should have been hidden: "+field, value == null || -1 == (Integer)value);
            }
        }

    }

    private Object get(final UserField field, final User user) {
        switch(field) {
        case ALIASES: return user.getAliases();
        case TIME_ZONE: return user.getTimeZone();
        case LOCALE: return user.getLocale();
        case GROUPS: return user.getGroups();
        case CONTACT_ID: return user.getContactId();
        case LOGIN_INFO: return user.getLoginInfo();
        }
        return null;
    }

    private User getFilledUser() throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final User user = new UserImpl();
        final BeanInfo beanInfo = Introspector.getBeanInfo(UserImpl.class);

        final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final Method writeMethod = propertyDescriptor.getWriteMethod();
            if(writeMethod != null) {
                final Object value = DEFAULT_VALUES.get(writeMethod.getParameterTypes()[0]);
                if(value != null) {
                    writeMethod.invoke(user, value);
                } else {
                    System.err.println("No value found for setter: "+writeMethod);
                }
            }
        }

        return user;
    }


    private static final Map <Class, Object> DEFAULT_VALUES = new HashMap<Class, Object>() {{
        put(String.class, "Some String");
        put(Date.class, new Date());
        put(Integer.class, 12);
        put(int.class, 12);
        put(byte[].class, new byte[0]);
        put(Boolean.class, true);
        put(boolean.class, true);
        put(Locale.class, Locale.getDefault());
        put(String[].class, new String[0]);
    }};

}
