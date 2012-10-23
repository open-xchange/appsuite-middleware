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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.images.transformations;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.logging.Log;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.log.LogFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.images.ImageTransformations;
import com.openexchange.tools.images.ScaleType;
import com.openexchange.tools.images.impl.ImageInformation;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ImageTransformationsImpl}
 * 
 * Default {@link ImageTransformations} implementation.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ImageTransformationsImpl implements ImageTransformations {
    
    private static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ImageTransformationsImpl.class));
    
    private final InputStream sourceImageStream;
    private final List<ImageTransformation> transformations;
    private BufferedImage sourceImage;
    private Metadata metadata;    
    
    private ImageTransformationsImpl(BufferedImage sourceImage, InputStream sourceImageStream) {
        super();
        this.sourceImage = sourceImage;
        this.sourceImageStream = sourceImageStream;
        this.transformations = new ArrayList<ImageTransformation>();
    }
    
    /**
     * Initializes a new {@link ImageTransformationsImpl}.
     * 
     * @param sourceImage The source image
     */
    public ImageTransformationsImpl(BufferedImage sourceImage) {
        this(sourceImage, null);
    }    

    /**
     * Initializes a new {@link ImageTransformationsImpl}.
     * 
     * @param sourceImage The source image
     * @throws IOException 
     */
    public ImageTransformationsImpl(InputStream sourceImageStream) throws IOException {
        this(null, sourceImageStream);
    }    

    @Override
    public ImageTransformations rotate() {
        transformations.add(new RotateTransformation());
        return this;
    }

    @Override
    public ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType) {
        transformations.add(new ScaleTransformation(maxWidth, maxHeight, scaleType));
        return this;
    }

    @Override
    public ImageTransformations crop(int x, int y, int width, int height) {
        transformations.add(new CropTransformation(x, y, width, height));
        return this;
    }

    @Override
    public BufferedImage getImage() throws IOException {
        BufferedImage image = this.getSourceImage();
        if (null != sourceImage) {
            ImageInformation imageInformation = getImageInformation(this.metadata);
            for (ImageTransformation transformation : transformations) {
                image = transformation.perform(image, imageInformation);
            }
        } else {
            LOG.debug("Got no source image, skipping transformations.");
        }
        return image;
    }

    @Override
    public byte[] getBytes(String formatName) throws IOException {
        if (null == formatName || 0 == formatName.length()) {
            LOG.debug("No format name specified, falling back to 'jpeg'.");
            return write(getImage(), "jpeg");
        } else {
            return write(getImage(), getImageFormat(formatName));
        }
    }

    @Override
    public InputStream getInputStream(String formatName) throws IOException {
        return Streams.newByteArrayInputStream(getBytes(formatName));
    }
    
    /**
     * Gets the source image
     * 
     * @return The source image
     * @throws IOException
     */
    private BufferedImage getSourceImage() throws IOException {
        if (null == this.sourceImage && null != this.sourceImageStream) {
            sourceImage = needsMetadata() ? readAndExtractMetadata(sourceImageStream) : read(sourceImageStream); 
        }
        return sourceImage;
    }
    
    /**
     * Gets a value indicating whether additional metadata is required for one of the transformations or not.
     * 
     * @return <code>true</code>, if metadata is needed, <code>false</code>, otherwise
     */
    private boolean needsMetadata() {
        for (ImageTransformation transformation : transformations) {
            if (transformation.needsImageInformation()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Writes out an image into a byte-array.
     * 
     * @param image The image to write
     * @param formatName The format to use, e.g. "jpeg" or "tiff"
     * @return The image data
     * @throws IOException
     */
    private static byte[] write(BufferedImage image, String formatName) throws IOException {
        ImageWriter writer = null;
        UnsynchronizedByteArrayOutputStream baos = null;
        ImageOutputStream imageOutputStream = null;
        try {
            baos = new UnsynchronizedByteArrayOutputStream(8192);
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatName);
            if (null == iter || false == iter.hasNext()) {
                throw new IOException("No image writer for fromat " + formatName);
            }
            writer = iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            adjustWriteParams(iwp);
            imageOutputStream = ImageIO.createImageOutputStream(baos);
            writer.setOutput(imageOutputStream);
            IIOImage iioImage = new IIOImage(image, null, null);
            writer.write(null, iioImage, iwp);
            return baos.toByteArray();
        } finally {
            if (null != writer) {
                writer.dispose();
            }
            if (null != imageOutputStream) {
                imageOutputStream.close();
            }
            Streams.close(baos);
        }
    }
    
    /**
     * Tries to adjust the default settings on the supplied image write parameters, ignoring any {@link UnsupportedOperationException}s
     * that may occur. 
     * 
     * @param parameters The parameters to adjust
     */
    private static void adjustWriteParams(ImageWriteParam parameters) {
        try {
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        } catch (UnsupportedOperationException e) {
            LOG.debug(e.getMessage(), e);
        }
        try {
            parameters.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
        } catch (UnsupportedOperationException e) {
            LOG.debug(e.getMessage(), e);
        }
        try {
            parameters.setCompressionQuality(0.8f);
        } catch (UnsupportedOperationException e) {
            LOG.debug(e.getMessage(), e);
        }
    }
    
    /**
     * Reads a buffered image from the supplied stream and closes the stream afterwards.
     * 
     * @param inputStream The stream to read the image from
     * @return The buffered image
     * @throws IOException
     */
    private BufferedImage read(InputStream inputStream) throws IOException {
        try {
            return ImageIO.read(inputStream);
        } finally {
            Streams.close(inputStream);
        }
    }
    
    /**
     * Reads a buffered image from the supplied stream and closes the stream afterwards, trying to extract metadata information.
     * 
     * @param inputStream The stream to read the image from
     * @return The buffered image
     * @throws IOException
     */
    private BufferedImage readAndExtractMetadata(InputStream inputStream) throws IOException {
        ManagedFile managedFile = null;
        try {
            ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            managedFile = mfm.createManagedFile(inputStream);
            try {
                metadata = ImageMetadataReader.readMetadata(new BufferedInputStream(managedFile.getInputStream()), false);
            } catch (ImageProcessingException e) {
                LOG.warn("error getting metadata", e);
            }
            return ImageIO.read(managedFile.getInputStream());
        } catch (OXException e) {
            throw new IOException("error accessing managed file", e);
        } finally {
            if (managedFile != null) {
                managedFile.delete();
            }
        }
    }
    
    /**
     * Extracts image information from the supplied metadata.
     * 
     * @param metadata The metadata to extract the image information
     * @return The image information, or <code>null</code> if none could be extracted 
     */
    private static ImageInformation getImageInformation(Metadata metadata) {
        if (null != metadata) {
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
        } else {
            return null;
        }
    }

    /**
     * Strips a leading "image/" as well as trailing additional properties after the first ";" from the supplied value if necessary from 
     * image formats passed as content type.
     * 
     * @param value The value
     * @return The cleaned image format
     */
    private static String getImageFormat(String value) {
        if (null != value) {
            if (value.toLowerCase().startsWith("image/")) {
                value = value.substring(6);
            }
            int idx = value.indexOf(';'); 
            if (0 < idx) {
                value = value.substring(0, idx);
            }
        } 
        return value;
    }

}