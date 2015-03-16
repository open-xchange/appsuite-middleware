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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.AJAXServlet.CONTENTTYPE_HTML;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER_ID;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link RedeemReservationLogin} - A login handler that redeems a token for a session reservation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class RedeemReservationLogin implements LoginRequestHandler {

    private final LoginConfiguration conf;

    /**
     * Initializes a new {@link RedeemReservationLogin}.
     */
    public RedeemReservationLogin(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doSsoLogin(req, resp);
        } catch (OXException e) {
            String errorPage = conf.getErrorPageTemplate().replace("ERROR_MESSAGE", e.getMessage());
            resp.setContentType(CONTENTTYPE_HTML);
            resp.getWriter().write(errorPage);
        }
    }

    private void doSsoLogin(HttpServletRequest req, HttpServletResponse resp) throws OXException, IOException {
        String token = LoginTools.parseToken(req);
        if (null == token) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        SessionReservationService service = ServerServiceRegistry.getInstance().getService(SessionReservationService.class);
        Reservation reservation = null == service ? null : service.getReservation(token);
        if (null == reservation) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Context context = ContextStorage.getInstance().getContext(reservation.getContextId());
        if (!context.isEnabled()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        User user = UserStorage.getInstance().getUser(reservation.getUserId(), context);
        if (!user.isMailEnabled()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Do the login
        LoginResult result = login(req, context, user, reservation.getState(), conf);

        // Obtain associated session
        Session session = result.getSession();

        // Add session log properties
        LogProperties.putSessionProperties(session);

        // Add headers and cookies from login result
        LoginServlet.addHeadersAndCookies(result, resp);

        // Store session
        SessionUtility.rememberSession(req, new ServerSessionAdapter(session));
        LoginServlet.writeSecretCookie(req, resp, session, session.getHash(), req.isSecure(), req.getServerName(), conf);

        // Send redirect
        resp.sendRedirect(generateRedirectURL(session, user.getPreferredLanguage(), conf.getUiWebPath(), conf.getHttpAuthAutoLogin()));
    }

    private LoginResult login(HttpServletRequest httpRequest, final Context context, final User user, Map<String, String> optState, LoginConfiguration loginConfiguration) throws OXException {
        // The properties derived from optional state
        Map<String, Object> props = optState == null ? new HashMap<String, Object>(4) : new HashMap<String, Object>(optState);

        // The login request
        String login = user.getLoginInfo() + '@' + context.getLoginInfo()[0];
        String defaultClient = loginConfiguration.getDefaultClient();
        LoginRequestImpl loginRequest = LoginTools.parseLogin(httpRequest, login, null, false, defaultClient, conf.isCookieForceHTTPS(), false);

        // Do the login
        return LoginPerformer.getInstance().doLogin(loginRequest, props);
    }

    private static String generateRedirectURL(Session session, String language, String uiWebPath, String shouldStore) {
        String retval = uiWebPath;

        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_SESSION, session.getSessionID());

        // App Suite UI requires some additional values.
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER, session.getLogin());
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER_ID, Integer.toString(session.getUserId()));
        retval = LoginTools.addFragmentParameter(retval, "language", language);
        if (shouldStore != null) {
            retval = LoginTools.addFragmentParameter(retval, "store", shouldStore);
        }
        return retval;
    }

}
