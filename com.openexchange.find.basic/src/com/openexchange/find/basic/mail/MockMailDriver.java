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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
package com.openexchange.find.basic.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.ModuleConfig;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.DefaultFolderType;
import com.openexchange.find.common.FolderDisplayItem;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.MandatoryFilter;
import com.openexchange.find.mail.DefaultMailFolderType;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.mail.MailFolderImpl.MailFolderType;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MockMailDriver}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class MockMailDriver implements ModuleSearchDriver {

    private static final MailFolderFilter NO_FILTER = null;

    private static final ContactField[] CONTACT_FIELDS = new ContactField[] {
        ContactField.OBJECT_ID,
        ContactField.FOLDER_ID,
        ContactField.PRIVATE_FLAG,
        ContactField.DISPLAY_NAME,
        ContactField.GIVEN_NAME,
        ContactField.SUR_NAME,
        ContactField.TITLE,
        ContactField.POSITION,
        ContactField.INTERNAL_USERID,
        ContactField.EMAIL1,
        ContactField.EMAIL2,
        ContactField.EMAIL3,
        ContactField.COMPANY,
        ContactField.DISTRIBUTIONLIST,
        ContactField.MARK_AS_DISTRIBUTIONLIST,
        ContactField.IMAGE1_URL,
        ContactField.CELLULAR_TELEPHONE1,
        ContactField.CELLULAR_TELEPHONE2
    };

    private static final Set<String> PERSONS_FILTER_FIELDS = new HashSet<String>(3);
    static {
        PERSONS_FILTER_FIELDS.add("from");
        PERSONS_FILTER_FIELDS.add("to");
        PERSONS_FILTER_FIELDS.add("cc");
    }

    private static final Set<String> FOLDERS_FILTER_FIELDS = Collections.singleton("folder");

    public MockMailDriver() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.MAIL;
    }

    @Override
    public boolean isValidFor(ServerSession session) throws OXException {
        return session.getUserConfiguration().hasWebMail() && session.getUserConfiguration().hasContact();
    }

    @Override
    public ModuleConfig getConfiguration(ServerSession session) throws OXException {
        Facet folderFacet = null;
        MandatoryFilter folderFilter = null;
        {
            List<UserizedFolder> mailFolders = loadMailFolders(session, NO_FILTER);
            if (mailFolders.isEmpty()) {
                throw FindExceptionCode.NO_READABLE_FOLDER.create(Module.MAIL, session.getUserId(), session.getContextId());
            }

            UserizedFolder defaultFolder = null;
            for (Iterator<UserizedFolder> it = mailFolders.iterator(); it.hasNext();) {
                UserizedFolder folder = it.next();
                if (folder.isDefault() && folder.getDefaultType() == MailFolderType.INBOX.getType()) {
                    defaultFolder = folder;
                    it.remove();
                } else if (folder.isDefault() && folder.getDefaultType() == MailFolderType.ROOT.getType()) {
                    // Don't show root folder in facet
                    it.remove();
                }
            }

            // Fallback
            if (defaultFolder == null) {
                defaultFolder = mailFolders.remove(0);
            }

            MailAccountStorageService mass = Services.getMailAccountStorageService();
            MailAccount mailAccount = mass.getMailAccount(new FullnameArgument(defaultFolder.getID()).getAccountId(), session.getUserId(), session.getContextId());
            FacetValue defaultValue = buildFolderFacetValue(defaultFolder, mailAccount);
            folderFacet = buildFolderFacet(mailFolders);
            folderFacet.getValues().add(defaultValue);
            folderFilter = new MandatoryFilter(folderFacet, defaultValue);
        }

        List<Facet> staticFacets = new ArrayList<Facet>(3);
        Facet subjectFacet = new Facet(MailFacetType.SUBJECT, Collections.singletonList(new FacetValue(
            new SimpleDisplayItem("subject"),
            FacetValue.UNKNOWN_COUNT,
            new Filter(Collections.singleton("subject"), "override"))));
        Facet bodyFacet = new Facet(MailFacetType.MAIL_TEXT, Collections.singletonList(new FacetValue(
            new SimpleDisplayItem("body"),
            FacetValue.UNKNOWN_COUNT,
            new Filter(Collections.singleton("body"), "override"))));
        if (folderFacet != null) {
            staticFacets.add(folderFacet);
        }
        staticFacets.add(subjectFacet);
        staticFacets.add(bodyFacet);

        List<MandatoryFilter> mandatoryFilters = (List<MandatoryFilter>) (folderFilter == null ? Collections.emptyList() : Collections.singletonList(folderFilter));
        return new ModuleConfig(getModule(), staticFacets, mandatoryFilters);
    }

    @Override
    public AutocompleteResult autocomplete(ServerSession session, AutocompleteRequest autocompleteRequest) throws OXException {
        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<FacetValue> contactValues = new ArrayList<FacetValue>(contacts.size());
        for (Contact contact : contacts) {
            // TODO: add multiple times for different mail addresses?
            String mailAddress = contact.getEmail1();
            if (mailAddress == null) {
                mailAddress = contact.getEmail2();
                if (mailAddress == null) {
                    mailAddress = contact.getEmail3();
                }
            }

            if (mailAddress == null) {
                continue;
            }

            Filter filter = new Filter(PERSONS_FILTER_FIELDS, mailAddress);
            contactValues.add(new FacetValue(new ContactDisplayItem(contact), FacetValue.UNKNOWN_COUNT, filter));
        }
        Facet contactFacet = new Facet(MailFacetType.CONTACTS, contactValues);

//        List<UserizedFolder> folders = autocompleteFolders(session, autocompleteRequest);
//        Facet folderFacet = buildFolderFacet(folders);

        List<Facet> facets = new ArrayList<Facet>();
        facets.add(contactFacet);
//        facets.add(folderFacet);

        return new AutocompleteResult(facets);
    }

    @Override
    public SearchResult search(ServerSession session, SearchRequest searchRequest) throws OXException {
        List<Filter> filters = searchRequest.getFilters();
        if (filters == null || filters.isEmpty()) {
            // TODO: throw exception, we need at least a folder filter!
        }

        String folderName = null;
        for (Filter filter : filters) {
            Set<String> fields = filter.getFields();
            if (fields.size() == 1 && "folder".equals(fields.iterator().next())) {
                folderName = filter.getQuery();
                break;
            }
        }

        if (folderName == null) {
            // TODO: throw exception, we need at least a folder filter!
        }

        FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(folderName);
        MailService mailService = Services.getMailService();
        MailMessage[] messages = null;
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, fullnameArgument.getAccountId());
            mailAccess.connect();
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            IndexRange indexRange = new IndexRange(searchRequest.getStart(), searchRequest.getStart() + searchRequest.getSize());
            MailSortField sortField = MailSortField.RECEIVED_DATE;
            OrderDirection order = OrderDirection.DESC;
            messages = messageStorage.getAllMessages(fullnameArgument.getFullname(), indexRange, sortField, order, MailField.FIELDS_LOW_COST);
//            TODO: implement real search
//            SearchTerm<?> searchTerm = new SubjectTerm("*");
//            messages = messageStorage.searchMessages(fullnameArgument.getFullname(), indexRange, sortField, order, searchTerm, MailField.FIELDS_LOW_COST);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true); // caching necessary?
            }
        }

        List<Document> documents = new ArrayList<Document>(messages.length);
        for (MailMessage message : messages) {
            documents.add(new MailDocument(message));
        }

        // TODO: Does ui need the numFound value? Could become expensive to implement here
        return new SearchResult(-1, searchRequest.getStart(), documents);
    }

    private Facet buildFolderFacet(List<UserizedFolder> folders) throws OXException {
        MailAccountStorageService mass = Services.getMailAccountStorageService();
        Map<Integer, MailAccount> accountCache = new HashMap<Integer, MailAccount>();
        List<FacetValue> folderValues = new ArrayList<FacetValue>(folders.size());
        for (UserizedFolder folder : folders) {
            FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(folder.getID());
            MailAccount mailAccount = accountCache.get(fullnameArgument.getAccountId());
            if (mailAccount == null) {
                mailAccount = mass.getMailAccount(fullnameArgument.getAccountId(), folder.getUser().getId(), folder.getContext().getContextId());
                accountCache.put(mailAccount.getId(), mailAccount);
            }

            FacetValue value = buildFolderFacetValue(folder, mailAccount);
            if (value != null) {
                folderValues.add(value);
            }
        }

        return new Facet(MailFacetType.FOLDERS, folderValues);
    }

    private FacetValue buildFolderFacetValue(UserizedFolder folder, MailAccount mailAccount) throws OXException {
        DefaultFolderType defaultFolderType = DefaultFolderType.NONE;
        if (folder.isDefault()) {
            int type = folder.getDefaultType();
            if (type == MailFolderType.INBOX.getType()) {
                defaultFolderType = DefaultMailFolderType.INBOX;
            } else if (type == MailFolderType.SENT.getType()) {
                defaultFolderType = DefaultMailFolderType.SENT;
            } else if (type == MailFolderType.TRASH.getType()) {
                defaultFolderType = DefaultMailFolderType.TRASH;
            } else if (type == MailFolderType.SPAM.getType()) {
                defaultFolderType = DefaultMailFolderType.SPAM;
            } else if (type == MailFolderType.DRAFTS.getType()) {
                defaultFolderType = DefaultMailFolderType.DRAFTS;
            }
        }
        Filter filter = new Filter(FOLDERS_FILTER_FIELDS, folder.getID());
        return new FacetValue(new FolderDisplayItem(folder, defaultFolderType, mailAccount.getName(), mailAccount.isDefaultAccount()), FacetValue.UNKNOWN_COUNT, filter);
    }

    private List<Contact> autocompleteContacts(Session session, AutocompleteRequest autocompleteRequest) throws OXException {
        ContactService contactService = Services.getContactService();
        String prefix = autocompleteRequest.getPrefix() + '*';
        ContactSearchObject searchObject = new ContactSearchObject();
        searchObject.setOrSearch(true);
        searchObject.setEmailAutoComplete(false);
        searchObject.setDisplayName(prefix);
        searchObject.setSurname(prefix);
        searchObject.setGivenName(prefix);
        searchObject.setEmail1(prefix);
        searchObject.setEmail2(prefix);
        searchObject.setEmail3(prefix);

        SortOptions sortOptions = new SortOptions();
        sortOptions.setRangeStart(0);
        sortOptions.setLimit(10);

        SearchIterator<Contact> it = contactService.searchContacts(session, searchObject, CONTACT_FIELDS, sortOptions);
        if (it == null || !it.hasNext()) {
            return Collections.emptyList();
        }

        List<Contact> contacts = new ArrayList<Contact>();
        while (it.hasNext()) {
            contacts.add(it.next());
        }

        return contacts;
    }

    private List<UserizedFolder> autocompleteFolders(Session session, AutocompleteRequest autocompleteRequest) throws OXException {
        final String prefix = autocompleteRequest.getPrefix();
        return loadMailFolders(session, new MailFolderFilter() {
            @Override
            public boolean accept(UserizedFolder folder) {
                String name = extractDisplayName(folder);
                if (name != null && name.toLowerCase().startsWith(prefix.toLowerCase())) {
                    return true;
                }

                return false;
            }
        });
    }

    private List<UserizedFolder> loadMailFolders(Session session, MailFolderFilter filter) throws OXException {
        FolderService folderService = Services.getFolderService();
        FolderResponse<UserizedFolder[]> folderResponse = folderService.getVisibleFolders(
                FolderStorage.REAL_TREE_ID,
                MailContentType.getInstance(),
                PrivateType.getInstance(),
                false,
                session,
                null);

        UserizedFolder[] folderArray = folderResponse.getResponse();
        List<UserizedFolder> folders = new ArrayList<UserizedFolder>(folderArray.length);
        for (UserizedFolder folder : folderArray) {
            if (filter == NO_FILTER) {
                folders.add(folder);
            } else if (filter.accept(folder)) {
                folders.add(folder);
            }
        }

        return folders;
    }

    private static String extractDisplayName(UserizedFolder folder) {
        String name = folder.getLocalizedName(folder.getUser().getLocale(), true);
        if (name == null) {
            name = folder.getName();
        }

        return name;
    }

    private static interface MailFolderFilter {
        boolean accept(UserizedFolder folder);
    }

}
