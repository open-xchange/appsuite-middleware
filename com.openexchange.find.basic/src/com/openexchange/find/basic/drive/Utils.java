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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.find.basic.drive;

import static com.openexchange.java.Strings.isEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.search.ContentTerm;
import com.openexchange.file.storage.search.DescriptionTerm;
import com.openexchange.file.storage.search.FileMimeTypeTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.file.storage.search.VersionCommentTerm;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.facet.Filter;


/**
 * {@link Utils} - Utilities for drive search.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utils {

    /**
     * Initializes a new {@link Utils}.
     */
    private Utils() {
        super();
    }

    /**
     * Gets the search term for given field and query
     *
     * @param field The field identifier
     * @param query The query
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If field is unknown
     */
    public static SearchTerm<?> termForQuery(final String field, final String query) throws OXException {
        if (isEmpty(field) || isEmpty(query)) {
            return null;
        }

        if (Constants.FIELD_FILE_NAME.equals(field)) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(2);
            terms.add(new FileNameTerm(query));
            terms.add(new TitleTerm(query, true, true));
            return new OrTerm(terms);
        } else if (Constants.FIELD_FILE_DESC.equals(field)) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(2);
            terms.add(new DescriptionTerm(query, true, true));
            terms.add(new VersionCommentTerm(query, true));
            return new OrTerm(terms);
        } else if (Constants.FIELD_FILE_CONTENT.equals(field)) {
            return new ContentTerm(query, true, true);
        } else if (Constants.FIELD_FILE_TYPE.equals(field)) {
            return new FileMimeTypeTerm(query);
        } else if (Constants.FIELD_FOLDER_TYPE.equals(field)) {
            // TODO
            return null;
        }

        throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(field);
    }

    /**
     * Gets the search term for given field and queries.
     *
     * @param field The field identifier
     * @param queries The queries
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If field is unknown
     */
    public static SearchTerm<?> termForField(final String field, final Set<String> queries) throws OXException {
        final int size = queries.size();
        if (size > 1) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(size);
            for (final String query : queries) {
                final SearchTerm<?> term = termForQuery(field, query);
                if (null != term) {
                    terms.add(term);
                }
            }

            if (terms.isEmpty()) {
                return null;
            }

            return new OrTerm(terms);
        }

        return termForQuery(field, queries.iterator().next());
    }

    /**
     * Gets the search term for given fields and queries.
     *
     * @param fields The field identifiers
     * @param queries The queries
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If a field is unknown
     */
    public static SearchTerm<?> termFor(final Set<String> fields, final Set<String> queries) throws OXException {
        final int size = fields.size();
        if (size > 1) {
            final List<SearchTerm<?>> terms = new ArrayList<SearchTerm<?>>(size);
            for (final String field : fields) {
                final SearchTerm<?> term = termForField(field, queries);
                if (null != term) {
                    terms.add(term);
                }
            }

            if (terms.isEmpty()) {
                return null;
            }

            return new OrTerm(terms);
        }

        return termForField(fields.iterator().next(), queries);
    }

    /**
     * Gets the search term for specified filter.
     *
     * @param filter The filter
     * @return The appropriate search term or <code>null</code>
     * @throws OXException If a filter is invalid
     */
    public static SearchTerm<?> termFor(final Filter filter) throws OXException {
        if (null == filter) {
            return null;
        }

        final Set<String> fields = filter.getFields();
        if (fields == null || fields.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_FIELDS.create(filter);
        }

        final Set<String> queries = filter.getQueries();
        if (queries == null || queries.isEmpty()) {
            throw FindExceptionCode.INVALID_FILTER_NO_QUERIES.create(filter);
        }

        return termFor(fields, queries);
    }

}
