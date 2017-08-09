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

package com.openexchange.oidc.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.oidc.OIDCConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.OIDCWebSSOProvider;
import com.openexchange.oidc.spi.OIDCExceptionHandler;
import com.openexchange.server.ServiceLookup;

/**
 * The servlet to handle OpenID specific requests like login and logout.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class InitService extends OIDCServlet {

    private static final Logger LOG = LoggerFactory.getLogger(InitService.class);
    private static final long serialVersionUID = -7066156332544428369L;
    private final ServiceLookup services;
    private final OIDCConfig config;

    private static final ArrayList<String> acceptedFlows = new ArrayList<String>() {

        private static final long serialVersionUID = -2423863714624255114L;
        {
            add("login");
            add("logout");
        }
    };

    public InitService(OIDCWebSSOProvider provider, OIDCExceptionHandler exceptionHandler, ServiceLookup services, OIDCConfig config) {
        super(provider, exceptionHandler);
        this.services = services;
        this.config = config;
    }

    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        String flow = httpRequest.getParameter("flow");
        if (!validateFlow(flow)) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            String redirectURI = this.getRedirectURI(flow, httpRequest);
            buildResponse(httpResponse, redirectURI, httpRequest.getParameter("redirect"));
        } catch (OXException e) {
            //TODO QS-VS: Alle exceptions hier ausgeben und weiteres Vorgehen angeben
            if (e.getExceptionCode() == OIDCExceptionCode.INVALID_LOGOUT_REQUEST) {
                LOG.debug(e.getLocalizedMessage());
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    private String getRedirectURI(String flow, HttpServletRequest httpRequest) throws OXException {
        String redirectUri = "";
        if (flow.equals("login")) {
            redirectUri = provider.getLoginRedirectRequest(httpRequest);
        } else if (flow.equals("logout")) {
            redirectUri = provider.getLogoutRedirectRequest(httpRequest);
        }
        return redirectUri;
    }

    private void buildResponse(HttpServletResponse httpResponse, String redirectURI, String respondWithRedirect) throws IOException {
        if (respondWithRedirect != null && Boolean.parseBoolean(respondWithRedirect)) {
            httpResponse.sendRedirect(redirectURI);
        } else {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            httpResponse.setCharacterEncoding(Charsets.UTF_8_NAME);
            httpResponse.setContentType("application/json");
            PrintWriter writer = httpResponse.getWriter();
            writer.write("{\"redirect\":\"" + redirectURI + "\"}");
            writer.flush();
        }
    }

    private boolean validateFlow(String flow) {
        boolean isValid = true;
        if (flow == null) {
            LOG.debug("OpenID flow parameter not set");
            isValid = false;
        } else if (!acceptedFlows.contains(flow)) {
            StringBuilder validFlows = new StringBuilder();
            String delim = "";
            for (String validFlow : acceptedFlows) {
                validFlows.append(delim).append(validFlow);
                delim = ",";
            }
            LOG.debug("OpenID flow parameter unknown, valid parameters are: {}. Input is: {}", validFlows, flow);
            isValid = false;
        }
        return isValid;
    }
}
