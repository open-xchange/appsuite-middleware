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

package com.openexchange.api.client;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ApiClientService} - Service to obtain an {@link ApiClient}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
@SingletonService
public interface ApiClientService {

    /**
     * Gets a client that can execute requests to another OX server
     *
     * @param session The user's session
     * @param loginLink The link to the login endpoint
     * @param credentials The optional credentials to access the share
     * @return An {@link ApiClient}
     * @throws OXException In case an access can't be generated
     */
    ApiClient getApiClient(Session session, String loginLink, Credentials credentials) throws OXException;

    /**
     * Closes the client and logs out from the remote system
     *
     * @param client The client to close
     */
    void close(ApiClient client);

    /**
     * Closes the client and logs out from the remote system
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param loginLink The optional login link the client was created with. If not set, all clients for the user will be removed
     */
    void close(Session session, String loginLink);
}
