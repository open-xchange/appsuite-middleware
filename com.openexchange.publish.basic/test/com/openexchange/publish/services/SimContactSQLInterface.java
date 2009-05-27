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

package com.openexchange.publish.services;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.contact.LdapServer;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link SimContactSQLInterface}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimContactSQLInterface implements ContactSQLInterface {

    private Map<Integer, List<ContactObject>> folders = new HashMap<Integer, List<ContactObject>>();
    
    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.ContactSQLInterface#deleteContactObject(int, int, java.util.Date)
     */
    public void deleteContactObject(int objectId, int inFolder, Date clientLastModified) throws OXObjectNotFoundException, OXConflictException, OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.ContactSQLInterface#insertContactObject(com.openexchange.groupware.container.ContactObject)
     */
    public void insertContactObject(ContactObject contactObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.ContactSQLInterface#updateContactObject(com.openexchange.groupware.container.ContactObject, int,
     * java.util.Date)
     */
    public void updateContactObject(ContactObject contactObj, int inFolder, Date clientLastModified) throws OXException, OXConcurrentModificationException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see
     * com.openexchange.groupware.contact.ContactInterface#getContactsByExtendedSearch(com.openexchange.groupware.search.ContactSearchObject
     * , int, java.lang.String, int[])
     */
    public SearchIterator<ContactObject> getContactsByExtendedSearch(ContactSearchObject searchobject, int orderBy, String orderDir, int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    public SearchIterator<ContactObject> getContactsInFolder(int folderId, int from, int to, int orderBy, String orderDir, int[] cols) throws OXException {
        return new SearchIteratorAdapter(getFolderList(folderId).iterator());
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getDeletedContactsInFolder(int, int[], java.util.Date)
     */
    public SearchIterator<ContactObject> getDeletedContactsInFolder(int folderId, int[] cols, Date since) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getFolderId()
     */
    public int getFolderId() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getLdapServer()
     */
    public LdapServer getLdapServer() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getModifiedContactsInFolder(int, int[], java.util.Date)
     */
    public SearchIterator<ContactObject> getModifiedContactsInFolder(int folderId, int[] cols, Date since) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getNumberOfContacts(int)
     */
    public int getNumberOfContacts(int folderId) throws OXException {
        return getFolderList(folderId).size();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getObjectById(int, int)
     */
    public ContactObject getObjectById(int objectId, int inFolder) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getObjectsById(int[][], int[])
     */
    public SearchIterator<ContactObject> getObjectsById(int[][] objectIdAndInFolder, int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getUserById(int)
     */
    public ContactObject getUserById(int userId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#searchContacts(java.lang.String, int, int, java.lang.String, int[])
     */
    public SearchIterator<ContactObject> searchContacts(String searchpattern, int folderId, int orderBy, String orderDir, int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#setSession(com.openexchange.session.Session)
     */
    public void setSession(Session s) throws OXException {
        // TODO Auto-generated method stub

    }

    public void simulateContact(int cid, int folderId, int id1, String surname) {
        ContactObject contact = new ContactObject();
        contact.setContextId(cid);
        contact.setParentFolderID(folderId);
        contact.setObjectID(id1);
        contact.setSurName(surname);

        getFolderList(folderId).add(contact);
    }

    private List<ContactObject> getFolderList(int folderId) {
        if (folders.containsKey(folderId)) {
            return folders.get(folderId);
        }
        folders.put(folderId, new LinkedList<ContactObject>());
        return folders.get(folderId);
    }

}
