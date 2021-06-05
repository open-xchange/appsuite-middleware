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

package com.openexchange.admin.storage.mysqlStorage;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.imagetransformation.ImageMetadataService;
import com.openexchange.imagetransformation.ImageTransformationProvider;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.java.Streams;

/**
 * {@link ContactImageUtil} - Scales contacts images
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public class ContactImageScaler {

    private final ImageMetadataService imageMetadataService;
    private final ImageTransformationProvider transformationProvider;

    /**
     * Initializes a new {@link ContactImageScaler}.
     *
     * @param imageMetadataService The {@link ImageMetadataService} to use
     * @param transformationService The {@link ImageTransformationProvider} to use
     */
    public ContactImageScaler(ImageMetadataService imageMetadataService, ImageTransformationService transformationService) {
        this.imageMetadataService = Objects.requireNonNull(imageMetadataService, "imageMetadataService must not be null");
        this.transformationProvider = Objects.requireNonNull(transformationService, "transformationService must not be null");
    }

    /**
     * Resolves the scaleTypeNumber form the configuration to an actual instance of {@link ScaleType}
     *
     * @param scaleTypeNumber The number
     * @return The corresponding ScaleType to the given number
     */
    private static ScaleType toScaleType(int scaleTypeNumber) {
        ScaleType type = ScaleType.CONTAIN;
        switch (scaleTypeNumber) {
            case 1:
                type = ScaleType.CONTAIN;
                break;
            case 2:
                type = ScaleType.COVER;
                break;
            case 3:
                type = ScaleType.AUTO;
                break;
            default:
                break;
        }
        return type;
    }

    /**
     * Whether or not image scaling should be applied by configuration
     *
     * @return <code>true</code> if <code>com.openexchange.contact.image.scaleImages</code> is set, <code>false</code> otherwise
     */
    private boolean shouldScale(ContactConfig conf) {
        return conf.getBoolean(ContactConfig.Property.SCALE_IMAGES).booleanValue();
    }

    /**
     * Scales the given image if required, i.e. <code>com.openexchange.contact.image.scaleImages</code> is set.
     *
     * @param image The image to scale
     * @param contentType The content type of the image
     * @return The scaled image if <code>com.openexchange.contact.image.scaleImages</code> is set, the original image otherwise
     * @throws StorageException
     */
    public byte[] scaleIfRequired(byte[] image, String contentType) throws StorageException {
        ContactConfig conf = ContactConfig.getInstance();
        if (shouldScale(conf)) {
            int imageWidth = Integer.parseInt(conf.getString(ContactConfig.Property.SCALED_IMAGE_WIDTH));
            int imageHeight = Integer.parseInt(conf.getString(ContactConfig.Property.SCALED_IMAGE_HEIGHT));
            int scaleTypeNumber = Integer.parseInt(conf.getString(ContactConfig.Property.SCALE_TYPE));
            return scale(image, contentType, imageWidth, imageHeight, scaleTypeNumber);
        }
        return image;
    }

    /**
     * Scales the given image data
     *
     * @param image The image data to scale
     * @param contentType The content-type of the image
     * @param maxImageWidth Defines the width of scaled contact images
     * @param maxImageHeight Defines the height of scaled contact images
     * @param scaleType Defines the scale type
     * @return The scaled image
     * @throws StorageException
     */
    private byte[] scale(byte[] image, String contentType, int maxImageWidth, int maxImageHeight, int scaleType) throws StorageException {
        return scale(image, contentType, maxImageWidth, maxImageHeight, toScaleType(scaleType));
    }

    /**
     * Scales the given image data
     *
     * @param image The image data to scale
     * @param contentType The content-type of the image
     * @param maxImageWidth Defines the width of scaled contact images
     * @param maxImageHeight Defines the height of scaled contact images
     * @param scaleType Defines the scale type
     * @return The scaled image
     * @throws StorageException
     */
    private byte[] scale(byte[] image, String contentType, int maxImageWidth, int maxImageHeight, ScaleType scaleType) throws StorageException {
        try {
            byte[] transformedImage;
            {
                String formatName = null != contentType ? contentType : "image/jpeg";

                if (null == imageMetadataService) {
                    BufferedImage originalImage = transformationProvider.transfom(image).getImage();
                    if (null == originalImage || originalImage.getWidth() <= maxImageWidth && originalImage.getHeight() <= maxImageHeight) {
                        return image;
                    }
                } else {
                    Dimension dimension = imageMetadataService.getDimensionFor(Streams.newByteArrayInputStream(image), formatName, null);
                    if (null != dimension && dimension.getWidth() <= maxImageWidth && dimension.getHeight() <= maxImageHeight) {
                        return image;
                    }
                }

                transformedImage = transformationProvider.transfom(image).rotate().scale(maxImageWidth, maxImageHeight, scaleType, true).getBytes(formatName);
            }
            if (null != transformedImage && 0 < transformedImage.length) {
                return transformedImage;
            }
            return image;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

}
