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

package com.openexchange.contact.picture.finder;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.contact.picture.ContactPictureExceptionCodes;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link FinderUtil}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class FinderUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(FinderUtil.class);

    /**
     * Checks if the contact does contain a valid image to use
     *
     * @param contextId The context identifier for logging
     * @param contact The {@link Contact}
     * @param data Information to log
     * @return <code>true</code> if the image is valid and can be used, <code>false</code> otherwise
     * @throws OXException In case picture contains harmful content
     */
    public static boolean hasValidImage(Integer contextId, Contact contact, PictureSearchData data) throws OXException {
        return null != contact && null != contact.getImage1() && checkImage(contact.getImage1(), contextId, data);
    }

    /**
     * Checks the image and logs errors
     *
     * @param fileHolder The {@link IFileHolder} containing the data
     * @param contextId The context identifier for logging
     * @param data Information to log
     * @return <code>true</code> If all checks passed, <code>false</code> otherwise
     */
    public static boolean checkImage(IFileHolder fileHolder, Integer contextId, PictureSearchData data) {
        try {
            return checkImage(Streams.stream2bytes(fileHolder.getStream()), contextId, data);
        } catch (OXException | IOException e) {
            LOGGER.warn("Unable to convert input stream. Therefore can't check the image.", e);
        }
        return false;
    }

    /**
     * Checks the image and logs errors
     *
     * @param imageBytes The image to check
     * @param contextId The context identifier for logging
     * @param data Information to log
     * @return <code>true</code> If all checks passed, <code>false</code> otherwise
     * @throws OXException In case picture contains harmful content
     */
    public static boolean checkImage(byte[] imageBytes, Integer contextId, PictureSearchData data) throws OXException {
        if (false == com.openexchange.ajax.helper.ImageUtils.isValidImage(imageBytes)) {
            LOGGER.debug("Detected non-image data in contact: object-id={} folder={} context={} session-user={}. Try to obtain valid image.", data.getContactId(), data.getFolderId(), contextId, data.getUserId());
            return false;
        }
        if (com.openexchange.ajax.helper.ImageUtils.isSvg(imageBytes)) {
            LOGGER.debug("Detected a possibly harmful SVG image in contact: object-id={} folder={} context={} session-user={}. Returning an empty image as fallback.", data.getContactId(), data.getFolderId(), contextId, data.getUserId());
            throw ContactPictureExceptionCodes.HARMFULL_PICTURE.create(data.getContactId());
        }
        return true;
    }

    /**
     * Checks if two contacts are equal in terms of mail addresses
     *
     * @param c1 The one {@link Contact}
     * @param c2 The other {@link Contact}
     * @return <code>true</code> if the mail belongs to the contact, <code>false</code> otherwise
     */
    public static boolean checkEmails(Contact c1, Contact c2) {
        return checkEmail(c1.getEmail1(), c2.getEmail1()) && checkEmail(c1.getEmail2(), c2.getEmail2()) && checkEmail(c1.getEmail3(), c2.getEmail3());
    }

    /**
     * Checks if the given email belongs to the given contact
     *
     * @param c The {@link Contact}
     * @param email The mail to match
     * @return <code>true</code> if the mail belongs to the contact, <code>false</code> otherwise
     */
    public static boolean checkEmails(Contact c, String email) {
        return checkEmail(c.getEmail1(), email) || checkEmail(c.getEmail2(), email) || checkEmail(c.getEmail3(), email);
    }

    /**
     * Checks if two mail addresses can be considered equal
     *
     * @param contactMail The actual mail from a contact
     * @param email The mail to match
     * @return <code>true</code> if the mail addresses can be considered equal, <code>false</code> otherwise
     */
    public static boolean checkEmail(String contactMail, String email) {
        if (Strings.isNotEmpty(contactMail) && contactMail.equalsIgnoreCase(email)) {
            return true;
        }
        return false;
    }

}
