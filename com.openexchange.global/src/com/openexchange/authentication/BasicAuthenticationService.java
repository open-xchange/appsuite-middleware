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

package com.openexchange.authentication;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link BasicAuthenticationService} - The special basic authentication service.
 * <p>
 * Especially used to authenticate guest users.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
@SingletonService
public interface BasicAuthenticationService extends AuthenticationService {

    /**
     * This method maps the login information from the login screen to the both parts needed to resolve the context and the user of that
     * context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return An {@link Authenticated} instance that is rather used to perform additional modifications through implementing
     *         {@link SessionEnhancement} and/or {@link ResponseEnhancement}
     * @throws OXException If an Open-Xchange error occurs
     */
    Authenticated handleLoginInfo(int userId, int contextId) throws OXException;

    /**
     * This method maps the login information from the login screen to the both parts needed to resolve the context and the user of that
     * context and checks if the password is valid for the user/context relation.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param password The password to check
     * @return An {@link Authenticated} instance that is rather used to perform additional modifications through implementing
     *         {@link SessionEnhancement} and/or {@link ResponseEnhancement}
     * @throws OXException If the user cannot be authenticated or an Open-Xchange error occurs
     */
    Authenticated handleLoginInfo(int userId, int contextId, String password) throws OXException;
}
