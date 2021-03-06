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

package com.openexchange.imagetransformation;

import static com.openexchange.java.Strings.toLowerCase;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.imagetransformation.osgi.Services;
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

    // ------------------------------------------------- SETTINGS -----------------------------------------------------------------------------------

    private static volatile Integer waitTimeoutSeconds;

    /**
     * Gets the number of seconds to wait for an image transformation to complete.
     * <p>
     * If exceeded an {@link IOException} <code>"Image transformation timed out"</code> is thrown.
     *
     * @return The number of seconds to wait for an image transformation to complete
     */
    public static int waitTimeoutSeconds() {
        Integer tmp = waitTimeoutSeconds;
        if (null == tmp) {
            synchronized (Utility.class) {
                tmp = waitTimeoutSeconds;
                if (null == tmp) {
                    int defaultValue = 10;
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Integer.valueOf(configService.getIntProperty("com.openexchange.tools.images.transformations.waitTimeoutSeconds", defaultValue));
                    waitTimeoutSeconds = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Long maxSize;

    /**
     * Gets the max. number of bytes that are allowed for an image being processed.
     *
     * @return The max. number of bytes
     */
    public static long maxSize() {
        Long tmp = maxSize;
        if (null == tmp) {
            synchronized (Utility.class) {
                tmp = maxSize;
                if (null == tmp) {
                    int defaultValue = 10485760; // 10 MB
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(configService.getIntProperty("com.openexchange.tools.images.transformations.maxSize", defaultValue));
                    maxSize = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Long maxResolution;

    /**
     * Gets the max. resolution that is allowed for an image being processed.
     *
     * @return The max. number of bytes
     */
    public static long maxResolution() {
        Long tmp = maxResolution;
        if (null == tmp) {
            synchronized (Utility.class) {
                tmp = maxResolution;
                if (null == tmp) {
                    int defaultValue = 26824090; // ~ 6048x4032 (24 megapixels) + 10%
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(configService.getIntProperty("com.openexchange.tools.images.transformations.maxResolution", defaultValue));
                    maxResolution = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Float preferThumbnailThreshold;

    /**
     * Indicates the percentage threshold of original image's width/height that is preferred for its thumbnail image.
     *
     * @return The percentage threshold
     */
    public static float preferThumbnailThreshold() {
        Float tmp = preferThumbnailThreshold;
        if (null == tmp) {
            synchronized (Utility.class) {
                tmp = preferThumbnailThreshold;
                if (null == tmp) {
                    float defaultValue = 0.8f;
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    try {
                        tmp = Float.valueOf(configService.getProperty("com.openexchange.tools.images.transformations.preferThumbnailThreshold", String.valueOf(defaultValue)));
                    } catch (NumberFormatException e) {
                        LOG.warn("error parsing \"com.openexchange.tools.images.transformations.preferThumbnailThreshold\", sticking to defaults.", e);
                        tmp = Float.valueOf(defaultValue);
                    }
                    preferThumbnailThreshold = tmp;
                }
            }
        }
        return tmp.floatValue();
    }

    static {
        ImageTransformationReloadable.getInstance().addReloadable(new Reloadable() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                waitTimeoutSeconds = null;
                maxSize = null;
                maxResolution = null;
                preferThumbnailThreshold = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties(
                    "com.openexchange.tools.images.transformations.preferThumbnailThreshold",
                    "com.openexchange.tools.images.transformations.maxResolution",
                    "com.openexchange.tools.images.transformations.maxSize",
                    "com.openexchange.tools.images.transformations.waitTimeoutSeconds"
                    );
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the {@link Dimension dimension} for specified image data.
     *
     * @param imageStream The image data
     * @param mimeType The image MIME type
     * @param name The image name
     * @return The dimension
     * @throws IOException If dimension cannot be returned
     */
    public static Dimension getImageDimensionFor(InputStream imageStream, String mimeType, String name) throws IOException {
        ImageMetadataService service = Services.optService(ImageMetadataService.class);
        if (null == service) {
            throw new IOException("No such service: " + ImageMetadataService.class.getName());
        }

        return service.getDimensionFor(imageStream, mimeType, name);
    }

    /**
     * Gets the meta-data for specified image data.
     *
     * @param imageStream The image data
     * @param mimeType The image MIME type
     * @param name The image name
     * @param options The options for retrieving image's meta-data
     * @return The meta-data
     * @throws IOException If meta-data cannot be returned
     */
    public static ImageMetadata getImageMetadataFor(InputStream imageStream, String mimeType, String name, ImageMetadataOptions options) throws IOException {
        ImageMetadataService service = Services.optService(ImageMetadataService.class);
        if (null == service) {
            throw new IOException("No such service: " + ImageMetadataService.class.getName());
        }

        return service.getMetadataFor(imageStream, mimeType, name, options);
    }

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
        String val = value.trim();
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
