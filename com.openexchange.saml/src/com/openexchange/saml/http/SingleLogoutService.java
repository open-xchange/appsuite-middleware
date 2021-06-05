/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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

    private void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws IOException {
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
