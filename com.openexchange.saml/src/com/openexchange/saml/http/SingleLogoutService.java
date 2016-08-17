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

package com.openexchange.saml.http;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.SAMLWebSSOProvider;
import com.openexchange.saml.spi.ExceptionHandler;
import com.openexchange.tools.servlet.http.Tools;


/**
 * Handles requests and responses of the single logout profile.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SingleLogoutService extends SAMLServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SingleLogoutService.class);

    private static final long serialVersionUID = 8167911323803230663L;

    /**
     * Initializes a new {@link SingleLogoutService}.
     * @param provider
     * @param exceptionHandler
     */
    public SingleLogoutService(SAMLWebSSOProvider provider, ExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }

    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        handleRequest(httpRequest, httpResponse, Binding.HTTP_REDIRECT);
    }

    @Override
    protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        handleRequest(httpRequest, httpResponse, Binding.HTTP_POST);
    }

    private void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws ServletException, IOException {
        switch (getRequestType(httpRequest)) {
            case SAML_REQUEST:
                provider.handleLogoutRequest(httpRequest, httpResponse, binding);
                break;
            case SAML_RESPONSE:
                try {
                    provider.handleLogoutResponse(httpRequest, httpResponse, binding);
                } catch (OXException e) {
                    LOG.error("Error while handling SAML login response", e);
                    exceptionHandler.handleLogoutResponseFailed(httpRequest, httpResponse, e);
                }
                break;
            default:
                Tools.disableCaching(httpResponse);
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                break;
        }
    }

    private Type getRequestType(HttpServletRequest httpRequest) {
        @SuppressWarnings("unchecked")
        Enumeration<String> params = httpRequest.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            if ("SAMLRequest".equals(param)) {
                return Type.SAML_REQUEST;
            } else if ("SAMLResponse".equals(param)) {
                return Type.SAML_RESPONSE;
            }
        }

        return Type.INVALID;
    }

    private static enum Type {
        SAML_REQUEST,
        SAML_RESPONSE,
        INVALID
    }

}
