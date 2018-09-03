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

import java.util.Iterator;
import java.util.Set;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.finder.FinderUtil;
import com.openexchange.contact.picture.finder.UnmodifiablePictureSearchData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ContactPictureUtil}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactPictureUtil extends FinderUtil {

    /**
     * Generates a {@link ContactPicture} based on the given bytes
     *
     * @param unmodifiableData The {@link UnmodifiablePictureSearchData}
     * @param contact The {@link Contact}
     * @param onlyETag <code>true</code> if only eTag should be set
     * @return A {@link ContactPicture}
     */
    public static ContactPicture fromContact(UnmodifiablePictureSearchData unmodifiableData, Contact contact, boolean onlyETag) {
        return new ContactPicture(genereateETag(unmodifiableData, contact), onlyETag ? null : transformToFileHolder(contact));
    }

    /**
     * Generates the ETag
     *
     * @param unmodifiableData The {@link UnmodifiablePictureSearchData}
     * @param contact The {@link Contact}
     * @return The ETag
     */
    private static String genereateETag(UnmodifiablePictureSearchData unmodifiableData, Contact contact) {
        /*
         *  Use the request, so that changed request will lead in different eTags.
         *  This is important for requests containing resizing. If the picture shall be delivered in a
         *  different size the eTag must not be the same compared to the original size
         */
        return null == contact ? null : new StringBuilder(512) // @formatter:off#
            .append(unmodifiableData.hashCode())
            .append('/')
            .append(contact.getParentFolderID())
            .append('/')
            .append(contact.getObjectID())
            .append('/')
            .append(contact.getLastModified().getTime()).toString(); // @formatter:on
    }

    /**
     * Transforms a byte array into a {@link ByteArrayFileHolder}
     *
     * @param contact The {@link Contact}
     * @return The IFileHolder
     */
    private static ByteArrayFileHolder transformToFileHolder(Contact contact) {
        ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(contact.getImage1());
        fileHolder.setContentType(contact.getImageContentType());
        fileHolder.setName(new StringBuilder("contact-image-").append(contact.getObjectID()).toString());
        return fileHolder;
    }

    public final static ContactField[] IMAGE_FIELD = new ContactField[] { ContactField.OBJECT_ID, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, ContactField.IMAGE1, ContactField.IMAGE1_CONTENT_TYPE, ContactField.IMAGE1_URL,
        ContactField.IMAGE_LAST_MODIFIED, ContactField.LAST_MODIFIED };

    /**
     * Searches for a contact via its mail address.
     *
     * @param contactService The {@link ContactService}
     * @param emails The mail addresses
     * @param session The {@link Session}
     * @param useGAB <code>true</code> if a search in the global address book shall be performed, <code>false</code> if search shall be done in all other folders
     * @return The {@link Contact} or <code>null</code>
     * @throws OXException If the contact could not be found
     */
    public static Contact getContactFromMail(ContactService contactService, Set<String> emails, Session session, boolean useGAB) throws OXException {
        for (Iterator<String> iterator = emails.iterator(); iterator.hasNext();) {
            String email = iterator.next();

            ContactSearchObject cso = new ContactSearchObject();
            cso.setEmail1(email);
            cso.setEmail2(email);
            cso.setEmail3(email);
            cso.setOrSearch(true);

            // Search in GAB or exclude GAB
            if (useGAB) {
                cso.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID);
            } else {
                cso.setExcludeFolders(FolderObject.SYSTEM_LDAP_FOLDER_ID);
            }

            SearchIterator<Contact> result = null;
            try {
                result = contactService.searchContacts(session, cso, new SortOptions(ContactField.LAST_MODIFIED, Order.DESCENDING));
                if (result == null) {
                    continue;
                }

                while (result.hasNext()) {
                    Contact contact = result.next();
                    if (null != contact.getImage1() && (checkEmails(contact, email))) {
                        return contact;
                    }
                }
            } finally {
                Streams.close(result);
            }

        }
        return null;
    }

}
