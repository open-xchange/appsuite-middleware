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

import com.openexchange.tools.session.ServerSession;

/**
 * {@link MultifactorAuthenticatorFactory}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public interface MultifactorAuthenticatorFactory {

    /**
     * Creates an instance of MultifactorAuthenticator
     *
     * For authentication that modifies the session, use {@link #createSessionModifyingAuthenticator(MultifactorProvider, ServerSession)}
     *
     * @param provider The provider to create an authenticator for
     * @return The new authenticator
     */
    MultifactorAuthenticator createAuthenticator(MultifactorProvider provider);

    /**
     * Returns an instance of MultifactorAuthenticator used for authentication.
     * Can modify the session to mark as authenticated if authentication successful
     *
     * @param provider The provider to create an authenticator for
     * @param session The session to modify
     * @return The new authenticator
     */
    MultifactorAuthenticator createSessionModifyingAuthenticator(MultifactorProvider provider, ServerSession session);

}
