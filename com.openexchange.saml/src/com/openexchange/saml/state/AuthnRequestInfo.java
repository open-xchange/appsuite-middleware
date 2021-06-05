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

package com.openexchange.saml.state;


/**
 * Contains the available information about an already sent authentication request.
 * This is for example used to assign responses to their according requests, i.e.
 * to validate InResponseTo attributes of response objects.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see DefaultAuthnRequestInfo
 */
public interface AuthnRequestInfo {

    /**
     * Gets the unique ID of the AuthnRequest.
     *
     * @return The ID
     */
    String getRequestId();

    /**
     * Gets the domain name via which the HTTP request initiating the AuthnRequest was
     * received. This domain is later on used to redirect to the <code>redeemReservation</code>
     * login action.
     *
     * @return The domain name
     */
    String getDomainName();

    /**
     * Gets the path that is later on passed as <code>uiWebPath</code> parameter to the
     * <code>redeemReservation</code>. This action will redirect the client to this path
     * and append the session ID and other user-specific parameters as fragment parameters.
     *
     * @return The path or <code>null</code> to omit the <code>uiWebPath</code> parameter
     * and force the default. This will be the configured <code>com.openexchange.UIWebPath</code>.
     */
    String getLoginPath();

    /**
     * Gets the client identifier that shall be assigned with the session to be created.
     *
     * @return The client ID or <code>null</code> if none was provided. In this case the
     * configured default will be used, i.e. the value of <code>com.openexchange.ajax.login.http-auth.client</code>
     * in <code>login.properties</code>.
     */
    String getClientID();

    /**
     * Gets the URI fragment that is later on passed back as part of the redirect URI,
     * when the actual client session is created and returned.
     *
     * @return The fragment or <code>null</code>
     */
    String getUriFragment();

}
