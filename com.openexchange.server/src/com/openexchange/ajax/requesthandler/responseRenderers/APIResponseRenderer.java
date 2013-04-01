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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static com.openexchange.ajax.requesthandler.AJAXRequestDataTools.parseBoolParameter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.java.Strings;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link APIResponseRenderer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class APIResponseRenderer implements ResponseRenderer {

    /**
     * The logger constant.
     */
    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(APIResponseRenderer.class));

    private static final String JSONP = "jsonp";

    private static final String CALLBACK = "callback";

    private static final String PLAIN_JSON = "plainJson";

    private static final String INCLUDE_STACK_TRACE_ON_ERROR = "includeStackTraceOnError";

    /**
     * Initializes a new {@link APIResponseRenderer}.
     */
    public APIResponseRenderer() {
        super();
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public boolean handles(final AJAXRequestData request, final AJAXRequestResult result) {
        return result.getResultObject() instanceof Response;
    }

    @Override
    public void write(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest req, final HttpServletResponse resp) {
        final Boolean plainJson = (Boolean) result.getParameter(PLAIN_JSON);
        final Response response = (Response) result.getResultObject();
        if (parseBoolParameter(INCLUDE_STACK_TRACE_ON_ERROR, request) ) {
            response.setIncludeStackTraceOnError(true);
        }
        writeResponse(response, request.getAction(), req, resp, null == plainJson ? false : plainJson.booleanValue());
    }

    private static final String SESSION_KEY = SessionServlet.SESSION_KEY;

    /**
     * Returns the remembered session.
     *
     * @param req The Servlet request.
     * @return The remembered session
     */
    protected static ServerSession getSession(final ServletRequest req) {
        final Object attribute = req.getAttribute(SESSION_KEY);
        if (attribute != null) {
            return (ServerSession) req.getAttribute(SESSION_KEY);
        }
        return null;
    }

    /**
     * Gets the locale for given HTTP request
     *
     * @param req The request
     * @return The locale
     */
    protected static Locale localeFrom(final HttpServletRequest req) {
        return localeFrom(getSession(req));
    }

    /**
     * Gets the locale for given server session
     *
     * @param session The server session
     * @return The locale
     */
    protected static Locale localeFrom(final ServerSession session) {
        if (null == session) {
            return Locale.US;
        }
        return session.getUser().getLocale();
    }

    /**
     * Write specified response to Servlet output stream either as HTML callback or as JSON data.
     * <p>
     * The response is considered as HTML callback if one of these conditions is met:
     * <ul>
     * <li>The HTTP Servlet request indicates <i>multipart/*</i> content type</li>
     * <li>The HTTP Servlet request has the <code>"respondWithHTML"</code> parameter set to <code>"true"</code></li>
     * <li>The HTTP Servlet request contains non-<code>null</code> <code>"callback"</code> parameter</li>
     * </ul>
     *
     * @param response The response to write
     * @param action The request's action
     * @param req The HTTP Servlet request
     * @param resp The HTTP Servlet response
     */
    public static void writeResponse(final Response response, final String action, final HttpServletRequest req, final HttpServletResponse resp) {
        writeResponse(response, action, req, resp, false);
    }

    /*-
     *      <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
     *      <html>
     *       <head>
     *        <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
     *        <script type="text/javascript">
     *          (parent.callback_**action** || window.opener && window.opener.callback_**action**)(**json**)
     *        </script>
     *       </head>
     *      </html>
     *
     */

    private static final char[] JS_FRAGMENT_PART1 = ("<!DOCTYPE HTML PUBLIC "
        + "\"-//W3C//DTD HTML 4.01//EN\" "
        + "\"http://www.w3.org/TR/html4/strict.dtd\"><html><head>"
        + "<META http-equiv=\"Content-Type\" "
        + "content=\"text/html; charset=UTF-8\">"
        + "<script type=\"text/javascript\">"
        + "(parent.callback_").toCharArray();

    private static final char[] JS_FRAGMENT_PART2 = " || window.opener && window.opener.callback_".toCharArray();

    private static final char[] JS_FRAGMENT_PART3 = ")</script></head></html>".toCharArray();

    private static void writeResponse(final Response response, final String action, final HttpServletRequest req, final HttpServletResponse resp, final boolean plainJson) {
        try {
            if (plainJson) {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(req));
            } else if (isMultipartContent(req) || isRespondWithHTML(req) || req.getParameter(CALLBACK) != null) {
                resp.setContentType(AJAXServlet.CONTENTTYPE_HTML);
                String callback = req.getParameter(CALLBACK);
                if (callback == null) {
                    callback = action;
                }
                // Write: PART1 + <action> + PART2 + <action> + ")(" + <json> + PART3
                final PrintWriter writer = resp.getWriter();
                writer.write(JS_FRAGMENT_PART1);
                writer.write(callback);
                writer.write(JS_FRAGMENT_PART2);
                writer.write(callback);
                writer.write(")(");
                ResponseWriter.write(response, new EscapingWriter(writer), localeFrom(req));
                writer.write(JS_FRAGMENT_PART3);
                /*-
                 * Previous code:
                 *
                final Writer w = new AllocatingStringWriter();
                ResponseWriter.write(response, w, localeFrom(getSession(req)));
                resp.getWriter().print(substituteJS(w.toString(), callback));
                 *
                 */
            } else if (req.getParameter(JSONP) != null) {
                resp.setContentType("text/javascript");
                final String call = req.getParameter(JSONP);
                // Write: <call> + "(" + <json> + ")"
                final PrintWriter writer = resp.getWriter();
                writer.write(call);
                writer.write('(');
                ResponseWriter.write(response, writer, localeFrom(req));
                writer.write(')');
            } else {
                ResponseWriter.write(response, resp.getWriter(), localeFrom(req));
            }
        } catch (final JSONException e) {
            LOG.error(e.getMessage(), e);
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A JSON error occurred: " + e.getMessage());
            } catch (final IOException ioe) {
                LOG.error(ioe.getMessage(), ioe);
            }
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Part of HTTP content type header.
     */
    private static final String MULTIPART = "multipart/";

    /**
     * Utility method that determines whether the request contains multipart content
     *
     * @param request The request to be evaluated.
     * @return <code>true</code> if the request is multipart; <code>false</code> otherwise.
     */
    private static final boolean isMultipartContent(final HttpServletRequest request) {
        final String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        if (contentType.toLowerCase().startsWith(MULTIPART)) {
            return true;
        }
        return false;
    }

    private static boolean isRespondWithHTML(final HttpServletRequest req) {
        return Boolean.parseBoolean(req.getParameter("respondWithHTML"));
    }

    private static final String JS_FRAGMENT = AJAXServlet.JS_FRAGMENT;

    private static final Pattern RPL_JSON = Pattern.compile("**json**", Pattern.LITERAL);

    private static final Pattern RPL_ACTION = Pattern.compile("**action**", Pattern.LITERAL);

    private static String substituteJS(final String json, final String action) {
        return RPL_ACTION.matcher(RPL_JSON.matcher(JS_FRAGMENT).replaceAll(Strings.quoteReplacement(json.replaceAll(Pattern.quote("</") , "<\\/")))).replaceAll(
            Strings.quoteReplacement(action));
    }

    /**
     * Escapes <tt>"&lt;/"</tt> char sequence to <tt>"&lt;\/"</tt>.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class EscapingWriter extends Writer {

        private int prev;
        private final Writer writer;

        protected EscapingWriter(final Writer writer) {
            super();
            this.writer = writer;
            prev = 0;
        }

        @Override
        public void write(final int c) throws IOException {
            if ('<' == c) {
                prev = c;
            } else if ('/' == c) {
                if (prev > 0) {
                    //  </   -->   <\/
                    writer.write("<\\/");
                    prev = 0;
                } else {
                    writer.write(c);
                }
            } else {
                if (prev > 0) {
                    writer.write('<');
                    prev = 0;
                }
                writer.write(c);
            }
        }

        @Override
        public void write(final char[] cbuf) throws IOException {
            write(cbuf, 0, cbuf.length);
        }

        @Override
        public void write(final char[] cbuf, final int off, final int len) throws IOException {
            for (int i = off, end = off + len; i < end; i++) {
                write(cbuf[i]);
            }
        }

        @Override
        public void write(final String str) throws IOException {
            write(str, 0, str.length());
        }

        @Override
        public void write(final String str, final int off, final int len) throws IOException {
            for (int i = off, end = off + len; i < end; i++) {
                write(str.charAt(i));
            }
        }

        @Override
        public Writer append(final CharSequence csq) throws IOException {
            if (csq == null) {
                write("null");
            } else {
                write(csq.toString());
            }
            return this;
        }

        @Override
        public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
            CharSequence cs = (csq == null ? "null" : csq);
            write(cs.subSequence(start, end).toString());
            return this;
        }

        @Override
        public Writer append(final char c) throws IOException {
            write(c);
            return this;
        }

        @Override
        public void flush() throws IOException {
            if ('<' == prev) {
                writer.write('<');
                prev = '\0';
            }
            writer.flush();
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }

        @Override
        public String toString() {
            return writer.toString();
        }

    }
}
