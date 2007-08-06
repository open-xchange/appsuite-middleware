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

package com.openexchange.groupware.contexts;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.SystemConfig.Property;
import com.openexchange.sessiond.LoginException;

/**
 * This interface defines the methods for handling the login information. E.g.
 * the login information <code>user@domain.tld</code> is split into
 * <code>user</code> and <code>domain.tld</code> and the context part will be
 * used to resolve the context while the user part will be used to authenticate
 * the user.
 * TODO move to com.openexchange.groupware.login
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class LoginInfo {

    /**
     * Reference to the class implementing the LoginInfo.
     */
    private static Class< ? extends LoginInfo> implementingClass;

    /**
     * Default constructor.
     */
    protected LoginInfo() {
        super();
    }

    /**
     * Creates an instance implementing the login info.
     * @return an instance implementing the login info.
     * @throws LoginException if instanciation fails.
     */
    public static LoginInfo getInstance() throws LoginException {
        LoginInfo instance = null;
        try {
            instance = implementingClass.newInstance();
        } catch (InstantiationException e) {
            throw new LoginException(LoginException.Code.INSTANCIATION_FAILED,
                e);
        } catch (IllegalAccessException e) {
            throw new LoginException(LoginException.Code.INSTANCIATION_FAILED,
                e);
        }
        return instance;
    }

    /**
     * This method maps the login information from the login screen to the both
     * parts needed to resolve the context and the user of that context.
     * @param loginInfo the complete login informations from the login screen.
     * @return a string array with two elements in which the first contains the
     * login info for the context and the second contains the login info for the
     * user.
     * @throws LoginException if something with the login info is wrong.
     */
    public abstract String[] handleLoginInfo(Object... loginInfo)
        throws LoginException;

    /**
     * Initializes the login info implementation.
     * @throws LoginException if initialization fails.
     */
    public static void init() throws LoginException {
        if (null != implementingClass) {
            return;
        }
        final String className = SystemConfig.getProperty(Property.LOGIN_INFO);
        if (null == className) {
            throw new LoginException(LoginException.Code.MISSING_SETTING,
                Property.LOGIN_INFO.getPropertyName());
        }
        try {
            implementingClass = Class.forName(className)
                .asSubclass(LoginInfo.class);
        } catch (ClassNotFoundException e) {
            throw new LoginException(LoginException.Code.CLASS_NOT_FOUND, e,
                className);
        } catch (ClassCastException e) {
            throw new LoginException(LoginException.Code.CLASS_NOT_FOUND, e,
                className);
        }
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no seperator is found.
     */
    protected String[] split(final String loginInfo) throws LoginException {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @param character for spliting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no seperator is found.
     */
    protected String[] split(final String loginInfo, final char separator) {
        final int pos = loginInfo.lastIndexOf(separator);
        final String[] splitted = new String[2];
        if (-1 == pos) {
            splitted[1] = loginInfo;
            splitted[0] = "defaultcontext";
        } else {
            splitted[1] = loginInfo.substring(0, pos);
            splitted[0] = loginInfo.substring(pos + 1);
        }
        return splitted;
    }
}
