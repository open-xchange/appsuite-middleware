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

package com.openexchange.mail.autoconfig;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link AutoconfigService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
@SingletonService
public interface AutoconfigService {

    /**
     * Tries to generate an auto-config result just with the given mail address.
     *
     * @param email The E-Mail address
     * @param password The password
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return An auto-config result if generation was successful, <code>null</code> otherwise
     * @throws OXException If determining auto-config result causes an error
     */
    Autoconfig getConfig(String email, String password, int userId, int contextId) throws OXException;

    /**
     * Tries to generate an auto-config result just with the given mail address.
     *
     * @param email The E-Mail address
     * @param password The password
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param forceSecure <code>true</code> if a secure connection should be enforced; otherwise <code>false</code> to also allow plain ones
     * @return An auto-config result if generation was successful, <code>null</code> otherwise
     * @throws OXException If determining auto-config result causes an error
     */
    Autoconfig getConfig(String email, String password, int userId, int contextId, boolean forceSecure) throws OXException;

}
