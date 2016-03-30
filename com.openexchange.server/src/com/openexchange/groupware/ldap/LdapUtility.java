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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.configuration.DirectoryService;
import com.openexchange.tools.file.TagFiller;
import com.openexchange.tools.file.TagFillerAdapter;
import com.openexchange.tools.tag.LineParserUtility;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein </a>
 */
public final class LdapUtility {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LdapUtility.class);

    /**
     * Empty map.
     */
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

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
     * Returns the base distinguished name under that a search will take place.
     * This method should only be used if a search base dn for users is needed.
     * @param propname name of the property containing the search base dn.
     * @param cred credentials for ldap authentication.
     * @return the appropriate base dn for the search.
     * @throws OXException if the property can't be found.
     */
    static String getSearchBaseDN(final String propname, final Credentials cred)
        throws OXException {
        String retval = findProperty(propname, true);
        if (retval.length() > 0 && retval.charAt(0) == '['
            && retval.charAt(retval.length() - 1) == ']') {
            if (retval.indexOf('[', 1) == -1) {
                retval = cred.getValue(retval.substring(1,
                    retval.length() - 1));
            } else {
                retval = LineParserUtility.parseLine(retval, FILLER,
                    new TagFillerData(cred, EMPTY_MAP));
            }
        }
        return retval;
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
     * This class is a container for the temporary data for the TagFiller.
     */
    private static class TagFillerData {

        /**
         * Constructor to create a data object for the TagFiller.
         * @param values user specific values to read the correct data from the
         * ldap.
         * @param datamap dynamically replaced content.
         */
        public TagFillerData(final Credentials values,
            final Map<String, String> datamap) {
            this.values = values;
            this.datamap = datamap;
        }

        /**
         * User specific values to read the correct data from the ldap.
         */
        private final Credentials values;

        /**
         * Dynamically replaced content.
         */
        private final Map<String, String> datamap;
    }

    /**
     * This TagFiller will be used to replace the dynamic content of the
     * customization.
     */
    private static final TagFiller FILLER = new TagFillerAdapter() {

        /**
         * {@inheritDoc}
         */
        @Override
		public String replace(final String tag, final Object data) {
            final TagFillerData temp = (TagFillerData) data;
            String retval = tag;
            if (temp.datamap != null && temp.datamap.containsKey(tag)) {
                retval = temp.datamap.get(tag);
            } else if (temp.values != null && tag.endsWith("BaseDN")) {
                try {
                    retval = LdapUtility.getSearchBaseDN(tag, temp.values);
                } catch (final OXException e) {
                    retval = e.getMessage();
                }
            } else {
                try {
                    final String property = LdapUtility.findProperty(tag,
                        false);
                    if (null != property && !property.equals('[' + tag + ']')) {
                        retval = LineParserUtility.parseLine(property, this,
                            data);
                    }
                } catch (final OXException e) {
                    LOG.error("", e);
                    retval = tag;
                }
            }
            return retval;
        }
    };

   /**
    * This method searches a the value of a property in the properties. The
    * property to search will only be preceded by the package name to find it.
    * This method can't be used to find class specific properties.
    * @param propname name of the property to search
    * @param mustExist <code>true</code> causes this method to throw a
    *        NamingException if the property can't be found.
    * @return the value of the property or <code>null</code> if the property
    * can't be found and <code>mustExist</code> is false.
    * @throws OXException if the property can't be found and the parameter
    * <code>mustExist</code> is <code>true</code>.
    */
   static String findProperty(final String propname, final boolean mustExist)
       throws OXException {
       final String retval = getCustomization().getProperty(propname);
       if (retval == null && mustExist) {
           throw LdapExceptionCode.PROPERTY_MISSING.create(propname).setPrefix(EnumComponent.LDAP.getAbbreviation());
       }
       return retval;
   }

    /**
     * This method searches a the value of a property in the properties. The
     * property to search will only be preceded by the package name to find it.
     * @param propname name of the property to search
     * @return the value of the property and never <code>null</code>.
     * @throws OXException if the property can't be found.
     */
    static String findProperty(final String propname) throws OXException {
        return findProperty(propname, true);
    }

    /**
     * Mutex for customization.
     */
    private static Object mutex = new Object();

    /**
     * Proxy attribute to store the customization properties.
     */
    private static volatile Properties customization;

    /**
     * Returns the properties for the customization of the ldap interface.
     * @return the properties for the customization of the ldap interface.
     */
    private static Properties getCustomization() {
        synchronized (mutex) {
            if (null == customization) {
                customization = DirectoryService.getCustomization();
            }
        }
        return customization;
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
        } catch (final ClassNotFoundException e) {
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
        } catch (final SecurityException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (final NoSuchMethodException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (final InstantiationException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (final IllegalAccessException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (final IllegalArgumentException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        } catch (final InvocationTargetException e) {
            throw LdapExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName()).setPrefix(EnumComponent.LDAP.getAbbreviation());
        }
    }
}
