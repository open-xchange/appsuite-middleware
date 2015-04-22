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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;

/**
 * Utility methods to handle oauth redirects.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class OAuthRedirectUtils {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OAuthRedirectUtils.class);

    private static final String COM_OPENEXCHANGE_UI_WEB_PATH = "com.openexchange.UIWebPath";

    private static final Pattern P_UIWEBPATH = Pattern.compile("[uiwebpath]", Pattern.LITERAL);

    private static final String LOGIN_PAGE = "/[uiwebpath]/ui/login";

    private static final String ERROR_PAGE = "/[uiwebpath]/ui/error";

    private OAuthRedirectUtils() {
        // avoid
    }

    public static void setUnkownErrorRedirectUrl(HttpServletRequest request, HttpServletResponse response, String message, String error) {
        StringBuilder fragment = new StringBuilder();
        if (!Strings.isEmpty(message) && !Strings.isEmpty(error)) {
            fragment.append("&message=").append(message).append("&message_type=").append("ERROR").append("&error=").append(error);
        }
        URIBuilder builder;
        try {
            builder = new URIBuilder(getErrorPageUrl(request)).setFragment(fragment.toString());
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect(builder.build().toString());
        } catch (URISyntaxException | OXException | IOException e) {
            LOG.error("Unable to create the redirect for 'unkown error'.", e);
        }
    }

    public static void setInvalidCredentialsErrorRedirectUrl(HttpServletRequest request, HttpServletResponse response, Locale locale, OXException exception) throws IOException {
        try {
            URI builder = new URIBuilder(getSecureLocation(request))
                .addParameter(OAuthProviderConstants.PARAM_RESPONSE_TYPE, request.getParameter(OAuthProviderConstants.PARAM_RESPONSE_TYPE))
                .addParameter(OAuthProviderConstants.PARAM_CLIENT_ID, request.getParameter(OAuthProviderConstants.PARAM_CLIENT_ID))
                .addParameter(OAuthProviderConstants.PARAM_REDIRECT_URI, request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI))
                .addParameter(OAuthProviderConstants.PARAM_STATE, request.getParameter(OAuthProviderConstants.PARAM_STATE))
                .addParameter(OAuthProviderConstants.PARAM_SCOPE, request.getParameter(OAuthProviderConstants.PARAM_SCOPE))
                .setFragment("error=" + exception.getDisplayMessage(locale))
                .build();
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect(builder.toASCIIString());
        } catch (URISyntaxException e) {
            setUnkownErrorRedirectUrl(request, response, "Error while building the invalid credentials error.", e.getLocalizedMessage());
        }
    }

    public static void setPermissionErrorRedirectUrl(HttpServletRequest request, HttpServletResponse response, Locale locale, String error, String errorDescription) throws OXException, IOException {
        setParameterErrorRedirectUrl(request, response, locale, error, errorDescription, null);
    }

    public static void setParameterErrorRedirectUrl(HttpServletRequest request, HttpServletResponse response, Locale locale, String error, String errorDescription, String errorParam, String... additionalParams) throws OXException, IOException {
        List<String> params = new ArrayList<>(8);
        params.add(OAuthProviderConstants.PARAM_ERROR);
        params.add(error);
        params.add(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION);
        params.add(translate(errorDescription, locale));
        params.add("error_param");
        params.add(errorParam);

        if (additionalParams != null) {
            for (String str : additionalParams) {
                params.add(str);
            }
        }
        StringBuilder url = new StringBuilder(getErrorPageUrl(request));
        String redirectLocation = getRedirectLocationWithFragment(url.toString(), params.toArray(new String[params.size()]));
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.sendRedirect(redirectLocation);
    }

    protected static String getErrorPageUrl(HttpServletRequest request) throws OXException {
        String redirectUri = request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);

        ConfigurationService configService = Services.requireService(ConfigurationService.class);
        String uiWebPath = configService.getProperty(COM_OPENEXCHANGE_UI_WEB_PATH, "/appsuite");
        String errorLink = configService.getProperty("com.openexchange.oauth.provider.baseLink", ERROR_PAGE);

        errorLink = P_UIWEBPATH.matcher(errorLink).replaceAll(Matcher.quoteReplacement(trimSlashes(uiWebPath)));

        if (redirectUri == null) {
            redirectUri = getSecureLocation(request);
        }
        errorLink = redirectUri.toString() + errorLink;

        return errorLink;
    }

    public static String getLoginPageUrl(HttpServletRequest request) throws OXException {
        String redirectUri = request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);
        ConfigurationService configService = Services.requireService(ConfigurationService.class);
        String uiWebPath = configService.getProperty(COM_OPENEXCHANGE_UI_WEB_PATH, "/appsuite");
        String loginLink = configService.getProperty("com.openexchange.oauth.provider.baseLink", LOGIN_PAGE);

        loginLink = P_UIWEBPATH.matcher(loginLink).replaceAll(Matcher.quoteReplacement(trimSlashes(uiWebPath)));

        if (redirectUri == null) {
            redirectUri = getSecureLocation(request);
        }
        loginLink = redirectUri.toString() + loginLink;

        return loginLink;
    }

    /**
     * Trims trailing and leading slashes from the supplied path.
     *
     * @param path The path
     * @return The trimmed path
     */
    private static String trimSlashes(String path) {
        String pazz = path;
        if (null != pazz && 0 < pazz.length()) {
            if ('/' == pazz.charAt(0)) {
                pazz = pazz.substring(1);
            }
            if (0 < pazz.length() && '/' == pazz.charAt(pazz.length() - 1)) {
                pazz = pazz.substring(0, pazz.length() - 1);
            }
        }
        return pazz;
    }

    /**
     * Translates the given String for to the given locale
     *
     * @param toTranslate - String to translate
     * @param locale - {@link Locale} to translate to
     * @return translated String
     * @throws OXException
     */
    private static String translate(String toTranslate, Locale locale) throws OXException {
        TranslatorFactory factory = Services.requireService(TranslatorFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(TranslatorFactory.class);
        }
        Translator translator = factory.translatorFor(locale);
        return translator.translate(toTranslate);
    }

    public static void setSuccessfullyAuthenticatedRedirect(HttpServletResponse response, String redirectLocation) throws IOException {
        response.sendRedirect(redirectLocation);
    }

    /**
     * Takes a {@link HttpServletRequest} and constructs the value for a 'Location' header
     * while forcing 'https://' as protocol.
     *
     * @param request The servlet request
     * @return The absolute location
     * @throws OXException If determining the host name fails
     */
    public static String getSecureLocation(HttpServletRequest request) {
        String hostname = getHostname(request);
        StringBuilder requestURL = new StringBuilder("https://").append(hostname).append(request.getServletPath());
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            requestURL.append(pathInfo);
        }

        String queryString = request.getQueryString();
        if (queryString != null) {
            requestURL.append('?');
            requestURL.append(queryString);
        }
        return requestURL.toString();
    }

    public static String getBaseLocation(HttpServletRequest request) {
        String hostname = getHostname(request);
        String prefix = Services.getServiceLookup().getService(DispatcherPrefixService.class).getPrefix();
        return "https://" + hostname + prefix;
    }

    public static String getHostname(HttpServletRequest request) {
        String hostname;
        HostnameService hostnameService = Services.getServiceLookup().getService(HostnameService.class);
        if (hostnameService == null) {
            hostname = request.getServerName();
        } else {
            hostname = hostnameService.getHostname(-1, -1);
        }

        return hostname;
    }

    public static String getRedirectLocationWithFragment(String redirectURI, String... additionalParams) throws UnsupportedEncodingException {
        return getRedirectLocation(redirectURI, '#', additionalParams);
    }

    public static String getRedirectLocationWithParameter(String redirectURI, String... additionalParams) throws UnsupportedEncodingException {
        return getRedirectLocation(redirectURI, '?', additionalParams);
    }

    private static String getRedirectLocation(String redirectURI, char concat, String... additionalParams) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder(redirectURI);
        if (additionalParams != null && additionalParams.length > 0) {
            if (additionalParams.length % 2 != 0) {
                throw new IllegalArgumentException("The number of additional arguments must be even!");
            }

            if (redirectURI.indexOf(concat) >= 0) {
                concat = '&';
            } else if (redirectURI.endsWith("/")) {
                builder.setLength(builder.length() - 1);
            }

            for (int i = 0; i < additionalParams.length; i++) {
                String name = additionalParams[i++];
                String value = additionalParams[i];
                if (value == null) {
                    continue;
                }
                builder.append(concat);
                builder.append(name).append('=').append(URLEncoder.encode(value, "UTF-8"));
                if (i > 0) {
                    concat = '&';
                }
            }
        }
        return builder.toString();
    }
}
