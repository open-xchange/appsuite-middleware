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

package com.openexchange.tools.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Utility methods for file handling.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class IOUtils {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IOUtils.class);

    /**
     * Prevent instantiation
     */
    private IOUtils() {
        super();
    }

    /**
     * Convenience method for closing an I/O resource.
     *
     * @param closeable The I/O resource to close.
     */
    public static void closeStuff(final Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Convenience method for closing an I/O resource quietly.
     *
     * @param closeable The I/O resource to close.
     */
    public static void closeQuietly(Closeable closeable) {
        if (null == closeable) {
            return;
        }
        try {
            closeable.close();
        } catch (@SuppressWarnings("unused") IOException e) {
            // Ignore
        }
    }

    /**
     * Convenience method for closing streams.
     *
     * @param input The stream to close.
     */
    public static void closeStreamStuff(final InputStream input) {
        closeStuff(input);
    }

    /**
     * Convenience method for closing readers.
     *
     * @param reader The reader to close.
     */
    public static void closeReaderStuff(final Reader reader) {
        closeStuff(reader);
    }

    /**
     * Convenience method for reading all from input stream and writing that to the output stream until end of file (EOF). This method does
     * not close either of the streams.
     *
     * @param in some input stream
     * @param out some output stream
     * @throws IOException if some problem occurs.
     */
    public static void transfer(final InputStream in, final OutputStream out) throws IOException {
        byte[] buffer = new byte[65536];
        for (int length; (length = in.read(buffer)) > 0;) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }

}
