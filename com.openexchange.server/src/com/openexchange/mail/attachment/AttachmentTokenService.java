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

package com.openexchange.mail.attachment;

import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link AttachmentTokenService} - The attachment token service
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@SingletonService
public interface AttachmentTokenService {

    /**
     * Drops tokens for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    void dropFor(int userId, int contextId);

    /**
     * Drops tokens for given session.
     *
     * @param Session The session
     */
    void dropFor(Session session);

    /**
     * Removes the token with specified identifier from this registry.
     *
     * @param tokenId The token identifier
     */
    void removeToken(String tokenId);

    /**
     * Gets the token for specified token identifier.
     *
     * @param tokenId The token identifier
     * @param chunked <code>true</code> if a chunk-wise retrieval is performed; otherwise <code>false</code>
     * @return The token or <code>null</code> if absent or expired
     */
    AttachmentToken getToken(String tokenId, boolean chunked);

    /**
     * Puts specified token into this registry.
     *
     * @param token The token
     * @param session The session providing user data
     */
    void putToken(AttachmentToken token, Session session);

}
