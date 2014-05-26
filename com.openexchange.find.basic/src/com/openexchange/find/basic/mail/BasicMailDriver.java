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

import static com.openexchange.find.basic.SimpleTokenizer.tokenize;
import static com.openexchange.find.basic.mail.Constants.FIELD_BODY;
import static com.openexchange.find.basic.mail.Constants.FIELD_CC;
import static com.openexchange.find.basic.mail.Constants.FIELD_FROM;
import static com.openexchange.find.basic.mail.Constants.FIELD_SUBJECT;
import static com.openexchange.find.basic.mail.Constants.FIELD_TIME;
import static com.openexchange.find.basic.mail.Constants.FIELD_TO;
import static com.openexchange.find.basic.mail.Constants.QUERY_LAST_MONTH;
import static com.openexchange.find.basic.mail.Constants.QUERY_LAST_WEEK;
import static com.openexchange.find.basic.mail.Constants.QUERY_LAST_YEAR;
import static com.openexchange.find.basic.mail.Constants.RECIPIENT_FIELDS;
import static com.openexchange.find.basic.mail.Constants.SENDER_AND_RECIPIENT_FIELDS;
import static com.openexchange.find.basic.mail.Constants.SENDER_FIELDS;
import static com.openexchange.find.common.CommonFacetType.GLOBAL;
import static com.openexchange.find.facet.Facets.newSimpleBuilder;
import static com.openexchange.find.mail.MailFacetType.CONTACTS;
import static com.openexchange.find.mail.MailFacetType.MAIL_TEXT;
import static com.openexchange.find.mail.MailFacetType.SUBJECT;
import static com.openexchange.find.mail.MailFacetType.TIME;
import static com.openexchange.find.mail.MailStrings.FACET_MAIL_TEXT;
import static com.openexchange.find.mail.MailStrings.FACET_RECIPIENT;
import static com.openexchange.find.mail.MailStrings.FACET_SENDER;
import static com.openexchange.find.mail.MailStrings.FACET_SENDER_AND_RECIPIENT;
import static com.openexchange.find.mail.MailStrings.FACET_SUBJECT;
import static com.openexchange.find.mail.MailStrings.LAST_MONTH;
import static com.openexchange.find.mail.MailStrings.LAST_WEEK;
import static com.openexchange.find.mail.MailStrings.LAST_YEAR;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets;
import com.openexchange.find.facet.Facets.DefaultFacetBuilder;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.Option;
import com.openexchange.find.facet.SimpleDisplayItem;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.spi.SearchConfiguration;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.java.util.TimeZones;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.BodyTerm;
import com.openexchange.mail.search.CatenatingTerm;
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
import com.openexchange.tools.session.ServerSession;

/**
 * A basic implementation to search within the mail module. Based on {@link IMailMessageStorage} and {@link SearchTerm}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class BasicMailDriver extends AbstractContactFacetingModuleSearchDriver {

    private static final Logger LOG = LoggerFactory.getLogger(BasicMailDriver.class);

    // Denotes if simple queries (from the global facet) be searched within the mail body
    private final boolean searchMailBody;

    // Name of an optional virtual all-messages folder for primary accounts
    private final String virtualAllMessagesFolder;

    public BasicMailDriver(final String virtualAllMessagesFolder, final boolean searchMailBody) {
        super();
        this.virtualAllMessagesFolder = virtualAllMessagesFolder;
        this.searchMailBody = searchMailBody;
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
    public SearchConfiguration getSearchConfiguration(ServerSession session) throws OXException {
        SearchConfiguration config = new SearchConfiguration();
        if (Strings.isEmpty(virtualAllMessagesFolder)) {
            config.setRequiresFolder();
        }

        return config;
    }

    @Override
    protected Set<FolderType> getSupportedFolderTypes() {
        return FOLDER_TYPE_NOT_SUPPORTED;
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        String prefix = autocompleteRequest.getPrefix();
        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<Facet> facets = new ArrayList<Facet>(5);
        List<String> prefixTokens = Collections.emptyList();
        if (!prefix.isEmpty()) {
            prefixTokens = tokenize(prefix);
            addSimpleFacets(facets, prefix, prefixTokens);
        }
        addContactsFacet(facets, contacts, prefix, prefixTokens, session);
        addTimeFacet(facets);
        return new AutocompleteResult(facets);
    }

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        String folderName = searchRequest.getFolderId();
        if (folderName == null && Strings.isEmpty(virtualAllMessagesFolder)) {
            throw FindExceptionCode.MISSING_MANDATORY_FACET.create(CommonFacetType.FOLDER.getId());
        }
        if (folderName == null) {
            folderName = virtualAllMessagesFolder;
        }

        int[] requestedColumns = searchRequest.getColumns();
        MailField[] mailFields = MailField.FIELDS_LOW_COST;
        if (requestedColumns != null) {
            mailFields = MailField.getMatchingFields(requestedColumns);
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
            SearchTerm<?> searchTerm = prepareSearchTerm(folder, searchRequest);
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            List<MailMessage> messages = searchMessages(
                messageStorage,
                folder,
                searchTerm,
                mailFields,
                searchRequest.getStart(),
                searchRequest.getSize());
            List<Document> documents = new ArrayList<Document>(messages.size());
            for (MailMessage message : messages) {
                documents.add(new MailDocument(message));
            }

            return new SearchResult(-1, searchRequest.getStart(), documents, searchRequest.getActiveFacets());
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    private static void addSimpleFacets(List<Facet> facets, String prefix, List<String> prefixTokens) {
        if (!prefixTokens.isEmpty()) {

            facets.add(newSimpleBuilder(GLOBAL)
                .withSimpleDisplayItem(prefix)
                .withFilter(Filter.of(GLOBAL.getId(), prefixTokens))
                .build());

            facets.add(buildSimpleFacet(
                SUBJECT,
                FACET_SUBJECT,
                prefix,
                FIELD_SUBJECT,
                prefixTokens));

            facets.add(buildSimpleFacet(
                MAIL_TEXT,
                FACET_MAIL_TEXT,
                prefix,
                FIELD_BODY,
                prefixTokens));
        }
    }

    private static SimpleFacet buildSimpleFacet(FacetType type, String formatString, String formatArg, String filterField, List<String> prefixTokens) {
        return newSimpleBuilder(type)
            .withFormattableDisplayItem(formatString, formatArg)
            .withFilter(Filter.of(filterField, prefixTokens))
            .build();
    }

    private static void addTimeFacet(List<Facet> facets) {
        facets.add(Facets.newExclusiveBuilder(TIME)
            .addValue(FacetValue.newBuilder(QUERY_LAST_WEEK)
                .withLocalizableDisplayItem(LAST_WEEK)
                .withFilter(Filter.of(FIELD_TIME, QUERY_LAST_WEEK))
                .build())
            .addValue(FacetValue.newBuilder(QUERY_LAST_MONTH)
                .withLocalizableDisplayItem(LAST_MONTH)
                .withFilter(Filter.of(FIELD_TIME, QUERY_LAST_MONTH))
                .build())
            .addValue(FacetValue.newBuilder(QUERY_LAST_YEAR)
                .withLocalizableDisplayItem(LAST_YEAR)
                .withFilter(Filter.of(FIELD_TIME, QUERY_LAST_YEAR))
                .build())
            .build());
    }

    private static void addContactsFacet(List<Facet> facets, List<Contact> contacts, String prefix, List<String> prefixTokens, ServerSession session) {
        DefaultFacetBuilder builder = Facets.newDefaultBuilder(CONTACTS);
        boolean valuesAdded = false;
        for (Contact contact : contacts) {
            String valueId = prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID()));
            List<String> queries = extractMailAddessesFrom(contact);
            builder.addValue(buildContactValue(valueId, queries, new ContactDisplayItem(contact), session));
            valuesAdded = true;
        }

        if (!prefix.isEmpty() && !prefixTokens.isEmpty()) {
            builder.addValue(buildContactValue(prefix, tokenize(prefix), new SimpleDisplayItem(prefix), session));
            valuesAdded = true;
        }

        if (valuesAdded) {
            facets.add(builder.build());
        }
    }

    private static FacetValue buildContactValue(String valueId, List<String> queries, DisplayItem item, ServerSession session) {
        return FacetValue.newBuilder(valueId)
            .withDisplayItem(item)
            .addOption(Option.newInstance("all", FACET_SENDER_AND_RECIPIENT, Filter.of(SENDER_AND_RECIPIENT_FIELDS, queries)))
            .addOption(Option.newInstance("sender", FACET_SENDER, Filter.of(SENDER_FIELDS, queries)))
            .addOption(Option.newInstance("recipient", FACET_RECIPIENT, Filter.of(RECIPIENT_FIELDS, queries)))
            .build();
    }

    private static List<MailMessage> searchMessages(IMailMessageStorage messageStorage, MailFolder folder, SearchTerm<?> searchTerm, MailField[] fields, int start, int size) throws OXException {
        MailSortField sortField = folder.isSent() ? MailSortField.SENT_DATE : MailSortField.RECEIVED_DATE;
        MailMessage[] messages = messageStorage.searchMessages(
            folder.getFullname(),
            new IndexRange(start, start + size),
            sortField,
            OrderDirection.DESC,
            searchTerm,
            fields);

        List<MailMessage> resultMessages = new ArrayList<MailMessage>(messages.length);
        Collections.addAll(resultMessages, messages);
        return resultMessages;
    }

    private static enum OP {
        OR, AND;
    }

    private static CatenatingTerm catenationFor(OP op, SearchTerm<?> t1, SearchTerm<?> t2) {
        switch(op) {
            case OR:
                return new ORTerm(t1, t2);

            case AND:
                return new ANDTerm(t1, t2);
        }

        return null;
    }

    private SearchTerm<?> prepareSearchTerm(MailFolder folder, SearchRequest searchRequest) throws OXException {
        List<String> queryFields = searchMailBody ? Constants.QUERY_FIELDS_BODY : Constants.QUERY_FIELDS;
        SearchTerm<?> queryTerm = prepareQueryTerm(folder, queryFields, searchRequest.getQueries());

        List<SearchTerm<?>> facetTerms = new LinkedList<SearchTerm<?>>();
        SearchTerm<?> timeTerm = prepareTermForFacet(searchRequest, MailFacetType.TIME, folder, OP.OR, OP.OR, OP.OR);
        if (timeTerm != null) {
            facetTerms.add(timeTerm);
        }

        SearchTerm<?> subjectTerm = prepareTermForFacet(searchRequest, MailFacetType.SUBJECT, folder, OP.AND, OP.AND, OP.AND);
        if (subjectTerm != null) {
            facetTerms.add(subjectTerm);
        }

        SearchTerm<?> bodyTerm = prepareTermForFacet(searchRequest, MailFacetType.MAIL_TEXT, folder, OP.AND, OP.AND, OP.AND);
        if (bodyTerm != null) {
            facetTerms.add(bodyTerm);
        }

        SearchTerm<?> contactsTerm = prepareTermForFacet(searchRequest, MailFacetType.CONTACTS, folder, OP.AND, OP.OR, OP.OR);
        if (contactsTerm != null) {
            facetTerms.add(contactsTerm);
        }

        SearchTerm<?> facetTerm = null;
        if (!facetTerms.isEmpty()) {
            if (facetTerms.size() == 1) {
                facetTerm = facetTerms.get(0);
            } else {
                Iterator<SearchTerm<?>> it = facetTerms.iterator();
                SearchTerm<?> t1 = it.next();
                SearchTerm<?> t2 = it.next();
                CatenatingTerm finalTerm = catenationFor(OP.AND, t1, t2);
                while (it.hasNext()) {
                    CatenatingTerm newTerm = catenationFor(OP.AND, finalTerm.getSecondTerm(), it.next());
                    finalTerm.setSecondTerm(newTerm);
                }
                facetTerm = finalTerm;
            }
        }

        SearchTerm<?> searchTerm = null;
        if (facetTerm == null || queryTerm == null) {
            if (facetTerm != null) {
                searchTerm = facetTerm;
            } else {
                searchTerm = queryTerm;
            }
        } else {
            searchTerm = new ANDTerm(queryTerm, facetTerm);
        }

        return searchTerm;
    }

    private static SearchTerm<?> prepareQueryTerm(MailFolder folder, List<String> fields, List<String> queries) throws OXException {
        if (queries == null || queries.isEmpty()) {
            return null;
        }

        return termFor(fields, queries, OP.OR, OP.AND, folder.isSent());
    }

    private static SearchTerm<?> prepareTermForFacet(SearchRequest searchRequest, FacetType type, MailFolder folder, OP filterOP, OP fieldOP, OP queryOP) throws OXException {
        List<ActiveFacet> facets = searchRequest.getActiveFacets(type);
        if (facets != null && !facets.isEmpty()) {
            List<Filter> filters = new LinkedList<Filter>();
            for (ActiveFacet facet : facets) {
                Filter filter = facet.getFilter();
                if (filter != Filter.NO_FILTER) {
                    filters.add(filter);
                }
            }

            return prepareFilterTerm(folder, filters, filterOP, fieldOP, queryOP);
        }

        return null;
    }

    private static SearchTerm<?> prepareFilterTerm(MailFolder folder, List<Filter> filters, OP filterOP, OP fieldOP, OP queryOP) throws OXException {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        if (filters.size() == 1) {
            return termFor(filters.get(0), fieldOP, queryOP, folder.isSent());
        }

        Iterator<Filter> it = filters.iterator();
        Filter f1 = it.next();
        Filter f2 = it.next();
        CatenatingTerm finalTerm = catenationFor(filterOP, termFor(f1, fieldOP, queryOP, folder.isSent()), termFor(f2, fieldOP, queryOP, folder.isSent()));
        while (it.hasNext()) {
            CatenatingTerm newTerm = catenationFor(filterOP, finalTerm.getSecondTerm(), termFor(it.next(), fieldOP, queryOP, folder.isSent()));
            finalTerm.setSecondTerm(newTerm);
        }

        return finalTerm;
    }

    private static SearchTerm<?> termFor(Filter filter, OP fieldOP, OP queryOP, boolean isOutgoingFolder) throws OXException {
        List<String> fields = filter.getFields();
        if (fields == null || fields.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_FIELDS.create(filter);
        }

        List<String> queries = filter.getQueries();
        if (queries == null || queries.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_QUERIES.create(filter);
        }

        return termFor(fields, queries, fieldOP, queryOP, isOutgoingFolder);
    }

    private static SearchTerm<?> termFor(List<String> fields, List<String> queries, OP fieldOP, OP queryOP, boolean isOutgoingFolder) throws OXException {
        if (fields.size() > 1) {
            Iterator<String> it = fields.iterator();
            String f1 = it.next();
            String f2 = it.next();
            CatenatingTerm finalTerm = catenationFor(fieldOP, termForField(f1, queries, queryOP, isOutgoingFolder), termForField(f2, queries, queryOP, isOutgoingFolder));
            while (it.hasNext()) {
                String f = it.next();
                CatenatingTerm newTerm = catenationFor(fieldOP, finalTerm.getSecondTerm(), termForField(f, queries, queryOP, isOutgoingFolder));
                finalTerm.setSecondTerm(newTerm);
            }

            return finalTerm;
        }

        return termForField(fields.iterator().next(), queries, queryOP, isOutgoingFolder);
    }

    private static SearchTerm<?> termForField(String field, List<String> queries, OP queryOP, boolean isOutgoingFolder) throws OXException {
        if (queries.size() > 1) {
            Iterator<String> it = queries.iterator();
            String q1 = it.next();
            String q2 = it.next();
            CatenatingTerm finalTerm = catenationFor(queryOP, termForQuery(field, q1, isOutgoingFolder), termForQuery(field, q2, isOutgoingFolder));
            while (it.hasNext()) {
                String q = it.next();
                CatenatingTerm newTerm = catenationFor(queryOP, finalTerm.getSecondTerm(), termForQuery(field, q, isOutgoingFolder));
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
        SearchTerm<?> term = null;
        switch (comparison) {
            case EQUALS:
            {
                term = dateTermFor(ComparisonType.EQUALS, new Date(timestamp), isOutgoingFolder);
                break;
            }

            case GREATER_THAN:
            {
                term = dateTermFor(ComparisonType.GREATER_THAN, new Date(timestamp), isOutgoingFolder);
                break;
            }

            case LOWER_THAN:
            {
                term = dateTermFor(ComparisonType.LESS_THAN, new Date(timestamp), isOutgoingFolder);
                break;
            }

            case GREATER_EQUALS:
            {
                term = dateTermFor(ComparisonType.GREATER_THAN, new Date(timestamp - 1), isOutgoingFolder);
                break;
            }

            case LOWER_EQUALS:
            {
                term = dateTermFor(ComparisonType.LESS_THAN, new Date(timestamp + 1), isOutgoingFolder);
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
        if (Strings.isEmpty(query)) {
            throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, FIELD_TIME);
        }

        Comparison comparison;
        long timestamp;
        Calendar cal = new GregorianCalendar(TimeZones.UTC);
        if (QUERY_LAST_WEEK.equals(query)) {
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else if (QUERY_LAST_MONTH.equals(query)) {
            cal.add(Calendar.MONTH, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else if (QUERY_LAST_YEAR.equals(query)) {
            cal.add(Calendar.YEAR, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else {
            /*
             * This block just preserves the code, as we likely have to implement
             * custom time ranges in the future. Currently this else path should
             * never be called.
             *
             * Idea: Introduce an additional custom time facet that might be set,
             * but is not part of autocomplete responses. If it is set, we also
             * should not deliver the normal time facet in autocomplete responses.
             *
             * {
             *   'facet':'time_custom',
             *   'value':'>=12345678900'
             * }
             */
            char[] chars = query.toCharArray();
            String sTimestamp;
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
                } else {
                    comparison = Comparison.EQUALS;
                }

                sTimestamp = String.copyValueOf(chars, offset, chars.length);
            } else {
                comparison = Comparison.EQUALS;
                sTimestamp = query;
            }

            try {
                timestamp = Long.parseLong(sTimestamp);
            } catch (NumberFormatException e) {
                throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, FIELD_TIME);
            }
        }

        return new Pair<Comparison, Long>(comparison, timestamp);
    }
}
