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

package com.openexchange.oauth.provider.impl.servlets;

import static com.openexchange.osgi.Tools.requireService;
import static com.openexchange.tools.servlet.http.Tools.sendEmptyErrorResponse;
import static com.openexchange.tools.servlet.http.Tools.sendErrorResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.impl.OAuthProviderConstants;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.OXTemplateExceptionHandler;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link OAuthEndpoint} - The abstract OAuth endpoint servlet
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class OAuthEndpoint extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthEndpoint.class);

    private static final long serialVersionUID = 6538319126816587520L;

    protected static final String ATTR_OAUTH_CSRF_TOKEN = "oauth-csrf-token";

    protected final ClientManagement clientManagement;

    protected final GrantManagement grantManagement;

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link OAuthEndpoint}.
     */
    protected OAuthEndpoint(ClientManagement clientManagement, GrantManagement grantManagement, ServiceLookup services) {
        super();
        this.clientManagement = clientManagement;
        this.grantManagement = grantManagement;
        this.services = services;
    }

    /**
     * Responds with a HTML error page.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param statusCode The HTTP status code
     * @param message The detailed error message to guide client developers
     * @throws IOException
     */
    protected void respondWithErrorPage(HttpServletRequest request, HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Content-Disposition", "inline");
        response.setStatus(statusCode);
        try {
            PrintWriter writer = response.getWriter();
            writeErrorPage(writer, message, determineLocale(request));
            writer.flush();
        } catch (OXException e) {
            LOG.error("Could not write error page", e);
            Tools.sendErrorPage(response, HttpServletResponse.SC_OK, message);
        }
    }

    /**
     * Responds with a HTML error page. The HTTP status code will be <code>500</code>.
     * The passed exceptions ID is returned as part of the detailed error description.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param e The OXException causing the error response
     * @throws IOException
     */
    protected void respondWithErrorPage(HttpServletRequest request, HttpServletResponse response, OXException e) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Content-Disposition", "inline");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String message = "An internal error occurred. If you want to contact our support because of this, please provide this error code: " + e.getErrorCode() + "(" + e.getExceptionId() + ").";
        try {
            PrintWriter writer = response.getWriter();
            writeErrorPage(writer, message, determineLocale(request));
            writer.flush();
        } catch (OXException oxe) {
            LOG.error("Could not write error page", oxe);
            Tools.sendErrorPage(response, HttpServletResponse.SC_OK, message);
        }
    }

    protected static void failWithMissingParameter(HttpServletResponse httpResponse, String param) throws IOException {
        fail(httpResponse, HttpServletResponse.SC_BAD_REQUEST, "invalid_request", "missing required parameter: " + param);
    }

    protected static void failWithInvalidParameter(HttpServletResponse httpResponse, String param) throws IOException {
        fail(httpResponse, HttpServletResponse.SC_BAD_REQUEST, "invalid_request", "invalid parameter value: " + param);
    }

    protected static void fail(HttpServletResponse httpResponse, int statusCode, String error, String errorDescription) throws IOException {
        try {
            JSONObject result = new JSONObject();
            result.put(OAuthProviderConstants.PARAM_ERROR, error);
            result.put(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION, errorDescription);
            sendErrorResponse(httpResponse, statusCode, result.toString());
        } catch (JSONException e) {
            LOG.error("Could not compile error response object", e);
            sendEmptyErrorResponse(httpResponse, statusCode);
        }
    }

    protected static Locale determineLocale(HttpServletRequest request) {
        Locale locale = LocaleTools.DEFAULT_LOCALE;
        String language = request.getParameter(OAuthProviderConstants.PARAM_LANGUAGE);
        if (language != null) {
            locale = LocaleTools.getSaneLocale(LocaleTools.getLocale(language));
        }
        if (Strings.isNotEmpty(request.getHeader("Accept-Language"))) {
            return request.getLocale();
        }
        return locale;
    }

    private void writeErrorPage(Writer writer, String message, Locale locale) throws OXException, IOException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        TemplateService templateService = requireService(TemplateService.class, services);
        Translator translator = translatorFactory.translatorFor(locale);

        Map<String, Object> vars = new HashMap<>();
        vars.put("lang", locale.getLanguage());
        vars.put("title", translator.translate(OAuthProviderStrings.ERROR_PAGE_TITLE));
        vars.put("headline", translator.translate(OAuthProviderStrings.ERROR_HEADLINE));
        vars.put("message", translator.translate(OAuthProviderStrings.ERROR_MESSAGE));
        vars.put("detailsSummary", translator.translate(OAuthProviderStrings.ERROR_DETAILS_SUMMARY));
        vars.put("detailsText", message);
        vars.put("close", translator.translate(OAuthProviderStrings.CLOSE));

        OXTemplate loginPage = templateService.loadTemplate("oauth-provider-error.tmpl", OXTemplateExceptionHandler.RETHROW_HANDLER);
        loginPage.process(vars, writer);
    }

}
