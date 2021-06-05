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

package com.openexchange.notification.mail.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.datasource.FileDataSource;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.notification.osgi.Services;


/**
 * {@link FileMailAttachment}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class FileMailAttachment extends AbstractMailAttachment {

    private final File file;

    /**
     * Initializes a new {@link FileMailAttachment}.
     */
    public FileMailAttachment(File file) {
        super();
        this.file = file;
    }

    @Override
    public DataSource asDataHandler() throws IOException, OXException {
        return new FileDataSource(file, getContentType());
    }

    @Override
    public InputStream getStream() throws IOException {
        return Streams.bufferedInputStreamFor(new FileInputStream(file));
    }

    @Override
    public long getLength() {
        return file.length();
    }

    @Override
    public String getName() {
        return name == null ? file.getName() : name;
    }

    @Override
    public String getContentType() {
        return contentType == null ? determineMimeTypeByFile() : contentType;
    }

    private String determineMimeTypeByFile() {
        MimeTypeMap mimeTypeMap = Services.getOptionalService(MimeTypeMap.class);
        return null == mimeTypeMap ? "application/octet-stream" : mimeTypeMap.getContentType(file);
    }

}
