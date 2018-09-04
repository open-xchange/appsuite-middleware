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
package com.openexchange.oidc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.state.LogoutRequestInfo;

/**
 * Provides the web features for OpenID SSO services.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
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
