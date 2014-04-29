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
import static com.openexchange.find.basic.mail.Constants.FIELD_CC;
import static com.openexchange.find.basic.mail.Constants.FIELD_FROM;
import static com.openexchange.find.basic.mail.Constants.FIELD_SUBJECT;
import static com.openexchange.find.basic.mail.Constants.FIELD_TIME;
import static com.openexchange.find.basic.mail.Constants.FIELD_TO;
import static com.openexchange.find.basic.mail.Constants.RECIPIENT_FIELDS;
import static com.openexchange.find.basic.mail.Constants.SENDER_AND_RECIPIENT_FIELDS;
import static com.openexchange.find.basic.mail.Constants.SENDER_FIELDS;
import static com.openexchange.find.basic.mail.Constants.asList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
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
import com.openexchange.find.common.FormattableDisplayItem;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FieldFacet;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.mail.MailConstants;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.mail.MailStrings;
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
    protected Set<Integer> getSupportedFolderTypes() {
        return FOLDER_TYPE_NOT_SUPPORTED;
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        String prefix = autocompleteRequest.getPrefix();
        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<Facet> facets = new ArrayList<Facet>(5);
        addFieldFacets(facets, prefix);
        addContactFacet(facets, contacts, prefix, session);
        addTimeFacet(facets);
        return new AutocompleteResult(facets);
    }

    @Override
    protected String getFormatStringForGlobalFacet() {
        return MailStrings.FACET_GLOBAL;
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

        FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(folderName);
        int accountId = fullnameArgument.getAccountId();
        MailService mailService = Services.getMailService();
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            MailFolder folder = folderStorage.getFolder(fullnameArgument.getFullname());

            List<Filter> filters = searchRequest.getFilters();
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

            return new SearchResult(-1, searchRequest.getStart(), documents, searchRequest.getActiveFacets());
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true);
            }
        }
    }

    private static void addFieldFacets(List<Facet> facets, String prefix) {
        if (!prefix.isEmpty()) {
            Facet subjectFacet = new FieldFacet(
                MailFacetType.SUBJECT,
                new FormattableDisplayItem(MailStrings.FACET_SUBJECT, prefix),
                FIELD_SUBJECT,
                prefix);
            facets.add(subjectFacet);
            Facet bodyFacet = new FieldFacet(
                MailFacetType.MAIL_TEXT,
                new FormattableDisplayItem(MailStrings.FACET_MAIL_TEXT, prefix),
                FIELD_BODY,
                prefix);
            facets.add(bodyFacet);
        }
    }

    private static void addTimeFacet(List<Facet> facets) {
        List<FacetValue> values = new ArrayList<FacetValue>(3);
        values.add(new FacetValue(
            MailConstants.FACET_VALUE_LAST_WEEK,
            new SimpleDisplayItem(MailStrings.LAST_WEEK, true),
            FacetValue.UNKNOWN_COUNT,
            new Filter(asList(FIELD_TIME), MailConstants.FACET_VALUE_LAST_WEEK)));
        values.add(new FacetValue(
            MailConstants.FACET_VALUE_LAST_MONTH,
            new SimpleDisplayItem(MailStrings.LAST_MONTH, true),
            FacetValue.UNKNOWN_COUNT,
            new Filter(asList(FIELD_TIME), MailConstants.FACET_VALUE_LAST_MONTH)));
        values.add(new FacetValue(
            MailConstants.FACET_VALUE_LAST_YEAR,
            new SimpleDisplayItem(MailStrings.LAST_YEAR, true),
            FacetValue.UNKNOWN_COUNT,
            new Filter(asList(FIELD_TIME), MailConstants.FACET_VALUE_LAST_YEAR)));
        facets.add(new Facet(MailFacetType.TIME, values));
    }

    private static void addContactFacet(List<Facet> facets, List<Contact> contacts, String prefix, ServerSession session) {
        List<FacetValue> contactValues = new ArrayList<FacetValue>(contacts.size());
        for (Contact contact : contacts) {
            String valueId = prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID()));
            List<String> queries = extractMailAddessesFrom(contact);
            contactValues.add(buildContactValue(valueId, queries, new ContactDisplayItem(contact), session));
        }

        if (!prefix.isEmpty()) {
            contactValues.add(buildContactValue(prefix, asList(prefix), new SimpleDisplayItem(prefix), session));
        }

        if (!contactValues.isEmpty()) {
            facets.add(new Facet(MailFacetType.CONTACTS, contactValues));
        }
    }

    private static FacetValue buildContactValue(String valueId, List<String> queries, DisplayItem item, ServerSession session) {
        List<Filter> filters = new ArrayList<Filter>(3);
        filters.add(new Filter(
            "all",
            MailStrings.FACET_SENDER_AND_RECIPIENT,
            SENDER_AND_RECIPIENT_FIELDS,
            queries));
        filters.add(new Filter(
            "sender",
            MailStrings.FACET_SENDER, SENDER_FIELDS,
            queries));
        filters.add(new Filter("recipient",
            MailStrings.FACET_RECIPIENT,
            RECIPIENT_FIELDS,
            queries));
        return new FacetValue(
            valueId,
            item,
            FacetValue.UNKNOWN_COUNT,
            filters);
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

        List<MailMessage> resultMessages = new ArrayList<MailMessage>(messages.length);
        Collections.addAll(resultMessages, messages);
        return resultMessages;
    }

    private static List<String> extractMailAddessesFrom(final Contact contact) {
        List<String> addrs = new ArrayList<String>(3);
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

    private SearchTerm<?> prepareSearchTerm(MailFolder folder, List<String> queries, List<Filter> filters) throws OXException {
        List<String> queryFields = searchMailBody ? Constants.QUERY_FIELDS_BODY : Constants.QUERY_FIELDS;
        SearchTerm<?> queryTerm = prepareQueryTerm(folder, queryFields, queries);
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

    private static SearchTerm<?> prepareQueryTerm(MailFolder folder, List<String> fields, List<String> queries) throws OXException {
        if (queries == null || queries.isEmpty()) {
            return null;
        }

        return termFor(fields, queries, OP.AND, folder.isSent());
    }

    private static SearchTerm<?> prepareFilterTerm(MailFolder folder, List<Filter> filters) throws OXException {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        OP queryOperator = OP.OR;
        if (filters.size() == 1) {
            return termFor(filters.get(0), queryOperator, folder.isSent());
        }

        Iterator<Filter> it = filters.iterator();
        Filter f1 = it.next();
        Filter f2 = it.next();
        ANDTerm finalTerm = new ANDTerm(termFor(f1, queryOperator, folder.isSent()), termFor(f2, queryOperator, folder.isSent()));
        while (it.hasNext()) {
            ANDTerm newTerm = new ANDTerm(finalTerm.getSecondTerm(), termFor(it.next(), queryOperator, folder.isSent()));
            finalTerm.setSecondTerm(newTerm);
        }

        return finalTerm;
    }

    private static SearchTerm<?> termFor(Filter filter, OP queryOperator, boolean isOutgoingFolder) throws OXException {
        List<String> fields = filter.getFields();
        if (fields == null || fields.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_FIELDS.create(filter);
        }

        List<String> queries = filter.getQueries();
        if (queries == null || queries.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_QUERIES.create(filter);
        }

        return termFor(fields, queries, queryOperator, isOutgoingFolder);
    }

    private static SearchTerm<?> termFor(List<String> fields, List<String> queries, OP queryOperator, boolean isOutgoingFolder) throws OXException {
        if (fields.size() > 1) {
            Iterator<String> it = fields.iterator();
            String f1 = it.next();
            String f2 = it.next();
            CatenatingTerm finalTerm = catenationFor(OP.OR, termForField(f1, queries, queryOperator, isOutgoingFolder), termForField(f2, queries, queryOperator, isOutgoingFolder));
            while (it.hasNext()) {
                String f = it.next();
                CatenatingTerm newTerm = catenationFor(OP.OR, finalTerm.getSecondTerm(), termForField(f, queries, queryOperator, isOutgoingFolder));
                finalTerm.setSecondTerm(newTerm);
            }

            return finalTerm;
        }

        return termForField(fields.iterator().next(), queries, queryOperator, isOutgoingFolder);
    }

    private static SearchTerm<?> termForField(String field, List<String> queries, OP queryOperator, boolean isOutgoingFolder) throws OXException {
        if (queries.size() > 1) {
            Iterator<String> it = queries.iterator();
            String q1 = it.next();
            String q2 = it.next();
            CatenatingTerm finalTerm = catenationFor(queryOperator, termForQuery(field, q1, isOutgoingFolder), termForQuery(field, q2, isOutgoingFolder));
            while (it.hasNext()) {
                String q = it.next();
                CatenatingTerm newTerm = catenationFor(queryOperator, finalTerm.getSecondTerm(), termForQuery(field, q, isOutgoingFolder));
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
        if (Strings.isEmpty(query)) {
            throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(query, FIELD_TIME);
        }

        Comparison comparison;
        long timestamp;
        Calendar cal = new GregorianCalendar(TimeZones.UTC);
        if (MailConstants.FACET_VALUE_LAST_WEEK.equals(query)) {
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else if (MailConstants.FACET_VALUE_LAST_MONTH.equals(query)) {
            cal.add(Calendar.MONTH, -1);
            comparison = Comparison.GREATER_EQUALS;
            timestamp = cal.getTime().getTime();
        } else if (MailConstants.FACET_VALUE_LAST_YEAR.equals(query)) {
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
