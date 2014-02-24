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

import static com.openexchange.find.basic.mail.Constants.FIELD_BODY;
import static com.openexchange.find.basic.mail.Constants.FIELD_FOLDER;
import static com.openexchange.find.basic.mail.Constants.FIELD_SUBJECT;
import static com.openexchange.find.basic.mail.Constants.FOLDERS_FILTER_FIELDS;
import static com.openexchange.find.basic.mail.Constants.PERSONS_FILTER_FIELDS;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.ModuleConfig;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.DefaultFolderType;
import com.openexchange.find.common.FolderDisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FieldFacet;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.MandatoryFilter;
import com.openexchange.find.mail.DefaultMailFolderType;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.groupware.container.Contact;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageInfoSupport;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderInfo;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MockMailDriver}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class MockMailDriver extends AbstractContactFacetingModuleSearchDriver {

    /**
     * Initializes a new {@link MockMailDriver}.
     */
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
        final TIntObjectMap<MailAccount> accountCache = new TIntObjectHashMap<MailAccount>(8);
        final List<MailFolderInfo> mailFolders = loadMailFolders(session, null, accountCache);
        if (mailFolders.isEmpty()) {
            throw FindExceptionCode.NO_READABLE_FOLDER.create(Module.MAIL, session.getUserId(), session.getContextId());
        }

        MailFolderInfo defaultFolder = null;
        for (final Iterator<MailFolderInfo> it = mailFolders.iterator(); it.hasNext();) {
            final MailFolderInfo folder = it.next();
            if (folder.isDefaultFolder() && folder.getDefaultFolderType() == com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType.INBOX) {
                defaultFolder = folder;
                it.remove();
            } else if (folder.isRootFolder()) {
                // Don't show root folder in facet
                it.remove();
            }
        }

        // Fallback
        if (defaultFolder == null) {
            defaultFolder = mailFolders.remove(0);
        }

        final MailAccountStorageService mass = Services.getMailAccountStorageService();
        final MailAccount mailAccount = mass.getMailAccount(defaultFolder.getAccountId(), session.getUserId(), session.getContextId());
        final FacetValue defaultValue = buildFolderFacetValue(defaultFolder, mailAccount, session.getContextId());
        final Facet folderFacet = buildFolderFacet(mailFolders, session.getUserId(), session.getContextId(), accountCache);
        folderFacet.getValues().add(defaultValue);
        final MandatoryFilter folderFilter = new MandatoryFilter(folderFacet, defaultValue);

        final List<Facet> staticFacets = new ArrayList<Facet>(3);
        final Facet subjectFacet = new FieldFacet(MailFacetType.SUBJECT, FIELD_SUBJECT);
        final Facet bodyFacet = new FieldFacet(MailFacetType.MAIL_TEXT, FIELD_BODY);
        staticFacets.add(subjectFacet);
        staticFacets.add(bodyFacet);
        if (folderFacet != null) {
            staticFacets.add(folderFacet);
        }

        return new ModuleConfig(getModule(), staticFacets, Collections.singletonList(folderFilter));
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<FacetValue> contactValues = new ArrayList<FacetValue>(contacts.size());
        for (Contact contact : contacts) {
            Filter filter = new Filter(PERSONS_FILTER_FIELDS, extractMailAddessesFrom(contact));
            String valueId = prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID()));
            contactValues.add(new FacetValue(
                valueId,
                new ContactDisplayItem(contact),
                FacetValue.UNKNOWN_COUNT,
                filter));
        }

        Facet contactFacet = new Facet(MailFacetType.CONTACTS, contactValues);
        List<Facet> facets = Collections.singletonList(contactFacet);
        return new AutocompleteResult(facets);
    }

    private List<String> extractMailAddessesFrom(final Contact contact) {
        final Set<String> addrs = new HashSet<String>(4);

        String mailAddress = contact.getEmail1();
        if (mailAddress != null) {
            addrs.add(mailAddress);
        }

        mailAddress = contact.getEmail2();
        if (mailAddress != null) {
            addrs.add(mailAddress);
        }

        mailAddress = contact.getEmail3();
        if (mailAddress != null) {
            addrs.add(mailAddress);
        }

        return new ArrayList<String>(addrs);
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        List<Filter> filters = searchRequest.getFilters();
        if (filters == null || filters.isEmpty()) {
            // TODO: throw exception, we need at least a folder filter!
        }

        String folderName = null;
        for (Filter filter : filters) {
            List<String> fields = filter.getFields();
            if (fields.size() == 1 && FIELD_FOLDER.equals(fields.iterator().next())) {
                folderName = filter.getQueries().iterator().next();
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

    private Facet buildFolderFacet(List<MailFolderInfo> folders, int userId, int contextId, TIntObjectMap<MailAccount> accountCache) throws OXException {
        MailAccountStorageService mass = Services.getMailAccountStorageService();
        List<FacetValue> folderValues = new ArrayList<FacetValue>(folders.size());
        for (MailFolderInfo folder : folders) {
            MailAccount mailAccount = accountCache.get(folder.getAccountId());
            if (mailAccount == null) {
                mailAccount = mass.getMailAccount(folder.getAccountId(), userId, contextId);
                accountCache.put(mailAccount.getId(), mailAccount);
            }

            FacetValue value = buildFolderFacetValue(folder, mailAccount, contextId);
            if (value != null) {
                folderValues.add(value);
            }
        }

        return new Facet(MailFacetType.FOLDERS, folderValues);
    }

    private FacetValue buildFolderFacetValue(MailFolderInfo defaultFolder, MailAccount mailAccount, int contextId) throws OXException {
        DefaultFolderType defaultFolderType = DefaultFolderType.NONE;
        if (defaultFolder.isDefaultFolder()) {
            com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType type = defaultFolder.getDefaultFolderType();
            if (type == com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType.INBOX) {
                defaultFolderType = DefaultMailFolderType.INBOX;
            } else if (type == com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType.SENT) {
                defaultFolderType = DefaultMailFolderType.SENT;
            } else if (type == com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType.TRASH) {
                defaultFolderType = DefaultMailFolderType.TRASH;
            } else if (type == com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType.SPAM) {
                defaultFolderType = DefaultMailFolderType.SPAM;
            } else if (type == com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType.DRAFTS) {
                defaultFolderType = DefaultMailFolderType.DRAFTS;
            }
        }
        Filter filter = new Filter(FOLDERS_FILTER_FIELDS, defaultFolder.getFullname());
        return new FacetValue(prepareFacetValueId("folder", contextId, preparedName(defaultFolder)),
            new FolderDisplayItem(defaultFolder,
                defaultFolderType,
                mailAccount.getName(),
                mailAccount.isDefaultAccount()),
                FacetValue.UNKNOWN_COUNT,
                filter);
    }

    private String preparedName(final MailFolderInfo defaultFolder) {
        return MailFolderUtility.prepareFullname(defaultFolder.getAccountId(), defaultFolder.getFullname());
    }

    private List<MailFolderInfo> autocompleteFolders(Session session, AutocompleteRequest autocompleteRequest) throws OXException {
        final String prefix = autocompleteRequest.getPrefix();
        return loadMailFolders(session, new MailFolderFilter() {
            @Override
            public boolean accept(MailFolderInfo folder) {
                String name = folder.getDisplayName();
                if (name != null && name.toLowerCase().startsWith(prefix.toLowerCase())) {
                    return true;
                }

                return false;
            }
        });
    }

    private List<MailFolderInfo> loadMailFolders(final Session session, final MailFolderFilter optFilter) throws OXException {
        return loadMailFolders(session, optFilter, null);
    }

    private List<MailFolderInfo> loadMailFolders(final Session session, final MailFolderFilter optFilter, final TIntObjectMap<MailAccount> optAccountCache) throws OXException {
        final MailService mailService = Services.getMailService();
        final List<MailFolderInfo> retval = new LinkedList<MailFolderInfo>();

        // Primay account
        retval.addAll(getFolderInfos(MailAccount.DEFAULT_ID, mailService, optFilter, session));

        // Other accounts
        final UnifiedInboxManagement uim = Services.requireService(UnifiedInboxManagement.class);
        final MailAccountStorageService mass = Services.getMailAccountStorageService();
        for (final MailAccount account : mass.getUserMailAccounts(session.getUserId(), session.getContextId())) {
            if (null != optAccountCache) {
                optAccountCache.put(account.getId(), account);
            }
            if (!account.isDefaultAccount() && account.getId() != uim.getUnifiedINBOXAccountID(session)) {
                retval.addAll(getFolderInfos(account.getId(), mailService, optFilter, session));
            }
        }

        return retval;
    }

    private List<MailFolderInfo> getFolderInfos(final int accountId, final MailService mailService, final MailFolderFilter filter, final Session session) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();
            final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            if (folderStorage instanceof IMailFolderStorageInfoSupport) {
                final IMailFolderStorageInfoSupport infoSupport = (IMailFolderStorageInfoSupport) folderStorage;
                if (null == filter) {
                    return infoSupport.getAllFolderInfos(true);
                }
                // Need to filter
                final List<MailFolderInfo> folderInfos = infoSupport.getAllFolderInfos(true);
                for (final Iterator<MailFolderInfo> i = folderInfos.iterator(); i.hasNext(); ) {
                    if (!filter.accept(i.next())) {
                        i.remove();
                    }
                }
                return folderInfos;
            }
            // The regular way...
            final List<MailFolderInfo> folderInfos = new LinkedList<MailFolderInfo>();
            final MailFolder rootFolder = folderStorage.getRootFolder();
            collectMailFolderInfos(rootFolder.getFullname(), folderStorage, accountId, folderInfos);
            if (null != filter) {
                for (final Iterator<MailFolderInfo> i = folderInfos.iterator(); i.hasNext(); ) {
                    if (!filter.accept(i.next())) {
                        i.remove();
                    }
                }
            }
            return folderInfos;
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    private void collectMailFolderInfos(final String parentFullName, final IMailFolderStorage folderStorage, final int accountId, final List<MailFolderInfo> folderInfos) throws OXException {
        for (final MailFolder child : folderStorage.getSubfolders(parentFullName, false)) {
            folderInfos.add(child.asMailFolderInfo(accountId));
            if (child.hasSubscribedSubfolders()) {
                collectMailFolderInfos(child.getFullname(), folderStorage, accountId, folderInfos);
            }
        }
    }

    private static interface MailFolderFilter {
        boolean accept(MailFolderInfo folder);
    }

}
