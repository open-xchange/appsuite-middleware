/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
        } catch (final IOException e) {
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
        } catch (final IOException e) {
            throw RdiffExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
        }
    }

}
