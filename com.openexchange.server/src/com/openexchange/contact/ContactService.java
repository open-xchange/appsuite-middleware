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

package com.openexchange.contact;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link ContactService} - Provides access to the contact module.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface ContactService {
	
    /**
     * Contact fields that may be queried from contacts of the global address 
     * list, even if the current session's user has no sufficient access 
     * permissions for that folder. 
     */
	public static final ContactField[] LIMITED_USER_FIELDS = new ContactField[] { ContactField.DISPLAY_NAME, ContactField.GIVEN_NAME, 
			ContactField.SUR_NAME, ContactField.MIDDLE_NAME, ContactField.SUFFIX, ContactField.LAST_MODIFIED };
	
    /**
     * Gets a contact with all fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param id the object ID
     * @return the contact
     * @throws OXException
     */
    Contact getContact(Session session, String folderId, String id) throws OXException;
    
    /**
     * Gets a contact with specified fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param id the object ID
     * @param fields the contact fields that should be retrieved
     * @return the contact
     * @throws OXException
     */
    Contact getContact(Session session, String folderId, String id, ContactField[] fields) throws OXException;
    
    /**
     * Gets all contacts with all fields in a folder.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getAllContacts(Session session, String folderId) throws OXException;

    /**
     * Gets all contacts with all fields in a folder.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getAllContacts(Session session, String folderId, SortOptions sortOptions) throws OXException;

    /**
     * Gets all contacts with specified fields in a folder.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getAllContacts(Session session, String folderId, ContactField[] fields) throws OXException;

    /**
     * Gets all contacts with specified fields in a folder.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getAllContacts(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Gets a list of contacts with all fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param ids the object IDs 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids) throws OXException;

    /**
     * Gets a list of contacts with all fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param ids the object IDs 
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, SortOptions sortOptions) throws OXException;

    /**
     * Gets a list of contacts with specified fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param ids the object IDs 
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, ContactField[] fields) throws OXException;

    /**
     * Gets a list of contacts with specified fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param ids the object IDs 
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Gets a list of deleted contacts in a folder with all fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum deletion time to consider
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since) throws OXException;

    /**
     * Gets a list of deleted contacts in a folder with specified fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum deletion time to consider
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since, ContactField[] fields) throws OXException;

    /**
     * Gets a list of deleted contacts in a folder with specified fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum deletion time to consider
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Gets a list of modified contacts in a folder with all fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum modification time to consider
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since) throws OXException;

    /**
     * Gets a list of modified contacts in a folder with specified fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum modification time to consider
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, ContactField[] fields) throws OXException;

    /**
     * Gets a list of modified contacts in a folder with specified fields.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum modification time to consider
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException;
    
    /**
     * Searches for contacts.
     * 
     * @param session the session
     * @param term the search term
     * @return the contacts found with the search
     * @throws OXException
     */
    <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term) throws OXException;

    /**
     * Searches for contacts.
     * 
     * @param session the session
     * @param term the search term
     * @param sortOptions the options to sort the results 
     * @return the contacts found with the search
     * @throws OXException
     */
    <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, SortOptions sortOptions) throws OXException;

    /**
     * Searches for contacts.
     * 
     * @param session the session
     * @param term the search term
     * @param fields the contact fields that should be retrieved
     * @return the contacts found with the search
     * @throws OXException
     */
    <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields) throws OXException;

    /**
     * Searches for contacts.
     * 
     * @param session the session
     * @param term the search term
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts found with the search
     * @throws OXException
     */
    <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException;
    
    /**
     * Searches for contacts.
     * 
     * @param session the session
     * @param contactSearch the contact search object
     * @return the contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch) throws OXException;

    /**
     * Searches for contacts.
     * 
     * @param session the session
     * @param contactSearch the contact search object
     * @param sortOptions the options to sort the results 
     * @return the contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, SortOptions sortOptions) throws OXException;

    /**
     * Searches for contacts.
     * 
     * @param session the session
     * @param contactSearch the contact search object
     * @param fields the contact fields that should be retrieved
     * @return the contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, ContactField[] fields) throws OXException;

    /**
     * Searches for contacts.
     * 
     * @param session the session
     * @param contactSearch the contact search object
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException;
    
    /**
     * Creates a new contact in a folder.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param contact the contact to create
     * @throws OXException
     */
    void createContact(Session session, String folderId, Contact contact) throws OXException;
    
    /**
     * Updates a contact. 
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param id the object ID
     * @param contact the contact to update
     * @param lastRead the time the object was last read from the storage
     * @throws OXException
     */
    void updateContact(Session session, String folderId, String id, Contact contact, Date lastRead) throws OXException;

    /**
     * Updates a user's contact data, ignoring the folder permissions of the
     * global address book folder. Required to update user data in environments
     * where access to the global address book is restricted.
     *
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param id the object ID
     * @param contact the contact to update
     * @param lastRead the time the object was last read from the storage
     * @throws OXException
     */
    void updateUser(Session session, String folderId, String id, Contact contact, Date lastRead) throws OXException;

    /**
     * Deletes a contact.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param id the object ID
     * @param lastRead the time the object was last read from the storage
     * @throws OXException
     */
    void deleteContact(Session session, String folderId, String id, Date lastRead) throws OXException;

    /**
     * Deletes all contacts in a folder.
     * 
     * @param session the session
     * @param folderId the ID of the parent folder
     * @throws OXException
     */
    void deleteContacts(Session session, String folderId) throws OXException;

    /**
     * Gets a user's contact with all fields.<p>
     * If the current user has no adequate permissions, no exception is thrown, 
     * but the queried contact fields are limited to fields defined by 
     * <code>ContactService.LIMITED_USER_FIELDS</code>.
     *  
     * @param session the session
     * @param userID the user's ID
     * @return the contact
     * @throws OXException
     */
    Contact getUser(Session session, int userID) throws OXException;

	/**
     * Gets a user's contact with specified fields.<p>
     * 
     * If the current user has no adequate permissions, no exception is thrown, 
     * but the queried contact fields are limited to the fields defined by 
     * <code>ContactService.LIMITED_USER_FIELDS</code>.
     * 
     * @param session the session
     * @param userID the user's ID
     * @param fields the contact fields that should be retrieved
     * @return the contact
     * @throws OXException
     */
    Contact getUser(Session session, int userID, ContactField[] fields) throws OXException;
    
    /**
     * Gets a user contacts with all fields.<p>
     * 
     * If the current user has no adequate permissions, no exception is thrown, 
     * but the queried contact fields are limited to the fields defined by 
     * <code>ContactService.LIMITED_USER_FIELDS</code>.
     * 
     * @param session the session
     * @param userIDs the user IDs
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getUsers(Session session, int[] userIDs) throws OXException;

	/**
     * Gets user contacts with specified fields.<p>
     * 
     * If the current user has no adequate permissions, no exception is thrown, 
     * but the queried contact fields are limited to the fields defined by 
     * <code>ContactService.LIMITED_USER_FIELDS</code>.
     * 
     * @param session the session
     * @param userIDs the user IDs
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getUsers(Session session, int[] userIDs, ContactField[] fields) throws OXException;
    
	/**
     * Gets all user contacts with specified fields.<p>
     * 
     * If the current user has no adequate permissions, no exception is thrown, 
     * but the queried contact fields are limited to the fields defined by 
     * <code>ContactService.LIMITED_USER_FIELDS</code>.
     * 
     * @param session the session
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> getAllUsers(Session session, ContactField[] fields, SortOptions sortOptions) throws OXException;
    
    /**
     * Gets the value of the <code>ContactField.COMPANY</code> field from the
     * contact representing the current context's mail admin.
     * 
     * @param session the session
     * @return the organization
     * @throws OXException
     */
    String getOrganization(Session session) throws OXException;

    /**
     * Searches for users.
     * 
     * @param session the session
     * @param term the search term
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the user contacts found with the search
     * @throws OXException
     */
	<O> SearchIterator<Contact> searchUsers(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException;
	
	
    /**
     * Searches for users.
     * 
     * @param session the session
     * @param contactSearch the contact search object
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the user contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> searchUsers(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException;

}
