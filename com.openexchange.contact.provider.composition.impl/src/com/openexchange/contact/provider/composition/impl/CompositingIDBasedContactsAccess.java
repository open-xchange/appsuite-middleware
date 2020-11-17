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

package com.openexchange.contact.provider.composition.impl;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableList;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.common.AccountAwareContactsFolder;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.ContactsPermission;
import com.openexchange.contact.common.DefaultContactsFolder;
import com.openexchange.contact.common.DefaultContactsPermission;
import com.openexchange.contact.common.ExtendedProperties;
import com.openexchange.contact.common.GroupwareContactsFolder;
import com.openexchange.contact.common.GroupwareFolderType;
import com.openexchange.contact.common.UsedForSync;
import com.openexchange.contact.provider.ContactsAccess;
import com.openexchange.contact.provider.ContactsAccessCapability;
import com.openexchange.contact.provider.ContactsAccountService;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.provider.ContactsProviderRegistry;
import com.openexchange.contact.provider.ContactsProviders;
import com.openexchange.contact.provider.GroupwareContactsProvider;
import com.openexchange.contact.provider.basic.BasicContactsAccess;
import com.openexchange.contact.provider.basic.BasicContactsProvider;
import com.openexchange.contact.provider.basic.ContactsSettings;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.impl.idmangling.IDMangler;
import com.openexchange.contact.provider.extensions.BasicSearchAware;
import com.openexchange.contact.provider.folder.ContactsFolderProvider;
import com.openexchange.contact.provider.folder.FolderContactsAccess;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.ContactsSearchObject;
import com.openexchange.java.Strings;
import com.openexchange.search.SearchTerm;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositingIDBasedContactsAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class CompositingIDBasedContactsAccess extends AbstractCompositingIDBasedContactsAccess implements IDBasedContactsAccess {

    private static final Logger LOG = LoggerFactory.getLogger(CompositingIDBasedContactsAccess.class);

    /**
     * Initialises a new {@link CompositingIDBasedContactsAccess}.
     * 
     * @param session the session
     * @param providerRegistry The provider registry
     * @param services The {@link ServiceLookup} instance
     * @throws OXException
     */
    public CompositingIDBasedContactsAccess(Session session, ContactsProviderRegistry providerRegistry, ServiceLookup services) throws OXException {
        super(session, providerRegistry, services);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    @Override
    public int countContacts(String folderId) throws OXException {
        ContactsAccount account = getAccount(IDMangler.getAccountId(folderId), true);
        if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
            return getBasicAccess(account).countContacts();
        } else if (isTypedProvider(account.getProviderId(), FolderContactsAccess.class)) {
            return getFolderAccess(account).countContacts(folderId);
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    @Override
    public void createContact(String folderId, Contact contact) throws OXException {
        getGroupwareAccess(IDMangler.getAccountId(folderId)).createContact(IDMangler.getRelativeFolderId(folderId), contact);
    }

    @Override
    public void updateContact(ContactID contactId, Contact contact, long clientTimestamp) throws OXException {
        getGroupwareAccess(IDMangler.getAccountId(contactId.getFolderID())).updateContact(contactId, contact, clientTimestamp);
    }

    @Override
    public void deleteContact(ContactID contactId, long clientTimestamp) throws OXException {
        getGroupwareAccess(IDMangler.getAccountId(contactId.getFolderID())).deleteContact(contactId, clientTimestamp);
    }

    @Override
    public void deleteContacts(List<ContactID> contactsIds, long clientTimestamp) throws OXException {
        for (Entry<Integer, List<ContactID>> entry : IDMangler.getRelativeIdsPerAccountId(contactsIds).entrySet()) {
            getGroupwareAccess(getAccount(i(entry.getKey()), true).getAccountId()).deleteContacts(entry.getValue(), clientTimestamp);
        }
    }

    @Override
    public void deleteContacts(String folderId) throws OXException {
        getGroupwareAccess(IDMangler.getAccountId(folderId)).deleteContacts(folderId);
    }

    @Override
    public Contact getContact(ContactID contactId) throws OXException {
        String folderId = contactId.getFolderID();

        ContactsAccount account = getAccount(IDMangler.getAccountId(folderId), true);
        if (isTypedProvider(account.getProviderId(), ContactsFolderProvider.class)) {
            return IDMangler.withUniqueID(getFolderAccess(account).getContact(IDMangler.getRelativeFolderId(folderId), contactId.getObjectID()), account.getAccountId());
        }
        if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
            return IDMangler.withUniqueID(getBasicAccess(account).getContact(contactId.getObjectID()), account.getAccountId());
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    @Override
    public List<Contact> getContacts(List<ContactID> contactIDs) throws OXException {
        Map<Integer, List<ContactID>> idsPerAccountId = IDMangler.getRelativeIdsPerAccountId(contactIDs);
        Map<Integer, List<Contact>> contactsPerAccountId = new HashMap<>(idsPerAccountId.size());
        for (Entry<Integer, List<ContactID>> entry : idsPerAccountId.entrySet()) {
            ContactsAccount account = getAccount(i(entry.getKey()), true);
            if (isTypedProvider(account.getProviderId(), ContactsFolderProvider.class)) {
                contactsPerAccountId.put(I(account.getAccountId()), getFolderAccess(account).getContacts(entry.getValue()));
            } else if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
                parentFolderMatches(entry.getValue(), BasicContactsAccess.FOLDER_ID);
                contactsPerAccountId.put(I(account.getAccountId()), getBasicAccess(account).getContacts(entry.getValue()));
            } else {
                throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
            }
        }

        List<Contact> contacts = new ArrayList<>(contactIDs.size());
        for (ContactID requestedID : contactIDs) {
            int accountId = IDMangler.getAccountId(requestedID.getFolderID());
            Optional<Contact> optional = find(contactsPerAccountId.get(I(accountId)), IDMangler.getRelativeId(requestedID));
            optional.ifPresent(contact -> contacts.add(IDMangler.withUniqueID(contact, accountId)));
        }
        return contacts;
    }

    @Override
    public List<Contact> getContacts(String folderId) throws OXException {
        int accountId = IDMangler.getAccountId(folderId);
        ContactsAccount account = getAccount(accountId, true);
        if (isTypedProvider(account.getProviderId(), ContactsFolderProvider.class)) {
            return IDMangler.withUniqueIDs(getFolderAccess(account).getContacts(IDMangler.getRelativeFolderId(folderId)), accountId);
        }
        if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
            return IDMangler.withUniqueIDs(getBasicAccess(account).getContacts(), accountId);
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    @Override
    public List<Contact> getDeletedContacts(String folderId, Date from) throws OXException {
        ContactsAccount account = getAccount(IDMangler.getAccountId(folderId), true);
        if (isTypedProvider(account.getProviderId(), ContactsFolderProvider.class)) {
            return IDMangler.withUniqueIDs(getFolderAccess(account).getDeletedContacts(IDMangler.getRelativeFolderId(folderId), from), account.getAccountId());
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    @Override
    public List<Contact> getModifiedContacts(String folderId, Date from) throws OXException {
        ContactsAccount account = getAccount(IDMangler.getAccountId(folderId), true);
        if (isTypedProvider(account.getProviderId(), ContactsFolderProvider.class)) {
            return IDMangler.withUniqueIDs(getFolderAccess(account).getModifiedContacts(IDMangler.getRelativeFolderId(folderId), from), account.getAccountId());
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    @Override
    public List<Contact> getContacts() throws OXException {
        List<ContactsAccount> accounts = getAccounts();
        List<Contact> contacts = new LinkedList<>();

        for (ContactsAccount account : accounts) {
            if (isTypedProvider(account.getProviderId(), ContactsFolderProvider.class)) {
                contacts.addAll(IDMangler.withUniqueIDs(getFolderAccess(account).getContacts(), account.getAccountId()));
            } else if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
                contacts.addAll(IDMangler.withUniqueIDs(getBasicAccess(account).getContacts(), account.getAccountId()));
            } else {
                throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
            }
        }
        return contacts;
    }

    @Override
    public List<Contact> getContactsInFolders(List<String> folderIds) throws OXException {
        List<Contact> contacts = new LinkedList<>();
        Map<ContactsAccount, List<String>> relativeFolderIdsPerAccount = getRelativeFolderIdsPerAccount(folderIds);

        if (1 == relativeFolderIdsPerAccount.size()) {
            Entry<ContactsAccount, List<String>> entry = relativeFolderIdsPerAccount.entrySet().iterator().next();
            contacts.addAll(getContactsInFolders(entry.getKey(), entry.getValue()));
        } else {
            CompletionService<List<Contact>> completionService = getCompletionService();
            for (Entry<ContactsAccount, List<String>> entry : relativeFolderIdsPerAccount.entrySet()) {
                completionService.submit(() -> getContactsInFolders(entry.getKey(), entry.getValue()));
            }
            contacts.addAll(collectContacts(completionService, relativeFolderIdsPerAccount.size()));
        }
        return contacts;
    }

    ////////////////////////////////// FOLDERS ////////////////////////////////

    @Override
    public List<AccountAwareContactsFolder> getVisibleFolders(GroupwareFolderType type) throws OXException {
        List<AccountAwareContactsFolder> folders = new LinkedList<>();

        for (ContactsAccount account : getAccounts()) {
            try {
                folders.addAll(IDMangler.withUniqueID(getVisibleFolders(account, type), account));
            } catch (OXException e) {
                throw IDMangler.withUniqueIDs(e, account.getAccountId());
            }
        }
        return folders;
    }

    @Override
    public List<AccountAwareContactsFolder> getFolders(List<String> folderIds) throws OXException {
        Map<ContactsAccount, List<String>> foldersPerAccount = getRelativeFolderIdsPerAccount(folderIds);
        if (foldersPerAccount.isEmpty()) {
            return Collections.emptyList();
        }
        List<AccountAwareContactsFolder> folders = new ArrayList<>(folderIds.size());

        for (Map.Entry<ContactsAccount, List<String>> entry : foldersPerAccount.entrySet()) {
            ContactsAccount account = entry.getKey();
            try {
                for (String folderId : entry.getValue()) {
                    folders.add(IDMangler.withUniqueID(getFolder(account, folderId), account));
                }
            } catch (OXException e) {
                throw IDMangler.withUniqueIDs(e, account.getAccountId());
            }
        }
        return folders;
    }

    @Override
    public AccountAwareContactsFolder getFolder(String folderId) throws OXException {
        ContactsAccount account = getAccount(IDMangler.getAccountId(folderId), false);
        try {
            return IDMangler.withUniqueID(getFolder(account, IDMangler.getRelativeFolderId(folderId)), account);
        } catch (OXException e) {
            throw IDMangler.withUniqueIDs(e, account.getAccountId());
        }
    }

    @Override
    public ContactsFolder getDefaultFolder() throws OXException {
        try {
            GroupwareContactsFolder defaultFolder = getGroupwareAccess(ContactsAccount.DEFAULT_ACCOUNT).getDefaultFolder();
            return IDMangler.withUniqueID(defaultFolder, ContactsAccount.DEFAULT_ACCOUNT);
        } catch (OXException e) {
            throw IDMangler.withUniqueIDs(e, ContactsAccount.DEFAULT_ACCOUNT.getAccountId());
        }
    }

    @Override
    public String createFolder(String providerId, ContactsFolder folder, JSONObject userConfig) throws OXException {
        // Create folder within matching folder-aware account targeted by parent folder if set
        String parentFolderId = GroupwareContactsFolder.class.isInstance(folder) ? ((GroupwareContactsFolder) folder).getParentId() : null;

        if (Strings.isNotEmpty(parentFolderId)) {
            int accountId = IDMangler.getAccountId(parentFolderId);
            ContactsAccount existingAccount = optAccount(accountId);
            if (null != existingAccount && (null == providerId || providerId.equals(existingAccount.getProviderId()))) {
                try {
                    String folderId = getAccess(accountId, FolderContactsAccess.class).createFolder(IDMangler.withRelativeID(folder));
                    return IDMangler.getUniqueFolderId(existingAccount.getAccountId(), folderId);
                } catch (OXException e) {
                    throw IDMangler.withUniqueIDs(e, ContactsAccount.DEFAULT_ACCOUNT.getAccountId());
                }
            }
        }
        // Dynamically create new account for provider, otherwise throw
        if (null == providerId) {
            throw ContactsProviderExceptionCodes.MANDATORY_FIELD.create("provider");
        }
        ContactsSettings settings = getBasicContactsSettings(folder, userConfig);
        ContactsAccount newAccount = requireService(ContactsAccountService.class, services).createAccount(session, providerId, settings, this);
        return IDMangler.getUniqueFolderId(newAccount.getAccountId(), BasicContactsAccess.FOLDER_ID);
    }

    @Override
    public String updateFolder(String folderId, ContactsFolder folder, JSONObject userConfig, long clientTimestamp) throws OXException {
        int accountId = IDMangler.getAccountId(folderId);
        try {
            ContactsAccess contactsAccess = getAccess(accountId);
            if (FolderContactsAccess.class.isInstance(contactsAccess)) {
                // Update folder directly within folder-aware account
                String updatedId = ((FolderContactsAccess) contactsAccess).updateFolder(IDMangler.getRelativeFolderId(folderId), IDMangler.withRelativeID(folder), clientTimestamp);
                return IDMangler.getUniqueFolderId(accountId, updatedId);
            }
            // update account settings
            folderMatches(IDMangler.getRelativeFolderId(folderId), BasicContactsAccess.FOLDER_ID);
            ContactsSettings settings = getBasicContactsSettings(folder, userConfig);
            ContactsAccount updatedAccount = requireService(ContactsAccountService.class, services).updateAccount(session, accountId, clientTimestamp, settings, this);
            return IDMangler.getUniqueFolderId(updatedAccount.getAccountId(), BasicContactsAccess.FOLDER_ID);
        } catch (OXException e) {
            throw IDMangler.withUniqueIDs(e, accountId);
        }
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        int accountId = IDMangler.getAccountId(folderId);
        try {
            if (isContactsFolderProvider(accountId)) {
                // Delete folder in contacts account
                getFolderAccess(accountId).deleteFolder(IDMangler.getRelativeFolderId(folderId), clientTimestamp);
                return;
            }
            // Delete whole contacts account if not folder-aware
            folderMatches(IDMangler.getRelativeFolderId(folderId), BasicContactsAccess.FOLDER_ID);
            requireService(ContactsAccountService.class, services).deleteAccount(session, accountId, clientTimestamp, this);
        } catch (OXException e) {
            throw IDMangler.withUniqueIDs(e, accountId);
        }
    }

    @Override
    public boolean isFolderEmpty(String folderId) throws OXException {
        int accountId = IDMangler.getAccountId(folderId);
        if (isContactsFolderProvider(accountId)) {
            return getFolderAccess(accountId).isFolderEmpty(folderId);
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(getAccount(accountId).getProviderId());
    }

    @Override
    public boolean containsForeignObjectInFolder(String folderId) throws OXException {
        int accountId = IDMangler.getAccountId(folderId);
        if (isContactsFolderProvider(accountId)) {
            return getGroupwareAccess(accountId).containsForeignObjectInFolder(folderId);
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(getAccount(accountId).getProviderId());
    }

    @Override
    public boolean supports(String folderId, ContactField... fields) throws OXException {
        int accountId = IDMangler.getAccountId(folderId);
        if (isContactsFolderProvider(accountId)) {
            return getFolderAccess(accountId).supports(folderId, fields);
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(getAccount(accountId).getProviderId());
    }
    //////////////////////////////////// SEARCH ///////////////////////////////

    @Override
    public <O> List<Contact> searchContacts(SearchTerm<O> term) throws OXException {
        List<ContactsAccount> accounts = getAccounts(ContactsAccessCapability.SEARCH);
        if (accounts.isEmpty()) {
            return ImmutableList.of();
        }
        if (accounts.size() == 1) {
            return searchContacts(accounts.get(0), term);
        }
        CompletionService<List<Contact>> completionService = getCompletionService();
        for (ContactsAccount account : accounts) {
            completionService.submit(() -> searchContacts(account, term));
        }
        return collectContacts(completionService, accounts.size());
    }

    @Override
    public List<Contact> searchContacts(ContactsSearchObject contactSearch) throws OXException {
        List<ContactsAccount> accounts = getAccounts(ContactsAccessCapability.SEARCH);
        if (accounts.isEmpty()) {
            return ImmutableList.of();
        }
        if (accounts.size() == 1) {
            return searchContacts(accounts.get(0), contactSearch);
        }
        CompletionService<List<Contact>> completionService = getCompletionService();
        for (ContactsAccount account : accounts) {
            completionService.submit(() -> searchContacts(account, contactSearch));
        }
        return collectContacts(completionService, accounts.size());
    }

    @Override
    public List<Contact> autocompleteContacts(List<String> folderIds, String query, AutocompleteParameters parameters) throws OXException {
        if (folderIds == null || folderIds.isEmpty()) {
            return autocompleteContacts(query, parameters);
        }
        Map<ContactsAccount, List<String>> foldersPerAccount = getRelativeFolderIdsPerAccount(folderIds);
        if (foldersPerAccount.size() == 1) {
            Entry<ContactsAccount, List<String>> entry = foldersPerAccount.entrySet().iterator().next();
            return autocompleteContacts(entry.getKey(), query, parameters);
        }
        CompletionService<List<Contact>> completionService = getCompletionService();
        for (Entry<ContactsAccount, List<String>> entry : foldersPerAccount.entrySet()) {
            completionService.submit(() -> autocompleteContacts(entry.getKey(), query, parameters));
        }
        return collectContacts(completionService, foldersPerAccount.size());
    }

    @Override
    public List<Contact> autocompleteContacts(String query, AutocompleteParameters parameters) throws OXException {
        List<ContactsAccount> accounts = getAccounts(ContactsAccessCapability.SEARCH);
        if (accounts.isEmpty()) {
            return ImmutableList.of();
        }
        if (accounts.size() == 1) {
            return autocompleteContacts(accounts.get(0), query, parameters);
        }
        CompletionService<List<Contact>> completionService = getCompletionService();
        for (ContactsAccount account : accounts) {
            completionService.submit(() -> autocompleteContacts(account, query, parameters));
        }
        return collectContacts(completionService, accounts.size());
    }

    @Override
    public List<Contact> searchContactsWithAnniversary(Date from, Date until) throws OXException {
        List<ContactsAccount> accounts = getAccounts(ContactsAccessCapability.SEARCH);
        if (accounts.isEmpty()) {
            return ImmutableList.of();
        }
        if (accounts.size() == 1) {
            return searchContactsWithAnniversary(accounts.get(0), from, until);
        }
        CompletionService<List<Contact>> completionService = getCompletionService();
        for (ContactsAccount account : accounts) {
            completionService.submit(() -> searchContactsWithAnniversary(account, from, until));
        }
        return collectContacts(completionService, accounts.size());
    }

    @Override
    public List<Contact> searchContactsWithAnniversary(List<String> folderIds, Date from, Date until) throws OXException {
        Map<ContactsAccount, List<String>> foldersPerAccount = getRelativeFolderIdsPerAccount(folderIds);
        if (foldersPerAccount.size() == 1) {
            Entry<ContactsAccount, List<String>> entry = foldersPerAccount.entrySet().iterator().next();
            return searchContactsWithAnniversary(entry.getKey(), from, until);
        }
        CompletionService<List<Contact>> completionService = getCompletionService();
        for (Entry<ContactsAccount, List<String>> entry : foldersPerAccount.entrySet()) {
            completionService.submit(() -> searchContactsWithAnniversary(entry.getKey(), from, until));
        }
        return collectContacts(completionService, foldersPerAccount.size());
    }

    @Override
    public List<Contact> searchContactsWithBirthday(Date from, Date until) throws OXException {
        List<ContactsAccount> accounts = getAccounts(ContactsAccessCapability.SEARCH);
        if (accounts.isEmpty()) {
            return ImmutableList.of();
        }
        if (accounts.size() == 1) {
            return searchContactsWithBirthday(accounts.get(0), from, until);
        }
        CompletionService<List<Contact>> completionService = getCompletionService();
        for (ContactsAccount account : accounts) {
            completionService.submit(() -> searchContactsWithBirthday(account, from, until));
        }
        return collectContacts(completionService, accounts.size());
    }

    @Override
    public List<Contact> searchContactsWithBirthday(List<String> folderIds, Date from, Date until) throws OXException {
        Map<ContactsAccount, List<String>> foldersPerAccount = getRelativeFolderIdsPerAccount(folderIds);
        if (foldersPerAccount.size() == 1) {
            Entry<ContactsAccount, List<String>> entry = foldersPerAccount.entrySet().iterator().next();
            return searchContactsWithBirthday(entry.getKey(), from, until);
        }
        CompletionService<List<Contact>> completionService = getCompletionService();
        for (Entry<ContactsAccount, List<String>> entry : foldersPerAccount.entrySet()) {
            completionService.submit(() -> searchContactsWithBirthday(entry.getKey(), from, until));
        }
        return collectContacts(completionService, foldersPerAccount.size());
    }

    ////////////////////////////////// HELPERS ///////////////////////////////

    /**
     * Searches for contacts whose anniversaries fall into a specified period.
     * 
     * @param account The account
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose anniversaries start on or after this date should be returned.
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose anniversaries end before this date should be returned.
     * @return The found contacts
     * @throws OXException
     */
    private List<Contact> searchContactsWithAnniversary(ContactsAccount account, Date from, Date until) throws OXException {
        return performSearch(account, access -> access.searchContactsWithAnniversary(from, until));
    }

    /**
     * Searches for contacts whose birthdays fall into a specified period.
     * 
     * @param account The account
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose birthdays start on or after this date should be returned.
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose birthdays end before this date should be returned.
     * @return The found contacts
     * @throws OXException
     */
    private List<Contact> searchContactsWithBirthday(ContactsAccount account, Date from, Date until) throws OXException {
        return performSearch(account, access -> access.searchContactsWithBirthday(from, until));
    }

    /**
     * Search for contacts using the specified {@link SearchTerm}
     *
     * @param <O> the term type
     * @param account The account
     * @param searchTerm the search term
     * @return The results
     */
    private <O> List<Contact> searchContacts(ContactsAccount account, SearchTerm<O> searchTerm) throws OXException {
        return performSearch(account, access -> access.searchContacts(IDMangler.withRelativeID(searchTerm)));
    }

    /**
     * Search for contacts using the specified {@link ContactSearchObject}
     *
     * @param account The account
     * @param contactSearch the search data
     * @return The results
     */
    private List<Contact> searchContacts(ContactsAccount account, ContactsSearchObject contactSearch) throws OXException {
        return performSearch(account, access -> access.searchContacts(contactSearch));
    }

    /**
     * Perform an auto-complete search for contacts using the specified query and {@link AutocompleteParameters}
     *
     * @param account The account
     * @param query The query
     * @param parameters The {@link AutocompleteParameters}
     * @return The results
     */
    private List<Contact> autocompleteContacts(ContactsAccount account, String query, AutocompleteParameters parameters) throws OXException {
        return performSearch(account, access -> access.autocompleteContacts(query, parameters));
    }

    /**
     * Performs the search
     *
     * @param account The contacts account
     * @param performer The search performer
     * @return a List with found contacts
     * @throws OXException
     */
    private List<Contact> performSearch(ContactsAccount account, SearchPerformer performer) throws OXException {
        requireCapability(account.getProviderId());
        ContactsAccess access = getAccess(account);
        if (BasicSearchAware.class.isInstance(access)) {
            return IDMangler.withUniqueIDs(performer.perform((BasicSearchAware) access), account.getAccountId());
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    /**
     * Checks that all folder identifiers within the supplied list of full contact identifiers match a specific expected folder id.
     *
     * @param contactIDs The list of full contact identifiers to check
     * @param expectedFolderId The expected folder id to check against
     * @return The passed contact identifiers, after all were checked
     * @throws OXException {@link ContactsProviderExceptionCodes#CONTACT_NOT_FOUND_IN_FOLDER}
     */
    private List<ContactID> parentFolderMatches(List<ContactID> contactIDs, String expectedFolderId) throws OXException {
        if (null == contactIDs) {
            return contactIDs;
        }
        for (ContactID contactID : contactIDs) {
            parentFolderMatches(contactID, expectedFolderId);
        }
        return contactIDs;
    }

    /**
     * Checks that the folder identifier within the supplied full contact identifier matches a specific expected folder id.
     *
     * @param contactID The full contact identifier to check
     * @param expectedFolderId The expected folder id to check against
     * @return The passed contact identifier, after it was checked
     * @throws OXException {@link ContactsProviderExceptionCodes#CONTACT_NOT_FOUND_IN_FOLDER}
     */
    private ContactID parentFolderMatches(ContactID contactID, String expectedFolderId) throws OXException {
        if (null != contactID && false == Objects.equals(expectedFolderId, contactID.getFolderID())) {
            throw ContactsProviderExceptionCodes.CONTACT_NOT_FOUND_IN_FOLDER.create(contactID.getFolderID(), contactID.getObjectID());
        }
        return contactID;
    }

    /**
     * Checks that the folder identifier matches a specific expected folder id.
     *
     * @param folderId The folder identifier to check
     * @param expectedFolderId The expected folder id to check against
     * @return The passed folder identifier, after it was checked
     * @throws OXException {@link contactContactsProviderExceptionCodes#FOLDER_NOT_FOUND}
     */
    private String folderMatches(String folderId, String expectedFolderId) throws OXException {
        if (false == Objects.equals(expectedFolderId, folderId)) {
            throw ContactsProviderExceptionCodes.FOLDER_NOT_FOUND.create(folderId);
        }
        return folderId;
    }

    /**
     * Find the contact with the specified id in the specified list.
     *
     * @param contacts The list of contacts
     * @param contactID The contact identifier
     * @return The found contact or <code>null</code> if none found
     */
    private Optional<Contact> find(List<Contact> contacts, ContactID contactID) {
        return find(contacts, contactID.getFolderID(), contactID.getObjectID());
    }

    /**
     * Searches for the contact with the specified id and in the specified folder
     *
     * @param contacts The list of contacts to search
     * @param folderId The folder identifier
     * @param contactId The contact identifier
     * @return The found contact or <code>null</code> if none found
     */
    private Optional<Contact> find(List<Contact> contacts, String folderId, String contactId) {
        if (null == contacts) {
            return Optional.empty();
        }
        // @formatter:off
        return contacts.stream().filter(contact -> contact != null 
            && contactId.equals(Integer.toString(contact.getObjectID())) 
            && (folderId.equals(Integer.toString(contact.getParentFolderID())) 
                || folderId.equals(BasicContactsAccess.FOLDER_ID) 
                && 0 == contact.getParentFolderID())).findFirst();
        // @formatter:on
    }

    /**
     * Takes a specific number of contact list results from the completion service, and adds them to a single resulting, sorted list of
     * contacts.
     *
     * @param completionService The completion service to take the results from
     * @param count The number of results to collect
     * @return The resulting list of contacts
     */
    private List<Contact> collectContacts(CompletionService<List<Contact>> completionService, int count) throws OXException {
        List<Contact> contacts = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            try {
                contacts.addAll(completionService.take().get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ContactsProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (null != cause && OXException.class.isInstance(e.getCause())) {
                    throw (OXException) cause;
                }
                throw ContactsProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return contacts;
    }

    /**
     * Gets all contacts in a list of folders from a specific contacts account. Potential errors are placed in the results implicitly.
     *
     * @param account The contacts account
     * @param folderIds The relative identifiers of the folders to get the contacts from
     * @return The contacts results per folder, already adjusted to contain unique composite identifiers
     * @throws OXException
     */
    private List<Contact> getContactsInFolders(ContactsAccount account, List<String> folderIds) throws OXException {
        List<Contact> contactsPerFolderId = new LinkedList<>();
        requireCapability(account.getProviderId());
        if (isTypedProvider(account.getProviderId(), ContactsFolderProvider.class)) {
            contactsPerFolderId.addAll(getFolderAccess(account).getContactsInFolders(folderIds));
        } else if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
            BasicContactsAccess access = getBasicAccess(account);
            for (String folderId : folderIds) {
                folderMatches(folderId, BasicContactsAccess.FOLDER_ID);
                contactsPerFolderId.addAll(access.getContacts());
            }
        } else {
            throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        }
        return IDMangler.withUniqueIDs(contactsPerFolderId, account.getAccountId());
    }

    /**
     * Gets the relative representation of a list of unique composite folder identifiers, mapped to their associated contacts account.
     * <p/>
     * {@link IDMangling#ROOT_FOLDER_IDS} are passed as-is implicitly, mapped to the default account.
     *
     * @param folderIds The unique composite folder identifiers, e.g. <code>con://11/38</code>
     * @return The relative folder identifiers, mapped to their associated contacts account
     * @throws OXException
     */
    protected Map<ContactsAccount, List<String>> getRelativeFolderIdsPerAccount(List<String> folderIds) throws OXException {
        Map<Integer, List<String>> folderIdsPerAccountId = IDMangler.getRelativeFolderIdsPerAccountId(folderIds);
        if (null == folderIdsPerAccountId || folderIdsPerAccountId.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ContactsAccount, List<String>> foldersIdsPerAccount = new HashMap<>(folderIdsPerAccountId.size());
        try {
            // Attempt to batch-load all referenced accounts
            List<ContactsAccount> accounts = getAccounts(folderIdsPerAccountId.keySet().stream().collect(Collectors.toList()));
            for (Entry<Integer, List<String>> entry : folderIdsPerAccountId.entrySet()) {
                ContactsAccount account = accounts.stream().filter(a -> i(entry.getKey()) == a.getAccountId()).findFirst().orElse(null);
                foldersIdsPerAccount.put(account, entry.getValue());
            }
        } catch (OXException e) {
            // Load each account separately as fallback
            LOG.debug("Error batch-loading referenced accounts, loading individually as fallback.", e);
            for (Entry<Integer, List<String>> entry : folderIdsPerAccountId.entrySet()) {
                foldersIdsPerAccount.put(getAccount(i(entry.getKey())), entry.getValue());
            }
        }
        return foldersIdsPerAccount;
    }

    /**
     * Gets all visible folders of a certain type in a specific contacts account.
     * <p/>
     * In case of certain errors (provider not available or disabled by capability), a placeholder folder for the non-functional account
     * is returned automatically.
     *
     * @param account The contacts account to get the folder from
     * @param folderId The <i>relative</i> identifier of the folder to get
     * @return The folder (with <i>relative</i> identifiers)
     */
    private ContactsFolder getFolder(ContactsAccount account, String folderId) throws OXException {
        // Check if provider is enabled by capability, falling back to a placeholder folder if not
        if (false == hasCapability(account.getProviderId())) {
            OXException error = ContactsProviderExceptionCodes.MISSING_CAPABILITY.create(ContactsProviders.getCapabilityName(account.getProviderId()));
            if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
                return getBasicContactsFolder(getBasicAccess(account), isAutoProvisioned(account), error);
            }
            return getBasicContactsFolder(account, error);
        }
        // Query or get the folder from account
        if (isTypedProvider(account.getProviderId(), ContactsFolderProvider.class)) {
            return getFolderAccess(account).getFolder(folderId);
        }
        if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
            folderMatches(folderId, BasicContactsAccess.FOLDER_ID);
            return getBasicContactsFolder(getBasicAccess(account), isAutoProvisioned(account));
        }
        // Unsupported, otherwise (should not get here, though)
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    /**
     * Gets all visible folders in a specific contacts account.
     * <p/>
     * In case of certain errors (provider not available or disabled by capability), a placeholder folder for the non-functional account
     * is returned automatically.
     *
     * @param account The contacts account to get the visible folders from
     * @param type The groupware folder type
     * @return The visible folders (with <i>relative</i> identifiers), or an empty list if there are none
     */
    private List<? extends ContactsFolder> getVisibleFolders(ContactsAccount account, GroupwareFolderType type) throws OXException {
        // Non-private folders are handled by groupware contacts access exclusively
        if (false == GroupwareFolderType.PRIVATE.equals(type) && ContactsAccount.DEFAULT_ACCOUNT.getAccountId() != account.getAccountId()) {
            return Collections.emptyList();
        }
        // Check if provider is enabled by capability, if not, skip auto-provisioned folders, and fall back to a placeholder folder otherwise
        if (false == hasCapability(account.getProviderId())) {
            OXException error = ContactsProviderExceptionCodes.MISSING_CAPABILITY.create(ContactsProviders.getCapabilityName(account.getProviderId()));
            if (isAutoProvisioned(account)) {
                warnings.add(error);
                return Collections.emptyList();
            }
            if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
                return Collections.singletonList(getBasicContactsFolder(getBasicAccess(account), false, error));
            }
            return Collections.singletonList(getBasicContactsFolder(account, error));
        }
        // Query or build visible folders for contacts account
        if (isTypedProvider(account.getProviderId(), GroupwareContactsProvider.class)) {
            return getGroupwareAccess(account).getVisibleFolders(type);
        }
        if (false == GroupwareFolderType.PRIVATE.equals(type)) {
            return Collections.emptyList();
        }
        if (isTypedProvider(account.getProviderId(), ContactsFolderProvider.class)) {
            return getFolderAccess(account).getVisibleFolders();
        }
        if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
            return Collections.singletonList(getBasicContactsFolder(getBasicAccess(account), isAutoProvisioned(account)));
        }
        // Unsupported otherwise (should not get here, though)
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    /**
     * Gets specific contacts accounts.
     *
     * @param accountIds The identifiers of the account to get
     * @return The contacts accounts
     */
    private List<ContactsAccount> getAccounts(List<Integer> accountIds) throws OXException {
        return requireService(ContactsAccountService.class, services).getAccounts(session, accountIds, this);
    }

    /**
     * Gets the basic contacts folder based on the specified access' settings
     *
     * @param access The {@link BasicContactsAccess}
     * @param autoProvisioned flag to indicate if the account is auto provisioned
     * @return the folder
     */
    private ContactsFolder getBasicContactsFolder(BasicContactsAccess contactsAccess, boolean autoProvisioned) {
        return getBasicContactsFolder(contactsAccess, autoProvisioned, null);
    }

    /**
     * Gets a basic contacts folder based on the specified account's settings
     * and sets the specified account error
     *
     * @param account The {@link ContactsAccount}
     * @param accountError An optional account error
     * @return the folder
     */
    private ContactsFolder getBasicContactsFolder(ContactsAccount account, OXException accountError) {
        DefaultContactsFolder folder = new DefaultContactsFolder();
        folder.setId(BasicContactsAccess.FOLDER_ID);
        folder.setLastModified(account.getLastModified());
        folder.setPermissions(Collections.singletonList(new DefaultContactsPermission(session.getUserId(), ContactsPermission.READ_FOLDER, ContactsPermission.READ_ALL_OBJECTS, ContactsPermission.NO_PERMISSIONS, ContactsPermission.NO_PERMISSIONS, true, false, 0)));
        folder.setAccountError(accountError);
        folder.setName(getAccountName(account));
        folder.setUsedForSync(UsedForSync.DEACTIVATED);
        ExtendedProperties extendedProperties = new ExtendedProperties();
        folder.setExtendedProperties(extendedProperties);
        return folder;
    }

    /**
     * Gets the basic contacts folder based on the specified access' settings
     *
     * @param access The {@link BasicContactsAccess}
     * @param autoProvisioned flag to indicate if the account is auto provisioned
     * @param accountError An optional account error
     * @return the folder
     */
    private ContactsFolder getBasicContactsFolder(BasicContactsAccess access, boolean autoProvisioned, OXException accountError) {
        DefaultContactsFolder folder = new DefaultContactsFolder();
        folder.setId(BasicContactsAccess.FOLDER_ID);
        ContactsSettings settings = access.getSettings();
        folder.setAccountError(settings.getError());
        folder.setExtendedProperties(settings.getExtendedProperties());
        folder.setName(settings.getName());
        folder.setLastModified(settings.getLastModified());
        folder.setSubscribed(B(settings.isSubscribed()));
        folder.setUsedForSync(settings.getUsedForSync().orElse(UsedForSync.DEFAULT));
        // @formatter:off
        folder.setPermissions(Collections.singletonList(new DefaultContactsPermission(session.getUserId(), ContactsPermission.READ_FOLDER, 
            ContactsPermission.READ_ALL_OBJECTS, ContactsPermission.NO_PERMISSIONS, ContactsPermission.NO_PERMISSIONS, false == autoProvisioned, false, 0)));
        // @formatter:on
        if (null != accountError) {
            folder.setAccountError(accountError); // prefer passed account error if assigned
        }
        return folder;
    }

    /**
     * Retrieves the {@link ContactsSettings} for the specified contacts folder
     *
     * @param contactsFolder The contacts folder
     * @param userConfig The user configuration
     * @return The {@link ContactsSettings}
     */
    private ContactsSettings getBasicContactsSettings(ContactsFolder contactsFolder, JSONObject userConfig) {
        ContactsSettings settings = new ContactsSettings();
        if (null != contactsFolder.getExtendedProperties()) {
            settings.setExtendedProperties(contactsFolder.getExtendedProperties());
        }
        if (null != contactsFolder.getAccountError()) {
            settings.setError(contactsFolder.getAccountError());
        }
        if (null != contactsFolder.getName()) {
            settings.setName(contactsFolder.getName());
        }
        if (null != contactsFolder.getLastModified()) {
            settings.setLastModified(contactsFolder.getLastModified());
        }
        if (null != userConfig) {
            settings.setConfig(userConfig);
        }
        if (null != contactsFolder.isSubscribed()) {
            settings.setSubscribed(b(contactsFolder.isSubscribed()));
        }
        if (null != contactsFolder.getUsedForSync()) {
            settings.setUsedForSync(contactsFolder.getUsedForSync());
        }
        return settings;
    }

    /**
     * {@link SearchPerformer}
     */
    @FunctionalInterface
    interface SearchPerformer {

        /**
         * Performs the search
         *
         * @param access The search aware access
         * @return The result
         * @throws OXException if an error is occurred
         */
        List<Contact> perform(BasicSearchAware access) throws OXException;
    }
}
