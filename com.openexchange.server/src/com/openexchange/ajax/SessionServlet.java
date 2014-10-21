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

import static com.google.common.net.HttpHeaders.RETRY_AFTER;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
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

    /**
     * Initializes a new {@link SessionServlet}.
     */
    protected SessionServlet() {
        super();
        SessionUtility.initialize();
    }

    /**
     * Initializes associated request's session.
     *
     * @param req The request
     * @param resp The response
     * @throws OXException If initialization fails
     */
    protected void initializeSession(final HttpServletRequest req, final HttpServletResponse resp) throws OXException {
        SessionUtility.defaultInitializeSession(req, resp);
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        Tools.disableCaching(resp);
        AtomicInteger counter = null;
        final SessionThreadCounter threadCounter = SessionThreadCounter.REFERENCE.get();
        String sessionId = null;
        try {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            initializeSession(req, resp);
            ServerSession session = SessionUtility.getSessionObject(req, true);
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
            if(e.getRetryAfter() > 0) {
                resp.setHeader(RETRY_AFTER, String.valueOf(e.getRetryAfter()));
            }
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

    protected void superService(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    /**
     * Handle specified SessionD exception.
     *
     * @param e The SessionD exception
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    protected void handleSessiondException(final OXException e, final HttpServletRequest req, final HttpServletResponse resp) {
        if (SessionUtility.isIpCheckError(e)) {
            try {
                // Drop Open-Xchange cookies
                final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                final String sessionId = SessionUtility.getSessionId(req);
                final ServerSession session = SessionUtility.getSession(req, sessionId, sessiondService);
                SessionUtility.removeOXCookies(session.getHash(), req, resp);
                SessionUtility.removeJSESSIONID(req, resp);
                sessiondService.removeSession(sessionId);
            } catch (final Exception e2) {
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
