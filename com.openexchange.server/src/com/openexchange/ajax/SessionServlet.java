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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.log.LogProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.sessiond.impl.ThreadLocalSessionHolder;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * Overridden service method that checks if a valid session can be found for the request.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class SessionServlet extends AJAXServlet {

    private static final long serialVersionUID = -8308340875362868795L;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessionServlet.class));

    public static final String SESSION_KEY = "sessionObject";

    public static final String SESSION_WHITELIST_FILE = "noipcheck.cnf";

    private static final Queue<IPRange> RANGES = new ConcurrentLinkedQueue<IPRange>();

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    private static volatile boolean checkIP = true;

    private static volatile CookieHashSource hashSource;

    private static volatile boolean rangesLoaded;

    private static final Lock RANGE_LOCK = new ReentrantLock();

    /**
     * Initializes a new {@link SessionServlet}.
     */
    protected SessionServlet() {
        super();
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        if (INITIALIZED.compareAndSet(false, true)) {
            checkIP = Boolean.parseBoolean(config.getInitParameter(ServerConfig.Property.IP_CHECK.getPropertyName()));
            hashSource = CookieHashSource.parse(config.getInitParameter(Property.COOKIE_HASH.getPropertyName()));
        }
        initRanges(config);
    }

    private void initRanges(final ServletConfig config) {
        if (rangesLoaded) {
            return;
        }
        if (checkIP) {
            String text = null;
            text = config.getInitParameter(SESSION_WHITELIST_FILE);
            if (text == null) {
                // Fall back to configuration service
                final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (configurationService != null) {
                    text = config.getInitParameter(SESSION_WHITELIST_FILE);
                } else {
                    //LOG.error("Can't load IP Check whitelist file. Please check that the servlet activator is in order");
                    return;
                }
            }
            rangesLoaded = true;
            if (text != null) {
                LOG.info("Exceptions from IP Check have been defined.");
                RANGE_LOCK.lock();
                try {
                    // Serialize range parsing. This might happen more than once, but shouldn't matter, since the list
                    // is accessed exclusively, so it winds up correct.
                    RANGES.clear();
                    final String[] lines = text.split("\n");
                    for (String line : lines) {
                        line = line.replaceAll("\\s", "");
                        if (!line.equals("") && !line.startsWith("#")) {
                            RANGES.add(IPRange.parseRange(line));
                        }
                    }
                } finally {
                    RANGE_LOCK.unlock();
                }
            }

        } else {
            rangesLoaded = true;
        }
    }

    protected void initializeSession(final HttpServletRequest req) throws OXException {
        if (null != getSessionObject(req)) {
            return;
        }
        /*
         * Remember session
         */
        final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (sessiondService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }
        final String sessionId = getSessionId(req);
        final ServerSession session = getSession(req, sessionId, sessiondService);
        if (LogProperties.isEnabled()) {
            final Map<String, Object> properties = LogProperties.getLogProperties();
            properties.put("com.openexchange.session.sessionId", sessionId);
            properties.put("com.openexchange.session.userId", Integer.valueOf(session.getUserId()));
            properties.put("com.openexchange.session.contextId", Integer.valueOf(session.getContextId()));
        }
        if (!sessionId.equals(session.getSessionID())) {
            throw SessionExceptionCodes.WRONG_SESSION.create();
        }
        final Context ctx = session.getContext();
        if (!ctx.isEnabled()) {
            sessiondService.removeSession(sessionId);
            throw SessionExceptionCodes.CONTEXT_LOCKED.create();
        }
        checkIP(session, req.getRemoteAddr());
        rememberSession(req, session);
    }

    /**
     * Checks the session ID supplied as a query parameter in the request URI.
     */
    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        Tools.disableCaching(resp);
        AtomicInteger counter = null;
        try {
            initializeSession(req);
            final ServerSession session = getSessionObject(req);
            /*
             * Assign counter
             */
            final int maxConcurrentRequests = getMaxConcurrentRequests(session);
            if (maxConcurrentRequests > 0) {
                counter = (AtomicInteger) session.getParameter(Session.PARAM_COUNTER);
                if (null != counter) {
                    if (counter.incrementAndGet() > maxConcurrentRequests) {
                        throw AjaxExceptionCodes.TOO_MANY_REQUESTS.create();
                    }
                }
            }
            super.service(req, resp);
        } catch (final OXException e) {
            e.log(LOG);
            final Response response = new Response(getSessionObject(req));
            response.setException(e);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
                writer.flush();
            } catch (final JSONException e1) {
                log(RESPONSE_ERROR, e1);
                sendError(resp);
            }
        } finally {
            ThreadLocalSessionHolder.getInstance().setSession(null);
            if (LogProperties.isEnabled()) {
                LogProperties.putLogProperty("com.openexchange.session.sessionId", null);
                LogProperties.putLogProperty("com.openexchange.session.userId", null);
                LogProperties.putLogProperty("com.openexchange.session.contextId", null);
            }
            if (null != counter) {
                counter.getAndDecrement();
            }
        }
    }

    private static int getMaxConcurrentRequests(final ServerSession session) {
        final Set<String> set = session.getUser().getAttributes().get("ajax.maxCount");
        if (null == set || set.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(set.iterator().next());
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    protected void superService(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    private void checkIP(final Session session, final String actual) throws OXException {
        checkIP(checkIP, getRanges(), session, actual);
    }

    private Queue<IPRange> getRanges() {
        return RANGES;
    }

    /**
     * Handle specified SessionD exception.
     *
     * @param e The SessionD exception
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    protected void handleSessiondException(final OXException e, final HttpServletRequest req, final HttpServletResponse resp) {
        if (isIpCheckError(e)) {
            try {
                // Drop Open-Xchange cookies
                final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                final String sessionId = getSessionId(req);
                final ServerSession session = getSession(req, sessionId, sessiondService);
                removeOXCookies(session.getHash(), req, resp);
                removeJSESSIONID(req, resp);
                sessiondService.removeSession(sessionId);
            } catch (final Exception e2) {
                LOG.error("Cookies could not be removed.", e2);
            }
        }
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
     * @param checkIP <code>true</code> to deny request with an exception.
     * @param ranges The white-list ranges
     * @param session session object
     * @param actual IP address of the current request.
     * @throws OXException if the IP addresses don't match.
     */
    public static void checkIP(final boolean checkIP, final Queue<IPRange> ranges, final Session session, final String actual) throws OXException {
        if (null == actual || (!isWhitelistedFromIPCheck(actual, ranges) && !actual.equals(session.getLocalIp()))) {
            if (checkIP) {
                LOG.info("Request to server denied for session: " + session.getSessionID() + ". Client login IP changed from " + session.getLocalIp() + " to " + actual + ".");
                throw SessionExceptionCodes.WRONG_CLIENT_IP.create();
            }
            if (LOG.isDebugEnabled()) {
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

    public static boolean isWhitelistedFromIPCheck(final String actual, final Queue<IPRange> ranges) {
        for (final IPRange range : ranges) {
            if (range.contains(actual)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the cookie identifier from the request.
     *
     * @param req servlet request.
     * @return the cookie identifier.
     * @throws OXException if the cookie identifier can not be found.
     */
    protected static String getSessionId(final ServletRequest req) throws OXException {
        final String retval = req.getParameter(PARAMETER_SESSION);
        if (null == retval) {
            /*
             * Throw an error...
             */
            if (LOG.isDebugEnabled()) {
                final StringBuilder debug = new StringBuilder();
                debug.append("Parameter session not found: ");
                final Enumeration<?> enm = req.getParameterNames();
                while (enm.hasMoreElements()) {
                    debug.append(enm.nextElement());
                    debug.append(',');
                }
                if (debug.length() > 0) {
                    debug.setCharAt(debug.length() - 1, '.');
                }
                LOG.debug(debug.toString());
            }
            throw SessionExceptionCodes.SESSION_PARAMETER_MISSING.create();
        }
        return retval;
    }

    /**
     * Finds appropriate local session.
     *
     * @param sessionId identifier of the session.
     * @param sessiondService The SessionD service
     * @return the session.
     * @throws OXException if the session can not be found.
     */
    public ServerSession getSession(final HttpServletRequest req, final String sessionId, final SessiondService sessiondService) throws OXException {
        return getSession(hashSource, req, sessionId, sessiondService);
    }

    /**
     * Finds appropriate local session.
     *
     * @param hashSource defines how the cookie should be found
     * @param sessionId identifier of the session.
     * @param sessiondService The SessionD service
     * @return the session.
     * @throws SessionException if the session can not be found.
     */
    public static ServerSession getSession(final CookieHashSource hashSource, final HttpServletRequest req, final String sessionId, final SessiondService sessiondService) throws OXException {
        final Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
        }
        final String secret = extractSecret(hashSource, req, session.getHash(), session.getClient());

        if (secret == null || !session.getSecret().equals(secret)) {
            throw SessionExceptionCodes.WRONG_SESSION_SECRET.create(secret, session.getSecret());
        }
        final Context context;
        final User user;
        try {
            context = ContextStorage.getInstance().getContext(session.getContextId());
            user = UserStorage.getInstance().getUser(session.getUserId(), context);
            if (!user.isMailEnabled()) {
                throw SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
            }
        } catch (final UndeclaredThrowableException e) {
            throw UserExceptionCode.USER_NOT_FOUND.create(e, I(session.getUserId()), I(session.getContextId()));
        }
        return new ServerSessionAdapter(session, context, user);
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
        final String retval;
        switch (cookieHash) {
        default:
        case CALCULATE:
            retval = HashCalculator.getHash(req, client);
            break;
        case REMEMBER:
            retval = hash;
            break;
        }
        return retval;
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
     * Removes the Open-Xchange cookies belonging to specified hash string.
     *
     * @param hash The hash string identifying appropriate cookie
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    public static void removeOXCookies(final String hash, final HttpServletRequest req, final HttpServletResponse resp) {
        final Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return;
        }
        final List<String> cookieNames = Arrays.asList(Login.SESSION_PREFIX + hash, Login.SECRET_PREFIX + hash);
        for (final Cookie cookie : cookies) {
            final String name = cookie.getName();

            for (final String string : cookieNames) {
                if (name.startsWith(string)) {
                    final Cookie respCookie = new Cookie(name, cookie.getValue());
                    respCookie.setPath("/");
                    respCookie.setMaxAge(0); // delete
                    resp.addCookie(respCookie);
                }
            }
        }
    }

    public static void removeJSESSIONID(final HttpServletRequest req, final HttpServletResponse resp) {
        final Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return;
        }
        for (final Cookie cookie : cookies) {
            final String name = cookie.getName();
            if (Tools.JSESSIONID_COOKIE.equals(name)) {
                final Cookie respCookie = new Cookie(name, cookie.getValue());
                respCookie.setPath("/");
                respCookie.setMaxAge(0); // delete
                resp.addCookie(respCookie);
            }
        }
    }

    /**
     * Returns the remembered session.
     *
     * @param req The servlet request.
     * @return the The remembered session.
     */
    protected static ServerSession getSessionObject(final ServletRequest req) {
        return (ServerSession) req.getAttribute(SESSION_KEY);
    }

}
