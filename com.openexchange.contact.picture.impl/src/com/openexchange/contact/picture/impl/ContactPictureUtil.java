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

package com.openexchange.contact.picture.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureExceptionCodes;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link ContactPictureUtil}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactPictureUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContactPictureUtil.class);

    /**
     * Generates a {@link ContactPicture} based on the given bytes
     * 
     * @param contact The {@link Contact}
     * @return A {@link ContactPicture}
     */
    public static ContactPicture fromContact(Contact contact) {
        return fromContact(contact, false);
    }

    /**
     * Generates a {@link ContactPicture} based on the given bytes
     * 
     * @param contact The {@link Contact}
     * @param onlyETag <code>true</code> if only eTag should be set
     * @return A {@link ContactPicture}
     */
    public static ContactPicture fromContact(Contact contact, boolean onlyETag) {
        return new ContactPicture(genereateETag(contact), onlyETag ? null : transformToFileHolder(contact));
    }

    /**
     * Generates the ETag
     * 
     * @param contact The {@link Contact}
     * @return The ETag
     */
    private static String genereateETag(Contact contact) {
        return null == contact ? null : new StringBuilder(512) // @formatter:off
            .append(contact.getParentFolderID())
            .append('/')
            .append(contact.getObjectID())
            .append('/')
            .append(contact.getLastModified().getTime()).toString(); // @formatter:on
    }

//    private static String generateETagForLocation(Contact contact, Session session) {
//        final ImageLocation imageLocation = new ImageLocation.Builder().folder(Integer.toString(contact.getParentFolderID())).id(Integer.toString(contact.getObjectID())).build();
//
//        final StringBuilder sb = new StringBuilder(64);
//        ImageUtility.startImageUrl(imageLocation, session, null, true, sb);
//        if (null == imageLocation.getTimestamp()) {
//            sb.append('&').append("timestamp=").append(contact.getLastModified().getTime());
//        }
//        return sb.toString();
//
//    }

    /**
     * Transforms a byte array into a {@link ByteArrayFileHolder}
     * 
     * @param contact The {@link Contact}
     * @return The IFileHolder
     */
    private static ByteArrayFileHolder transformToFileHolder(Contact contact) {
        ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(contact.getImage1());
        fileHolder.setContentType(contact.getImageContentType());
        // TODO       fileHolder.setName();
        return fileHolder;
    }

    /**
     * Checks the image and logs errors
     * 
     * @param imageBytes The image to check
     * @param cprd Information to log
     * @return <code>true</code> If all checks passed, <code>false</code> otherwise
     * @throws OXException In case picture contains harmful content
     */
    public static boolean checkImage(byte[] imageBytes, ContactPictureRequestData cprd) throws OXException {
        if (false == com.openexchange.ajax.helper.ImageUtils.isValidImage(imageBytes)) {
            LOGGER.warn("Detected non-image data in contact: object-id={} folder={} context={} session-user={}. Try to obtain valid image.", cprd.getContactId(), cprd.getFolderId(), cprd.getContextId(), cprd.getUserId());
            return false;
        }
        if (com.openexchange.ajax.helper.ImageUtils.isSvg(imageBytes)) {
            LOGGER.debug("Detected a possibly harmful SVG image in contact: object-id={} folder={} context={} session-user={}. Returning an empty image as fallback.", cprd.getContactId(), cprd.getFolderId(), cprd.getContextId(), cprd.getUserId());
            throw ContactPictureExceptionCodes.HARMFULL_PICTURE.create(cprd.getContactId());
        }
        return true;
    }

}
