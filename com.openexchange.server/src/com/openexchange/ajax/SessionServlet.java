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

import static com.openexchange.tools.servlet.http.Tools.getWriterFrom;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Streams;
import com.openexchange.log.LogProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.SessionResult;
import com.openexchange.session.SessionThreadCounter;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.ThreadLocalSessionHolder;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.tools.session.ServerSession;

/**
 * Overridden service method that checks if a valid session can be found for the request.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class SessionServlet extends AJAXServlet {

    private static final long serialVersionUID = -8308340875362868795L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionServlet.class);

    /** The session key */
    public static final String SESSION_KEY = "sessionObject";

    /** White-list file identifier */
    public static final String SESSION_WHITELIST_FILE = "noipcheck.cnf";

    // ------------------------------------------------------------------------------------------------------------------------------

    /** The error prefix for session-related exceptions */
    private final String sessionErrorPrefix;

    /**
     * Initializes a new {@link SessionServlet}.
     */
    protected SessionServlet() {
        super();
        SessionUtility.initialize();
        sessionErrorPrefix = SessionExceptionCodes.getErrorPrefix();
    }

    /**
     * Initializes associated request's session.
     *
     * @param req The request
     * @param resp The response
     * @throws OXException If initialization fails
     */
    protected SessionResult<ServerSession> initializeSession(final HttpServletRequest req, final HttpServletResponse resp) throws OXException {
        return SessionUtility.defaultInitializeSession(req, resp);
    }

    @Override
    protected void doService(HttpServletRequest req, HttpServletResponse resp, boolean checkRateLimit) throws ServletException, IOException {
        Tools.disableCaching(resp);
        AtomicInteger counter = null;
        final SessionThreadCounter threadCounter = SessionThreadCounter.REFERENCE.get();
        ServerSession session = null;
        String sessionId = null;
        try {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);

            // Get session result
            SessionResult<ServerSession> result = initializeSession(req, resp);
            if (Reply.STOP == result.getReply()) {
                return;
            }

            // Get associated session (may be null)
            session = result.getSession();
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
                LogProperties.putSessionProperties(session);

                /*
                 * Check max. concurrent AJAX requests
                 */
                int maxConcurrentRequests = getMaxConcurrentRequests(session);
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
                for (SessionServletInterceptor interceptor : SessionServletInterceptorRegistry.getInstance().getInterceptors()) {
                    interceptor.intercept(session, req, resp);
                }
            }

            // Invoke service() method
            super.doService(req, resp, checkRateLimit);
        } catch (RateLimitedException e) {
            e.send(resp);
        } catch (final OXException e) {
            Locale locale = getLocaleFrom(session, null);
            if (null != locale) {
                e.setProperty(OXExceptionConstants.PROPERTY_LOCALE, locale.toString());
            }
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

    protected void superService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    /**
     * Gets the locale from the user associated with given session
     *
     * @param session The session
     * @param defaultLocale The default locale
     * @return The determined locale or given <code>defaultLocale</code>
     */
    protected Locale getLocaleFrom(ServerSession session, Locale defaultLocale) {
        if (null == session) {
            return defaultLocale;
        }

        User user = session.getUser();
        return null == user ? defaultLocale : user.getLocale();
    }

    /**
     * Handle specified SessionD exception.
     *
     * @param e The SessionD exception
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    protected void handleSessiondException(OXException e, HttpServletRequest req, HttpServletResponse resp) {
        if (SessionUtility.isIpCheckError(e)) {
            try {
                // Drop Open-Xchange cookies
                SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                String sessionId = SessionUtility.getSessionId(req);
                SessionResult<ServerSession> result = SessionUtility.getSession(req, resp, sessionId, sessiondService);
                if (null != result.getSession()) {
                    SessionUtility.removeOXCookies(result.getSession(), req, resp);
                }
                SessionUtility.removeJSESSIONID(req, resp);
                sessiondService.removeSession(sessionId);
            } catch (Exception e2) {
                LOG.error("Cookies could not be removed.", e2);
            } finally {
                LogProperties.removeSessionProperties();
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
    protected void writeErrorAsJsCallback(OXException e, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        if (httpResponse.isCommitted()) {
            // Cannot do anything about it as response is already committed. Just log that OXException...
            return;
        }

        // First, try to obtain the writer
        PrintWriter writer = getWriterFrom(httpResponse);
        if (null != writer) {
            try {
                // As API response
                APIResponseRenderer.writeJsCallback(new Response().setException(e), Dispatchers.getActionFrom(httpRequest), writer, httpRequest, httpResponse);
            } catch (JSONException je) {
                LOG.error("", je);
                try {
                    httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A JSON error occurred: " + je.getMessage());
                } catch (IOException ioe) {
                    LOG.error("", ioe);
                }
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
        handleOXException(e, req, resp, true, true);
    }

    /**
     * Handles passed {@link OXException} instance.
     *
     * @param e The {@code OXException} instance
     * @param req The associated HTTP request
     * @param resp The associated HTTP response
     * @param checkUploadQuota Whether to check for an upload-quote error or not
     * @param doLog <code>true</code> to perform appropriate logging; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     */
    protected void handleOXException(OXException e, HttpServletRequest req, HttpServletResponse resp, boolean checkUploadQuota, boolean doLog) throws IOException {
        handleOXException(e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred inside the server which prevented it from fulfilling the request.", req, resp, checkUploadQuota, doLog);
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
     * @param checkUploadQuota Whether to check for an upload-quote error or not
     * @param doLog <code>true</code> to perform appropriate logging; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     */
    protected void handleOXException(OXException e, int statusCode, String reasonPhrase, HttpServletRequest req, HttpServletResponse resp, boolean checkUploadQuota, boolean doLog) throws IOException {
        if (checkUploadQuota && (UploadException.UploadCode.MAX_UPLOAD_SIZE_EXCEEDED.equals(e) || UploadException.UploadCode.MAX_UPLOAD_FILE_SIZE_EXCEEDED.equals(e))) {
            // An upload failed
            LOG.debug("", e);
            String sLoc = e.getProperty(OXExceptionConstants.PROPERTY_LOCALE);
            writeErrorPage(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, e.getDisplayMessage(null == sLoc ? Locale.US : LocaleTools.getLocale(sLoc)), resp);
        } else if (sessionErrorPrefix.equals(e.getPrefix())) {
            LOG.debug("", e);
            handleSessiondException(e, req, resp);

            // Output
            outputOXException(e, HttpServletResponse.SC_FORBIDDEN, e.getMessage(), req, resp);
        } else {
            if (doLog) {
                switch (e.getCategories().get(0).getLogLevel()) {
                    case TRACE:
                        LOG.trace("", e);
                        break;
                    case DEBUG:
                        LOG.debug("", e);
                        break;
                    case INFO:
                        LOG.info("", e);
                        break;
                    case WARNING:
                        LOG.warn("", e);
                        break;
                    case ERROR:
                        LOG.error("", e);
                        break;
                    default:
                        break;
                }
            }

            // Output
            outputOXException(e, statusCode, reasonPhrase, req, resp);
        }
    }

    private void outputOXException(OXException e, int statusCode, String reasonPhrase, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Check expected output format
        if (isJsonResponseExpected(req, true) || Dispatchers.isApiOutputExpectedFor(req)) {
            // First, try to obtain the writer
            PrintWriter writer = getWriterFrom(resp);

            // API response
            if (null != writer) {
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                resp.setHeader("Content-Disposition", "inline");
                APIResponseRenderer.writeResponse(new Response().setException(e), Dispatchers.getActionFrom(req), writer, req, resp);
            }
        } else {
            // No JSON response; either JavaScript call-back or regular HTML error (page)
            if (USM_USER_AGENT.equals(req.getHeader("User-Agent"))) {
                writeErrorAsJsCallback(e, req, resp);
            } else {
                // First, try to obtain the writer
                PrintWriter writer = getWriterFrom(resp);

                // Write error page
                if (null != writer) {
                    String desc = null == reasonPhrase ? "An error occurred inside the server which prevented it from fulfilling the request." : reasonPhrase;
                    resp.setStatus(statusCode);
                    writeErrorPage(statusCode, desc, resp);
                }
            }
        }
    }

    /**
     * Sends error page to client
     *
     * @param statusCode The HTTP status code
     * @param statusMsg The status message
     * @param httpResponse The HTTP response
     * @throws IOException If an I/O error occurs while sending the error page
     */
    public static void sendErrorAndPage(int statusCode, String statusMsg, HttpServletResponse httpResponse) throws IOException {
        // Check if HTTP response is committed
        if (httpResponse.isCommitted()) {
            // Status code and headers already written. Nothing can be done anymore...
            return;
        }

        // Try to write error page
        try {
            httpResponse.setStatus(statusCode);
            writeErrorPage(statusCode, statusMsg, httpResponse);
        } catch (Exception x) {
            // Ignore
            httpResponse.sendError(statusCode, null == statusMsg ? null : statusMsg.toString());
            flushSafe(httpResponse);
        }
    }

    private static void flushSafe(HttpServletResponse httpResponse) {
        try {
            try {
                Streams.flush(httpResponse.getWriter());
            } catch (IllegalStateException e) {
                // getOutputStream has already been called
                Streams.flush(httpResponse.getOutputStream());
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Checks if the <code>"Accept"</code> header of specified HTTP request signals to expect JSON data.
     *
     * @param request The HTTP request
     * @param interpretMissingAsTrue <code>true</code> to interpret a missing/empty <code>"Accept"</code> header as <code>true</code>; otherwise <code>false</code>
     * @return <code>true</code> if JSON data is expected; otherwise <code>false</code>
     */
    public static boolean isJsonResponseExpected(HttpServletRequest request, boolean interpretMissingAsTrue) {
        return Tools.isJsonResponseExpected(request, interpretMissingAsTrue);
    }

    /**
     * Attempts to write an error page to HTTP response.
     *
     * @param statusCode The HTTP status code
     * @param desc The error description
     * @param resp The HTTP response
     * @throws IOException If an I/O error occurs
     */
    public static void writeErrorPage(int statusCode, String desc, HttpServletResponse resp) throws IOException {
        Tools.sendErrorPage(resp, statusCode, desc);
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
    public static String getErrorPage(int sc, String msg, String desc) {
        return Tools.getErrorPage(sc, msg, desc);
    }

    // --------------------------------------------------------------------------------------------------------------------- //

    /**
     * Returns the remembered session.
     *
     * @param req The Servlet request.
     * @return The remembered session.
     */
    protected ServerSession getSessionObject(final ServletRequest req) {
        return SessionUtility.getSessionObject(req, false);
    }

    /**
     * Returns the remembered session.
     *
     * @param req The Servlet request.
     * @param mayUseFallbackSession <code>true</code> to look-up fall-back session; otherwise <code>false</code>
     * @return The remembered session
     */
    protected ServerSession getSessionObject(final ServletRequest req, final boolean mayUseFallbackSession) {
        return SessionUtility.getSessionObject(req, mayUseFallbackSession);
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

}
