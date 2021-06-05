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

import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataSource;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.datasource.FileHolderDataSource;


/**
 * {@link FileHolderMailAttachment}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class FileHolderMailAttachment extends AbstractMailAttachment {

    private final IFileHolder fileHolder;

    /**
     * Initializes a new {@link FileHolderMailAttachment}.
     */
    public FileHolderMailAttachment(IFileHolder fileHolder) {
        super();
        this.fileHolder = fileHolder;
    }

    @Override
    public DataSource asDataHandler() throws IOException, OXException {
        ThresholdFileHolder tfh = (fileHolder instanceof ThresholdFileHolder) ? ((ThresholdFileHolder) fileHolder) : new ThresholdFileHolder(fileHolder);
        return new FileHolderDataSource(tfh, getContentType());
    }

    @Override
    public InputStream getStream() throws IOException {
        try {
            return fileHolder.getStream();
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException("Stream cannot be returned", null == cause ? e : cause);
        }
    }

    @Override
    public void close() throws Exception {
        fileHolder.close();
    }

    @Override
    public long getLength() {
        return fileHolder.getLength();
    }

    @Override
    public String getContentType() {
        return fileHolder.getContentType();
    }

    @Override
    public String getName() {
        return fileHolder.getName();
    }

    @Override
    public String getDisposition() {
        return fileHolder.getDisposition();
    }

    @Override
    public void setContentType(String contentType) {
        throw new UnsupportedOperationException("FileHolderMailAttachment.setContentType()");
    }

    @Override
    public void setDisposition(String disposition) {
        throw new UnsupportedOperationException("FileHolderMailAttachment.setDisposition()");
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("FileHolderMailAttachment.setName()");
    }

}
