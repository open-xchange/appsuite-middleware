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

package com.openexchange.tools;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.java.Streams;

/**
 * {@link ImageTypeDetector} - Detects MIME type of passed image bytes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageTypeDetector {

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
        if (null == bytes) {
            return "application/octet-stream";
        }
        return com.openexchange.java.ImageTypeDetector.getMimeType(bytes);
    }

    /**
     * Detects MIME type of passed image bytes.
     *
     * @param binary The image bytes
     * @return The image's MIME type or <code>"application/octet-stream"</code> if unknown
     * @throws IOException If an I/O error occurs
     */
    public static String getMimeType(final InputStream binary) throws IOException {
        if (null == binary) {
            return "application/octet-stream";
        }
        try {
            byte[] buf = new byte[32];
            final int read = binary.read(buf, 0, 32);
            if (0 >= read) {
                return "application/octet-stream";
            }
            if (read < 32) {
                byte[] tmp = new byte[read];
                System.arraycopy(buf, 0, tmp, 0, read);
                buf = tmp;
            }
            return com.openexchange.java.ImageTypeDetector.getMimeType(buf);
        } finally {
            Streams.close(binary);
        }
    }
}
