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

package com.openexchange.groupware.upload.impl;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.groupware.upload.StreamedUploadFile;
import com.openexchange.groupware.upload.StreamedUploadFileInputStream;

/**
 * {@link StreamedUploadFileImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class StreamedUploadFileImpl implements StreamedUploadFile {

    private String fieldName;
    private String fileName;
    private String preparedFileName;
    private String contentType;
    private String contentId;
    private long size;
    private StreamedUploadFileInputStream stream;

    /**
     * Initializes a new {@link StreamedUploadFileImpl}.
     */
    public StreamedUploadFileImpl() {
        super();
        size = -1;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name
     *
     * @param fieldName The field name to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content-type
     *
     * @param contentType The content-type to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    /**
     * Sets the content-id
     *
     * @param contentId The content-id to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name
     *
     * @param fileName The file name to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public long getSize() {
        return size;
    }

    /**
     * Sets the size in bytes.x
     *
     * @param size The size
     */
    public void setSize(final long size) {
        this.size = size;
    }

    @Override
    public String getPreparedFileName() {
        if (null == preparedFileName) {
            if (null == fileName) {
                return null;
            }
            preparedFileName = fileName;
            /*
             * Try guessing the filename separator
             */
            int pos = -1;
            if ((pos = preparedFileName.lastIndexOf('\\')) != -1) {
                preparedFileName = preparedFileName.substring(pos + 1);
            } else if ((pos = preparedFileName.lastIndexOf('/')) != -1) {
                preparedFileName = preparedFileName.substring(pos + 1);
            }
            // TODO: Ensure that filename is not transfer-encoded
            // preparedFileName = CodecUtils.decode(preparedFileName,
            // ServerConfig
            // .getProperty(ServerConfig.Property.DefaultEncoding));
        }
        return preparedFileName;
    }

    @Override
    public StreamedUploadFileInputStream getStream() throws IOException {
        return stream;
    }

    /**
     * Sets the stream
     *
     * @param stream The stream to set
     */
    public void setStream(InputStream stream) {
        this.stream = StreamedUploadFileInputStream.streamFor(stream);
    }

}
