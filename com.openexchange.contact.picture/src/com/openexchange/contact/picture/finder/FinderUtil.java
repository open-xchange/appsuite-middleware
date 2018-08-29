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

package com.openexchange.contact.picture.finder;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.contact.picture.ContactPictureExceptionCodes;
import com.openexchange.contact.picture.ContactPictureRequestData;
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
     * @param contextId The context identifier
     * @param contact The {@link Contact}
     * @param data Information to log
     * @return <code>true</code> if the image is valid and can be used, <code>false</code> otherwise
     * @throws OXException In case picture contains harmful content
     */
    public static boolean hasValidImage(Integer contextId, Contact contact, ContactPictureRequestData data) throws OXException {
        return null != contact && null != contact.getImage1() && checkImage(contact.getImage1(), contextId, data);
    }

    /**
     * Checks the image and logs errors
     * 
     * @param fileHolder The {@link IFileHolder} containing the data
     * @param contextId The context identifier
     * @param data Information to log
     * @return <code>true</code> If all checks passed, <code>false</code> otherwise
     */
    public static boolean checkImage(IFileHolder fileHolder, Integer contextId, ContactPictureRequestData data) {
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
     * @param contextId The context identifier
     * @param data Information to log
     * @return <code>true</code> If all checks passed, <code>false</code> otherwise
     * @throws OXException In case picture contains harmful content
     */
    public static boolean checkImage(byte[] imageBytes, Integer contextId, ContactPictureRequestData data) throws OXException {
        if (false == com.openexchange.ajax.helper.ImageUtils.isValidImage(imageBytes)) {
            LOGGER.warn("Detected non-image data in contact: object-id={} folder={} context={} session-user={}. Try to obtain valid image.", data.getContactId(), data.getFolderId(), contextId, data.getUserId());
            return false;
        }
        if (com.openexchange.ajax.helper.ImageUtils.isSvg(imageBytes)) {
            LOGGER.debug("Detected a possibly harmful SVG image in contact: object-id={} folder={} context={} session-user={}. Returning an empty image as fallback.", data.getContactId(), data.getFolderId(), contextId, data.getUserId());
            throw ContactPictureExceptionCodes.HARMFULL_PICTURE.create(data.getContactId());
        }
        return true;
    }

    /**
     * Checks if two contacts are equal in Terms of mail addresses
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
