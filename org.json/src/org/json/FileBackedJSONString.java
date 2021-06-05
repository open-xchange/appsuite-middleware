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

package org.json;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * {@link FileBackedJSONString} - A JSON string backed by a file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface FileBackedJSONString extends JSONString, Closeable, CharSequence {

    /**
     * Gets the optional temporary file.
     * <p>
     * If {@link #isInMemory()} signals <code>true</code>, then this method will return <code>null</code>, and the content should rather be obtained by {@link #getBuffer()}.
     *
     * @return The temporary file or <code>null</code>
     * @see #isInMemory()
     */
    File getTempFile();

    /**
     * Writes a single character.
     *
     * @param c The character to be written
     * @throws IOException If an I/O error occurs
     */
    void write(int c) throws IOException;

    /**
     * Writes an array of characters.
     *
     * @param cbuf The characters to be written
     * @throws IOException If an I/O error occurs
     */
    void write(char cbuf[]) throws IOException;

    /**
     * Writes a portion of an array of characters.
     *
     * @param cbuf The array of characters
     * @param off The offset from which to start writing characters
     * @param len The number of characters to write
     * @throws IOException If an I/O error occurs
     */
    void write(char cbuf[], int off, int len) throws IOException;

    /**
     * Writes a string.
     *
     * @param str The string to be written
     * @throws IOException If an I/O error occurs
     */
    void write(String str) throws IOException;

    /**
     * Flushes this instance.
     *
     * @exception IOException If an I/O error occurs
     */
    void flush() throws IOException;

}
