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

package com.openexchange.carddav.photos;

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
        } else {
            return "http://www.open-xchange.com/etags/" + contact.getObjectID() + "-" + contact.getImageLastModified().getTime();
        }
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return contact.getImageLastModified();
    }

    @Override
    public Long getLength() throws WebdavProtocolException {
        byte[] image = getPhotoData();
        return null == image ? 0L : image.length;
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
            return new Dimension(200, 200);
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
