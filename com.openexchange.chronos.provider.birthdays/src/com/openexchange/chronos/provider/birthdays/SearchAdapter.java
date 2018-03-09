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

package com.openexchange.chronos.provider.birthdays;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
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
    private static final SearchTerm<?> NO_RESULTS_TERM = new SingleSearchTerm(SingleOperation.ISNULL).addOperand(new ContactFieldOperand(ContactField.OBJECT_ID));

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

    private static SearchTerm<?> getQueryTerm(List<String> queries) throws OXException {
        return getContactFieldTerm(queries, CompositeOperation.AND, SUMMARY_FIELDS);
    }

    private static List<SearchTerm<?>> getFieldFilterTerms(List<SearchFilter> filters) throws OXException {
        List<SearchTerm<?>> searchTerms = new ArrayList<SearchTerm<?>>();
        if (null != filters && 0 < filters.size()) {
            for (SearchFilter filter : filters) {
                List<String> fields = filter.getFields();
                if (null != fields && 0 < fields.size()) {
                    for (String field : fields) {
                        SearchTerm<?> searchTerm = getFieldFilterTerm(field, filter.getQueries());
                        if (null != searchTerm) {
                            searchTerms.add(searchTerm);
                        }
                    }
                }
            }
        }
        return searchTerms;
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
            case "participant":
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

    private static boolean isSeriesTypeOnly(List<String> recurringTypeQueries) throws OXException {
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
