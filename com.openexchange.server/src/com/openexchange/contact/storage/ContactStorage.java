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

package com.openexchange.contact.storage;

import java.util.Date;

import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.search.SearchTerm;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ContactStorage} - Basic methods for storing and accessing {@link Contact}s.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface ContactStorage {
    
    /**
     * Gets a value indicating whether the storage supports a folder or not.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the folder to check the support for
     * @return <code>true</code>, if the folder is supported, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean supports(int contextID, String folderId) throws OXException;
    
    /**
     * Gets a contact with specified fields.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param id the object ID
     * @param fields the contact fields that should be retrieved
     * @return the contact
     * @throws OXException
     */
    Contact get(int contextID, String folderId, String id, ContactField[] fields) throws OXException;
    
    /**
     * Gets all contacts with specified fields in a folder.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> all(int contextID, String folderId, ContactField[] fields) throws OXException;

    /**
     * Gets all contacts with specified fields in a folder.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> all(int contextID, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Gets a list of contacts with specified fields.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param ids the object IDs 
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> list(int contextID, String folderId, String[] ids, ContactField[] fields) throws OXException;

    /**
     * Gets a list of contacts with specified fields.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param ids the object IDs 
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> list(int contextID, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Gets a list of deleted contacts in a folder with specified fields.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum deletion time to consider
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> deleted(int contextID, String folderId, Date since, ContactField[] fields) throws OXException;

    /**
     * Gets a list of deleted contacts in a folder with specified fields.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum deletion time to consider
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> deleted(int contextID, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Gets a list of modified contacts in a folder with specified fields.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum modification time to consider
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> modified(int contextID, String folderId, Date since, ContactField[] fields) throws OXException;

    /**
     * Gets a list of modified contacts in a folder with specified fields.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param since the exclusive minimum modification time to consider
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts
     * @throws OXException
     */
	SearchIterator<Contact> modified(int contextID, String folderID, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Searches for contacts.
     * 
     * @param contextID the context ID
     * @param term the search term
     * @param fields the contact fields that should be retrieved
     * @return the contacts found with the search
     * @throws OXException
     */
    <O> SearchIterator<Contact> search(int contextID, SearchTerm<O> term, ContactField[] fields) throws OXException;

    /**
     * Searches for contacts.
     * 
     * @param contextID the context ID
     * @param term the search term
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts found with the search
     * @throws OXException
     */
    <O> SearchIterator<Contact> search(int contextID, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException;
    
    /**
     * Searches for contacts.
     * 
     * @param contextID the context ID
     * @param contactSearch the contact search object
     * @param fields the contact fields that should be retrieved
     * @return the contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> search(int contextID, ContactSearchObject contactSearch, ContactField[] fields) throws OXException;

    /**
     * Searches for contacts.
     * 
     * @param contextID the context ID
     * @param contactSearch the contact search object
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results 
     * @return the contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> search(int contextID, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException;
    
    /**
     * Creates a new contact in a folder.
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param contact the contact to create
     * @throws OXException
     */
    void create(int contextID, String folderId, Contact contact) throws OXException;
    
    /**
     * Updates a contact. 
     * 
     * @param contextID the context ID
     * @param folderId the ID of the parent folder
     * @param contact the contact to update
     * @param lastRead the time the object was last read from the storage
     * @throws OXException
     */
    void update(int contextID, String folderId, String id, Contact contact, Date lastRead) throws OXException;

    /**
     * Updates references to the supplied contact. This method is called for 
     * after a contact has been updated to propagate the changes throughout 
     * all storages, e.g. to update referenced distribution list members.
     * 
     * @param contextID the context ID
     * @param contact the contact that has been updated
     * @throws OXException
     */
    void updateReferences(int contextID, Contact contact) throws OXException;
    	
    /**
     * Deletes a contact.
     * 
     * @param contextID the context ID
     * @param userID the ID of the user performing the operation (to allow storing the id in 'modified by')
     * @param folderId the ID of the parent folder
     * @param id the object ID
     * @param lastRead the time the object was last read from the storage
     * @throws OXException
     */
    void delete(int contextID, int userID, String folderId, String id, Date lastRead) throws OXException;
    
}
