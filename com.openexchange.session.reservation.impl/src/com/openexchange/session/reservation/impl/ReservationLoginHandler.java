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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.session.reservation.impl;

import static com.openexchange.ajax.AJAXServlet.CONTENTTYPE_HTML;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER;
import static com.openexchange.ajax.AJAXServlet.PARAMETER_USER_ID;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.Cookie;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.ConfigurationProperty;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.reservation.Reservation;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.tools.io.IOTools;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.UserService;


/**
 * {@link ReservationLoginHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ReservationLoginHandler implements LoginRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationLoginHandler.class);

    private final SessionReservationService sessionReservationService;

    private final ServiceLookup services;

    private LoginConfiguration loginConfiguration;

    public ReservationLoginHandler(SessionReservationService sessionReservationService, ServiceLookup services) throws OXException {
        super();
        this.sessionReservationService = sessionReservationService;
        this.services = services;
        this.loginConfiguration = initLoginConfiguration();
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Tools.disableCaching(resp);
        try {
            ContextService contextService = requireService(ContextService.class, services);
            UserService userService = requireService(UserService.class, services);

            String token = req.getParameter("token");
            if (token == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Reservation reservation = sessionReservationService.getReservation(token);
            if (reservation == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Context context = contextService.getContext(reservation.getContextId());
            User user = userService.getUser(reservation.getUserId(), context);

            LoginResult result = login(req, context, user, reservation.getState(), loginConfiguration);
            Session session = result.getSession();
            LoginServlet.writeSecretCookie(req, resp, session, session.getHash(), req.isSecure(), req.getServerName(), loginConfiguration);
            LoginServlet.addHeadersAndCookies(result, resp);
            resp.sendRedirect(generateRedirectURL(
                loginConfiguration.getHttpAuthAutoLogin(),
                session,
                user.getPreferredLanguage(),
                loginConfiguration.getUiWebPath()));
        } catch (OXException e) {
            String errorPage = loginConfiguration.getErrorPageTemplate().replace("ERROR_MESSAGE", e.getMessage());
            resp.setContentType(CONTENTTYPE_HTML);
            resp.getWriter().write(errorPage);
        }
    }

    private LoginResult login(HttpServletRequest httpRequest, final Context context, final User user, Map<String, String> optState, LoginConfiguration loginConfiguration) throws OXException {
        Map<String, Object> properties;
        if (optState == null) {
            properties = new HashMap<String, Object>(4);
        } else {
            properties = new HashMap<String, Object>(optState);
        }

        return LoginPerformer.getInstance().doLogin(getLoginRequest(httpRequest, context, user, loginConfiguration), properties);
    }

    private LoginRequestImpl getLoginRequest(HttpServletRequest httpRequest, Context context, User user, LoginConfiguration loginConfiguration) throws OXException {
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        String defaultClient = loginConfiguration.getDefaultClient();
        String hash = HashCalculator.getInstance().getHash(httpRequest, userAgent, defaultClient);
        boolean forceHTTPS = com.openexchange.tools.servlet.http.Tools.considerSecure(httpRequest, forceHTTPS());
        Cookie[] cookies = getCookies(httpRequest);
        Map<String, List<String>> headers = getHeaders(httpRequest);
        LoginRequestImpl req = new LoginRequestImpl(
            user.getLoginInfo() + '@' + context.getLoginInfo()[0],
            null,                                         /* password */
            httpRequest.getRemoteAddr(),
            userAgent,
            UUIDs.getUnformattedString(UUID.randomUUID()), /* auth id */
            defaultClient,
            "1.0",
            hash,
            Interface.HTTP_JSON,
            headers,
            cookies,
            forceHTTPS,
            httpRequest.getServerName(),
            httpRequest.getServerPort(),
            com.openexchange.tools.servlet.http.Tools.getRoute(httpRequest.getSession(true).getId()));

        return req;
    }

    private static Cookie[] getCookies(HttpServletRequest req) {
        final List<Cookie> cookies;
        if (null == req) {
            cookies = Collections.emptyList();
        } else {
            cookies = new ArrayList<Cookie>();
            for (final javax.servlet.http.Cookie c : req.getCookies()) {
                cookies.add(new Cookie() {
                    @Override
                    public String getName() {
                        return c.getName();
                    }

                    @Override
                    public String getValue() {
                        return c.getValue();
                    }
                });
            }
        }
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    private static Map<String, List<String>> getHeaders(HttpServletRequest req) {
        final Map<String, List<String>> headers;
        if (null == req) {
            headers = Collections.emptyMap();
        } else {
            headers = new HashMap<String, List<String>>();
            @SuppressWarnings("unchecked") Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                List<String> header = new ArrayList<String>();
                if (headers.containsKey(name)) {
                    header = headers.get(name);
                }
                header.add(req.getHeader(name));
                headers.put(name, header);
            }
        }
        return headers;
    }

    private static String generateRedirectURL(String shouldStore, Session session, String language, String uiWebPath) {
        String retval = uiWebPath;
        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_SESSION, session.getSessionID());
        // App Suite UI requires some additional values.
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER, session.getLogin());
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_USER_ID, Integer.toString(session.getUserId()));
        retval = LoginTools.addFragmentParameter(retval, "language", language);
        if (shouldStore != null) {
            retval = LoginTools.addFragmentParameter(retval, "store", shouldStore);
        }
        return retval;
    }

    private LoginConfiguration initLoginConfiguration() throws OXException {
        ConfigurationService config = requireService(ConfigurationService.class, services);
        final String uiWebPath = config.getProperty(ServerConfig.Property.UI_WEB_PATH.getPropertyName(), ServerConfig.Property.UI_WEB_PATH.getDefaultValue());
        final boolean sessiondAutoLogin = config.getBoolProperty(ConfigurationProperty.SESSIOND_AUTOLOGIN.getPropertyName(), Boolean.parseBoolean(ConfigurationProperty.SESSIOND_AUTOLOGIN.getDefaultValue()));
        final CookieHashSource hashSource = CookieHashSource.parse(config.getProperty(Property.COOKIE_HASH.getPropertyName(), Property.COOKIE_HASH.getDefaultValue()));
        final String httpAuthAutoLogin = config.getProperty(ConfigurationProperty.HTTP_AUTH_AUTOLOGIN.getPropertyName(), ConfigurationProperty.HTTP_AUTH_AUTOLOGIN.getDefaultValue());
        final String defaultClient = config.getProperty(ConfigurationProperty.HTTP_AUTH_CLIENT.getPropertyName(), ConfigurationProperty.HTTP_AUTH_CLIENT.getDefaultValue());
        final String clientVersion = config.getProperty(ConfigurationProperty.HTTP_AUTH_VERSION.getPropertyName(), ConfigurationProperty.HTTP_AUTH_VERSION.getDefaultValue());
        final String templateFileLocation = config.getProperty(ConfigurationProperty.ERROR_PAGE_TEMPLATE.getPropertyName(), ConfigurationProperty.ERROR_PAGE_TEMPLATE.getDefaultValue());
        String errorPageTemplate;
        if (null == templateFileLocation) {
            errorPageTemplate = LoginServlet.ERROR_PAGE_TEMPLATE;
        } else {
            final File templateFile = new File(templateFileLocation);
            try {
                errorPageTemplate = IOTools.getFileContents(templateFile);
                LOG.info("Found an error page template at {}", templateFileLocation);
            } catch (final FileNotFoundException e) {
                LOG.error("Could not find an error page template at {}, using default.", templateFileLocation);
                errorPageTemplate = LoginServlet.ERROR_PAGE_TEMPLATE;
            }
        }
        final int cookieExpiry = ConfigTools.parseTimespanSecs(config.getProperty(ServerConfig.Property.COOKIE_TTL.getPropertyName(), ServerConfig.Property.COOKIE_TTL.getDefaultValue()));
        final boolean cookieForceHTTPS = config.getBoolProperty(ServerConfig.Property.COOKIE_FORCE_HTTPS.getPropertyName(), Boolean.parseBoolean(ServerConfig.Property.COOKIE_FORCE_HTTPS.getDefaultValue())) || config.getBoolProperty(ServerConfig.Property.FORCE_HTTPS.getPropertyName(), Boolean.parseBoolean(ServerConfig.Property.FORCE_HTTPS.getDefaultValue()));
        final boolean insecure = config.getBoolProperty(ConfigurationProperty.INSECURE.getPropertyName(), Boolean.parseBoolean(ConfigurationProperty.INSECURE.getDefaultValue()));
        final boolean ipCheck = config.getBoolProperty(ServerConfig.Property.IP_CHECK.getPropertyName(), Boolean.parseBoolean(ServerConfig.Property.IP_CHECK.getDefaultValue()));
        final ClientWhitelist ipCheckWhitelist = new ClientWhitelist().add(config.getProperty(Property.IP_CHECK_WHITELIST.getPropertyName(), Property.IP_CHECK_WHITELIST.getDefaultValue()));
        final boolean redirectIPChangeAllowed = config.getBoolProperty(ConfigurationProperty.REDIRECT_IP_CHANGE_ALLOWED.getPropertyName(), Boolean.parseBoolean(ConfigurationProperty.REDIRECT_IP_CHANGE_ALLOWED.getDefaultValue()));
        final List<IPRange> ranges = new LinkedList<IPRange>();
        final String tmp = config.getProperty(ConfigurationProperty.NO_IP_CHECK_RANGE.getPropertyName());
        if (tmp != null) {
            final String[] lines = Strings.splitByCRLF(tmp);
            for (String line : lines) {
                line = line.replaceAll("\\s", "");
                if (!line.equals("") && (line.length() == 0 || line.charAt(0) != '#')) {
                    ranges.add(IPRange.parseRange(line));
                }
            }
        }
        final boolean disableTrimLogin = config.getBoolProperty(ConfigurationProperty.DISABLE_TRIM_LOGIN.getPropertyName(), Boolean.parseBoolean(ConfigurationProperty.DISABLE_TRIM_LOGIN.getDefaultValue()));
        final boolean formLoginWithoutAuthId = config.getBoolProperty(ConfigurationProperty.FORM_LOGIN_WITHOUT_AUTHID.getPropertyName(), Boolean.parseBoolean(ConfigurationProperty.FORM_LOGIN_WITHOUT_AUTHID.getDefaultValue()));
        final boolean isRandomTokenEnabled = config.getBoolProperty(ConfigurationProperty.RANDOM_TOKEN.getPropertyName(), Boolean.parseBoolean(ConfigurationProperty.RANDOM_TOKEN.getDefaultValue()));
        LoginConfiguration conf = new LoginConfiguration(
            uiWebPath,
            sessiondAutoLogin,
            hashSource,
            httpAuthAutoLogin,
            defaultClient,
            clientVersion,
            errorPageTemplate,
            cookieExpiry,
            cookieForceHTTPS,
            insecure,
            ipCheck,
            ipCheckWhitelist,
            redirectIPChangeAllowed,
            ranges,
            disableTrimLogin,
            formLoginWithoutAuthId,
            isRandomTokenEnabled);
        return conf;
    }

    private boolean forceHTTPS() throws OXException {
        ConfigurationService configService = requireService(ConfigurationService.class, services);
        return Boolean.parseBoolean(configService.getProperty(Property.FORCE_HTTPS.getPropertyName(), Property.FORCE_HTTPS.getDefaultValue()));
    }

    private static <T> T requireService(Class<T> serviceClass, ServiceLookup serviceLookup) throws OXException {
        T service = serviceLookup.getService(serviceClass);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(serviceClass.getName());
        }

        return service;
    }

}
