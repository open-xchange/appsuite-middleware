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

package com.openexchange.saml;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig.Binding;

/**
 * The interface for SAML 2.0 Web SSO support.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface WebSSOProvider {

    /**
     * Responds with an authentication request based on the configured binding.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @throws OXException If preparing the AuthnRequest fails
     * @throws IOException If writing to the servlet response fails
     */
    void respondWithAuthnRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException, IOException;

    /**
     * Handles an authentication response.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param binding The binding used to call the assertion consumer service
     * @throws OXException If an error occurs while processing the response
     * @throws IOException If writing to the servlet response fails
     */
    void handleAuthnResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws OXException, IOException;

    /**
     * handles a single logout request.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param binding The binding used to call the single logout service
     * @throws IOException If writing to the servlet response fails
     */
    void handleLogoutRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Binding binding) throws IOException;

    /**
     * Generates the service providers metadata XML.
     *
     * @return The XML as string
     */
    String getMetadataXML() throws OXException;

}
