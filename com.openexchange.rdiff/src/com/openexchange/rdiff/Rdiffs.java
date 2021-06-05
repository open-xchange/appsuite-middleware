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

package com.openexchange.rdiff;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec.digest.DigestUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link Rdiffs} - A utility class for rdiff.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Rdiffs {

    /**
     * Initializes a new {@link Rdiffs}.
     */
    private Rdiffs() {
        super();
    }

    /**
     * Compute the MD5 checksum for a file and returns the value as a 32 character hex string.
     *
     * @param fname The name of the file to checksum
     * @return The 32 character hex string of the MD5 digest of the file
     * @throws OXException If the file cannot be read
     */
    public static String fileChecksumString(final String fname) throws OXException {
        InputStream in = null;
        try {
            in = new FileInputStream(fname);
            return DigestUtils.md5Hex(in);
        } catch (IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
        }
    }

    /**
     * Compute the MD5 checksum for a file, returning it in a new buffer.
     *
     * @param fname The name of the file to checksum
     * @return The MD5 digest of the file.
     * @throws OXException If the file cannot be read
     */
    public static byte[] fileChecksum(final String fname) throws OXException {
        InputStream in = null;
        try {
            in = new FileInputStream(fname);
            return DigestUtils.md5(in);
        } catch (IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
        }
    }

}
