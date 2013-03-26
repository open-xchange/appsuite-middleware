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

package com.openexchange.realtime.atmosphere.impl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.atmosphere.cpr.AtmosphereRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.log.Log;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.atmosphere.AtmosphereConfig;
import com.openexchange.realtime.atmosphere.AtmosphereExceptionCode;
import com.openexchange.realtime.atmosphere.osgi.AtmosphereServiceRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link SessionValidator} - Validate session information of incoming realtime requests like done in the
 * com.openexchange.ajax.SessionServlet
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class SessionValidator {
    
    private static final org.apache.commons.logging.Log LOG = Log.loggerFor(SessionValidator.class);
    
    private AtmosphereConfig atmosphereConfig;
    
    private AtmosphereServiceRegistry atmosphereServiceRegistry;

    private final AtmosphereRequest request;

    private final String postData;

    private boolean isAlreadyValidated = false;

    private String sessionId;

    private ServerSession serverSession;

    /**
     * Initializes a new {@link SessionValidator} from a GET Request.
     * 
     * @param request The incoming {@link AtmosphereRequest}
     */
    public SessionValidator(AtmosphereRequest request) {
        this(request, null);
    }

    /**
     * Initializes a new {@link SessionValidator} from a POST Request and the associated post data.
     * 
     * @param request The incoming {@link AtmosphereRequest}
     * @param postData the post data of the request, can be null
     */
    public SessionValidator(AtmosphereRequest request, String postData) {
        this.request = request;
        this.postData = postData;
        this.atmosphereServiceRegistry = AtmosphereServiceRegistry.getInstance();
        this.atmosphereConfig = AtmosphereConfig.getInstance();
    }

    /**
     * Returns the sessionId the client sent to the server.
     * 
     * @return the sessionId the client sent to the server
     * @throws OXException if retrieval of the sessionId fails
     */
    public String getSessionId() throws OXException {
        if (this.sessionId == null) {
            this.sessionId = getSessionId(getSessionFromParameters(request), getSessionFromPostData(postData));
        }
        return sessionId;
    }

    /**
     * Returns the validated {@link ServerSession} that is associated with the client that sent the request.
     * 
     * @return the validated {@link ServerSession} that is associated with the client that sent the request
     * @throws OXException if retrieval or validation of the serverSession fails
     */
    public ServerSession getServerSession() throws OXException {
        if (!isAlreadyValidated) {
            this.serverSession = getValidatedServerSession();
        }
        return this.serverSession;
    }

    /**
     * Gets and validates the ServerSession associated with this Request.
     * 
     * @return the validated ServerSession associated with this Request.
     * @throws OXException if the ServerSession can't be retrieved or fails to validate
     */
    private ServerSession getValidatedServerSession() throws OXException {
        String sessionId = getSessionId();
        ServerSession serverSession = getServerSessionFromSessionId(sessionId);
        validateServerSession(request, serverSession);
        this.isAlreadyValidated = true;
        return serverSession;
    }

    /**
     * Checks the session:
     * <ol>
     * <li>request provides the open-xchange-secret
     * <li>open-xchange-secret matches the one stored in sessiond for the Session object looked up via the session identifier
     * <li>requests ip
     * <li>and secret cookie against information stored in the sessiond
     * 
     * @return false if the session was invalid, true if the request contains session,
     * @throws OXException
     */
    private void validateServerSession(HttpServletRequest request, ServerSession serverSession) throws OXException {
//        session id <-> serverssionid
//        ipcheck
//        context enabled
        checkSecret(atmosphereConfig.getCookieHashSource(), request, serverSession);
        // validateIP
    }
    
    
    /**
     * Check if 
     * @param sessionId
     * @param session
     * @throws OXException
     */
    private void matchIDs(String sessionId, ServerSession session) throws OXException {
        if (!sessionId.equals(session.getSessionID())) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Request's session identifier \"" + sessionId + "\" differs from the one indicated by SessionD service \"" + session.getSessionID() + "\".");
            }
            throw SessionExceptionCodes.WRONG_SESSION.create();
        }

    }
    
    /**
     * Check if the context associated with the ServerSession is locked.
     * @param session The ServerSession containing the context
     * @throws OXException If the Context is locked
     */
    private void checkContext(ServerSession session) throws OXException {
        final Context ctx = session.getContext();
        if (!ctx.isEnabled()) {
            SessiondService sessiondService = atmosphereServiceRegistry.getService(SessiondService.class);
            if(sessiondService != null) {
                sessiondService.removeSession(sessionId);
            } else {
                if(LOG.isWarnEnabled())
                LOG.warn("Couldn't remove Session " + sessionId + " from SessionD", RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(SessiondService.class.getName()));
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("The context " + ctx.getContextId() + " associated with session is locked.");
            }
            throw SessionExceptionCodes.CONTEXT_LOCKED.create();
        }
    }
    
    private void checkIP() throws OXException {
        
    }
    
    
    
    public static void checkSecret(final CookieHashSource source, final HttpServletRequest req, final Session session) throws OXException {
        final String secret = extractSecret(source, req, session.getHash(), session.getClient());
        if (secret == null || !session.getSecret().equals(secret)) {
            if (LOG.isInfoEnabled() && null != secret) {
                LOG.info("Session secret is different. Given secret \"" + secret + "\" differs from secret in session \"" + session.getSecret() + "\".");
            }
            throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
        }
    }
    
    /**
     * Extracts the secret string from specified cookies using given hash string.
     *
     * @param req the HTTP servlet request object.
     * @param hash remembered hash from session.
     * @param client the remembered client from the session.
     * @return The secret string or <code>null</code>
     */
    public static String extractSecret(final CookieHashSource cookieHash, final HttpServletRequest req, final String hash, final String client) {
        final Cookie[] cookies = req.getCookies();
        if (null != cookies) {
            final String cookieName = Login.SECRET_PREFIX + getHash(cookieHash, req, hash, client);
            for (final Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Didn't found an appropriate Cookie for name \"" + cookieName + "\" (CookieHashSource=" + cookieHash.toString() + ") which provides the session secret.");
            }
        } else if (LOG.isInfoEnabled()) {
            LOG.info("Missing Cookies in HTTP request. No session secret can be looked up.");
        }
        return null;
    }
    
    /**
     * Gets the appropriate hash for specified request.
     *
     * @param cookieHash defines how the cookie should be found.
     * @param req The HTTP request
     * @param hash The previously remembered hash
     * @param client The client identifier
     * @return The appropriate hash
     */
    public static String getHash(final CookieHashSource cookieHash, final HttpServletRequest req, final String hash, final String client) {
        if (CookieHashSource.REMEMBER == cookieHash) {
            return hash;
        }
        // Default is calculate
        return HashCalculator.getInstance().getHash(req, client);
    }

    /**
     * Inspect the request headers for session information and return it.
     * 
     * @param request the request to inspect
     * @return null if no session info can be found, the session info otherwise
     */
    private String getSessionFromHeader(AtmosphereRequest request) {
        return request.getHeader("session");
    }

    /**
     * Inspect the request parameters for session information and return it.
     * 
     * @param request the request to inspect
     * @return null if no session info can be found, the session info otherwise
     */
    private String getSessionFromParameters(AtmosphereRequest request) {
        return request.getParameter("session");
    }

    /**
     * Inspect the request's post data for session information and return it.
     * 
     * @param postData the JSON String to inspect
     * @return null if no session info can be found, the session info otherwise
     * @throws OXException if the postData isn't valid JSON
     */
    private String getSessionFromPostData(String postData) throws OXException {
        String sessionId = null;
        if (postData != null) {
            JSONObject requestData;
            try {
                requestData = new JSONObject(postData);
                sessionId = requestData.optString("session");
            } catch (JSONException e) {
                throw AtmosphereExceptionCode.POST_DATA_MALFORMED.create();
            }
        }
        return sessionId;
    }

    /**
     * Get a sessionId from a list of possible sessionId sources contained in the request. Retrieves the first non-null, non-empty sessionId
     * that can befound.
     * 
     * @param sessionIds a list of session infos, nulls are allowed
     * @return The first sessionId that is not null and not empty.
     * @throws OXException if no sessionId can be found
     */
    private String getSessionId(String... sessionIds) throws OXException {
        for (int i = 0; i < sessionIds.length; i++) {
            String currentSessionInfo = sessionIds[i];
            if (currentSessionInfo != null && !currentSessionInfo.isEmpty()) {
                return currentSessionInfo;
            }
        }
        throw SessionExceptionCodes.SESSION_PARAMETER_MISSING.create();
    }

    /**
     * Get a ServerSession object from a list of session infos submitted in the request. Fails on the first non-null sessionInfo parameter
     * that is invalid.
     * 
     * @param sessionInfo a list of session infos, nulls are allowed
     * @return The Serversession that matches the first given session info
     * @throws OXException if no matching ServerSession can be found
     */
    private ServerSession getServerSession(String... sessionInfo) throws OXException {
        ServerSession serverSession = null;
        for (int i = 0; serverSession == null && i < sessionInfo.length; i++) {
            String currentSessionInfo = sessionInfo[i];
            if (currentSessionInfo != null && !currentSessionInfo.isEmpty()) {
                serverSession = getServerSessionFromSessionId(sessionInfo[i]);
            }
        }
        return serverSession;
    }

    /**
     * Convert the session info into a ServerSession Object.
     * 
     * @param sessionInfo The sessionInfo to convert
     * @return The ServerSession objectmatching the session infos
     * @throws IllegalArgumentException if the sessionInfo is null or empty
     * @throws OXException if an error happens while trying to build the ServerSession from the session infos
     */
    private ServerSession getServerSessionFromSessionId(String sessionInfo) throws OXException {
        if (sessionInfo == null || sessionInfo.isEmpty()) {
            throw new IllegalArgumentException("Invalid parameter: sessionInfo");
        }

        SessiondService sessiondService = atmosphereServiceRegistry.getService(SessiondService.class);
        if (sessiondService == null) {
            throw OXExceptionFactory.getInstance().create(ServiceExceptionCode.SERVICE_UNAVAILABLE, SessiondService.class);
        }
        Session session = sessiondService.getSession(sessionInfo);
        if (session == null) {
            throw OXExceptionFactory.getInstance().create(SessionExceptionCodes.SESSION_EXPIRED, sessionInfo);
        }
        return ServerSessionAdapter.valueOf(session);
    }
}
