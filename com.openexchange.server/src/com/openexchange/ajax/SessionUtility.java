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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.ajax;

import static com.openexchange.ajax.LoginServlet.SESSION_PREFIX;
import static com.openexchange.ajax.LoginServlet.getPublicSessionCookieName;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Strings.toLowerCase;
import static com.openexchange.tools.servlet.http.Cookies.extractDomainValue;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.uadetector.UserAgentFamily;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.server.services.SessionInspector;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.SessionResult;
import com.openexchange.session.SessionSecretChecker;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.sessiond.impl.SubnetMask;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.uadetector.UserAgentParser;


/**
 * {@link SessionUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public final class SessionUtility {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionUtility.class);

    // ------------------------------------------------------ Constants ---------------------------------------------------- //

    private static final String SESSION_KEY = SessionServlet.SESSION_KEY;
    private static final String SESSION_WHITELIST_FILE = SessionServlet.SESSION_WHITELIST_FILE;
    private static final String PUBLIC_SESSION_KEY = "publicSessionObject";
    private static final String PARAMETER_PUBLIC_SESSION = AJAXServlet.PARAMETER_PUBLIC_SESSION;
    private static final String PARAMETER_SESSION = AJAXServlet.PARAMETER_SESSION;
    private static final Queue<IPRange> RANGES = new ConcurrentLinkedQueue<IPRange>();

    // ------------------------------------------------------ Statics ---------------------------------------------------- //

    private static volatile boolean initialized = false;
    private static volatile boolean checkIP = true;
    private static volatile ClientWhitelist clientWhitelist;
    private static volatile CookieHashSource hashSource;
    private static volatile boolean rangesLoaded;
    private static volatile SubnetMask allowedSubnet;

    // ------------------------------------------------------ Methods ---------------------------------------------------- //

    /**
     * Initializes session utility.
     */
    public static void initialize() {
        boolean init = initialized;
        if (!init) {
            synchronized (SessionUtility.class) {
                init = initialized;
                if (!init) {
                    final ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == configService) {
                        return;
                    }

                    checkIP = Boolean.parseBoolean(configService.getProperty(ServerConfig.Property.IP_CHECK.getPropertyName()));
                    hashSource = CookieHashSource.parse(configService.getProperty(Property.COOKIE_HASH.getPropertyName()));
                    clientWhitelist = new ClientWhitelist().add(configService.getProperty(Property.IP_CHECK_WHITELIST.getPropertyName()));
                    final String ipMaskV4 = configService.getProperty(ServerConfig.Property.IP_MASK_V4.getPropertyName());
                    final String ipMaskV6 = configService.getProperty(ServerConfig.Property.IP_MASK_V6.getPropertyName());
                    allowedSubnet = new SubnetMask(ipMaskV4, ipMaskV6);
                    initRanges(configService);
                    initialized = true;
                }
            }
        }
    }

    private static void initRanges(final ConfigurationService configService) {
        if (rangesLoaded) {
            return;
        }
        if (checkIP) {
            String text = null;
            text = configService.getProperty(SESSION_WHITELIST_FILE);
            if (text == null) {
                // Fall back to configuration service
                final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (configurationService != null) {
                    text = configurationService.getText(SESSION_WHITELIST_FILE);
                } else {
                    //LOG.error("Can't load IP Check whitelist file. Please check that the servlet activator is in order");
                    return;
                }
            }
            rangesLoaded = true;
            if (text != null) {
                LOG.info("Exceptions from IP Check have been defined.");
                // Serialize range parsing. This might happen more than once, but shouldn't matter, since the list
                // is accessed exclusively, so it winds up correct.
                RANGES.clear();
                final String[] lines = Strings.splitByCRLF(text);
                final List<IPRange> ranges = new LinkedList<IPRange>();
                for (String line : lines) {
                    line = line.replaceAll("\\s", "");
                    if (!line.equals("") && (line.length() == 0 || line.trim().charAt(0) != '#')) {
                        ranges.add(IPRange.parseRange(line));
                    }
                }
                RANGES.addAll(ranges);
            }

        } else {
            rangesLoaded = true;
        }
    }

    /**
     * Gets the hash source
     *
     * @return The hash source
     */
    public static CookieHashSource getHashSource() {
        return hashSource;
    }

    /**
     * Initializes associated request's session.
     *
     * @param req The request
     * @param resp The response
     * @throws OXException If initialization fails
     */
    public static SessionResult<ServerSession> defaultInitializeSession(final HttpServletRequest req, final HttpServletResponse resp) throws OXException {
        ServerSession session = getSessionObject(req, true);
        if (null != session) {
            return new SessionResult<ServerSession>(Reply.CONTINUE, session);
        }

        // Require SessionD service
        SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (sessiondService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }

        // Look-up & remember session
        SessionResult<ServerSession> result;
        {
            String sessionId = optSessionId(req);
            if (sessionId != null && sessionId.length() > 0) {
                result = getSession(req, resp, sessionId, sessiondService);
                if (Reply.STOP == result.getReply()) {
                    return result;
                }
                session = result.getSession();
                if (null == session) {
                    // Should not occur
                    throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                }
                verifySession(req, sessiondService, sessionId, session);
                rememberSession(req, session);
                checkPublicSessionCookie(req, resp, session, sessiondService);
            }
        }

        // Try public session
        findPublicSessionId(req, session, sessiondService, false, false);
        return new SessionResult<ServerSession>(Reply.CONTINUE, session);
    }

    private static final String PARAM_ALTERNATIVE_ID = Session.PARAM_ALTERNATIVE_ID;
    private static final String PUBLIC_SESSION_PREFIX = LoginServlet.PUBLIC_SESSION_PREFIX;
    private static final String USER_AGENT = Header.USER_AGENT;

    /**
     * Looks-up <code>"open-xchange-public-session"</code> cookie and remembers appropriate session if possible to validate it.
     *
     * @param req The HTTP request
     * @param session The looked-up session
     * @param sessiondService The SessionD service
     * @param mayUseFallbackSession <code>true</code> if request is allowed to use fall-back session, otherwise <code>false</code>
     * @param mayPerformPublicSessionAuth <code>true</code> if public session authentication is allowed for specified request, otherwise <code>false</code>
     * @return <code>true</code> if an appropriate public session is found; otherwise <code>false</code>
     * @throws OXException If public session cannot be created
     */
    public static boolean findPublicSessionId(final HttpServletRequest req, final ServerSession session, final SessiondService sessiondService, final boolean mayUseFallbackSession, final boolean mayPerformPublicSessionAuth) throws OXException {
        Map<String, Cookie> cookies = Cookies.cookieMapFor(req);

        Cookie cookie = null == session ? null : cookies.get(getPublicSessionCookieName(req, new String[] { Integer.toString(session.getContextId()), Integer.toString(session.getUserId()) }));
        if (null != cookie) {
            return handlePublicSessionIdentifier(cookie.getValue(), req, session, sessiondService, false);
        }

        // Look-up cookie providing alternative session identifier
        {
            String user = req.getParameter("user");
            String context = req.getParameter("context");

            ServerSession publicSession;
            if (Strings.isEmpty(user) || Strings.isEmpty(context)) {
                publicSession = getSessionObjectByAnyAlternativeId(req, cookies);
            } else {
                publicSession = getSessionObjectByAlternativeId(req, cookies, context, user);
            }

            if (null != publicSession) {
                try {
                    if (false == mayPerformPublicSessionAuth) {
                        checkSecret(hashSource, req, publicSession, false);
                    }
                    verifySession(req, sessiondService, publicSession.getSessionID(), publicSession);
                    rememberPublicSession(req, publicSession);
                    return true;
                } catch (final OXException e) {
                    // Verification of public session failed
                }

                // Look-up failed
                return false;
            }
        }

        // No suitable cookie
        String publicSessionId = req.getParameter(PARAMETER_PUBLIC_SESSION);
        if (null != publicSessionId) {
            return handlePublicSessionIdentifier(publicSessionId, req, session, sessiondService, mayPerformPublicSessionAuth);
        }

        // No such "public_session" parameter
        if (mayUseFallbackSession && isChangeable(req)) {
            for (Map.Entry<String, Cookie> entry : cookies.entrySet()) {
                if (entry.getKey().startsWith(PUBLIC_SESSION_PREFIX)) {
                    return handlePublicSessionIdentifier(entry.getValue().getValue(), req, session, sessiondService, false);
                }
            }
        }

        // No public session found
        return false;
    }

    private static boolean handlePublicSessionIdentifier(String altId, HttpServletRequest req, ServerSession session, SessiondService sessiondService, boolean publicSessionAuth) throws OXException {
        if (null != altId && null != session && altId.equals(session.getParameter(PARAM_ALTERNATIVE_ID))) {
            // same session (thus already verified)
            rememberPublicSession(req, session);
            return true;
        }

        // Lookup session by alternative id
        final ServerSession publicSession = null == altId ? null : ServerSessionAdapter.valueOf(sessiondService.getSessionByAlternativeId(altId, publicSessionAuth));
        if (publicSession != null) {
            try {
                if (false == publicSessionAuth) {
                    checkSecret(hashSource, req, publicSession, false);
                }
                verifySession(req, sessiondService, publicSession.getSessionID(), publicSession);
                rememberPublicSession(req, publicSession);
                return true;
            } catch (final OXException e) {
                // Verification of public session failed
            }
        }

        // Look-up failed
        return false;
    }

    /**
     * Verifies given session.
     * <ul>
     * <li>Tests if specified <code>sessionId</code> and <code>session.getSessionID()</code> are equal</li>
     * <li>Tests if associated context is enabled</li>
     * <li>Checks IP address (if IP-check is enabled as per configuration)</li>
     * </ul>
     *
     * @param req The HTTP request
     * @param sessiondService The service
     * @param sessionId The session identifier
     * @param session The session
     * @throws OXException If verification fails
     */
    public static void verifySession(final HttpServletRequest req, final SessiondService sessiondService, final String sessionId, final ServerSession session) throws OXException {
        if (!sessionId.equals(session.getSessionID())) {
            LOG.info("Request's session identifier \"{}\" differs from the one indicated by SessionD service \"{}\".", sessionId, session.getSessionID());
            throw SessionExceptionCodes.WRONG_SESSION.create();
        }
        final Context ctx = session.getContext();
        if (!ctx.isEnabled()) {
            sessiondService.removeSession(sessionId);
            LOG.info("The context {} associated with session is locked.", Integer.toString(ctx.getContextId()));
            throw SessionExceptionCodes.CONTEXT_LOCKED.create(Integer.valueOf(ctx.getContextId()), ctx.getName());
        }
        checkIP(session, req.getRemoteAddr());
    }

    /**
     * Performs the IP check.
     *
     * @param session The session to check for
     * @param actual The current IP for given session
     * @throws OXException If IP check fails
     */
    public static void checkIP(final Session session, final String actual) throws OXException {
        checkIP(checkIP, getRanges(), session, actual, clientWhitelist);
    }

    private static Collection<IPRange> getRanges() {
        return RANGES;
    }

    /**
     * Checks whether passed exception indicates an IP check error.
     *
     * @param e The exception to check
     * @return <code>true</code> if passed exception indicates an IP check error; otherwise <code>false</code>
     */
    public static boolean isIpCheckError(final OXException e) {
        final SessionExceptionCodes code = SessionExceptionCodes.WRONG_CLIENT_IP;
        return (code.equals(e)) && code.getCategory().equals(e.getCategory());
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
    public static void checkIP(final boolean doCheck, final Collection<IPRange> ranges, final Session session, final String actual, final ClientWhitelist whitelist) throws OXException {
        if (null == actual || !actual.equals(session.getLocalIp())) {
            // IP is missing or changed
            if (doCheck && !isWhitelistedFromIPCheck(actual, ranges) && !isWhitelistedClient(session, whitelist) && !allowedSubnet.areInSameSubnet(actual, session.getLocalIp())) {
                // kick client with changed IP address
                {
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
                throw SessionExceptionCodes.WRONG_CLIENT_IP.create(session.getLocalIp(), null == actual ? "<unknown>" : actual);
            }
            if (null != actual) {
                if (isWhitelistedClient(session, whitelist)) {
                    // change IP in session so the IMAP NOOP command contains the correct client IP address (Bug #21842)
                    session.setLocalIp(actual);
                } else if (!doCheck) {
                    // Do not change session's IP address anymore in case of USM/EAS (Bug #29136)
                    if (!isUsmEas(session.getClient())) {
                        session.setLocalIp(actual);
                    }
                }
            }
            if (LOG.isDebugEnabled() && !isWhitelistedFromIPCheck(actual, ranges) && !isWhitelistedClient(session, whitelist)) {
                LOG.debug("Session {} requests now from {} but login came from {}", session.getSessionID(), actual, session.getLocalIp());
            }
        }
    }

    private static boolean isUsmEas(final String clientId) {
        if (Strings.isEmpty(clientId)) {
            return false;
        }
        final String uc = Strings.toUpperCase(clientId);
        return uc.startsWith("USM-EAS") || uc.startsWith("USM-JSON");
    }

    /**
     * White listed clients are necessary for the Mobile Web Interface. This clients often change their IP address in mobile data networks.
     */
    private static boolean isWhitelistedClient(final Session session, final ClientWhitelist whitelist) {
        if (null == whitelist || whitelist.isEmpty()) {
            return false;
        }
        return whitelist.isAllowed(session.getClient());
    }

    public static boolean isWhitelistedFromIPCheck(final String actual, final Collection<IPRange> ranges) {
        for (final IPRange range : ranges) {
            if (range.contains(actual)) {
                return true;
            }
        }
        return false;
    }

    /**
     * (Optionally) Gets the session identifier from the request.
     *
     * @param req The Servlet request
     * @return The session identifier or <code>null</code>
     */
    public static String optSessionId(ServletRequest req) {
        return req.getParameter(PARAMETER_SESSION);
    }

    /**
     * Gets the session identifier from the request.
     *
     * @param req The Servlet request
     * @return The session identifier
     * @throws OXException If the session identifier can not be found.
     */
    public static String getSessionId(ServletRequest req) throws OXException {
        String retval = optSessionId(req);
        if (null == retval) {
            /*
             * Throw an error...
             */
            {
                final StringBuilder sb = new StringBuilder(32);
                sb.append("Parameter \"").append(PARAMETER_SESSION).append("\" not found");
                if (LOG.isDebugEnabled()) {
                    sb.append(": ");
                    final Enumeration<?> enm = req.getParameterNames();
                    while (enm.hasMoreElements()) {
                        sb.append(enm.nextElement());
                        sb.append(',');
                    }
                    if (sb.length() > 0) {
                        sb.setCharAt(sb.length() - 1, '.');
                    }
                }
                LOG.info(sb.toString());
            }
            throw SessionExceptionCodes.SESSION_PARAMETER_MISSING.create();
        }
        return retval;
    }

    /**
     * Checks if a valid session exists in terms of the passed ID and servlet request.
     * If the session ID is valid, the according sessions secret will be checked against
     * the cookies of the servlet request.
     *
     * @param req The associated HTTP request
     * @param resp The associated HTTP response
     * @param sessionId identifier of the session.
     * @param sessiondService The SessionD service
     * @return The session result
     * @throws OXException If the session can not be found. The following error codes indicate
     *         a validation error:
     *         <ul>
     *          <li>{@link SessionExceptionCodes#SESSION_EXPIRED}: The session ID is invalid or
     *              the according context or user have been deleted/disabled.</li>
     *          <li>{@link SessionExceptionCodes#WRONG_SESSION_SECRET}: The session of the
     *              passed ID does not match to the requests secret cookie.</li>
     *         </ul>
     */
    public static SessionResult<ServerSession> getSession(HttpServletRequest req, HttpServletResponse resp, String sessionId, SessiondService sessiondService) throws OXException {
        return getSession(hashSource, req, resp, sessionId, sessiondService);
    }

    /**
     * Checks if a valid session exists in terms of the passed ID and servlet request.
     * If the session ID is valid, the according sessions secret will be checked against
     * the cookies of the servlet request.
     *
     * @param source defines how the cookie should be found
     * @param req The associated HTTP request
     * @param resp The associated HTTP response
     * @param sessionId identifier of the session.
     * @param sessiondService The SessionD service
     * @return The session result
     * @throws OXException If the session can not be found. The following error codes indicate
     *         a validation error:
     *         <ul>
     *          <li>{@link SessionExceptionCodes#SESSION_EXPIRED}: The session ID is invalid or
     *              the according context or user have been deleted/disabled.</li>
     *          <li>{@link SessionExceptionCodes#WRONG_SESSION_SECRET}: The session of the
     *              passed ID does not match to the requests secret cookie.</li>
     *         </ul>
     */
    public static SessionResult<ServerSession> getSession(CookieHashSource source, HttpServletRequest req, HttpServletResponse resp, String sessionId, SessiondService sessiondService) throws OXException {
        return getSession(source, req, resp, sessionId, sessiondService, null);
    }

    /**
     * Checks if a valid session exists in terms of the passed ID and servlet request.
     * If the session ID is valid, the according sessions secret will be checked against
     * the cookies of the servlet request.
     *
     * @param source defines how the cookie should be found
     * @param req The associated HTTP request
     * @param resp The associated HTTP response
     * @param sessionId identifier of the session.
     * @param sessiondService The SessionD service
     * @param optChecker The {@link SessionSecretChecker} to verify the secret cookie.
     *        May be <code>null</code> to use the default.
     * @return The session result
     * @throws OXException If the session can not be found. The following error codes indicate
     *         a validation error:
     *         <ul>
     *          <li>{@link SessionExceptionCodes#SESSION_EXPIRED}: The session ID is invalid or
     *              the according context or user have been deleted/disabled.</li>
     *          <li>{@link SessionExceptionCodes#WRONG_SESSION_SECRET}: The session of the
     *              passed ID does not match to the requests secret cookie.</li>
     *         </ul>
     */
    public static SessionResult<ServerSession> getSession(CookieHashSource source, HttpServletRequest req, HttpServletResponse resp, String sessionId, SessiondService sessiondService, SessionSecretChecker optChecker) throws OXException {
        Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            if (!"unset".equals(sessionId)) {
                LOG.info("There is no session associated with session identifier: {}", sessionId);
            }
            /*
             * Session MISS -- Consult session inspector
             */
            if (Reply.STOP == SessionInspector.getInstance().getChain().onSessionMiss(sessionId, req, resp)) {
                return new SessionResult<ServerSession>(Reply.STOP, null);
            }

            // Otherwise throw appropriate error
            throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
        }
        /*
         * Session HIT -- Consult session inspector
         */
        if (Reply.STOP == SessionInspector.getInstance().getChain().onSessionHit(session, req, resp)) {
            return new SessionResult<ServerSession>(Reply.STOP, ServerSessionAdapter.valueOf(session));
        }
        /*
         * Get session secret
         */
        if (null == optChecker) {
            checkSecret(source, req, session);
        } else {
            optChecker.checkSecret(session, req, source.name());
        }
        try {
            User user = UserStorage.getInstance().getUser(session.getUserId(), ContextStorage.getInstance().getContext(session.getContextId()));
            if (!user.isMailEnabled()) {
                LOG.info("User {} in context {} is not activated.", Integer.toString(user.getId()), Integer.toString(session.getContextId()));
                throw SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
            }
            return new SessionResult<ServerSession>(Reply.CONTINUE, ServerSessionAdapter.valueOf(session));
        } catch (OXException e) {
            if (ContextExceptionCodes.NOT_FOUND.equals(e)) {
                // An outdated session; context absent
                sessiondService.removeSession(sessionId);
                LOG.info("The context associated with session \"{}\" cannot be found. Obviously an outdated session which is invalidated now.", sessionId);
                throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            }
            if (UserExceptionCode.USER_NOT_FOUND.getPrefix().equals(e.getPrefix())) {
                int code = e.getCode();
                if (UserExceptionCode.USER_NOT_FOUND.getNumber() == code || LdapExceptionCode.USER_NOT_FOUND.getNumber() == code) {
                    // An outdated session; user absent
                    sessiondService.removeSession(sessionId);
                    LOG.info("The user associated with session \"{}\" cannot be found. Obviously an outdated session which is invalidated now.", sessionId);
                    throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                }
            }
            throw e;
        } catch (UndeclaredThrowableException e) {
            throw UserExceptionCode.USER_NOT_FOUND.create(e, I(session.getUserId()), I(session.getContextId()));
        }
    }

    private static final Set<String> AGENTS_WO_PUBLIC_SESSION_COOKIE = ImmutableSet.of("open-xchange usm http client");

    /**
     * Checks presence of public session cookie.
     *
     * @param req The request
     * @param resp The response
     * @param session The request-associated session
     * @param sessiondService The <code>SessiondService</code> instance
     */
    public static void checkPublicSessionCookie(final HttpServletRequest req, final HttpServletResponse resp, final Session session, final SessiondService sessiondService) {
        final String userAgent = HashCalculator.getUserAgent(req);
        if (AGENTS_WO_PUBLIC_SESSION_COOKIE.contains(userAgent.toLowerCase())) {
            return;
        }

        // Check for public session cookie
        final Map<String, Cookie> cookies = Cookies.cookieMapFor(req);
        if (null != cookies) {
            final String cookieName = getPublicSessionCookieName(req, new String[] { Integer.toString(session.getContextId()), Integer.toString(session.getUserId()) });
            if (null == cookies.get(cookieName)) {
                final boolean restored = LoginServlet.writePublicSessionCookie(req, resp, session, req.isSecure(), req.getServerName());
                if (restored) {
                    LOG.info("Restored public session cookie for \"{}\": {} (User-Agent: {})", session.getLogin(), cookieName, userAgent);
                }
            }
        }
    }

    /**
     * Check if the secret encoded in the open-xchange-secret Cookie matches the secret saved in the session.
     *
     * @param source    The configured CookieHashSource
     * @param req       The incoming HttpServletRequest
     * @param session   The Session object looked up for the incoming request
     * @param logInfo   Whether to log info or not
     * @throws OXException If the secrets differ
     */
    public static void checkSecret(final CookieHashSource source, final HttpServletRequest req, final Session session) throws OXException {
        checkSecret(source, req, session, true);
    }

    /**
     * Check if the secret encoded in the open-xchange-secret Cookie matches the secret saved in the session.
     *
     * @param source    The configured CookieHashSource
     * @param req       The incoming HttpServletRequest
     * @param session   The Session object looked up for the incoming request
     * @param logInfo   Whether to log info or not
     * @throws OXException If the secrets differ
     */
    public static void checkSecret(final CookieHashSource source, final HttpServletRequest req, final Session session, final boolean logInfo) throws OXException {
        String[] additionalsForHash;
        if (Boolean.TRUE.equals(session.getParameter(Session.PARAM_GUEST))) {
            /*
             * inject context- and user-id to allow parallel guest sessions
             */
            additionalsForHash = new String[] { Integer.toString(session.getContextId()), Integer.toString(session.getUserId()) };
        } else {
            additionalsForHash = null;
        }
        final String secret = extractSecret(source, req, session.getHash(), session.getClient(), (String) session.getParameter("user-agent"), additionalsForHash);
        if (secret == null || !session.getSecret().equals(secret)) {
            if (logInfo && null != secret) {
                LOG.info("Session secret is different. Given secret \"{}\" differs from secret in session \"{}\".", secret, session.getSecret());
            }
            final OXException oxe = SessionExceptionCodes.WRONG_SESSION_SECRET.create();
            oxe.setProperty(SessionExceptionCodes.WRONG_SESSION_SECRET.name(), null == secret ? "null" : secret);
            throw oxe;
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
        return extractSecret(cookieHash, req, hash, client, null);
    }

    private static final String SECRET_PREFIX = LoginServlet.SECRET_PREFIX;

    /**
     * Extracts the secret string from specified cookies using given hash string.
     *
     * @param hashSource The hash source for the secret cookie
     * @param req The HTTP Servlet request object.
     * @param hash The remembered hash from session.
     * @param client The remembered client from the session.
     * @param originalUserAgent The original <tt>'User-Agent'</tt> associated with session
     * @return The secret string or <tt>null</tt>
     */
    public static String extractSecret(final CookieHashSource hashSource, final HttpServletRequest req, final String hash, final String client, final String originalUserAgent) {
        return extractSecret(hashSource, req, hash, client, originalUserAgent, (String[])null);
    }

    /**
     * Extracts the secret string from the cookies supplied with the HTTP servlet request, based on the remembered hash-, client-
     * and original user-agent-values stored in the session.
     *
     * @param hashSource The hash source for the secret cookie
     * @param request The underlying HTTP servlet request
     * @return The secret string, or <code>null</code> if no matching secret cookie was found in the request
     */
    public static String extractSecret(CookieHashSource hashSource, HttpServletRequest request, Session session) {
        String hash = session.getHash();
        String client = session.getClient();
        String originalUserAgent = (String) session.getParameter("user-agent");
        String[] additionalsForHash;
        if (Boolean.TRUE.equals(session.getParameter(Session.PARAM_GUEST))) {
            additionalsForHash = new String[] { Integer.toString(session.getContextId()), Integer.toString(session.getUserId()) };
        } else {
            additionalsForHash = null;
        }
        return extractSecret(hashSource, request, hash, client, originalUserAgent, additionalsForHash);
    }

    /**
     * Extracts the secret string from specified cookies using given hash string.
     *
     * @param hashSource The hash source for the secret cookie
     * @param req The HTTP Servlet request object.
     * @param hash The remembered hash from session.
     * @param client The remembered client from the session.
     * @param originalUserAgent The original <tt>'User-Agent'</tt> associated with session
     * @param additionalsForHash Additional values to include when calculating the client-specific hash for the cookie names, or
     *                           <code>null</code> if not needed
     * @return The secret string or <tt>null</tt>
     */
    public static String extractSecret(final CookieHashSource hashSource, final HttpServletRequest req, final String hash, final String client, final String originalUserAgent, String...additionalsForHash) {
        final Map<String, Cookie> cookies = Cookies.cookieMapFor(req);
        if (null != cookies) {
            if (cookies.isEmpty()) {
                LOG.info("Empty Cookies in HTTP request. No session secret can be looked up.");
            } else {
                final String secretPrefix = SECRET_PREFIX;
                final StringBuilder tmp = new StringBuilder(256);
                final String expectedSecretCookieName = tmp.append(secretPrefix).append(getHash(hashSource, req, hash, client, additionalsForHash)).toString();

                // Look-up Cookie by expected name
                Cookie cookie = cookies.get(expectedSecretCookieName);
                if (null != cookie) {
                    return cookie.getValue();
                }

                // Check for special User-Agent to allow look-up by remembered cookie name
                if (isChangeable(req)) {
                    tmp.setLength(0);
                    cookie = cookies.get(tmp.append(secretPrefix).append(hash).toString());
                    if (null != cookie) {
                        return cookie.getValue();
                    }
                }

                // All look-up attempts failed - Log information
                tmp.setLength(0);
                for (final String cookieName : cookies.keySet()) {
                    if (cookieName.startsWith(secretPrefix)) {
                        tmp.append(cookieName.substring(secretPrefix.length())).append(", ");
                    }
                }
                final int hlen = tmp.length();
                if (hlen > 0) {
                    tmp.setLength(hlen - 2);
                    LOG.debug("Didn't find an appropriate Cookie for expected name \"{}\" (CookieHashSource={}) which provides the session secret. Remembered hash: {}. Available hashes: {}", expectedSecretCookieName, hashSource.toString(), hash, tmp.toString());
                } else {
                    LOG.debug("Didn't find an appropriate Cookie for expected name \"{}\" (CookieHashSource={}) which provides the session secret. Remembered hash={}. No available hashes.", expectedSecretCookieName, hashSource.toString(), hash);
                }
            }
        } else {
            LOG.debug("Missing Cookies in HTTP request. No session secret can be looked up.");
        }
        return null;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private static boolean isChangeable(HttpServletRequest req) {
        return isChangeableUserAgent(req.getHeader(USER_AGENT));
    }

    private static boolean isChangeableUserAgent(String userAgent) {
        return isMediaPlayerAgent(userAgent) || isMSIE11(userAgent);
    }

    private static final Set<String> MEDIA_AGENTS = ImmutableSet.of("applecoremedia/", "stagefright/");

    private static boolean isMediaPlayerAgent(String userAgent) {
        if (null == userAgent) {
            return false;
        }
        final String lcua = toLowerCase(userAgent);
        for (final String agentPrefix : MEDIA_AGENTS) {
            if (lcua.startsWith(agentPrefix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMSIE11(String userAgent) {
        if (null == userAgent) {
            return false;
        }
        return ServerServiceRegistry.getServize(UserAgentParser.class).matches(userAgent, UserAgentFamily.IE, 11);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the appropriate hash for specified request.
     *
     * @param hashSource Specifies how the cookie should be found.
     * @param req The HTTP request
     * @param hash The previously remembered hash
     * @param client The client identifier
     * @param additionalsForHash Additional values to include when calculating the client-specific hash for the cookie names, or
     *                           <code>null</code> if not needed
     * @return The appropriate hash
     */
    public static String getHash(final CookieHashSource hashSource, final HttpServletRequest req, final String hash, final String client, String[] additionalsForHash) {
        if (CookieHashSource.REMEMBER == hashSource) {
            return hash;
        }
        // Default is calculate
        return HashCalculator.getInstance().getHash(req, HashCalculator.getUserAgent(req), client, additionalsForHash);
    }

    /**
     * Convenience method to remember the session for a request in the servlet attributes.
     *
     * @param req The servlet request.
     * @param session The session to remember.
     */
    public static void rememberSession(final HttpServletRequest req, final ServerSession session) {
        req.setAttribute(SESSION_KEY, session);
        session.setParameter("JSESSIONID", req.getSession().getId());
    }

    /**
     * Convenience method to remember the public session for a request in the servlet attributes.
     *
     * @param req The servlet request.
     * @param session The public session to remember.
     */
    public static void rememberPublicSession(final HttpServletRequest req, final ServerSession session) {
        req.setAttribute(PUBLIC_SESSION_KEY, session);
        session.setParameter("JSESSIONID", req.getSession().getId());
    }

    /**
     * Removes the Open-Xchange cookies belonging to specified hash string.
     *
     * @param hash The hash string identifying appropriate session and secret cookie
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    public static void removeOXCookies(String hash, HttpServletRequest req, HttpServletResponse resp) {
        removeOXCookies(hash, req, resp, null);
    }

    /**
     * Removes the Open-Xchange cookies belonging to specified hash string.
     *
     * @param hash The hash string identifying appropriate session and secret cookie
     * @param req The HTTP request
     * @param resp The HTTP response
     * @param optSession The associated session if available/known (needed to reliably remove public session cookie); otherwise <code>null</code> (public session cookie not removed then)
     */
    public static void removeOXCookies(String hash, HttpServletRequest req, HttpServletResponse resp, Session optSession) {
        Map<String, Cookie> cookies = Cookies.cookieMapFor(req);
        if (cookies == null) {
            return;
        }

        // Drop "open-xchange-session" cookie
        {
            Cookie cookie = cookies.get(SESSION_PREFIX + hash);
            if (null != cookie) {
                removeCookie(cookie, resp);
            }
        }

        // Drop "open-xchange-secret" cookie
        {
            Cookie cookie = cookies.get(SECRET_PREFIX + hash);
            if (null != cookie) {
                removeCookie(cookie, resp);
            }
        }

        // Drop "open-xchange-share" cookie
        {
            Cookie cookie = cookies.get(LoginServlet.getShareCookieName(req));
            if (null != cookie) {
                removeCookie(cookie, resp);
            }
        }

        // Only possible to drop accompanying "open-xchange-public-session" cookie if a session is given
        if (null != optSession) {
            String cookieName = getPublicSessionCookieName(req, new String[] { Integer.toString(optSession.getContextId()), Integer.toString(optSession.getUserId()) });
            Cookie cookie = cookies.get(cookieName);
            if (null != cookie) {
                removeCookie(cookie, resp);
            }
        }
    }

    /**
     * Removes the Open-Xchange cookies belonging to a specific session. This includes
     * <ul>
     * <li>A cookie named "open-xchange-session-<code>{session.getHash()}</code>"</li>
     * <li>A cookie named "open-xchange-secret-<code>{session.getHash()}</code>"</li>
     * <li>A cookie named "open-xchange-public-session-<code>{HashCalculator.getUserAgentHash(request)}</code>" matching the sessions alternative identifier</li>
     * <li>A cookie named "open-xchange-share-<code>{HashCalculator.getUserAgentHash(request)}</code>" in case of a guest session</li>
     * </ul>
     *
     * @param session The session to remove the cookies for
     * @param request The HTTP request
     * @param response The HTTP response
     */
    public static void removeOXCookies(Session session, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
        String sessionHash = session.getHash();
        removeCookie(cookies, response, SESSION_PREFIX + sessionHash);
        removeCookie(cookies, response, SECRET_PREFIX + sessionHash);
        removeCookie(cookies, response, getPublicSessionCookieName(request, new String[] { Integer.toString(session.getContextId()), Integer.toString(session.getUserId()) }), (String) session.getParameter(Session.PARAM_ALTERNATIVE_ID));
        if (Boolean.TRUE.equals(session.getParameter(Session.PARAM_GUEST))) {
            removeCookie(cookies, response, LoginServlet.getShareCookieName(request));
        }
    }

    /**
     * Removes the Open-Xchange cookies belonging to specified hash string.
     *
     * @param req The HTTP request
     * @param resp The HTTP response
     * @param cookieNames The names of the cookies to remove
     */
    public static void removeOXCookies(final HttpServletRequest req, final HttpServletResponse resp, final List<String> cookieNames) {
        final Map<String, Cookie> cookies = Cookies.cookieMapFor(req);
        if (cookies == null) {
            return;
        }
        for (final String cookieName : cookieNames) {
            final Cookie cookie = cookies.get(cookieName);
            if (null != cookie) {
                removeCookie(cookie, resp);
            }
        }
    }

    /**
     * Removes all JSESSIONID cookies found in given HTTP Servlet request.
     *
     * @param req The HTTP Servlet request
     * @param resp The HTTP Servlet response
     */
    public static void removeJSESSIONID(final HttpServletRequest req, final HttpServletResponse resp) {
        final Map<String, Cookie> cookies = Cookies.cookieMapFor(req);
        if (cookies == null) {
            return;
        }
        final String name = Tools.JSESSIONID_COOKIE;
        final Cookie cookie = cookies.get(name);
        if (null != cookie) {
            removeCookie(cookie, resp);
        }
    }

    /**
     * Removes a given cookie by setting its MaxAge parameter to 0.
     *
     * @param cookie The cookie
     * @param resp The HTTP Servlet response
     */
    public static void removeCookie(final Cookie cookie, final HttpServletResponse resp) {
        final String name = cookie.getName();
        final String value = cookie.getValue();
        final Cookie respCookie = new Cookie(name, value);
        respCookie.setPath("/");
        final String domain = extractDomainValue(value);
        if (null != domain) {
            respCookie.setDomain(domain);
            // Once again without domain parameter
            final Cookie respCookie2 = new Cookie(name, value);
            respCookie2.setPath("/");
            respCookie2.setMaxAge(0); // delete
            resp.addCookie(respCookie2);
        }
        respCookie.setMaxAge(0); // delete
        resp.addCookie(respCookie);
    }

    /**
     * Returns the remembered session.
     *
     * @param req The Servlet request.
     * @return The remembered session.
     */
    public static ServerSession getSessionObject(final ServletRequest req) {
        return getSessionObject(req, false);
    }

    /**
     * Returns the remembered session.
     *
     * @param req The Servlet request.
     * @param mayUseFallbackSession <code>true</code> to look-up fall-back session; otherwise <code>false</code>
     * @return The remembered session
     */
    public static ServerSession getSessionObject(final ServletRequest req, final boolean mayUseFallbackSession) {
        final Object attribute = req.getAttribute(SESSION_KEY);
        if (attribute != null) {
            return (ServerSession) attribute;
        }
        if (mayUseFallbackSession) {
            return (ServerSession) req.getAttribute(PUBLIC_SESSION_KEY);
        }
        // No session found
        {
            final HttpServletRequest httpRequest = (HttpServletRequest) req;
            LogProperties.put(LogProperties.Name.SERVLET_SERVLET_PATH, httpRequest.getServletPath());
            final String pathInfo = httpRequest.getPathInfo();
            if (null != pathInfo) {
                LogProperties.put(LogProperties.Name.SERVLET_PATH_INFO, pathInfo);
            }
            final String queryString = httpRequest.getQueryString();
            if (null != queryString) {
                LogProperties.put(LogProperties.Name.SERVLET_QUERY_STRING, LogProperties.getSanitizedQueryString(queryString));
            }
        }
        return null;
    }

    /**
     * Get session by public-session cookie for userId in contextId
     *
     * @param req The HTTP request
     * @param cookies The cookies map (as returned from <code>Cookies.cookieMapFor(HttpServletRequest)</code>)
     * @param context The context identifier
     * @param user The user identifier
     * @return The session identified by given user in context
     * @throws OXException If context cannot be resolved
     */
    private static ServerSession getSessionObjectByAlternativeId(HttpServletRequest req, Map<String, Cookie> cookies, String context, String user) throws OXException {
        SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null == sessiondService) {
            return null;
        }

        Cookie publicSessionCookie = cookies.get(getPublicSessionCookieName(req, new String[] { context, user }));
        if (null == publicSessionCookie) {
            return null;
        }

        Session session = sessiondService.getSessionByAlternativeId(publicSessionCookie.getValue());
        return null == session ? null : ServerSessionAdapter.valueOf(session);
    }

    /**
     * Search public-session cookies for any active session
     *
     * @param req The HTTP request
     * @param cookies The cookies map (as returned from <code>Cookies.cookieMapFor(HttpServletRequest)</code>)
     * @return A matching session or <code>null</code>
     * @throws OXException If look-up fails
     */
    private static ServerSession getSessionObjectByAnyAlternativeId(HttpServletRequest req, Map<String, Cookie> cookies) throws OXException {
        SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null == sessiondService) {
            return null;
        }

        for (Map.Entry<String, Cookie> cookieEntry : cookies.entrySet()) {
            String name = cookieEntry.getKey();
            if (name.startsWith(PUBLIC_SESSION_PREFIX)) {
                // Found a possible candidate for a matching public session cookie. Use its value (alternative session ID) to get associated session
                Session session = sessiondService.getSessionByAlternativeId(cookieEntry.getValue().getValue());
                if (null != session) {
                    // There is such a session for alternative session ID; check the expected cookie name in order to verify the session belongs to current cookie
                    String expectedName = getPublicSessionCookieName(req, new String[] { Integer.toString(session.getContextId()), Integer.toString(session.getUserId()) });
                    if (name.equals(expectedName)) {
                        // Check secret
                        return ServerSessionAdapter.valueOf(session);
                    }
                }
            }
        }

        // No suitable session
        return null;
    }

    /**
     * Removes a cookie matching a specific name by setting appropriate headers in the response.
     *
     * @param existingCookies The currently existing cookies in the client as carried with the corresponding request
     * @param response The response for instructing the client to remove the cookie
     * @param name The name of the cookie to remove
     * @return <code>true</code> if a matching cookie is was found and is going to be removed, <code>false</code>, otherwise
     */
    private static boolean removeCookie(Map<String, Cookie> existingCookies, HttpServletResponse response, String name) {
        return removeCookie(existingCookies, response, name, null);
    }

    /**
     * Removes a cookie matching a specific name (and optional value) by setting appropriate headers in the response.
     *
     * @param existingCookies The currently existing cookies in the client as carried with the corresponding request
     * @param response The response for instructing the client to remove the cookie
     * @param name The name of the cookie to remove
     * @param value The value of the cookie to remove, or <code>null</code> to only match by name
     * @return <code>true</code> if a matching cookie is was found and is going to be removed, <code>false</code>, otherwise
     */
    private static boolean removeCookie(Map<String, Cookie> existingCookies, HttpServletResponse response, String name, String value) {
        if (null != existingCookies) {
            Cookie cookie = existingCookies.get(name);
            if (null != cookie && (null == value || value.equals(cookie.getValue()))) {
                removeCookie(cookie, response);
                return true;
            }
        }
        return false;
    }

    // ------------------------------------- Private constructor -------------------------------------------------- //

    /**
     * Initializes a new {@link SessionUtility}.
     */
    private SessionUtility() {
        super();
    }

}
