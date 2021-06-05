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

package com.openexchange.file.storage.config;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ConfigFileStorageAuthenticator} - Sets the authentication properties for a pre-configured account of a certain service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ConfigFileStorageAuthenticator {

    /**
     * Indicates if this authenticator handles the specified file storage service.
     *
     * @param serviceId The file storage service identifier
     * @return <code>true</code> if this authenticator handles the specified file storage service; otherwise <code>false</code>
     */
    boolean handles(String serviceId);

    /**
     * Gets this authenticator's ranking.
     * <p>
     * The default ranking is zero (<tt>0</tt>). An authenticator with a ranking of {@code Integer.MAX_VALUE} is very likely to be returned
     * as the default authenticator, whereas an authenticator with a ranking of {@code Integer.MIN_VALUE} is very unlikely to be returned.
     *
     * @return The ranking
     */
    int getRanking();

    /**
     * Sets the authentication properties to given account.
     *
     * @param account The account to apply authentication properties to
     * @param session The session of the associated user
     * @throws OXException If setting authentication properties fails
     */
    void setAuthenticationProperties(ConfigFileStorageAccount account, Session session) throws OXException;

}
