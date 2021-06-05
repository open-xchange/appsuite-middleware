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

package com.openexchange.gdpr.dataexport.provider.general;

/**
 * {@link Folder} - Represents a folder; carrying an identifier, a (display) name and a flag whether folder is a root folder that contains no items.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class Folder {

    private final String folderId;
    private final String name;
    private final boolean rootFolder;

    /**
     * Initializes a new {@link Folder}.
     */
    public Folder(String folderId, String name, boolean rootFolder) {
        super();
        this.folderId = folderId;
        this.name = name;
        this.rootFolder = rootFolder;
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
     * Gets the name.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks whether folder is a root folder that contains no items.
     *
     * @return <code>true</code> for root folder; otherwise <code>false</code>
     */
    public boolean isRootFolder() {
        return rootFolder;
    }

    @Override
    public String toString() {
        return new StringBuilder(name).append(" (folderId=").append(folderId).append(", root=").append(rootFolder).append(')').toString();
    }

}
