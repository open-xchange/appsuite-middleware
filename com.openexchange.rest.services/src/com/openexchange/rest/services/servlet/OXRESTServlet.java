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

package com.openexchange.rest.services.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.OXRESTMatch;
import com.openexchange.rest.services.OXRESTService;
import com.openexchange.rest.services.Response;
import com.openexchange.rest.services.internal.OXRESTServiceWrapper;
import com.openexchange.rest.services.internal.Services;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * The {@link OXRESTServlet} delegates handling to an {@link OXRESTService} instance
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXRESTServlet extends HttpServlet implements Servlet {

    private static final long serialVersionUID = -1956702653546932381L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXRESTServlet.class);

    private static final String PREFIX = "/preliminary";

    /**
     * The OX REST registry instance.
     */
    public static final OXRESTRegistry REST_SERVICES = new OXRESTRegistry();

    // ---------------------------------------------------------------------------------------------------------------------------------------------- //

    private volatile boolean doFail;
    private volatile String authLogin;
    private volatile String authPassword;

    /**
     * Initializes a new {@link OXRESTServlet}.
     */
    public OXRESTServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();

        ConfigurationService service = Services.getService(ConfigurationService.class);
        if (null == service) {
            throw new ServletException("Missing configuration service");
        }

        String authLogin = service.getProperty("com.openexchange.rest.services.basic-auth.login");
        String authPassword = service.getProperty("com.openexchange.rest.services.basic-auth.password");
        if (Strings.isEmpty(authLogin) || Strings.isEmpty(authPassword)) {
            doFail = true;
            this.authLogin = null;
            this.authPassword = null;
        } else {
            doFail = false;
            this.authLogin = authLogin.trim();
            this.authPassword = authPassword.trim();
        }
    }

    /**
     * Authentication identifier.
     */
    private static final String basicRealm = "OX REST";

    /**
     * Adds the header to the response message for authorization. Only add this header if the authorization of the user failed.
     *
     * @param resp the response to that the header should be added.
     */
    protected static void addUnauthorizedHeader(final HttpServletResponse resp) {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("Basic realm=\"").append(basicRealm).append("\", encoding=\"UTF-8\"");
        resp.setHeader("WWW-Authenticate", builder.toString());
    }

    private boolean authenticated(HttpServletRequest req) {
        if (doFail) {
            LOGGER.error("Denied incoming HTTP request to REST interface due to unset Basic-Auth configuration. Please set properties 'com.openexchange.rest.services.basic-auth.login' and 'com.openexchange.rest.services.basic-auth.password' appropriately.", new Throwable("Denied request to REST interface"));
            return false;
        }

        final String auth = req.getHeader("authorization");
        if (null == auth) {
            // Authorization header missing
            return false;
        }
        if (com.openexchange.tools.servlet.http.Authorization.checkForBasicAuthorization(auth)) {
            final Credentials creds = com.openexchange.tools.servlet.http.Authorization.decode(auth);
            if (!com.openexchange.tools.servlet.http.Authorization.checkLogin(creds.getPassword())) {
                // Empty password
                return false;
            }
            // Check parsed credentials
            return authLogin.equals(creds.getLogin()) && authPassword.equals(creds.getPassword());
        }

        // Unsupported auth scheme
        return false;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!authenticated(req)) {
                addUnauthorizedHeader(resp);
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
                return;
            }

            AJAXRequestData request = AJAXRequestDataTools.getInstance().parseRequest(req, false, false, ServerSessionAdapter.valueOf(0, 0), PREFIX, resp);

            for (@SuppressWarnings("unchecked") final Enumeration<String> headers = req.getHeaderNames(); headers.hasMoreElements();) {
                String headerName = headers.nextElement();
                request.setHeader(headerName, req.getHeader(headerName));
            }

            /*-
             * 1. Determine OXRESTRoute for method/path pair. OXRESTRoute knows path pattern and variables.
             *
             * 2. Yield OXRESTMatch by OXRESTRoute.match().
             *    The OXRESTMatch instance stores the parameters (variable-name -> path-value)
             *
             * 3. Create an OXRESTServiceWrapper from the OXRESTMatch instance (IntrospectingServiceFactory -> ReflectiveServiceWrapper)
             *    a) Create a new service instance
             *    b) Pass reflection information (java.lang.reflect.Method) and OXRESTMatch instance
             *    c) Create a new ReflectiveServiceWrapper
             *
             * 4. Put parameters obtained from OXRESTServiceWrapper into AJAXRequestData ( enhance() )
             *
             * 5. Apply AJAXRequestData to OXRESTServiceWrapper
             *
             * 6. Invoke OXRESTServiceWrapper.execute()
             *    Reflection-based invocation via java.lang.reflect.Method inside ReflectiveServiceWrapper
             */

            OXRESTServiceWrapper wrapper = REST_SERVICES.retrieve(req.getMethod(), request.getPathInfo());
            if (wrapper == null) {
                resp.sendError(404, "No such REST service or method match found");
                return;
            }

            enhance(wrapper.getMatch(), request);
            wrapper.setRequest(request);
            Response response = wrapper.execute();

            sendResponse(response, resp);
        } catch (OXException e) {
            resp.sendError(500, e.getMessage());
        }
    }

    private void sendResponse(Response response, HttpServletResponse resp) throws IOException {
        // Set HTTP status code
        resp.setStatus(response.getStatus());

        // Set headers
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            resp.setHeader(entry.getKey(), entry.getValue());
        }
        resp.setHeader("X-OX-ACHTUNG", "This is an internal API that may change without notice.");

        // Ensure a Content-Type is set
        if (!resp.containsHeader("Content-Type")) {
            resp.setContentType(OXRESTService.CONTENT_TYPE_JAVASCRIPT);
        }

        // Disable caching
        if (response.isDisableCaching()) {
            Tools.disableCaching(resp);
        } else {
            // For binary output
            Tools.removeCachingHeader(resp);
        }

        // Write response body
        // TODO: Allow for binary streams
        Iterable<String> body = response.getBody();
        if (body != null) {
            PrintWriter writer = resp.getWriter();
            for (String chunk : body) {
                writer.print(chunk);
            }
            writer.flush();
        }
    }

    private void enhance(OXRESTMatch match, AJAXRequestData request) {
        for (Map.Entry<String, String> entry : match.getParameters().entrySet()) {
            request.putParameter(entry.getKey(), entry.getValue());
        }
    }

}
