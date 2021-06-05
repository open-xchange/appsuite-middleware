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

package com.openexchange.imagetransformation.java.services;

import static com.openexchange.imagetransformation.java.transformations.Utils.getImageInputStream;
import static com.openexchange.imagetransformation.java.transformations.Utils.getImageReader;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.HeifDirectory;
import com.openexchange.imagetransformation.ImageMetadata;
import com.openexchange.imagetransformation.ImageMetadataOptions;
import com.openexchange.imagetransformation.ImageMetadataService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;


/**
 * {@link JavaImageMetadataService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class JavaImageMetadataService implements ImageMetadataService {

    private static final String HEIF_FILE_FORMAT = "heif";

    /**
     * Initializes a new {@link JavaImageMetadataService}.
     */
    public JavaImageMetadataService() {
        super();
    }

    @Override
    public Dimension getDimensionFor(InputStream imageStream, String mimeType, String name) throws IOException {
        BufferedInputStream bufferedInputStream = imageStream instanceof BufferedInputStream ? (BufferedInputStream) imageStream : new BufferedInputStream(imageStream, 65536);
        try {
            // Check for heif file
            bufferedInputStream.mark(65536);
            Dimension result = getDimensionFromHeifFile(bufferedInputStream);
            if (result != null) {
                return result;
            }

            // Using ImageIO to read metadata
            ImageInputStream imageInputStream = null;
            ImageReader reader = null;
            try {
                imageInputStream = getImageInputStream(bufferedInputStream);
                reader = getImageReader(imageInputStream, mimeType, name);
                reader.setInput(imageInputStream);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                return new Dimension(width, height);
            } finally {
                if (null != reader) {
                    try {
                        reader.dispose();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                Streams.close(imageInputStream);
            }
        } finally {
            Streams.close(bufferedInputStream);
        }
    }

    /**
     * Checks whether the given image is a heif file and uses {@link ImageMetadataReader} to get the dimension
     *
     * @param bufferedInputStream The stream containing the image
     * @return The {@link Dimension} or null
     * @throws IOException
     */
    private Dimension getDimensionFromHeifFile(BufferedInputStream bufferedInputStream) throws IOException {
        // Use ImageMetadataReader if the image is a heif file
        FileType detectedFileType;
        try {
            detectedFileType = FileTypeDetector.detectFileType(bufferedInputStream);
        } catch (AssertionError e) {
            detectedFileType = FileType.Unknown;
        }
        bufferedInputStream.reset();
        if (FileType.Heif == detectedFileType) {
            Metadata metadata;
            try {
                metadata = ImageMetadataReader.readMetadata(bufferedInputStream, -1, detectedFileType);
            } catch (ImageProcessingException e) {
                throw new IOException(e.getMessage(), e);
            }

            // extract width and height from available HeifDirectories
            final Dimension dimension = new Dimension(0, 0);
            Long longObject;
            for (final Directory curDir : metadata.getDirectoriesOfType(HeifDirectory.class)) {
                if (null != (longObject = curDir.getLongObject(HeifDirectory.TAG_IMAGE_WIDTH))) {
                    dimension.width = Math.toIntExact(longObject.longValue());
                }

                if (null != (longObject = curDir.getLongObject(HeifDirectory.TAG_IMAGE_HEIGHT))) {
                    dimension.height = Math.toIntExact(longObject.longValue());
                }

                // return if an assignment for width and height was successful
                if ((0 != dimension.width) && (0 != dimension.height)) {
                    return dimension;
                }
            }
            // Doesn't contain a heif directory -> fall back to normal handling
            bufferedInputStream.reset();
        }
        return null;
    }

    @Override
    public ImageMetadata getMetadataFor(InputStream imageStream, String mimeType, String name, ImageMetadataOptions imageMetadataOptions) throws IOException {
        BufferedInputStream bufferedInputStream = imageStream instanceof BufferedInputStream ? (BufferedInputStream) imageStream : new BufferedInputStream(imageStream, 65536);
        try {
            // Check for heif file
            ImageMetadata result = getMetadataFromHeifFile(bufferedInputStream, imageMetadataOptions);
            if (result != null) {
                return result;
            }

            // Using ImageIO to read metadata
            ImageInputStream imageInputStream = null;
            ImageReader reader = null;
            try {
                imageInputStream = getImageInputStream(bufferedInputStream);
                reader = getImageReader(imageInputStream, mimeType, name);
                reader.setInput(imageInputStream);
                ImageMetadata.Builder builder = ImageMetadata.builder();
                if (imageMetadataOptions.isDimension()) {
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    builder.withDimension(new Dimension(width, height));
                }

                if (imageMetadataOptions.isFormatName()) {
                    String formatName = reader.getFormatName();
                    if (null != formatName) {
                        builder.withFormatName(Strings.asciiLowerCase(formatName));
                    }
                }

                return builder.build();
            } finally {
                if (null != reader) {
                    try {
                        reader.dispose();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                Streams.close(imageInputStream);
            }
        } finally {
            Streams.close(bufferedInputStream);
        }

    }

    /**
     * Gets the image metadata from the given file, if it is a heif file.
     *
     * @param bufferedInputStream The stream containing the image
     * @param imageMetadataOptions The options to consider when retrieving image's meta-data
     * @return The {@link ImageMetadata} if it is a heif file, null otherwise
     * @throws IOException
     */
    private ImageMetadata getMetadataFromHeifFile(BufferedInputStream bufferedInputStream, ImageMetadataOptions imageMetadataOptions) throws IOException {
        Dimension dimension = getDimensionFromHeifFile(bufferedInputStream);
        if (null != dimension) {
            ImageMetadata.Builder builder = ImageMetadata.builder();
            if (imageMetadataOptions.isDimension()) {
                builder.withDimension(dimension);
            }

            if (imageMetadataOptions.isFormatName()) {
                builder.withFormatName(HEIF_FILE_FORMAT);
            }

            return builder.build();
        }
        return null;
    }

}
