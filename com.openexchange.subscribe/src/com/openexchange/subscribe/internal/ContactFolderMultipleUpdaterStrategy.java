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

package com.openexchange.subscribe.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.subscribe.TargetFolderSession;
import com.openexchange.subscribe.osgi.SubscriptionServiceRegistry;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link ContactFolderMultipleUpdaterStrategy}
 * This differs from ContactFolderUpdaterStrategy in 2 ways
 * - individual fields are only written if present in the update and not filled yet. So no fields will be deleted and none will be overwritten.
 * - aggregating relations between contacts are respected as well as generated if appropriate
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactFolderMultipleUpdaterStrategy implements FolderUpdaterStrategy<Contact> {
    private static final int SQL_INTERFACE = 1;

    private static final int TARGET = 2;

    private static final int SESSION = 3;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactFolderMultipleUpdaterStrategy.class);

    // All columns need to be loaded here as we keep the original, not the update and no data may be lost
    private static final ContactField[] COMPARISON_FIELDS = ContactField.values();

    @Override
    public int calculateSimilarityScore(final Contact original, final Contact candidate, final Object session) throws OXException {
        int score = 0;
        final int threshhold = getThreshold(session);

        if ((isset(original.getGivenName()) || isset(candidate.getGivenName())) && eq(original.getGivenName(), candidate.getGivenName())) {
            score += 5;
        }
        if ((isset(original.getSurName()) || isset(candidate.getSurName())) && eq(original.getSurName(), candidate.getSurName())) {
            score += 5;
        }
        if ((isset(original.getDisplayName()) || isset(candidate.getDisplayName())) && eq(original.getDisplayName(), candidate.getDisplayName())) {
            score += 10;
        }
        // an email-address is unique so if this is identical the contact should be the same
        if (eq(original.getEmail1(), candidate.getEmail1())) {
            score += 10;
        }
        if (eq(original.getEmail2(), candidate.getEmail2())) {
            score += 10;
        }
        if (eq(original.getEmail3(), candidate.getEmail3())) {
            score += 10;
        }
        if (eq(original.getCellularTelephone1(), candidate.getCellularTelephone1())) {
            score += 10;
        }
        if (original.containsBirthday() && candidate.containsBirthday() && eq(original.getBirthday(), candidate.getBirthday())) {
            score += 5;
        }

        if( score < threshhold && original.equalsContentwise(candidate)) { //the score check is only to speed the process up
            score += threshhold + 1;
        }
        return score;
    }

    private boolean isset(final String s) {
        return s == null || s.length() > 0;
    }

    protected boolean eq(final Object o1, final Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }

    @Override
    public void closeSession(final Object session) throws OXException {

    }

    @Override
    public Collection<Contact> getData(final TargetFolderDefinition target, final Object session) throws OXException {
        List<Contact> contacts = new ArrayList<Contact>();
        ContactService contactService = (ContactService)getFromSession(SQL_INTERFACE, session);
        TargetFolderSession targetFolderSession = (TargetFolderSession)getFromSession(SESSION, session);
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator = contactService.getAllContacts(targetFolderSession, target.getFolderId(), COMPARISON_FIELDS);
            if (null != searchIterator) {
                while (searchIterator.hasNext()) {
                    contacts.add(searchIterator.next());
                }
            }
        } finally {
            if (null != searchIterator) {
                searchIterator.close();
            }
        }
        return contacts;
    }

    @Override
    public int getThreshold(final Object session) throws OXException {
        return 9;
    }

    @Override
    public boolean handles(final FolderObject folder) {
        return folder.getModule() == FolderObject.CONTACT;
    }

    @Override
    public void save(final Contact newElement, final Object session, Collection<OXException> errors) throws OXException {
        ContactService contactService = (ContactService)getFromSession(SQL_INTERFACE, session);
        TargetFolderSession targetFolderSession = (TargetFolderSession)getFromSession(SESSION, session);
        TargetFolderDefinition target = (TargetFolderDefinition) getFromSession(TARGET, session);
        newElement.setParentFolderID(target.getFolderIdAsInt());

        try {
            contactService.createContact(targetFolderSession, target.getFolderId(), newElement);
        } catch (OXException e) {
            if (ContactExceptionCodes.DATA_TRUNCATION.equals(e)) {
                boolean hasTrimmed = false;
                try {
                    hasTrimmed = MappedTruncation.truncate(e.getProblematics(), newElement);
                } catch (OXException x) {
                    LOG.warn("error trying to handle truncated attributes", x);
                }
                if (hasTrimmed) {
                    save(newElement, session, errors);
                    return;
                }
            }
            throw e;
        }
    }

    private Object getFromSession(final int key, final Object session) {
        return ((Map<Integer, Object>) session).get(key);
    }

    @Override
    public Object startSession(final TargetFolderDefinition target) throws OXException {
        final Map<Integer, Object> userInfo = new HashMap<Integer, Object>();
        final TargetFolderSession session = new TargetFolderSession(target);
        ContactService contactService = SubscriptionServiceRegistry.getInstance().getService(ContactService.class);
        userInfo.put(SQL_INTERFACE, contactService);
        userInfo.put(TARGET, target);
        userInfo.put(SESSION, session);
        return userInfo;
    }

    @Override
    public void update(final Contact original, final Contact update, final Object session) throws OXException {
        final ContactService contactService = (ContactService)getFromSession(SQL_INTERFACE, session);
        final TargetFolderSession targetFolderSession = (TargetFolderSession)getFromSession(SESSION, session);

        final String folderId = Integer.toString(original.getParentFolderID());
        final String contactId = Integer.toString(original.getObjectID());

        Contact origContact = original;
        for (int retry = 2; retry-- > 0;) {
            //This may only fill up fields NEVER overwrite them. Original should be used as base and filled up as needed
            //ALL Content Columns need to be considered here
            final int[] columns = Contact.CONTENT_COLUMNS;
            for (final int field : columns){
                if (origContact.get(field) == null){
                    final Object newValue = update.get(field);
                    if (newValue != null){
                        origContact.set(field, newValue);
                    }
                }
            }

            // Update of image bytes w/o MIME type will fail...
            if (origContact.getImage1() != null) {
                if (origContact.getImageContentType() == null) {
                    String imageContentType = update.getImageContentType();
                    if (null == imageContentType) {
                        imageContentType = "image/jpeg";
                    }
                    origContact.setImageContentType(imageContentType);
                }
            }

            try {
                contactService.updateContact(targetFolderSession, folderId, contactId, origContact, origContact.getLastModified());
            } catch (final OXException e) {
                if (!ContactExceptionCodes.OBJECT_HAS_CHANGED.equals(e) || retry <= 0) {
                    throw e;
                }

                // Retry...
                origContact = contactService.getContact(targetFolderSession, folderId, contactId, COMPARISON_FIELDS);
            }
        }
    }
}
