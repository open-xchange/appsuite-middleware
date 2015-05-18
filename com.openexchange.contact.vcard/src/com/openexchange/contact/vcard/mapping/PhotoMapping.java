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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.contact.vcard.mapping;

import static org.slf4j.LoggerFactory.getLogger;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.internal.VCardExceptionCodes;
import com.openexchange.contact.vcard.internal.VCardServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.images.ImageTransformationService;
import com.openexchange.tools.images.ScaleType;
import com.openexchange.tools.images.TransformedImage;
import ezvcard.VCard;
import ezvcard.parameter.ImageType;
import ezvcard.property.Photo;

/**
 * {@link PhotoMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PhotoMapping extends AbstractMapping {

    private static final String X_ABCROP_RECTANGLE = "X-ABCROP-RECTANGLE";

    /**
     * Initializes a new {@link PhotoMapping}.
     */
    public PhotoMapping() {
        super();
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters) {
        Photo photo = getFirstProperty(vCard.getPhotos());
        if (null != photo) {
            vCard.removeProperty(photo);
        }
        Photo newPhoto = exportPhoto(contact, parameters);
        if (null != newPhoto) {
            vCard.addPhoto(newPhoto);
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters) {
        Photo photo = getFirstProperty(vCard.getPhotos());
        if (null == photo) {
            contact.setImage1(null);
            contact.setImageContentType(null);
            contact.setNumberOfImages(0);
        } else {
            importPhoto(contact, photo, parameters);
        }
    }

    private static Photo exportPhoto(Contact contact, VCardParameters parameters) {
        byte[] contactImage = contact.getImage1();
        if (null != contactImage) {
            ImageType imageType = getImageType(contact.getImageContentType());
            Dimension targetDimension = null != parameters ? parameters.getPhotoScaleDimension() : null;
            TransformedImage transformedImage = null;
            try {
                transformedImage = scaleImageIfNeeded(contactImage, getFormatName(imageType), targetDimension, getSource(parameters));
            } catch (OXException e) {
                getLogger(PhotoMapping.class).error("error scaling image, falling back to unscaled image.", e);
            } catch (RuntimeException e) {
                getLogger(PhotoMapping.class).error("error scaling image, falling back to unscaled image.", e);
            }
            if (null != transformedImage) {
                Photo photo = new Photo(transformedImage.getImageData(), imageType);
                photo.addParameter(X_ABCROP_RECTANGLE, getABCropRectangle(transformedImage));
                return photo;
            } else {
                return new Photo(contactImage, imageType);
            }
        }
        return null;
    }

    private static void importPhoto(Contact contact, Photo photo, VCardParameters parameters) {
        byte[] imageData = null != photo ? photo.getData() : null;
        if (null != imageData) {
            Rectangle clipRect = extractClipRect(photo.getParameters(X_ABCROP_RECTANGLE));
            if (null != clipRect) {
                /*
                 * try to crop the image based on defined rectangular area
                 */
                try {
                    imageData = doABCrop(imageData, clipRect, getFormatName(photo.getContentType()), parameters);
                } catch (IOException e) {
                    getLogger(PhotoMapping.class).error("error cropping image, falling back to uncropped image.", e);
                } catch (OXException e) {
                    getLogger(PhotoMapping.class).error("error cropping image, falling back to uncropped image.", e);
                }
            }
        }
        if (null == imageData) {
            contact.setImage1(null);
            contact.setNumberOfImages(0);
            contact.setImageContentType(null);
        } else {
            contact.setImage1(imageData);
            contact.setNumberOfImages(1);
            contact.setImageContentType(getMimeType(photo.getContentType()));
        }
    }

    private static String getMimeType(ImageType imageType) {
        return null != imageType ? imageType.getMediaType() : ImageType.JPEG.getMediaType();
    }

    private static String getFormatName(ImageType imageType) {
        return null != imageType ? imageType.getExtension() : ImageType.JPEG.getExtension();
    }

    private static ImageType getImageType(String mimeType) {
        ImageType imageType = ImageType.find(null, mimeType, null);
        return null != imageType ? imageType : ImageType.JPEG;
    }

    /**
     * Scales a contact image to fit into the supplied target dimension.
     *
     * @param source The image data
     * @param formatName The image format name
     * @param targetDimension The maximum target dimension, or <code>null</code> to skip scaling
     * @param source The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     * @return The scaled image data, or <code>null</code> if no scaling needed
     */
    private static TransformedImage scaleImageIfNeeded(byte[] imageBytes, String formatName, Dimension targetDimension, Object source) throws OXException {
        if (null == imageBytes || null == targetDimension || 1 > targetDimension.getWidth() || 1 > targetDimension.getHeight()) {
            return null;
        }
        ImageTransformationService imageService = VCardServiceLookup.getOptionalService(ImageTransformationService.class);
        if (null == imageService) {
            getLogger(PhotoMapping.class).warn("unable to acquire image transformation service, unable to scale image");
            return null;
        }
        try {
            return imageService.transfom(imageBytes, source)
                .scale((int) targetDimension.getWidth(), (int) targetDimension.getHeight(), ScaleType.CONTAIN).getTransformedImage(formatName);
        } catch (IOException e) {
            throw VCardExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Generates a "X-ABCROP-RECTANGLE" parameter value for the supplied transformed image.
     *
     * @param transformedImage The transformed image
     * @return The parameter value
     */
    private static String getABCropRectangle(TransformedImage transformedImage) {
        StringBuilder stringBuilder = new StringBuilder(64);
        stringBuilder.append("ABClipRect_1&");
        int width = transformedImage.getWidth();
        int height = transformedImage.getHeight();
        if (width < height) {
            stringBuilder.append('-').append((height - width) / 2).append("&0&").append(height).append('&').append(height);
        } else if (width > height) {
            stringBuilder.append("0&-").append((width - height) / 2).append('&').append(width).append('&').append(width);
        } else {
            stringBuilder.append("0&0&").append(width).append('&').append(height);
        }
        if (null != transformedImage.getMD5()) {
            stringBuilder.append('&').append(Base64.encode(transformedImage.getMD5()));
        }
        return stringBuilder.toString();
    }

    /**
     * Extracts the clipping information from the supplied 'X-ABCROP-RECTANGLE' parameter values if defined. The result's 'width' and
     * 'height' properties represent the dimensions of the target image. The 'x' property is the horizontal offset to draw the source
     * image in the target image from the left border, the 'y' property is the vertical offset from the bottom.
     *
     * @param cropValues The 'X-ABCROP-RECTANGLE' parameter values
     * @return The clipping rectangle, or <code>null</code>, if not defined
     */
    private static Rectangle extractClipRect(List<String> cropValues) {
        if (null != cropValues && 0 < cropValues.size()) {
            Pattern clipRectPattern = Pattern.compile("ABClipRect_1&([-+]?\\d+?)&([-+]?\\d+?)&([-+]?\\d+?)&([-+]?\\d+?)&");
            for (String value : cropValues) {
                Matcher matcher = clipRectPattern.matcher(value);
                if (matcher.find()) {
                    try {
                        int offsetLeft = Integer.parseInt(matcher.group(1));
                        int offsetBottom = Integer.parseInt(matcher.group(2));
                        int targetWidth = Integer.parseInt(matcher.group(3));
                        int targetHeight = Integer.parseInt(matcher.group(4));
                        return new Rectangle(offsetLeft, offsetBottom, targetWidth, targetHeight);
                    } catch (NumberFormatException e) {
                        getLogger(PhotoMapping.class).warn("unable to parse clipping rectangle from {}", value, e);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Performs a crop operation on the source image as defined by the
     * supplied clipping rectangle.
     *
     * @param imageBytes The source image
     * @param clipRect The clip rectangle from an 'X-ABCROP-RECTANGLE' property
     * @param formatName The target image format
     * @param parameters Further options to use, or <code>null</code> to use to the default options
     * @return The cropped image
     */
    private static byte[] doABCrop(byte[] imageBytes, Rectangle clipRect, String formatName, VCardParameters parameters) throws IOException, OXException {
        InputStream inputStream = null;
        try {
            /*
             * read source image
             */
            inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage sourceImage = ImageIO.read(inputStream);
            /*
             * crop the image
             */
            ImageTransformationService imageService = VCardServiceLookup.getOptionalService(ImageTransformationService.class);
            if (null == imageService) {
                getLogger(PhotoMapping.class).warn("unable to acquire image transformation service, unable to crop image");
                return imageBytes;
            }
            return imageService.transfom(sourceImage, getSource(parameters)).crop(clipRect.x * -1,
                clipRect.height + clipRect.y - sourceImage.getHeight(), clipRect.width, clipRect.height).getBytes(formatName);
        } finally {
            Streams.close(inputStream);
        }
    }

    private static Object getSource(VCardParameters parameters) {
        return null != parameters && null != parameters.getSession() ? parameters.getSession().getSessionID() : null;
    }

}
