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

package com.openexchange.carddav.photos;

import static com.openexchange.java.Autoboxing.L;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.imagetransformation.ScaleType;
import com.openexchange.imagetransformation.TransformedImage;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link PhotoResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class PhotoResource extends DAVResource {

    private final Contact contact;

    private byte[] photoData;

    /**
     * Initializes a new {@link PhotoResource}.
     *
     * @param factory The factory
     * @param contact The contact
     * @param url The WebDAV path of the resource
     */
    public PhotoResource(DAVFactory factory, Contact contact, WebdavPath url) {
        super(factory, url);
        this.contact = contact;
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return contact.getDisplayName();
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        if (false == exists() || null == contact || null == contact.getImageLastModified()) {
            return "";
        }
        return String.format("%d-%d-%d", Integer.valueOf(getFactory().getSession().getContextId()), Integer.valueOf(contact.getObjectID()), Long.valueOf(contact.getImageLastModified().getTime()));
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return contact.getImageLastModified();
    }

    @Override
    public Long getLength() throws WebdavProtocolException {
        byte[] image = getPhotoData();
        return L(null == image ? 0L : image.length);
    }

    @Override
    public String getContentType() throws WebdavProtocolException {
        return contact.getImageContentType();
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return getLastModified();
    }

    @Override
    public boolean hasBody() throws WebdavProtocolException {
        return true;
    }

    @Override
    public InputStream getBody() throws WebdavProtocolException {
        byte[] image = getPhotoData();
        return null == image ? Streams.EMPTY_INPUT_STREAM : Streams.newByteArrayInputStream(image);
    }

    private byte[] getPhotoData() {
        if (null == photoData && null != contact.getImage1()) {
            TransformedImage transformedImage = null;
            try {
                transformedImage = scaleImageIfNeeded(contact.getImage1(), contact.getImageContentType(), getPhotoScaleDimension());
                if (null != transformedImage) {
                    photoData = transformedImage.getImageData();
                }
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(PhotoResource.class).warn("Error getting scaled contact image", e);
            } finally {
                Streams.close(transformedImage);
            }
        }
        if (null == photoData) {
            photoData = contact.getImage1();
        }
        return photoData;
    }

    private Dimension getPhotoScaleDimension () {
        try {
            ConfigView view = factory.getService(ConfigViewFactory.class).getView(factory.getUser().getId(), factory.getContext().getContextId());
            ComposedConfigProperty<String> property = view.property("com.openexchange.contact.scaleVCardImages", String.class);
            String value = property.isDefined() ? property.get() : null;
            if (Strings.isEmpty(value)) {
                return null;
            }
            int idx = value.indexOf('x');
            if (1 > idx) {
                throw new NumberFormatException(value);
            }
            return new Dimension(Integer.parseInt(value.substring(0, idx)), Integer.parseInt(value.substring(idx + 1)));
        } catch (OXException | NumberFormatException e) {
            org.slf4j.LoggerFactory.getLogger(PhotoResource.class).warn(
                "Error getting \"com.openexchange.carddav.scaleVCardImages\", falling back to defaults.", e);
            return new Dimension(600, 800);
        }
    }

    /**
     * Scales a contact image to fit into the supplied target dimension.
     *
     * @param source The image data
     * @param formatName The image format name
     * @param targetDimension The target dimension, or <code>null</code> to skip scaling
     * @return The scaled image data, or <code>null</code> if no scaling needed or scaling failed
     */
    private TransformedImage scaleImageIfNeeded(byte[] imageBytes, String formatName, Dimension targetDimension) {
        if (null == imageBytes || null == targetDimension || 1 > targetDimension.getWidth() || 1 > targetDimension.getHeight()) {
            return null;
        }
        ImageTransformationService imageService = factory.getOptionalService(ImageTransformationService.class);
        if (null == imageService) {
            org.slf4j.LoggerFactory.getLogger(PhotoResource.class).warn("unable to acquire image transformation service, unable to scale image");
            return null;
        }
        try {
            return imageService.transfom(imageBytes, factory.getSession().getSessionID())
                .scale((int) targetDimension.getWidth(), (int) targetDimension.getHeight(), ScaleType.CONTAIN, true).getFullTransformedImage(formatName);
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(PhotoResource.class).warn("error scaling image", e);
            return null;
        }
    }

}
