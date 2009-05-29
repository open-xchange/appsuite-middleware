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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSession;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link ContactFolderUpdaterStrategy}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactFolderUpdaterStrategy implements FolderUpdaterStrategy<ContactObject> {
    
    private static final int SQL_INTERFACE = 1;
    private static final int SUBSCRIPTION = 2;
    
    private static final int[] COMPARISON_COLUMNS = {
        ContactObject.OBJECT_ID,
        ContactObject.FOLDER_ID,
        ContactObject.GIVEN_NAME,
        ContactObject.SUR_NAME,
        ContactObject.BIRTHDAY
    };
    
    public int calculateSimilarityScore(ContactObject original, ContactObject candidate, Object session) throws AbstractOXException {
        int score = 0;
        if(eq(original.getGivenName(), candidate.getGivenName())) {
            score += 5;
        }
        if(eq(original.getSurName(), candidate.getSurName())) {
            score += 5;
        }
        if(original.containsBirthday() && candidate.containsBirthday() && eq(original.getBirthday(), candidate.getBirthday())) {
            score += 5;
        }
        
        return score;
    }
    
    protected boolean eq(Object o1, Object o2) {
        if(o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    public void closeSession(Object session) throws AbstractOXException {
        
    }

    public Collection<ContactObject> getData(Subscription subscription, Object session) throws AbstractOXException {
        RdbContactSQLInterface contacts = (RdbContactSQLInterface) getFromSession(SQL_INTERFACE, session);
        
        int folderId = subscription.getFolderIdAsInt();
        int numberOfContacts = contacts.getNumberOfContacts(folderId);
        SearchIterator<ContactObject> contactsInFolder = contacts.getContactsInFolder(folderId, 0, numberOfContacts, ContactObject.OBJECT_ID, "ASC", COMPARISON_COLUMNS);
        List<ContactObject> retval = new ArrayList<ContactObject>();
        while(contactsInFolder.hasNext()) {
            retval.add(contactsInFolder.next());
        }
        return retval;
    }

    public int getThreshhold(Object session) throws AbstractOXException {
        return 9;
    }

    public boolean handles(FolderObject folder) {
        return folder.getModule() == FolderObject.CONTACT;
    }

    public void save(ContactObject newElement, Object session) throws AbstractOXException {
        RdbContactSQLInterface contacts = (RdbContactSQLInterface) getFromSession(SQL_INTERFACE, session);
        Subscription subscription = (Subscription) getFromSession(SUBSCRIPTION, session);
        newElement.setParentFolderID(subscription.getFolderIdAsInt());
        
        contacts.insertContactObject(newElement);
    }


    private Object getFromSession(int key, Object session) {
        return ((Map<Integer, Object>) session).get(key);
    }

    public Object startSession(Subscription subscription) throws AbstractOXException {
        Map<Integer, Object> userInfo = new HashMap<Integer, Object>();
        userInfo.put(SQL_INTERFACE, new RdbContactSQLInterface(new SubscriptionSession(subscription)));
        userInfo.put(SUBSCRIPTION, subscription);
        return userInfo;
    }

    public void update(ContactObject original, ContactObject update, Object session) throws AbstractOXException {
        RdbContactSQLInterface contacts = (RdbContactSQLInterface) getFromSession(SQL_INTERFACE, session);

        update.setParentFolderID(original.getParentFolderID());
        update.setObjectID(original.getObjectID());
        update.setLastModified(original.getLastModified());

        contacts.updateContactObject(update, update.getParentFolderID(), update.getLastModified());
    }

}
