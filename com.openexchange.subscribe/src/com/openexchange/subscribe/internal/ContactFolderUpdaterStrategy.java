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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i2I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.OverridingContactInterface;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.groupware.search.Order;
import com.openexchange.subscribe.TargetFolderSession;
import com.openexchange.subscribe.osgi.SubscriptionServiceRegistry;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ContactFolderUpdaterStrategy}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactFolderUpdaterStrategy implements FolderUpdaterStrategy<Contact> {

    private static final int SQL_INTERFACE = 1;

    private static final int TARGET = 2;

    private static final int[] COMPARISON_COLUMNS = {
        Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.GIVEN_NAME, Contact.SUR_NAME, Contact.BIRTHDAY, Contact.DISPLAY_NAME, Contact.EMAIL1,
        Contact.EMAIL2, Contact.EMAIL3, Contact.USERFIELD20 };

    private static final int[] MATCH_COLUMNS = I2i(Arrays.remove(i2I(Contact.CONTENT_COLUMNS), I(Contact.USERFIELD20)));

    @Override
    public int calculateSimilarityScore(final Contact original, final Contact candidate, final Object session) {
        int score = 0;
        final int threshold = getThreshold(session);

        if(isReasonablyEmpty(original) && isReasonablyEmpty(candidate)) {
        	return threshold + 1;
        }
        // For the sake of simplicity we assume that equal names mean equal contacts
        // TODO: This needs to be diversified in the form of "unique-in-context" later (if there is only one "Max Mustermann" in a folder it
        // is unique and qualifies as identifier. If there are two "Max Mustermann" it does not.)
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
        if (original.containsBirthday() && candidate.containsBirthday() && eq(original.getBirthday(), candidate.getBirthday())) {
            score += 5;
        }

        if( score < threshold && original.matches(candidate, MATCH_COLUMNS)) { //the score check is only to speed the process up
            score = threshold + 1;
        }
        return score;
    }

    private boolean isReasonablyEmpty(Contact c) {
		return !(c.containsEmail1() 
		|| c.containsEmail2() 
		|| c.containsEmail3() 
		|| c.containsSurName() 
		|| c.containsGivenName() 
		|| c.containsYomiFirstName()
		|| c.containsYomiLastName()
		|| c.containsCompany()
		|| c.containsYomiCompany()
		|| c.containsDisplayName()
		|| c.containsNickname());
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

    }

    @Override
    public Collection<Contact> getData(final TargetFolderDefinition target, final Object session) throws OXException {
        final Object sqlInterface = getFromSession(SQL_INTERFACE, session);
        if (sqlInterface instanceof ContactInterface) {
            final ContactInterface contacts = (ContactInterface) getFromSession(SQL_INTERFACE, session);

            final int folderId = target.getFolderIdAsInt();
            final int numberOfContacts = contacts.getNumberOfContacts(folderId);
            final SearchIterator<Contact> contactsInFolder = contacts.getContactsInFolder(
                folderId,
                0,
                numberOfContacts,
                DataObject.OBJECT_ID,
                Order.ASCENDING,
                null,
                COMPARISON_COLUMNS);
            final List<Contact> retval = new ArrayList<Contact>();
            while (contactsInFolder.hasNext()) {
                retval.add(contactsInFolder.next());
            }
            return retval;
        }
        return Collections.emptyList();
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
    public void save(final Contact newElement, final Object session) throws OXException {
        final Object sqlInterface = getFromSession(SQL_INTERFACE, session);
        if (sqlInterface instanceof OverridingContactInterface) {
            final OverridingContactInterface contacts = (OverridingContactInterface) sqlInterface;
            final TargetFolderDefinition target = (TargetFolderDefinition) getFromSession(TARGET, session);
            newElement.setParentFolderID(target.getFolderIdAsInt());
            // as this is a new contact it needs a UUID to make later aggregation possible. This has to be a new one.
            newElement.setUserField20(UUID.randomUUID().toString());
            contacts.forceInsertContactObject(newElement);
        }
    }

    private Object getFromSession(final int key, final Object session) {
        return ((Map<Integer, Object>) session).get(key);
    }

    @Override
    public Object startSession(final TargetFolderDefinition target) throws OXException {
        final Map<Integer, Object> userInfo = new HashMap<Integer, Object>();
        final TargetFolderSession session = new TargetFolderSession(target);
        final int folderID = target.getFolderIdAsInt();

        final ContactInterface contactInterface = SubscriptionServiceRegistry.getInstance().getService(
        		ContactInterfaceDiscoveryService.class).newContactInterface(
        				folderID,
        				session);

        userInfo.put(SQL_INTERFACE, contactInterface);
        userInfo.put(TARGET, target);
        return userInfo;
    }

    @Override
    public void update(final Contact original, final Contact update, final Object session) throws OXException {
        final Object sqlInterface = getFromSession(SQL_INTERFACE, session);
        if (sqlInterface instanceof ContactInterface) {
            final ContactInterface contactInterface = (ContactInterface) sqlInterface;

            update.setParentFolderID(original.getParentFolderID());
            update.setObjectID(original.getObjectID());
            update.setLastModified(new Date(System.currentTimeMillis()));
            // We need to carry over the UUID to keep existing relations
            update.setUserField20(original.getUserField20());

            contactInterface.updateContactObject(update, update.getParentFolderID(), update.getLastModified());
        }
    }

}
