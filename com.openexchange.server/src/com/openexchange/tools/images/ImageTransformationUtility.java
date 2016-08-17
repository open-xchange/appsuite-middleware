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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.psd.PsdMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.imagetransformation.ImageInformation;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.imagetransformation.Utility;
import com.openexchange.java.BoolReference;
import com.openexchange.java.Streams;


/**
 * {@link ImageTransformationUtility} - Utility class for image transformation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ImageTransformationUtility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ImageTransformationUtility.class);
    private static final int JPEG_FILE_MAGIC_NUMBER = 0xFFD8;
    private static final int MOTOROLA_TIFF_MAGIC_NUMBER = 0x4D4D;  // "MM"
    private static final int INTEL_TIFF_MAGIC_NUMBER = 0x4949;     // "II"
    private static final int PSD_MAGIC_NUMBER = 0x3842;            // "8B" note that the full magic number is 8BPS

    /**
     * Initializes a new {@link ImageTransformationUtility}.
     */
    private ImageTransformationUtility() {
        super();
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------

    private static final String COVER = ScaleType.COVER.getKeyword();

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
        return Utility.getImageFormat(value);
    }

    /**
     * Gets a value indicating whether an image format can be read or not.
     *
     * @param formatName The image format name
     * @return <code>true</code> if the image format can be read, <code>false</code>, otherwise
     */
    public static boolean canRead(String formatName) {
        return Utility.canRead(formatName);
    }

    /**
     * Gets a value indicating whether supplied image format is supposed to support transparency or not.
     *
     * @param formatName The image format name, e.g. "jpeg" or "tiff"
     * @return <code>true</code> if transparency is supported, <code>false</code>, otherwise
     */
    public static boolean supportsTransparency(String formatName) {
        return Utility.supportsTransparency(formatName);
    }

    /**
     * Returns a buffered {@link InputStream} for specified stream.
     *
     * @param in The stream
     * @return A new buffered input stream
     */
    public static BufferedInputStream bufferedInputStreamFor(InputStream in) {
        return Utility.bufferedInputStreamFor(in);
    }

    /**
     * Gets either a buffered or byte-array backed {@link InputStream} for specified stream, which is known to return <code>true</code> for {@link InputStream#markSupported() markSupported()}.
     *
     * @param in The stream
     * @return A mark-supporting input stream
     */
    public static InputStream markSupportingInputStreamFor(InputStream in) {
        return Utility.markSupportingInputStreamFor(in);
    }

    /**
     * Reads the magic number & resets specified image input stream.
     *
     * @param inputStream The input stream
     * @return The magic number or <code>-1</code> if stream is <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    public static int readMagicNumber(BufferedInputStream inputStream) throws IOException {
        return Utility.readMagicNumber(inputStream);
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
            return null != imageInformation && 1 < imageInformation.orientation;
        } catch (ImageProcessingException e) {
            LOG.debug("error getting metadata.", e);
        } finally {
            Streams.close(in);
        }

        // RotateTransformation does nothing if 'ImageInformation' is absent
        return false;
    }

    /**
     * Reads out image information from an image stream.
     *
     * @param bufferedStream The stream to read the image information from
     * @param reset Will be set to <code>true</code> if something was read and the stream should be reset
     * @return The image information, or <code>null</code> if not available
     */
    public static ImageInformation readImageInformation(BufferedInputStream bufferedStream, BoolReference reset) throws IOException {
        // Read the magic number
        int magicNumber = ImageTransformationUtility.readMagicNumber(bufferedStream);

        // Read image metadata
        try {
            if ((magicNumber & JPEG_FILE_MAGIC_NUMBER) == JPEG_FILE_MAGIC_NUMBER) {
                // This covers all JPEG files
                bufferedStream.mark(65536); // 64K mark limit
                reset.setValue(true);
                return getImageInformation(JpegMetadataReader.readMetadata(bufferedStream, false));
            } else if (magicNumber == INTEL_TIFF_MAGIC_NUMBER || magicNumber == MOTOROLA_TIFF_MAGIC_NUMBER) {
                // This covers all TIFF and camera RAW files
                // Do not read meta-data from TIFF images using 'com.drew.imaging' package as it is very inefficient
            } else if (magicNumber == PSD_MAGIC_NUMBER) {
                // This covers PSD files, which only need 26 bytes for extracting metadata
                bufferedStream.mark(32);
                reset.setValue(true);
                return getImageInformation(PsdMetadataReader.readMetadata(bufferedStream, false));
            }
        } catch (ImageProcessingException e) {
            LOG.debug("Error getting metadata", e);
        }
        return null;
    }

}
