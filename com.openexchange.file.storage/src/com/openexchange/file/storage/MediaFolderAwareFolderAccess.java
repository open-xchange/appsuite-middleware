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

import com.openexchange.exception.OXException;


/**
 * {@link MediaFolderAwareFolderAccess}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8
 */
public interface MediaFolderAwareFolderAccess {

    /**
     * Gets the folder considered as pictures folder.
     * <p>
     * <b>Note</b>: If pictures folder is not supported by this file storage, {@link FileStorageExceptionCodes#NO_SUCH_FOLDER
     * NO_SUCH_FOLDER} is thrown.
     *
     * @return The corresponding instance of {@link FileStorageFolder}
     * @throws OXException If either such a folder does not exist or could not be fetched
     */
    FileStorageFolder getPicturesFolder() throws OXException;

    /**
     * Gets the folder considered as documents folder.
     * <p>
     * <b>Note</b>: If documents folder is not supported by this file storage, {@link FileStorageExceptionCodes#NO_SUCH_FOLDER
     * NO_SUCH_FOLDER} is thrown.
     *
     * @return The corresponding instance of {@link FileStorageFolder}
     * @throws OXException If either such a folder does not exist or could not be fetched
     */
    FileStorageFolder getDocumentsFolder() throws OXException;

    /**
     * Gets the folder considered as templates folder.
     * <p>
     * <b>Note</b>: If templates folder is not supported by this file storage, {@link FileStorageExceptionCodes#NO_SUCH_FOLDER
     * NO_SUCH_FOLDER} is thrown.
     *
     * @return The corresponding instance of {@link FileStorageFolder}
     * @throws OXException If either such a folder does not exist or could not be fetched
     */
    FileStorageFolder getTemplatesFolder() throws OXException;

    /**
     * Gets the folder considered as music folder.
     * <p>
     * <b>Note</b>: If music folder is not supported by this file storage, {@link FileStorageExceptionCodes#NO_SUCH_FOLDER
     * NO_SUCH_FOLDER} is thrown.
     *
     * @return The corresponding instance of {@link FileStorageFolder}
     * @throws OXException If either such a folder does not exist or could not be fetched
     */
    FileStorageFolder getMusicFolder() throws OXException;

    /**
     * Gets the folder considered as videos folder.
     * <p>
     * <b>Note</b>: If videos folder is not supported by this file storage, {@link FileStorageExceptionCodes#NO_SUCH_FOLDER
     * NO_SUCH_FOLDER} is thrown.
     *
     * @return The corresponding instance of {@link FileStorageFolder}
     * @throws OXException If either such a folder does not exist or could not be fetched
     */
    FileStorageFolder getVideosFolder() throws OXException;

}
