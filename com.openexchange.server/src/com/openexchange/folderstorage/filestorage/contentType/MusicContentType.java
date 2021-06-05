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

package com.openexchange.folderstorage.filestorage.contentType;


/**
 * {@link MusicContentType}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8
 */
public class MusicContentType extends FileStorageContentType {

    private static final MusicContentType instance = new MusicContentType();

    /**
     * Gets the {@link MusicContentType} instance.
     *
     * @return The {@link MusicContentType} instance
     */
    public static MusicContentType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link MusicContentType}.
     */
    private MusicContentType() {
        super();
    }

    @Override
    public int getPriority() {
        return 1;
    }

}
