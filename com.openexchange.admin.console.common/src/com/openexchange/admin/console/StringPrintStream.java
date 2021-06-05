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

package com.openexchange.admin.console;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * {@link StringPrintStream} - Gathers everything written to print stream as a string.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class StringPrintStream extends PrintStream {

    /**
     * Creates a new {@link StringPrintStream} instance
     *
     * @param capacity The capacity
     * @return The new {@link StringPrintStream} instance
     * @throws UnsupportedEncodingException If the output stream can't be initialized with <code>UTF-8</code> encoding
     */
    public static StringPrintStream newInstance(final int capacity) throws UnsupportedEncodingException {
        return new StringPrintStream(new StringOutputStream(capacity));
    }

    // -------------------------------------------------------------------------------------------- //

    final StringOutputStream sos;

    private StringPrintStream(final StringOutputStream sos) throws UnsupportedEncodingException {
        super(sos, false, com.openexchange.java.Charsets.UTF_8_NAME);
        this.sos = sos;
    }

    @Override
    public String toString() {
        return sos.builder.toString();
    }

    private static class StringOutputStream extends OutputStream {

        final StringBuilder builder;

        StringOutputStream(final int capacity) {
            builder = new StringBuilder(capacity);
        }

        @Override()
        public void close() {
            // Nope
        }

        @Override()
        public void flush() {
            // Nope
        }

        @Override()
        public void write(final byte[] b) {
            builder.append(new String(b, StandardCharsets.UTF_8));
        }

        @Override()
        public void write(final byte[] b, final int off, final int len) {
            final byte[] bytes = new byte[len];
            System.arraycopy(b, off, bytes, 0, len);
            builder.append(new String(bytes, StandardCharsets.UTF_8));
        }

        @Override
        public void write(final int b) {
            builder.append(b);
        }
    }

}
