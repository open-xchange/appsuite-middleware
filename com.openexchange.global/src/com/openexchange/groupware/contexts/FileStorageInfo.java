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

package com.openexchange.groupware.contexts;

import java.io.Serializable;

/**
 * {@link FileStorageInfo} - Provides information to access a certain file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public interface FileStorageInfo extends Serializable {

    /**
     * Gets the file storage credentials.
     *
     * @return A string array with login and password for the file storage.
     */
    String[] getFileStorageAuth();

    /**
     * Gets the file storage quota
     *
     * @return The quota for the file storage, <code>0</code> (zero) if no quota at all (deny all), or less than <code>0</code> (zero) for unlimited/not set
     */
    long getFileStorageQuota();

    /**
     * Gets the file storage identifier
     *
     * @return The file storage identifier
     */
    int getFilestoreId();

    /**
     * Gets the entity-specific location inside the file storage; e.g. <code>"1_ctx_store"</code>.
     *
     * @return The entity-specific location inside the file storage.
     */
    String getFilestoreName();

}
