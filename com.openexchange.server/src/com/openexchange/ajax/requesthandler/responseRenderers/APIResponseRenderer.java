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

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.UnsynchronizedStringWriter;

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
        writeResponse((Response) result.getResultObject(), request.getAction(), req, resp);
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
        try {
            if (FileUploadBase.isMultipartContent(new ServletRequestContext(req)) || isRespondWithHTML(req) || req.getParameter(CALLBACK) != null) {
                resp.setContentType(AJAXServlet.CONTENTTYPE_HTML);
                String callback = req.getParameter(CALLBACK);
                if (callback == null) {
                    callback = action;
                }
                final Writer w = new UnsynchronizedStringWriter();
                ResponseWriter.write(response, w);
                resp.getWriter().print(substituteJS(w.toString(), callback));
            } else if (req.getParameter(JSONP) != null) {
                final String call = req.getParameter(JSONP);

                final Writer w = new UnsynchronizedStringWriter();
                ResponseWriter.write(response, w);

                final StringBuilder sb = new StringBuilder(call);
                sb.append('(');
                sb.append(w.toString());
                sb.append(");");
                resp.setContentType("text/javascript");
                resp.getWriter().write(sb.toString());
            } else {
                ResponseWriter.write(response, resp.getWriter());
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

    private static boolean isRespondWithHTML(final HttpServletRequest req) {
        return Boolean.parseBoolean(req.getParameter("respondWithHTML"));
    }

    private static final String JS_FRAGMENT = AJAXServlet.JS_FRAGMENT;

    private static final Pattern RPL_JSON = Pattern.compile("**json**", Pattern.LITERAL);

    private static final Pattern RPL_ACTION = Pattern.compile("**action**", Pattern.LITERAL);

    private static String substituteJS(final String json, final String action) {
        return RPL_ACTION.matcher(RPL_JSON.matcher(JS_FRAGMENT).replaceAll(Matcher.quoteReplacement(json))).replaceAll(
            Matcher.quoteReplacement(action));
    }

}
