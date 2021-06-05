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

package com.openexchange.login.multifactor;

import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.session.Session;

/**
 * {@link MultifactorLoginService} performs multifactor authentication
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public interface MultifactorLoginService {

    /**
     * Performs multifactor authentication
     *
     * @param userId The user id
     * @param contextId The context id
     * @param locale The user locale
     * @param request The {@link LoginRequest} Request
     * @return <code>true</code> if authenticated and <code>false</code> if no multifactor installed
     * @throws OXException if authentication fails
     */
    public boolean checkMultiFactorAuthentication(int userId, int contextId, Locale locale, LoginRequest request) throws OXException;

    /**
     * Checks if the multifactor authentication was performed recently for a given session.
     *
     * If no multifactor authentication is required for the given session, no action is performed.
     * If multifactor is required for the given session, and an autologin happened since last authentication,
     * or the time of the last authentication has exceeded (See "c.o.multifactor.recentAuthenticationTime" configuration property)
     * an exception is thrown.
     *
     * @see {@link com.openexchange.MultifactorProperties#recentAuthenticationTime}
     * @param userId The ID of the user
     * @param contextId The ID of the user's context
     * @throws OXException If the authentication was not performed recently or during another error
     */
    public void checkRecentMultifactorAuthentication(Session session) throws OXException;

    /**
     * Checks if the user has any multifactor devices registered, and will require multifactor authentication
     * before access is granted
     *
     * @param userId The user id
     * @param contextId The context id
     * @return <code>true</code> if devices are registered, <code>false</code> otherwise
     * @throws OXException
     */
    boolean requiresMultifactor(int userId, int contextId) throws OXException;
}
