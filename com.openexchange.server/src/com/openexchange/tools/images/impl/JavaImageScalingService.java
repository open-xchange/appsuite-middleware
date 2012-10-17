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

package com.openexchange.tools.images.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
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
import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleOp;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.log.LogFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.images.ImageScalingService;
import com.openexchange.tools.images.ScaleType;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link JavaImageScalingService}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class JavaImageScalingService implements ImageScalingService {

    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(JavaImageScalingService.class));

    private static final String CT_JPEG = "image/jpeg";

    private static final String CT_JPG = "image/jpg";

    private static final String CT_TIFF = "image/tiff";

    @Override
    public InputStream scale(InputStream pictureData, int maxWidth, int maxHeight, ScaleType scaleType) throws IOException {
        if (null == pictureData) {
            throw new IOException("pictureData == null!");
        }
        final BufferedImage image;
        try {
            image = ImageIO.read(pictureData);
        } finally {
            Streams.close(pictureData);
        }

        DimensionConstrain constrain;
        switch (scaleType) {
        case COVER:
            constrain = new CoverDimensionConstrain(maxWidth, maxHeight);
            break;
        case CONTAIN:
            constrain = new ContainDimensionConstrain(maxWidth, maxHeight);
            break;
        default:
            constrain = new AutoDimensionConstrain(maxWidth, maxHeight);
            break;
        }
        ResampleOp op = new ResampleOp(constrain);

        BufferedImage scaled = op.filter(image, null);

        UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(8192);
        
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(0.9f);
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(baos);
        writer.setOutput(imageOutputStream);
        IIOImage iioImage = new IIOImage(scaled, null, null);
        writer.write(null, iioImage, iwp);
        writer.dispose();

        try {
            return Streams.newByteArrayInputStream(baos.toByteArray());
        } finally {
            imageOutputStream.close();
            baos.close();
        }
    }

    @Override
    public InputStream rotateAccordingExif(InputStream pictureData, String contentType) throws IOException, OXException {
        String fileType;
        {
            final String lcct = null == contentType ? "" : contentType.toLowerCase(Locale.ENGLISH);
            if (lcct.startsWith(CT_JPEG)) {
                fileType = "jpeg";
            } else if (lcct.startsWith(CT_JPG)) {
                fileType = "jpg";
            } else if (lcct.startsWith(CT_TIFF)) {
                fileType = "tiff";
            } else {
                return pictureData;
            }
        }
        if (null == pictureData) {
            return pictureData;
        }
        ManagedFile managedFile = null;
        try {
            ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            managedFile = mfm.createManagedFile(pictureData);
            ImageInformation imageInformation = readImageInformation(managedFile.getInputStream());
            if (imageInformation == null) {
                return Streams.newByteArrayInputStream(managedFile.getInputStream());
            }
   
            AffineTransform exifTransformation = getExifTransformation(imageInformation);
            if (exifTransformation == null) {
                return Streams.newByteArrayInputStream(managedFile.getInputStream());
            }
   
            AffineTransformOp op = new AffineTransformOp(exifTransformation, AffineTransformOp.TYPE_BICUBIC);
            BufferedImage image = ImageIO.read(managedFile.getInputStream());
            ColorModel cm = (image.getType() == BufferedImage.TYPE_BYTE_GRAY) ? image.getColorModel() : null;
            BufferedImage destinationImage = op.createCompatibleDestImage(image, cm);
            Graphics2D g = destinationImage.createGraphics();
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
            destinationImage = op.filter(image, destinationImage);
   
            UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
            if (!ImageIO.write(destinationImage, fileType, baos)) {
                throw new IOException("Couldn't rotate image");
            }
   
            return new ByteArrayInputStream(baos.toByteArray());
        } finally {
            if (managedFile != null) {
                managedFile.delete();
            }
        }
    }

	@Override
	public InputStream crop(InputStream pictureData, int x, int y, int width, int height, String contentType) throws IOException {
		/*
		 * read source image
		 */
    	BufferedImage sourceImage = ImageIO.read(pictureData);
    	/*
    	 * get cropped image
    	 */
    	BufferedImage targetImage = crop(sourceImage, x, y, width, height);
		/*
		 * write back to output stream
		 */    		
		ByteArrayOutputStream outputStream = new UnsynchronizedByteArrayOutputStream();
		if (false == ImageIO.write(targetImage, getImageFormat(contentType), outputStream)) {
			throw new IOException("Couldn't write cropped image");
		}
		outputStream.flush();
		return new ByteArrayInputStream(outputStream.toByteArray());
	}

	@Override
	public BufferedImage crop(BufferedImage sourceImage, int x, int y, int width, int height) throws IOException {
    	/*
    	 * prepare target image
    	 */
    	BufferedImage targetImage = null; 
    	if (0 <= x && sourceImage.getWidth() > x && sourceImage.getWidth() >= x + width &&
    			0 <= y && sourceImage.getHeight() > y && sourceImage.getHeight() >= y + height) {
    		/*
    		 * extract sub-image directly
    		 */
    		targetImage = sourceImage.getSubimage(x, y, width, height);
    	} else {
    		/*
    		 * draw partial region to target image
    		 */
        	targetImage = new BufferedImage(width, height, sourceImage.getType());
    		Graphics2D graphics = targetImage.createGraphics();
    		graphics.setBackground(new Color(255, 255, 255, 0));
    		graphics.clearRect(0, 0, width, height);
    		graphics.drawImage(sourceImage, x, y, null);
    	}
    	return targetImage;
	}
	
	private static String getImageFormat(String contentType) {
		return null != contentType && contentType.toLowerCase().startsWith("image/") ? 
				contentType.substring(6) : contentType;	
	}

    private AffineTransform getExifTransformation(ImageInformation info) {
        AffineTransform t = new AffineTransform();

        switch (info.orientation) {
        default:
        case 1:
            return null;
        case 2:
            t.scale(-1.0, 1.0);
            t.translate(-info.width, 0);
            break;
        case 3:
            t.translate(info.width, info.height);
            t.rotate(Math.PI);
            break;
        case 4:
            t.scale(1.0, -1.0);
            t.translate(0, -info.height);
            break;
        case 5:
            t.rotate(-Math.PI / 2);
            t.scale(-1.0, 1.0);
            break;
        case 6:
            t.translate(info.height, 0);
            t.rotate(Math.PI / 2);
            break;
        case 7:
            t.scale(-1.0, 1.0);
            t.translate(-info.height, 0);
            t.translate(0, info.width);
            t.rotate(3 * Math.PI / 2);
            break;
        case 8:
            t.translate(0, info.width);
            t.rotate(3 * Math.PI / 2);
            break;
        }
        return t;
    }

    public ImageInformation readImageInformation(InputStream imageFile) throws IOException {
        int orientation = 1;
        int width = 0;
        int height = 0;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new BufferedInputStream(imageFile), false);
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
        } catch (ImageProcessingException e) {
            LOG.debug("Unable to retrieve image information.", e);
            return null;
        }

        return new ImageInformation(orientation, width, height);
    }

    private class ImageInformation {

        public final int orientation;

        public final int width;

        public final int height;

        public ImageInformation(int orientation, int width, int height) {
            this.orientation = orientation;
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return String.format("%dx%d,%d", this.width, this.height, this.orientation);
        }
    }

}
