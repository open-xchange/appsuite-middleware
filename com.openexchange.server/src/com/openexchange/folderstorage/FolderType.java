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
 * {@link FolderType} - The folder type of a certain folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FolderType {

    /**
     * The folder type to store global parameters to {@link StorageParameters storage parameters}.
     */
    public static final FolderType GLOBAL = new FolderType() {

        @Override
        public boolean servesTreeId(final String treeId) {
            return false;
        }

        @Override
        public boolean servesParentId(final String parentId) {
            return false;
        }

        @Override
        public boolean servesFolderId(final String folderId) {
            return false;
        }
    };

    /**
     * Indicates if this folder type serves specified tree identifier.
     *
     * @param treeId The tree identifier
     * @return <code>true</code> if this folder type serves specified tree identifier; otherwise <code>false</code>
     */
    boolean servesTreeId(String treeId);

    /**
     * Indicates if this folder type serves specified folder identifier.
     *
     * @param folderId The folder identifier
     * @return <code>true</code> if this folder type serves specified folder identifier; otherwise <code>false</code>
     */
    boolean servesFolderId(String folderId);

    /**
     * Indicates if this folder type serves specified parent identifier. If <code>true</code> the
     * {@link FolderStorage#getSubfolders(String, String, StorageParameters)} delivers a non-empty array for this parent identifier.
     *
     * @param parentId The parent identifier
     * @return <code>true</code> if this folder type serves specified parent identifier; otherwise <code>false</code>
     */
    boolean servesParentId(String parentId);

    /**
     * Must be implemented according to {@link Object#hashCode()}.
     */
    @Override
    int hashCode();

    /**
     * Must be implemented according to {@link Object#equals(Object)}.
     */
    @Override
    boolean equals(Object obj);

}
