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
import static com.openexchange.find.basic.mail.Constants.FIELD_FOLDER;
import static com.openexchange.find.basic.mail.Constants.FIELD_FROM;
import static com.openexchange.find.basic.mail.Constants.FIELD_SUBJECT;
import static com.openexchange.find.basic.mail.Constants.FIELD_TIME_RANGE;
import static com.openexchange.find.basic.mail.Constants.FIELD_TO;
import static com.openexchange.find.basic.mail.Constants.QUERY_FIELDS;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.facet.Filter;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.BodyTerm;
import com.openexchange.mail.search.CcTerm;
import com.openexchange.mail.search.FromTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SubjectTerm;
import com.openexchange.mail.search.ToTerm;
import com.openexchange.tools.session.ServerSession;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SearchParameters {

    private final String folder;
    private final int start;
    private final int size;
    private final SearchTerm<?> searchTerm;

    private SearchParameters(String folder, int start, int size, SearchTerm<?> searchTerm) {
        super();
        this.folder = folder;
        this.start = start;
        this.size = size;
        this.searchTerm = searchTerm;
    }

    public String getMailFolder() {
        return folder;
    }

    public IndexRange getIndexRange() {
        return new IndexRange(start, start + size);
    }

    public MailSortField getSortField() {
        return MailSortField.RECEIVED_DATE;
    }

    public OrderDirection getOrderDirection() {
        return OrderDirection.DESC;
    }

    public MailField[] getMailFields() {
        return MailField.FIELDS_LOW_COST;
    }

    public int getStart() {
        return start;
    }

    public int getSize() {
        return size;
    }

    public SearchTerm<?> getSearchTerm() {
        return searchTerm;
    }

    public static SearchParameters newInstance(SearchRequest searchRequest, ServerSession session) throws OXException {
        List<Filter> filters = new LinkedList<Filter>(searchRequest.getFilters());
        if (filters == null || filters.isEmpty()) {
            throw FindExceptionCode.MISSING_SEARCH_FILTER.create("folder", Module.MAIL.getIdentifier());
        }

        String folderName = prepareFiltersAndGetFolder(filters);
        if (folderName == null) {
            throw FindExceptionCode.MISSING_SEARCH_FILTER.create("folder", Module.MAIL.getIdentifier());
        }

        SearchTerm<?> queryTerm = prepareQueryTerm(new HashSet<String>(searchRequest.getQueries()));
        SearchTerm<?> filterTerm = prepareSearchTerm(filters);
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

        return new SearchParameters(
            folderName,
            searchRequest.getStart(),
            searchRequest.getSize(),
            searchTerm);
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

    private static SearchTerm<?> prepareQueryTerm(Set<String> queries) throws OXException {
        if (queries == null || queries.isEmpty()) {
            return null;
        }

        return termFor(QUERY_FIELDS, queries);
    }

    private static SearchTerm<?> prepareSearchTerm(List<Filter> filters) throws OXException {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        if (filters.size() == 1) {
            return termFor(filters.get(0));
        }

        Iterator<Filter> it = filters.iterator();
        Filter f1 = it.next();
        Filter f2 = it.next();
        ANDTerm finalTerm = new ANDTerm(termFor(f1), termFor(f2));
        while (it.hasNext()) {
            ANDTerm newTerm = new ANDTerm(finalTerm.getSecondTerm(), termFor(it.next()));
            finalTerm.setSecondTerm(newTerm);
        }

        return finalTerm;
    }

    private static String determineFolderName(Filter filter) {
        String folderName = null;
        Set<String> fields = filter.getFields();
        if (fields.size() == 1 && FIELD_FOLDER.equals(fields.iterator().next())) {
            try {
                folderName = filter.getQueries().iterator().next();
            } catch (NoSuchElementException e) {
                // ignore
            }
        }

        return folderName;
    }

    private static SearchTerm<?> termFor(Filter filter) throws OXException {
        Set<String> fields = filter.getFields();
        if (fields == null || fields.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_FIELDS.create(filter);
        }

        Set<String> queries = filter.getQueries();
        if (queries == null || queries.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_QUERIES.create(filter);
        }

        return termFor(fields, queries);
    }

    private static SearchTerm<?> termFor(Set<String> fields, Set<String> queries) throws OXException {
        if (fields.size() > 1) {
            Iterator<String> it = fields.iterator();
            String f1 = it.next();
            String f2 = it.next();
            ORTerm finalTerm = new ORTerm(termForField(f1, queries), termForField(f2, queries));
            while (it.hasNext()) {
                String f = it.next();
                ORTerm newTerm = new ORTerm(finalTerm.getSecondTerm(), termForField(f, queries));
                finalTerm.setSecondTerm(newTerm);
            }

            return finalTerm;
        }

        return termForField(fields.iterator().next(), queries);
    }

    private static SearchTerm<?> termForField(String field, Set<String> queries) throws OXException {
        if (queries.size() > 1) {
            Iterator<String> it = queries.iterator();
            String q1 = it.next();
            String q2 = it.next();
            ORTerm finalTerm = new ORTerm(termForQuery(field, q1), termForQuery(field, q2));
            while (it.hasNext()) {
                String q = it.next();
                ORTerm newTerm = new ORTerm(finalTerm.getSecondTerm(), termForQuery(field, q));
                finalTerm.setSecondTerm(newTerm);
            }

            return finalTerm;
        }

        return termForQuery(field, queries.iterator().next());
    }

    private static SearchTerm<?> termForQuery(String field, String query) throws OXException {
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
        } else if (FIELD_TIME_RANGE.equals(field)) {
            // TODO: ReceivedDateTerm or SentDateTerm based on folder
            return null;
        }

        throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(field);
    }

}
