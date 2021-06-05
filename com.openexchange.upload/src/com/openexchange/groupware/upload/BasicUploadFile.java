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

package com.openexchange.groupware.upload;

/**
 * {@link BasicUploadFile} - The basic interface for an uploaded file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface BasicUploadFile {

    /**
     * Gets the file's field name in multipart upload.
     *
     * @return The file's field name in multipart upload.
     */
    String getFieldName();

    /**
     * Gets the file's content type.
     *
     * @return The file's content type.
     */
    String getContentType();

    /**
     * Gets the value of the optional <code>"Content-Id"</code> header.
     *
     * @return The value of the <code>"Content-Id"</code> header or <code>null</code>
     */
    String getContentId();

    /**
     * Gets the file name as given through upload form.
     * <p>
     * The file name possible contains the full path on sender's file system and may be encoded as well; e.g.<br>
     * <code>l=C3=B6l=C3=BCl=C3=96=C3=96=C3=96.txt</code> or <code>C:\MyFolderOnDisk\myfile.dat</code>
     * <p>
     * To ensure to deal with the expected file name call {@link #getPreparedFileName()}.
     *
     * @see #getPreparedFileName()
     * @return The file name.
     */
    String getFileName();

    /**
     * Gets the prepared file name; meaning prepending path and encoding information omitted.
     *
     * @return The prepared file name
     */
    String getPreparedFileName();

    /**
     * Gets the file size in bytes.
     *
     * @return The file size in bytes or <code>-1</code> if unknown
     */
    long getSize();

}
