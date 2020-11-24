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

package com.openexchange.contact.provider.composition;

import java.util.Date;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.common.AccountAwareContactsFolder;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.common.GroupwareFolderType;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactsSearchObject;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tx.TransactionAware;

/**
 * {@link IDBasedContactsAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface IDBasedContactsAccess extends TransactionAware, ContactsParameters {

    /**
     * Gets the session associated with this contacts access instance.
     *
     * @return The session the access was initialized for
     */
    Session getSession();

    /**
     * Gets a list of warnings that occurred during processing.
     *
     * @return A list if warnings, or an empty list if there were none
     */
    List<OXException> getWarnings();

    /**
     * Counts all contacts within the given folder.
     *
     * @param folderId ID of the folder to count in
     * @return the number of contacts
     * @throws OXException if an error is occurred
     */
    int countContacts(String folderId) throws OXException;

    /**
     * Creates a new contact
     *
     * @param folderId The fully qualified identifier of the parent folder to create the contact in
     * @param contact The contact to create
     * @throws OXException if an error is occurred
     */
    void createContact(String folderId, Contact contact) throws OXException;

    /**
     * Updates an existing contact.
     *
     * @param contactId The contact identifier
     * @param contact The contact data to update
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @throws OXException if an error is occurred
     */
    void updateContact(ContactID contactId, Contact contact, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing contact.
     *
     * @param contactId The contact identifier
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @throws OXException if an error is occurred
     */
    void deleteContact(ContactID contactId, long clientTimestamp) throws OXException;

    /**
     * Deletes all contacts in a folder.
     *
     * @param folderId the ID of the parent folder
     * @throws OXException if an error is occurred
     */
    void deleteContacts(String folderId) throws OXException;

    /**
     * Deletes multiple contacts.
     * 
     * @param contactIds The contact identifiers
     * @param clientTimestamp The last know timestamp by the client
     * @throws OXException if an error is occurred
     */
    void deleteContacts(List<ContactID> contactsIds, long clientTimestamp) throws OXException;

    /**
     * Gets a specific contact
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * </ul>
     * 
     * @param contactId The identifier of the contact to get
     * 
     * @return The contact
     * @throws OXException if an error is occurred
     */
    Contact getContact(ContactID contactId) throws OXException;

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
     * @throws OXException if an error is occurred
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
     * @throws OXException if an error is occurred
     */
    List<Contact> getContacts(String folderId) throws OXException;

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
     * @throws OXException if an error is occurred
     */
    List<Contact> getContacts() throws OXException;

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
     * @throws OXException if an error is occurred
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
     * @throws OXException if an error is occurred
     */
    List<Contact> getDeletedContacts(String folderId, Date from) throws OXException;

    ///////////////////////////////////// FOLDERS ////////////////////////////////////

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
     * @throws OXException if an error is occurred
     */
    List<Contact> getContactsInFolders(List<String> folderIds) throws OXException;

    /**
     * Gets a list of all visible contacts folders.
     *
     * @param type The type to get the visible folders for
     * @return A list of all visible contacts folders of the type
     * @throws OXException if an error is occurred
     */
    List<AccountAwareContactsFolder> getVisibleFolders(GroupwareFolderType type) throws OXException;

    /**
     * Gets multiple contacts folders.
     *
     * @param folderIds The fully qualified identifiers of the folders to get
     * @return The contacts folders (including information about the underlying account)
     * @throws OXException if an error is occurred
     */
    List<AccountAwareContactsFolder> getFolders(List<String> folderIds) throws OXException;

    /**
     * Gets a specific contacts folder.
     *
     * @param folderId The fully qualified identifier of the folder to get
     * @return The contacts folder (including information about the underlying account)
     * @throws OXException if an error is occurred
     */
    AccountAwareContactsFolder getFolder(String folderId) throws OXException;

    /**
     * Gets the user's default contacts folder.
     *
     * @return The default contacts folder
     * @throws OXException if an error is occurred
     */
    ContactsFolder getDefaultFolder() throws OXException;

    /**
     * Create a new contacts folder.
     * <p/>
     * Depending on the capabilities of the targeted contacts provider, either a new subfolder is created within an existing contacts
     * account (of a {@link ContactsFolderProvider}), or a new contacts account representing a contacts subscription (of a
     * {@link BasicContactsProvider}) is created implicitly, resulting in a new virtual folder.
     *
     * @param providerId The fully qualified identifier of the parent folder, or <code>null</code> if not needed
     * @param folder contacts folder data to take over for the new contacts account
     * @param userConfig Arbitrary user configuration data for the new contacts account, or <code>null</code> if not needed
     * @return The fully qualified identifier of the newly created folder
     * @throws OXException if an error is occurred
     */
    String createFolder(String providerId, ContactsFolder folder, JSONObject userConfig) throws OXException;

    /**
     * Updates a contacts folder.
     * <p/>
     * Depending on the capabilities of the underlying contacts provider, also arbitrary account properties can be updated.
     *
     * @param folderId The fully qualified identifier of the folder to update
     * @param folder The updated contacts folder data
     * @param userConfig Arbitrary user configuration data for the contacts account, or <code>null</code> if not needed
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The (possibly changed) fully qualified identifier of the updated folder
     * @throws OXException if an error is occurred
     */
    String updateFolder(String folderId, ContactsFolder folder, JSONObject userConfig, long clientTimestamp) throws OXException;

    /**
     * Deletes a contacts folder.
     *
     * @param folderId The fully qualified identifier of the folder to delete
     * @throws OXException if an error is occurred
     */
    void deleteFolder(String folderId, long clientTimestamp) throws OXException;

    /**
     * Gets a value indicating if the folder with the supplied identifier is empty.
     *
     * @param folderId The ID of the folder to check
     * @return <code>true</code> if the folder is empty, <code>false</code>, otherwise.
     * @throws OXException if an error is occurred
     */
    boolean isFolderEmpty(String folderId) throws OXException;

    /**
     * Gets a value indicating if the folder with the supplied identifier contains foreign objects, i.e. contacts that were not created
     * by the current session's user.
     *
     * @param folderId The ID of the folder to check
     * @return <code>true</code> if the folder contains foreign objects, <code>false</code>, otherwise.
     * @throws OXException if an error is occurred
     */
    boolean containsForeignObjectInFolder(String folderId) throws OXException;

    /**
     * Returns if the provided {@link ContactField}s are supported by the storage. To 'support' the given field the storage
     * should be able to set new values for it. If at least one of the provided fields is not supported <code>false</code> will be
     * returned.
     *
     * @param folderId The ID of the folder to check
     * @param fields the contact fields that should be checked for support
     * @return <code>true</code> if all fields are supported; <code>false</code> if at least one is not supported
     * @throws OXException if an error is occurred
     */
    boolean supports(String folderId, ContactField... fields) throws OXException;

    ////////////////////////////////////// SEARCH ///////////////////////////////////////

    /**
     * Searches for contacts.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link ContactsParameters#PARAMETER_EXCLUDE_ADMIN}</li>
     * </ul>
     *
     * @param term the search term
     * @return the contacts found with the search
     * @throws OXException if an error is occurred
     */
    <O> List<Contact> searchContacts(SearchTerm<O> term) throws OXException;

    /**
     * Searches for contacts.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link ContactsParameters#PARAMETER_EXCLUDE_ADMIN}</li>
     * </ul>
     *
     * @param contactSearch the contact search object
     * @return the contacts found with the search
     * @throws OXException if an error is occurred
     */
    List<Contact> searchContacts(ContactsSearchObject contactSearch) throws OXException;

    /**
     * Performs an "auto-complete" lookup for contacts.
     * 
     * @param query The search query as supplied by the client
     * @param parameters The additional parameters to refine the auto-complete search. Don't pass <code>null</code> here,
     *            but use an empty instance to use the default parameter values.
     * @return The contacts found with the search
     * @throws OXException if an error is occurred
     *
     * @see {@link AutocompleteParameters#newInstance()}
     */
    List<Contact> autocompleteContacts(String query, AutocompleteParameters parameters) throws OXException;

    /**
     * Performs an "auto-complete" lookup for contacts. Depending <code>com.openexchange.contacts.allFoldersForAutoComplete</code>, either
     * all folders visible to the user, or a reduced set of specific folders is used for the search.
     *
     * @param folderIDs A list of folder IDs to restrict the search to
     * @param query The search query as supplied by the client
     * @param parameters The additional parameters to refine the auto-complete search. Don't pass <code>null</code> here,
     *            but use an empty instance to use the default parameter values.
     * @return The contacts found with the search
     * @throws OXException if an error is occurred
     */
    List<Contact> autocompleteContacts(List<String> folderIds, String query, AutocompleteParameters parameters) throws OXException;

    /**
     * Searches for contacts whose birthday falls into the specified period.
     * 
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     * 
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose birthdays start on or after this date should be returned.
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose birthdays end before this date should be returned.
     * @return the contacts found with the search
     * @throws OXException if an error is occurred
     */
    List<Contact> searchContactsWithBirthday(Date from, Date until) throws OXException;

    /**
     * Searches for contacts whose birthday falls into the specified period.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     * 
     * @param folderIDs the IDs of the parent folders
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose birthdays start on or after this date should be returned.
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose birthdays end before this date should be returned.
     * @return the contacts found with the search
     * @throws OXException if an error is occurred
     */
    List<Contact> searchContactsWithBirthday(List<String> folderIDs, Date from, Date until) throws OXException;

    /**
     * Searches for contacts whose anniversary falls into the specified period.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     * 
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose anniversaries start on or after this date should be returned.
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose anniversaries end before this date should be returned.
     * @return the contacts found with the search
     * @throws OXException if an error is occurred
     */
    List<Contact> searchContactsWithAnniversary(Date from, Date until) throws OXException;

    /**
     * Searches for contacts whose anniversary falls into the specified period.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     * 
     * @param folderIDs the IDs of the parent folders
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose anniversaries start on or after this date should be returned.
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose anniversaries end before this date should be returned.
     * @return the contacts found with the search
     * @throws OXException if an error is occurred
     */
    List<Contact> searchContactsWithAnniversary(List<String> folderIDs, Date from, Date until) throws OXException;
}
