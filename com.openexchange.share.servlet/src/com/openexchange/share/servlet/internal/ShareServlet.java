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

package com.openexchange.share.servlet.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.share.Share;
import com.openexchange.share.ShareService;
import com.openexchange.share.servlet.auth.ShareLoginMethod;
import com.openexchange.tools.servlet.RateLimitedException;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.webdav.OXServlet;
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
        try {
            /*
             * create a new HttpSession if it's missing
             */
            request.getSession(true);
            /*
             * extract share from path info
             */
            Share share = resolveShare(request.getPathInfo());
            if (null == share) {
                LOG.debug("No share found at '{}'", request.getPathInfo());
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            LOG.debug("Successfully resolved share at '{}' to {}", request.getPathInfo(), share);
            /*
             * check if there's already a valid guest session
             */
            User guestUser;
            Session session = OXServlet.findSessionByCookie(request, response);
            if (null != session && session.getUserId() == share.getGuest() && session.getContextId() == share.getContextID()) {
                LOG.debug("Existing session found via supplied cookies for share {} with guest user {} in context {}.",
                    share.getToken(), share.getGuest(), share.getContextID());
                guestUser = ServerSessionAdapter.valueOf(session).getUser();
            } else {
                /*
                 * get, authenticate and login as associated guest user
                 */
                LoginResult loginResult = login(share, request, response);
                if (null == loginResult) {
                    return;
                }
                session = loginResult.getSession();
                guestUser = loginResult.getUser();
            }
            /*
             * prepare response
             */
            Tools.disableCaching(response);
            LoginServlet.writeSecretCookie(request, response, session, session.getHash(), request.isSecure(), request.getServerName(),
                LoginServlet.getLoginConfiguration());
            response.addCookie(new Cookie("sessionid", session.getSessionID()));
            response.addCookie(new Cookie("JSESSIONID", request.getSession().getId()));

            /*
             * construct redirect URL
             */
            String url = getRedirectURL(session, guestUser, share);
            LOG.info("Redirecting share {} to {}...", share.getToken(), url);
            response.sendRedirect(url);
        } catch (RateLimitedException e) {
            response.sendError(429, e.getMessage());
        } catch (OXException e) {
            LOG.error("Error processing share '{}': {}", request.getPathInfo(), e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Authenticates the request to the share and performs a guest login, sending an appropriate HTTP response in case of unauthorized
     * access.
     *
     * @param share The share
     * @param request The request
     * @param response The response
     * @return The login result, or <code>null</code> if not successful
     */
    private static LoginResult login(Share share, HttpServletRequest request, HttpServletResponse response) throws OXException, IOException {
        /*
         * parse login request
         */
        Context context = ShareServiceLookup.getService(UserService.class, true).getContext(share.getContextID());
        User user = ShareServiceLookup.getService(UserService.class, true).getUser(share.getGuest(), context);
        LoginConfiguration loginConfig = LoginServlet.getLoginConfiguration();
        LoginRequestImpl loginRequest = LoginTools.parseLogin(request, user.getMail(), user.getUserPassword(), false,
            loginConfig.getDefaultClient(), loginConfig.isCookieForceHTTPS(), false);
        loginRequest.setTransient(true);
        /*
         * login
         */
        ShareLoginMethod loginMethod = new ShareLoginMethod(share, context, user);
        Map<String, Object> properties = new HashMap<String, Object>();
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, properties, loginMethod);
        if (null == loginResult || null == loginResult.getSession()) {
            LOG.debug("Unsuccessful login for share {} with guest user {} in context {}.",
                share.getToken(), share.getGuest(), share.getContextID());
            loginMethod.sendUnauthorized(request, response);
            return null;
        }
        LOG.debug("Successful login for share {} with guest user {} in context {}.",
            share.getToken(), share.getGuest(), share.getContextID());
        return loginResult;
    }

    // http://192.168.32.191/ajax/share/19496DEDE78141A6AB77B316ADDA3660 kontakte steffen
    // http://192.168.32.191/ajax/share/19496DED2C6542B5A1D6EF4AEEEA4D24 infostore tobias

    /**
     * Extracts the token from a HTTP request's path info and looks up the referenced share.
     *
     * @param pathInfo The path info
     * @return The share, or <code>null</code> if no share was found or the share is expired
     * @throws OXException
     */
    private static Share resolveShare(String pathInfo) throws OXException {
        if (false == Strings.isEmpty(pathInfo)) {
            /*
             * extract & resolve token
             */
            Matcher matcher = PATH_PATTERN.matcher(pathInfo);
            if (matcher.matches()) {
                return ShareServiceLookup.getService(ShareService.class, true).resolveToken(matcher.group(1));
            }
        }
        return null;
    }

    /**
     * Constructs the redirect URL pointing to the share in the web interface.
     *
     * @param session The session
     * @param user The user
     * @param share The share
     * @return The redirect URL
     */
    private static String getDriveRedirectURL(Session session, User user, Share share) {
        StringBuilder stringBuilder = new StringBuilder()
            .append("/ajax/drive?action=syncfolders")
            .append("&root=").append(share.getFolder())
            .append("&session=").append(session.getSessionID())
        ;
        return stringBuilder.toString();
    }

    /**
     * Constructs the redirect URL pointing to the share in the web interface.
     *
     * @param session The session
     * @param user The user
     * @param share The share
     * @return The redirect URL
     */
    private static String getRedirectURL(Session session, User user, Share share) {
        boolean ox6 = true;

        StringBuilder stringBuilder = new StringBuilder();
        if (ox6) {
            stringBuilder.append("/ox6/")
        //        .append(ShareServiceLookup.getService(DispatcherPrefixService.class).getPrefix())
                .append("#session=").append(session.getSessionID())
                .append("&user=").append(user.getMail())
                .append("&user_id=").append(session.getUserId())
                .append("&language=").append(user.getLocale())
                .append("&store=true")
                .append("&m=").append(getApp(Module.getForFolderConstant(share.getModule())))
                .append("&f=").append(share.getFolder())
                ;
            if (false == share.isFolder()) {
                stringBuilder.append("&id=").append(share.getItem());
            }
        } else {
            stringBuilder
                .append("/appsuite/")
    //            .append(ShareServiceLookup.getService(DispatcherPrefixService.class).getPrefix())
                .append("#session=").append(session.getSessionID())
                .append("&user=").append(session.getLogin())
                .append("&user_id=").append(session.getUserId())
                .append("&language=").append(user.getLocale())
                .append("&store=true")
                .append("&app=").append(getApp(Module.getForFolderConstant(share.getModule())))
                .append("&folder=").append(share.getFolder())
            ;
            if (false == share.isFolder()) {
                stringBuilder.append("&id=").append(share.getItem());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets the application identifier as used by the app suite client.
     *
     * @param module The module
     * @return The application identifier, or <code>null</code> if not known
     */
    private static String getApp(Module module) {
        switch (module) {
        case CALENDAR:
            return "io.ox/calendar";
        case CONTACTS:
            return "io.ox/contacts";
        case INFOSTORE:
            return "io.ox/files";
        case MAIL:
            return "io.ox/mail";
        case TASK:
            return "io.ox/tasks";
        default:
            return null;
        }
    }

}
