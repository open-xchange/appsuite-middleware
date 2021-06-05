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

package com.openexchange.authentication.application;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link AppAuthenticatorService}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
@SingletonService
public interface AppAuthenticatorService {

    /**
     * Gets a value indicating whether the supplied login request will probably be handled by a known application-specific authenticator
     * or (definitely) not.
     * 
     * @param loginRequest The login request to check
     * @return <code>true</code> if the login request will probably be handled, <code>false</code> if (definitely) not
     */
    boolean applies(AppLoginRequest loginRequest);

    /**
     * Authenticates a login request using a registered application-specific authenticator.
     * <p/>
     * This assumes that {@link #applies(AppLoginRequest)} previously returned <code>true</code> for the supplied login request.
     *
     * @param loginRequest The login request to authenticate
     * @return The restricted authentication
     */
    RestrictedAuthentication doAuth(AppLoginRequest loginRequest) throws OXException;

}
