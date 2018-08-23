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

package com.openexchange.contact.picture.finder.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.functions.OXFunction;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ContactMailFinder} - Finds picture based on user identifier
 *
 * @author<a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a> Logic from 'ContactDataSource'
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactMailFinder extends AbstractContactFinder {

    /**
     * Initializes a new {@link ContactMailFinder}.
     * 
     * @param userPermissionService The {@link UserPermissionService}
     * @param contactService The {@link ContactService}
     */
    public ContactMailFinder(UserPermissionService userPermissionService, ContactService contactService) {
        super(userPermissionService, contactService);
    }

    @Override
    public boolean isRunnable(ContactPictureRequestData cprd) {
        return super.isRunnable(cprd) && cprd.hasUser();
    }

    @Override
    BiConsumer<ContactPictureRequestData, OXException> handleException() {
        return (ContactPictureRequestData cprd, OXException e) -> {
            LOGGER.debug("Unable to get contact for mail addresses {}.", cprd.getEmails(), e);
        };
    }

    @SuppressWarnings("resource")
    @Override
    OXFunction<ContactPictureRequestData, Contact> getContact() {
        return (ContactPictureRequestData cprd) -> {
            for (Iterator<String> iterator = cprd.getEmails().iterator(); iterator.hasNext();) {
                String email = iterator.next();

                ContactSearchObject cso = new ContactSearchObject();
                cso.setEmail1(email);
                cso.setEmail2(email);
                cso.setEmail3(email);
                cso.setOrSearch(true);
                
                SearchIterator<Contact> result = null;
                try {
                    result = contactService.searchContacts(cprd.getSession(), cso, IMAGE_FIELD);
                    if (result == null) {
                        continue;
                    }

                    List<Contact> contacts = new ArrayList<Contact>();
                    while (result.hasNext()) {
                        Contact contact = result.next();
                        if (null != contact.getImage1() && (checkEmail(contact, email))) {
                            contacts.add(contact);
                        }
                    }

                    if (contacts.size() != 1) {
                        Collections.sort(contacts, new ImagePrecedence());
                    }
                    return contacts.get(0);
                } finally {
                    Streams.close(result);
                }

            }
            return null;
        };
    }

    private boolean checkEmail(Contact c, String email) {
        if (c.getEmail1() != null && c.getEmail1().equalsIgnoreCase(email)) {
            return true;
        }
        if (c.getEmail2() != null && c.getEmail2().equalsIgnoreCase(email)) {
            return true;
        }
        if (c.getEmail3() != null && c.getEmail3().equalsIgnoreCase(email)) {
            return true;
        }
        return false;
    }

    private static class ImagePrecedence implements Comparator<Contact> {

        ImagePrecedence() {
            super();
        }

        @Override
        public int compare(Contact o1, Contact o2) {
            if (o1.getParentFolderID() == 6 && o2.getParentFolderID() != 6) {
                return -1;
            }

            if (o1.getParentFolderID() != 6 && o2.getParentFolderID() == 6) {
                return 1;
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
        }
    }

}
