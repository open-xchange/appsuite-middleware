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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.servlet.http.Tools;

/**
 * New version with a login/handling that is more secure. Also parameter AuthID is added. 
 * Defaults are still set for maximum security: no GET, SSL only
 * 
 * @author <a href="mailto:info@open-xchange.com">Holger Achtziger</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class EasyLogin extends HttpServlet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7233346063627500582L;

    private static final Log LOG = LogFactory.getLog(EasyLogin.class);

    private static String AJAX_ROOT = "/ajax";

    private static String passwordPara = "password";

    private static String loginPara = "login";

    private static String redirPara = "redirect"; // param for what should be done after error on login

    private static String directLinkPara = "direct_link";

    private static String OX_PATH_RELATIVE = "../";

    private static boolean doGetEnabled = false;

    private static boolean popUpOnError = true;

    private static boolean allowInsecure = false;

    private static String remoteIP = "NONE";

    private static final String authIdParameter = "authId";

    private static String authID;
    
    private static String errorPageTemplate = "<html><body><h1>ERROR_MESSAGE</h1></body></html>";
    
    private static String loadBalancer = "localhost";

    /**
     * Initializes a new {@link EasyLogin}
     */
    public EasyLogin() {
        super();
    }

    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        remoteIP = req.getRemoteAddr();
        processLoginRequest(req, resp);

    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        remoteIP = req.getRemoteAddr();

        if (!doGetEnabled) {
            // show error to user
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET not supported");
        } else {
            processLoginRequest(req, resp);
        }

    }

    private void processLoginRequest(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        logInfo("Hostname=" + req.getRemoteHost() + ", URI=" + req.getRequestURI() + ", Scheme=" + req.getScheme());
        Tools.disableCaching(resp);
        resp.setContentType("text/html");
        if (allowInsecure || !req.isSecure()) {
            if (allowInsecure && !req.isSecure()) {
                logInfo("Using insecure transmission.");
            } else {
                logInfo("Rejecting insecure transmission.");
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "EasyLogin: Only secure transmission allowed");
                return;
            }
        }
        PrintWriter out = resp.getWriter();

        if (req.getParameter(passwordPara) == null || req.getParameter(passwordPara).trim().length() == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "parameter " + passwordPara + " missing");
            logError("Got request without password");
        } else if (req.getParameter(loginPara) == null || req.getParameter(loginPara).trim().length() == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "parameter " + loginPara + " missing");
            logError("Got request without login");
        } else {

            final String password = req.getParameter(passwordPara);
            final String login = req.getParameter(loginPara).trim().toLowerCase();

            logInfo("Login=" + login);
            // check for / generate AuthID
            if (req.getParameter(authIdParameter) == null || req.getParameter(authIdParameter).trim().length() == 0) {
                authID = UUIDs.getUnformattedString(UUID.randomUUID());
            } else {
                authID = req.getParameter(authIdParameter).trim();
            }
            // send login request via https
            String urlString = "http://"+ loadBalancer + AJAX_ROOT + "/login?action=login&name=" + login + "&password=" + password + "&" + authIdParameter + "=" + authID;
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.connect();
            int i = 1;
            String hdrKey = null;
            String jSessionID = "";
            
            while ((hdrKey = con.getHeaderFieldKey(i)) != null) {
                if (hdrKey.equals("Set-Cookie")) {
                    String content = con.getHeaderField(i);
                    String key = "";
                    String value = "";
                    Pattern keyPattern = Pattern.compile(("([^=]*)"));
                    Matcher keyMatcher = keyPattern.matcher(content);
                    if (keyMatcher.find()) {
                        key = keyMatcher.group(1);
                    }
                    Pattern valuePattern = Pattern.compile("=([^;]*)");
                    Matcher valueMatcher = valuePattern.matcher(content);
                    if (valueMatcher.find()) {
                        value = valueMatcher.group(1);
                    }
                    logInfo("Getting cookie : " + key + "=" + value);
                    if (content.startsWith("JSESSIONID")) {
                        jSessionID = value;
                        logInfo("jSessionID : " + jSessionID);
                    }
                }
                i++;
            }

            // get the random token from the response
            String randomToken = "";
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            String errorMessage = null;
            while ((line = rd.readLine()) != null) {
                Pattern randomPattern = Pattern.compile("\"random\":\"([^\"]*)");
                Matcher randomMatcher = randomPattern.matcher(line);
                if (randomMatcher.find()) {
                    randomToken = randomMatcher.group(1);
                    logInfo("randomToken : " + randomToken);
                }
                Pattern errorPattern = Pattern.compile("\"error\":\"([^\"]*)\"");
                Matcher errorMatcher = errorPattern.matcher(line);
                if (errorMatcher.find()) {
                    errorMessage = errorMatcher.group(1);
                }
            }
            rd.close();
            con.disconnect();
            
            if (errorMessage == null){
                // send redirect if login worked
                logInfo("Login worked, sending redirect");
                resp.sendRedirect(AJAX_ROOT + "/login;jsessionid=" + jSessionID + "?action=redirect&random=" + randomToken);
            } else {
                logInfo("Login did not work, sending errorPage");
                String errorPage = errorPageTemplate.replace("ERROR_MESSAGE", errorMessage);
                out.write(errorPage);
            }
            
        }
    }

    private static void logit(final String msg, final Throwable e, final boolean isError) {
        if (isError) {
            if (e != null) {
                LOG.error("EasyLogin IP: " + remoteIP + ", authId: " + authID + " : " + msg, e);
            } else {
                LOG.error("EasyLogin IP: " + remoteIP + ", authId: " + authID + " : " + msg);
            }
        } else {
            if (e != null) {
                LOG.info("EasyLogin IP: " + remoteIP + ", authId: " + authID + " : " + msg, e);
            } else {
                LOG.info("EasyLogin IP: " + remoteIP + ", authId: " + authID + " : " + msg);
            }
        }
    }

    private static void logError(final String msg) {
        logit(msg, null, true);
    }

    private static void logError(final String msg, final Throwable e) {
        logit(msg, e, true);
    }

    private static void logInfo(final String msg) {
        logit(msg, null, false);
    }

    public static void initConfig(ConfigurationService config) {
        synchronized (EasyLogin.class) {

            if (config.getProperty("com.openexchange.easylogin.passwordPara") != null) {
                passwordPara = config.getProperty("com.openexchange.easylogin.passwordPara");
                logInfo("Set passwordPara to " + passwordPara);
            } else {
                logError("Could not find passwordPara in properties-file, using default: " + passwordPara);
            }

            if (config.getProperty("com.openexchange.easylogin.loginPara") != null) {
                loginPara = config.getProperty("com.openexchange.easylogin.loginPara");
                logInfo("Set loginPara to " + loginPara);
            } else {
                logError("Could not find loginPara in properties-file, using default: " + loginPara);
            }

            if (config.getProperty("com.openexchange.easylogin.AJAX_ROOT") != null) {
                AJAX_ROOT = config.getProperty("com.openexchange.easylogin.AJAX_ROOT");
                logInfo("Set AJAX_ROOT to " + AJAX_ROOT);
            } else {
                logError("Could not find AJAX_ROOT in properties-file, using default: " + AJAX_ROOT);
            }

            if (config.getProperty("com.openexchange.easylogin.OX_PATH_RELATIVE") != null) {
                OX_PATH_RELATIVE = config.getProperty("com.openexchange.easylogin.OX_PATH_RELATIVE");
                logInfo("Set OX_PATH_RELATIVE to " + OX_PATH_RELATIVE);
            } else {
                logError("Could not find OX_PATH_RELATIVE in properties-file, using default: " + OX_PATH_RELATIVE);
            }

            if (config.getProperty("com.openexchange.easylogin.doGetEnabled") != null) {
                String property = config.getProperty("com.openexchange.easylogin.doGetEnabled", "").trim();
                doGetEnabled = Boolean.parseBoolean(property);
                logInfo("Set doGetEnabled to " + doGetEnabled);
            } else {
                logError("Could not find doGetEnabled in properties-file, using default: " + doGetEnabled);
            }

            if (config.getProperty("com.openexchange.easylogin.popUpOnError") != null) {
                String property = config.getProperty("com.openexchange.easylogin.popUpOnError", "").trim();
                popUpOnError = Boolean.parseBoolean(property);
                logInfo("Set popUpOnError to " + popUpOnError);
            } else {
                logError("Could not find popUpOnError in properties-file, using default: " + popUpOnError);
            }
            if (config.getProperty("com.openexchange.easylogin.allowInsecureTransmission") != null) {
                String property = config.getProperty("com.openexchange.easylogin.allowInsecureTransmission", "").trim();
                allowInsecure = Boolean.parseBoolean(property);
                logInfo("Set allowInsecure to " + allowInsecure);
            } else {
                logError("Could not find allowInsecure in properties-file, using default: " + allowInsecure);
            }
            if (config.getProperty("com.openexchange.easylogin.errorPageTemplate") != null) {
                String templateFileLocation = config.getProperty("com.openexchange.easylogin.errorPageTemplate", "");
                File templateFile = new File(templateFileLocation);
                if (templateFile.exists() && templateFile.canRead() && templateFile.isFile()){
                    errorPageTemplate = getFileContents(templateFile);
                    logInfo("Found an errorPage-template at " + templateFileLocation);
                } else {
                    logError("Could not find an errorPage-template at "+templateFileLocation+", using default.");
                }
            } else {
                logError("No errorPage-template was specified, using default.");
            }
            if (config.getProperty("com.openexchange.easylogin.loadBalancer") != null) {
                loadBalancer = config.getProperty("com.openexchange.easylogin.loadBalancer");
                logInfo("Set loadBalancer to " + loadBalancer);
            } else {
                logError("Could not find parameter loadBalancer in properties-file, using default: " + loadBalancer);
            }
        }
    }

    static public String getFileContents(File file) {
        StringBuilder stringBuilder = new StringBuilder();        
        try {
          BufferedReader input =  new BufferedReader(new FileReader(file));
          try {
            String line = null;
            while (( line = input.readLine()) != null){
              stringBuilder.append(line);
              stringBuilder.append(System.getProperty("line.separator"));
            }
          }
          finally {
            input.close();
          }
        }
        catch (IOException e){
          logError(e.getMessage(), e);
        }
        return stringBuilder.toString();
      }
}
