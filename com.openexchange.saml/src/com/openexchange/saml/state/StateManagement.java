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

import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;


/**
 * The state management is used to assign authentication responses to previously generated requests
 * and to cache responses to check for replay attacks.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface StateManagement {

    /**
     * Adds an {@link AuthnRequestInfo} to the distributed state management.
     *
     * @param requestInfo The request info
     * @param ttl The time to live
     * @param timeUnit The time unit of <code>ttl</code>
     * @return The ID via which the stored request can be retrieved later on
     * @throws OXException If storing the request info fails
     */
    String addAuthnRequestInfo(AuthnRequestInfo requestInfo, long ttl, TimeUnit timeUnit) throws OXException;

    /**
     * Removes an {@link AuthnRequestInfo} by its ID and returns it.
     *
     * @param id The ID
     * @return The request info or <code>null</code> if the ID is invalid or the time to live is already expired
     * @throws OXException If removing the request info fails
     */
    AuthnRequestInfo removeAuthnRequestInfo(String id) throws OXException;

    /**
     * Adds the ID of an authentication response that will be remembered for the given time to live.
     *
     * @param responseID The ID of the response
     * @param ttl The time to live
     * @param timeUnit The time unit of <code>ttl</code>
     * @throws OXException If storing the response ID fails
     */
    void addAuthnResponseID(String responseID, long ttl, TimeUnit timeUnit) throws OXException;

    /**
     * Checks if a given response ID was remembered.
     *
     * @param responseID The ID of the response
     * @return <code>true</code> if the response ID was remembered before or <code>false</code> if not or if its time
     * to live is already exceeded.
     * @throws OXException If looking up the response ID fails
     */
    boolean hasAuthnResponseID(String responseID) throws OXException;

    /**
     * Adds a {@link LogoutRequestInfo} to the distributed state management.
     *
     * @param requestInfo The request info
     * @param ttl The time to live
     * @param timeUnit The time unit of <code>ttl</code>
     * @return The ID via which the stored request can be retrieved later on
     * @throws OXException If storing the request info fails
     */
    String addLogoutRequestInfo(LogoutRequestInfo requestInfo, long ttl, TimeUnit timeUnit) throws OXException;

    /**
     * Removes a {@link LogoutRequestInfo} by its ID and returns it.
     *
     * @param id The ID
     * @return The request info or <code>null</code> if the ID is invalid or the time to live is already expired
     * @throws OXException If removing the request info fails
     */
    LogoutRequestInfo removeLogoutRequestInfo(String id) throws OXException;

}
