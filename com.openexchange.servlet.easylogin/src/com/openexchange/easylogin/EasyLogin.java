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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.easylogin;

import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.authentication.Cookie;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;

/**
 * New version with a login/handling that is more secure. Also parameter AuthID is added. Defaults are still set for maximum security: no
 * GET, SSL only
 *
 * @author <a href="mailto:info@open-xchange.com">Holger Achtziger</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class EasyLogin extends HttpServlet {

    private static final long serialVersionUID = 7233346063627500582L;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(EasyLogin.class));

    private static final String AUTH_ID_PARAMETER = "authId";

    private static final String UI_WEB_PATH_PARAMETER = "uiWebPath";

    private String ajaxRoot;

    private String passwordParam;

    private String loginParam;

    private String autologinParam;

    private boolean autologinDefault;

    private String clientParam;

    private String defaultClient;

    private boolean doGetEnabled;

    private boolean popupOnError;

    private boolean allowInsecure;

    private String errorPageTemplate;

    private String loadBalancer;

    public EasyLogin() {
        super();
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        passwordParam = config.getInitParameter("com.openexchange.easylogin.passwordPara");
        if (null == passwordParam) {
            passwordParam = "password";
            LOG.error("Could not find passwordPara in configuration file, using default: " + passwordParam);
        } else {
            LOG.info("Set passwordPara to " + passwordParam);
        }
        loginParam = config.getInitParameter("com.openexchange.easylogin.loginPara");
        if (null == loginParam) {
            loginParam = "login";
            LOG.error("Could not find loginPara in configuration file, using default: " + loginParam);
        } else {
            LOG.info("Set loginPara to " + loginParam);
        }
        autologinParam = config.getInitParameter("com.openexchange.easylogin.autologinPara");
        if (null == autologinParam) {
            autologinParam = "autologin";
            LOG.error("Could not find autologinPara in configuration file, using default: " + autologinParam);
        } else {
            LOG.info("Set autologinPara to " + autologinParam);
        }

        clientParam = config.getInitParameter("com.openexchange.easylogin.clientPara");
        if (null == clientParam) {
            clientParam = "client";
            LOG.error("Could not find clientPara in configuration file, using default: " + clientParam);
        } else {
            LOG.info("Set clientPara to " + clientParam);
        }

        defaultClient = config.getInitParameter("com.openexchange.easylogin.defaultClient");
        if (null == defaultClient) {
            LOG.info("No default client set for easylogin.");
        } else {
            LOG.info("Defaulting to client " + defaultClient);
        }

        final String autologinDefaultS = config.getInitParameter("com.openexchange.easylogin.autologin.default");
        if (null == autologinDefaultS) {
            LOG.error("No default for autologin param defined. Assuming false");
            autologinDefault = false;
        } else {
            autologinDefault = Boolean.parseBoolean(autologinDefaultS);
            LOG.info("Set autologin default to " + autologinDefault);
        }

        ajaxRoot = config.getInitParameter("com.openexchange.easylogin.AJAX_ROOT");
        if (null == ajaxRoot) {
            ajaxRoot = "/ajax";
            LOG.error("Could not find AJAX_ROOT in configuration file, using default: " + ajaxRoot);
        } else {
            LOG.info("Set AJAX_ROOT to " + ajaxRoot);
        }
        if (null == config.getInitParameter("com.openexchange.easylogin.doGetEnabled")) {
            doGetEnabled = false;
            LOG.error("Could not find doGetEnabled in configuration file, using default: " + doGetEnabled);
        } else {
            final String value = config.getInitParameter("com.openexchange.easylogin.doGetEnabled").trim();
            doGetEnabled = Boolean.parseBoolean(value);
            LOG.info("Set doGetEnabled to " + doGetEnabled);
        }

        if (null == config.getInitParameter("com.openexchange.easylogin.popUpOnError")) {
            popupOnError = true;
            LOG.error("Could not find popUpOnError in properties-file, using default: " + popupOnError);
        } else {
            final String value = config.getInitParameter("com.openexchange.easylogin.popUpOnError").trim();
            popupOnError = Boolean.parseBoolean(value);
            LOG.info("Set popUpOnError to " + popupOnError);
        }
        if (null == config.getInitParameter("com.openexchange.easylogin.allowInsecureTransmission")) {
            allowInsecure = false;
            LOG.error("Could not find allowInsecure in configuration file, using default: " + allowInsecure);
        } else {
            final String value = config.getInitParameter("com.openexchange.easylogin.allowInsecureTransmission").trim();
            allowInsecure = Boolean.parseBoolean(value);
            LOG.info("Set allowInsecure to " + allowInsecure);
        }
        if (null == config.getInitParameter("com.openexchange.easylogin.errorPageTemplate")) {
            errorPageTemplate = ERROR_PAGE_TEMPLATE;
            LOG.error("No errorPage-template was specified, using default.");
        } else {
            final String templateFileLocation = config.getInitParameter("com.openexchange.easylogin.errorPageTemplate");
            final File templateFile = new File(templateFileLocation);
            if (templateFile.exists() && templateFile.canRead() && templateFile.isFile()) {
                errorPageTemplate = getFileContents(templateFile);
                LOG.info("Found an error page template at " + templateFileLocation);
            } else {
                LOG.error("Could not find an error page template at " + templateFileLocation + ", using default.");
                errorPageTemplate = ERROR_PAGE_TEMPLATE;
            }
        }
        loadBalancer = config.getInitParameter("com.openexchange.easylogin.loadBalancer");
        if (null == loadBalancer) {
            loadBalancer = "localhost";
            LOG.error("Could not find parameter loadBalancer in configuration file, using default: " + loadBalancer);
        } else {
            LOG.info("Set loadBalancer to " + loadBalancer);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        processLoginRequest(req, resp);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (!doGetEnabled) {
            LOG.error("IP: " + req.getRemoteAddr() + ", AuthID: " + getAuthID(req) + ", Denied GET request.");
            // show error to user
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET is not allowed.");
        } else {
            processLoginRequest(req, resp);
        }
    }

    private void processLoginRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Tools.disableCaching(resp);
        resp.setContentType("text/html");
        // check for / generate AuthID
        final String authID = getAuthID(req);
        final String login = getParameter(req, loginParam);
        if (!req.isSecure()) {
            if (!allowInsecure) {
                LOG.error("IP: " + req.getRemoteAddr() + ", Login: " + login + ", AuthID: " + authID + ", Denied insecure request.");
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "EasyLogin: Only secure transmission allowed.");
                return;
            }
        }
        if (null == login) {
            LOG.error("IP: " + req.getRemoteAddr() + ", AuthID: " + authID + ", Parameter " + loginParam + " is missing.");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "parameter " + loginParam + " is missing.");
            return;
        }
        final String password = getParameter(req, passwordParam);
        if (null == password) {
            LOG.error("IP: " + req.getRemoteAddr() + ", Login: " + login + ", AuthID: " + authID + ", Parameter " + passwordParam + " is missing.");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter " + passwordParam + " is missing.");
            return;
        }
        // do login via HTTPS
        // doHTTPLogin(resp, out, password, login, authID);
        // do login via Java
        doJavaLogin(req, resp, authID, login, password);
    }

    private String getAuthID(final HttpServletRequest req) {
        String authID = req.getParameter(AUTH_ID_PARAMETER);
        if (authID == null || req.getParameter(AUTH_ID_PARAMETER).trim().length() == 0) {
            authID = UUIDs.getUnformattedString(UUID.randomUUID());
        } else {
            authID = authID.trim();
        }
        return authID;
    }

    private String getParameter(final HttpServletRequest req, final String param) {
        String login = req.getParameter(param);
        if (null != login && login.trim().length() == 0) {
            login = null;
        }
        return login;
    }

    private void doJavaLogin(final HttpServletRequest req, final HttpServletResponse resp, final String authID, final String login, final String password) throws IOException {
        final LoginResult result;
        final String client = getClient(req);
        final String userAgent = parseUserAgent(req);
        final String clientIP = parseClientIP(req);
        final Map<String, List<String>> headers = copyHeaders(req);
        try {
            final Map<String, Object> properties = new HashMap<String, Object>(1);
            properties.put("http.request", req);
            result = LoginPerformer.getInstance().doLogin(new LoginRequest() {

                private final String hash = HashCalculator.getHash(req, client);

                @Override
                public String getUserAgent() {
                    return userAgent;
                }

                @Override
                public String getPassword() {
                    return password;
                }

                @Override
                public String getLogin() {
                    return login;
                }

                @Override
                public Interface getInterface() {
                    return Interface.HTTP_JSON;
                }

                @Override
                public String getClientIP() {
                    return clientIP;
                }

                @Override
                public String getAuthId() {
                    return authID;
                }

                @Override
                public String getClient() {
                    return client;
                }

                @Override
                public String getVersion() {
                    return null;
                }

                @Override
                public String getHash() {
                    return hash;
                }

                @Override
                public Map<String, List<String>> getHeaders() {
                    return headers;
                }

                @Override
                public Cookie[] getCookies() {
                    // TODO Auto-generated method stub
                    return null;
                }
            }, properties);
        } catch (final OXException e) {
            LOG.error("IP: " + req.getRemoteAddr() + ", Login: " + login + ", AuthID: " + authID + ", Login failed.", e);
            final String errorPage = errorPageTemplate.replace("ERROR_MESSAGE", e.getMessage());
            resp.getWriter().write(errorPage);
            return;
        }
        final Session session = result.getSession();
        // send redirect if login worked
        LOG.info("IP: " + req.getRemoteAddr() + ", Login: " + login + ", AuthID: " + authID + ", Login successful. Redirecting.");
        // JSESSIONID cookie gets automatically set by AJP connector on this response. Browser should reuse it for request to login servlet.
        final StringBuilder sb = new StringBuilder(ajaxRoot);
        sb.append("/login?action=redirect&random=");
        sb.append(session.getRandomToken());
        String uiWebPath = getParameter(req, UI_WEB_PATH_PARAMETER);

        // Escape significant characters like '#' so that the redirect target doesn't mistakenly parse them


        if (null != uiWebPath) {
            uiWebPath = URLEncoder.encode(uiWebPath, "UTF-8");
            sb.append('&');
            sb.append(UI_WEB_PATH_PARAMETER);
            sb.append('=');
            sb.append(uiWebPath);
        }
        // Store needed?
        final String autologinS = getParameter(req, autologinParam);
        boolean store = autologinDefault;
        if (autologinS != null) {
            store = Boolean.parseBoolean(autologinS);
        }
        sb.append("&store=").append(store);

        // Client
        if (client != null) {
            sb.append("&client=").append(client);
        }

        resp.sendRedirect(sb.toString());
    }

    private String getClient(final HttpServletRequest req) {
        String retval = getParameter(req, clientParam);
        if (retval == null) {
            retval = defaultClient;
        }
        return retval;
    }

    private static String parseUserAgent(final HttpServletRequest req) {
        final String userAgent;
        if (null == req.getParameter(LoginFields.USER_AGENT)) {
            userAgent = req.getHeader(Header.USER_AGENT);
        } else {
            userAgent = req.getParameter(LoginFields.USER_AGENT);
        }
        return userAgent;
    }

    private static String parseClientIP(final HttpServletRequest req) {
        final String clientIP;
        if (null == req.getParameter(LoginFields.CLIENT_IP_PARAM)) {
            clientIP = req.getRemoteAddr();
        } else {
            clientIP = req.getParameter(LoginFields.CLIENT_IP_PARAM);
        }
        return clientIP;
    }

    static public String getFileContents(final File file) {
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            final BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return stringBuilder.toString();
    }

    private static final String ERROR_PAGE_TEMPLATE = "<html>\n" + "<script type=\"text/javascript\">\n" + "\n" + "// Display normal HTML for 3 seconds, then redirect via referrer.\n" + "setTimeout(redirect,3000);\n" + "\n" + "function redirect(){\n" + " var referrer=document.referrer;\n" + " var redirect_url;\n" + " // If referrer already contains failed parameter, we don't add a 2nd one.\n" + " if(referrer.indexOf(\"login=failed\")>=0){\n" + "  redirect_url=referrer;\n" + " }else{\n" + "  // Check if referrer contains multiple parameter\n" + "  if(referrer.indexOf(\"?\")<0){\n" + "   redirect_url=referrer+\"?login=failed\";\n" + "  }else{\n" + "   redirect_url=referrer+\"&login=failed\";\n" + "  }\n" + " }\n" + " // Redirect to referrer\n" + " window.location.href=redirect_url;\n" + "}\n" + "\n" + "</script>\n" + "<body>\n" + "<h1>ERROR_MESSAGE</h1>\n" + "</body>\n" + "</html>\n";

}
