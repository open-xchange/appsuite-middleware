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

package com.openexchange.subscribe.internal;

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.log.LogProperties;
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
    public int calculateSimilarityScore(final Contact original, final Contact candidate, final Object session) {
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

        if ( score < threshhold && original.equalsContentwise(candidate)) { //the score check is only to speed the process up
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
        }
        return o1.equals(o2);
    }

    @Override
    public void closeSession(final Object session) {
        LogProperties.remove(LogProperties.Name.SUBSCRIPTION_ADMIN);
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
    public int getThreshold(final Object session) {
        return 9;
    }

    @Override
    public boolean handles(final FolderObject folder) {
        return folder.getModule() == FolderObject.CONTACT;
    }

    @Override
    public void save(final Contact newElement, final Object session, Collection<OXException> errors) throws OXException {
        ContactService contactService = (ContactService) getFromSession(SQL_INTERFACE, session);
        TargetFolderSession targetFolderSession = (TargetFolderSession) getFromSession(SESSION, session);
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

    @SuppressWarnings("unchecked")
    private Object getFromSession(final int key, final Object session) {
        return ((Map<Integer, Object>) session).get(I(key));
    }

    @Override
    public Object startSession(final TargetFolderDefinition target) {
        final Map<Integer, Object> userInfo = new HashMap<Integer, Object>();
        final TargetFolderSession session = new TargetFolderSession(target);
        ContactService contactService = SubscriptionServiceRegistry.getInstance().getService(ContactService.class);
        userInfo.put(I(SQL_INTERFACE), contactService);
        userInfo.put(I(TARGET), target);
        LogProperties.put(LogProperties.Name.SUBSCRIPTION_ADMIN, "true");
        userInfo.put(I(SESSION), session);
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
            } catch (OXException e) {
                if (!ContactExceptionCodes.OBJECT_HAS_CHANGED.equals(e) || retry <= 0) {
                    throw e;
                }

                // Retry...
                origContact = contactService.getContact(targetFolderSession, folderId, contactId, COMPARISON_FIELDS);
            }
        }
    }
}
