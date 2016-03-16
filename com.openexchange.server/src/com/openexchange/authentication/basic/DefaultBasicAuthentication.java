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

package com.openexchange.authentication.basic;

import static com.openexchange.authentication.LoginExceptionCodes.COMMUNICATION;
import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS;
import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING;
import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.BasicAuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.user.UserService;

/**
 * This implementation authenticates the user against the database.
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class DefaultBasicAuthentication implements BasicAuthenticationService {

    private static final class AuthenticatedImpl implements Authenticated {

        private final String[] splitted;

        protected AuthenticatedImpl(String[] splitted) {
            this.splitted = splitted;
        }

        @Override
        public String getContextInfo() {
            return splitted[0];
        }

        @Override
        public String getUserInfo() {
            return splitted[1];
        }

    } // End of class AuthenticatedImpl

    // ------------------------------------------------------------------------------------------------------------------------------- //

    private final ContextService contextService;
    private final UserService userService;

    /**
     * Default constructor.
     */
    public DefaultBasicAuthentication(ContextService contextService, UserService userService) {
        super();
        this.contextService = contextService;
        this.userService = userService;
    }

    @Override
    public Authenticated handleLoginInfo(int userId, int contextId) throws OXException {
        Context ctx = contextService.getContext(contextId);
        User user = userService.getUser(userId, ctx);
        return new AuthenticatedImpl(new String[] {ctx.getLoginInfo()[0], user.getLoginInfo()});
    }

    @Override
    public Authenticated handleLoginInfo(int userId, int contextId, String password) throws OXException {
        Context ctx = contextService.getContext(contextId);
        User user = userService.getUser(userId, ctx);

        if (!userService.authenticate(user, password)) {
            throw INVALID_CREDENTIALS.create();
        }
        return new AuthenticatedImpl(new String[] {ctx.getLoginInfo()[0], user.getLoginInfo()});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo)
        throws OXException {
        final String password = loginInfo.getPassword();
        if (null == password || 0 == password.length()) {
            throw INVALID_CREDENTIALS.create();
        }
        final String[] splitted = split(loginInfo.getUsername());
        try {
            final int ctxId;
            try {
                ctxId = contextService.getContextId(splitted[0]);
            } catch (final OXException e) {
                throw COMMUNICATION.create(e);
            }
            if (ContextStorage.NOT_FOUND == ctxId) {
                throw INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING.create(splitted[0]);
            }
            final Context ctx = contextService.getContext(ctxId);
            final int userId;
            try {
                userId = userService.getUserId(splitted[1], ctx);
            } catch (final OXException e) {
                if (e.equalsCode(LdapExceptionCode.USER_NOT_FOUND.getNumber(), UserExceptionCode.PROPERTY_MISSING.getPrefix())) {
                    throw INVALID_CREDENTIALS_MISSING_USER_MAPPING.create(splitted[1]);
                }
                throw e;
            }
            final User user = userService.getUser(userId, ctx);
            if (!userService.authenticate(user, password)) {
                throw INVALID_CREDENTIALS.create();
            }
        } catch (final OXException e) {
            throw e;
        }
        return new AuthenticatedImpl(splitted);
    }

    @Override
    public Authenticated handleAutoLoginInfo(LoginInfo loginInfo) throws OXException {
        throw LoginExceptionCodes.NOT_SUPPORTED.create(DefaultBasicAuthentication.class.getName());
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     */
    private String[] split(final String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @param separator for spliting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws OXException if no seperator is found.
     */
    private String[] split(final String loginInfo, final char separator) {
        final int pos = loginInfo.lastIndexOf(separator);
        final String[] splitted;
        if (-1 == pos) {
            splitted = new String[] { "defaultcontext", loginInfo };
        } else {
            splitted = new String[] { loginInfo.substring(pos + 1), loginInfo.substring(0, pos) };
        }
        return splitted;
    }
}
