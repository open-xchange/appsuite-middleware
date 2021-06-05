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

package com.openexchange.client.onboarding.carddav.custom;

import com.openexchange.exception.OXException;

/**
 * {@link CustomLoginSource} provides the CardDAV login name to use for the CardDAV client on-boarding provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface CustomLoginSource {

    /**
     * Provides the CardDAV login.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The CardDAV login
     * @throws OXException If the CardDAV login cannot be returned
     */
    String getCardDAVLogin(int userId, int contextId) throws OXException;

}
