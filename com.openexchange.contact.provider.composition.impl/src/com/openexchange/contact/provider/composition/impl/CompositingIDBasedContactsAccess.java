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

import static com.openexchange.contact.provider.composition.impl.idmangling.IDMangler.getAccountId;
import static com.openexchange.contact.provider.composition.impl.idmangling.IDMangler.getRelativeFolderId;
import static com.openexchange.contact.provider.composition.impl.idmangling.IDMangler.getRelativeId;
import static com.openexchange.contact.provider.composition.impl.idmangling.IDMangler.withUniqueID;
import static com.openexchange.contact.provider.composition.impl.idmangling.IDMangler.withUniqueIDs;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.SortOrder;
import com.openexchange.contact.common.AccountAwareContactsFolder;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.ContactsPermission;
import com.openexchange.contact.common.DefaultContactsFolder;
import com.openexchange.contact.common.DefaultContactsPermission;
import com.openexchange.contact.common.GroupwareContactsFolder;
import com.openexchange.contact.common.GroupwareFolderType;
import com.openexchange.contact.common.UsedForSync;
import com.openexchange.contact.provider.ContactsAccess;
import com.openexchange.contact.provider.ContactsAccessCapability;
import com.openexchange.contact.provider.ContactsAccountService;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.provider.ContactsProviderRegistry;
import com.openexchange.contact.provider.basic.BasicContactsAccess;
import com.openexchange.contact.provider.basic.BasicContactsProvider;
import com.openexchange.contact.provider.basic.BasicSearchAware;
import com.openexchange.contact.provider.basic.ContactsSettings;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.impl.idmangling.IDMangler;
import com.openexchange.contact.provider.folder.AnnualDateFolderSearchAware;
import com.openexchange.contact.provider.folder.FolderContactsAccess;
import com.openexchange.contact.provider.folder.FolderContactsProvider;
import com.openexchange.contact.provider.folder.FolderSearchAware;
import com.openexchange.contact.provider.groupware.GroupwareContactsAccess;
import com.openexchange.contact.provider.groupware.InternalContactsAccess;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactsSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Strings;
import com.openexchange.l10n.SuperCollator;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

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
    public Contact getContact(ContactID contactId) throws OXException {
        ContactsAccount account = getAccount(getAccountId(contactId.getFolderID()));
        try {
            ContactID relativeContactId = getRelativeId(contactId);
            ContactsAccess access = getAccess(account.getAccountId());
            Contact contact;
            if (FolderContactsAccess.class.isInstance(access)) {
                contact = ((FolderContactsAccess) access).getContact(relativeContactId.getFolderID(), relativeContactId.getObjectID());
            } else if (BasicContactsAccess.class.isInstance(access)) {
                parentFolderMatches(relativeContactId, BasicContactsAccess.FOLDER_ID);
                contact = ((BasicContactsAccess) access).getContact(relativeContactId.getObjectID());
            } else {
                throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
            }
            return withUniqueID(contact, account.getAccountId());
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
        }
    }

    @Override
    public List<Contact> getContacts(List<ContactID> contactIDs) throws OXException {
        Map<Integer, List<ContactID>> idsPerAccountId = IDMangler.getRelativeIdsPerAccountId(contactIDs);
        Map<Integer, List<Contact>> contactsPerAccountId = new HashMap<>(idsPerAccountId.size());
        for (Entry<Integer, List<ContactID>> entry : idsPerAccountId.entrySet()) {
            ContactsAccount account = getAccount(i(entry.getKey()), true);
            if (isTypedProvider(account.getProviderId(), FolderContactsProvider.class)) {
                contactsPerAccountId.put(I(account.getAccountId()), getFolderAccess(account).getContacts(entry.getValue()));
            } else if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
                List<String> objectIds = new ArrayList<String>(entry.getValue().size());
                for (ContactID contactID : entry.getValue()) {
                    parentFolderMatches(contactID, BasicContactsAccess.FOLDER_ID);
                    objectIds.add(contactID.getObjectID());
                }
                contactsPerAccountId.put(I(account.getAccountId()), getBasicAccess(account).getContacts(objectIds));
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
        if (isTypedProvider(account.getProviderId(), FolderContactsProvider.class)) {
            return withUniqueIDs(getFolderAccess(account).getContacts(IDMangler.getRelativeFolderId(folderId)), accountId);
        }
        if (isTypedProvider(account.getProviderId(), BasicContactsProvider.class)) {
            return withUniqueIDs(getBasicAccess(account).getContacts(), accountId);
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    @Override
    public List<Contact> getDeletedContacts(String folderId, Date from) throws OXException {
        ContactsAccount account = getAccount(IDMangler.getAccountId(folderId), true);
        if (isTypedProvider(account.getProviderId(), FolderContactsProvider.class)) {
            return IDMangler.withUniqueIDs(getFolderAccess(account).getDeletedContacts(IDMangler.getRelativeFolderId(folderId), from), account.getAccountId());
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    @Override
    public List<Contact> getModifiedContacts(String folderId, Date from) throws OXException {
        ContactsAccount account = getAccount(IDMangler.getAccountId(folderId), true);
        if (isTypedProvider(account.getProviderId(), FolderContactsProvider.class)) {
            return IDMangler.withUniqueIDs(getFolderAccess(account).getModifiedContacts(IDMangler.getRelativeFolderId(folderId), from), account.getAccountId());
        }
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    ////////////////////////////////// FOLDERS ////////////////////////////////

    @Override
    public List<AccountAwareContactsFolder> getVisibleFolders(GroupwareFolderType type) throws OXException {
        List<AccountAwareContactsFolder> folders = new ArrayList<AccountAwareContactsFolder>();
        for (ContactsAccount account : getAccounts()) {
            try {
                folders.addAll(withUniqueID(getVisibleFolders(account, type), account));
            } catch (OXException e) {
                throw withUniqueIDs(e, account.getAccountId());
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
        ContactsAccount account = getAccount(getAccountId(folderId));
        try {
            return withUniqueID(getFolder(account, getRelativeFolderId(folderId)), account);
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
        }
    }

    @Override
    public AccountAwareContactsFolder getDefaultFolder() throws OXException {
        ContactsAccount account = getAccount(ContactsAccount.DEFAULT_ACCOUNT.getAccountId());
        try {
            return withUniqueID(getAccess(account, InternalContactsAccess.class).getDefaultFolder(), account);
        } catch (OXException e) {
            throw withUniqueIDs(e, account.getAccountId());
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
        return searchContacts((List<String>) null, term);
    }

    @Override
    public List<Contact> searchContacts(ContactsSearchObject contactSearch) throws OXException {
        if (null == contactSearch.getFolders() || contactSearch.getFolders().isEmpty()) {
            /*
             * search in all folders of all accounts, excluding the defined ones
             */
            List<ContactsAccount> accounts = getAccounts(ContactsAccessCapability.SEARCH);
            if (accounts.isEmpty()) {
                return Collections.emptyList();
            }
            Map<ContactsAccount, List<String>> excludedFolderIdsPerAccount = null == contactSearch.getExcludeFolders() ?
                Collections.emptyMap() : getRelativeFolderIdsPerAccount(new ArrayList<String>(contactSearch.getExcludeFolders()));
            if (1 == accounts.size()) {
                return searchContacts(accounts.get(0), maskFolderIds(contactSearch, null, excludedFolderIdsPerAccount.get(accounts.get(0))));
            }
            CompletionService<List<Contact>> completionService = getCompletionService();
            for (ContactsAccount account : accounts) {
                completionService.submit(() -> searchContacts(account, maskFolderIds(contactSearch, null, excludedFolderIdsPerAccount.get(account))));
            }
            return sortResults(collectContacts(completionService, accounts.size()));
        }
        /*
         * search in accounts of folders specified in search object, otherwise
         */
        Map<ContactsAccount, List<String>> folderIdsPerAccount = getRelativeFolderIdsPerAccount(new ArrayList<String>(contactSearch.getFolders()));
        if (folderIdsPerAccount.isEmpty()) {
            return Collections.emptyList();
        } else if (1 == folderIdsPerAccount.size()) {
            Entry<ContactsAccount, List<String>> entry = folderIdsPerAccount.entrySet().iterator().next();
            return searchContacts(entry.getKey(), maskFolderIds(contactSearch, entry.getValue(), null));
        } else {
            CompletionService<List<Contact>> completionService = getCompletionService();
            for (Entry<ContactsAccount, List<String>> entry : folderIdsPerAccount.entrySet()) {
                completionService.submit(() -> searchContacts(entry.getKey(), maskFolderIds(contactSearch, entry.getValue(), null)));
            }
            return sortResults(collectContacts(completionService, folderIdsPerAccount.size()));
        }
    }

    public <O> List<Contact> searchContacts(List<String> folderIds, SearchTerm<O> term) throws OXException {
        if (null == folderIds) {
            /*
             * search in all folders of all accounts
             */
            List<ContactsAccount> accounts = getAccounts(ContactsAccessCapability.SEARCH);
            if (accounts.isEmpty()) {
                return Collections.emptyList();
            }
            if (1 == accounts.size()) {
                return searchContacts(accounts.get(0), folderIds, term);
            }
            CompletionService<List<Contact>> completionService = getCompletionService();
            for (ContactsAccount account : accounts) {
                completionService.submit(() -> searchContacts(account, null, term));
            }
            return sortResults(collectContacts(completionService, accounts.size()));
        }
        /*
         * search in accounts of specified folders, otherwise
         */
        Map<ContactsAccount, List<String>> folderIdsPerAccount = getRelativeFolderIdsPerAccount(folderIds);
        if (folderIdsPerAccount.isEmpty()) {
            return Collections.emptyList();
        } else if (1 == folderIdsPerAccount.size()) {
            Entry<ContactsAccount, List<String>> entry = folderIdsPerAccount.entrySet().iterator().next();
            return searchContacts(entry.getKey(), entry.getValue(), term);
        } else {
            CompletionService<List<Contact>> completionService = getCompletionService();
            for (Entry<ContactsAccount, List<String>> entry : folderIdsPerAccount.entrySet()) {
                completionService.submit(() -> searchContacts(entry.getKey(), entry.getValue(), term));
            }
            return sortResults(collectContacts(completionService, folderIdsPerAccount.size()));
        }
    }

    @Override
    public List<Contact> autocompleteContacts(List<String> folderIds, String query) throws OXException {
        if (null == folderIds) {
            /*
             * search in all folders, or in certain folders of default account
             */
            if (false == services.getServiceSafe(LeanConfigurationService.class).getBooleanProperty(ContactsProviderProperty.ALL_FOLDERS_FOR_AUTOCOMPLETE)) {
                return autocompleteContacts(getAccount(ContactsAccount.DEFAULT_ACCOUNT.getAccountId()), null, query);
            }
            List<ContactsAccount> accounts = getAccounts(ContactsAccessCapability.SEARCH);
            if (accounts.isEmpty()) {
                return Collections.emptyList();
            }
            if (1 == accounts.size()) {
                return autocompleteContacts(accounts.get(0), null, query);
            }
            CompletionService<List<Contact>> completionService = getCompletionService();
            for (ContactsAccount account : accounts) {
                completionService.submit(() -> autocompleteContacts(account, null, query));
            }
            return sortResults(collectContacts(completionService, accounts.size()));
        }
        /*
         * search in accounts of specified folders, otherwise
         */
        Map<ContactsAccount, List<String>> folderIdsPerAccount = getRelativeFolderIdsPerAccount(folderIds);
        if (folderIdsPerAccount.isEmpty()) {
            return Collections.emptyList();
        } else if (1 == folderIdsPerAccount.size()) {
            Entry<ContactsAccount, List<String>> entry = folderIdsPerAccount.entrySet().iterator().next();
            return autocompleteContacts(entry.getKey(), entry.getValue(), query);
        } else {
            CompletionService<List<Contact>> completionService = getCompletionService();
            for (Entry<ContactsAccount, List<String>> entry : folderIdsPerAccount.entrySet()) {
                completionService.submit(() -> autocompleteContacts(entry.getKey(), entry.getValue(), query));
            }
            return sortResults(collectContacts(completionService, folderIdsPerAccount.size()));
        }
    }

    @Override
    public List<Contact> searchContactsWithBirthday(List<String> folderIds, Date from, Date until) throws OXException {
        return searchContactsByAnnualDate(folderIds, ContactField.BIRTHDAY, from, until);
    }

    @Override
    public List<Contact> searchContactsWithAnniversary(List<String> folderIds, Date from, Date until) throws OXException {
        return searchContactsByAnnualDate(folderIds, ContactField.ANNIVERSARY, from, until);
    }

    /**
     * Searches contacts by annual date.
     *
     * @param folderIds The identifiers of the folders to perform the search in, or <code>null</code> to search in all folders
     * @param annualDateField Either {@link ContactField#BIRTHDAY} or {@link ContactField#ANNIVERSARY}
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose birthdays start on or after this date should be returned
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose birthdays end before this date should be returned
     * @return The found contacts
     */
    public List<Contact> searchContactsByAnnualDate(List<String> folderIds, ContactField annualDateField, Date from, Date until) throws OXException {
        if (null == folderIds) {
            /*
             * search in all folders
             */
            List<ContactsAccount> accounts = getAccounts(ContactsAccessCapability.SEARCH);
            if (accounts.isEmpty()) {
                return Collections.emptyList();
            }
            if (1 == accounts.size()) {
                return searchByAnnualDate(accounts.get(0), null, annualDateField, from, until);
            }
            CompletionService<List<Contact>> completionService = getCompletionService();
            for (ContactsAccount account : accounts) {
                completionService.submit(() -> searchByAnnualDate(account, null, annualDateField, from, until));
            }
            return sortResults(collectContacts(completionService, accounts.size()));
        }
        /*
         * search in accounts of specified folders, otherwise
         */
        Map<ContactsAccount, List<String>> folderIdsPerAccount = getRelativeFolderIdsPerAccount(folderIds);
        if (folderIdsPerAccount.isEmpty()) {
            return Collections.emptyList();
        } else if (1 == folderIdsPerAccount.size()) {
            Entry<ContactsAccount, List<String>> entry = folderIdsPerAccount.entrySet().iterator().next();
            return searchByAnnualDate(entry.getKey(), entry.getValue(), annualDateField, from, until);
        } else {
            CompletionService<List<Contact>> completionService = getCompletionService();
            for (Entry<ContactsAccount, List<String>> entry : folderIdsPerAccount.entrySet()) {
                completionService.submit(() -> searchByAnnualDate(entry.getKey(), entry.getValue(), annualDateField, from, until));
            }
            return sortResults(collectContacts(completionService, folderIdsPerAccount.size()));
        }
    }

    ////////////////////////////////// HELPERS ///////////////////////////////

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
     * @throws OXException {@link ContactsProviderExceptionCodes#FOLDER_NOT_FOUND}
     */
    private String folderMatches(String folderId, String expectedFolderId) throws OXException {
        if (false == Objects.equals(expectedFolderId, folderId)) {
            throw ContactsProviderExceptionCodes.FOLDER_NOT_FOUND.create(folderId);
        }
        return folderId;
    }

    /**
     * Checks that all folder identifiers do match the virtual basic folder identifier, if specified.
     *
     * @param folderIds The folder identifiers to check, or <code>null</code> for a no-op
     * @return The passed collection reference
     * @throws OXException {@link ContactsProviderExceptionCodes#FOLDER_NOT_FOUND}
     */
    private <T extends Collection<String>> T checkBasicContactFolderIdsOnly(T folderIds) throws OXException {
        if (null != folderIds) {
            for (String folderId : folderIds) {
                if (false == BasicContactsAccess.FOLDER_ID.equals(folderId)) {
                    throw ContactsProviderExceptionCodes.FOLDER_NOT_FOUND.create(folderId);
                }
            }
        }
        return folderIds;
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
        for (Contact contact : contacts) {
            if (contact.containsId() && Objects.equals(contactId, contact.getId()) ||
                contact.containsObjectID() && Objects.equals(contactId, String.valueOf(contact.getObjectID()))) {
                if (BasicContactsAccess.FOLDER_ID.equals(folderId) ||
                    contact.containsFolderId() && Objects.equals(folderId, contact.getFolderId()) ||
                    contact.containsParentFolderID() && Objects.equals(folderId, String.valueOf(contact.getParentFolderID()))) {
                    return Optional.of(contact);
                }
            }
        }
        return Optional.empty();
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
     * Generates a new contact search object yielding a different set of folder ids / excluded folder ids.
     *
     * @param delegate The search object to mask the folder identifiers in
     * @param folderIds The folder identifiers to indicate
     * @param excludedFolderIds The excluded folder identifiers to indicate
     * @return A new contact search object for the new folder identifiers
     */
    private static ContactsSearchObject maskFolderIds(ContactsSearchObject delegate, List<String> folderIds, List<String> excludedFolderIds) {
        ContactsSearchObject contactsSearch = new ContactsSearchObject(delegate);
        contactsSearch.setFolders(null == folderIds ? Collections.emptySet() : new HashSet<String>(folderIds));
        contactsSearch.setExcludeFolders(null == excludedFolderIds ? Collections.emptySet() : new HashSet<String>(excludedFolderIds));
        return contactsSearch;
    }

    /**
     * Gets a folder in a specific contacts account.
     *
     * @param account The contacts account to get the folder from
     * @param folderId The <i>relative</i> identifier of the folder to get
     * @return The folder (with <i>relative</i> identifiers)
     */
    private ContactsFolder getFolder(ContactsAccount account, String folderId) throws OXException {
        /*
         * query or get the folder from account
         */
        ContactsAccess access = getAccess(account.getAccountId());
        if (FolderContactsAccess.class.isInstance(access)) {
            return ((FolderContactsAccess) access).getFolder(folderId);
        }
        if (BasicContactsAccess.class.isInstance(access)) {
            folderMatches(folderId, BasicContactsAccess.FOLDER_ID);
            return getBasicContactsFolder((BasicContactsAccess) access, isAutoProvisioned(account));
        }
        /*
         * unsupported, otherwise (should not get here, though)
         */
        throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
    }

    /**
     * Gets all visible folders in a specific contacts account.
     *
     * @param account The contacts account to get the visible folders from
     * @param type The groupware folder type
     * @return The visible folders (with <i>relative</i> identifiers), or an empty list if there are none
     */
    private List<? extends ContactsFolder> getVisibleFolders(ContactsAccount account, GroupwareFolderType type) throws OXException {
        /*
         * query or build visible folders for calendar account
         */
        ContactsAccess access = getAccess(account.getAccountId());
        if (GroupwareContactsAccess.class.isInstance(access)) {
            return ((GroupwareContactsAccess) access).getVisibleFolders(type);
        }
        if (false == GroupwareFolderType.PRIVATE.equals(type)) {
            return Collections.emptyList();
        }
        if (BasicContactsAccess.class.isInstance(access)) {
            return Collections.singletonList(getBasicContactsFolder((BasicContactsAccess) access, isAutoProvisioned(account)));
        }
        if (FolderContactsAccess.class.isInstance(access)) {
            return ((FolderContactsAccess) access).getVisibleFolders();
        }
        /*
         * unsupported, otherwise (should not get here, though)
         */
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
     * Performs a term-based search for contacts in a specific account.
     *
     * @param account The account to perform the search in
     * @param folderIds The (relative) identifiers of the folders to perform the search in, or <code>null</code> to search in all folders of the account
     * @param term The search term as supplied by the client
     * @return The found contacts, already adjusted to contain unique composite identifiers
     */
    private List<Contact> searchContacts(ContactsAccount account, List<String> folderIds, SearchTerm<?> term) {
        try {
            ContactsAccess access = getAccess(account);
            if (FolderSearchAware.class.isInstance(access)) {
                // TODO with folderids
                return withUniqueIDs(((FolderSearchAware) access).searchContacts(term), account.getAccountId());
            }
            if (BasicSearchAware.class.isInstance(access)) {
                checkBasicContactFolderIdsOnly(folderIds);
                return withUniqueIDs(((BasicSearchAware) access).searchContacts(term), account.getAccountId());
            }
            throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        } catch (OXException e) {
            warnings.add(e);
            return Collections.emptyList();
        }
    }

    private List<Contact> searchContacts(ContactsAccount account, ContactsSearchObject contactsSearch) {
        try {
            ContactsAccess access = getAccess(account);
            if (FolderSearchAware.class.isInstance(access)) {
                return withUniqueIDs(((FolderSearchAware) access).searchContacts(contactsSearch), account.getAccountId());
            }
            if (BasicSearchAware.class.isInstance(access)) {
                checkBasicContactFolderIdsOnly(contactsSearch.getExcludeFolders());
                checkBasicContactFolderIdsOnly(contactsSearch.getFolders());
                return withUniqueIDs(((BasicSearchAware) access).searchContacts(contactsSearch), account.getAccountId());
            }
            throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        } catch (OXException e) {
            warnings.add(e);
            return Collections.emptyList();
        }
    }

    /**
     * Performs an "auto-complete" lookup for contacts in a specific account.
     *
     * @param account The account to perform the search in
     * @param folderIds The (relative) identifiers of the folders to perform the search in, or <code>null</code> to search in all folders of the account
     * @param query The search query as supplied by the client
     * @return The found contacts, already adjusted to contain unique composite identifiers
     */
    private List<Contact> autocompleteContacts(ContactsAccount account, List<String> folderIds, String query) {
        try {
            ContactsAccess access = getAccess(account);
            if (FolderSearchAware.class.isInstance(access)) {
                return withUniqueIDs(((FolderSearchAware) access).autocompleteContacts(folderIds, query), account.getAccountId());
            }
            if (BasicSearchAware.class.isInstance(access)) {
                checkBasicContactFolderIdsOnly(folderIds);
                return withUniqueIDs(((BasicSearchAware) access).autocompleteContacts(query), account.getAccountId());
            }
            throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        } catch (OXException e) {
            warnings.add(e);
            return Collections.emptyList();
        }
    }

    /**
     * Searches contacts by annual date in the specified account.
     *
     * @param account The account to perform the search in
     * @param folderIds The (relative) identifiers of the folders to perform the search in, or <code>null</code> to search in all folders of the account
     * @param annualDateField Either {@link ContactField#BIRTHDAY} or {@link ContactField#ANNIVERSARY}
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose birthdays start on or after this date should be returned
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose birthdays end before this date should be returned
     * @return The found contacts, already adjusted to contain unique composite identifiers
     */
    private List<Contact> searchByAnnualDate(ContactsAccount account, List<String> folderIds, ContactField annualDateField, Date from, Date until) {
        try {
            ContactsAccess access = getAccess(account);
            if (AnnualDateFolderSearchAware.class.isInstance(access)) {
                if (ContactField.BIRTHDAY.equals(annualDateField)) {
                    return withUniqueIDs(((AnnualDateFolderSearchAware) access).searchContactsWithBirthday(folderIds, from, until), account.getAccountId());
                } else if (ContactField.ANNIVERSARY.equals(annualDateField)) {
                    return withUniqueIDs(((AnnualDateFolderSearchAware) access).searchContactsWithAnniversary(folderIds, from, until), account.getAccountId());
                }
                throw new IllegalArgumentException(String.valueOf(annualDateField));
            }
            if (FolderSearchAware.class.isInstance(access)) {
                SearchTerm<?> hasDateTerm = new CompositeSearchTerm(CompositeOperation.NOT).addSearchTerm(
                    new SingleSearchTerm(SingleOperation.ISNULL).addOperand(new ContactFieldOperand(annualDateField)));
                //TODO: add method with folder ids
                return withUniqueIDs(filterByAnnualDate(((FolderSearchAware) access).searchContacts(hasDateTerm), annualDateField, from, until), account.getAccountId());
            }
            if (BasicSearchAware.class.isInstance(access)) {
                checkBasicContactFolderIdsOnly(folderIds);
                SearchTerm<?> hasDateTerm = new CompositeSearchTerm(CompositeOperation.NOT).addSearchTerm(
                    new SingleSearchTerm(SingleOperation.ISNULL).addOperand(new ContactFieldOperand(annualDateField)));
                return withUniqueIDs(filterByAnnualDate(((BasicSearchAware) access).searchContacts(hasDateTerm), annualDateField, from, until), account.getAccountId());
            }
            throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(account.getProviderId());
        } catch (OXException e) {
            warnings.add(e);
            return Collections.emptyList();
        }
    }

    /**
     * Filters out contacts whose month/day portion of the date field falls between the supplied period. This does only work for the
     * 'birthday'- and 'anniversary' fields.
     *
     * @param contacts The contacts to filter
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @param annualDateField One of <code>ContactField.ANNIVERSARY</code> or <code>ContactField.BIRTHDAY</code>
     * @return The filtered contacts
     */
    private static List<Contact> filterByAnnualDate(List<Contact> contacts, ContactField annualDateField, Date from, Date until) {
        if (null == contacts || contacts.isEmpty()) {
            return contacts;
        }
        if (from.after(until)) {
            throw new IllegalArgumentException("from must not be after until");
        }
        /*
         * get from/until years
         */
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(from);
        int fromYear = calendar.get(Calendar.YEAR);
        calendar.setTime(until);
        int untilYear = calendar.get(Calendar.YEAR);
        /*
         * filter resulting contacts accordingly
         */
        return contacts.stream().filter(contact -> {
            Date date = ContactField.ANNIVERSARY.equals(annualDateField) ? contact.getAnniversary() :
                ContactField.BIRTHDAY.equals(annualDateField) ? contact.getBirthday() : null;
            if (null != date) {
                calendar.setTime(date);
                for (int y = fromYear; y <= untilYear; y++) {
                    calendar.set(Calendar.YEAR, y);
                    if (calendar.getTime().before(until) && false == calendar.getTime().before(from)) {
                        return true;
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    /**
     * Sorts and truncates a resulting list of contacts according to the criteria found in this contacts access session.
     *
     * @param contacts The contacts to sort
     * @return The sorted contacts, or the passed collection as-is if no sorting necessary
     */
    private List<Contact> sortResults(List<Contact> contacts) throws OXException {
        if (null == contacts || contacts.isEmpty()) {
            return contacts;
        }
        /*
         * sort & truncate results as needed
         */
        int leftHandLimit = i(get(PARAMETER_LEFT_HAND_LIMIT, Integer.class, I(0)));
        if (leftHandLimit > contacts.size()) {
            return Collections.emptyList();
        }
        if (1 < contacts.size()) {
            Comparator<Contact> comparator = optComparator();
            if (null != comparator) {
                Collections.sort(contacts, comparator);
            }
        }
        int rightHandLimit = i(get(PARAMETER_RIGHT_HAND_LIMIT, Integer.class, I(-1)));
        if (-1 != rightHandLimit && rightHandLimit < leftHandLimit) {
            throw ContactsProviderExceptionCodes.INVALID_RANGE_LIMITS.create();
        }
        if (0 < leftHandLimit || -1 < rightHandLimit) {
            return contacts.subList(leftHandLimit, Math.min(rightHandLimit, contacts.size()));
        }
        return contacts;
    }

    /**
     * Optionally gets a contact comparator for sorting result lists based on the current contact access parameters.
     *
     * @return A suitable contact comparator, or <code>null</code> if no sorting of results is requested
     */
    private Comparator<Contact> optComparator() throws OXException {
        /*
         * determine sort order
         */
        ContactField orderBy = parameters.get(PARAMETER_ORDER_BY, ContactField.class, null);
        if (null == orderBy) {
            return null;
        }
        Order order = parameters.get(PARAMETER_ORDER, Order.class, Order.ASCENDING);
        SortOrder[] sortOrder;
        if (ContactField.USE_COUNT.equals(orderBy)) {
            //TODO: workaround to mimic "use count, global first" semantics - perhaps should allow multiple sort orders through parameters?
            sortOrder = new SortOrder[] { new SortOrder(orderBy, order), new SortOrder(ContactField.FOLDER_ID, Order.ASCENDING) };
        } else {
            sortOrder = new SortOrder[] { new SortOrder(orderBy, order) };
        }
        /*
         * determine locale
         */
        Locale locale;
        String collation = parameters.get(PARAMETER_COLLATION, String.class);
        if (null != collation) {
            SuperCollator superCollator = SuperCollator.get(collation);
            if (null == superCollator) {
                LOG.warn("Ignoring unsupported collation {}", collation);
                locale = ServerSessionAdapter.valueOf(session).getUser().getLocale();
            } else {
                locale = superCollator.getJavaLocale();
            }
        } else {
            locale = ServerSessionAdapter.valueOf(session).getUser().getLocale();
        }
        /*
         * get appropriate comparator
         */
        return getComparator(sortOrder, locale);
    }

    /**
     * Gets a contact comparator suitable for the supplied sort order(s).
     *
     * @param sortOrder The requested sort order
     * @param locale The locale, or <code>null</code> if not specified
     * @return A suitable contact comparator
     */
    private static Comparator<Contact> getComparator(SortOrder[] sortOrder, Locale locale) {
        return new Comparator<Contact>() {

            @Override
            public int compare(Contact o1, Contact o2) {
                for (SortOrder order : sortOrder) {
                    int comparison = 0;
                    try {
                        comparison = ContactMapper.getInstance().get(order.getBy()).compare(o1, o2, locale);
                    } catch (OXException e) {
                        LOG.error("error comparing objects", e);
                    }
                    if (0 != comparison) {
                        return Order.DESCENDING.equals(order.getOrder()) ? -1 * comparison : comparison;
                    }
                }
                return 0;
            }
        };
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
