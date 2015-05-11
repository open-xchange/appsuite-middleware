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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.servlets;

import static com.openexchange.tools.servlet.http.Tools.sendEmptyErrorResponse;
import static com.openexchange.tools.servlet.http.Tools.sendErrorResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.net.HttpHeaders;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.client.Icon;
import com.openexchange.oauth.provider.internal.URLHelper;


/**
 * {@link OAuthEndpoint} - The abstract OAuth endpoint servlet
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class OAuthEndpoint extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthEndpoint.class);

    private static final long serialVersionUID = 6538319126816587520L;

    protected static final String ATTR_OAUTH_CSRF_TOKEN = "oauth-csrf-token";

    protected final OAuthProviderService oAuthProvider;

    /**
     * Initializes a new {@link OAuthEndpoint}.
     */
    protected OAuthEndpoint(OAuthProviderService oAuthProvider) {
        super();
        this.oAuthProvider = oAuthProvider;
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
            result.put("error", error);
            result.put("error_description", errorDescription);
            sendErrorResponse(httpResponse, statusCode, result.toString());
        } catch (JSONException e) {
            LOG.error("Could not compile error response object", e);
            sendEmptyErrorResponse(httpResponse, statusCode);
        }
    }

    protected static Locale determineLocale(HttpServletRequest request) {
        Locale locale = LocaleTools.DEFAULT_LOCALE;
        String language = request.getParameter("language");
        if (language != null) {
            locale = LocaleTools.getSaneLocale(LocaleTools.getLocale(language));
        }
        return locale;
    }

    protected static String icon2HTMLDataSource(Icon icon) throws IOException {
        return "data:" + icon.getMimeType() + ";charset=UTF-8;base64," + Base64.encodeBase64String(IOUtils.toByteArray(icon.getInputStream()));
    }

    protected static boolean isInvalidCSRFToken(HttpServletRequest request, boolean remove) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        String csrfToken = (String) session.getAttribute(ATTR_OAUTH_CSRF_TOKEN);
        if (remove) {
            session.removeAttribute(ATTR_OAUTH_CSRF_TOKEN);
        }

        if (csrfToken == null) {
            return true;
        }

        String actualToken = request.getParameter(OAuthProviderConstants.PARAM_CSRF_TOKEN);
        if (actualToken == null) {
            return true;
        }

        if (!csrfToken.equals(actualToken)) {
            return true;
        }

        return false;
    }

    protected static boolean isInvalidReferer(HttpServletRequest request) throws OXException {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (Strings.isEmpty(referer)) {
            return true;
        }

        try {
            URI expectedReferer = new URI(URLHelper.getSecureLocation(request));
            URI actualReferer = new URI(referer);
            if (!expectedReferer.getScheme().equals(actualReferer.getScheme())) {
                return true;
            }

            if (!expectedReferer.getHost().equals(actualReferer.getHost())) {
                return true;
            }

            if (expectedReferer.getPort() != actualReferer.getPort()) {
                return true;
            }

            if (!expectedReferer.getPath().equals(actualReferer.getPath())) {
                return true;
            }
        } catch (URISyntaxException e) {
            return true;
        }

        return false;
    }

}
