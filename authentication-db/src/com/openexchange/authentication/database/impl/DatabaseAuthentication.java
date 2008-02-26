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

package com.openexchange.authentication.database.impl;

import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.IAuthenticated;
import com.openexchange.authentication.ILoginInfo;
import com.openexchange.authentication.LoginException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.groupware.ldap.UserStorage;

/**
 * This implementation authenticates the user against the database.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class DatabaseAuthentication implements AuthenticationService {

    /**
     * Default constructor.
     */
    public DatabaseAuthentication() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public IAuthenticated handleLoginInfo(final ILoginInfo loginInfo)
        throws LoginException {
        final String password = loginInfo.getPassword();
        if (null == password || 0 == password.length()) {
            throw new LoginException(LoginException.Code.INVALID_CREDENTIALS);
        }
        final String[] splitted = split(loginInfo.getUsername());
        final ContextStorage ctxStor = ContextStorage.getInstance();
        try {
            final int ctxId = ctxStor.getContextId(splitted[0]);
            if (ContextStorage.NOT_FOUND == ctxId) {
                throw new LoginException(LoginException.Code
                    .INVALID_CREDENTIALS);
            }
            final Context ctx = ctxStor.getContext(ctxId);
            final UserStorage userStor = UserStorage.getInstance();
            final int userId;
            try {
                userId = userStor.getUserId(splitted[1], ctx);
            } catch (LdapException e) {
                throw new LoginException(LoginException.Code
                    .INVALID_CREDENTIALS, e);
            }
            final User user = userStor.getUser(userId, ctx);
            if (!userStor.authenticate(user, password)) {
                throw new LoginException(LoginException.Code
                    .INVALID_CREDENTIALS);
            }
        } catch (ContextException e) {
            throw new LoginException(e);
        } catch (LdapException e) {
            throw new LoginException(e);
        } catch (UserException e) {
            throw new LoginException(e);
        }
        return new IAuthenticated() {
            public String getContextInfo() {
                return splitted[0];
            }
            public String getUserInfo() {
                return splitted[1];
            }
        };
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no seperator is found.
     */
    private String[] split(final String loginInfo) throws LoginException {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @param separator for spliting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no seperator is found.
     */
    private String[] split(final String loginInfo, final char separator) {
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
