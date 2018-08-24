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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureExceptionCodes;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ContactPictureUtil}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a> Logic from 'ContactDataSource'
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

    /**
     * Get a {@link Comparator} for {@link Contact}s
     * 
     * @return A {@link Comparator}
     */
    public final static Comparator<Contact> getContactComperator() {
        return (Contact o1, Contact o2) -> {
            if (o1.getParentFolderID() == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                if (o2.getParentFolderID() != FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                    return -1;
                }
            } else {
                if (o2.getParentFolderID() == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                    return 1;
                }
            }

            Date lastModified1 = o1.getLastModified();
            Date lastModified2 = o2.getLastModified();
            if (lastModified1 == null) {
                lastModified1 = new Date(Long.MIN_VALUE);
            }
            if (lastModified2 == null) {
                lastModified2 = new Date(Long.MIN_VALUE);
            }
            return lastModified2.compareTo(lastModified1);
        };
    }

    public final static ContactField[] IMAGE_FIELD = new ContactField[] { ContactField.OBJECT_ID, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, ContactField.IMAGE1, ContactField.IMAGE1_CONTENT_TYPE, ContactField.IMAGE1_URL,
        ContactField.IMAGE_LAST_MODIFIED, ContactField.LAST_MODIFIED };

    /**
     * Searches for a contact via its mail address.
     * 
     * @param contactService The {@link ContactService}
     * @param emails The mail addresses
     * @param session The {@link Session}
     * @param folderId The identifier of the folder to search in. Can be <code>null</code>
     * @return The {@link Contact} or <code>null</code>
     * @throws OXException If the contact could not be found
     */
    @SuppressWarnings("resource")
    public static Contact getContactFromMail(ContactService contactService, Set<String> emails, Session session, Integer folderId) throws OXException {
        for (Iterator<String> iterator = emails.iterator(); iterator.hasNext();) {
            String email = iterator.next();

            ContactSearchObject cso = new ContactSearchObject();
            cso.setEmail1(email);
            cso.setEmail2(email);
            cso.setEmail3(email);
            cso.setOrSearch(true);

            if (null != folderId) {
                cso.addFolder(folderId.intValue());
            }

            SearchIterator<Contact> result = null;
            try {
                result = contactService.searchContacts(session, cso, IMAGE_FIELD);
                if (result == null) {
                    continue;
                }

                List<Contact> contacts = new ArrayList<Contact>();
                while (result.hasNext()) {
                    Contact contact = result.next();
                    if (null != contact.getImage1() && (ContactPictureUtil.checkEmails(contact, email))) {
                        contacts.add(contact);
                    }
                }

                if (contacts.size() != 1) {
                    Collections.sort(contacts, ContactPictureUtil.getContactComperator());
                }
                return contacts.get(0);
            } finally {
                Streams.close(result);
            }

        }
        return null;
    }

}
