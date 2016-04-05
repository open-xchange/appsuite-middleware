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

package com.openexchange.imagetransformation.java.transformations;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.java.exif.ExifTool;
import com.openexchange.imagetransformation.java.exif.Orientation;
import com.openexchange.tools.images.ImageTransformationUtility;
import com.twelvemonkeys.imageio.stream.ByteArrayImageInputStream;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class Utils {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Utils.class);

    /**
     * Removes the transparency from the given image if necessary, i.e. the color model has an alpha channel and the supplied image
     * format is supposed to not support transparency.
     *
     * @param image The image
     * @param formatName The image format name, e.g. "jpeg" or "tiff"
     * @return The processed buffered image, or the previous image if no processing was necessary
     */
    public static BufferedImage removeTransparencyIfNeeded(BufferedImage image, String formatName) {
        if (null != image && null != formatName && false == ImageTransformationUtility.supportsTransparency(formatName)) {
            ColorModel colorModel = image.getColorModel();
            if (null != colorModel && colorModel.hasAlpha()) {
                BufferedImage targetImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = targetImage.createGraphics();
                graphics.drawImage(image, 0, 0, Color.WHITE, null);
                graphics.dispose();
                return targetImage;
            }
        }
        return image;
    }

    /**
     * Gets the {@code InputStream} from specified image file.
     *
     * @param imageFile The image file
     * @return The input stream
     * @throws IOException If input stream cannot be returned
     */
    public static InputStream getFileStream(IFileHolder imageFile) throws IOException {
        if (null == imageFile) {
            return null;
        }
        try {
            return imageFile.getStream();
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
        }
    }

    /**
     * Gets the {@code ImageInputStream} from specified image file.
     *
     * @param imageFile The image file
     * @return The image input stream
     * @throws IOException If input stream cannot be returned
     */
    public static ImageInputStream getImageInputStream(IFileHolder imageFile) throws IOException {
        try {
            /*
             * prefer 'optimized' image input streams for threshold file holders
             */
            if (ThresholdFileHolder.class.isInstance(imageFile)) {
                ThresholdFileHolder fileHolder = (ThresholdFileHolder) imageFile;
                if (fileHolder.isInMemory()) {
                    return new ByteArrayImageInputStream(fileHolder.toByteArray());
                }
                return new FileImageInputStream(fileHolder.getTempFile());
            }
            /*
             * fallback to default spi-based image input stream instantiation
             */
            return ImageIO.createImageInputStream(imageFile.getStream());
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw null == cause ? new IOException(e.getMessage(), e) : new IOException(cause.getMessage(), cause);
        }
    }

    /**
     * Tries to read out the Exif orientation of an image using the supplied image reader.
     *
     * @param reader The image reader to use
     * @param imageIndex The image index
     * @return The Exif orientation, or <code>0</code> if not available
     */
    public static int readExifOrientation(ImageReader reader, int imageIndex) {
        try {
            String formatName = reader.getFormatName();
            if ("jpeg".equalsIgnoreCase(formatName) || "jpg".equalsIgnoreCase(formatName)) {
                Orientation orientatation = ExifTool.readOrientation(reader, imageIndex);
                if (null != orientatation) {
                    return orientatation.getValue();
                }
            }
        } catch (Exception e) {
            LOG.debug("error reading Exif orientation", e);
        }
        return 0;
    }

    /**
     * Determines the required source resolution to fulfill one or more image transformation operations.
     *
     * @param transformations The transformations to inspect
     * @param originalWidth The original image's width
     * @param originalHeight The original image's height
     * @return The required resolution, or <code>null</code> if not relevant
     */
    public static Dimension getRequiredResolution(List<ImageTransformation> transformations, int originalWidth, int originalHeight) {
        Dimension originalResolution = new Dimension(originalWidth, originalHeight);
        Dimension requiredResolution = null;
        for (ImageTransformation transformation : transformations) {
            Dimension resolution = transformation.getRequiredResolution(originalResolution);
            if (null == requiredResolution) {
                requiredResolution = resolution;
            } else if (null != resolution) {
                if (requiredResolution.height < resolution.height) {
                    requiredResolution.height = resolution.height;
                }
                if (requiredResolution.width < resolution.width) {
                    requiredResolution.width = resolution.width;
                }
            }
        }
        return requiredResolution;
    }

    /**
     * Selects the most appropriate image index for the required target image resolution from the images available in the supplied image reader.
     *
     * @param reader The image reader to get the most appropriate image index for
     * @param requiredResolution The required resolution for the target image
     * @param maxResolution The max. resolution for an image or less than/equal to 0 (zero) for no resolution limitation
     * @return The image index, or <code>0</code> for the default image
     */
    public static int selectImage(ImageReader reader, Dimension requiredResolution, long maxResolution) {
        try {
            int numImages = reader.getNumImages(false);
            if (1 < numImages) {
                Dimension selectedResolution = new Dimension(reader.getWidth(0), reader.getHeight(0));
                int selectedIndex = 0;
                for (int i = 1; i < numImages; i++) {
                    Dimension resolution = new Dimension(reader.getWidth(i), reader.getHeight(i));
                    if (1 == selectResolution(selectedResolution, resolution, requiredResolution, maxResolution)) {
                        selectedIndex = i;
                        selectedResolution = resolution;
                    }
                }
                return selectedIndex;
            }
        } catch (IOException e) {
            LOG.debug("Error determining most appropriate image index", e);
        }
        return 0;
    }

    /**
     * Selects the most appropriate resolution to match a the target resolution.
     *
     * @param resolution1 The first candidate
     * @param resolution2 The second candidate
     * @param requiredResolution The required resolution
     * @param maxResolution The max. resolution for an image or less than/equal to 0 (zero) for no resolution limitation
     * @return <code>-1</code> if the first candidate was selected, <code>1</code> for the second one
     */
    private static int selectResolution(Dimension resolution1, Dimension resolution2, Dimension requiredResolution, long maxResolution) {
        if (0 < maxResolution) {
            if (resolution1.width * resolution1.height <= maxResolution) {
                if (resolution2.width * resolution2.height > maxResolution) {
                    /*
                     * only first resolution fulfills max. resolution constraint
                     */
                    return -1;
                }
            } else if (resolution2.width * resolution2.height <= maxResolution) {
                /*
                 * only second resolution fulfills max. resolution constraint
                 */
                return 1;
            }
        }
        if (resolution1.width >= requiredResolution.width && resolution1.height >= requiredResolution.height) {
            if (resolution2.width >= requiredResolution.width && resolution2.height >= requiredResolution.height) {
                /*
                 * both resolutions fulfill required resolution, choose closest one
                 */
                return resolution1.width * resolution1.height > resolution2.width * resolution2.height ? 1 : -1;
            } else {
                /*
                 * only first resolution fulfills required resolution
                 */
                return -1;
            }
        } else if (resolution2.width >= requiredResolution.width && resolution2.height >= requiredResolution.height) {
            /*
             * only second resolution fulfills required resolution
             */
            return 1;
        } else {
            /*
             * no resolution fulfills required resolution, choose closest one
             */
            return resolution1.width * resolution1.height >= resolution2.width * resolution2.height ? -1 : 1;
        }
    }

}
