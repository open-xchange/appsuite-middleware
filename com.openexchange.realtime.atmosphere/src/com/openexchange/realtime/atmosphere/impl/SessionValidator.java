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

import static com.openexchange.ajax.SessionServlet.checkSecret;
import static com.openexchange.ajax.SessionServlet.isIpCheckError;
import static com.openexchange.ajax.SessionServlet.removeJSESSIONID;
import static com.openexchange.ajax.SessionServlet.removeOXCookies;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.log.Log;
import com.openexchange.log.LogProperties;
import com.openexchange.realtime.atmosphere.AtmosphereConfig;
import com.openexchange.realtime.atmosphere.AtmosphereExceptionCode;
import com.openexchange.realtime.atmosphere.osgi.AtmosphereServiceRegistry;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.exception.RealtimeExceptionFactory;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.sessiond.impl.SubnetMask;
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

    private final AtmosphereConfig atmosphereConfig;

    private final AtmosphereServiceRegistry atmosphereServiceRegistry;

    private final AtmosphereRequest request;

    private final AtmosphereResponse response;

    private final String postData;

    private boolean isAlreadyValidated = false;

    private String sessionId;

    private ServerSession serverSession;

    /**
     * Initializes a new {@link SessionValidator} from a GET Request.
     *
     * @param request The incoming {@link AtmosphereRequest}
     */
    public SessionValidator(AtmosphereResource resource) {
        this(resource, null);
    }

    /**
     * Initializes a new {@link SessionValidator} from a POST Request and the associated post data.
     *
     * @param request The incoming {@link AtmosphereRequest}
     * @param postData the post data of the request, can be null
     */
    public SessionValidator(AtmosphereResource resource, String postData) {
        this.request = resource.getRequest();
        this.response = resource.getResponse();
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
    public String getSessionId() throws RealtimeException {
        if (this.sessionId == null) {
            try {
                this.sessionId = getSessionId(getSessionFromParameters(request));
            } catch (OXException oxe) {
                RealtimeException realtimeException = RealtimeExceptionFactory.getInstance().create(RealtimeExceptionCodes.SESSION_INVALID);
                LOG.error("Couldn't find session id.", realtimeException);
                throw realtimeException;
            }
        }
        return sessionId;
    }

    /**
     * Returns the validated {@link ServerSession} that is associated with the client that sent the request.
     *
     * @return the validated {@link ServerSession} that is associated with the client that sent the request
     * @throws OXException if retrieval or validation of the serverSession fails
     */
    public ServerSession getServerSession() throws RealtimeException {
        if (!isAlreadyValidated) {
            try {
                this.serverSession = getValidatedServerSession();
            } catch (OXException e) {
                RealtimeException realtimeException = RealtimeExceptionFactory.getInstance().create(RealtimeExceptionCodes.SESSION_INVALID);
                LOG.error("Couldn't get ServerSession.", realtimeException);
                throw realtimeException;
            }
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
        validateServerSession(request, sessionId, serverSession);
        this.isAlreadyValidated = true;
        return serverSession;
    }

    /**
     * Checks the session:
     * <ol>
     * <li>does the provided sessionId match the on in the Session stored in the Sessiond?
     * <li>is the context of the user who sent the request unlocked
     * <li>is the current ip the same that created the session initially
     * <li>does the request provide the open-xchange-secret and does the provided secret matche the one stored in Sessiond for the Session
     * object looked up via the session identifier
     *
     * @param request The incoming HttpServletRequest
     * @param sessionId The sessionId sent with the incoming request
     * @param serverSession The ServerSession associated with the sessionId
     * @throws OXException if the validation of the session fails
     */
    private void validateServerSession(HttpServletRequest request, String sessionId, ServerSession serverSession) throws OXException {
        try {
            matchIDs(sessionId, serverSession);
            checkContext(serverSession);
            checkIP(
                atmosphereConfig.isIPCheckEnabled(),
                atmosphereConfig.getIpRangeWhitelist(),
                serverSession,
                request.getRemoteAddr(),
                atmosphereConfig.getClientWhitelist());
            checkSecret(atmosphereConfig.getCookieHashSource(), request, serverSession);

            // set LogProperties after validation
            LogProperties.putSessionProperties(serverSession);
        } catch (OXException oxe) {
            /*
             * If we got a SessionException during validation properly handle the server side consequences
             */
            if (SessionExceptionCodes.getErrorPrefix().equals(oxe.getPrefix())) {
                LOG.debug(oxe.getMessage(), oxe);
                handleSessiondException(oxe, request, response, serverSession);
            }
            /* and rethrow the exception so the ChannelHandler can properly hand the Exception to the client*/
            throw oxe;
        }
    }

    /**
     * Handle specified SessionD exception by removing the Session related cookies and removing the Session from the Sessiond.
     *
     * @param e The SessionD exception
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    protected void handleSessiondException(final OXException e, final HttpServletRequest req, final HttpServletResponse resp, ServerSession session) {
        if (isIpCheckError(e)) {
            try {
                removeOXCookies(session.getHash(), req, resp);
                removeJSESSIONID(req, resp);
            } catch (final Exception ex) {
                LOG.error("Cookies could not be removed", ex);
            }
            SessiondService sessiondService = atmosphereServiceRegistry.getService(SessiondService.class);
            if (sessiondService == null) {
                LOG.error(
                    "Session could not be removed",
                    RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(SessiondService.class.getName()));
            }
            sessiondService.removeSession(sessionId);
        }
    }

    /**
     * Checks if the client IP address of the current request matches the one through that the session has been created.
     *
     * @param doCheck <code>true</code> to deny request with an exception.
     * @param ranges The white-list ranges
     * @param session session object
     * @param actual IP address of the current request.
     * @param whitelist The optional IP check whitelist (by client identifier)
     * @throws OXException if the IP addresses don't match.
     */
    private void checkIP(final boolean doCheck, final List<IPRange> ranges, final Session session, final String actual, final ClientWhitelist whitelist) throws OXException {
        if (null == actual || !actual.equals(session.getLocalIp())) {
            // IP is missing or changed
            SubnetMask allowedSubnet = atmosphereConfig.getAllowedSubnet();
            if (doCheck && !isWhitelistedFromIPCheck(actual, ranges) && !isWhitelistedClient(session, whitelist) && !allowedSubnet.areInSameSubnet(
                actual,
                session.getLocalIp())) {
                // kick client with changed IP address
                if (LOG.isInfoEnabled()) {
                    final StringBuilder sb = new StringBuilder(96);
                    sb.append("Request to server denied (IP check activated) for session: ");
                    sb.append(session.getSessionID());
                    sb.append(". Client login IP changed from ");
                    sb.append(session.getLocalIp());
                    sb.append(" to ");
                    sb.append((null == actual ? "<missing>" : actual));
                    sb.append(" and is not covered by IP white-list or netmask.");
                    LOG.info(sb.toString());
                }
                throw SessionExceptionCodes.WRONG_CLIENT_IP.create();
            }
            if (null != actual && (!doCheck || isWhitelistedClient(session, whitelist))) {
                // change IP in session so the IMAP NOOP command contains the correct client IP address (Bug #21842)
                session.setLocalIp(actual);
            }
            if (LOG.isDebugEnabled() && !isWhitelistedFromIPCheck(actual, ranges) && !isWhitelistedClient(session, whitelist)) {
                final StringBuilder sb = new StringBuilder(64);
                sb.append("Session ");
                sb.append(session.getSessionID());
                sb.append(" requests now from ");
                sb.append(actual);
                sb.append(" but login came from ");
                sb.append(session.getLocalIp());
                LOG.debug(sb.toString());
            }
        }
    }

    /**
     * White listed clients are necessary for the Mobile Web Interface. This clients often change their IP address in mobile data networks.
     *
     * @param session The looked up Session
     * @param clientWhitelist The ClientWhiteList allowing to whitelist clients based on Strings with optional regular expressions
     * @return true if the client contained in the Session is part if the clientWhitelist, else false
     */
    private boolean isWhitelistedClient(final Session session, final ClientWhitelist clientWhitelist) {
        return null != clientWhitelist && !clientWhitelist.isEmpty() && clientWhitelist.isAllowed(session.getClient());
    }

    /**
     * Clients can be whitelisted based on IP ranges. This way you can whitelist whole subnets.
     *
     * @param actual The actual remote IP of the client
     * @param ranges The whitelisted IP ranges
     * @return true if the remote IP of the client is contained in the whitelisted IP ranges, else false
     */
    private boolean isWhitelistedFromIPCheck(final String actual, final List<IPRange> ranges) {
        for (final IPRange range : ranges) {
            if (range.contains(actual)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the sent sessionid matches the one saved in the ServerSession.
     *
     * @param sessionId The sent sessionid
     * @param session the SeverSession
     * @throws OXException If the sessionids differ
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
     *
     * @param session The ServerSession containing the context
     * @throws OXException If the Context is locked
     */
    private void checkContext(ServerSession session) throws OXException {
        final Context ctx = session.getContext();
        if (!ctx.isEnabled()) {
            SessiondService sessiondService = atmosphereServiceRegistry.getService(SessiondService.class);
            if (sessiondService != null) {
                sessiondService.removeSession(sessionId);
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        "Couldn't remove Session " + sessionId + " from SessionD",
                        RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(SessiondService.class.getName()));
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("The context " + ctx.getContextId() + " associated with session is locked.");
            }
            throw SessionExceptionCodes.CONTEXT_LOCKED.create();
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
    private static String extractSecret(final CookieHashSource cookieHash, final HttpServletRequest req, final String hash, final String client) {
        final Cookie[] cookies = req.getCookies();
        if (null != cookies) {
            final String cookieName = Login.SECRET_PREFIX + getHash(cookieHash, req, hash, client);
            for (final Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Didn't find an appropriate Cookie for name \"" + cookieName + "\" (CookieHashSource=" + cookieHash.toString() + ") which provides the session secret.");
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
    private static String getHash(final CookieHashSource cookieHash, final HttpServletRequest req, final String hash, final String client) {
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
