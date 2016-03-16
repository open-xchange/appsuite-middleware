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

package com.openexchange.halo.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactImageSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.halo.Picture;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

public class ContactDataSource implements HaloContactDataSource, HaloContactImageSource {

    private ServiceLookup services = null;

    public ContactDataSource(ServiceLookup services) {
        this.services = services;
    }

	@Override
	public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData req, ServerSession session)
			throws OXException {
		List<Contact> allContacts = new ArrayList<Contact>();

		allContacts.add(query.getContact());
		List<Contact> mergedContacts = query.getMergedContacts();
		allContacts.addAll(mergedContacts);

		return new AJAXRequestResult(allContacts, "contact");
	}

	@Override
	public String getId() {
		return "com.openexchange.halo.contacts";
	}

	@Override
	public boolean isAvailable(ServerSession session) {
		return true;
	}

    @Override
    public int getPriority() {
        return 10000; // Pretty likely
    }

    @Override
    public Picture getPicture(HaloContactQuery contactQuery, ServerSession session) throws OXException {
        return getPicture0(contactQuery, session, true);
    }

    @Override
    public String getPictureETag(HaloContactQuery contactQuery, ServerSession session) throws OXException {
        final Picture picture = getPicture0(contactQuery, session, true);
        return null == picture ? null : picture.getEtag();
    }

    private Picture getPicture0(HaloContactQuery contactQuery, ServerSession session, boolean withBytes) throws OXException {
        List<Contact> mergedContacts = contactQuery.getMergedContacts();
        Collections.sort(mergedContacts,  new ImagePrecedence());

        for (final Contact contact : mergedContacts) {
            if (contact.getImage1() != null) {
                final ByteArrayFileHolder holder;
                if (withBytes) {
                    holder = new ByteArrayFileHolder(contact.getImage1());
                    holder.setContentType(contact.getImageContentType());
                    holder.setName("image");
                } else {
                    holder = null;
                }

                return new Picture(buildETagFor(contact), holder);
            }
        }

        // Try with explicit load
        for (Contact c : mergedContacts) {
            final Contact contact = getContact(session, Integer.toString(c.getParentFolderID()), Integer.toString(c.getObjectID()));
            if (contact.getImage1() != null) {
                final ByteArrayFileHolder holder;
                if (withBytes) {
                    holder = new ByteArrayFileHolder(contact.getImage1());
                    holder.setContentType(contact.getImageContentType());
                    holder.setName("image");
                } else {
                    holder = null;
                }

                return new Picture(buildETagFor(contact), holder);
            }
        }

        Contact contact = contactQuery.getContact();

        Picture p = searchByMailAddress(session, contact.getEmail1(), withBytes);
        if (p != null) {
            return p;
        }

        p = searchByMailAddress(session, contact.getEmail2(), withBytes);
        if (p != null) {
            return p;
        }

        p = searchByMailAddress(session, contact.getEmail3(), withBytes);
        if (p != null) {
            return p;
        }
        // Give up

        return null;
    }

    private Picture searchByMailAddress(ServerSession session, String email, boolean withBytes) throws OXException {
        if (email == null) {
            return null;
        }
        ContactSearchObject cso = new ContactSearchObject();
        cso.setEmail1(email);
        cso.setEmail2(email);
        cso.setEmail3(email);
        cso.setOrSearch(true);

        SearchIterator<Contact> result = services.getService(ContactService.class).searchContacts(session, cso, new ContactField[]{ContactField.FOLDER_ID, ContactField.IMAGE1, ContactField.IMAGE1_CONTENT_TYPE, ContactField.LAST_MODIFIED});
        if (result == null) {
            return null;
        }

        List<Contact> contacts = new ArrayList<Contact>();
        while(result.hasNext()) {
            Contact contact = result.next();

            if (contact.getImage1() != null && (checkEmail(contact, email))) {
                contacts.add(contact);
            }
        }
        Collections.sort(contacts, new ImagePrecedence());

        for (Contact contact : contacts) {
            final ByteArrayFileHolder holder;
            if (withBytes) {
                holder = new ByteArrayFileHolder(contact.getImage1());
                holder.setContentType(contact.getImageContentType());
                holder.setName("image");
            } else {
                holder = null;
            }

            return new Picture(buildETagFor(contact), holder);
        }

        return null;
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

    private Contact getContact(ServerSession session, String folderId, String id) throws OXException {
        return services.getService(ContactService.class).getContact(session, folderId, id);
    }

    private static String buildETagFor(final Contact contact) {
        return null == contact ? null : new StringBuilder(512).append(contact.getParentFolderID()).append('/').append(contact.getObjectID()).append('/').append(contact.getLastModified().getTime()).toString();
    }

    private static class ImagePrecedence implements Comparator<Contact> {

        /**
         * Initializes a new {@link ContactDataSource.ImagePrecedence}.
         */
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
