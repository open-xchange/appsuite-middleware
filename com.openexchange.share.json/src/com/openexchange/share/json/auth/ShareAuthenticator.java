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

package com.openexchange.share.json.auth;

import static com.openexchange.tools.servlet.http.Authorization.checkForBasicAuthorization;
import static com.openexchange.tools.servlet.http.Authorization.checkForDigestAuthorization;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fields.Header;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.Share;
import com.openexchange.share.json.internal.ShareServiceLookup;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.webdav.digest.DigestUtility;
import com.openexchange.user.UserService;

/**
 * {@link ShareAuthenticator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class ShareAuthenticator {

    private final Share share;

    /**
     * Initializes a new {@link ShareAuthenticator}.
     *
     * @param share The share
     */
    public ShareAuthenticator(Share share) {
        super();
        this.share = share;
    }

    /**
     * Authenticates the supplied HTTP request using the share's authentication mode. In case authentication fails, an appropriate HTTP
     * status and suitable authentication headers are automatically written to the supplied response.
     *
     * @param request The request
     * @param response The response
     * @return The authentication, or <code>null</code> if not authorized
     * @throws OXException
     * @throws IOException
     */
    public ShareAuthentication authenticate(HttpServletRequest request, HttpServletResponse response) throws OXException, IOException {
        switch (share.getAuthentication()) {
        case ANONYMOUS:
            return anonymous(request, response);
        case BASIC:
            return basic(request, response);
        case DIGEST:
            return digest(request, response);
        default:
            throw new UnsupportedOperationException(String.valueOf(share.getAuthentication()));
        }
    }

    private ShareAuthentication anonymous(HttpServletRequest request, HttpServletResponse response) throws OXException {
        Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(share.getContextID());
        User user = ShareServiceLookup.getService(UserService.class, true).getUser(share.getGuest(), context);
        return new ShareAuthentication(user, context);
    }

    private ShareAuthentication basic(HttpServletRequest request, HttpServletResponse response) throws OXException, IOException {
        String authHeader = request.getHeader(Header.AUTH_HEADER);
        if (false == checkForBasicAuthorization(authHeader)) {
            return unauthorized(share, request, response);
        }
        Credentials credentials = Authorization.decode(authHeader);
        if (null == credentials || false == Authorization.checkLogin(credentials.getPassword())) {
            return unauthorized(share, request, response);
        }
        //TODO: existence of guest via login2guest table?
        //      guestUserID = userService.getUserId(credentials.getLogin(), context);
        //      if (guestUserID != share.getGuest()) {
        //          sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "401 Unauthorized");
        //          return;
        //      }
        //      guestUser = userService.getUser(guestUserID, context);
        Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(share.getContextID());
        UserService userService = ShareServiceLookup.getService(UserService.class, true);
        User user = userService.getUser(share.getGuest(), context);
        if (Strings.isEmpty(credentials.getLogin()) || false == credentials.getLogin().equalsIgnoreCase(user.getMail()) ||
            Strings.isEmpty(credentials.getPassword()) || false == credentials.getPassword().equals(decrypt(user.getUserPassword()))) {
            return unauthorized(share, request, response);
        }
        return new ShareAuthentication(user, context);
    }

    private ShareAuthentication digest(HttpServletRequest request, HttpServletResponse response) throws OXException, IOException {
        String authHeader = request.getHeader(Header.AUTH_HEADER);
        if (false == checkForDigestAuthorization(authHeader)) {
            return unauthorized(share, request, response);
        }
        com.openexchange.tools.webdav.digest.Authorization parsed = DigestUtility.getInstance().parseDigestAuthorization(authHeader);
        String username = parsed.getUser();
        Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(share.getContextID());
        UserService userService = ShareServiceLookup.getService(UserService.class, true);
        User user = userService.getUser(share.getGuest(), context);
        if (Strings.isEmpty(username) || false == username.equalsIgnoreCase(user.getMail())) {
            return unauthorized(share, request, response);
        }
        String serverDigest = DigestUtility.getInstance().generateServerDigest(request, decrypt(user.getUserPassword()));
        if (null == serverDigest || false == serverDigest.equals(parsed.getResponse())) {
            return unauthorized(share, request, response);
        }
        return new ShareAuthentication(user, context);
    }

//    private static String encrypt(String value) throws OXException {
//        CryptoService cryptoService = ShareServiceLookup.getService(CryptoService.class, true);
//        String cryptKey = ShareServiceLookup.getService(ConfigurationService.class, true).getProperty(
//            "com.openexchange.share.cryptKey", "erE2e8OhAo71");
//        return cryptoService.encrypt(value, cryptKey);
//    }

    private static String decrypt(String value) throws OXException {
        CryptoService cryptoService = ShareServiceLookup.getService(CryptoService.class, true);
        String cryptKey = ShareServiceLookup.getService(ConfigurationService.class, true).getProperty(
            "com.openexchange.share.cryptKey", "erE2e8OhAo71");
        return cryptoService.decrypt(value, cryptKey);
    }

    private ShareAuthentication unauthorized(Share share, HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (share.getAuthentication()) {
        case BASIC:
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm(share) + "\", encoding=\"UTF-8\"");
            break;
        case DIGEST:
            response.setHeader("WWW-Authenticate", new StringBuilder()
                .append("Digest realm=\"").append(getRealm(share)).append("\", ")
                .append("qop=\"auth,auth-int\", ")
                .append("nonce=\"").append(DigestUtility.getInstance().generateNOnce(request)).append("\", ")
                .append("opaque=\"").append(UUIDs.getUnformattedString(UUID.randomUUID())).append("\", ")
                .append("stale=\"false\", ")
                .append("algorithm=\"MD5\"")
            .toString());
            break;
        default:
            break;
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "401 Unauthorized");
        return null;
    }

    private static String getRealm(Share share) {
        return "Share " + share.getToken();
    }

}
