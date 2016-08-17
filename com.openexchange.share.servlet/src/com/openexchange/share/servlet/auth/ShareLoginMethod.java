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

package com.openexchange.share.servlet.auth;

import static com.openexchange.tools.servlet.http.Authorization.checkForBasicAuthorization;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fields.Header;
import com.openexchange.authentication.GuestAuthenticated;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.guest.GuestService;
import com.openexchange.java.Strings;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.passwordmechs.PasswordMechFactory;
import com.openexchange.share.core.ShareConstants;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.user.UserService;

/**
 * {@link ShareLoginMethod}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareLoginMethod implements LoginMethodClosure {

    private static interface CredentialsAuthenticator {

        boolean authenticate(Credentials credentials, User user, int contextId) throws OXException;
    }

    private static final CredentialsAuthenticator ANONYMOUS_AUTHENTICATOR = new CredentialsAuthenticator() {

        @Override
        public boolean authenticate(Credentials credentials, User user, int contextId) throws OXException {
            // In case of anonymous guest user, only the password is relevant
            String password = credentials.getPassword();
            return (false == Strings.isEmpty(password) && password.equals(decrypt(user.getUserPassword())));
        }
    };

    private static final CredentialsAuthenticator GUEST_AUTHENTICATOR = new CredentialsAuthenticator() {

        @Override
        public boolean authenticate(Credentials credentials, User user, int contextId) throws OXException {
            // In case of named guest user, both the password and name are relevant
            String password = credentials.getPassword();
            String login = credentials.getLogin();
            if (Strings.isEmpty(password) || Strings.isEmpty(login)) {
                return false;
            }

            // Authenticate the user
            UserService userService = ShareServiceLookup.getService(UserService.class, true);
            if (!userService.authenticate(user, password)) {
                GuestService guestService = ShareServiceLookup.getService(GuestService.class);
                if (guestService != null) {
                    return guestService.authenticate(user, contextId, password);
                }
                return false;
            }

            // Check against E-Mail address
            if (!login.equals(user.getMail())) {
                return false;
            }

            return true;
        }
    };

    // ---------------------------------------------------------------------------------------------------------------------

    private final Context context;
    private final User user;
    private final SessionEnhancement enhancement;

    /**
     * Initializes a new {@link ShareLoginMethod}.
     *
     * @param context The context
     * @param user The user
     * @param enhancement The session enhancement, or <code>null</code> if not applicable
     */
    public ShareLoginMethod(Context context, User user, SessionEnhancement enhancement) {
        super();
        this.context = context;
        this.user = user;
        this.enhancement = enhancement;
    }

    @Override
    public GuestAuthenticated doAuthentication(LoginResultImpl loginResult) throws OXException {
        ShareAuthenticated authenticated;
        if (Strings.isEmpty(user.getMail())) {
            /*
             * anonymous
             */
            if (Strings.isEmpty(user.getUserPassword())) {
                /*
                 * ... without password
                 */
                authenticated = new ShareAuthenticated(user, context, enhancement);
            } else {
                /*
                 * ... with password
                 */
                authenticated = basic(loginResult, ANONYMOUS_AUTHENTICATOR);
            }
        } else {
            if (Strings.isEmpty(user.getUserPassword())) {
                /*
                 * ... without password
                 */
                authenticated = new ShareAuthenticated(user, context, enhancement);
            } else {
                /*
                 * ... with password
                 */
                authenticated = basic(loginResult, GUEST_AUTHENTICATOR);
            }
        }

        // Check returned ShareAuthenticated instance
        if (null == authenticated) {
            return null;
        }

        // Check associated context
        {
            Context context = authenticated.getContext();
            if (null != context && false == context.isEnabled()) {
                return null;
            }
        }

        // Check associated user
        {
            User user = authenticated.getUser();
            if (null != user && false == user.isMailEnabled()) {
                return null;
            }
        }
        return authenticated;
    }

    private ShareAuthenticated basic(LoginResultImpl loginResult, CredentialsAuthenticator authenticator) throws OXException {
        // Get "Authorization" header
        String authHeader = getAuthHeader(loginResult);

        // Check for "basic" authorization
        if (false == checkForBasicAuthorization(authHeader)) {
            return null;
        }

        // Parse credentials from given "Authorization" header
        Credentials credentials = Authorization.decode(authHeader);
        if (null == credentials || false == Authorization.checkLogin(credentials.getPassword())) {
            return null;
        }

        // Verify credentials
        if (false == authenticator.authenticate(credentials, user, context.getContextId())) {
            return null;
        }

        // Successfully authenticated
        return new ShareAuthenticated(user, context, enhancement);
    }

    /**
     * Decrypts specified string value using {@link ShareConstants.PASSWORD_MECH_ID} via PasswordMechFactory.
     *
     * @param value The value to decrypt
     * @return The decrypted value
     * @throws OXException If decryption fails
     */
    public static String decrypt(String value) throws OXException {
        return ShareServiceLookup.getService(PasswordMechFactory.class, true).get(ShareConstants.PASSWORD_MECH_ID).decode(value);
    }

    /**
     * Puts the unauthorized information/data to given HTTP response
     *
     * @param request The associated HTTP request
     * @param response The HTTP response
     * @throws IOException If an I/O error occurs
     */
    public void sendUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (false == Strings.isEmpty(user.getMail()) || false == Strings.isEmpty(user.getPasswordMech())) {
            StringBuilder builder = appendRealm(new StringBuilder(32).append("Basic realm=\""));
            builder.append("\", encoding=\"UTF-8\"");
            response.setHeader("WWW-Authenticate", builder.toString());
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "401 Unauthorized");
    }

    private StringBuilder appendRealm(StringBuilder builder) {
        builder.append("Share/").append(context.getContextId()).append('/').append(user.getId());
        return builder;
    }

    private static String getAuthHeader(LoginResultImpl loginResult) {
        Map<String, List<String>> headers = loginResult.getRequest().getHeaders();
        if (null != headers) {
            List<String> authHeaders = headers.get(Header.AUTH_HEADER);
            if (null != authHeaders && 0 < authHeaders.size()) {
                return authHeaders.get(0);
            }
        }
        return null;
    }

}
