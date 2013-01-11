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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.ajax.fields.LoginFields.CLIENT_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_TOKEN;
import static com.openexchange.ajax.fields.LoginFields.SERVER_TOKEN;
import static com.openexchange.ajax.fields.LoginFields.VERSION_PARAM;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.Login;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;

/**
 * Implements the tokens login request taking the client and the server token to activate a previously created session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Tokens implements LoginRequestHandler {

    private LoginConfiguration conf;

    public Tokens(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doTokens(req, resp);
        } catch (OXException e) {
            Login.logAndSendException(resp, e);
        }
    }

    private void doTokens(HttpServletRequest req, HttpServletResponse resp) throws OXException, IOException {
        String clientToken = LoginTools.parseParameter(req, CLIENT_TOKEN);
        String serverToken = LoginTools.parseParameter(req, SERVER_TOKEN);
        String client = LoginTools.parseParameter(req, CLIENT_PARAM);
        String version = LoginTools.parseParameter(req, VERSION_PARAM);
        String userAgent = LoginTools.parseUserAgent(req);

        // TODO Register this login action dynamically if the SessiondService gets available.
        SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class, true);
        Session session = sessiondService.getSessionWithTokens(clientToken, serverToken);

        // update IP address with IP check
        if (!conf.isIpCheck()) {
            LoginTools.updateIPAddress(conf, newIP, session);
        }
        // TODO update client, version, userAgent
        // TODO update hash

        // TODO write cookies
        // TODO write response

//        Tools.disableCaching(resp);
//        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
//        final Response response = new Response();
//        Session session = null;
//        try {
//            String secret = null;
//            final String hash = HashCalculator.getHash(req);
//            final String sessionCookieName = SESSION_PREFIX + hash;
//            final String secretCookieName = SECRET_PREFIX + hash;
//
//            NextCookie: for (final Cookie cookie : cookies) {
//                final String cookieName = cookie.getName();
//                if (cookieName.startsWith(sessionCookieName)) {
//                    final String sessionId = cookie.getValue();
//                    session = sessiondService.getSession(sessionId);
//                    if (null != session) {
//                        // IP check if enabled; otherwise update session's IP address if different to request's IP address
//                        // Insecure check is done in updateIPAddress method.
//                        if (!conf.isIpCheck()) {
//                            // Update IP address if necessary
//                            updateIPAddress(req.getRemoteAddr(), session);
//                        } else {
//                            final String newIP = req.getRemoteAddr();
//                            SessionServlet.checkIP(true, conf.getRanges(), session, newIP, conf.getIpCheckWhitelist());
//                            // IP check passed: update IP address if necessary
//                            updateIPAddress(newIP, session);
//                        }
//                        try {
//                            final Context ctx = ContextStorage.getInstance().getContext(session.getContextId());
//                            final User user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
//                            if (!ctx.isEnabled() || !user.isMailEnabled()) {
//                                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
//                            }
//                        } catch (final UndeclaredThrowableException e) {
//                            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
//                        }
//                        final JSONObject json = new JSONObject();
//                        LoginWriter.write(session, json);
//                        // Append "config/modules"
//                        appendModules(session, json, req);
//                        response.setData(json);
//                        /*
//                         * Secret already found?
//                         */
//                        if (null != secret) {
//                            break NextCookie;
//                        }
//                    }
//                } else if (cookieName.startsWith(secretCookieName)) {
//                    secret = cookie.getValue();
//                    /*
//                     * Session already found?
//                     */
//                    if (null != session) {
//                        break NextCookie;
//                    }
//                }
//            }
//            if (null == response.getData() || session == null || secret == null || !(session.getSecret().equals(secret))) {
//                SessionServlet.removeOXCookies(hash, req, resp);
//                SessionServlet.removeJSESSIONID(req, resp);
//                if (doAutoLogin(req, resp)) {
//                    throw OXJSONExceptionCodes.INVALID_COOKIE.create();
//                }
//                return;
//            }
//        } catch (final OXException e) {
//            if (AjaxExceptionCodes.DISABLED_ACTION.equals(e)) {
//                LOG.debug(e.getMessage(), e);
//            } else {
//                e.log(LOG);
//            }
//            if (SessionServlet.isIpCheckError(e) && null != session) {
//                try {
//                    // Drop Open-Xchange cookies
//                    final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
//                    SessionServlet.removeOXCookies(session.getHash(), req, resp);
//                    SessionServlet.removeJSESSIONID(req, resp);
//                    sessiondService.removeSession(session.getSessionID());
//                } catch (final Exception e2) {
//                    LOG.error("Cookies could not be removed.", e2);
//                }
//            }
//            response.setException(e);
//        } catch (final JSONException e) {
//            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
//            LOG.error(oje.getMessage(), oje);
//            response.setException(oje);
//        }
//        // The magic spell to disable caching
//        Tools.disableCaching(resp);
//        resp.setStatus(HttpServletResponse.SC_OK);
//        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
//        try {
//            if (response.hasError()) {
//                ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
//            } else {
//                ((JSONObject) response.getData()).write(resp.getWriter());
//            }
//        } catch (final JSONException e) {
//            log(RESPONSE_ERROR, e);
//            sendError(resp);
//        }
    }

}
