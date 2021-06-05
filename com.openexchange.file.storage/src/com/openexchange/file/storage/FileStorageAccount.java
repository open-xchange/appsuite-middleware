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

package com.openexchange.file.storage;

import java.io.Serializable;
import java.util.Map;
import org.json.JSONObject;

/**
 * {@link FileStorageAccount} - A file storage account.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public interface FileStorageAccount extends Serializable, FileStorageConstants {

    /**
     * The identifier for default/primary file storage account.
     */
    public static final String DEFAULT_ID = "0";

    /**
     * Gets this account's configuration.
     *
     * @return The configuration as a {@link Map}
     */
    Map<String, Object> getConfiguration();

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the display name.
     *
     * @return The display name
     */
    String getDisplayName();

    /**
     * Gets the associated file storage service.
     *
     * @return The associated file storage service
     */
    FileStorageService getFileStorageService();

    /**
     * Gets the account's meta data
     *
     * @return The meta data of the account
     */
    JSONObject getMetadata();
}
