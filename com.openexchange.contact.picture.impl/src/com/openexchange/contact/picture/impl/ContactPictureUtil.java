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

package com.openexchange.contact.picture.impl;

import java.util.Date;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.annotation.NonNull;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.finder.FinderUtil;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ContactPictureUtil}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactPictureUtil extends FinderUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactPictureUtil.class);

    /**
     * Generates a {@link ContactPicture} based on the given bytes
     *
     * @param contact The {@link Contact}
     * @return A {@link ContactPicture}
     */
    public static ContactPicture fromContact(@NonNull final Contact contact) {
        return new ContactPicture(generateETag(contact), transformToFileHolder(contact), getContactLastModified(contact));
    }

    /**
     * Generates the ETag
     *
     * @param contact The {@link Contact}
     * @return The ETag
     */
    private static String generateETag(Contact contact) {
        /*
         * Use the request, so that changed request will lead in different eTags.
         * This is important for requests containing resizing. If the picture shall be delivered in a
         * different size the eTag must not be the same compared to the original size
         */
        return new StringBuilder(512) // @formatter:off
            .append(contact.getFolderId(true))
            .append('/')
            .append(contact.getId(true))
            .append('/')
            .append(getContactLastModified(contact).getTime()).toString(); // @formatter:on
    }

    /**
     * Transforms a byte array into a {@link ByteArrayFileHolder}
     *
     * @param contact The {@link Contact}
     * @return The IFileHolder
     */
    private static ByteArrayFileHolder transformToFileHolder(Contact contact) {
        if (null == contact.getImage1()) {
            return null;
        }
        ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(contact.getImage1());
        fileHolder.setContentType(contact.getImageContentType());
        fileHolder.setName(new StringBuilder("contact-image-").append(contact.getObjectID()).toString());
        return fileHolder;
    }

    /**
     * Get the last modified value for the contact
     *
     * @param contact The contact to get the modification date from
     * @return The last modification date of the date or {@link ContactPicture#UNMODIFIED}
     */
    private static Date getContactLastModified(Contact contact) {
        return null == contact.getLastModified() ? ContactPicture.UNMODIFIED : contact.getLastModified();
    }

    /**
     * Get a value indicating if the current user has GAB capability
     *
     * @param session The {@link Session} of the current user
     * @return <code>true</code> if the current user is allowed to use GAB
     *         <code>false</code> otherwise
     */
    public static boolean hasGAB(Session session) {
        try {
            return ServerSessionAdapter.valueOf(session).getUserPermissionBits().isGlobalAddressBookEnabled();
        } catch (OXException e) {
            // Ignore
            LOGGER.debug("No GAB access.", e);
        }
        return false;
    }

    /**
     * Checks if the given mail address is valid
     *
     * @param mail The mail address
     * @return <code>true</code> if the mail can be parsed and is valid,
     *         <code>false</code> otherwise
     * @see InternetAddress
     */
    public static boolean isValidMailAddress(String mail) {
        try {
            new InternetAddress(mail).validate();
        } catch (AddressException e) {
            LOGGER.debug("Mail address isn't valid.", e);
            return false;
        }
        return true;
    }

}
