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
 * Contains the available information about an already sent logout request.
 * This is for example used to assign responses to their according requests, i.e.
 * to validate InResponseTo attributes of response objects.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see DefaultLogoutRequestInfo
 */
public interface LogoutRequestInfo {

    /**
     * Gets the ID of the sent logout request.
     *
     * @return The ID
     */
    String getRequestId();

    /**
     * Gets the ID of the session that is to be terminated.
     *
     * @return The session ID
     */
     String getSessionId();

    /**
     * Gets the domain name via which the HTTP request initiating the logout request was
     * received.
     *
     * @return The domain name
     */
     String getDomainName();

}
