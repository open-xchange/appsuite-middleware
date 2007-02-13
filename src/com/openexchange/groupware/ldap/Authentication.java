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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.groupware.contexts.Context;

/**
 * This interface provides methods to find users in the directory service and
 * to authenticate them against the directory service. This class is implemented
 * according the DAO design pattern.
 * @author <a href="mailto:marcus@open-xchange.de">Marcus Klein </a>
 */
public abstract class Authentication {

    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class< ? extends Authentication> implementingClass;

    /**
     * Creates a new instance implementing the authentication interface.
     * @param context Context.
     * @return an instance implementing the authentication interface.
     * @throws LdapException if the instance can't be created.
     */
    public static Authentication getInstance(final Context context)
        throws LdapException {
        synchronized (Authentication.class) {
            if (null == implementingClass) {
                final String className = LdapUtility.findProperty(Names.
                    AUTHENTICATION_IMPL);
                implementingClass = LdapUtility.getImplementation(className,
                    Authentication.class);
            }
        }
        return LdapUtility.getInstance(implementingClass, context);
    }

    /**
     * Authenticates a user.
     * @param uid Identifier of the user. This is the full bind distinguished
     * name.
     * @param password plain text password of the user.
     * @return The credentials object that must be used then to authenticate the
     * user to the ldap interface or <code>null</code> if the authentication
     * fails.
     * @throws LdapException if an error occurs during authentication process.
     */
    public abstract Credentials authenticate(final String uid,
        final String password) throws LdapException;

    /**
     * Checks if a user is enabled.
     * A user is enabled if its attribute mailEnabled contains the value true.
     * @param uid the login name of the user.
     * @return true if the user is enabled.
     * @throws LdapException if an error occurs while reading from ldap.
     */
    public abstract boolean isUserEnabled(String uid) throws LdapException;

    /**
     * Checks if the password of the user is expired. If the password is expired
     * the user has to change it.
     * @param uid the login name of the user.
     * @return true if the password of the user is expired.
     * @throws LdapException if an error occurs while reading from ldap.
     */
    public abstract boolean isPasswordExpired(String uid) throws LdapException;
}
