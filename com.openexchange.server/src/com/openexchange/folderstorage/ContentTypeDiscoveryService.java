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

package com.openexchange.folderstorage;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ContentTypeDiscoveryService} - Discovery service for folder content types.
 */
@SingletonService
public interface ContentTypeDiscoveryService {

    /**
     * Gets the content type for specified string.
     *
     * @param contentTypeString The content type string
     * @return The content type for specified string or <code>null</code> if none matches
     */
    ContentType getByString(String contentTypeString);

    /**
     * Gets the content type for specified module identifier.
     *
     * @param module The module identifier
     * @return The content type for specified module or <code>null</code> if none matches
     */
    ContentType getByModule(int module);

}
