/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.servlet.utils;

import static com.openexchange.ajax.LoginServlet.SECRET_PREFIX;
import static com.openexchange.ajax.LoginServlet.configureCookie;
import static com.openexchange.ajax.LoginServlet.getPublicSessionCookieName;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.AutoLoginTools;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.ajax.login.ShareLoginConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.servlet.ShareServletStrings;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link ShareServletUtils} - Utilities for Share Servlet layer.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public final class ShareServletUtils {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareServletUtils.class);
    private static final Pattern PATH_PATTERN = Pattern.compile("/+([a-f0-9]{32})(?:/+items(?:/+([0-9]+))?)?/*", Pattern.CASE_INSENSITIVE);

    /**
     * Initializes a new {@link ShareServletUtils}.
     */
    private ShareServletUtils() {
        super();
    }

    /**
     * Gets the login configuration for shares
     *
     * @return The login configuration for shares
     */
    public static ShareLoginConfiguration getShareLoginConfiguration() {
        ShareLoginConfiguration config = LoginServlet.getShareLoginConfiguration();
        if (config == null) {
            throw new IllegalStateException("ShareServletUtils have not been initialized yet!");
        }
        return config;
    }

    public static boolean createSessionAndRedirect(GuestInfo guest, ShareTarget target, HttpServletRequest request, HttpServletResponse response, LoginMethodClosure loginMethod) throws OXException {
        Session session = null;
        try {
            /*
             * get, authenticate and login as associated guest user
             */
            ShareLoginConfiguration shareLoginConfig = getShareLoginConfiguration();
            LoginConfiguration loginConfig = shareLoginConfig.getLoginConfig(guest);
            LoginResult loginResult = ShareServletUtils.login(guest, request, response, loginConfig, shareLoginConfig.isTransientShareSessions(), loginMethod);
            if (null == loginResult) {
                return false;
            }
            session = loginResult.getSession();
            Tools.disableCaching(response);
            /*
             * set secret & share cookies
             */
            LoginServlet.addHeadersAndCookies(loginResult, response);
            boolean staySignedIn = session.isStaySignedIn();
            response.addCookie(configureCookie(new Cookie(SECRET_PREFIX + session.getHash(), session.getSecret()), request, loginConfig, staySignedIn));
            if (staySignedIn) {
                response.addCookie(configureCookie(new Cookie(LoginServlet.getShareCookieName(request), guest.getBaseToken()), request, loginConfig, true));
            }
            /*
             * set public session cookie
             */
            String alternativeID = (String) session.getParameter(Session.PARAM_ALTERNATIVE_ID);
            if (null != alternativeID) {
                String publicCookieName = getPublicSessionCookieName(request, new String[] { String.valueOf(session.getContextId()), String.valueOf(session.getUserId()) });
                Cookie cookie = Cookies.cookieMapFor(request).get(publicCookieName);
                if (null == cookie || false == alternativeID.equals(cookie.getValue())) {
                    response.addCookie(configureCookie(new Cookie(publicCookieName, alternativeID), request, loginConfig, staySignedIn));
                }
            }
            /*
             * construct & send redirect
             */
            String url = ShareRedirectUtils.getWebSessionRedirectURL(session, loginResult.getUser(), target, loginConfig);
            LOG.debug("Redirecting share {} to {}...", guest.getBaseToken(), url);
            response.sendRedirect(url);
            return true;
        } catch (IOException e) {
            throw ShareExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Authenticates the request to the share and performs a guest login.
     *
     * @param guest The guest info
     * @param request The request
     * @param response The response
     * @param loginConfig The login configuration to use
     * @param tranzient <code>true</code> to mark the session as transient, <code>false</code>, otherwise
     * @param loginMethod The login method to use
     * @return The login result, or <code>null</code> if not successful
     */
    public static LoginResult login(GuestInfo guest, HttpServletRequest request, HttpServletResponse response, LoginConfiguration loginConfig, boolean tranzient, LoginMethodClosure loginMethod) throws OXException {
        /*
         * try guest auto-login at this stage if enabled
         */
        LoginResult loginResult = AutoLoginTools.tryGuestAutologin(guest, loginConfig, request, response);
        if (null != loginResult) {
            LOG.debug("Successful autologin for share {} with guest user {} in context {}, using session {}.",
                guest.getBaseToken(), I(guest.getGuestID()), I(guest.getContextID()), loginResult.getSession().getSessionID());
            return loginResult;
        }
        /*
         * parse login request
         */
        String[] additionalsForHash = new String[] { String.valueOf(guest.getContextID()), String.valueOf(guest.getGuestID()) };
        String client = LoginTools.parseClient(request, false, loginConfig.getDefaultClient());
        LoginRequestImpl loginRequest = LoginTools.parseLogin(request, getLogin(guest), null, false,
            client, loginConfig.isCookieForceHTTPS(), false, additionalsForHash);
        loginRequest.setTransient(tranzient);
        /*
         * perform regular guest login
         */
        Map<String, Object> properties = new HashMap<String, Object>();
        loginResult = LoginPerformer.getInstance().doLogin(loginRequest, properties, loginMethod);
        if (null == loginResult || null == loginResult.getSession()) {
            return null;
        }
        LOG.debug("Successful login for share {} with guest user {} in context {}, using session {}.",
            guest.getBaseToken(), I(guest.getGuestID()), I(guest.getContextID()), loginResult.getSession().getSessionID());
        loginResult.getSession().setParameter(Session.PARAM_GUEST, Boolean.TRUE);
        return loginResult;
    }

    /**
     * Determines the most appropriate login name for a guest user, falling back to the generic "Guest" name for anonymous guest.
     *
     * @param guestUser The guest user to get the login for
     * @return The login
     */
    private static String getLogin(GuestInfo guest) {
        if (Strings.isNotEmpty(guest.getEmailAddress())) {
            return guest.getEmailAddress();
        }
        TranslatorFactory factory = ShareServiceLookup.getService(TranslatorFactory.class);
        return null != factory ? factory.translatorFor(guest.getLocale()).translate(ShareServletStrings.GUEST) : ShareServletStrings.GUEST;
    }

    /**
     * Performs a logout for the supplied session (if not <code>null</code>).
     *
     * @param session The session
     * @throws OXException
     */
    public static void logout(Session session) throws OXException {
        if (null != session) {
            LoginPerformer.getInstance().doLogout(session.getSessionID());
        }
    }

    /**
     * Extracts the token from a HTTP request's path info.
     *
     * @param pathInfo The path info
     * @return The token, or <code>null</code> if no token could be extracted
     */
    public static String extractToken(String pathInfo) {
        if (Strings.isEmpty(pathInfo)) {
            return null;
        }
        Matcher matcher = PATH_PATTERN.matcher(pathInfo);
        return matcher.matches() ? matcher.group(1) : null;
    }

    /**
     * Splits the supplied path by the separator char <code>/</code> into their components. Empty components are removed implicitly.
     *
     * @param pathInfo The path info to split
     * @return The splitted path
     */
    public static List<String> splitPath(String pathInfo) {
        List<String> paths = Strings.splitAndTrim(pathInfo, "/");
        Iterator<String> iterator = paths.iterator();
        while (iterator.hasNext()) {
            if (Strings.isEmpty(iterator.next())) {
                iterator.remove();
            }
        }
        return paths;
    }

}
