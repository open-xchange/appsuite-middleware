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

package com.openexchange.mail.mime.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;

/**
 * {@link FileHolderDataSource} - A data source backed by a file holder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileHolderDataSource implements DataSource {

    private final ThresholdFileHolder fh;
    private final String contentType;

    /**
     * Initializes a new {@link FileHolderDataSource}.
     *
     * @param fh The file holder
     * @param contentType The associated MIME type
     */
    public FileHolderDataSource(ThresholdFileHolder fh, String contentType) {
        super();
        this.fh = fh;
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return fh.getStream();
        } catch (OXException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(e);
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        ThresholdFileHolder tmp = fh;
        if (null != tmp) {
            try {
                tmp.close();
            } catch (Exception ignore) {
                // Ignore
            }
        }
    }

}