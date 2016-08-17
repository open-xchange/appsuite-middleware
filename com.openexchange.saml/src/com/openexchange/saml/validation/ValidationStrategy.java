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

package com.openexchange.saml.validation;

import javax.servlet.http.HttpServletRequest;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.LogoutRequestInfo;


/**
 * Interface for the strategy that is used to validate SAML messages.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface ValidationStrategy {

    /**
     * Validates an authentication response and extracts the bearer assertion that denotes the authentication
     * of a certain subject.
     *
     * @param response The response
     * @param requestInfo The request info according to the response
     * @param binding The binding
     * @return The validation result containing the determined bearer assertion and optionally an according
     *         {@link AuthnRequestInfo} denoting the AuthnRequest that initiated the authentication flow.
     * @throws ValidationException If the response validation fails
     */
    AuthnResponseValidationResult validateAuthnResponse(Response response, AuthnRequestInfo requestInfo, Binding binding) throws ValidationException;

    /**
     * Validates a logout request.
     *
     * @param response The request
     * @param httpRequest The according servlet request
     * @param binding The binding
     * @throws ValidationException If the response validation fails
     */
    void validateLogoutRequest(LogoutRequest logoutRequest, HttpServletRequest httpRequest, Binding binding) throws ValidationException;

    /**
     * Validates a logout response.
     *
     * @param response The response
     * @param requestInfo The request info according to the response
     * @param binding The binding
     * @throws ValidationException If the response validation fails
     */
    void validateLogoutResponse(LogoutResponse response, LogoutRequestInfo requestInfo, Binding binding) throws ValidationException;

}
