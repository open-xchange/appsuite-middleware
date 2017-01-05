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

package com.openexchange.ajax.requesthandler;

import static com.openexchange.tools.servlet.http.Tools.isMultipartContent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Client;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.log.LogProperties;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.server.services.SessionInspector;
import com.openexchange.servlet.StatusKnowing;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.SessionResult;
import com.openexchange.session.SessionSecretChecker;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.StatusKnowingHttpServletResponse;
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

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DispatcherServlet.class);

    private static final @NonNull Session NO_SESSION = new SessionObject(Dispatcher.class.getSimpleName() + "-Fake-Session");

    private static final @NonNull String RETRY_AFTER = "Retry-After";

    /*-
     * /!\ These must be static for our servlet container to work properly. /!\
     */

    private static final EnumSet<Name> PROPS_TO_IGNORE = EnumSet.of(LogProperties.Name.SESSION_CONTEXT_ID);

    /** The reference to the <code>Dispatcher</code> instance */
    private static final AtomicReference<Dispatcher> DISPATCHER = new AtomicReference<Dispatcher>();

    /**
     * Sets the dispatcher instance.
     *
     * @param dispatcher The dispatcher instance or <code>null</code> to remove
     */
    public static void setDispatcher(Dispatcher dispatcher) {
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

    private static final AtomicReference<List<ResponseRenderer>> RESPONSE_RENDERERS = new AtomicReference<List<ResponseRenderer>>(Collections.<ResponseRenderer> emptyList());

    /**
     * The prefix reference for dispatcher; e.g. <tt>"/ajax/"</tt> (default).
     * <p>
     * All requests starting with this prefix are directed to dispatcher framework.
     *
     * @deprecated Use {@link DispatcherPrefixService} instead! Classes of the AJAX framework (i.e.
     *             non-module-specific classes below the com.openexchange.ajax package might also
     *             use {@link Dispatchers#getPrefix()} after the framework is guaranteed to be initialized.
     *
     */
    @Deprecated
    public static String getPrefix() {
        return Dispatchers.getPrefix();
    }

    /**
     * Adds specified renderer.
     *
     * @param renderer The renderer
     */
    public static void registerRenderer(ResponseRenderer renderer) {
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
    public static synchronized void unregisterRenderer(ResponseRenderer renderer) {
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

    // -------------------------------------------------------------------------------------------------

    /**
     * The default <code>AJAXRequestDataTools</code>.
     */
    protected final AJAXRequestDataTools defaultRequestDataTools;

    /**
     * The line separator.
     */
    protected final String lineSeparator;

    /**
     * The dispatcher servlet prefix (e.g. /appsuite/api/)
     */
    protected final String prefix;


    /**
     * Initializes a new {@link DispatcherServlet}.
     */
    public DispatcherServlet(String prefix) {
        super();
        this.prefix = prefix;
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

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doService(req, resp, false);
    }

    @Override
    protected SessionResult<ServerSession> initializeSession(HttpServletRequest req, HttpServletResponse resp) throws OXException {
        ServerSession session = getSessionObject(req, true);
        if (null != session) {
            return new SessionResult<ServerSession>(Reply.CONTINUE, session);
        }

        // Require SessionD service
        SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (sessiondService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }

        // Check "session" parameter
        String sessionId = req.getParameter(PARAMETER_SESSION);
        boolean sessionParamFound = sessionId != null && sessionId.length() > 0;

        // Associated module & action pair
        Pair<String, String> pair = null;
        boolean mayOmitSession = false;

        // Check for possible session inspector chain
        if (!SessionInspector.getInstance().getChain().isEmpty()) {
            // Session inspectors available -- bypass those requests that do not require a session
            if (!sessionParamFound) {
                AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
                String module = requestDataTools.getModule(prefix, req);
                String action = requestDataTools.getAction(req);
                pair = new Pair<String, String>(module, action);
                Dispatcher dispatcher = DISPATCHER.get();
                mayOmitSession = dispatcher.mayOmitSession(module, action);
                if (mayOmitSession) {
                    return new SessionResult<ServerSession>(Reply.CONTINUE, session);
                }
            }
        }

        // Look-up & remember session
        SessionResult<ServerSession> result;
        if (sessionParamFound) {
            try {
                result = SessionUtility.getSession(req, resp, sessionId, sessiondService);
            } catch (OXException e) {
                if (!SessionExceptionCodes.WRONG_SESSION_SECRET.equals(e)) {
                    throw e;
                }
                // Got a wrong or missing secret
                String wrongSecret = e.getProperty(SessionExceptionCodes.WRONG_SESSION_SECRET.name());
                if (!"null".equals(wrongSecret)) {
                    // No information available or a differing secret
                    throw e;
                }
                // Missing secret cookie
                result = SessionUtility.getSession(SessionUtility.getHashSource(), req, resp, sessionId, sessiondService, new NoSecretCallbackChecker(DISPATCHER.get(), prefix, e, getAjaxRequestDataTools()));
            }
            if (Reply.STOP == result.getReply()) {
                return result;
            }
            session = result.getSession();
            if (null == session) {
                // Should not occur
                throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
            }
            SessionUtility.verifySession(req, sessiondService, sessionId, session);
            SessionUtility.rememberSession(req, session);
            SessionUtility.checkPublicSessionCookie(req, resp, session, sessiondService);
        }

        // Check if associated request allows no session (if no "session" parameter was found)
        boolean mayUseFallbackSession = false;
        boolean mayPerformPublicSessionAuth = false;
        if (!sessionParamFound) {
            String module, action;
            if (null == pair) {
                AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
                module = requestDataTools.getModule(prefix, req);
                action = requestDataTools.getAction(req);
            } else {
                module = pair.getFirst();
                action = pair.getSecond();
            }
            Dispatcher dispatcher = DISPATCHER.get();
            mayOmitSession = dispatcher.mayOmitSession(module, action);
            mayUseFallbackSession = dispatcher.mayUseFallbackSession(module, action);
            mayPerformPublicSessionAuth = dispatcher.mayPerformPublicSessionAuth(module, action);
        }

        // Try public session
        if (!mayOmitSession) {
            SessionUtility.findPublicSessionId(req, session, sessiondService, mayUseFallbackSession, mayPerformPublicSessionAuth);
        }

        return new SessionResult<ServerSession>(Reply.CONTINUE, session);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, false);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, false);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp, true);
    }

    /**
     * A set of those {@link OXExceptionCode} that should not be logged as <tt>ERROR</tt>, but as <tt>DEBUG</tt> only.
     */
    private static final Set<OXExceptionCode> IGNOREES = ImmutableSet.<OXExceptionCode> of(
            OXFolderExceptionCode.NOT_EXISTS,
            MailExceptionCode.MAIL_NOT_FOUND,
            MailExceptionCode.IMAGE_ATTACHMENT_NOT_FOUND,
            MailExceptionCode.ATTACHMENT_NOT_FOUND,
            MailExceptionCode.REFERENCED_MAIL_NOT_FOUND,
            MailExceptionCode.FOLDER_NOT_FOUND,
            SessionExceptionCodes.SESSION_EXPIRED,
            UploadException.UploadCode.MAX_UPLOAD_FILE_SIZE_EXCEEDED,
            UploadException.UploadCode.MAX_UPLOAD_SIZE_EXCEEDED
        );

    /**
     * A set of those {@link Category categories} that should not be logged as <tt>ERROR</tt>, but as <tt>DEBUG</tt> only.
     */
    private static final Set<Category> CAT_IGNOREES = ImmutableSet.of(Category.CATEGORY_PERMISSION_DENIED);

    /**
     * Checks if passed {@code OXException} instance should not be logged as <tt>ERROR</tt>, but as <tt>DEBUG</tt> only.
     *
     * @param e The {@code OXException} instance to check
     * @return <code>true</code> to ignore; otherwise <code>false</code> for common error handling
     */
    protected static boolean ignore(OXException e) {
        Category category = e.getCategory();
        for (Category cat : CAT_IGNOREES) {
            if (cat == category) {
                return true;
            }
        }

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
    public void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean preferStream) throws IOException {
        /*-
         * No needed because SessionServlet.service() does already perform it
         *
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        httpResponse.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        */

        // Disable caching and create wrapper
        Tools.disableCaching(httpResponse);
        HttpServletResponse httpResp = StatusKnowing.class.isInstance(httpResponse) ? httpResponse : new StatusKnowingHttpServletResponse(httpResponse);

        ServerSession session = null;
        AJAXState state = null;
        AJAXRequestResult result = null;
        Exception exc = null;
        Dispatcher dispatcher = DISPATCHER.get();
        try {
            AJAXRequestData requestData = initializeRequestData(httpRequest, httpResp, preferStream);

            // Acquire session
            session = requestData.getSession();
            if (null != session && false == session.isAnonymous()) {
                // A non-anonymous session
                enableRateLimitCheckFor(httpRequest);
            }

            // Start dispatcher processing
            state = dispatcher.begin();

            // Perform request
            result = dispatcher.perform(requestData, state, requestData.getSession());

            // Render the request's result
            if (renderResponse(requestData, result, httpRequest, httpResp)) {
                /*-
                 * A common result
                 *
                 * Check for optional exception to log...
                 */
                logException(result.getException(), LogLevel.DEBUG);
                /*
                 * ... and send response
                 */
                sendResponse(requestData, result, httpRequest, httpResp);
            }
        } catch (UploadException e) {
            exc = e;
            boolean forceJSON = AJAXRequestDataTools.parseBoolParameter(httpRequest.getParameter("force_json_response"));
            if (!forceJSON && (UploadException.UploadCode.MAX_UPLOAD_FILE_SIZE_EXCEEDED.equals(e) || UploadException.UploadCode.MAX_UPLOAD_SIZE_EXCEEDED.equals(e))) {
                // An upload failed

                if (null == session || !Client.OX6_UI.getClientId().equals(session.getClient())) {
                    httpResp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, e.getDisplayMessage(getLocaleFrom(session, Locale.US)));
                    logException(e, LogLevel.DEBUG, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                    return;
                }
            }
            Locale locale = getLocaleFrom(session, null);
            if (null != locale) {
                e.setProperty(OXExceptionConstants.PROPERTY_LOCALE, locale.toString());
            }
            handleOXException(e, httpRequest, httpResp);
        } catch (OXException e) {
            exc = e;
            Locale locale = getLocaleFrom(session, null);
            if (null != locale) {
                e.setProperty(OXExceptionConstants.PROPERTY_LOCALE, locale.toString());
            }
            handleOXException(e, httpRequest, httpResp);
        } catch (RuntimeException e) {
            exc = e;
            logException(e);
            OXException oxe = AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            Locale locale = getLocaleFrom(session, null);
            if (null != locale) {
                oxe.setProperty(OXExceptionConstants.PROPERTY_LOCALE, locale.toString());
            }
            super.handleOXException(oxe, httpRequest, httpResp, false, false);
        } finally {
            Dispatchers.signalDone(result, exc);
            if (null != state) {
                dispatcher.end(state);
            }
        }
    }

    @Override
    protected void handleOXException(OXException e, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (AjaxExceptionCodes.MISSING_PARAMETER.equals(e)) {
            // E.g. "Accept: application/json, text/javascript, ..."
            if (isJsonResponseExpected(req, true)) {
                writeJsCallbackOrHandle(e, req, resp);
                return;
            }

            sendErrorAndPage(HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), resp);
            logException(e, LogLevel.DEBUG, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (AjaxExceptionCodes.BAD_REQUEST.equals(e)) {
            sendErrorAndPage(HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), resp);
            logException(e, LogLevel.DEBUG, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (AjaxExceptionCodes.HTTP_ERROR.equals(e)) {
            Object[] logArgs = e.getLogArgs();
            Object statusMsg = logArgs.length > 1 ? logArgs[1] : null;
            int sc = ((Integer) logArgs[0]).intValue();
            sendErrorAndPage(sc, null == statusMsg ? null : statusMsg.toString(), resp);
            Throwable cause = e.getNonOXExceptionCause();
            if (null == cause) {
                logException(e, LogLevel.DEBUG, sc);
            } else {
                logException(e);
            }
            return;
        }

        // Handle other OXExceptions
        if (AjaxExceptionCodes.UNEXPECTED_ERROR.equals(e)) {
            Throwable cause = e.getCause();
            LOG.error("Unexpected error", null == cause ? e : cause);
        } else {
            // Ignore special errors
            if (ignore(e)) {
                logException(e, LogLevel.DEBUG, -1);
            } else {
                logException(e);
            }
        }

        writeJsCallbackOrHandle(e, req, resp);
    }

    private void writeJsCallbackOrHandle(OXException e, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (APIResponseRenderer.expectsJsCallback(req)) {
            writeErrorAsJsCallback(e, req, resp);
        } else {
            super.handleOXException(e, req, resp, false, false);
        }
    }

    /**
     * Checks if the result shall be rendered and written onto the servlet response.
     *
     * @param requestData The request data
     * @param result The result
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @return <code>true</code> if the result shall be written out normally. Returns <code>false</code> if the result was treated specially
     * and the response was already written.
     * @throws IOException If an error occurs while reading/writing the servlet request/response
     */
    protected boolean renderResponse(AJAXRequestData requestData, AJAXRequestResult result, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        /*
         * Check result's type
         */
        ResultType resultType = result.getType();
        switch (resultType) {
            case ETAG: {
                httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                long expires = result.getExpires();
                Tools.setETag(requestData.getETag(), expires > 0 ? expires : -1L, httpResponse);
                return false;
            }
            case HTTP_ERROR: {
                handleError(result, httpResponse);
                return false;
            }
            case DIRECT: {
                // No further processing
                return false;
            }
            default:
                break;
        }

        return true;
    }

    /**
     * Initializes the {@link AJAXRequestData} for this request. If a session is required, the according
     * {@link ServerSession} must be also initialized and set within the request data. If any precondition
     * checks fail, appropriate exceptions must be thrown (e.g. invalid session ID, unsupported action etc.).
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param preferStream
     * @return <code>true</code> to prefer passing request's body as binary data using an {@link InputStream} (typically for
     *            HTTP POST method); otherwise <code>false</code> to generate an appropriate {@link Object} from request's body
     * @throws OXException On failing precondition checks
     * @throws IOException If reading/writing the servlet request/response fails
     */
    protected AJAXRequestData initializeRequestData(HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean preferStream) throws OXException, IOException {
        AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
        String module = requestDataTools.getModule(prefix, httpRequest);
        String action = requestDataTools.getAction(httpRequest);
        ServerSession session = getSession(httpRequest, DISPATCHER.get(), module, action);
        /*
         * Parse AJAXRequestData
         */
        AJAXRequestData requestData = requestDataTools.parseRequest(httpRequest, preferStream, isMultipartContent(httpRequest), false, session, prefix, httpResponse);
        requestData.setSession(session);
        LogProperties.putSessionProperties(session);
        return requestData;
    }

    /**
     * Do "error" handling in case the response status is != 200. Like writing Retry-After header for a successful 202 response or removing
     * the default Content-Type: text/javascript we assume in {@link AJAXServlet#service()}.
     *
     * @param result The current {@link AJAXRequestResult}
     * @param httpServletResponse The current {@link HttpServletResponse}
     * @throws IOException If sending the error fails
     */
    protected void handleError(AJAXRequestResult result, HttpServletResponse httpServletResponse) throws IOException {
        int httpStatusCode = result.getHttpStatusCode();
        switch (httpStatusCode) {
            case HttpServletResponse.SC_ACCEPTED: {
                // Status code (202) indicating that a request was accepted for processing, but was not completed.
                httpServletResponse.setContentType(null);
                String retryAfter = result.getHeader(RETRY_AFTER);
                if (!Strings.isEmpty(retryAfter)) {
                    httpServletResponse.addHeader(RETRY_AFTER, retryAfter);
                }
                break;
            }
            default:
                break;
        }
        httpServletResponse.sendError(result.getHttpStatusCode());
    }

    protected void logException(@Nullable Exception e) {
        logException(e, null, -1);
    }

    protected void logException(@Nullable Exception e, @Nullable LogLevel logLevel) {
        logException(e, logLevel, -1);
    }

    protected void logException(@Nullable Exception e, @Nullable LogLevel logLevel, int statusCode) {
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

    private ServerSession getSession(HttpServletRequest httpRequest, Dispatcher dispatcher, String module, String action) throws OXException {
        ServerSession session = SessionUtility.getSessionObject(httpRequest, dispatcher.mayUseFallbackSession(module, action));
        if (session == null) {
            if (!dispatcher.mayOmitSession(module, action)) {
                if (dispatcher.mayUseFallbackSession(module, action)) {
                    // "open-xchange-public-session" allowed, but missing for associated action
                    String name = LoginServlet.getPublicSessionCookieName(httpRequest, null);
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
        UserImpl user = new UserImpl();
        user.setAttributes(new HashMap<String, String>(1));
        return new ServerSessionAdapter(NO_SESSION, new ContextImpl(-1), user);
    }

    /**
     * Sends a proper response to requesting client after request has been orderly dispatched.
     *
     * @param requestData The AJAX request data
     * @param result The AJAX request result
     * @param httpRequest The associated HTTP Servlet request
     * @param httpResponse The associated HTTP Servlet response
     * @throws IOException If an I/O error occurs
     */
    protected static void sendResponse(AJAXRequestData requestData, AJAXRequestResult result, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        List<ResponseRenderer> responseRenderers = RESPONSE_RENDERERS.get();
        Iterator<ResponseRenderer> iter = responseRenderers.iterator();
        for (int i = responseRenderers.size(); i-- > 0;) {
            ResponseRenderer renderer = iter.next();
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

    // ---------------------------------------------------------------------------------------------------------------------- //

    /**
     * Helper class.
     */
    private static final class NoSecretCallbackChecker implements SessionSecretChecker {

        private static final String PARAM_TOKEN = Session.PARAM_TOKEN;

        private final Dispatcher dispatcher;
        private final String prefix;
        private final OXException e;
        private final AJAXRequestDataTools requestDataTools;

        /**
         * Initializes a new {@link SessionSecretCheckerImplementation}.
         */
        protected NoSecretCallbackChecker(Dispatcher dispatcher, String prefix, OXException e, AJAXRequestDataTools requestDataTools) {
            super();
            this.requestDataTools = requestDataTools;
            this.prefix = prefix;
            this.dispatcher = dispatcher;
            this.e = e;
        }

        @Override
        public void checkSecret(Session session, HttpServletRequest req, String cookieHashSource) throws OXException {
            String module = requestDataTools.getModule(prefix, req);
            String action = requestDataTools.getAction(req);
            boolean noSecretCallback = dispatcher.noSecretCallback(module, action);
            if (!noSecretCallback) {
                throw e;
            }
            String paramToken = PARAM_TOKEN;
            String token = (String) session.getParameter(paramToken);
            session.setParameter(paramToken, null);
            if (null == token || !token.equals(req.getParameter(paramToken))) {
                throw e;
            }
            // Token does match for this noSecretCallback-capable request
        }
    }

}
