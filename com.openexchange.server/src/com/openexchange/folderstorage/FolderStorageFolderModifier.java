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


/**
 * {@link FolderStorageFolderModifier} - Additional methods that allow to register a folder modifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface FolderStorageFolderModifier<F extends Folder> extends FolderStorage {

    /**
     * Adds specified folder modifier.
     *
     * @param modifier The modifier to add
     */
    void addFolderModifier(FolderModifier<F> modifier);

    /**
     * Removes the given folder modifier.
     *
     * @param modifier The modifier to remove
     */
    void removeFolderModifier(FolderModifier<F> modifier);
}
