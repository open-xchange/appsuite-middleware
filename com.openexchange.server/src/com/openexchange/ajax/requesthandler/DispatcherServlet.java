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

import static com.openexchange.ajax.requesthandler.Dispatcher.PREFIX;
import static com.openexchange.tools.servlet.http.Tools.isMultipartContent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogProperties;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.log.PropertiesAppendingLogWrapper;
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

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DispatcherServlet.class);

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
    protected void initializeSession(final HttpServletRequest req) throws OXException {
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
                sessionParamFound = true;
            } else {
                session = null;
                sessionParamFound = false;
            }
        }
        // Check if associated request allows no session (if no "session" parameter was found)
        boolean mayOmitSession = false;
        if (!sessionParamFound) {
            final AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
            final String module = requestDataTools.getModule(PREFIX.get(), req);
            final String action = requestDataTools.getAction(req);
            mayOmitSession = DISPATCHER.get().mayOmitSession(module, action);
        }
        // Try public session
        if (!mayOmitSession) {
            findPublicSessionId(req, session, sessiondService);
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
     * The <code>ETag</code> result type.
     */
    private static final AJAXRequestResult.ResultType ETAG = AJAXRequestResult.ResultType.ETAG;

    /**
     * The <code>direct</code> result type.
     */
    private static final AJAXRequestResult.ResultType DIRECT = AJAXRequestResult.ResultType.DIRECT;

    /**
     * Handles given HTTP request and generates an appropriate result using referred {@link AJAXActionService}.
     *
     * @param httpRequest The HTTP request to handle
     * @param httpResponse The HTTP response to write to
     * @param preferStream <code>true</code> to prefer passing request's body as binary data using an {@link InputStream} (typically for
     *            HTTP POST method); otherwise <code>false</code> to generate an appropriate {@link Object} from request's body
     * @throws IOException If an I/O error occurs
     */
    protected void handle(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, final boolean preferStream) throws IOException {
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
            if (ETAG.equals(result.getType())) {
                httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                final long expires = result.getExpires();
                Tools.setETag(requestData.getETag(), expires > 0 ? new Date(System.currentTimeMillis() + expires) : null, httpResponse);
                return;
            }
            if (DIRECT.equals(result.getType())) {
                // No further processing
                return;
            }
            /*
             * A common result
             */
            sendResponse(requestData, result, httpRequest, httpResponse);
        } catch (final OXException e) {
            if (AjaxExceptionCodes.BAD_REQUEST.equals(e)) {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
            if (AjaxExceptionCodes.HTTP_ERROR.equals(e)) {
                final Object[] logArgs = e.getLogArgs();
                final Object statusMsg = logArgs.length > 1 ? logArgs[1] : null;
                httpResponse.sendError(((Integer) logArgs[0]).intValue(), null == statusMsg ? null : statusMsg.toString());
                return;
            }
            // Handle other OXExceptions
            if (AjaxExceptionCodes.UNEXPECTED_ERROR.equals(e)) {
                LOG.error(new StringAllocator("Unexpected error: '").append(e.getMessage()).append('\'').toString(), e);
            } else if (e.isLoggable(LogLevel.ERROR)) {
                // Ignore special "folder not found" error
                if (OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                    logException(e, LogLevel.DEBUG);
                } else {
                    logException(e);
                }
            }
            final String action = httpRequest.getParameter(PARAMETER_ACTION);
            APIResponseRenderer.writeResponse(new Response().setException(e), null == action ? toUpperCase(httpRequest.getMethod()) : action, httpRequest, httpResponse);
        } catch (final RuntimeException e) {
            logException(e);
            final OXException exception = AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            final String action = httpRequest.getParameter(PARAMETER_ACTION);
            APIResponseRenderer.writeResponse(new Response().setException(exception), null == action ? toUpperCase(httpRequest.getMethod()) : action, httpRequest, httpResponse);
        } finally {
            if (null != state) {
                dispatcher.end(state);
            }
        }
    }

    private void logException(final Exception e) {
        logException(e, null);
    }

    private void logException(final Exception e, final LogLevel logLevel) {
        final String msg;
        if (LogProperties.isEnabled()) {
            final StringAllocator logBuilder = new StringAllocator(1024).append("Error processing request:").append(lineSeparator);
            if (LOG instanceof PropertiesAppendingLogWrapper) {
                final Set<Name> nonmatching = ((PropertiesAppendingLogWrapper) LOG).getPropertiesFor(com.openexchange.log.LogPropertyName.LogLevel.ERROR, LogProperties.optLogProperties());
                logBuilder.append(LogProperties.getAndPrettyPrint(nonmatching));
            } else {
                logBuilder.append(LogProperties.getAndPrettyPrint(PROPS_TO_IGNORE));
            }
            msg = logBuilder.toString();
        } else {
            msg = "Error processing request.";
        }
        if (null == logLevel) {
            LOG.error(msg, e);
        } else {
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
    }

    private ServerSession getSession(final HttpServletRequest httpRequest, final Dispatcher dispatcher, final String module, final String action) throws OXException {
        ServerSession session = getSessionObject(httpRequest, dispatcher.mayUseFallbackSession(module, action));
        if (session == null) {
            if (!dispatcher.mayOmitSession(module, action)) {
                if (dispatcher.mayUseFallbackSession(module, action)) {
                    // "open-xchange-public-session" allowed, but missing for associated action
                    throw httpRequest.getCookies() == null ? AjaxExceptionCodes.MISSING_COOKIES.create(Login.PUBLIC_SESSION_NAME) : AjaxExceptionCodes.MISSING_COOKIE.create(Login.PUBLIC_SESSION_NAME);
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
        ResponseRenderer candidate = null;
        final List<ResponseRenderer> responseRenderers = RESPONSE_RENDERERS.get();
        final Iterator<ResponseRenderer> iter = responseRenderers.iterator();
        for (int i = responseRenderers.size(); i-- > 0;) {
            final ResponseRenderer renderer = iter.next();
            if (renderer.handles(requestData, result)) {
                candidate = renderer;
                break;
            }
        }
        if (null == candidate) {
            throw new IllegalStateException("No appropriate " + ResponseRenderer.class.getSimpleName() + " for request data/result pair.");
        }
        candidate.write(requestData, result, httpRequest, httpResponse);
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
