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

import static com.openexchange.find.basic.mail.Constants.*;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.DefaultFolderType;
import com.openexchange.find.common.FolderDisplayItem;
import com.openexchange.find.common.FormattableDisplayItem;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FieldFacet;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.mail.DefaultMailFolderType;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.mail.MailStrings;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.Pair;
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
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.BodyTerm;
import com.openexchange.mail.search.CcTerm;
import com.openexchange.mail.search.ComparablePattern;
import com.openexchange.mail.search.ComparisonType;
import com.openexchange.mail.search.FromTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.ReceivedDateTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SentDateTerm;
import com.openexchange.mail.search.SubjectTerm;
import com.openexchange.mail.search.ToTerm;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * A basic implementation to search within the mail module. Based on {@link IMailMessageStorage} and {@link SearchTerm}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class BasicMailDriver extends AbstractContactFacetingModuleSearchDriver {

    private static final Logger LOG = LoggerFactory.getLogger(BasicMailDriver.class);

    public BasicMailDriver() {
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
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        // TODO: load folders and contacts in parallel
        final String prefix = autocompleteRequest.getPrefix();
        final Facet subjectFacet = new FieldFacet(
            MailFacetType.SUBJECT,
            new FormattableDisplayItem(MailStrings.FACET_SUBJECT, prefix),
            FIELD_SUBJECT,
            prefix);
        final Facet bodyFacet = new FieldFacet(
            MailFacetType.MAIL_TEXT,
            new FormattableDisplayItem(MailStrings.FACET_MAIL_TEXT, prefix),
            FIELD_BODY,
            prefix);
//        final Facet timeFacet = buildTimeFacet();
        final Facet folderFacet = buildFolderFacet(session, autocompleteRequest.getFolder());
        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<FacetValue> contactValues = new ArrayList<FacetValue>(contacts.size());
        for (Contact contact : contacts) {
            String valueId = prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID()));
            List<Filter> filters = new ArrayList<Filter>(3);
            filters.add(new Filter(
                "all",
                MailStrings.FACET_SENDER_AND_RECIPIENT,
                SENDER_AND_RECIPIENT_FIELDS,
                extractMailAddessesFrom(contact)));
            filters.add(new Filter(
                "sender",
                MailStrings.FACET_SENDER, SENDER_FIELDS,
                extractMailAddessesFrom(contact)));
            filters.add(new Filter("recipient",
                MailStrings.FACET_RECIPIENT,
                RECIPIENT_FIELDS,
                extractMailAddessesFrom(contact)));
            contactValues.add(new FacetValue(
                valueId,
                new ContactDisplayItem(contact),
                FacetValue.UNKNOWN_COUNT,
                filters));
        }

        List<Facet> facets = new ArrayList<Facet>(5);
        Facet contactFacet = new Facet(MailFacetType.CONTACTS, contactValues);
        facets.add(subjectFacet);
        facets.add(bodyFacet);
        facets.add(contactFacet);
//        facets.add(timeFacet);
        facets.add(folderFacet);

        return new AutocompleteResult(facets);
    }

    @Override
    protected DisplayItem getDisplayItemForGlobalFacet(AutocompleteRequest autocompleteRequest) {
        return new FormattableDisplayItem(MailStrings.FACET_GLOBAL, autocompleteRequest.getPrefix());
    }

    @Override
    protected Filter getFilterForGlobalFacet(AutocompleteRequest autocompleteRequest) {
        return new Filter(QUERY_FIELDS, autocompleteRequest.getPrefix());
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        List<Filter> filters = new LinkedList<Filter>(searchRequest.getFilters());
        if (filters == null || filters.isEmpty()) {
            throw FindExceptionCode.MISSING_SEARCH_FILTER.create("folder", Module.MAIL.getIdentifier());
        }

        String folderName = prepareFiltersAndGetFolder(filters);
        if (folderName == null) {
            throw FindExceptionCode.MISSING_SEARCH_FILTER.create("folder", Module.MAIL.getIdentifier());
        }

        FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(folderName);
        int accountId = fullnameArgument.getAccountId();
        MailService mailService = Services.getMailService();
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            MailFolder folder = folderStorage.getFolder(fullnameArgument.getFullname());

            SearchTerm<?> searchTerm = prepareSearchTerm(folder, searchRequest.getQueries(), filters);
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            List<MailMessage> messages = searchMessages(
                messageStorage,
                folder,
                searchTerm,
                searchRequest.getStart(),
                searchRequest.getSize());
            List<Document> documents = new ArrayList<Document>(messages.size());
            for (MailMessage message : messages) {
                documents.add(new MailDocument(message));
            }

            return new SearchResult(-1, searchRequest.getStart(), documents);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    private static Facet buildFolderFacet(ServerSession session, String currentFolder) throws OXException {
        final TIntObjectMap<MailAccount> accountCache = new TIntObjectHashMap<MailAccount>(8);
        final List<MailFolderInfo> mailFolders = loadMailFolders(session, currentFolder, accountCache);
        if (mailFolders.isEmpty()) {
            throw FindExceptionCode.NO_READABLE_FOLDER.create(Module.MAIL, session.getUserId(), session.getContextId());
        }

        return buildFolderFacet(mailFolders, session.getUserId(), session.getContextId(), accountCache);
    }

    private static List<MailMessage> searchMessages(IMailMessageStorage messageStorage, MailFolder folder, SearchTerm<?> searchTerm, int start, int size) throws OXException {
        MailSortField sortField = folder.isSent() ? MailSortField.SENT_DATE : MailSortField.RECEIVED_DATE;
        MailMessage[] messages = messageStorage.searchMessages(
            folder.getFullname(),
            new IndexRange(start, start + size),
            sortField,
            OrderDirection.DESC,
            searchTerm,
            MailField.FIELDS_LOW_COST);

        if (start > messages.length) {
            return Collections.emptyList();
        }

        List<MailMessage> resultMessages = new ArrayList<MailMessage>(messages.length);
        Collections.addAll(resultMessages, messages);
        int toIndex = (start + size) <= resultMessages.size() ? (start + size) : resultMessages.size();
        return resultMessages.subList(start, toIndex);
    }

    private static List<String> extractMailAddessesFrom(final Contact contact) {
        final List<String> addrs = new ArrayList<String>(3);

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

        return addrs;
    }

    private static Facet buildTimeFacet() {
        return new FieldFacet(MailFacetType.TIME, FIELD_TIME);
        /*
        List<FacetValue> values = new ArrayList<FacetValue>(3);
        values.add(new FacetValue(
            MailConstants.FACET_VALUE_LAST_WEEK,
            new SimpleDisplayItem(MailStrings.LAST_WEEK),
            FacetValue.UNKNOWN_COUNT,
            new Filter(asList(FIELD_TIME), MailConstants.FACET_VALUE_LAST_WEEK)));
        values.add(new FacetValue(
            MailConstants.FACET_VALUE_LAST_MONTH,
            new SimpleDisplayItem(MailStrings.LAST_MONTH),
            FacetValue.UNKNOWN_COUNT,
            new Filter(asList(FIELD_TIME), MailConstants.FACET_VALUE_LAST_MONTH)));
        values.add(new FacetValue(
            MailConstants.FACET_VALUE_LAST_YEAR,
            new SimpleDisplayItem(MailStrings.LAST_YEAR),
            FacetValue.UNKNOWN_COUNT,
            new Filter(asList(FIELD_TIME), MailConstants.FACET_VALUE_LAST_YEAR)));

        return new Facet(MailFacetType.TIME, values);
        */
    }

    private static Facet buildFolderFacet(List<MailFolderInfo> folders, int userId, int contextId, TIntObjectMap<MailAccount> accountCache) throws OXException {
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

    private static FacetValue buildFolderFacetValue(MailFolderInfo defaultFolder, MailAccount mailAccount, int contextId) throws OXException {
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
        Filter filter = new Filter(FOLDERS_FIELDS, MailFolderUtility.prepareFullname(mailAccount.getId(), defaultFolder.getFullname()));
        return new FacetValue(prepareFacetValueId("folder", contextId, preparedName(defaultFolder)),
            new FolderDisplayItem(defaultFolder,
                defaultFolderType,
                mailAccount.getName(),
                mailAccount.isDefaultAccount()),
                FacetValue.UNKNOWN_COUNT,
                filter);
    }

    private static String preparedName(final MailFolderInfo defaultFolder) {
        return MailFolderUtility.prepareFullname(defaultFolder.getAccountId(), defaultFolder.getFullname());
    }

    private static List<MailFolderInfo> loadMailFolders(final Session session, final String currentFolder, final TIntObjectMap<MailAccount> optAccountCache) throws OXException {
        final MailService mailService = Services.getMailService();
        final LinkedList<MailFolderInfo> retval = new LinkedList<MailFolderInfo>();
        FullnameArgument currentFullname = null;
        if (currentFolder != null) {
            currentFullname = MailFolderUtility.prepareMailFolderParam(currentFolder);
        }

        // primary account
        if (currentFullname != null && currentFullname.getAccountId() == MailAccount.DEFAULT_ID) {
            addFolderInfos(retval, session, mailService, MailAccount.DEFAULT_ID, currentFullname.getFullname());
        } else {
            addFolderInfos(retval, session, mailService, MailAccount.DEFAULT_ID, null);
        }

        // Other accounts
        final UnifiedInboxManagement uim = Services.requireService(UnifiedInboxManagement.class);
        final MailAccountStorageService mass = Services.getMailAccountStorageService();
        for (final MailAccount account : mass.getUserMailAccounts(session.getUserId(), session.getContextId())) {
            if (null != optAccountCache) {
                optAccountCache.put(account.getId(), account);
            }
            if (!account.isDefaultAccount() && account.getId() != uim.getUnifiedINBOXAccountID(session)) {
                if (currentFullname != null && currentFullname.getAccountId() == account.getId()) {
                    addFolderInfos(retval, session, mailService, account.getId(), currentFullname.getFullname());
                } else {
                    addFolderInfos(retval, session, mailService, account.getId(), null);
                }
            }
        }

        return retval;
    }

    private static void addFolderInfos(final Deque<MailFolderInfo> infos, final Session session, final MailService mailService, final int accountId, final String optFullName) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            String inbox = "INBOX";
            List<String> foldersToLoad = new LinkedList<String>();
            if (accountId == MailAccount.DEFAULT_ID) {
                String drafts = folderStorage.getDraftsFolder();
                String sent = folderStorage.getSentFolder();
                String[] defaultFolders = new String[] { inbox, drafts, sent };
                for (String folder : defaultFolders) {
                    if (optFullName == null || !optFullName.equals(folder)) {
                        foldersToLoad.add(folder);
                    }
                }
            } else {
                if (optFullName == null || !optFullName.equals(inbox)) {
                    foldersToLoad.add(inbox);
                }
            }

            if (optFullName != null) {
                infos.addFirst(getFolderInfo(optFullName, accountId, folderStorage));
            }

            for (String folder : foldersToLoad) {
                infos.add(getFolderInfo(folder, accountId, folderStorage));
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    private static MailFolderInfo getFolderInfo(final String fullName, final int accountId, final IMailFolderStorage folderStorage) throws OXException {
        if (folderStorage instanceof IMailFolderStorageInfoSupport) {
            final IMailFolderStorageInfoSupport infoSupport = (IMailFolderStorageInfoSupport) folderStorage;
            if (infoSupport.isInfoSupported()) {
                return infoSupport.getFolderInfo(fullName);
            }
        }

        // Alternative way
        return folderStorage.getFolder(fullName).asMailFolderInfo(accountId);
    }

    private static List<MailFolderInfo> getFolderInfos(final int accountId, final MailService mailService, final MailFolderFilter filter, final Session session) throws OXException {
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

    private static void collectMailFolderInfos(final String parentFullName, final IMailFolderStorage folderStorage, final int accountId, final List<MailFolderInfo> folderInfos) throws OXException {
        for (final MailFolder child : folderStorage.getSubfolders(parentFullName, false)) {
            folderInfos.add(child.asMailFolderInfo(accountId));
            if (child.hasSubscribedSubfolders()) {
                collectMailFolderInfos(child.getFullname(), folderStorage, accountId, folderInfos);
            }
        }
    }

    private static SearchTerm<?> prepareSearchTerm(MailFolder folder, List<String> queries, List<Filter> filters) throws OXException {
        SearchTerm<?> queryTerm = prepareQueryTerm(folder, queries);
        SearchTerm<?> filterTerm = prepareFilterTerm(folder, filters);
        SearchTerm<?> searchTerm = null;
        if (filterTerm == null || queryTerm == null) {
            if (filterTerm != null) {
                searchTerm = filterTerm;
            } else {
                searchTerm = queryTerm;
            }
        } else {
            searchTerm = new ANDTerm(queryTerm, filterTerm);
        }

        return searchTerm;
    }

    private static SearchTerm<?> prepareQueryTerm(MailFolder folder, List<String> queries) throws OXException {
        if (queries == null || queries.isEmpty()) {
            return null;
        }

        return termFor(QUERY_FIELDS, queries, folder.isSent());
    }

    private static SearchTerm<?> prepareFilterTerm(MailFolder folder, List<Filter> filters) throws OXException {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        if (filters.size() == 1) {
            return termFor(filters.get(0), folder.isSent());
        }

        Iterator<Filter> it = filters.iterator();
        Filter f1 = it.next();
        Filter f2 = it.next();
        ANDTerm finalTerm = new ANDTerm(termFor(f1, folder.isSent()), termFor(f2, folder.isSent()));
        while (it.hasNext()) {
            ANDTerm newTerm = new ANDTerm(finalTerm.getSecondTerm(), termFor(it.next(), folder.isSent()));
            finalTerm.setSecondTerm(newTerm);
        }

        return finalTerm;
    }

    private static SearchTerm<?> termFor(Filter filter, boolean isOutgoingFolder) throws OXException {
        List<String> fields = filter.getFields();
        if (fields == null || fields.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_FIELDS.create(filter);
        }

        List<String> queries = filter.getQueries();
        if (queries == null || queries.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_QUERIES.create(filter);
        }

        return termFor(fields, queries, isOutgoingFolder);
    }

    private static SearchTerm<?> termFor(List<String> fields, List<String> queries, boolean isOutgoingFolder) throws OXException {
        if (fields.size() > 1) {
            Iterator<String> it = fields.iterator();
            String f1 = it.next();
            String f2 = it.next();
            ORTerm finalTerm = new ORTerm(termForField(f1, queries, isOutgoingFolder), termForField(f2, queries, isOutgoingFolder));
            while (it.hasNext()) {
                String f = it.next();
                ORTerm newTerm = new ORTerm(finalTerm.getSecondTerm(), termForField(f, queries, isOutgoingFolder));
                finalTerm.setSecondTerm(newTerm);
            }

            return finalTerm;
        }

        return termForField(fields.iterator().next(), queries, isOutgoingFolder);
    }

    private static SearchTerm<?> termForField(String field, List<String> queries, boolean isOutgoingFolder) throws OXException {
        if (queries.size() > 1) {
            Iterator<String> it = queries.iterator();
            String q1 = it.next();
            String q2 = it.next();
            ORTerm finalTerm = new ORTerm(termForQuery(field, q1, isOutgoingFolder), termForQuery(field, q2, isOutgoingFolder));
            while (it.hasNext()) {
                String q = it.next();
                ORTerm newTerm = new ORTerm(finalTerm.getSecondTerm(), termForQuery(field, q, isOutgoingFolder));
                finalTerm.setSecondTerm(newTerm);
            }

            return finalTerm;
        }

        return termForQuery(field, queries.iterator().next(), isOutgoingFolder);
    }

    private static enum Comparison {
        GREATER_THAN, GREATER_EQUALS, EQUALS, LOWER_THAN, LOWER_EQUALS;
    }

    private static SearchTerm<?> termForQuery(String field, String query, boolean isOutgoingFolder) throws OXException {
        if (FIELD_FROM.equals(field)) {
            return new FromTerm(query);
        } else if (FIELD_TO.equals(field)) {
            return new ToTerm(query);
        } else if (FIELD_CC.equals(field)) {
            return new CcTerm(query);
        } else if (FIELD_SUBJECT.equals(field)) {
            return new SubjectTerm(query);
        } else if (FIELD_BODY.equals(field)) {
            return new BodyTerm(query);
        } else if (FIELD_TIME.equals(field)) {
            Pair<Comparison, Long> parsed = parseTimeQuery(query);
            Comparison comparison = parsed.getFirst();
            Long timestamp = parsed.getSecond();
            return buildTimeTerm(comparison, timestamp, isOutgoingFolder);
        }

        throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(field);
    }

    private static SearchTerm<?> buildTimeTerm(Comparison comparison, long timestamp, boolean isOutgoingFolder) {
        Date date = new Date(timestamp);
        SearchTerm<?> term = null;
        switch (comparison) {
            case EQUALS:
            {
                term = dateTermFor(ComparisonType.EQUALS, date, isOutgoingFolder);
                break;
            }

            case GREATER_THAN:
            {
                term = dateTermFor(ComparisonType.GREATER_THAN, date, isOutgoingFolder);
                break;
            }

            case LOWER_THAN:
            {
                term = dateTermFor(ComparisonType.LESS_THAN, date, isOutgoingFolder);
                break;
            }

            case GREATER_EQUALS:
            {
                SearchTerm<ComparablePattern<Date>> equals =
                    dateTermFor(ComparisonType.EQUALS, date, isOutgoingFolder);
                SearchTerm<ComparablePattern<Date>> greater =
                    dateTermFor(ComparisonType.GREATER_THAN, date, isOutgoingFolder);
                term = new ORTerm(equals, greater);
                break;
            }

            case LOWER_EQUALS:
            {
                SearchTerm<ComparablePattern<Date>> equals =
                    dateTermFor(ComparisonType.EQUALS, date, isOutgoingFolder);
                SearchTerm<ComparablePattern<Date>> lower =
                    dateTermFor(ComparisonType.LESS_THAN, date, isOutgoingFolder);
                term = new ORTerm(equals, lower);
                break;
            }
        }

        return term;
    }

    private static SearchTerm<ComparablePattern<java.util.Date>> dateTermFor(ComparisonType comparisonType, Date date, boolean isOutgoingFolder) {
        if (isOutgoingFolder) {
            return new SentDateTerm(comparisonType, date);
        }

        return new ReceivedDateTerm(comparisonType, date);
    }

    private static Pair<Comparison, Long> parseTimeQuery(String query) throws OXException {
        char[] chars = query.toCharArray();
        if (chars.length == 0) {
            throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, FIELD_TIME);
        }

        Comparison comparison = Comparison.EQUALS;
        String timestamp = query;
        if (chars.length > 1) {
            int offset = 0;
            if (chars[0] == '<') {
                offset = 1;
                comparison = Comparison.LOWER_THAN;
                if (chars[1] == '=') {
                    offset = 2;
                    comparison = Comparison.LOWER_EQUALS;
                }
            } else if (chars[0] == '>') {
                offset = 1;
                comparison = Comparison.GREATER_THAN;
                if (chars[1] == '=') {
                    offset = 2;
                    comparison = Comparison.GREATER_EQUALS;
                }
            }

            timestamp = String.copyValueOf(chars, offset, chars.length);
        }

        try {
            long ts = Long.parseLong(timestamp);
            return new Pair<BasicMailDriver.Comparison, Long>(comparison, ts);
        } catch (NumberFormatException e) {
            throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, FIELD_TIME);
        }
    }

    private static String prepareFiltersAndGetFolder(List<Filter> filters) {
        String folderName = null;
        Iterator<Filter> it = filters.iterator();
        while (it.hasNext()) {
            Filter filter = it.next();
            folderName = determineFolderName(filter);
            if (folderName != null) {
                it.remove();
                break;
            }
        }

        return folderName;
    }

    private static String determineFolderName(Filter filter) {
        String folderName = null;
        List<String> fields = filter.getFields();
        if (fields.size() == 1 && FIELD_FOLDER.equals(fields.iterator().next())) {
            try {
                folderName = filter.getQueries().iterator().next();
            } catch (NoSuchElementException e) {
                // ignore
            }
        }

        return folderName;
    }

    private static interface MailFolderFilter {
        boolean accept(MailFolderInfo folder);
    }
}
