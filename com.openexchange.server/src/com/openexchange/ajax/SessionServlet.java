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

package com.openexchange.ajax;

import static com.openexchange.ajax.LoginServlet.getPublicSessionCookieName;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Strings.toLowerCase;
import static com.openexchange.tools.servlet.http.Cookies.extractDomainValue;
import static com.openexchange.tools.servlet.http.Cookies.getDomainValue;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.database.DatabaseService;
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
import com.openexchange.session.Session;
import com.openexchange.session.SessionSecretChecker;
import com.openexchange.session.SessionThreadCounter;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.sessiond.impl.SubnetMask;
import com.openexchange.sessiond.impl.ThreadLocalSessionHolder;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.RateLimitedException;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Overridden service method that checks if a valid session can be found for the request.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class SessionServlet extends AJAXServlet {

    private static final long serialVersionUID = -8308340875362868795L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionServlet.class);

    public static final String SESSION_KEY = "sessionObject";

    public static final String PUBLIC_SESSION_KEY = "publicSessionObject";

    public static final String SESSION_WHITELIST_FILE = "noipcheck.cnf";

    private static final List<IPRange> RANGES = new LinkedList<IPRange>();

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    private static volatile boolean checkIP = true;

    private static volatile ClientWhitelist clientWhitelist;

    protected static volatile CookieHashSource hashSource;

    private static volatile boolean rangesLoaded;

    private static final Lock RANGE_LOCK = new ReentrantLock();

    private static volatile SubnetMask allowedSubnet;

    /**
     * Initializes a new {@link SessionServlet}.
     */
    protected SessionServlet() {
        super();
        final ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null != configService && INITIALIZED.compareAndSet(false, true)) {
            checkIP = Boolean.parseBoolean(configService.getProperty(ServerConfig.Property.IP_CHECK.getPropertyName()));
            hashSource = CookieHashSource.parse(configService.getProperty(Property.COOKIE_HASH.getPropertyName()));
            clientWhitelist = new ClientWhitelist().add(configService.getProperty(Property.IP_CHECK_WHITELIST.getPropertyName()));
            final String ipMaskV4 = configService.getProperty(ServerConfig.Property.IP_MASK_V4.getPropertyName());
            final String ipMaskV6 = configService.getProperty(ServerConfig.Property.IP_MASK_V6.getPropertyName());
            allowedSubnet = new SubnetMask(ipMaskV4, ipMaskV6);
            initRanges(configService);
        }
    }

    private void initRanges(final ConfigurationService configService) {
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
                RANGE_LOCK.lock();
                try {
                    // Serialize range parsing. This might happen more than once, but shouldn't matter, since the list
                    // is accessed exclusively, so it winds up correct.
                    RANGES.clear();
                    final String[] lines = Strings.splitByCRLF(text);
                    for (String line : lines) {
                        line = line.replaceAll("\\s", "");
                        if (!line.equals("") && (line.length() == 0 || line.charAt(0) != '#')) {
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

    /**
     * Initializes associated request's session.
     *
     * @param req The request
     * @param resp The response
     * @throws OXException If initialization fails
     */
    protected void initializeSession(final HttpServletRequest req, final HttpServletResponse resp) throws OXException {
        if (null != getSessionObject(req, true)) {
            return;
        }
        // Remember session
        final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (sessiondService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }
        final ServerSession session;
        {
            final String sSession = req.getParameter(PARAMETER_SESSION);
            if (sSession != null && sSession.length() > 0) {
                final String sessionId = getSessionId(req);
                session = getSession(req, sessionId, sessiondService);
                verifySession(req, sessiondService, sessionId, session);
                rememberSession(req, session);
                checkPublicSessionCookie(req, resp, session, sessiondService);
            } else {
                session = null;
            }
        }
        // Try public session
        findPublicSessionId(req, session, sessiondService, false, false);
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
     * @throws OXException If public session cannot be created
     */
    protected void findPublicSessionId(final HttpServletRequest req, final ServerSession session, final SessiondService sessiondService, final boolean mayUseFallbackSession, final boolean mayPerformPublicSessionAuth) throws OXException {
        final Map<String, Cookie> cookies = Cookies.cookieMapFor(req);
        if (cookies != null) {
            final Cookie cookie = cookies.get(getPublicSessionCookieName(req));
            if (null != cookie) {
                handlePublicSessionCookie(req, session, sessiondService, cookie.getValue(), false);
            } else {
                final String publicSessionId = req.getParameter(PARAMETER_PUBLIC_SESSION);
                if (null != publicSessionId) {
                    handlePublicSessionCookie(req, session, sessiondService, publicSessionId, mayPerformPublicSessionAuth);
                } else {
                    if (mayUseFallbackSession && isMediaPlayerAgent(req.getHeader(USER_AGENT))) {
                        for (final Map.Entry<String, Cookie> entry : cookies.entrySet()) {
                            if (entry.getKey().startsWith(PUBLIC_SESSION_PREFIX)) {
                                handlePublicSessionCookie(req, session, sessiondService, entry.getValue().getValue(), false);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handlePublicSessionCookie(final HttpServletRequest req, final ServerSession session, final SessiondService sessiondService, final String altId, final boolean publicSessionAuth) throws OXException {
        if (null != altId && null != session && altId.equals(session.getParameter(PARAM_ALTERNATIVE_ID))) {
            // same session (thus already verified)
            rememberPublicSession(req, session);
        } else {
            // Lookup session by alternative id
            final ServerSession publicSession = null == altId ? null : ServerSessionAdapter.valueOf(sessiondService.getSessionByAlternativeId(altId));
            if (publicSession != null) {
                try {
                    if (false == publicSessionAuth) {
                        checkSecret(hashSource, req, publicSession, false);
                    }
                    verifySession(req, sessiondService, publicSession.getSessionID(), publicSession);
                    rememberPublicSession(req, publicSession);
                } catch (final OXException e) {
                    // Verification of public session failed
                }
            }
        }
    }

    /**
     * Verifies given session.
     *
     * @param req The HTTP request
     * @param sessiondService The service
     * @param sessionId The session identifier
     * @param session The session
     * @throws OXException If verification fails
     */
    protected void verifySession(final HttpServletRequest req, final SessiondService sessiondService, final String sessionId, final ServerSession session) throws OXException {
        if (!sessionId.equals(session.getSessionID())) {
            LOG.info("Request's session identifier \"{}\" differs from the one indicated by SessionD service \"{}\".", sessionId, session.getSessionID());
            throw SessionExceptionCodes.WRONG_SESSION.create();
        }
        final Context ctx = session.getContext();
        if (!ctx.isEnabled()) {
            sessiondService.removeSession(sessionId);
            LOG.info("The context {} associated with session is locked.", Integer.toString(ctx.getContextId()));
            throw SessionExceptionCodes.CONTEXT_LOCKED.create(Integer.toString(ctx.getContextId()), ctx.getName());
        }
        checkIP(session, req.getRemoteAddr());
    }

    /**
     * Checks the session ID supplied as a query parameter in the request URI.
     */
    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        Tools.disableCaching(resp);
        AtomicInteger counter = null;
        final SessionThreadCounter threadCounter = SessionThreadCounter.REFERENCE.get();
        String sessionId = null;
        ServerSession session = null;
        try {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            initializeSession(req, resp);
            session = getSessionObject(req, true);
            if (null != session) {
                /*
                 * Track DB schema
                 */
                String dbSchema = (String) session.getParameter(LogProperties.Name.DATABASE_SCHEMA.getName());
                if (dbSchema == null) {
                    DatabaseService dbService = ServerServiceRegistry.getServize(DatabaseService.class, true);
                    dbSchema = dbService.getSchemaName(session.getContextId());
                    session.setParameter(LogProperties.Name.DATABASE_SCHEMA.getName(), dbSchema);
                }
                LogProperties.put(LogProperties.Name.DATABASE_SCHEMA, dbSchema);

                /*
                 * Check max. concurrent AJAX requests
                 */
                final int maxConcurrentRequests = getMaxConcurrentRequests(session);
                if (maxConcurrentRequests > 0) {
                    counter = (AtomicInteger) session.getParameter(Session.PARAM_COUNTER);
                    if (null != counter && counter.incrementAndGet() > maxConcurrentRequests) {
                        LOG.info("User {} in context {} exceeded max. concurrent requests ({}).", session.getUserId(), session.getContextId(), maxConcurrentRequests);
                        throw AjaxExceptionCodes.TOO_MANY_REQUESTS.create();
                    }
                }
                ThreadLocalSessionHolder.getInstance().setSession(session);
                if (null != threadCounter) {
                    sessionId = session.getSessionID();
                    threadCounter.increment(sessionId);
                }
            }
            super.service(req, resp);
        } catch (final RateLimitedException e) {
            resp.setContentType("text/plain; charset=UTF-8");
            resp.sendError(429, "Too Many Requests - Your request is being rate limited.");
        } catch (final OXException e) {
            handleOXException(e, req, resp);
        } finally {
            if (null != sessionId && null != threadCounter) {
                threadCounter.decrement(sessionId);
            }
            ThreadLocalSessionHolder.getInstance().clear();
            LogProperties.removeSessionProperties();
            LogProperties.removeProperty(LogProperties.Name.DATABASE_SCHEMA);
            if (null != counter) {
                counter.getAndDecrement();
            }
        }
    }

    /**
     * Writes common JavaScript call-back for given error.
     *
     * @param e The error
     * @param httpRequest The HTTP request
     * @param httpResponse The HTTP response
     * @throws IOException If an I/O error occurs
     */
    protected void writeErrorAsJsCallback(final OXException e, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) throws IOException {
        try {
            // As API response
            APIResponseRenderer.writeJsCallback(new Response().setException(e), Dispatchers.getActionFrom(httpRequest), httpRequest, httpResponse);
        } catch (JSONException je) {
            LOG.error("", e);
            try {
                httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A JSON error occurred: " + e.getMessage());
            } catch (final IOException ioe) {
                LOG.error("", ioe);
            }
        }
    }

    /**
     * Handles passed {@link OXException} instance.
     *
     * @param e The {@code OXException} instance
     * @param req The associated HTTP request
     * @param resp The associated HTTP response
     * @throws IOException If an I/O error occurs
     */
    protected void handleOXException(OXException e, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleOXException(e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred inside the server which prevented it from fulfilling the request.", req, resp);
    }

    private static final String USM_USER_AGENT = "Open-Xchange USM HTTP Client";

    /**
     * Handles passed {@link OXException} instance.
     *
     * @param e The {@code OXException} instance
     * @param statusCode The HTTP status code
     * @param reasonPhrase The HTTP reason phrase
     * @param req The associated HTTP request
     * @param resp The associated HTTP response
     * @throws IOException If an I/O error occurs
     */
    protected void handleOXException(OXException e, int statusCode, String reasonPhrase, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (SessionExceptionCodes.getErrorPrefix().equals(e.getPrefix())) {
            LOG.debug("", e);
            handleSessiondException(e, req, resp);

            // Check expected output format
            if (Dispatchers.isApiOutputExpectedFor(req)) {
                // API response
                APIResponseRenderer.writeResponse(new Response().setException(e), Dispatchers.getActionFrom(req), req, resp);
            } else {
                // No JSON response; either JavaScript call-back or regular HTML error (page)
                if (USM_USER_AGENT.equals(req.getHeader("User-Agent"))) {
                    writeErrorAsJsCallback(e, req, resp);
                } else {
                    String desc = e.getMessage();
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    writeErrorPage(HttpServletResponse.SC_FORBIDDEN, desc, resp);
                }
            }
        } else {
            e.log(LOG);

            // Check expected output format
            if (Dispatchers.isApiOutputExpectedFor(req)) {
                // API response
                APIResponseRenderer.writeResponse(new Response().setException(e), Dispatchers.getActionFrom(req), req, resp);
            } else {
                // No JSON response; either JavaScript call-back or regular HTML error (page)
                if (USM_USER_AGENT.equals(req.getHeader("User-Agent"))) {
                    writeErrorAsJsCallback(e, req, resp);
                } else {
                    String desc = null == reasonPhrase ? "An error occurred inside the server which prevented it from fulfilling the request." : reasonPhrase;
                    resp.setStatus(statusCode);
                    writeErrorPage(statusCode, desc, resp);
                }
            }
        }
    }

    /**
     * Attempts to write an error page to HTTP response.
     *
     * @param statusCode The HTTP status code
     * @param desc The error description
     * @param resp The HTTP response
     * @throws IOException If an I/O error occurs
     */
    protected void writeErrorPage(int statusCode, String desc, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setHeader("Content-Disposition", "inline");
        PrintWriter writer = resp.getWriter();
        writer.write(getErrorPage(statusCode, null, desc));
        writer.flush();
    }

    /**
     * Generates a simple error page for given status code.
     *
     * @param sc The status code; e.g. <code>404</code>
     * @return A simple error page
     */
    protected String getErrorPage(int sc) {
        return getErrorPage(sc, null, null);
    }

    /**
     * Generates a simple error page for given arguments.
     *
     * @param sc The status code; e.g. <code>404</code>
     * @param msg The optional status message; e.g. <code>"Not Found"</code>
     * @param desc The optional status description; e.g. <code>"The requested URL was not found on this server."</code>
     * @return A simple error page
     */
    protected String getErrorPage(int sc, String msg, String desc) {
        String msg0 = null == msg ? HttpStatus.getStatusText(sc) : msg;

        StringBuilder sb = new StringBuilder(512);
        String lineSep = System.getProperty("line.separator");
        sb.append("<!DOCTYPE html>").append(lineSep);
        sb.append("<html><head>").append(lineSep);
        {
            sb.append("<title>").append(sc);
            if (null != msg0) {
                sb.append(' ').append(msg0);
            }
            sb.append("</title>").append(lineSep);
        }

        sb.append("</head><body>").append(lineSep);

        sb.append("<h1>");
        if (null == msg0) {
            sb.append(sc);
        } else {
            sb.append(msg0);
        }
        sb.append("</h1>").append(lineSep);

        String desc0 = null == desc ? msg0 : desc;
        if (null != desc0) {
            sb.append("<p>").append(desc0).append("</p>").append(lineSep);
        }

        sb.append("</body></html>").append(lineSep);
        return sb.toString();
    }

    // --------------------------------------------------------------------------------------------------------------------- //

    private static volatile Integer maxConcurrentRequests;

    private static int getMaxConcurrentRequests(final ServerSession session) {
        Integer tmp = maxConcurrentRequests;
        if (null == tmp) {
            synchronized (SessionServlet.class) {
                tmp = maxConcurrentRequests;
                if (null == tmp) {
                    tmp = maxConcurrentRequests = Integer.valueOf(getMaxConcurrentRequests0(session));
                }
            }
        }
        return tmp.intValue();
    }

    private static int getMaxConcurrentRequests0(final ServerSession session) {
    	if (session == null) {
    		return 0;
    	}
        final Set<String> set = session.getUser().getAttributes().get("ajax.maxCount");
        if (null == set || set.isEmpty()) {
            try {
                return ServerConfig.getInt(ServerConfig.Property.DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS);
            } catch (final OXException e) {
                return Integer.parseInt(ServerConfig.Property.DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS.getDefaultValue());
            }
        }
        try {
            return Integer.parseInt(set.iterator().next());
        } catch (final NumberFormatException e) {
            try {
                return ServerConfig.getInt(ServerConfig.Property.DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS);
            } catch (final OXException oxe) {
                return Integer.parseInt(ServerConfig.Property.DEFAULT_MAX_CONCURRENT_AJAX_REQUESTS.getDefaultValue());
            }
        }
    }

    protected void superService(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    private void checkIP(final Session session, final String actual) throws OXException {
        checkIP(checkIP, getRanges(), session, actual, clientWhitelist);
    }

    private List<IPRange> getRanges() {
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
            } finally {
                LogProperties.removeSessionProperties();
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
     * @param doCheck <code>true</code> to deny request with an exception.
     * @param ranges The white-list ranges
     * @param session session object
     * @param actual IP address of the current request.
     * @param whitelist The optional IP check whitelist (by client identifier)
     * @throws OXException if the IP addresses don't match.
     */
    public static void checkIP(final boolean doCheck, final List<IPRange> ranges, final Session session, final String actual, final ClientWhitelist whitelist) throws OXException {
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

    public static boolean isWhitelistedFromIPCheck(final String actual, final List<IPRange> ranges) {
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
     * @param source defines how the cookie should be found
     * @param sessionId identifier of the session.
     * @param sessiondService The SessionD service
     * @return the session.
     * @throws SessionException if the session can not be found.
     */
    public static ServerSession getSession(final CookieHashSource source, final HttpServletRequest req, final String sessionId, final SessiondService sessiondService) throws OXException {
        return getSession(source, req, sessionId, sessiondService, null);
    }

    /**
     * Finds appropriate local session.
     *
     * @param source defines how the cookie should be found
     * @param sessionId identifier of the session.
     * @param sessiondService The SessionD service
     * @return the session.
     * @throws SessionException if the session can not be found.
     */
    public static ServerSession getSession(final CookieHashSource source, final HttpServletRequest req, final String sessionId, final SessiondService sessiondService, final SessionSecretChecker optChecker) throws OXException {
        final Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            if (!"unset".equals(sessionId)) {
                LOG.info("There is no session associated with session identifier: {}", sessionId);
            }
            throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
        }
        LogProperties.putSessionProperties(session);
        /*
         * Get session secret
         */
        if (null == optChecker) {
            checkSecret(source, req, session);
        } else {
            optChecker.checkSecret(session, req, source.name());
        }
        try {
            final User user = UserStorage.getInstance().getUser(session.getUserId(), ContextStorage.getInstance().getContext(session.getContextId()));
            if (!user.isMailEnabled()) {
                LOG.info("User {} in context {} is not activated.", Integer.toString(user.getId()), Integer.toString(session.getContextId()));
                throw SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
            }
            return ServerSessionAdapter.valueOf(session);
        } catch (final OXException e) {
            if (ContextExceptionCodes.NOT_FOUND.equals(e)) {
                // An outdated session; context absent
                sessiondService.removeSession(sessionId);
                LOG.info("The context associated with session \"{}\" cannot be found. Obviously an outdated session which is invalidated now.", sessionId);
                throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            }
            if (UserExceptionCode.USER_NOT_FOUND.getPrefix().equals(e.getPrefix())) {
                final int code = e.getCode();
                if (UserExceptionCode.USER_NOT_FOUND.getNumber() == code || LdapExceptionCode.USER_NOT_FOUND.getNumber() == code) {
                    // An outdated session; user absent
                    sessiondService.removeSession(sessionId);
                    LOG.info("The user associated with session \"{}\" cannot be found. Obviously an outdated session which is invalidated now.", sessionId);
                    throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                }
            }
            throw e;
        } catch (final UndeclaredThrowableException e) {
            throw UserExceptionCode.USER_NOT_FOUND.create(e, I(session.getUserId()), I(session.getContextId()));
        }
    }

    private static final Set<String> AGENTS_WO_PUBLIC_SESSION_COOKIE = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("open-xchange usm http client")));

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
            final String cookieName = getPublicSessionCookieName(req);
            if (null == cookies.get(cookieName)) {
                final boolean restored = LoginServlet.writePublicSessionCookie(req, resp, session, req.isSecure(), req.getServerName(), LoginServlet.getLoginConfiguration());
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
        final String secret = extractSecret(source, req, session.getHash(), session.getClient(), (String) session.getParameter("user-agent"));
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
        final Map<String, Cookie> cookies = Cookies.cookieMapFor(req);
        if (null != cookies) {
            if (cookies.isEmpty()) {
                LOG.info("Empty Cookies in HTTP request. No session secret can be looked up.");
            } else {
                final String secretPrefix = SECRET_PREFIX;
                final StringBuilder tmp = new StringBuilder(256);
                final String expectedSecretCookieName = tmp.append(secretPrefix).append(getHash(hashSource, req, hash, client)).toString();

                // Look-up Cookie by expected name
                Cookie cookie = cookies.get(expectedSecretCookieName);
                if (null != cookie) {
                    return cookie.getValue();
                }

                // Check for special User-Agent to allow look-up by remembered cookie name
                if (isMediaPlayerAgent(req.getHeader(USER_AGENT))) {
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
                    LOG.info("Didn't find an appropriate Cookie for expected name \"{}\" (CookieHashSource={}) which provides the session secret. Remembered hash: {}. Available hashes: {}", expectedSecretCookieName, hashSource.toString(), hash, tmp.toString());
                } else {
                    LOG.info("Didn't find an appropriate Cookie for expected name \"{}\" (CookieHashSource={}) which provides the session secret. Remembered hash={}. No available hashes.", expectedSecretCookieName, hashSource.toString(), hash);
                }
            }
        } else {
            LOG.info("Missing Cookies in HTTP request. No session secret can be looked up.");
        }
        return null;
    }

    private static final Set<String> MEDIA_AGENTS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("applecoremedia/", "stagefright/")));

    private static boolean isMediaPlayerAgent(final String userAgent) {
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

    /**
     * Gets the appropriate hash for specified request.
     *
     * @param hashSource Specifies how the cookie should be found.
     * @param req The HTTP request
     * @param hash The previously remembered hash
     * @param client The client identifier
     * @return The appropriate hash
     */
    public static String getHash(final CookieHashSource hashSource, final HttpServletRequest req, final String hash, final String client) {
        if (CookieHashSource.REMEMBER == hashSource) {
            return hash;
        }
        // Default is calculate
        return HashCalculator.getInstance().getHash(req, client);
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
     * @param hash The hash string identifying appropriate cookie
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    public static void removeOXCookies(final String hash, final HttpServletRequest req, final HttpServletResponse resp) {
        removeOXCookies(req, resp, Arrays.asList(LoginServlet.SESSION_PREFIX + hash, SECRET_PREFIX + hash, getPublicSessionCookieName(req)));
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
                final String value = cookie.getValue();
                final Cookie respCookie = new Cookie(cookieName, value);
                respCookie.setPath("/");
                final String domain = getDomainValue(req.getServerName());
                if (null != domain) {
                    respCookie.setDomain(domain);
                    // Once again without domain parameter
                    final Cookie respCookie2 = new Cookie(cookieName, value);
                    respCookie2.setPath("/");
                    respCookie2.setMaxAge(0); // delete
                    resp.addCookie(respCookie2);
                }
                respCookie.setMaxAge(0); // delete
                resp.addCookie(respCookie);
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
    }

    /**
     * Returns the remembered session.
     *
     * @param req The Servlet request.
     * @return The remembered session.
     */
    protected static ServerSession getSessionObject(final ServletRequest req) {
        return getSessionObject(req, false);
    }

    /**
     * Returns the remembered session.
     *
     * @param req The Servlet request.
     * @param mayUseFallbackSession <code>true</code> to look-up fall-back session; otherwise <code>false</code>
     * @return The remembered session
     */
    protected static ServerSession getSessionObject(final ServletRequest req, final boolean mayUseFallbackSession) {
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
                LogProperties.put(LogProperties.Name.SERVLET_QUERY_STRING, queryString);
            }
        }
        return null;
    }

}
