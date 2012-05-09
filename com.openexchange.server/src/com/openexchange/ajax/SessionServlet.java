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

package com.openexchange.ajax;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Queue;
import java.util.Set;
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
import com.openexchange.log.LogFactory;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.writer.ResponseWriter;
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
import com.openexchange.java.Java7ConcurrentLinkedQueue;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.SessionThreadCounter;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.sessiond.impl.SubnetMask;
import com.openexchange.sessiond.impl.ThreadLocalSessionHolder;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.CountingHttpServletRequest;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * Overridden service method that checks if a valid session can be found for the request.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class SessionServlet extends AJAXServlet {

    private static final long serialVersionUID = -8308340875362868795L;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessionServlet.class));

    private static final boolean INFO = LOG.isInfoEnabled();

    private static final boolean DEBUG = LOG.isDebugEnabled();

    public static final String SESSION_KEY = "sessionObject";
    
    public static final String PUBLIC_SESSION_KEY = "publicSessionObject";
    

    public static final String SESSION_WHITELIST_FILE = "noipcheck.cnf";

    private static final Queue<IPRange> RANGES = new Java7ConcurrentLinkedQueue<IPRange>();

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    private static volatile boolean checkIP = true;

    private static volatile ClientWhitelist clientWhitelist;

    private static volatile CookieHashSource hashSource;

    private static volatile boolean rangesLoaded;

    private static final Lock RANGE_LOCK = new ReentrantLock();

    private static SubnetMask allowedSubnet;

    protected SessionServlet() {
        super();
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        if (INITIALIZED.compareAndSet(false, true)) {
            checkIP = Boolean.parseBoolean(config.getInitParameter(ServerConfig.Property.IP_CHECK.getPropertyName()));
            hashSource = CookieHashSource.parse(config.getInitParameter(Property.COOKIE_HASH.getPropertyName()));
            clientWhitelist = new ClientWhitelist().add(config.getInitParameter(Property.IP_CHECK_WHITELIST.getPropertyName()));
            String ipMaskV4 = config.getInitParameter(ServerConfig.Property.IP_MASK_V4.getPropertyName());
            String ipMaskV6 = config.getInitParameter(ServerConfig.Property.IP_MASK_V6.getPropertyName());
            allowedSubnet = new SubnetMask(ipMaskV4, ipMaskV6);
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
                    final String[] lines = text.split("\n");
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

    protected void initializeSession(final HttpServletRequest req) throws OXException {
        if (null != getSessionObject(req, true)) {
            return;
        }
        // Remember session
        final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (sessiondService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }
        if (req.getParameter("session") != null) {
            final String sessionId = getSessionId(req);
            final ServerSession session = getSession(req, sessionId, sessiondService);
            verifySession(req, sessiondService, sessionId, session);
            rememberSession(req, session);
        }
        
        // Try public session
        final Cookie[] cookies = req.getCookies();
        
        if (cookies != null) {
            Session simpleSession = null;
        	for (final Cookie cookie : cookies) {
                if (Login.PUBLIC_SESSION_NAME.equals(cookie.getName())) {
                    simpleSession = sessiondService.getSessionByAlternativeId(cookie.getValue());
                    break;
                }
            }
        	
        	if (simpleSession != null) {
        		final ServerSession session = ServerSessionAdapter.valueOf(simpleSession);
        		verifySession(req, sessiondService, session.getSessionID(), session);
        		rememberPublicSession(req, session);
        	}
        	
        }

    }

	private void verifySession(final HttpServletRequest req,
			final SessiondService sessiondService, final String sessionId,
			final ServerSession session) throws OXException {
		if (!sessionId.equals(session.getSessionID())) {
		    if (INFO) {
		        LOG.info("Request's session identifier \"" + sessionId + "\" differs from the one indicated by SessionD service \"" + session.getSessionID() + "\".");
		    }
		    throw SessionExceptionCodes.WRONG_SESSION.create();
		}
		final Context ctx = session.getContext();
		if (!ctx.isEnabled()) {
		    sessiondService.removeSession(sessionId);
		    if (INFO) {
		        LOG.info("The context " + ctx.getContextId() + " associated with session is locked.");
		    }
		    throw SessionExceptionCodes.CONTEXT_LOCKED.create();
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
        try {
            initializeSession(req);
            final ServerSession session = getSessionObject(req, true);
            /*
             * Check max. concurrent AJAX requests
             */
            final int maxConcurrentRequests = getMaxConcurrentRequests(session);
            if (maxConcurrentRequests > 0) {
                counter = (AtomicInteger) session.getParameter(Session.PARAM_COUNTER);
                if (null != counter && counter.incrementAndGet() > maxConcurrentRequests) {
                    if (INFO) {
                        LOG.info("User " + session.getUserId() + " in context " + session.getContextId() + " exceeded max. concurrent requests (" + maxConcurrentRequests + ").");
                    }
                    throw AjaxExceptionCodes.TOO_MANY_REQUESTS.create();
                }
            }
            ThreadLocalSessionHolder.getInstance().setSession(session);
            if (null != threadCounter) {
                sessionId = session.getSessionID();
                threadCounter.increment(sessionId);
            }
            super.service(new CountingHttpServletRequest(req), resp);
        } catch (final OXException e) {
            if (SessionExceptionCodes.getErrorPrefix().equals(e.getPrefix())) {
                LOG.debug(e.getMessage(), e);
                handleSessiondException(e, req, resp);
                /*
                 * Return JSON response
                 */
                final Response response = new Response();
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
            } else {
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
            }
        } finally {
            if (null != sessionId && null != threadCounter) {
                threadCounter.decrement(sessionId);
            }
            ThreadLocalSessionHolder.getInstance().setSession(null);
            if (LogProperties.isEnabled()) {
                final Props properties = LogProperties.optLogProperties();
                if (null != properties) {
                    properties.remove("com.openexchange.session.sessionId");
                    properties.remove("com.openexchange.session.userId");
                    properties.remove("com.openexchange.session.contextId");
                    properties.remove("com.openexchange.session.clientId");
                    properties.remove("com.openexchange.session.session");
                }
            }
            if (null != counter) {
                counter.getAndDecrement();
            }
        }
    }

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
            } finally {
                if (LogProperties.isEnabled()) {
                    final Props properties = LogProperties.getLogProperties();
                    properties.remove("com.openexchange.session.sessionId");
                    properties.remove("com.openexchange.session.userId");
                    properties.remove("com.openexchange.session.contextId");
                    properties.remove("com.openexchange.session.clientId");
                    properties.remove("com.openexchange.session.session");
                }
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
    public static void checkIP(final boolean doCheck, final Queue<IPRange> ranges, final Session session, final String actual, final ClientWhitelist whitelist) throws OXException {
        if (null == actual || !actual.equals(session.getLocalIp())) {
            // IP is missing or changed
            if (doCheck && !isWhitelistedFromIPCheck(actual, ranges) && !isWhitelistedClient(session, whitelist) && !allowedSubnet.areInSameSubnet(actual, session.getLocalIp())) {
                // kick client with changed IP address
                if (INFO) {
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
            if (DEBUG && !isWhitelistedFromIPCheck(actual, ranges) && !isWhitelistedClient(session, whitelist)) {
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
     */
    private static boolean isWhitelistedClient(final Session session, final ClientWhitelist whitelist) {
        return null != whitelist && !whitelist.isEmpty() && whitelist.isAllowed(session.getClient());
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
            if (INFO) {
                final StringBuilder sb = new StringBuilder(32);
                sb.append("Parameter \"").append(PARAMETER_SESSION).append("\" not found");
                if (DEBUG) {
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
        final Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            if (INFO) {
                LOG.info("There is no session associated with session identifier: " + sessionId);
            }
            throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
        }
        if (LogProperties.isEnabled()) {
            final Props properties = LogProperties.getLogProperties();
            properties.put("com.openexchange.session.sessionId", sessionId);
            properties.put("com.openexchange.session.userId", Integer.valueOf(session.getUserId()));
            properties.put("com.openexchange.session.contextId", Integer.valueOf(session.getContextId()));
            final String client  = session.getClient();
            properties.put("com.openexchange.session.clientId", client == null ? "unknown" : client);
            properties.put("com.openexchange.session.session", session);
        }
        /*
         * Get session secret
         */
        final String secret = extractSecret(source, req, session.getHash(), session.getClient());
        if (secret == null || !session.getSecret().equals(secret)) {
            if (INFO && null != secret) {
                LOG.info("Session secret is different. Given secret \"" + secret + "\" differs from secret in session \"" + session.getSecret() + "\".");
            }
            throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
        }
        try {
            final Context context = ContextStorage.getInstance().getContext(session.getContextId());
            final User user = UserStorage.getInstance().getUser(session.getUserId(), context);
            if (!user.isMailEnabled()) {
                if (INFO) {
                    LOG.info("User " + user.getId() + " in context " + context.getContextId() + " is not activated.");
                }
                throw SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
            }
            return ServerSessionAdapter.valueOf(session, context, user);
        } catch (final OXException e) {
            if (ContextExceptionCodes.NOT_FOUND.equals(e)) {
                // An outdated session; context absent
                sessiondService.removeSession(sessionId);
                if (INFO) {
                    LOG.info("The context associated with session \"" + sessionId + "\" cannot be found. Obviously an outdated session which is invalidated now.");
                }
                throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            }
            if (UserExceptionCode.USER_NOT_FOUND.getPrefix().equals(e.getPrefix())) {
                final int code = e.getCode();
                if (UserExceptionCode.USER_NOT_FOUND.getNumber() == code || LdapExceptionCode.USER_NOT_FOUND.getNumber() == code) {
                    // An outdated session; user absent
                    sessiondService.removeSession(sessionId);
                    if (INFO) {
                        LOG.info("The user associated with session \"" + sessionId + "\" cannot be found. Obviously an outdated session which is invalidated now.");
                    }
                    throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                }
            }
            throw e;
        } catch (final UndeclaredThrowableException e) {
            throw UserExceptionCode.USER_NOT_FOUND.create(e, I(session.getUserId()), I(session.getContextId()));
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
            if (INFO) {
                LOG.info("Didn't found an appropriate Cookie for name \"" + cookieName + "\" (CookieHashSource=" + cookieHash.toString() + ") which provides the session secret.");
            }
        } else if (INFO) {
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
        final Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return;
        }
        final List<String> cookieNames = Arrays.asList(Login.SESSION_PREFIX + hash, Login.SECRET_PREFIX + hash, Login.PUBLIC_SESSION_NAME);
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
     * @param req The Servlet request
     * @return The remembered session
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
            return (ServerSession) req.getAttribute(SESSION_KEY);
        }
        if (mayUseFallbackSession) {
            return (ServerSession) req.getAttribute(PUBLIC_SESSION_KEY);
        }

        return null;
    }

}
