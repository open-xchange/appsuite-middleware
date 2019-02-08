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
 *    trademarks of the OX Software GmbH. group of companies.
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
            Dimension result = getDimensionFromHeifFile(bufferedInputStream);
            if(result != null) {
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
                    } catch (final Exception e) {
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
            Directory heifDirectory = metadata.getFirstDirectoryOfType(HeifDirectory.class);
            if (heifDirectory != null) {

                long width = -1;
                Long longObject = heifDirectory.getLongObject(HeifDirectory.TAG_IMAGE_WIDTH);
                if (null != longObject) {
                    width = longObject.longValue();
                }
                long height = -1;
                longObject = heifDirectory.getLongObject(HeifDirectory.TAG_IMAGE_HEIGHT);
                if (null != longObject) {
                    height = longObject.longValue();
                }

                return new Dimension(Math.toIntExact(width), Math.toIntExact(height));
            }
            // Don't contain a heif directory -> fall back to normal handling
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
            if(result != null) {
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
                    } catch (final Exception e) {
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
     * Checks whether the given image is a heif file and uses {@link ImageMetadataReader} to get the metadata
     * 
     * @param bufferedInputStream The stream containing the image
     * @param imageMetadataOptions The {@link ImageMetadataOptions}
     * @return The {@link ImageMetadata} or null
     * @throws IOException
     * @throws ImageProcessingException
     */
    private ImageMetadata getMetadataFromHeifFile(BufferedInputStream bufferedInputStream, ImageMetadataOptions imageMetadataOptions) throws IOException {
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
            Directory heifDirectory = metadata.getFirstDirectoryOfType(HeifDirectory.class);
            if (heifDirectory != null) {
                long width = -1;
                Long longObject = heifDirectory.getLongObject(HeifDirectory.TAG_IMAGE_WIDTH);
                if (null != longObject) {
                    width = longObject.longValue();
                }
                long height = -1;
                longObject = heifDirectory.getLongObject(HeifDirectory.TAG_IMAGE_HEIGHT);
                if (null != longObject) {
                    height = longObject.longValue();
                }

                ImageMetadata.Builder builder = ImageMetadata.builder();
                if (imageMetadataOptions.isDimension()) {
                    builder.withDimension(new Dimension(Math.toIntExact(width), Math.toIntExact(height)));
                }

                if (imageMetadataOptions.isFormatName()) {
                    builder.withFormatName(HEIF_FILE_FORMAT);
                }

                return builder.build();
            }
            // Don't contain a heif directory -> reset stream and return null
            bufferedInputStream.reset();
        }
        return null;
    }

}
