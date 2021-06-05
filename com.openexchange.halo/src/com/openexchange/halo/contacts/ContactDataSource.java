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

package com.openexchange.halo.contacts;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactImageSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

public class ContactDataSource implements HaloContactDataSource, HaloContactImageSource {

    private ServiceLookup services = null;

    public ContactDataSource(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData req, ServerSession session) {
        List<Contact> mergedContacts = query.getMergedContacts();
        List<Contact> allContacts = new ArrayList<Contact>(mergedContacts.size() + 1);
        allContacts.add(query.getContact());
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
    public ContactPicture getPicture(HaloContactQuery contactQuery, ServerSession session) throws OXException {
        final ContactPicture picture = getPicture0(contactQuery, session, true);
        if (null == picture) {
            return services.getServiceSafe(ContactPictureService.class).getPicture(session, getPictureSearchData(contactQuery));
        }
        return picture;
    }

    @Override
    public String getPictureETag(HaloContactQuery contactQuery, ServerSession session) throws OXException {
        final ContactPicture picture = getPicture0(contactQuery, session, true);
        if (null == picture) {
            return services.getServiceSafe(ContactPictureService.class).getETag(session, getPictureSearchData(contactQuery));
        }
        return picture.getETag();
    }

    private ContactPicture getPicture0(HaloContactQuery contactQuery, ServerSession session, boolean withBytes) throws OXException {
        List<Contact> mergedContacts = contactQuery.getCopyOfMergedContacts();
        if (mergedContacts == null) {
            mergedContacts = new ArrayList<>(0);
        }
        Collections.sort(mergedContacts, new ImagePrecedence());

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

                return new ContactPicture(buildETagFor(contact), holder, contact.getImageLastModified());
            }
        }

        // Try with explicit load
        for (Contact c : mergedContacts) {
            final Contact contact = getContact(session, c.getFolderId(true), c.getId(true));
            if (contact.getImage1() != null) {
                final ByteArrayFileHolder holder;
                if (withBytes) {
                    holder = new ByteArrayFileHolder(contact.getImage1());
                    holder.setContentType(contact.getImageContentType());
                    holder.setName("image");
                } else {
                    holder = null;
                }

                return new ContactPicture(buildETagFor(contact), holder, contact.getImageLastModified());
            }
        }

        return null;
    }

    private Contact getContact(ServerSession session, String folderId, String id) throws OXException {
        IDBasedContactsAccess contactsAccess = services.getServiceSafe(IDBasedContactsAccessFactory.class).createAccess(session);
        try {
            return contactsAccess.getContact(new ContactID(folderId, id));
        } finally {
            contactsAccess.finish();
        }
    }

    private static String buildETagFor(final Contact contact) {
        return null == contact ? null : new StringBuilder(512).append(contact.getFolderId(true)).append('/').append(contact.getId(true)).append('/').append(contact.getLastModified().getTime()).toString();
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

    private static PictureSearchData getPictureSearchData(HaloContactQuery contactQuery) {
        Integer userId = null != contactQuery.getUser() ? I(contactQuery.getUser().getId()) : null;
        Contact contact = contactQuery.getContact();
        if (null == contact) {
            return new PictureSearchData(userId, null, null, null);
        }
        return new PictureSearchData(userId, contact.getFolderId(true), contact.getId(true), null);
    }

}
