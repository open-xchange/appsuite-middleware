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

package com.openexchange.spellcheck.servlet;

import static com.openexchange.spellcheck.services.SpellCheckServletServiceRegistry.getServiceRegistry;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.session.Session;
import com.openexchange.spellcheck.SpellCheckError;
import com.openexchange.spellcheck.SpellCheckExceptionCode;
import com.openexchange.spellcheck.SpellCheckService;
import com.openexchange.spellcheck.SpellChecker;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link SpellCheckServlet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SpellCheckServlet extends SessionServlet {

    private static final String PARAM_LANG = "lang";

    private static final String ACTION_CHECK = "check";

    private static final String ACTION_SUGGESTIONS = "suggestions";

    private static final String ACTION_ADD = "add";

    private static final String ACTION_REMOVE = "remove";

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(SpellCheckServlet.class));

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 449494568630871599L;

    /**
     * Initializes a new {@link SpellCheckServlet}
     */
    public SpellCheckServlet() {
        super();
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        /*
         * The magic spell to disable caching
         */
        Tools.disableCaching(resp);
        try {
            actionGet(req, resp);
        } catch (final OXException e) {
            LOG.error("doGet", e);
            final Response response = new Response();
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                Response.write(response, writer);
            } catch (final JSONException e1) {
                throw new ServletException(e1);
            }
            writer.flush();
        } catch (final JSONException e) {
            LOG.error("doGet", e);
            final Response response = new Response();
            response.setException(SpellCheckServletExceptionCode.JSON_ERROR.create(e, e.getMessage()));
            final PrintWriter writer = resp.getWriter();
            try {
                Response.write(response, writer);
            } catch (final JSONException e1) {
                throw new ServletException(e1);
            }
            writer.flush();
        }
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        /*
         * The magic spell to disable caching
         */
        Tools.disableCaching(resp);
        try {
            actionPut(req, resp);
        } catch (final OXException e) {
            LOG.error("doPut", e);
            final Response response = new Response();
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                Response.write(response, writer);
            } catch (final JSONException e1) {
                throw new ServletException(e1);
            }
            writer.flush();
        } catch (final JSONException e) {
            LOG.error("doPut", e);
            final Response response = new Response();
            response.setException(SpellCheckServletExceptionCode.JSON_ERROR.create(e, e.getMessage()));
            final PrintWriter writer = resp.getWriter();
            try {
                Response.write(response, writer);
            } catch (final JSONException e1) {
                throw new ServletException(e1);
            }
            writer.flush();
        }
    }

    private void actionPut(final HttpServletRequest req, final HttpServletResponse resp) throws OXException, JSONException, IOException {
        final String actionStr = checkStringParam(req, PARAMETER_ACTION);
        if (actionStr.equalsIgnoreCase(ACTION_CHECK)) {
            actionPutCheck(req, resp);
        } else if (actionStr.equalsIgnoreCase(ACTION_SUGGESTIONS)) {
            actionPutSuggestions(req, resp);
        } else if (actionStr.equalsIgnoreCase(ACTION_ADD)) {
            actionPutAdd(req, resp);
        } else if (actionStr.equalsIgnoreCase(ACTION_REMOVE)) {
            actionPutRemove(req, resp);
        } else {
            throw SpellCheckServletExceptionCode.UNSUPPORTED_PARAM.create(PARAMETER_ACTION, actionStr);
        }
    }

    private void actionGet(final HttpServletRequest req, final HttpServletResponse resp) throws OXException, JSONException, IOException {
        final String actionStr = checkStringParam(req, PARAMETER_ACTION);
        if (actionStr.equalsIgnoreCase(ACTION_LIST)) {
            actionGetList(req, resp);
        } else {
            throw SpellCheckServletExceptionCode.UNSUPPORTED_PARAM.create(PARAMETER_ACTION, actionStr);
        }
    }

    private void actionGetList(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException, IOException {
        /*
         * Some variables
         */
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        final Session session = getSessionObject(req);
        Response response;
        try {
            response = new Response(session);
        } catch (final OXException what) {
            Response.write(new Response().setException(what), resp.getWriter());
            return;
        }
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            final List<String> userWords = getServiceRegistry().getService(SpellCheckService.class).getSpellChecker(
                session.getUserId(),
                ContextStorage.getStorageContext(session.getContextId())).getUserWords();
            for (final String userWord : userWords) {
                jsonWriter.value(userWord);
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } finally {
            jsonWriter.endArray();
        }
        /*
         * Close response and flush print writer
         */
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        Response.write(response, resp.getWriter());
    }

    private void actionPutCheck(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException, IOException {
        /*
         * Some variables
         */
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        final Session session = getSessionObject(req);
        Response response;
        try {
            response = new Response(session);
        } catch (final OXException e1) {
            Response.write(new Response().setException(e1), resp.getWriter());
            return;
        }
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            /*
             * Read in parameter(s)
             */
            final Locale locale = parseLocaleString(checkStringParam(req, PARAM_LANG));
            /*
             * Do the spell check on request's body data (which is supposed to be html content)
             */
            final Set<String> misspeltWords;
            {
                final SpellChecker spellCheck = getServiceRegistry().getService(SpellCheckService.class).getSpellChecker(
                    session.getUserId(),
                    locale,
                    ContextStorage.getStorageContext(session.getContextId()));
                final SpellCheckError[] errors = spellCheck.checkSpelling(html2Document(getBody(req)));
                misspeltWords = new HashSet<String>(errors.length);
                for (final SpellCheckError error : errors) {
                    misspeltWords.add(error.getInvalidWord());
                }
            }
            for (final String misspeltWord : misspeltWords) {
                jsonWriter.value(misspeltWord);
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } finally {
            jsonWriter.endArray();
        }
        /*
         * Close response and flush print writer
         */
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        Response.write(response, resp.getWriter());
    }

    private void actionPutSuggestions(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException, IOException {
        /*
         * Some variables
         */
        final Response response = new Response();
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        final Session session = getSessionObject(req);
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            /*
             * Read in parameter(s)
             */
            final Locale locale = parseLocaleString(checkStringParam(req, PARAM_LANG));
            /*
             * Determine suggestions
             */
            final List<String> suggestions;
            {
                final SpellChecker spellCheck = getServiceRegistry().getService(SpellCheckService.class).getSpellChecker(
                    session.getUserId(),
                    locale,
                    ContextStorage.getStorageContext(session.getContextId()));
                suggestions = spellCheck.getSuggestions(getBody(req), 0);
            }
            for (final String suggestion : suggestions) {
                jsonWriter.value(suggestion);
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } finally {
            jsonWriter.endArray();
        }
        /*
         * Close response and flush print writer
         */
        response.setData(jsonWriter.getObject());
        response.setTimestamp(null);
        Response.write(response, resp.getWriter());
    }

    private void actionPutAdd(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException, IOException {
        /*
         * Some variables
         */
        final Session session = getSessionObject(req);
        Response response;
        try {
            response = new Response(session);
        } catch (final OXException e1) {
            Response.write(new Response().setException(e1), resp.getWriter());
            return;
        }
        try {
            final SpellChecker spellCheck = getServiceRegistry().getService(SpellCheckService.class).getSpellChecker(
                session.getUserId(),
                ContextStorage.getStorageContext(session.getContextId()));
            spellCheck.addWord(getBody(req));
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(JSONObject.NULL);
        response.setTimestamp(null);
        Response.write(response, resp.getWriter());
    }

    private void actionPutRemove(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException, IOException {
        /*
         * Some variables
         */
        final Response response = new Response();
        final Session session = getSessionObject(req);
        try {
            final SpellChecker spellCheck = getServiceRegistry().getService(SpellCheckService.class).getSpellChecker(
                session.getUserId(),
                ContextStorage.getStorageContext(session.getContextId()));
            spellCheck.removeWord(getBody(req));
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(JSONObject.NULL);
        response.setTimestamp(null);
        Response.write(response, resp.getWriter());
    }

    private static String checkStringParam(final HttpServletRequest req, final String paramName) throws OXException {
        final String paramVal = req.getParameter(paramName);
        if ((paramVal == null) || (paramVal.length() == 0) || "null".equals(paramVal)) {
            throw SpellCheckServletExceptionCode.MISSING_PARAM.create(paramName);
        }
        return paramVal;
    }

    private static final Pattern PAT_LOCALE = Pattern.compile("([a-z]{2})(?:_([A-Z]{2})(?:_([A-Z]{2}))?)?");

    /**
     * Parses given locale string into an instance of {@link Locale}
     *
     * @param localeStr The locale string to parse
     * @return The parsed instance of {@link Locale}
     * @throws OXException If locale string is invalid
     */
    private static Locale parseLocaleString(final String localeStr) throws OXException {
        final Matcher m = PAT_LOCALE.matcher(localeStr);
        if (!m.matches()) {
            throw SpellCheckExceptionCode.INVALID_LOCALE_STR.create(localeStr);
        }
        final String country = m.group(2);
        if (null == country) {
            return new Locale(m.group(1));
        }
        final String variant = m.group(3);
        if (null == variant) {
            return new Locale(m.group(1), country);
        }
        return new Locale(m.group(1), country, variant);
    }

    /**
     * Converts given HTML text into a {@link javax.swing.text.Document}
     *
     * @param html The HTML text
     * @return The HTML document filled with given HTML text
     * @throws IOException On any I/O error
     */
    private static Document html2Document(final String html) throws IOException {
        final Document doc = new HTMLDocument();
        try {
            new HTMLEditorKit().read(new StringReader(html), doc, 0);
        } catch (final BadLocationException e) {
            /*
             * Cannot occur
             */
            LOG.error(e.getMessage(), e);
        }
        return doc;
    }

    /**
     * <pre>
     *
     *
     * private static String optStringParam(final HttpServletRequest req, final String paramName) throws OXException {
     *     final String paramVal = req.getParameter(paramName);
     *     if (paramVal == null || paramVal.length() == 0 || &quot;null&quot;.equals(paramVal)) {
     *         return null;
     *     }
     *     return paramVal;
     * }
     * </pre>
     */
}
