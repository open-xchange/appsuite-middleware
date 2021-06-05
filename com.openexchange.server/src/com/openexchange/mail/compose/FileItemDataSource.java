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

package com.openexchange.mail.compose;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.mail.json.compose.share.FileItem;


/**
 * {@link FileItemDataSource} - The data source (for the JavaBeans Activation Framework) backed by a file item.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class FileItemDataSource implements DataSource {

    private final FileItem fileItem;
    private final String contentType;

    /**
     * Initializes a new {@link FileItemDataSource}.
     *
     * @param fileItem The file item
     */
    public FileItemDataSource(FileItem fileItem) {
        this(fileItem, fileItem.getMimeType());
    }

    /**
     * Initializes a new {@link FileItemDataSource} with an explicit MIME type.
     *
     * @param fileItem The file item
     * @param contentType The MIME type
     */
    public FileItemDataSource(FileItem fileItem, String contentType) {
        super();
        this.fileItem = fileItem;
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return fileItem.getData();
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(e.getMessage(), e);
        }
    }


    @Override
    public String getName() {
        return fileItem.getName();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException(this.getClass().getName() + ".getOutputStream() isn't implemented");
    }

}
