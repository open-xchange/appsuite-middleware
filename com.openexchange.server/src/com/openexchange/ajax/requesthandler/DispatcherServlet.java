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
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.log.LogFactory;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObject;
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

    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(DispatcherServlet.class));
    
    private static final Session NO_SESSION = new SessionObject("");

    /*-
     * /!\ These must be static for our servlet container to work properly. /!\
     */

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

    private static final List<ResponseRenderer> RESPONSE_RENDERERS = new CopyOnWriteArrayList<ResponseRenderer>();

    /**
     * The default <code>AJAXRequestDataTools</code>.
     */
    protected final AJAXRequestDataTools defaultRequestDataTools;

    /**
     * Initializes a new {@link DispatcherServlet}.
     */
    public DispatcherServlet() {
        super();
        defaultRequestDataTools = AJAXRequestDataTools.getInstance();
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
        RESPONSE_RENDERERS.add(renderer);
    }

    /**
     * Removes specified renderer.
     *
     * @param renderer The renderer
     */
    public static void unregisterRenderer(final ResponseRenderer renderer) {
        RESPONSE_RENDERERS.remove(renderer);
    }

    /**
     * Clears all registered renderer.
     */
    public static void clearRenderer() {
        RESPONSE_RENDERERS.clear();
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

        final String action = httpRequest.getParameter(PARAMETER_ACTION);
        AJAXState state = null;
        final Dispatcher dispatcher = DISPATCHER.get();
        try {
            final AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
            final String module = requestDataTools.getModule(PREFIX.get(), httpRequest);
			final String action2 = requestDataTools.getAction(httpRequest);
			ServerSession session = getSessionObject(httpRequest, dispatcher.mayUseFallbackSession(module, action2));
            if (session == null && dispatcher.mayOmitSession(module, action2)) {
            	session = fakeSession();
            }
            if (null == session) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_SESSION);
            }
            /*
             * Parse AJAXRequestData
             */
            final AJAXRequestData requestData = requestDataTools.parseRequest(httpRequest, preferStream, isMultipartContent(httpRequest), session, PREFIX.get());
            requestData.setSession(session);
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
            /*
             * A common result
             */
            sendResponse(requestData, result, httpRequest, httpResponse);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            APIResponseRenderer.writeResponse(new Response().setException(e), action, httpRequest, httpResponse);
        } catch (final RuntimeException e) {
            LOG.error(e.getMessage(), e);
            final OXException exception = AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            APIResponseRenderer.writeResponse(new Response().setException(exception), action, httpRequest, httpResponse);
        } finally {
            if (null != state) {
                dispatcher.end(state);
            }
        }
    }

    private boolean isMultipartContent(final HttpServletRequest httpRequest) {
        return FileUploadBase.isMultipartContent(new ServletRequestContext(httpRequest));
    }

	private ServerSession fakeSession() {
		final UserImpl user = new UserImpl();
		user.setAttributes(new HashMap<String, Set<String>>());
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
        int highest = Integer.MIN_VALUE;
        ResponseRenderer candidate = null;
        for (final ResponseRenderer renderer : RESPONSE_RENDERERS) {
            if (renderer.handles(requestData, result) && highest <= renderer.getRanking()) {
                highest = renderer.getRanking();
                candidate = renderer;
            }
        }
        if (null == candidate) {
            throw new IllegalStateException("No appropriate " + ResponseRenderer.class.getSimpleName() + " for request data/result pair.");
        }
        candidate.write(requestData, result, httpRequest, httpResponse);
    }

}
