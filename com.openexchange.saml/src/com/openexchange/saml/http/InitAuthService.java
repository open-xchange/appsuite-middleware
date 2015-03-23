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

package com.openexchange.saml.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.saml.WebSSOProvider;
import com.openexchange.saml.spi.ExceptionHandler;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link InitAuthService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class InitAuthService extends SAMLServlet {

    private static final Logger LOG = LoggerFactory.getLogger(InitAuthService.class);

    private static final long serialVersionUID = -4022982444417155759L;


    /**
     * Initializes a new {@link InitAuthService}.
     * @param provider
     * @param exceptionHandler
     */
    public InitAuthService(WebSSOProvider provider, ExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }

    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        Tools.removeCachingHeader(httpResponse);
        try {
            String redirectURI = provider.buildAuthnRequest(httpRequest, httpResponse);
            String respondWithRedirect = httpRequest.getParameter("redirect");
            if (respondWithRedirect != null && Boolean.parseBoolean(respondWithRedirect)) {
                httpResponse.sendRedirect(redirectURI);
                return;
            }

            httpResponse.setStatus(HttpServletResponse.SC_OK);
            httpResponse.setCharacterEncoding(Charsets.UTF_8_NAME);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"redirect_uri\":\"" + redirectURI + "\"}");
        } catch (OXException e) {
            LOG.error("Could not build AuthnRequest", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
