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

package com.openexchange.saml;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.impl.SAMLLoginRequestHandler;
import com.openexchange.saml.impl.SAMLLogoutRequestHandler;
import com.openexchange.session.Session;

/**
 * The interface for SAML 2.0 Web SSO support.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface SAMLWebSSOProvider {

    /**
     * Builds an authentication request and compiles a redirect URI compliant to the HTTP-Redirect binding.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @return The redirect URI
     * @throws OXException If building the AuthnRequest fails
     */
    String buildAuthnRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException;

    /**
     * Handles an authentication response from the IdP. After successful validation of the response and determining
     * the principal, the user agent is redirected to the 'samlLogin' login action, where his session is created
     * and the session cookies are set.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param binding The binding used to call the assertion consumer service
     * @throws OXException If an error occurs while processing the response
     * @throws IOException If writing to the servlet response fails
     * @see SAMLLoginRequestHandler
     */
    void handleAuthnResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws OXException, IOException;

    /**
     * Builds with a logout request and compiles a redirect URI compliant to the HTTP-Redirect binding.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param session The session
     * @return The redirect URI
     * @throws OXException If preparing the LogoutRequest fails
     */
    String buildLogoutRequest(HttpServletRequest req, HttpServletResponse resp, Session session) throws OXException;

    /**
     * Handles a logout response. After successful validation of the response the user agent is redirected to the 'samlLogout'
     * login action, where the principals session is terminated and cookies are removed.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param binding The binding used to call the assertion consumer service
     * @throws OXException If an error occurs while processing the response
     * @throws IOException If writing to the servlet response fails
     * @see SAMLLogoutRequestHandler
     */
    void handleLogoutResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws IOException, OXException;

    /**
     * Handles a logout request from the IdP. After successful validation of the response the sessions to terminate are determined
     * (by <code>SessionIndex</code> elements or all sessions for a denoted principal). After termination the response is compiled
     * and sent to the IdP via the configured binding.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param binding The binding via which this request was received
     * @throws IOException If writing to the servlet response fails
     */
    void handleLogoutRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws IOException;

    /**
     * Generates the service providers metadata XML.
     *
     * @return The XML as string
     */
    String getMetadataXML() throws OXException;

    /**
     * Returns a static redirect for login if present or <code>null</code>
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @return a static redirect for login situations or <code>null</code>
     */
    String getStaticLoginRedirectLocation(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

}
