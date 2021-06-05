/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.provider.birthdays;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchExceptionMessages;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;

/**
 * {@link SearchAdapter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.0
 */
public class SearchAdapter {

    /** The contact fields considered for a birthday event's summary */
    private static final ContactField[] SUMMARY_FIELDS = { ContactField.DISPLAY_NAME, ContactField.SUR_NAME, ContactField.GIVEN_NAME };

    /** A synthetic search term that leads to no results */
    private static final SingleSearchTerm NO_RESULTS_TERM = new SingleSearchTerm(SingleOperation.ISNULL).addOperand(new ContactFieldOperand(ContactField.OBJECT_ID));

    /**
     * Gets a contact search term for the supplied list of search filters an general queries.
     *
     * @param filters A list of additional filters to be applied on the search, or <code>null</code> if not specified
     * @param queries The queries to search for, or <code>null</code> if not specified
     * @return The search term, or <code>null</code> if all contacts would be matched
     */
    public static SearchTerm<?> getContactSearchTerm(List<SearchFilter> filters, List<String> queries) throws OXException {
        List<SearchTerm<?>> searchTerms = new ArrayList<SearchTerm<?>>();
        List<SearchTerm<?>> filterTerms = getFieldFilterTerms(filters);
        if (null != filterTerms) {
            searchTerms.addAll(filterTerms);
        }
        SearchTerm<?> queryTerm = getQueryTerm(queries);
        if (null != queryTerm) {
            searchTerms.add(queryTerm);
        }
        if (searchTerms.isEmpty()) {
            return null;
        }
        if (1 == searchTerms.size()) {
            return searchTerms.get(0);
        }
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.AND);
        for (SearchTerm<?> searchTerm : searchTerms) {
            compositeTerm.addSearchTerm(searchTerm);
        }
        return compositeTerm;
    }

    /**
     * Gets a contact search term for the supplied calendar search term.
     *
     * @param calendarTerm The calendar term to get the contact search term for
     * @return The search term, or <code>null</code> if all contacts would be matched
     */
    public static SearchTerm<?> getContactSearchTerm(SearchTerm<?> calendarTerm) throws OXException {
        if (SingleSearchTerm.class.isInstance(calendarTerm)) {
            SingleSearchTerm singleSearchTerm = (SingleSearchTerm) calendarTerm;
            Operand<?> constantOperand = requireSingleOperand(singleSearchTerm, Operand.Type.CONSTANT);
            Operand<?> columnOperand = requireSingleOperand(singleSearchTerm, Operand.Type.COLUMN);
            if (false == EventField.class.isInstance(columnOperand.getValue())) {
                throw SearchExceptionMessages.PARSING_FAILED_UNSUPPORTED_OPERAND.create(columnOperand);
            }
            return getContactSearchTerm((EventField) columnOperand.getValue(), singleSearchTerm.getOperation(), constantOperand);
        }
        if (CompositeSearchTerm.class.isInstance(calendarTerm)) {
            CompositeSearchTerm contactCompositeSearchTerm = new CompositeSearchTerm((CompositeOperation) calendarTerm.getOperation());
            for (SearchTerm<?> calendarTermOperand : ((CompositeSearchTerm) calendarTerm).getOperands()) {
                contactCompositeSearchTerm.addSearchTerm(getContactSearchTerm(calendarTermOperand));
            }
            return contactCompositeSearchTerm;
        }
        throw new IllegalArgumentException("Need either an 'SingleSearchTerm' or 'CompositeSearchTerm'.");
    }

    /**
     * Extracts a single operand of a certain type from the supplied single search term, ensuring that exactly one operand of this type is
     * present in the term.
     * 
     * @param searchTerm The search term to extract the operand from
     * @param type The type of the operand to extract
     * @return The operand
     * @throws IllegalArgumentException If no or more than one operands of this type are found in the term
     */
    private static Operand<?> requireSingleOperand(SingleSearchTerm searchTerm, Operand.Type type) {
        Operand<?> singleOperand = null;
        for (Operand<?> operand : searchTerm.getOperands()) {
            if (type.equals(operand.getType())) {
                if (null != singleOperand) {
                    throw new IllegalArgumentException("Multiple operands of type " + type + " in term " + searchTerm);
                }
                singleOperand = operand; 
            }            
        }
        if (null == singleOperand) {
            throw new IllegalArgumentException("No operand of type " + type + " in term " + searchTerm);
        }                
        return singleOperand;
    }

    /**
     * Gets a search term for looking up contacts matching a certain event field criteria.
     * 
     * @param matchedField The event field to match
     * @param singleOperation The underlying search term's operation
     * @param constantOperand The underlying search term's constant operand
     * @return The contact search term
     * @throws OXException If search is not supported
     */
    private static SearchTerm<?> getContactSearchTerm(EventField matchedField, SingleOperation singleOperation, Operand<?> constantOperand) throws OXException {
        switch (matchedField) {
            case LOCATION:
            case DESCRIPTION:
            case ORGANIZER:
            case URL:
            case COLOR:
            case SEQUENCE:
            case CATEGORIES:
            case STATUS:
                return new SingleSearchTerm(singleOperation).addOperand(new ConstantOperand<String>("")).addOperand(constantOperand);
            case RECURRENCE_RULE:
                return new SingleSearchTerm(singleOperation).addOperand(new ConstantOperand<String>(EventConverter.BIRTHDAYS_RRULE)).addOperand(constantOperand);
            case TRANSP:
                return new SingleSearchTerm(singleOperation).addOperand(new ConstantOperand<String>(EventConverter.BIRTHDAYS_TRANSP.getValue())).addOperand(constantOperand);
            case CLASSIFICATION:
                return new SingleSearchTerm(singleOperation).addOperand(new ConstantOperand<String>(EventConverter.BIRTHDAYS_CLASSIFICATION.getValue())).addOperand(constantOperand);
            case TIMESTAMP:
            case LAST_MODIFIED:
                return getContactFieldTerm(ContactField.LAST_MODIFIED, singleOperation, constantOperand);
            case CREATED:
                return getContactFieldTerm(ContactField.CREATION_DATE, singleOperation, constantOperand);
            case UID:
                return getContactFieldTerm(ContactField.UID, singleOperation, constantOperand);
            case SUMMARY:
                return getSummaryTerm(singleOperation, constantOperand);
            case ATTENDEES:
                return getAttendeesTerm(singleOperation, constantOperand);
            default:
                throw SearchExceptionMessages.PARSING_FAILED_UNSUPPORTED_OPERAND.create(matchedField);
        }
    }

    private static CompositeSearchTerm getSummaryTerm(SingleOperation singleOperation, Operand<?> constantOperand) {
        CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (ContactField field : SUMMARY_FIELDS) {
            orTerm.addSearchTerm(getContactFieldTerm(field, singleOperation, constantOperand));
        }
        return orTerm;
    }

    private static SingleSearchTerm getAttendeesTerm(SingleOperation singleOperation, Operand<?> constantOperand) {
        if (Number.class.isInstance(constantOperand.getValue())) {
            /*
             * match against contact user id (as attendee entity identifier)
             */
            return new SingleSearchTerm(singleOperation).addOperand(new ContactFieldOperand(ContactField.INTERNAL_USERID)).addOperand(constantOperand);
        }
        /*
         * match against contact email1 (as attendee uri), otherwise
         */
        return new SingleSearchTerm(singleOperation).addOperand(new ContactFieldOperand(ContactField.EMAIL1)).addOperand(constantOperand);
    }

    private static SingleSearchTerm getContactFieldTerm(ContactField matchedField, SingleOperation singleOperation, Operand<?> constantOperand) {
        return new SingleSearchTerm(singleOperation).addOperand(new ContactFieldOperand(matchedField)).addOperand(constantOperand);
    }

    private static SearchTerm<?> getQueryTerm(List<String> queries) {
        return getContactFieldTerm(queries, CompositeOperation.AND, SUMMARY_FIELDS);
    }

    private static List<SearchTerm<?>> getFieldFilterTerms(List<SearchFilter> filters) throws OXException {
        List<SearchTerm<?>> searchTerms = new ArrayList<SearchTerm<?>>();
        if (null == filters || 0 >= filters.size()) {
            return new LinkedList<>();
        }
        for (SearchFilter filter : filters) {
            List<String> fields = filter.getFields();
            if (null == fields || 0 >= fields.size()) {
                continue;
            }
            processFields(searchTerms, filter, fields);
        }
        return searchTerms;
    }

    private static void processFields(List<SearchTerm<?>> searchTerms, SearchFilter filter, List<String> fields) throws OXException {
        for (String field : fields) {
            SearchTerm<?> searchTerm = getFieldFilterTerm(field, filter.getQueries());
            if (null == searchTerm) {
                continue;
            }
            searchTerms.add(searchTerm);
        }
    }

    private static SearchTerm<?> getFieldFilterTerm(String field, List<String> queries) throws OXException {
        switch (field) {
            case "range":
                // out-of-range event occurrences are filtered afterwards
                return null;
            case "subject":
                return getContactFieldTerm(queries, CompositeOperation.AND, SUMMARY_FIELDS);
            case "users":
                return getContactUserIdTerm(queries, CompositeOperation.AND);
            case "participants":
                return getContactEMailTerm(queries, CompositeOperation.AND);
            case "location":
            case "description":
            case "status":
            case "attachment":
                /*
                 * any non-wildcard filter leads to zero results
                 */
                return isWildcardOnly(queries) ? null : NO_RESULTS_TERM;
            case "type":
                /*
                 * any non-series event filter leads to zero results
                 */
                return isSeriesTypeOnly(queries) ? null : NO_RESULTS_TERM;
            default:
                throw new IllegalArgumentException("Unsupported filter field: " + field);
        }
    }

    private static boolean isSeriesTypeOnly(List<String> recurringTypeQueries) {
        if (null != recurringTypeQueries && 0 < recurringTypeQueries.size()) {
            for (String query : recurringTypeQueries) {
                if (false == "series".equals(query)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static SearchTerm<?> getContactUserIdTerm(List<String> queries, CompositeOperation operation) throws OXException {
        if (null == queries || queries.isEmpty()) {
            return null;
        }
        if (1 == queries.size()) {
            return getContactUserIdTerm(queries.get(0));
        }
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm(operation);
        for (String query : queries) {
            compositeTerm.addSearchTerm(getContactUserIdTerm(query));
        }
        return compositeTerm;
    }

    private static SearchTerm<?> getContactUserIdTerm(String query) throws OXException {
        if (isWildcardOnly(query)) {
            return null;
        }
        try {
            return new SingleSearchTerm(SingleOperation.EQUALS)
                .addOperand(new ContactFieldOperand(ContactField.INTERNAL_USERID))
                .addOperand(new ConstantOperand<Integer>(Integer.valueOf(query)))
            ;
        } catch (NumberFormatException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static boolean isWildcardOnly(String query) {
        return Strings.isEmpty(query) || "*".equals(query);
    }

    private static boolean isWildcardOnly(List<String> queries) {
        if (null != queries) {
            for (String query : queries) {
                if (isWildcardOnly(query)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String addWildcards(String pattern, boolean prepend, boolean append) {
        if ((null == pattern || 0 == pattern.length()) && (append || prepend)) {
            return "*";
        }
        if (null != pattern) {
            if (prepend && '*' != pattern.charAt(0)) {
                pattern = "*" + pattern;
            }
            if (append && '*' != pattern.charAt(pattern.length() - 1)) {
                pattern = pattern + "*";
            }
        }
        return pattern;
    }

    private static SearchTerm<?> getContactFieldTerm(List<String> queries, CompositeOperation operation, ContactField... fields) {
        if (null == queries || queries.isEmpty()) {
            return null;
        }
        if (1 == queries.size()) {
            return getContactFieldTerm(queries.get(0), fields);
        }
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm(operation);
        for (String query : queries) {
            compositeTerm.addSearchTerm(getContactFieldTerm(query, fields));
        }
        return compositeTerm;
    }

    private static SearchTerm<?> getContactFieldTerm(String query, ContactField... fields) {
        if (isWildcardOnly(query)) {
            return null;
        }
        String pattern = addWildcards(query, true, true);
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (ContactField field : fields) {
            compositeTerm.addSearchTerm(new SingleSearchTerm(SingleOperation.EQUALS)
                .addOperand(new ContactFieldOperand(field))
                .addOperand(new ConstantOperand<String>(pattern)
            ));
        }
        return compositeTerm;
    }

    private static SearchTerm<?> getContactEMailTerm(List<String> queries, CompositeOperation operation) {
        if (null == queries || queries.isEmpty()) {
            return null;
        }
        if (1 == queries.size()) {
            return getContactEMailTerm(queries.get(0));
        }
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm(operation);
        for (String query : queries) {
            compositeTerm.addSearchTerm(getContactEMailTerm(query));
        }
        return compositeTerm;
    }

    private static SearchTerm<?> getContactEMailTerm(String query) {
        if (isWildcardOnly(query)) {
            return null;
        }
        String mailAddress = CalendarUtils.extractEMailAddress(query);
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (ContactField field : new ContactField[] { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 }) {
            compositeTerm.addSearchTerm(new SingleSearchTerm(SingleOperation.EQUALS)
                .addOperand(new ContactFieldOperand(field))
                .addOperand(new ConstantOperand<String>(mailAddress)
            ));
        }
        return compositeTerm;
    }

    /**
     * Initializes a new {@link SearchAdapter}.
     */
    private SearchAdapter() {
        super();
    }

}
