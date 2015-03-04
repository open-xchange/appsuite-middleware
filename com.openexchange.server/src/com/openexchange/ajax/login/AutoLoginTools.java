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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.login.LoginTools.updateIPAddress;
import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.GuestAuthenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link AutoLoginTools}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AutoLoginTools {

    /**
     * Re-authenticates an auto-login result using the supplied credentials. This includes checking if the user/context information
     * in the session auto-login result's session matches the user/context identified by the given credentials.
     *
     * @param autoLoginResult The auto login result, or <code>null</code> if there is none
     * @param login The login name
     * @param password The password
     * @param properties The login properties
     * @return The login result, if authentication was performed successfully, or <code>null</code>, otherwise
     * @throws OXException
     */
    public static LoginResult reAuthenticate(LoginResult autoLoginResult, String login, String password, Map<String, Object> properties) throws OXException {
        if (null != autoLoginResult) {
            Authenticated authenticated = Authentication.login(login, password, properties);
            Context context;
            User user;
            if (GuestAuthenticated.class.isInstance(authenticated)) {
                /*
                 * use already resolved user / context
                 */
                GuestAuthenticated guestAuthenticated = (GuestAuthenticated) authenticated;
                context = getContext(guestAuthenticated.getContextID());
                user = getUser(context, guestAuthenticated.getUserID());
            } else {
                /*
                 * perform user / context lookup
                 */
                context = LoginPerformer.findContext(authenticated.getContextInfo());
                user = LoginPerformer.findUser(context, authenticated.getUserInfo());
            }
            if (context.getContextId() == autoLoginResult.getContext().getContextId() && context.getContextId() == autoLoginResult.getSession().getContextId() &&
                user.getId() == autoLoginResult.getUser().getId() && user.getId() == autoLoginResult.getSession().getUserId()) {
                return autoLoginResult;
            }
        }
        return null;
    }

    /**
     * Tries to lookup an existing session by the cookies supplied with the request.
     *
     * @param loginConfig A reference to the login configuration
     * @param request The request to try and perform the auto-login for
     * @param response The corresponding response
     * @return The login result if a valid session was found, or <code>null</code>, otherwise
     */
    public static LoginResult tryAutologin(LoginConfiguration loginConfig, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return tryAutologin(loginConfig, request, response, HashCalculator.getInstance().getHash(request));
    }

    /**
     * Tries to lookup an existing session by the cookies supplied with the request.
     *
     * @param loginConfig A reference to the login configuration
     * @param request The request to try and perform the auto-login for
     * @param response The corresponding response
     * @param hash The client-specific hash for the cookie names
     * @return The login result if a valid session was found, or <code>null</code>, otherwise
     */
    public static LoginResult tryAutologin(LoginConfiguration loginConfig, HttpServletRequest request, HttpServletResponse response, String hash) throws OXException {
        Cookie[] cookies = request.getCookies();
        if (loginConfig.isSessiondAutoLogin() && null != cookies && 0 < cookies.length) {
            /*
             * extract session & secret from supplied cookies
             */
            String sessionID = null;
            String secret = null;
            String sessionCookieName = LoginServlet.SESSION_PREFIX + hash;
            String secretCookieName = LoginServlet.SECRET_PREFIX + hash;
            try {
                for (int i = 0; i < cookies.length; i++) {
                    String name = cookies[i].getName();
                    if (name.startsWith(sessionCookieName)) {
                        sessionID = cookies[i].getValue();
                        /*
                         * try to auto-login once matching session- and secret cookies found
                         */
                        if (null != secret) {
                            return tryAutoLogin(loginConfig, request, sessionID, secret);
                        }
                    } else if (name.startsWith(secretCookieName)) {
                        secret = cookies[i].getValue();
                        /*
                         * try to auto-login once matching session- and secret cookies found
                         */
                        if (null != sessionID) {
                            return tryAutoLogin(loginConfig, request, sessionID, secret);
                        }
                    }
                }
            } catch (OXException e) {
                if (SessionExceptionCodes.WRONG_CLIENT_IP.equals(e)) {
                    /*
                     * session found, but IP changed -> discard session & cancel auto-login,
                     * invalidate session-cookie (public- and secret-cookies are re-written later)
                     */
                    SessionUtility.removeOXCookies(request, response, Collections.singletonList(sessionCookieName));
                    LoginPerformer.getInstance().doLogout(sessionID);
                    return null;
                }
                throw e;
            }
        }
        return null;
    }

    private static LoginResult tryAutoLogin(LoginConfiguration loginConfig, HttpServletRequest request, String sessionID, String secret) throws OXException {
        /*
         * lookup matching session
         */
        Session session = getSession(sessionID);
        if (null == session || false == secret.equals(session.getSecret())) {
            /*
             * not found / not matching
             */
            return null;
        }
        /*
         * check & take over remote IP
         */
        String remoteAddress = request.getRemoteAddr();
        if (loginConfig.isIpCheck()) {
            SessionUtility.checkIP(true, loginConfig.getRanges(), session, remoteAddress, loginConfig.getIpCheckWhitelist());
        }
        updateIPAddress(loginConfig, remoteAddress, session);
        /*
         * ensure user & context are enabled
         */
        Context context = ContextStorage.getInstance().getContext(session.getContextId());
        User user = UserStorage.getInstance().getUser(session.getUserId(), context);
        if (false == context.isEnabled() || false == user.isMailEnabled()) {
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        }
        /*
         * wrap valid session into login result & return
         */
        return new LoginResultImpl(session, context, user);
    }

    private static Session getSession(String sessionID) {
        SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null == sessiondService) {
            org.slf4j.LoggerFactory.getLogger(AutoLoginTools.class).error(
                "", ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName()));
            return null;
        }
        return sessiondService.getSession(sessionID);
    }

    /**
     * Gets a context by it's identifier from the context storage.
     *
     * @param contextID The context ID
     * @return The context
     */
    private static Context getContext(int contextID) throws OXException {
        final Context context = ContextStorage.getInstance().getContext(contextID);
        if (null == context) {
            throw ContextExceptionCodes.NOT_FOUND.create(I(contextID));
        }
        return context;
    }

    /**
     * Gets a user by it's identifier from the user storage.
     *
     * @param ctx The context
     * @param userID The user ID
     * @return The user
     */
    private static User getUser(Context ctx, int userID) throws OXException {
        return UserStorage.getInstance().getUser(userID, ctx);
    }

    /**
     * Initializes a new {@link AutoLoginTools}.
     */
    private AutoLoginTools() {
        super();
    }

}
