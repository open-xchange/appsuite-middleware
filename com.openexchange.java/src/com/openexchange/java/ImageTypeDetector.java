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

package com.openexchange.java;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link ImageTypeDetector} - Detects MIME type of passed image bytes.
 * <p>
 * See <a href="http://www.garykessler.net/library/file_sigs.html">http://www.garykessler.net/library/file_sigs.html</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageTypeDetector {

    /**
     * 42 4d
     */
    private static final byte[] PREFIX_BITMAP = { (byte) 0x42, (byte) 0x4D };

    /**
     * 53 49 4d 50 4c 45
     */
    private static final byte[] PREFIX_FITS = { (byte) 0x53, (byte) 0x49, (byte) 0x4D, (byte) 0x50, (byte) 0x4C, (byte) 0x45 };

    /**
     * 47 49 46 38
     */
    private static final byte[] PREFIX_GIF = { (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38 };

    /**
     * 47 4b 53 4d
     */
    private static final byte[] PREFIX_GKSM = { (byte) 0x47, (byte) 0x4B, (byte) 0x53, (byte) 0x4D };

    /**
     * 01 da
     */
    private static final byte[] PREFIX_IRIS = { (byte) 0x01, (byte) 0xDA };

    /**
     * f1 00 40 bb
     */
    private static final byte[] PREFIX_ITC = { (byte) 0xF1, (byte) 0x00, (byte) 0x40, (byte) 0xBB };

    /**
     * ff d8 ff e0
     */
    private static final byte[] PREFIX_JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 };

    /**
     * ff d8 ff e1
     *
     * Exif data available.
     */
    private static final byte[] PREFIX_JPEG2 = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1 };

    /**
     * ff d8 ff e2
     */
    private static final byte[] PREFIX_JPEG3 = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE2 };

    /**
     * 49 49 4e 31
     */
    private static final byte[] PREFIX_NIFF = { (byte) 0x49, (byte) 0x49, (byte) 0x4E, (byte) 0x31 };

    /**
     * 56 49 45 57
     */
    private static final byte[] PREFIX_PM = { (byte) 0x56, (byte) 0x49, (byte) 0x45, (byte) 0x57 };

    /**
     * 89 50 4e 47
     */
    private static final byte[] PREFIX_PNG = { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47 };

    /**
     * 25 21
     */
    private static final byte[] PREFIX_POSTSCRIPT = { (byte) 0x25, (byte) 0x21 };

    /**
     * 59 a6 6a 95
     */
    private static final byte[] PREFIX_SUN_RASTERFILE = { (byte) 0x59, (byte) 0xA6, (byte) 0x6A, (byte) 0x95 };

    /**
     * 4d 4d 00 2a
     */
    private static final byte[] PREFIX_TIFF_BIGENDIAN = { (byte) 0x4D, (byte) 0x4D, (byte) 0x00, (byte) 0x2A };

    /**
     * 49 49 2a 00
     */
    private static final byte[] PREFIX_TIFF_LITTLEENDIAN = { (byte) 0x49, (byte) 0x49, (byte) 0x2A, (byte) 0x00 };

    /**
     * 67 69 6d 70 20 78 63 66 20 76
     */
    private static final byte[] PREFIX_XCF_GIMP =
        { (byte) 0x67, (byte) 0x69, (byte) 0x6D, (byte) 0x70, (byte) 0x20, (byte) 0x78, (byte) 0x63, (byte) 0x66, (byte) 0x20, (byte) 0x76 };

    /**
     * 23 46 49 47
     */
    private static final byte[] PREFIX_XFIG = { (byte) 0x23, (byte) 0x46, (byte) 0x49, (byte) 0x47 };

    /**
     * 2f 2a 20 58 50 4d 20 2a 2f
     */
    private static final byte[] PREFIX_XPM =
        { (byte) 0x2F, (byte) 0x2A, (byte) 0x20, (byte) 0x58, (byte) 0x50, (byte) 0x4D, (byte) 0x20, (byte) 0x2A, (byte) 0x2F };

    /**
     * Initializes a new {@link ImageTypeDetector}.
     */
    private ImageTypeDetector() {
        super();
    }

    /**
     * Detects MIME type of passed image bytes.
     *
     * @param binary The image bytes
     * @return The image's MIME type or <code>"application/octet-stream"</code> if unknown
     * @throws IOException If an I/O error occurs
     */
    public static String getMimeType(InputStream binary) throws IOException {
        if (null == binary) {
            return "application/octet-stream";
        }
        try {
            byte[] head = new byte[32];
            int read = binary.read(head, 0, 32);
            Streams.close(binary);
            binary = null;

            if (0 >= read) {
                return "application/octet-stream";
            }

            if (read < 32) {
                byte[] tmp = new byte[read];
                System.arraycopy(head, 0, tmp, 0, read);
                head = tmp;
            }
            return getMimeType(head);
        } finally {
            Streams.close(binary);
        }
    }

    /**
     * <h2><a href="http://www.astro.keele.ac.uk/oldusers/rno/Computing/File_magic.html">Image files</a></h2>
     * <table border="1">
     * <tbody>
     * <tr>
     * <th>File type</th>
     * <th>Typical <br>
     * extension</th>
     * <th>Hex digits<br>
     * xx = variable</th>
     * <th>Ascii digits<br>
     * . = not an ascii char</th>
     * </tr>
     * <tr>
     * <td>Bitmap format</td>
     * <td>.bmp</td>
     * <td>42 4d</td>
     * <td>BM</td>
     * </tr>
     * <tr>
     * <td>FITS format</td>
     * <td>.fits</td>
     * <td>53 49 4d 50 4c 45</td>
     * <td>SIMPLE</td>
     * </tr>
     * <tr>
     * <td>GIF format</td>
     * <td>.gif</td>
     * <td>47 49 46 38</td>
     * <td>GIF8</td>
     * </tr>
     * <tr>
     * </tr>
     * <tr>
     * <td>Graphics Kernel System</td>
     * <td>.gks</td>
     * <td>47 4b 53 4d</td>
     * <td>GKSM</td>
     * </tr>
     * <tr>
     * <td>IRIS rgb format</td>
     * <td>.rgb</td>
     * <td>01 da</td>
     * <td>..</td>
     * </tr>
     * <tr>
     * <td>ITC (CMU WM) format</td>
     * <td>.itc</td>
     * <td>f1 00 40 bb</td>
     * <td>....</td>
     * </tr>
     * <tr>
     * <td>JPEG File Interchange Format</td>
     * <td>.jpg</td>
     * <td>ff d8 ff e0</td>
     * <td>....</td>
     * </tr>
     * <tr>
     * <td>NIFF (Navy TIFF)</td>
     * <td>.nif</td>
     * <td>49 49 4e 31</td>
     * <td>IIN1</td>
     * </tr>
     * <tr>
     * <td>PM format</td>
     * <td>.pm</td>
     * <td>56 49 45 57</td>
     * <td>VIEW</td>
     * </tr>
     * <tr>
     * <td>PNG format</td>
     * <td>.png</td>
     * <td>89 50 4e 47</td>
     * <td>.PNG</td>
     * </tr>
     * <tr>
     * <td>Postscript format</td>
     * <td>.[e]ps</td>
     * <td>25 21</td>
     * <td>%!</td>
     * </tr>
     * <tr>
     * <td>Sun Rasterfile</td>
     * <td>.ras</td>
     * <td>59 a6 6a 95</td>
     * <td>Y.j.</td>
     * </tr>
     * <tr>
     * <td>Targa format</td>
     * <td>.tga</td>
     * <td>xx xx xx</td>
     * <td>...</td>
     * </tr>
     * <tr>
     * <td>TIFF format (Motorola - big endian)</td>
     * <td>.tif</td>
     * <td>4d 4d 00 2a</td>
     * <td>MM.*</td>
     * </tr>
     * <tr>
     * <td>TIFF format (Intel - little endian)</td>
     * <td>.tif</td>
     * <td>49 49 2a 00</td>
     * <td>II*.</td>
     * </tr>
     * <tr>
     * <td>X11 Bitmap format</td>
     * <td>.xbm</td>
     * <td>xx xx</td>
     * <td>..</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>XCF Gimp file structure</td>
     * <td>.xcf</td>
     * <td>67 69 6d 70 20 78 63 66 20 76</td>
     * <td>gimp xcf</td>
     * </tr>
     * <tr>
     * <td>Xfig format</td>
     * <td>.fig</td>
     * <td>23 46 49 47</td>
     * <td>#FIG</td>
     * </tr>
     * <tr>
     * <td>XPM format</td>
     * <td>.xpm</td>
     * <td>2f 2a 20 58 50 4d 20 2a 2f</td>
     * <td>/&#042; XPM &#042;/</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param bytes The starting image bytes; at least with a length of ten to reliably detect MIME type
     * @return The image's MIME type or <code>"application/octet-stream"</code> if unknown
     */
    public static String getMimeType(final byte[] bytes) {
        return getMimeType(bytes, 0, bytes.length);
    }

    /**
     * <h2><a href="http://www.astro.keele.ac.uk/oldusers/rno/Computing/File_magic.html">Image files</a></h2>
     * <table border="1">
     * <tbody>
     * <tr>
     * <th>File type</th>
     * <th>Typical <br>
     * extension</th>
     * <th>Hex digits<br>
     * xx = variable</th>
     * <th>Ascii digits<br>
     * . = not an ascii char</th>
     * </tr>
     * <tr>
     * <td>Bitmap format</td>
     * <td>.bmp</td>
     * <td>42 4d</td>
     * <td>BM</td>
     * </tr>
     * <tr>
     * <td>FITS format</td>
     * <td>.fits</td>
     * <td>53 49 4d 50 4c 45</td>
     * <td>SIMPLE</td>
     * </tr>
     * <tr>
     * <td>GIF format</td>
     * <td>.gif</td>
     * <td>47 49 46 38</td>
     * <td>GIF8</td>
     * </tr>
     * <tr>
     * </tr>
     * <tr>
     * <td>Graphics Kernel System</td>
     * <td>.gks</td>
     * <td>47 4b 53 4d</td>
     * <td>GKSM</td>
     * </tr>
     * <tr>
     * <td>IRIS rgb format</td>
     * <td>.rgb</td>
     * <td>01 da</td>
     * <td>..</td>
     * </tr>
     * <tr>
     * <td>ITC (CMU WM) format</td>
     * <td>.itc</td>
     * <td>f1 00 40 bb</td>
     * <td>....</td>
     * </tr>
     * <tr>
     * <td>JPEG File Interchange Format</td>
     * <td>.jpg</td>
     * <td>ff d8 ff e0</td>
     * <td>....</td>
     * </tr>
     * <tr>
     * <td>NIFF (Navy TIFF)</td>
     * <td>.nif</td>
     * <td>49 49 4e 31</td>
     * <td>IIN1</td>
     * </tr>
     * <tr>
     * <td>PM format</td>
     * <td>.pm</td>
     * <td>56 49 45 57</td>
     * <td>VIEW</td>
     * </tr>
     * <tr>
     * <td>PNG format</td>
     * <td>.png</td>
     * <td>89 50 4e 47</td>
     * <td>.PNG</td>
     * </tr>
     * <tr>
     * <td>Postscript format</td>
     * <td>.[e]ps</td>
     * <td>25 21</td>
     * <td>%!</td>
     * </tr>
     * <tr>
     * <td>Sun Rasterfile</td>
     * <td>.ras</td>
     * <td>59 a6 6a 95</td>
     * <td>Y.j.</td>
     * </tr>
     * <tr>
     * <td>Targa format</td>
     * <td>.tga</td>
     * <td>xx xx xx</td>
     * <td>...</td>
     * </tr>
     * <tr>
     * <td>TIFF format (Motorola - big endian)</td>
     * <td>.tif</td>
     * <td>4d 4d 00 2a</td>
     * <td>MM.*</td>
     * </tr>
     * <tr>
     * <td>TIFF format (Intel - little endian)</td>
     * <td>.tif</td>
     * <td>49 49 2a 00</td>
     * <td>II*.</td>
     * </tr>
     * <tr>
     * <td>X11 Bitmap format</td>
     * <td>.xbm</td>
     * <td>xx xx</td>
     * <td>..</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>XCF Gimp file structure</td>
     * <td>.xcf</td>
     * <td>67 69 6d 70 20 78 63 66 20 76</td>
     * <td>gimp xcf</td>
     * </tr>
     * <tr>
     * <td>Xfig format</td>
     * <td>.fig</td>
     * <td>23 46 49 47</td>
     * <td>#FIG</td>
     * </tr>
     * <tr>
     * <td>XPM format</td>
     * <td>.xpm</td>
     * <td>2f 2a 20 58 50 4d 20 2a 2f</td>
     * <td>/&#042; XPM &#042;/</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param bytes The starting image bytes; at least with a length of ten to reliably detect MIME type
     * @param off The offset within byte array
     * @param len The length of valid bytes starting from offset
     * @return The image's MIME type or <code>"application/octet-stream"</code> if unknown
     */
    public static String getMimeType(final byte[] bytes, final int off, final int len) {
        if (bytes == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > bytes.length - off) {
            throw new IndexOutOfBoundsException();
        }
        // Check image MIME type
        if (startsWith(PREFIX_JPEG, bytes, off, len) || startsWith(PREFIX_JPEG2, bytes, off, len) ||
            startsWith(PREFIX_JPEG3, bytes, off, len)) {
            return "image/jpeg";
        }
        if (startsWith(PREFIX_BITMAP, bytes, off, len)) {
            return "image/x-ms-bmp";
        }
        if (startsWith(PREFIX_PNG, bytes, off, len)) {
            return "image/png";
        }
        if (startsWith(PREFIX_GIF, bytes, off, len)) {
            return "image/gif";
        }
        if (startsWith(PREFIX_XPM, bytes, off, len)) {
            return "image/x-xpixmap";
        }
        if (startsWith(PREFIX_TIFF_BIGENDIAN, bytes, off, len)) {
            return "image/tiff";
        }
        if (startsWith(PREFIX_TIFF_LITTLEENDIAN, bytes, off, len)) {
            return "image/tiff";
        }
        if (startsWith(PREFIX_POSTSCRIPT, bytes, off, len)) {
            return "application/postscript";
        }
        if (startsWith(PREFIX_FITS, bytes, off, len)) {
            return "image/fits";
        }
        if (startsWith(PREFIX_GKSM, bytes, off, len)) {
            return "image/gks";
        }
        if (startsWith(PREFIX_IRIS, bytes, off, len)) {
            return "image/x-rgb";
        }
        if (startsWith(PREFIX_ITC, bytes, off, len)) {
            return "image/itc";
        }
        if (startsWith(PREFIX_NIFF, bytes, off, len)) {
            return "image/niff";
        }
        if (startsWith(PREFIX_PM, bytes, off, len)) {
            return "image/x-portable-anymap";
        }
        if (startsWith(PREFIX_SUN_RASTERFILE, bytes, off, len)) {
            return "image/x-cmu-raste";
        }
        if (startsWith(PREFIX_XCF_GIMP, bytes, off, len)) {
            return "image/xcf";
        }
        if (startsWith(PREFIX_XFIG, bytes, off, len)) {
            return "image/fig";
        }
        return "application/octet-stream";
    }

    private static boolean startsWith(final byte[] prefix, final byte[] bytes, final int off, final int len) {
        int pc = prefix.length;
        if (len < pc) {
            return false;
        }
        if (off > 0) {
            while (--pc >= 0) {
                if (bytes[off + pc] != prefix[pc]) {
                    return false;
                }
            }
        } else {
            while (--pc >= 0) {
                if (bytes[pc] != prefix[pc]) {
                    return false;
                }
            }
        }
        return true;
    }

}
