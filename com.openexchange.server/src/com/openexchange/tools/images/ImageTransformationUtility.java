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

package com.openexchange.tools.images;

import static com.openexchange.java.Strings.toLowerCase;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.java.ByteArrayBufferedInputStream;
import com.openexchange.java.FastBufferedInputStream;
import com.openexchange.java.Streams;
import com.openexchange.tools.images.transformations.RotateTransformation;


/**
 * {@link ImageTransformationUtility} - Utility class for image transformation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ImageTransformationUtility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ImageTransformationUtility.class);

    /**
     * Initializes a new {@link ImageTransformationUtility}.
     */
    private ImageTransformationUtility() {
        super();
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------

    private static final String COVER = com.openexchange.tools.images.ScaleType.COVER.getKeyword();

    /**
     * Check if given AJAX request data seems to be a thumbnail request.
     *
     * @param requestData The AJAX request data
     * @return <code>true</code> if AJAX request data seems to be a thumbnail request; otherwise <code>false</code>
     */
    public static boolean seemsLikeThumbnailRequest(AJAXRequestData requestData) {
        return COVER.equals(requestData.getParameter("scaleType")) || "thumbnail_image".equals(requestData.getFormat());
    }

    /**
     * Check if given HTTP request seems to be a thumbnail request.
     *
     * @param request The HTTP request
     * @return <code>true</code> if HTTP request seems to be a thumbnail request; otherwise <code>false</code>
     */
    public static boolean seemsLikeThumbnailRequest(HttpServletRequest request) {
        return COVER.equals(request.getParameter("scaleType")) || "thumbnail_image".equals(request.getParameter("format"));
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
     * Gets a value indicating whether an image format can be written or not.
     *
     * @param formatName The image format name
     * @return <code>true</code> if the image format can be written, <code>false</code>, otherwise
     */
//    private static boolean canWrite(String formatName) {
//        String tmp = toLowerCase(formatName);
//        if ("vnd.microsoft.icon".equals(tmp) || "x-icon".equals(tmp)) {
//            return false;
//        }
//        for (String writerFormatName : ImageIO.getWriterFormatNames()) {
//            if (toLowerCase(writerFormatName).equals(tmp)) {
//                return true;
//            }
//        }
//        return false;
//    }

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

    /**
     * Extracts image information from the supplied metadata.
     *
     * @param metadata The metadata to extract the image information
     * @return The image information, or <code>null</code> if none could be extracted
     */
    public static ImageInformation getImageInformation(Metadata metadata) {
        if (null == metadata) {
            return null;
        }
        int orientation = 1;
        int width = 0;
        int height = 0;
        try {
            Directory directory = metadata.getDirectory(ExifIFD0Directory.class);
            if (null != directory) {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
            JpegDirectory jpegDirectory = metadata.getDirectory(JpegDirectory.class);
            if (null != jpegDirectory) {
                width = jpegDirectory.getImageWidth();
                height = jpegDirectory.getImageHeight();
            }
        } catch (MetadataException e) {
            LOG.debug("Unable to retrieve image information.", e);
            return null;
        }
        return new ImageInformation(orientation, width, height);
    }

    /**
     * Checks if rotate transformation is required.
     *
     * @param in The image input stream to check
     * @return <code>true</code> if rotate transformation is required; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     */
    public static boolean requiresRotateTransformation(InputStream in) throws IOException {
        if (null == in) {
            return false;
        }
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(bufferedInputStreamFor(in), false);
            ImageInformation imageInformation = getImageInformation(metadata);
            return RotateTransformation.getInstance().needsRotation(imageInformation);
        } catch (ImageProcessingException e) {
            LOG.debug("error getting metadata.", e);
        } finally {
        	Streams.close(in);
        }

        // RotateTransformation does nothing if 'ImageInformation' is absent
        return false;
    }

}
