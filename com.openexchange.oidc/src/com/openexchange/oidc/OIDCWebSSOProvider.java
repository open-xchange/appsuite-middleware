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
package com.openexchange.oidc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.state.LogoutRequestInfo;

/**
 * Provides the web features for OpenID SSO services.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public interface OIDCWebSSOProvider {

    /**
     * Builds the login request for the init service.
     * 
     * @param request - The servlet request
     * @param response - The servlet response
     * @return the login redirect request
     * @throws OXException when something fails during the process
     */
    String getLoginRedirectRequest(HttpServletRequest request, HttpServletResponse response) throws OXException;

    /**
     * Authenticate a user by sending a request to the OP and validating its response. Finally sends
     * a login request to the server to finish the login process and create a valid OX Session.
     * 
     * @param request - The servlet request
     * @param response - The servlet response
     * @throws OXException when something fails during the process
     */
    void authenticateUser(HttpServletRequest request, HttpServletResponse response) throws OXException;

    /**
     * Builds the logout request for the init service.
     * 
     * @param request - The servlet request
     * @param response - The servlet response
     * @return the logout redirect request
     * @throws OXException when something fails during the process
     */
    String getLogoutRedirectRequest(HttpServletRequest request, HttpServletResponse response) throws OXException;

    /**
     * Build the request to trigger user logout from OXServer. Validate request information by
     * inspecting the given state, before.
     * 
     * @param request - The servlet request
     * @param response - The servlet response
     * @return the logout redirect request to logout from server
     * @throws OXException when something fails during the process
     */
    String logoutSSOUser(HttpServletRequest request, HttpServletResponse response) throws OXException;
    
    /**
     * Terminate the session with the given sessionId directly, without any OP relevant
     * validation before.
     * 
     * @param sessionId - The session id, that should be terminated
     * @param request - The servlet request
     * @param response - The servlet response
     * @throws OXException when something fails during the process
     */
    void logoutInCaseOfError(String sessionId, HttpServletRequest request, HttpServletResponse response) throws OXException;

    /**
     * Check the request for valid third party login informations. The iss parameters content
     * is checked by comparing it to the backends known issuer in {@link OIDCBackendConfig}.getIssuer().
     * 
     * @param request - The request to validate
     * @return true if the issuer is known, false if no iss parameter is available or not matching the
     *  backends known issuer.
     */
    boolean validateThirdPartyRequest(HttpServletRequest request);

    /**
     * Resume the user to his previously opened page before the logout attempt.
     * 
     * @param request - The servlet request
     * @param response - The servlet response
     * @throws OXException If the {@link LogoutRequestInfo} can not be loaded
     */
    void resumeUser(HttpServletRequest request, HttpServletResponse response) throws OXException;
}
