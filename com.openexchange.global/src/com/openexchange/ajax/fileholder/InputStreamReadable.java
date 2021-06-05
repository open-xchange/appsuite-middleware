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

package com.openexchange.ajax.fileholder;

import java.io.IOException;
import java.io.InputStream;


/**
 * {@link InputStreamReadable} - The {@link Readable} representation for an {@link InputStream} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class InputStreamReadable implements Readable {

    private final InputStream in;

    /**
     * Initializes a new {@link InputStreamReadable}.
     *
     * @throws IllegalArgumentException If input stream is null
     */
    public InputStreamReadable(InputStream in) {
        super();
        if (null == in) {
            throw new IllegalArgumentException("Input stream is null.");
        }
        this.in = in;
    }

    /**
     * Gets the input stream
     *
     * @return The input stream
     */
    public InputStream getInputStream() {
        return in;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

}
