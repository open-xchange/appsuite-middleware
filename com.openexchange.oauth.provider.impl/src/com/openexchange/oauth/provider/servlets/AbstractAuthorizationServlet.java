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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.servlets;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.DefaultLoginInfo;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.oauth.provider.internal.OAuthProviderServiceLookup;
import com.openexchange.user.UserService;


/**
 * {@link AbstractAuthorizationServlet} - The abstract authorization {@link HttpServlet Servlet}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractAuthorizationServlet extends HttpServlet {

    private static final long serialVersionUID = 6538319126816587520L;

    /**
     * Initializes a new {@link AbstractAuthorizationServlet}.
     */
    protected AbstractAuthorizationServlet() {
        super();
    }

    /**
     * Resolves specified login/password pair to user/context.
     *
     * @param login The login
     * @param password The password
     * @return A map containing resolved user/context with keys <code>"user"</code>/<code>"context"</code>.
     * @throws OXException If resolving fails
     */
    protected Map<String, Object> resolveLogin(final String login, final String password) throws OXException {
        /*
         * Resolve login
         */
        final AuthenticationService authenticationService = OAuthProviderServiceLookup.getService(AuthenticationService.class);
        final Authenticated authenticated = authenticationService.handleLoginInfo(new DefaultLoginInfo(login, password));
        final Context ctx = findContext(authenticated.getContextInfo());
        final Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("user", findUser(ctx, authenticated.getUserInfo()));
        map.put("context", ctx);
        return map;
    }

    private Context findContext(final String contextInfo) throws OXException {
        final ContextService contextService = OAuthProviderServiceLookup.getService(ContextService.class);
        final int contextId = contextService.getContextId(contextInfo);
        if (ContextStorage.NOT_FOUND == contextId) {
            throw ContextExceptionCodes.NO_MAPPING.create(contextInfo);
        }
        final Context context = contextService.getContext(contextId);
        if (null == context) {
            throw ContextExceptionCodes.NOT_FOUND.create(Integer.valueOf(contextId));
        }
        return context;
    }

    private User findUser(final Context ctx, final String userInfo) throws OXException {
        final String proxyDelimiter = MailProperties.getInstance().getAuthProxyDelimiter();
        final UserService userService = OAuthProviderServiceLookup.getService(UserService.class);
        int userId = 0;
        if (null != proxyDelimiter && userInfo.contains(proxyDelimiter)) {
            userId = userService.getUserId(userInfo.substring(userInfo.indexOf(proxyDelimiter) + proxyDelimiter.length(), userInfo.length()), ctx);
        } else {
            userId = userService.getUserId(userInfo, ctx);
        }
        return userService.getUser(userId, ctx);
    }

    /**
     * Tests for empty string.
     */
    protected static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
