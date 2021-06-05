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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.groupware.upload.UploadFile;

/**
 * {@link UploadFileImpl} - Represents an uploaded file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UploadFileImpl implements UploadFile {

    private String fieldName;
    private String fileName;
    private String preparedFileName;
    private File tmpFile;
    private String contentType;
    private String contentId;
    private long size;

    /**
     * Initializes a new {@link UploadFileImpl}.
     */
    public UploadFileImpl() {
        super();
    }

    @Override
    public InputStream openStream() throws IOException {
        if (null == tmpFile) {
            throw new IOException("No such file");
        }
        return new FileInputStream(tmpFile);
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getFileName() {
        return fileName;
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
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(final long size) {
        this.size = size;
    }

    @Override
    public File getTmpFile() {
        return tmpFile;
    }

    @Override
    public void setTmpFile(final File tmpFile) {
        this.tmpFile = tmpFile;
    }

    @Override
    public String toString() {
        String fileName = getPreparedFileName();
        if (fileName != null) {
            return fileName;
        } else if (fieldName != null) {
            return fieldName;
        }
        return super.toString();
    }

}
