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

package com.openexchange.share.json.internal;

import static com.openexchange.tools.servlet.http.Authorization.checkForBasicAuthorization;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.fields.Header;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.Share;
import com.openexchange.share.ShareAuthentication;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.tools.servlet.RateLimitedException;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.UserService;


/**
 * {@link ShareServlet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class ShareServlet extends HttpServlet {

    private static final long serialVersionUID = -598653369873570676L;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareServlet.class);
    private static final Pattern PATH_PATTERN = Pattern.compile("/+([a-f0-9]{32})(?:/+items(?:/+([0-9]+))?)?/*", Pattern.CASE_INSENSITIVE);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // http://192.168.32.191/ajax/share/19496DEDE78141A6AB77B316ADDB366E
        // http://192.168.32.191/ajax/share/19496ded2c6542b5a1d6ef4aeeea4d20

        try {
            /*
             * create a new HttpSession if it's missing
             */
            request.getSession(true);
            /*
             * extract share from path info
             */
            String pathInfo = request.getPathInfo();
            if (Strings.isEmpty(pathInfo)) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    ShareExceptionCodes.INVALID_LINK.create(pathInfo).getDisplayMessage(null));
                return;
            }
            Matcher matcher = PATH_PATTERN.matcher(pathInfo);
            if (false == matcher.matches()) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    ShareExceptionCodes.INVALID_LINK.create(pathInfo).getDisplayMessage(null));
                return;
            }
            String token = matcher.group(1);
            Share share = ShareServiceLookup.getService(ShareService.class, true).resolveToken(token);
            /*
             * get & authenticate associated guest user
             */
            Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(share.getContextID());
            UserService userService = ShareServiceLookup.getService(UserService.class, true);
            User guestUser;
            if (false == ShareAuthentication.ANONYMOUS.equals(share.getAuthentication())) {
                String authHeader = request.getHeader(Header.AUTH_HEADER);
                if (false == checkForBasicAuthorization(authHeader)) {
                    response.setHeader("WWW-Authenticate", "Basic realm=\"Please enter your e-mail address and password to access the share "
                        + share.getToken() + "\", encoding=\"UTF-8\"");
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "401 Unauthorized");
                    return;
                }
                Credentials credentials = Authorization.decode(authHeader);
                if (null == credentials || false == Authorization.checkLogin(credentials.getPassword())) {
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "401 Unauthorized");
                    return;
                }
                  //TODO: existence via login2guest table?
//                guestUserID = userService.getUserId(credentials.getLogin(), context);
//                if (guestUserID != share.getGuest()) {
//                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "401 Unauthorized");
//                    return;
//                }
//                guestUser = userService.getUser(guestUserID, context);
                guestUser = userService.getUser(share.getGuest(), context);
                if (Strings.isEmpty(credentials.getLogin()) || false == credentials.getLogin().equalsIgnoreCase(guestUser.getMail())) {
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    return;
                }
                if (false == userService.authenticate(guestUser, credentials.getPassword())) {
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    return;
                }
            } else {
                guestUser = userService.getUser(share.getGuest(), context);
            }
            /*
             * create guest session
             */
            Session session = ShareServiceLookup.getService(SessiondService.class, true).addSession(
                new ShareAddSessionParameter(request, context, guestUser));
            if (null == session) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                return;
            }
            /*
             * prepare response
             */
            Tools.disableCaching(response);
            LoginServlet.writeSecretCookie(request, response, session, session.getHash(), request.isSecure(), request.getServerName(),
                LoginServlet.getLoginConfiguration());
            response.addCookie(new Cookie("sessionid", session.getSessionID()));
            /*
             * construct redirect URL
             */
            String url = getRedirectURL(session, guestUser, share);
            response.sendRedirect(url);
        } catch (RateLimitedException e) {
            sendError(response, 429, e.getMessage());
        } catch (OXException e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static String getRedirectURL(Session session, User user, Share share) {
        StringBuilder stringBuilder = new StringBuilder()
            .append("/appsuite/")
//            .append(ShareServiceLookup.getService(DispatcherPrefixService.class).getPrefix())
            .append("#session=").append(session.getSessionID())
            .append("&user=").append(session.getLogin())
            .append("&user_id=").append(session.getUserId())
            .append("&language=").append(user.getLocale())
            .append("&store=true")
            .append("&app=").append(getApp(share.getModule()))
            .append("&folder=").append(share.getFolder())
        ;
        if (false == share.isFolder()) {
            stringBuilder.append("&id=").append(share.getItem());
        }
        return stringBuilder.toString();
    }

    private static String getApp(Module module) {
        switch (module) {
        case CALENDAR:
        case CONTACTS:
            return "io.ox/contacts";
        case INFOSTORE:
            return "io.ox/files";
        case MAIL:
        case SYSTEM:
        case TASK:
        case UNBOUND:
        default:
            return null;
        }
    }

    private static void sendError(HttpServletResponse response, int status, String statusMessage) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.sendError(status, statusMessage);
    }

}
