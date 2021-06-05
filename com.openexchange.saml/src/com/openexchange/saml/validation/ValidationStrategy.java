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

package com.openexchange.saml.validation;

import javax.servlet.http.HttpServletRequest;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.Response;
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
     * @param httpRequest The according servlet request
     * @param requestInfo The request info according to the response
     * @param binding The binding
     * @throws ValidationException If the response validation fails
     */
    void validateLogoutResponse(LogoutResponse response, HttpServletRequest httpRequest, LogoutRequestInfo requestInfo, Binding binding) throws ValidationException;

}
