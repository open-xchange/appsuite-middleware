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

package com.openexchange.file.storage.json.actions.files;

import com.openexchange.file.storage.FileStorageFileAccess;

/**
 * {@link IdVersionPair} - A tuple for file identifier, version string and folder identifier.
 * <p>
 * The JSON representation for such a tuple is either:
 * <ul>
 *  <li>
 *   For a folder
 *   <ul><li><code>{"folder": &lt;folder&gt;}</code></li></ul>
 *  </li>
 *  <li>
 *   For a file
 *   <ul><li><code>{"id": &lt;file&gt;, "version": &lt;opt-version&gt;, "folder": &lt;opt-folder&gt;}</code></li></ul>
 *  </li>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IdVersionPair {

    private final String identifier;
    private final String version;
    private final String folderId;

    /**
     * Initializes a new {@link IdVersionPair}.
     *
     * @param identifier The file identifier
     * @param version The version string
     * @param folderId The folder identifier
     */
    public IdVersionPair(final String identifier, final String version, final String folderId) {
        super();
        this.identifier = identifier;
        this.version = version == null ? FileStorageFileAccess.CURRENT_VERSION : version;
        this.folderId = folderId;
    }

    /**
     * Gets the folder identifier.
     *
     * @return The folder identifier
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Gets the file identifier.
     *
     * @return The file identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the version string.
     *
     * @return The version or {@link FileStorageFileAccess#CURRENT_VERSION}
     */
    public String getVersion() {
        return version;
    }

}
