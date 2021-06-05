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
import com.openexchange.oidc.OIDCExceptionHandler;
import com.openexchange.oidc.OIDCWebSSOProvider;
import com.openexchange.oidc.tools.OIDCTools;

/**
 * {@link LogoutService} Servlet, that handle logout requests from client.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class LogoutService extends OIDCServlet {

    private static final Logger LOG = LoggerFactory.getLogger(LogoutService.class);
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6178674444883447429L;

    public LogoutService(OIDCWebSSOProvider provider, OIDCExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.trace("doGet(HttpServletRequest request: {}, HttpServletResponse response)", request.getRequestURI());
        String type = request.getParameter(OIDCTools.TYPE);
        if (type != null && request.getParameter(OIDCTools.TYPE).equalsIgnoreCase(OIDCTools.RESUME)) {
            try {
                this.provider.resumeUser(request, response);
            } catch (OXException e) {
                exceptionHandler.handleLogoutFailed(request, response, e);
            }
        } else {
            try {
                String redirectURI = this.provider.logoutSSOUser(request, response);
                OIDCTools.buildRedirectResponse(response, redirectURI, Boolean.TRUE);
            } catch (OXException e) {
                exceptionHandler.handleLogoutFailed(request, response, e);
            }
        }
    }
}
