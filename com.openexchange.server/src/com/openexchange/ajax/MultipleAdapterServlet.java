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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedPushbackReader;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MultipleAdapterServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class MultipleAdapterServlet extends PermissionServlet {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MultipleAdapterServlet.class);


    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }


    protected void handle(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        Tools.disableCaching(resp);

        if (handleOverride(req, resp)) {
            return;
        }
        final ServerSession session = getSessionObject(req);
        if (null == session) {
            final OXException e = SessionExceptionCodes.WRONG_SESSION_SECRET.create();
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            return;
        }
        final User user = session.getUser();
        try {
            final String action = req.getParameter(PARAMETER_ACTION);
            final JSONObject request = toJSON(req, action);
            final MultipleHandler handler = createMultipleHandler();

            if (action == null) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create( PARAMETER_ACTION);
            }
            final Object response = handler.performRequest(action, request, session, Tools.considerSecure(req));
            final Date timestamp = handler.getTimestamp();
            writeResponseSafely(response, null == user ? localeFrom(session) : user.getLocale(), timestamp, handler.getWarnings(), resp, session);
        } catch (final OXException x) {
            writeException(x, null == user ? localeFrom(session) : user.getLocale(), resp, session);
        } catch (final Throwable t) {
            writeException(wrap(t), null == user ? localeFrom(session) : user.getLocale(), resp, session);
        }
    }

    private OXException wrap(final Throwable t) {
        return AjaxExceptionCodes.UNEXPECTED_ERROR.create(t, t.getMessage());
    }

    protected boolean handleOverride(final HttpServletRequest req, final HttpServletResponse resp) {
        return false;
    }

    private void writeResponseSafely(final Object data, final Locale locale, final Date timestamp, final Collection<OXException> warnings, final HttpServletResponse resp, Session session) {
        final Response response = new Response(locale);
        response.setData(data);
        if(null != timestamp) {
            response.setTimestamp(timestamp);
        }
        if (null != warnings && !warnings.isEmpty()) {
            response.addWarnings(warnings);
        }
        try {
            writeResponse(response, resp, session);
        } catch (final IOException e) {
            LOG.error("", e);
        }
    }

    private void writeException(final OXException e, final Locale locale, final HttpServletResponse resp, Session session) {
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
        final Response response = new Response(locale);
        response.setException(e);
        try {
            writeResponse(response, resp, session);
        } catch (final IOException ioe) {
            LOG.error("", ioe);
        }
    }

    private JSONObject toJSON(final HttpServletRequest req, final String action) throws JSONException, IOException {
        final JSONObject request = new JSONObject();
        final Enumeration parameterNames = req.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            final String parameterName = (String) parameterNames.nextElement();
            final String parameter = req.getParameter(parameterName);
            request.put(parameterName, parameter);
        }
        if(requiresBody(action)) {
            request.put(ResponseFields.DATA, toJSONConformantValue(req));
        }
        return modify(req, action, request);
    }

    protected JSONObject modify(final HttpServletRequest req, final String action, final JSONObject request) throws JSONException {
        return request;
    }

    private Object toJSONConformantValue(final HttpServletRequest req) throws JSONException, IOException {
        if (null == req) {
            return null;
        }
        UnsynchronizedPushbackReader reader = null;
        try {
            reader = new UnsynchronizedPushbackReader(AJAXServlet.getReaderFor(req));
            final int read = reader.read();
            if (read < 0) {
                return null;
            }
            final char c = (char) read;
            reader.unread(c);
            if ('[' == c || '{' == c) {
                try {
                    return JSONObject.parse(reader);
                } catch (final JSONException e) {
                    return new JSONTokener(AJAXServlet.readFrom(reader)).nextValue();
                }
            }
            return new JSONTokener(AJAXServlet.readFrom(reader)).nextValue();
        } finally {
            Streams.close(reader);
        }
    }

    private static boolean startsWith(final char startingChar, final String toCheck) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        int i = 0;
        if (Strings.isWhitespace(toCheck.charAt(i))) {
            do {
                i++;
            } while (i < len && Strings.isWhitespace(toCheck.charAt(i)));
        }
        if (i >= len) {
            return false;
        }
        return startingChar == toCheck.charAt(i);
    }

    protected abstract boolean requiresBody(String action);
    protected abstract MultipleHandler createMultipleHandler();

}
