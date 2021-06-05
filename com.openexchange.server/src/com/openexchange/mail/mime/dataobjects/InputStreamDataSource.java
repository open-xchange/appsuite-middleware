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

package com.openexchange.mail.mime.dataobjects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;


/**
 * {@link InputStreamDataSource}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InputStreamDataSource implements DataSource {

    private final InputStream inStream;

    private String type;

    /**
     * Initializes a new {@link InputStreamDataSource}.
     *
     * @param inputStream The input stream
     */
    public InputStreamDataSource(InputStream inputStream) {
        super();
        this.inStream = inputStream;
    }

    /**
     * Sets the MIME type
     *
     * @param type The MIME type
     * @return Theis reference
     */
    public InputStreamDataSource setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String getContentType() {
        return type == null ? "application/octet-stream" : type;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inStream;
    }

    @Override
    public String getName() {
        return "InputStreamDataSource";
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("Not Supported");
    }

}
