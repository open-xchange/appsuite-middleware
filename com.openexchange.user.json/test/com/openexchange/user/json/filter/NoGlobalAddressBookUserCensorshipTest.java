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

package com.openexchange.user.json.filter;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import com.google.common.collect.ImmutableMap;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.user.User;
import com.openexchange.user.json.field.UserField;

/**
 * {@link NoGlobalAddressBookUserCensorshipTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NoGlobalAddressBookUserCensorshipTest {

    @Test
    public void testCensorsData() throws Exception {
        User user = getFilledUser();

        final UserCensorship censorship = new NoGlobalAdressBookUserCensorship();

        user = censorship.censor(user);

        for (final UserField field : UserField.ALL_FIELDS) {
            if (!UserField.isUserOnlyField(field.getColumn())) {
                continue;
            }
            final Object value = get(field, user);
            if (UserField.UNPROTECTED_FIELDS.contains(field)) {
                assertFalse("Should have remained untouched: " + field, value != null);
            } else {
                assertTrue("Should have been hidden: " + field, value == null || -1 == ((Integer) value).intValue());
            }
        }

    }

    private Object get(final UserField field, final User user) {
        switch (field) {
            case ALIASES:
                return user.getAliases();
            case TIME_ZONE:
                return user.getTimeZone();
            case LOCALE:
                return user.getLocale();
            case GROUPS:
                return user.getGroups();
            case CONTACT_ID:
                return I(user.getContactId());
            case LOGIN_INFO:
                return user.getLoginInfo();
            default:
                return null;
        }
    }

    private User getFilledUser() throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final User user = new UserImpl();
        final BeanInfo beanInfo = Introspector.getBeanInfo(UserImpl.class);

        final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final Method writeMethod = propertyDescriptor.getWriteMethod();
            if (writeMethod != null) {
                final Object value = DEFAULT_VALUES.get(writeMethod.getParameterTypes()[0]);
                if (value != null) {
                    writeMethod.invoke(user, value);
                } else {
                    System.err.println("No value found for setter: " + writeMethod);
                }
            }
        }

        return user;
    }

    private static final Map <Class<?>, Object> DEFAULT_VALUES = ImmutableMap.<Class<?>, Object>builder().
        put(String.class, "Some String").
        put(Date.class, new Date()).
        put(Integer.class, I(12)).
        put(int.class, I(12)).
        put(byte[].class, new byte[0]).
        put(Boolean.class, B(true)).
        put(boolean.class, B(true)).
        put(Locale.class, Locale.getDefault()).
        put(String[].class, new String[0]).build();

}
