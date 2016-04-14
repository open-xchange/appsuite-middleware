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

package com.openexchange.imagetransformation;

import static com.openexchange.java.Strings.toLowerCase;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import com.openexchange.java.ByteArrayBufferedInputStream;
import com.openexchange.java.FastBufferedInputStream;

/**
 * {@link Utility} - Utility class for image transformation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Utility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Utility.class);

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Strips a leading "image/" as well as trailing additional properties after the first ";" from the supplied value if necessary from
     * image formats passed as content type. Also implicitly converts "pjpeg"- and "x-png"-formats as used by Internet Explorer to their
     * common format names.
     *
     * @param value The sImage
     * @return The cleaned image format
     */
    public static String getImageFormat(final String value) {
        if (null == value) {
            LOG.debug("No format name specified, falling back to 'jpeg'.");
            return "jpeg";
        }
        // Sanitize given image format
        String val = value;
        if (val.toLowerCase().startsWith("image/")) {
            val = val.substring(6);
        }
        int idx = val.indexOf(';');
        if (0 < idx) {
            val = val.substring(0, idx);
        }
        if ("pjpeg".equals(val)) {
            LOG.debug("Assuming 'jpeg' for image format {}", val);
            return "jpeg";
        }
        if ("x-png".equals(val)) {
            LOG.debug("Assuming 'png' for image format {}", val);
            return "png";
        }
        if ("x-ms-bmp".equals(val)) {
            LOG.debug("Assuming 'bmp' for image format {}", val);
            return "bmp";
        }
        if ("svg+xml".equals(val)) {
            LOG.debug("Assuming 'svg' for image format {}", val);
            return "svg";
        }
        if ("x-pcx".equals(val)) {
            LOG.debug("Assuming 'pcx' for image format {}", val);
            return "pcx";
        }
        if ("vnd.adobe.photoshop".equals(val)) {
            LOG.debug("Assuming 'psd' for image format {}", val);
            return "psd";
        }
        return val;
    }

    /**
     * Gets a value indicating whether an image format can be read or not.
     *
     * @param formatName The image format name
     * @return <code>true</code> if the image format can be read, <code>false</code>, otherwise
     */
    public static boolean canRead(String formatName) {
        String tmp = toLowerCase(formatName);
        if ("vnd.microsoft.icon".equals(tmp) || "x-icon".equals(tmp)) {
            return false;
        }
        //TODO: cache reader format names
        for (String readerFormatName : ImageIO.getReaderFormatNames()) {
            if (toLowerCase(readerFormatName).equals(tmp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether supplied image format is supposed to support transparency or not.
     *
     * @param formatName The image format name, e.g. "jpeg" or "tiff"
     * @return <code>true</code> if transparency is supported, <code>false</code>, otherwise
     */
    public static boolean supportsTransparency(String formatName) {
        return false == "jpeg".equalsIgnoreCase(formatName) && false == "jpg".equalsIgnoreCase(formatName);
    }

    /**
     * Returns a buffered {@link InputStream} for specified stream.
     *
     * @param in The stream
     * @return A new buffered input stream
     */
    public static BufferedInputStream bufferedInputStreamFor(InputStream in) {
        if (null == in) {
            return null;
        }
        if ((in instanceof BufferedInputStream)) {
            return (BufferedInputStream) in;
        }
        if ((in instanceof ByteArrayInputStream)) {
            return new ByteArrayBufferedInputStream((ByteArrayInputStream) in);
        }
        return new FastBufferedInputStream(in, 65536);
    }

    /**
     * Gets either a buffered or byte-array backed {@link InputStream} for specified stream, which is known to return <code>true</code> for {@link InputStream#markSupported() markSupported()}.
     *
     * @param in The stream
     * @return A mark-supporting input stream
     */
    public static InputStream markSupportingInputStreamFor(InputStream in) {
        if (null == in) {
            return null;
        }
        if ((in instanceof BufferedInputStream)) {
            return in;
        }
        if ((in instanceof ByteArrayInputStream)) {
            return in;
        }
        return new BufferedInputStream(in, 65536);
    }

    /**
     * Reads the magic number & resets specified image input stream.
     *
     * @param inputStream The input stream
     * @return The magic number or <code>-1</code> if stream is <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    public static int readMagicNumber(BufferedInputStream inputStream) throws IOException {
        if (null == inputStream) {
            return -1;
        }

        inputStream.mark(2);
        int magicNumber = inputStream.read() << 8 | inputStream.read();
        inputStream.reset();
        return magicNumber;
    }

}
