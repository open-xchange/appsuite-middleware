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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.contact.provider.folder;

import java.util.Date;
import java.util.List;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.provider.ContactsAccess;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;

/**
 * {@link FolderContactsAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface FolderContactsAccess extends ContactsAccess {

    /**
     * Creates a new contact in the specified folder.
     *
     * @param folderId The identifier of the folder to create the event in
     * @param contact The contact data
     * @throws OXException
     */
    void createContact(String folderId, Contact contact) throws OXException;

    /**
     * Updates an existing contact
     *
     * @param contactId The contact identifier
     * @param contact The contact data to update
     * @param clientTimestamp The last know timestamp by the client
     * @throws OXException if an error is occurred
     */
    void updateContact(ContactID contactId, Contact contact, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing contact
     *
     * @param contactId The contact identifier
     * @param clientTimestamp The last know timestamp by the client
     * @throws OXException if an error is occurred
     */
    void deleteContact(ContactID contactId, long clientTimestamp) throws OXException;

    /**
     * Deletes all contacts with the specified contact identifiers
     * 
     * @param contactIds The contact identifiers
     * @param clientTimestamp The last know timestamp by the client
     * @throws OXException if an error is occurred
     */
    void deleteContacts(List<ContactID> contactsIds, long clientTimestamp) throws OXException;

    /**
     * Deletes all contacts in the specified folder
     * 
     * @param folderId The folder identifier
     * @throws OXException if an error is occurred
     */
    void deleteContacts(String folderId) throws OXException;

    /**
     * Counts all contacts within the given folder.
     *
     * @param folderId ID of the folder to count in
     * @return the number of contacts
     * @throws OXException if an error is occurred
     */
    int countContacts(String folderId) throws OXException;

    /**
     * Creates a new folder.
     *
     * @param folder The folder data to create
     * @return The identifier of the newly created folder
     */
    String createFolder(ContactsFolder folder) throws OXException;

    /**
     * Updates an existing folder.
     *
     * @param folderId The identifier of the folder to update
     * @param folder The folder data to update
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The (possibly changed) identifier of the updated folder
     */
    String updateFolder(String folderId, ContactsFolder folder, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing folder.
     *
     * @param folderId The identifier of the folder to delete
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     */
    void deleteFolder(String folderId, long clientTimestamp) throws OXException;

    /**
     * Gets a specific contacts folder.
     *
     * @param folderId The identifier of the contacts folder to get
     * @return The contacts folder
     */
    ContactsFolder getFolder(String folderId) throws OXException;

    /**
     * Returns a list with all visible contacts folders for the user
     *
     * @return a list with all visible contacts folders for the user
     */
    List<ContactsFolder> getVisibleFolders() throws OXException;

    /**
     * Gets a specific contact from a specific folder
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * </ul>
     * 
     * @param folderId The identifier of the folder representing the current user's contacts
     * @param contactId The identifier of the contact to get
     * @return The contact
     */
    Contact getContact(String folderId, String contactId) throws OXException;

    /**
     * Gets a list of contacts with the specified identifiers.
     * 
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param contactIDs A list of the identifiers of the contacts to get
     * @return The contacts
     */
    List<Contact> getContacts(List<ContactID> contactIDs) throws OXException;

    /**
     * Gets all contacts in a specific contacts folder.
     * 
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     * 
     * @param folderId The identifier of the folder to get the contacts from
     * @return The contacts
     */
    List<Contact> getContacts(String folderId) throws OXException;

    /**
     * Gets all contacts from one or more specific contact folders.
     * 
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     * 
     * @param folderId The identifier of the folder to get the contacts from
     * @return The resulting contacts
     */
    List<Contact> getContactsInFolders(List<String> folderIds) throws OXException;

    /**
     * Gets a list of modified contacts in the specified folder
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * 
     * @param folderId the folder identifier
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts modified on or after this date should be returned.
     * @return The list of modified contacts
     */
    List<Contact> getModifiedContacts(String folderId, Date from) throws OXException;

    /**
     * Gets a list of deleted contacts in the specified folder.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * 
     * @param folderId the folder identifier
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts deleted on or after this date should be returned.
     * @return The list of deleted contacts
     */
    List<Contact> getDeletedContacts(String folderId, Date from) throws OXException;

    /**
     * Gets all contacts from all visible folders.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * 
     * @return the contacts
     */
    List<Contact> getContacts() throws OXException;

    /**
     * Gets a value indicating if the folder with the supplied identifier is empty.
     *
     * @param folderId The ID of the folder to check
     * @return <code>true</code> if the folder is empty, <code>false</code>, otherwise.
     */
    boolean isFolderEmpty(String folderId) throws OXException;

    /**
     * Returns if the provided {@link ContactField}s are supported by the storage. To 'support' the given field the storage should
     * be able to set new values for it. If at least one of the provided fields is not supported <code>false</code> will be
     * returned.
     *
     * @param folderId The ID of the folder to check
     * @param fields the contact fields that should be checked for support
     * @return <code>true</code> if all fields are supported; <code>false</code> if at least one is not supported
     */
    boolean supports(String folderId, ContactField... fields) throws OXException;
}
