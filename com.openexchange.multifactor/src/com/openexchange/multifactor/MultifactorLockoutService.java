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

package com.openexchange.multifactor;

import com.openexchange.exception.OXException;

/**
 * {@link MultifactorLockoutService}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public interface MultifactorLockoutService {

    /**
     * Checks if a user is locked out
     *
     * @param multifactorRequest The users {@link MultifactorRequest}
     * @throws OXException if locked out
     */
    void checkLockedOut(MultifactorRequest multifactorRequest) throws OXException;

    /**
     * Checks if a user is locked out
     *
     * @param userId The user id
     * @param contextId The context id
     * @throws OXException if locked out
     */
    public void checkLockedOut(int userId, int contextId) throws OXException;

    /**
     * Registers a user as having a bad login attempt
     *
     * @param userId The user id
     * @param contextId The context id
     * @throws OXException
     */
    public void registerFailedAttempt(int userId, int contextId) throws OXException;

    /**
     * Reports a good login
     *
     * @param userId The user id
     * @param contextId The context id
     * @throws OXException
     */
    void registerSuccessfullLogin(int userId, int contextId) throws OXException;

    /**
     * Returns the configured maximum allowed bad attempts
     *
     * @param userId The user id
     * @param contextId The context id
     * @return  Max bad attempts allowed
     */
    int getMaxBadAttempts(int userId, int contextId);
}
