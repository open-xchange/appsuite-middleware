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

package com.openexchange.ajax.requesthandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.responseRenderers.JSONResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DispatcherServlet} - The main dispatcher servlet which delegates request to dispatcher framework.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DispatcherServlet extends SessionServlet {

    private static final long serialVersionUID = -8060034833311074781L;

    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(DispatcherServlet.class));

    private static final class HTTPRequestInputStreamProvider implements AJAXRequestData.InputStreamProvider {

        private final HttpServletRequest req;

        protected HTTPRequestInputStreamProvider(final HttpServletRequest req) {
            this.req = req;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return req.getInputStream();
        }
    }

    private static final AtomicReference<Dispatcher> DISPATCHER = new AtomicReference<Dispatcher>();

    private static final AtomicReference<String> PREFIX = new AtomicReference<String>();;

    private static final List<ResponseRenderer> RESPONSE_RENDERERS = new CopyOnWriteArrayList<ResponseRenderer>();

    /**
     * Initializes a new {@link DispatcherServlet}.
     *
     * @param dispatcher The dispatcher
     * @param prefix The prefix
     */
    public DispatcherServlet(final Dispatcher dispatcher, final String prefix) {
        if (null == dispatcher) {
            throw new NullPointerException("Dispatcher is null.");
        }
        // These must be static for our servlet container to work properly.
        DispatcherServlet.DISPATCHER.set(dispatcher);
        DispatcherServlet.PREFIX.set(prefix);
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
     * Handles given HTTP request and generates an appropriate result using referred {@link AJAXActionService}.
     *
     * @param req The HTTP request to handle
     * @param resp The HTTP response to write to
     * @param preferStream <code>true</code> to prefer passing request's body as binary data using an {@link InputStream} (typically for
     *            HTTP POST method); otherwise <code>false</code> to generate an appropriate {@link Object} from request's body
     * @throws IOException If an I/O error occurs
     */
    protected void handle(final HttpServletRequest req, final HttpServletResponse resp, final boolean preferStream) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        Tools.disableCaching(resp);

        final String action = req.getParameter(PARAMETER_ACTION);
        AJAXState state = null;
        final Dispatcher dispatcher = DISPATCHER.get();
        try {
            final ServerSession session = getSessionObject(req);
            /*
             * Parse AJAXRequestData
             */
            final AJAXRequestData request = parseRequest(req, preferStream, FileUploadBase.isMultipartContent(new ServletRequestContext(req)), session);
            /*
             * Start dispatcher processing
             */
            state = dispatcher.begin();
            /*
             * Perform request
             */
            final AJAXRequestResult result = dispatcher.perform(request, state, session);
            /*
             * Check result's type
             */
            if (AJAXRequestResult.ResultType.ETAG.equals(result.getType())) {
                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            /*
             * A common result
             */
            sendResponse(request, result, req, resp);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            JSONResponseRenderer.writeResponse(new Response().setException(e), action, req, resp);
        } catch (final RuntimeException e) {
            LOG.error(e.getMessage(), e);
            final OXException exception = AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            JSONResponseRenderer.writeResponse(new Response().setException(exception), action, req, resp);
        } finally {
            if (null != state) {
                dispatcher.end(state);
            }
        }
    }

    private void sendResponse(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest hReq, final HttpServletResponse hResp) {
        int highest = Integer.MIN_VALUE;
        ResponseRenderer candidate = null;
        for (final ResponseRenderer renderer : RESPONSE_RENDERERS) {
            if (renderer.handles(request, result) && highest <= renderer.getRanking()) {
                highest = renderer.getRanking();
                candidate = renderer;
            }
        }
        if (null == candidate) {
            throw new IllegalStateException("No appropriate " + ResponseRenderer.class.getSimpleName() + " for request data/result pair.");
        }
        candidate.write(request, result, hReq, hResp);
    }

    protected static AJAXRequestData parseRequest(final HttpServletRequest req, final boolean preferStream, final boolean isFileUpload, final ServerSession session) throws IOException, OXException {
        final AJAXRequestData retval = new AJAXRequestData();
        retval.setSecure(Tools.considerSecure(req));
        {
            final HostnameService hostnameService = ServerServiceRegistry.getInstance().getService(HostnameService.class);
            if (null == hostnameService) {
                retval.setHostname(req.getServerName());
            } else {
                final String hn = hostnameService.getHostname(session.getUserId(), session.getContextId());
                retval.setHostname(null == hn ? req.getServerName() : hn);
            }
        }
        retval.setRoute(Tools.getRoute(req.getSession(true).getId()));
        /*
         * Set the module
         */
        final String pathInfo = req.getRequestURI();
        retval.setModule(pathInfo.substring(PREFIX.get().length()));
        /*
         * Set request URI
         */
        retval.setServletRequestURI(AJAXServlet.getServletSpecificURI(req));
        /*
         * Set the action
         */
        {
            final String action = req.getParameter("action");
            if (null == action) {
                retval.setAction(req.getMethod().toUpperCase(Locale.US));
            } else {
                retval.setAction(action);
            }
        }
        /*
         * Set the format
         */
        retval.setFormat(req.getParameter("format"));
        /*
         * Pass all parameters to AJAX request object
         */
        {
            @SuppressWarnings("unchecked") final Set<Entry<String, String[]>> entrySet = req.getParameterMap().entrySet();
            for (final Entry<String, String[]> entry : entrySet) {
                retval.putParameter(entry.getKey(), entry.getValue()[0]);
            }
        }
        /*
         * Check for ETag header to support client caching
         */
        {
            final String eTag = req.getHeader("If-None-Match");
            if (null != eTag) {
                retval.setETag(eTag);
            }
        }
        /*
         * Set request body
         */
        if (isFileUpload) {
            final UploadEvent upload = processUploadStatic(req);
            final Iterator<UploadFile> iterator = upload.getUploadFilesIterator();
            while (iterator.hasNext()) {
                retval.addFile(iterator.next());
            }
            final Iterator<String> names = upload.getFormFieldNames();
            while (names.hasNext()) {
                final String name = names.next();
                retval.putParameter(name, upload.getFormField(name));
            }
            retval.setUploadEvent(upload);
        } else if (preferStream) {
            /*
             * Pass request's stream
             */
            retval.setUploadStreamProvider(new HTTPRequestInputStreamProvider(req));
        } else {
            /*
             * Guess an appropriate body object
             */
            final String body = AJAXServlet.getBody(req);
            if (startsWith('{', body, true)) {
                /*
                 * Expect the body to be a JSON object
                 */
                try {
                    retval.setData(new JSONObject(body));
                } catch (final JSONException e) {
                    retval.setData(body);
                }
            } else if (startsWith('[', body, true)) {
                /*
                 * Expect the body to be a JSON array
                 */
                try {
                    retval.setData(new JSONArray(body));
                } catch (final JSONException e) {
                    retval.setData(body);
                }
            } else {
                retval.setData(0 == body.length() ? null : body);
            }
        }
        return retval;
    }

    private static boolean startsWith(final char startingChar, final String toCheck, final boolean ignoreHeadingWhitespaces) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        if (!ignoreHeadingWhitespaces) {
            return startingChar == toCheck.charAt(0);
        }
        int i = 0;
        if (Character.isWhitespace(toCheck.charAt(i))) {
            do {
                i++;
            } while (i < len && Character.isWhitespace(toCheck.charAt(i)));
        }
        if (i >= len) {
            return false;
        }
        return startingChar == toCheck.charAt(i);
    }

}
