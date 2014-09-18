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
import com.openexchange.java.Strings;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.session.Session;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.servlet.auth.ShareLoginMethod;
import com.openexchange.share.servlet.handler.ResolvedShare;
import com.openexchange.share.servlet.handler.ShareHandler;
import com.openexchange.tools.servlet.RateLimitedException;
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

    private final ShareLoginConfiguration shareLoginConfig;
    private final RankingAwareNearRegistryServiceTracker<ShareHandler> shareHandlerRegistry;

    /**
     * Initializes a new {@link ShareServlet}.
     *
     * @param shareLoginConfig The share login configuration to use
     * @param shareHandlerRegistry The handler registry
     */
    public ShareServlet(ShareLoginConfiguration shareLoginConfig, RankingAwareNearRegistryServiceTracker<ShareHandler> shareHandlerRegistry) {
        super();
        this.shareLoginConfig = shareLoginConfig;
        this.shareHandlerRegistry = shareHandlerRegistry;
    }

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
            String token = extractToken(request.getPathInfo());
            Share share = null != token ? ShareServiceLookup.getService(ShareService.class, true).resolveToken(token) : null;
            if (null == share) {
                LOG.debug("No share found at '{}'", request.getPathInfo());
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            LOG.debug("Successfully resolved share at '{}' to {}", request.getPathInfo(), share);
            /*
             * get, authenticate and login as associated guest user
             */
            LoginConfiguration loginConfig = shareLoginConfig.getLoginConfig(share);
            LoginResult loginResult = login(share, request, response, loginConfig, shareLoginConfig.isTransientShareSessions());
            if (null == loginResult) {
                return;
            }
            ResolvedShare resolvedShare = new ResolvedShareImpl(share, loginResult, loginConfig, request, response);
            /*
             * prepare response
             */
            Tools.disableCaching(response);
            LoginServlet.addHeadersAndCookies(loginResult, response);
            LoginServlet.writeSecretCookie(request, response, resolvedShare.getSession(), resolvedShare.getSession().getHash(),
                request.isSecure(), request.getServerName(), loginConfig);
            /*
             * handle
             */
            ShareHandler handler = getHandler(resolvedShare);
            handler.handle(resolvedShare);
            if (false == handler.keepSession()) {
                logout(resolvedShare.getSession());
            }
        } catch (RateLimitedException e) {
            response.sendError(429, e.getMessage());
        } catch (OXException e) {
            LOG.error("Error processing share '{}': {}", request.getPathInfo(), e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Gets the share handler feeling responsible to handle the supplied share request.
     *
     * @param share The resolved share
     * @return The handler
     */
    private ShareHandler getHandler(ResolvedShare share) throws OXException {
        for (ShareHandler handler : shareHandlerRegistry.getServiceList()) {
            if (handler.handles(share)) {
                return handler;
            }
        }
        throw ShareExceptionCodes.UNEXPECTED_ERROR.create("no share handler found");
    }

    /**
     * Authenticates the request to the share and performs a guest login, sending an appropriate HTTP response in case of unauthorized
     * access.
     *
     * @param share The share
     * @param request The request
     * @param response The response
     * @param loginConfig The login configuration to use
     * @param tranzient <code>true</code> to mark the session as transient, <code>false</code>, otherwise
     * @return The login result, or <code>null</code> if not successful
     */
    private static LoginResult login(Share share, HttpServletRequest request, HttpServletResponse response, LoginConfiguration loginConfig, boolean tranzient) throws OXException, IOException {
        /*
         * parse login request
         */
        Context context = ShareServiceLookup.getService(UserService.class, true).getContext(share.getContextID());
        User user = ShareServiceLookup.getService(UserService.class, true).getUser(share.getGuest(), context);
        LoginRequestImpl loginRequest = LoginTools.parseLogin(request, user.getMail(), user.getUserPassword(), false,
            loginConfig.getDefaultClient(), loginConfig.isCookieForceHTTPS(), false);
        loginRequest.setTransient(tranzient);
        /*
         * login
         */
        ShareLoginMethod loginMethod = new ShareLoginMethod(share, context, user);
        Map<String, Object> properties = new HashMap<String, Object>();
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, properties, loginMethod);
        if (null == loginResult || null == loginResult.getSession()) {
            loginMethod.sendUnauthorized(request, response);
            return null;
        }
        LOG.debug("Successful login for share {} with guest user {} in context {}.",
            share.getToken(), share.getGuest(), share.getContextID());
        return loginResult;
    }

    /**
     * Performs a logout for the supplied session
     *
     * @param session The session
     * @throws OXException
     */
    private static void logout(Session session) throws OXException {
        LoginPerformer.getInstance().doLogout(session.getSessionID());
    }

    /**
     * Extracts the token from a HTTP request's path info.
     *
     * @param pathInfo The path info
     * @return The token, or <code>null</code> if no token could be extracted
     * @throws OXException
     */
    private static String extractToken(String pathInfo) throws OXException {
        if (false == Strings.isEmpty(pathInfo)) {
            Matcher matcher = PATH_PATTERN.matcher(pathInfo);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

//    /**
//     * Constructs the redirect URL pointing to the share in the web interface.
//     *
//     * @param session The session
//     * @param user The user
//     * @param share The share
//     * @return The redirect URL
//     */
//    private static String getDriveRedirectURL(Session session, User user, Share share) {
//        StringBuilder stringBuilder = new StringBuilder()
//            .append("/ajax/drive?action=syncfolders")
//            .append("&root=").append(share.getFolder())
//            .append("&session=").append(session.getSessionID())
//        ;
//        return stringBuilder.toString();
//    }

}
