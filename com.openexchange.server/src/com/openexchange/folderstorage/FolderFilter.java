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
 * {@link FolderFilter} - A filter for folders.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FolderFilter {

    /**
     * Tests whether or not the specified folder should be included.
     * <p>
     * For example filter folders of a certain content type:
     *
     * <pre>
     *
     *
     *
     * FolderFilter myFilter = new FolderFilter() {
     *
     *     public boolean accept(Folder folder) {
     *         return XYZContentType.getInstance().equals(folder.getContenType());
     *     }
     * };
     * </pre>
     *
     * @param folder The folder to be tested
     * @return <code>true</code> if and only if <code>folder</code> should be included
     */
    boolean accept(Folder folder);

}
