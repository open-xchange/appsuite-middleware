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

package com.openexchange.contact.storage;

import java.util.Date;
import java.util.List;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ContactStorage}
 *
 * Basic methods for storing and accessing {@link Contact}s.
 * <p/>
 * <b>Remarks for custom implementations:</b>
 * <ul><li>
 * To add a custom {@link ContactStorage} implementation, extend the abstract
 * class {@link DefaultContactStorage} and implement the needed methods. Then,
 * register an instance as a <code>ContactStorage.class</code> service during
 * activation to make the storage known to the contact service during runtime.
 * </li><li>
 * Before accessing the storage, the service will always query the storage if
 * it supports the folder ID, so the storage needs to know whether it is
 * responsible for a specific folder ID or not. A storage can be responsible
 * for multiple folder IDs.
 * </li><li>
 * To abstract from the column IDs as used by the HTTP API, the contact
 * storage uses the {@link ContactField} enumeration exclusively to indicate
 * which contact properties are queried from the storage. Therefore, a storage-
 * internal mapping of contact properties to the storage-representation
 * might be useful. As a starting point, base classes for mapping operations
 * can be found in the package <code>com.openexchange.groupware.tools.mappings
 * </code>.
 * </li><li>
 * While the interface uses {@link String} values for identifiers, the {@link Contact} class still expects them to be in a numerical format for
 * legacy reasons. Therefore, extensive parsing of identifiers may be necessary
 * for now. A convenience <code>parse</code>-method for handling numerical
 * identifiers is available in {@link DefaultContactStorage}.
 * </li><li>
 * The contact storage does not need to check permissions when accessing the
 * storage, since the contact service already takes care of checking the
 * current user's access rights to the folder and it's contents. However, the
 * current session is still passed down to the storage for possible
 * authentication requirements.
 * </li><li>
 * It's up to the storage to provide a history of deleted objects. While it is
 * not used to actually 'restore' deleted objects, these information are needed
 * for synchronization purposes ("get objects deleted since..."). Therefore, at
 * least the properties that identify an object are required here (i.e.
 * timestamps and different identifiers).
 * </li><li>
 * Possible exceptions thrown in the storage must extend the {@link OXException} base class. A storage might either define it's own
 * exceptions and/or use the pre-defined ones found at {@link ContactExceptionCodes}.
 * </li><li>
 * As a general convention, all passed and returned object references are
 * assumed to be non-<code>null</code> unless otherwise specified.
 * </li></ul>
 *
 * @see DefaultContactStorage
 * @see ContactField
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface ContactStorage {

    /**
     * Gets a value indicating whether the storage supports a folder or not.
     *
     * @param session the session
     * @param folderId the ID of the folder to check the support for
     * @return <code>true</code>, if the folder is supported, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean supports(Session session, String folderId) throws OXException;

    /**
     * Gets the priority of the storage that becomes relevant when multiple
     * storages pretend to support the same folder ID. A higher value means a
     * higher priority.
     *
     * @return the priority
     */
    int getPriority();

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
    Contact get(Session session, String folderId, String id, ContactField[] fields) throws OXException;

    /**
     * Counts all contacts within the given folder.
     *
     * @param session the session
     * @param folderId the ID of the folder
     * @param canReadAll Whether the requesting user is allowed to see all objects within the folder.
     *            If set to <code>true</code> all objects should be counted except the ones that are marked as private.
     *            If set to <code>false</code>, only objects that have been created by the user may be counted.
     * @return the number of contacts
     * @throws OXException
     */
    int count(Session session, String folderId, boolean canReadAll) throws OXException;

    /**
     * Gets all contacts with specified fields in a folder.
     *
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param fields the contact fields that should be retrieved
     * @return the contacts
     * @throws OXException
     */
    SearchIterator<Contact> all(Session session, String folderId, ContactField[] fields) throws OXException;

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
    SearchIterator<Contact> all(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException;

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
    SearchIterator<Contact> list(Session session, String folderId, String[] ids, ContactField[] fields) throws OXException;

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
    SearchIterator<Contact> list(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException;

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
    SearchIterator<Contact> deleted(Session session, String folderId, Date since, ContactField[] fields) throws OXException;

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
    SearchIterator<Contact> deleted(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException;

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
    SearchIterator<Contact> modified(Session session, String folderId, Date since, ContactField[] fields) throws OXException;

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
    SearchIterator<Contact> modified(Session session, String folderID, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Searches for contacts.
     *
     * @param session the session
     * @param term the search term
     * @param fields the contact fields that should be retrieved
     * @return the contacts found with the search
     * @throws OXException
     */
    <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields) throws OXException;

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
    <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Searches for contacts.
     *
     * @param session the session
     * @param contactSearch the contact search object
     * @param fields the contact fields that should be retrieved
     * @return the contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> search(Session session, ContactSearchObject contactSearch, ContactField[] fields) throws OXException;

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
    SearchIterator<Contact> search(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Creates a new contact in a folder.
     *
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param contact the contact to create
     * @throws OXException
     */
    void create(Session session, String folderId, Contact contact) throws OXException;

    /**
     * Creates a new contact in a folder and tries to persist the given VCard. The storage below is responsible for a correct persistence of the given VCard. The caller will be informed by the returned boolean if persisting the VCard has been
     * successful.
     *
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param contact the contact to create
     * @param vCard the VCard to persist
     * @return true, if saving the vCard has been successful. Otherwise false
     * @throws OXException
     */
    boolean create(Session session, String folderId, Contact contact, String vCard) throws OXException;

    /**
     * Updates a contact.
     *
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param contact the contact to update
     * @param lastRead the time the object was last read from the storage
     * @throws OXException
     */
    void update(Session session, String folderId, String id, Contact contact, Date lastRead) throws OXException;

    /**
     * Updates an existing contact in a folder and to persist the given VCard. The storage below is responsible for a correct persistence of the given VCard. The caller will be informed by the returned boolean if persisting the VCard has been
     * successful.
     *
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param contact the contact to update
     * @param lastRead the time the object was last read from the storage
     * @param vCard the VCard to persist
     * @return true, if saving the vCard has been successful. Otherwise false
     * @throws OXException
     */
    boolean update(Session session, String folderId, String id, Contact contact, Date lastRead, String vCard) throws OXException;

    /**
     * Updates references to the supplied contact. This method is called
     * after a contact has been updated to propagate the changes throughout
     * all storages, e.g. to update distribution lists that are holding
     * references to the updated contact.
     *
     * @param session the session
     * @param originalContact the original contact
     * @param updatedContact the updated contact
     * @throws OXException
     */
    void updateReferences(Session session, Contact originalContact, Contact updatedContact) throws OXException;

    /**
     * Deletes a contact.
     *
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param id the object ID
     * @param lastRead the time the object was last read from the storage
     * @throws OXException
     */
    void delete(Session session, String folderId, String id, Date lastRead) throws OXException;

    /**
     * Deletes multiple contacts.
     *
     * @param session the session
     * @param folderId the ID of the parent folder
     * @param ids the object IDs
     * @param lastRead the time the objects were last read from the storage
     * @throws OXException
     */
    void delete(Session session, String folderId, String[] ids, Date lastRead) throws OXException;

    /**
     * Deletes all contacts in a folder.
     *
     * @param session the session
     * @param folderId the ID of the parent folder
     * @throws OXException
     */
    void delete(Session session, String folderId) throws OXException;

    /**
     * Searches for contacts whose birthday falls into the specified period.
     *
     * @param session the session
     * @param folderIDs the IDs of the parent folder
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results
     * @return the contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> searchByBirthday(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Searches for contacts whose anniversary falls into the specified period.
     *
     * @param session the session
     * @param folderIDs the IDs of the parent folder
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @param fields the contact fields that should be retrieved
     * @param sortOptions the options to sort the results
     * @return the contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> searchByAnniversary(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Performs an optimized "auto-complete" lookup for contacts.
     *
     * @param session The session
     * @param folderIDs A list of folder IDs to restrict the search to
     * @param query The search query as supplied by the client
     * @param parameters The additional parameters to refine the auto-complete search. If possible parameters are missing,
     *            their default values must be used (see JavaDoc of the common parameter keys in {@link AutocompleteParameters}).
     * @param fields The contact fields that should be retrieved
     * @param sortOptions The options to sort the results
     * @return The contacts found with the search
     * @throws OXException
     */
    SearchIterator<Contact> autoComplete(Session session, List<String> folderIDs, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException;

    /**
     * Returns if the provided {@link ContactField}s are supported by the storage. To 'support' the given field the storage should be able to set new values for it. If at least one of the provided fields is not supported <code>false</code> will be
     * returned.
     *
     * @param fields The fields to check
     * @return <code>true</code> if all provided fields are supported, otherwise <code>false</code>
     * @throws OXException
     */
    boolean supports(ContactField... fields) throws OXException;

}
