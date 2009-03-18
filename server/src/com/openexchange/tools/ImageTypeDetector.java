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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * {@link ImageTypeDetector} - Detects MIME type of passed image bytes.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageTypeDetector {

    /**
     * 42 4d
     */
    private static final byte[] PREFIX_BITMAP = { (byte) Integer.parseInt("42", 16), (byte) Integer.parseInt("4D", 16) };

    /**
     * 53 49 4d 50 4c 45
     */
    private static final byte[] PREFIX_FITS = {
        (byte) Integer.parseInt("53", 16), (byte) Integer.parseInt("49", 16), (byte) Integer.parseInt("4D", 16),
        (byte) Integer.parseInt("50", 16), (byte) Integer.parseInt("4C", 16), (byte) Integer.parseInt("45", 16) };

    /**
     * 47 49 46 38
     */
    private static final byte[] PREFIX_GIF = {
        (byte) Integer.parseInt("47", 16), (byte) Integer.parseInt("49", 16), (byte) Integer.parseInt("46", 16),
        (byte) Integer.parseInt("38", 16) };

    /**
     * 47 4b 53 4d
     */
    private static final byte[] PREFIX_GKSM = {
        (byte) Integer.parseInt("47", 16), (byte) Integer.parseInt("4B", 16), (byte) Integer.parseInt("53", 16),
        (byte) Integer.parseInt("4D", 16) };

    /**
     * 01 da
     */
    private static final byte[] PREFIX_IRIS = { (byte) Integer.parseInt("01", 16), (byte) Integer.parseInt("DA", 16) };

    /**
     * f1 00 40 bb
     */
    private static final byte[] PREFIX_ITC = {
        (byte) Integer.parseInt("F1", 16), (byte) Integer.parseInt("00", 16), (byte) Integer.parseInt("40", 16),
        (byte) Integer.parseInt("BB", 16) };

    /**
     * ff d8 ff e0
     */
    private static final byte[] PREFIX_JPEG = {
        (byte) Integer.parseInt("FF", 16), (byte) Integer.parseInt("D8", 16), (byte) Integer.parseInt("FF", 16),
        (byte) Integer.parseInt("E0", 16) };

    /**
     * 49 49 4e 31
     */
    private static final byte[] PREFIX_NIFF = {
        (byte) Integer.parseInt("49", 16), (byte) Integer.parseInt("49", 16), (byte) Integer.parseInt("4E", 16),
        (byte) Integer.parseInt("31", 16) };

    /**
     * 56 49 45 57
     */
    private static final byte[] PREFIX_PM = {
        (byte) Integer.parseInt("56", 16), (byte) Integer.parseInt("49", 16), (byte) Integer.parseInt("45", 16),
        (byte) Integer.parseInt("57", 16) };

    /**
     * 89 50 4e 47
     */
    private static final byte[] PREFIX_PNG = {
        (byte) Integer.parseInt("89", 16), (byte) Integer.parseInt("50", 16), (byte) Integer.parseInt("4E", 16),
        (byte) Integer.parseInt("47", 16) };

    /**
     * 25 21
     */
    private static final byte[] PREFIX_POSTSCRIPT = { (byte) Integer.parseInt("25", 16), (byte) Integer.parseInt("21", 16) };

    /**
     * 59 a6 6a 95
     */
    private static final byte[] PREFIX_SUN_RASTERFILE = {
        (byte) Integer.parseInt("59", 16), (byte) Integer.parseInt("A6", 16), (byte) Integer.parseInt("6A", 16),
        (byte) Integer.parseInt("95", 16) };

    /**
     * 4d 4d 00 2a
     */
    private static final byte[] PREFIX_TIFF_BIGENDIAN = {
        (byte) Integer.parseInt("4D", 16), (byte) Integer.parseInt("4D", 16), (byte) Integer.parseInt("00", 16),
        (byte) Integer.parseInt("2A", 16) };

    /**
     * 49 49 2a 00
     */
    private static final byte[] PREFIX_TIFF_LITTLEENDIAN = {
        (byte) Integer.parseInt("49", 16), (byte) Integer.parseInt("49", 16), (byte) Integer.parseInt("2A", 16),
        (byte) Integer.parseInt("00", 16) };

    /**
     * 67 69 6d 70 20 78 63 66 20 76
     */
    private static final byte[] PREFIX_XCF_GIMP = {
        (byte) Integer.parseInt("67", 16), (byte) Integer.parseInt("69", 16), (byte) Integer.parseInt("6D", 16),
        (byte) Integer.parseInt("70", 16), (byte) Integer.parseInt("20", 16), (byte) Integer.parseInt("78", 16),
        (byte) Integer.parseInt("63", 16), (byte) Integer.parseInt("66", 16), (byte) Integer.parseInt("20", 16),
        (byte) Integer.parseInt("76", 16) };

    /**
     * 23 46 49 47
     */
    private static final byte[] PREFIX_XFIG = {
        (byte) Integer.parseInt("23", 16), (byte) Integer.parseInt("46", 16), (byte) Integer.parseInt("49", 16),
        (byte) Integer.parseInt("47", 16) };

    /**
     * 2f 2a 20 58 50 4d 20 2a 2f
     */
    private static final byte[] PREFIX_XPM = {
        (byte) Integer.parseInt("2F", 16), (byte) Integer.parseInt("2A", 16), (byte) Integer.parseInt("20", 16),
        (byte) Integer.parseInt("58", 16), (byte) Integer.parseInt("50", 16), (byte) Integer.parseInt("4D", 16),
        (byte) Integer.parseInt("20", 16), (byte) Integer.parseInt("2A", 16), (byte) Integer.parseInt("2F", 16) };

    /**
     * Initializes a new {@link ImageTypeDetector}.
     */
    private ImageTypeDetector() {
        super();
    }

    /**
     * <h2><a name="Image">Image files</a></h2>
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
     * @return The image's MIME type or "application/octet-stream" if unknown
     */
    public static String getMimeType(final byte[] bytes) {
        if (startsWith(PREFIX_JPEG, bytes)) {
            return "image/jpeg";
        }
        if (startsWith(PREFIX_BITMAP, bytes)) {
            return "image/bmp";
        }
        if (startsWith(PREFIX_PNG, bytes)) {
            return "image/png";
        }
        if (startsWith(PREFIX_GIF, bytes)) {
            return "image/gif";
        }
        if (startsWith(PREFIX_XPM, bytes)) {
            return "image/x-xpixmap";
        }
        if (startsWith(PREFIX_TIFF_BIGENDIAN, bytes)) {
            return "image/tiff";
        }
        if (startsWith(PREFIX_TIFF_LITTLEENDIAN, bytes)) {
            return "image/tiff";
        }
        if (startsWith(PREFIX_POSTSCRIPT, bytes)) {
            return "application/postscript";
        }
        if (startsWith(PREFIX_FITS, bytes)) {
            return "image/fits";
        }
        if (startsWith(PREFIX_GKSM, bytes)) {
            return "image/gks";
        }
        if (startsWith(PREFIX_IRIS, bytes)) {
            return "image/x-rgb";
        }
        if (startsWith(PREFIX_ITC, bytes)) {
            return "image/itc";
        }
        if (startsWith(PREFIX_NIFF, bytes)) {
            return "image/niff";
        }
        if (startsWith(PREFIX_PM, bytes)) {
            return "image/x-portable-anymap";
        }
        if (startsWith(PREFIX_SUN_RASTERFILE, bytes)) {
            return "image/x-cmu-raste";
        }
        if (startsWith(PREFIX_XCF_GIMP, bytes)) {
            return "image/xcf";
        }
        if (startsWith(PREFIX_XFIG, bytes)) {
            return "image/fig";
        }
        return "application/octet-stream";
    }

    private static boolean startsWith(final byte[] prefix, final byte[] bytes) {
        if (bytes.length < prefix.length) {
            return false;
        }
        int pc = prefix.length;
        int pos = 0;
        while (--pc >= 0) {
            if (bytes[pos] != prefix[pos++]) {
                return false;
            }
        }
        return true;
    }

}
