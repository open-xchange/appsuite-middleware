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

package com.openexchange.ajax.requesthandler;

import static com.google.common.net.HttpHeaders.RETRY_AFTER;
import static com.openexchange.ajax.requesthandler.Dispatcher.PREFIX;
import static com.openexchange.tools.servlet.http.Tools.isMultipartContent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.SessionSecretChecker;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link DispatcherServlet} - The main dispatcher servlet which delegates request to dispatcher framework.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DispatcherServlet extends SessionServlet {

    private static final long serialVersionUID = -8060034833311074781L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DispatcherServlet.class);

    private static final Session NO_SESSION = new SessionObject(Dispatcher.class.getSimpleName() + "-Fake-Session");

    /*-
     * /!\ These must be static for our servlet container to work properly. /!\
     */

    private static final EnumSet<Name> PROPS_TO_IGNORE = EnumSet.of(LogProperties.Name.SESSION_CONTEXT_ID);

    private static final AtomicReference<Dispatcher> DISPATCHER = new AtomicReference<Dispatcher>();

    /**
     * Sets the dispatcher instance.
     *
     * @param dispatcher The dispatcher instance or <code>null</code> to remove
     */
    public static void setDispatcher(final Dispatcher dispatcher) {
        DISPATCHER.set(dispatcher);
    }

    /**
     * Gets the dispatcher instance.
     *
     * @return The dispatcher instance or <code>null</code> if absent
     */
    public static Dispatcher getDispatcher() {
        return DISPATCHER.get();
    }

    /**
     * Sets the prefix.
     *
     * @param prefix The prefix or <code>null</code> to remove
     */
    public static void setPrefix(final String prefix) {
        PREFIX.set(prefix);
    }

    /**
     * Gets the prefix.
     *
     * @return The prefix or <code>null</code> if absent
     */
    public static String getPrefix() {
        return PREFIX.get();
    }

    private static final AtomicReference<List<ResponseRenderer>> RESPONSE_RENDERERS = new AtomicReference<List<ResponseRenderer>>(Collections.<ResponseRenderer> emptyList());

    /**
     * The default <code>AJAXRequestDataTools</code>.
     */
    protected final AJAXRequestDataTools defaultRequestDataTools;

    /**
     * The line separator.
     */
    protected final String lineSeparator;

    /**
     * Initializes a new {@link DispatcherServlet}.
     */
    public DispatcherServlet() {
        super();
        defaultRequestDataTools = AJAXRequestDataTools.getInstance();
        lineSeparator = System.getProperty("line.separator");
    }

    /**
     * Gets the <code>AJAXRequestDataTools</code> instance to use for parsing incoming requests.
     *
     * @return The <code>AJAXRequestDataTools</code> instance
     */
    protected AJAXRequestDataTools getAjaxRequestDataTools() {
        return defaultRequestDataTools;
    }

    /**
     * Adds specified renderer.
     *
     * @param renderer The renderer
     */
    public static void registerRenderer(final ResponseRenderer renderer) {
        List<ResponseRenderer> expect;
        List<ResponseRenderer> update;
        do {
            expect = RESPONSE_RENDERERS.get();
            update = new ArrayList<ResponseRenderer>(expect);
            update.add(renderer);
            Collections.sort(update, new Comparator<ResponseRenderer>() {

                @Override
                public int compare(ResponseRenderer responseRenderer, ResponseRenderer anotherResponseRenderer) {
                    // Higher ranked first
                    int thisRanking = responseRenderer.getRanking();
                    int anotherRanking = anotherResponseRenderer.getRanking();
                    return (thisRanking<anotherRanking ? 1 : (thisRanking==anotherRanking ? 0 : -1));
                }
            });
        } while (!RESPONSE_RENDERERS.compareAndSet(expect, update));
    }

    /**
     * Removes specified renderer.
     *
     * @param renderer The renderer
     */
    public static synchronized void unregisterRenderer(final ResponseRenderer renderer) {
        List<ResponseRenderer> expect;
        List<ResponseRenderer> update;
        do {
            expect = RESPONSE_RENDERERS.get();
            update = new ArrayList<ResponseRenderer>(expect);
            update.remove(renderer);
            Collections.sort(update, new Comparator<ResponseRenderer>() {

                @Override
                public int compare(ResponseRenderer responseRenderer, ResponseRenderer anotherResponseRenderer) {
                    // Higher ranked first
                    int thisRanking = responseRenderer.getRanking();
                    int anotherRanking = anotherResponseRenderer.getRanking();
                    return (thisRanking<anotherRanking ? 1 : (thisRanking==anotherRanking ? 0 : -1));
                }
            });
        } while (!RESPONSE_RENDERERS.compareAndSet(expect, update));
    }

    /**
     * Clears all registered renderer.
     */
    public static void clearRenderer() {
        RESPONSE_RENDERERS.set(Collections.<ResponseRenderer> emptyList());
    }

    @Override
    protected void initializeSession(final HttpServletRequest req, final HttpServletResponse resp) throws OXException {
        if (null != getSessionObject(req, true)) {
            return;
        }
        // Remember session
        final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (sessiondService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }
        ServerSession session;
        final boolean sessionParamFound;
        {
            final String sessionId = req.getParameter(PARAMETER_SESSION);
            if (sessionId != null && sessionId.length() > 0) {
                try {
                    session = getSession(req, sessionId, sessiondService);
                } catch (final OXException e) {
                    if (!SessionExceptionCodes.WRONG_SESSION_SECRET.equals(e)) {
                        throw e;
                    }
                    // Got a wrong or missing secret
                    final String wrongSecret = e.getProperty(SessionExceptionCodes.WRONG_SESSION_SECRET.name());
                    if (!"null".equals(wrongSecret)) {
                        // No information available or a differing secret
                        throw e;
                    }
                    // Missing secret cookie
                    session = getSession(hashSource, req, sessionId, sessiondService, new NoSecretCallbackChecker(DISPATCHER.get(), e, getAjaxRequestDataTools()));
                }
                verifySession(req, sessiondService, sessionId, session);
                rememberSession(req, session);
                checkPublicSessionCookie(req, resp, session, sessiondService);
                sessionParamFound = true;
            } else {
                session = null;
                sessionParamFound = false;
            }
        }
        // Check if associated request allows no session (if no "session" parameter was found)
        boolean mayOmitSession = false;
        boolean mayUseFallbackSession = false;
        if (!sessionParamFound) {
            final AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
            final String module = requestDataTools.getModule(PREFIX.get(), req);
            final String action = requestDataTools.getAction(req);
            final Dispatcher dispatcher = DISPATCHER.get();
            mayOmitSession = dispatcher.mayOmitSession(module, action);
            mayUseFallbackSession = dispatcher.mayUseFallbackSession(module, action);
        }
        // Try public session
        if (!mayOmitSession) {
            findPublicSessionId(req, session, sessiondService, mayUseFallbackSession);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, false);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, false);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, true);
    }

    /**
     * A set of those {@link OXExceptionCode} that should not be logged as <tt>ERROR</tt>, but as <tt>DEBUG</tt> only.
     */
    private static final Set<OXExceptionCode> IGNOREES = Collections.unmodifiableSet(new HashSet<OXExceptionCode>(Arrays.<OXExceptionCode> asList(OXFolderExceptionCode.NOT_EXISTS, MailExceptionCode.MAIL_NOT_FOUND)));

    /**
     * Checks if passed {@code OXException} instance should not be logged as <tt>ERROR</tt>, but as <tt>DEBUG</tt> only.
     *
     * @param e The {@code OXException} instance to check
     * @return <code>true</code> to ignore; otherwise <code>false</code> for common error handling
     */
    private static boolean ignore(OXException e) {
        for (OXExceptionCode code : IGNOREES) {
            if (code.equals(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles given HTTP request and generates an appropriate result using referred {@link AJAXActionService}.
     *
     * @param httpRequest The HTTP request to handle
     * @param httpResponse The HTTP response to write to
     * @param preferStream <code>true</code> to prefer passing request's body as binary data using an {@link InputStream} (typically for
     *            HTTP POST method); otherwise <code>false</code> to generate an appropriate {@link Object} from request's body
     * @throws IOException If an I/O error occurs
     */
    public void handle(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final boolean preferStream) throws IOException {
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        httpResponse.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        Tools.disableCaching(httpResponse);

        AJAXState state = null;
        final Dispatcher dispatcher = DISPATCHER.get();
        try {
            final AJAXRequestData requestData;
            final ServerSession session;
            /*
             * Parse & acquire session
             */
            {
                final AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
                final String module = requestDataTools.getModule(PREFIX.get(), httpRequest);
                final String action = requestDataTools.getAction(httpRequest);
                session = getSession(httpRequest, dispatcher, module, action);
                /*
                 * Parse AJAXRequestData
                 */
                requestData = requestDataTools.parseRequest(httpRequest, preferStream, isMultipartContent(httpRequest), session, PREFIX.get(), httpResponse);
                requestData.setSession(session);
            }
            /*
             * Start dispatcher processing
             */
            state = dispatcher.begin();
            /*
             * Perform request
             */
            final AJAXRequestResult result = dispatcher.perform(requestData, state, session);
            /*
             * Check result's type
             */
            if (ResultType.ETAG.equals(result.getType())) {
                httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                final long expires = result.getExpires();
                Tools.setETag(requestData.getETag(), expires > 0 ?  expires : -1L, httpResponse);
                return;
            }

            if (ResultType.HTTP_ERROR.equals(result.getType())) {
                handleError(result, httpResponse);
                return;
            }

            if (ResultType.DIRECT.equals(result.getType())) {
                // No further processing
                return;
            }
            /*-
             * A common result
             *
             * Check for optional exception to log...
             */
            logException(result.getException(), LogLevel.DEBUG);
            /*
             * ... and send response
             */
            sendResponse(requestData, result, httpRequest, httpResponse);
        } catch (final OXException e) {
            if (AjaxExceptionCodes.MISSING_PARAMETER.equals(e)) {
                sendErrorAndPage(HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), httpResponse);
                logException(e, LogLevel.DEBUG, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (AjaxExceptionCodes.BAD_REQUEST.equals(e)) {
                sendErrorAndPage(HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), httpResponse);
                logException(e, LogLevel.DEBUG, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (AjaxExceptionCodes.HTTP_ERROR.equals(e)) {
                Object[] logArgs = e.getLogArgs();
                Object statusMsg = logArgs.length > 1 ? logArgs[1] : null;
                int sc = ((Integer) logArgs[0]).intValue();
                sendErrorAndPage(sc, null == statusMsg ? null : statusMsg.toString(), httpResponse);
                logException(e, LogLevel.DEBUG, sc);
                return;
            }

            // Handle other OXExceptions

            if (AjaxExceptionCodes.UNEXPECTED_ERROR.equals(e)) {
                Throwable cause = e.getCause();
                LOG.error("Unexpected error", null == cause ? e : cause);
            } else {
                // Ignore special "folder not found" error
                if (ignore(e)) {
                    logException(e, LogLevel.DEBUG, -1);
                } else {
                    logException(e);
                }
            }

            if (APIResponseRenderer.expectsJsCallback(httpRequest)) {
                writeErrorAsJsCallback(e, httpRequest, httpResponse);
            } else {
                handleOXException(e, httpRequest, httpResponse);
            }
        } catch (final RuntimeException e) {
            logException(e);
            handleOXException(AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()), httpRequest, httpResponse);
        } finally {
            if (null != state) {
                dispatcher.end(state);
            }
        }
    }

    /**
     * Do "error" handling in case the response status is != 200. Like writing Retry-After header for a successful 202 response or removing
     * the default Content-Type: text/javascript we assume in {@link AJAXServlet#service()}.
     *
     * @param result The current {@link AJAXRequestResult}
     * @param httpServletResponse The current {@link HttpServletResponse}
     * @throws IOException If sending the error fails
     */
    private void handleError(AJAXRequestResult result, HttpServletResponse httpServletResponse) throws IOException {
        int httpStatusCode = result.getHttpStatusCode();
        switch (httpStatusCode) {
        case 202: {
            httpServletResponse.setContentType(null);
            String retry_after = result.getHeader(RETRY_AFTER);
            if (!Strings.isEmpty(retry_after)) {
                httpServletResponse.addHeader(RETRY_AFTER, retry_after);
            }
        }
        default:
            break;
        }
        httpServletResponse.sendError(result.getHttpStatusCode());
    }

    private void sendErrorAndPage(int statusCode, String statusMsg, HttpServletResponse httpResponse) throws IOException {
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

    private void logException(Exception e) {
        logException(e, null, -1);
    }

    private void logException(Exception e, LogLevel logLevel) {
        logException(e, logLevel, -1);
    }

    private void logException(Exception e, LogLevel logLevel, int statusCode) {
        if (null == e) {
            return;
        }

        String msg = statusCode > 0 ? new StringBuilder("Error processing request. Signaling HTTP error ").append(statusCode).toString() : "Error processing request.";

        if (null == logLevel) {
            LOG.error(msg, e);
            return;
        }

        switch (logLevel) {
        case TRACE:
            LOG.trace(msg, e);
            break;
        case DEBUG:
            LOG.debug(msg, e);
            break;
        case INFO:
            LOG.info(msg, e);
            break;
        case WARNING:
            LOG.warn(msg, e);
            break;
        case ERROR:
            // fall-through
        default:
            LOG.error(msg, e);
        }
    }

    private ServerSession getSession(final HttpServletRequest httpRequest, final Dispatcher dispatcher, final String module, final String action) throws OXException {
        ServerSession session = getSessionObject(httpRequest, dispatcher.mayUseFallbackSession(module, action));
        if (session == null) {
            if (!dispatcher.mayOmitSession(module, action)) {
                if (dispatcher.mayUseFallbackSession(module, action)) {
                    // "open-xchange-public-session" allowed, but missing for associated action
                    final String name = LoginServlet.getPublicSessionCookieName(httpRequest);
                    throw httpRequest.getCookies() == null ? AjaxExceptionCodes.MISSING_COOKIES.create(name) : AjaxExceptionCodes.MISSING_COOKIE.create(name);
                }
                // "open-xchange-public-session" NOT allowed for associated action, therefore complain about missing "session" parameter
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_SESSION);
            }
            session = fakeSession();
        }
        return session;
    }

    private ServerSession fakeSession() {
        final UserImpl user = new UserImpl();
        user.setAttributes(new HashMap<String, Set<String>>(1));
        return new ServerSessionAdapter(NO_SESSION, new ContextImpl(-1), user);
    }

    /**
     * Sends a proper response to requesting client after request has been orderly dispatched.
     *
     * @param requestData The AJAX request data
     * @param result The AJAX request result
     * @param httpRequest The associated HTTP Servlet request
     * @param httpResponse The associated HTTP Servlet response
     */
    protected void sendResponse(final AJAXRequestData requestData, final AJAXRequestResult result, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) {
        final List<ResponseRenderer> responseRenderers = RESPONSE_RENDERERS.get();
        final Iterator<ResponseRenderer> iter = responseRenderers.iterator();
        for (int i = responseRenderers.size(); i-- > 0;) {
            final ResponseRenderer renderer = iter.next();
            if (renderer.handles(requestData, result)) {
                renderer.write(requestData, result, httpRequest, httpResponse);
                return;
            }
        }
        // None found
        throw new IllegalStateException("No appropriate " + ResponseRenderer.class.getSimpleName() + " for request data/result pair.");
    }

    private void flushSafe(HttpServletResponse httpResponse) {
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

    /** ASCII-wise to upper-case */
    private String toUpperCase(CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
        }
        return builder.toString();
    }

    /**
     * Helper class.
     */
    private static final class NoSecretCallbackChecker implements SessionSecretChecker {

        private static final String PARAM_TOKEN = Session.PARAM_TOKEN;

        private final Dispatcher dispatcher;
        private final OXException e;
        private final AJAXRequestDataTools requestDataTools;

        /**
         * Initializes a new {@link SessionSecretCheckerImplementation}.
         */
        protected NoSecretCallbackChecker(final Dispatcher dispatcher, final OXException e, final AJAXRequestDataTools requestDataTools) {
            super();
            this.requestDataTools = requestDataTools;
            this.dispatcher = dispatcher;
            this.e = e;
        }

        @Override
        public void checkSecret(final Session session, final HttpServletRequest req, final String cookieHashSource) throws OXException {
            final String module = requestDataTools.getModule(PREFIX.get(), req);
            final String action = requestDataTools.getAction(req);
            final boolean noSecretCallback = dispatcher.noSecretCallback(module, action);
            if (!noSecretCallback) {
                throw e;
            }
            final String paramToken = PARAM_TOKEN;
            final String token = (String) session.getParameter(paramToken);
            session.setParameter(paramToken, null);
            if (null == token || !token.equals(req.getParameter(paramToken))) {
                throw e;
            }
            // Token does match for this noSecretCallback-capable request
        }
    }

}
