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

package com.openexchange.oidc.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.OIDCExceptionHandler;
import com.openexchange.oidc.OIDCWebSSOProvider;

/**
 * {@link AuthenticationService} Servlet to handle authentication requests.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class AuthenticationService extends OIDCServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);
    private static final long serialVersionUID = 7963146313895672894L;

    public AuthenticationService(OIDCWebSSOProvider provider, OIDCExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String queryString = request.getQueryString();
        if (null != queryString && Strings.toLowerCase(queryString).contains("error=")) {
            String errorMessage = request.getParameter("error") + ":" + request.getParameter("error_description");
            exceptionHandler.handleResponseException(request, response, OIDCExceptionCode.OP_SERVER_ERROR.create(errorMessage));
            return;
        }

        try {
            this.provider.authenticateUser(request, response);
        } catch (OXException e) {
            LOG.error("", e);
            exceptionHandler.handleAuthenticationFailed(request, response, e);
        }
    }

}
