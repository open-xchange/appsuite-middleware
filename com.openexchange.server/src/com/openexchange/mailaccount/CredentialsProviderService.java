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

package com.openexchange.mailaccount;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link CredentialsProviderService} - Provides dedicated credentials for mail access and mail transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface CredentialsProviderService {

    /**
     * Checks if this provider is appropriate for given arguments
     *
     * @param forMailAccess <code>true</code> if credentials are supposed to be determined for mail access; otherwise <code>false</code> for transport
     * @param accountId The account identifier
     * @param session The session
     * @return <code>true</code> if this provider handles given session's account; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isApplicableFor(boolean forMailAccess, int accountId, Session session) throws OXException;

    /**
     * Gets the credentials for mail access.
     *
     * @param accountId The account identifier
     * @param session The session
     * @return The credentials for mail access or <code>null</code>
     * @throws OXException If credential for mail access cannot be returned
     */
    Credentials getMailCredentials(int accountId, Session session) throws OXException;

    /**
     * Gets the credentials for mail transport.
     *
     * @param accountId The account identifier
     * @param session The session
     * @return The credentials for mail transport or <code>null</code>
     * @throws OXException If credential for mail transport cannot be returned
     */
    Credentials getTransportCredentials(int accountId, Session session) throws OXException;

}
