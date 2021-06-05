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

import java.io.Closeable;
import java.io.IOException;


/**
 * {@link Readable} - Represents a readable resource.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Readable extends Closeable {

    /**
     * Reads up to <code>b.length</code> bytes of data from this file into an array of bytes. This method blocks until at least one byte of input is available.
     *
     * @param b The buffer into which the data is read.
     * @return The total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end of
     *         this file has been reached.
     * @throws IOException If the first byte cannot be read for any reason
     *             other than end of file, or if the random access file has been closed, or if
     *             some other I/O error occurs.
     */
    int read(byte b[]) throws IOException;

    /**
     * Reads up to <code>len</code> bytes of data from this file into an array of bytes. This method blocks until at least one byte of input is available.
     *
     * @param b The buffer into which the data is read.
     * @param off The start offset in array <code>b</code>
     *            at which the data is written.
     * @param len The maximum number of bytes read.
     * @return The total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end of
     *         the file has been reached.
     * @throws IOException If the first byte cannot be read for any reason
     *             other than end of file, or if the random access file has been closed, or if
     *             some other I/O error occurs.
     */
    int read(byte b[], int off, int len) throws IOException;

}
