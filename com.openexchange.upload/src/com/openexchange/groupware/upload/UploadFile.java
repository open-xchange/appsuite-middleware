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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link UploadFile}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface UploadFile extends BasicUploadFile {

    /**
     * Sets the file's field name in multipart upload.
     *
     * @param fieldName The file's field name in multipart upload.
     */
    void setFieldName(final String fieldName);

    /**
     * Sets the file's content type.
     *
     * @param contentType The file's content type.
     */
    void setContentType(final String contentType);

    /**
     * Sets the value of the <code>"Content-Id"</code> header.
     *
     * @param contentId The value of the <code>"Content-Id"</code> header or <code>null</code>
     */
    void setContentId(final String contentId);

    /**
     * Sets the file name as provided through upload form.
     *
     * @param fileName The file name
     */
    void setFileName(final String fileName);

    /**
     * Sets the file size in bytes.
     *
     * @param size The file size in bytes.
     */
    void setSize(final long size);

    /**
     * Gets the associated unique temporary file on disk.
     *
     * @return The associated unique temporary file on disk.
     */
    File getTmpFile();

    /**
     * Sets the associated unique temporary file on disk.
     *
     * @param tmpFile The associated unique temporary file on disk.
     */
    void setTmpFile(final File tmpFile);

    /**
     * Gets the {@link InputStream} to the file held on disk.
     *
     * @return The <tt>InputStream</tt> instance
     * @throws IOException If opening stream fails
     */
    InputStream openStream() throws IOException;

}
