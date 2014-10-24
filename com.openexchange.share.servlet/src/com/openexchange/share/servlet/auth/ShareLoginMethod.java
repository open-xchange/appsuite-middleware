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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;

/**
 * {@link ShareLoginMethod}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareLoginMethod implements LoginMethodClosure {

    private final Context context;
    private final User user;

    /**
     * Initializes a new {@link ShareLoginMethod}.
     *
     * @param context The context
     * @param user The user
     */
    public ShareLoginMethod(Context context, User user) {
        super();
        this.context = context;
        this.user = user;
    }

    @Override
    public GuestAuthenticated doAuthentication(LoginResultImpl loginResult) throws OXException {
        ShareAuthenticated authenticated;
        if (Strings.isEmpty(user.getMail())) {
            /*
             * anonymous
             */
            if (Strings.isEmpty(user.getPasswordMech())) {
                /*
                 * ... without password
                 */
                authenticated = anonymous(loginResult);
            } else {
                /*
                 * ... with password
                 */
                authenticated = basic(loginResult, true);
            }
        } else {
            /*
             * named guest user with password
             */
            authenticated = basic(loginResult, false);
        }
        if (null == authenticated ||
            null != authenticated.getContext() && false == authenticated.getContext().isEnabled() ||
            null != authenticated.getUser() && false == authenticated.getUser().isMailEnabled()) {
            return null;
        }
        return authenticated;
    }

    private ShareAuthenticated anonymous(LoginResultImpl loginResult) throws OXException {
        return new ShareAuthenticated(user, context);
    }

    private ShareAuthenticated basic(LoginResultImpl loginResult, boolean ignoreUsername) throws OXException {
        String authHeader = getAuthHeader(loginResult);
        if (false == checkForBasicAuthorization(authHeader)) {
            return null;
        }
        Credentials credentials = Authorization.decode(authHeader);
        if (null == credentials || false == Authorization.checkLogin(credentials.getPassword())) {
            return null;
        }
        if (Strings.isEmpty(credentials.getPassword()) || false == credentials.getPassword().equals(decrypt(user.getUserPassword()))) {
            return null;
        }
        if (false == ignoreUsername &&
            (Strings.isEmpty(credentials.getLogin()) || false == credentials.getLogin().equalsIgnoreCase(user.getMail()))) {
            return null;
        }
        return new ShareAuthenticated(user, context);
    }

    private static String decrypt(String value) throws OXException {
        return ShareServiceLookup.getService(ShareCryptoService.class, true).decrypt(value);
    }

    public void sendUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (false == Strings.isEmpty(user.getMail()) || false == Strings.isEmpty(user.getPasswordMech())) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm() + "\", encoding=\"UTF-8\"");
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "401 Unauthorized");
    }

    private String getRealm() {
        return "Share/" + context + '/' + user;
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
