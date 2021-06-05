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

package com.openexchange.groupware.ldap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EnumComponent;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein </a>
 */
public final class LdapUtility {

    /**
     * Logger.
     */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LdapUtility.class);

    /**
     * Private constructor prevents instantiation.
     */
    private LdapUtility() {
        super();
    }

    /**
     * Sets the username and the password of a context. This is usefull if you
     * want to access contents of the LDAP that is specific for a special user.
     * E.g. the personal address book.
     * @param context the context in which to set the login.
     * @param login the distinguished name of the user.
     * @param pass password of the user.
     * @throws NamingException if an error occurs.
     */
    public static void setLogin(final LdapContext context, final String login,
        final String pass) throws NamingException {
        if (null == context) {
            return;
        }
        context.addToEnvironment(Context.SECURITY_PRINCIPAL, login);
        context.addToEnvironment(Context.SECURITY_CREDENTIALS, pass);
        context.reconnect(null);
    }

    /**
     * Removes any username and password from the context. This should be done
     * to return the context to the ldap connection pool for reuse.
     * @param context the context to remove the authentication from.
     * @throws NamingException if an error occurs.
     */
    public static void removeLogin(final LdapContext context)
        throws NamingException {
        if (null == context) {
            return;
        }
        context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
        context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
    }

    /**
     * Appends two directory service names without affecting the given names.
     * E.g. appends <code>uid=testuser</code> and <code>dc=example,dc=org</code>
     * to <code>uid=testuser,dc=example,dc=org</code>.
     * @param name1 Name that will be the first part in string representation
     *        and downmost part in the directory service tree.
     * @param name2 Name that will be the second part in string representation
     *        and topmost part in the directory service tree.
     * @return the appended names
     * @throws InvalidNameException if an error occurs while appending the
     *         names.
     */
    public static Name append(final Name name1, final Name name2)
        throws InvalidNameException {
        final Name retval = (Name) name2.clone();
        retval.addAll(name1);
        return retval;
    }

    /**
     * This method prepare some pattern to make it useful for a relational database search.
     * <p>
     * The pattern is surrounded by wild-cards and the wild-card character <code>'*'</code> is replaced with its database pendant
     * <code>'%'</code>.
     *
     * @param pattern The pattern to modify
     * @return The modified pattern or <code>null</code> if <code>pattern</code> is <code>null</code>
     */
    public static String prepareSearchPattern(final String pattern) {
        if (null == pattern) {
            return null;
        }
        final StringBuilder modifiedPattern = new StringBuilder(pattern.replace('*', '%'));
        if (modifiedPattern.length() == 0) {
            modifiedPattern.append('%');
        }
        if (modifiedPattern.charAt(0) != '%') {
            modifiedPattern.insert(0, '%');
        }
        if (modifiedPattern.charAt(modifiedPattern.length() - 1) != '%') {
            modifiedPattern.append('%');
        }
        return modifiedPattern.toString();
    }

    /**
     * Method for getting a class implementing an interface.
     * @param <U> Type of the class.
     * @param className Name of the class.
     * @param clazz super type of the class to load.
     * @return the class.
     * @throws OXException if the class can not be loaded.
     */
    public static <U> Class< ? extends U> getImplementation(
        final String className, final Class<U> clazz) throws OXException {
        try {
            return Class.forName(className).asSubclass(clazz);
        } catch (ClassNotFoundException e) {
            throw LdapExceptionCode.CLASS_NOT_FOUND.create(e, className).setPrefix(EnumComponent.LDAP.getAbbreviation());
        }
    }

    /**
     * Creates a new instance implementing the resources interface.
     * @param <T> Type of the class.
     * @param clazz class that should be instantiated.
     * @param context Context.
     * @return an instance implementing the resources interface.
     * @throws OXException if the instance can't be created.
     */
    public static <T extends Object> T getInstance(final Class< ? extends T> clazz)
        throws OXException {
        try {
            final Constructor< ? extends T> cons = clazz.getConstructor(new Class[0]);
            return cons.newInstance(new Object[0]);
        } catch (SecurityException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (NoSuchMethodException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (InstantiationException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (IllegalAccessException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (IllegalArgumentException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (InvocationTargetException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        }
    }
}
