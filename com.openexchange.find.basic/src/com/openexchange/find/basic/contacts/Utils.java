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

package com.openexchange.find.basic.contacts;

import java.util.List;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.exception.OXException;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class Utils {

    /**
     * Creates a search term for the queries using a facet matching the supplied fields.
     *
     * @param session The server session
     * @param fields The filter fields to select the matching facet
     * @param queries The queries
     * @return The search term, or <code>null</code> to indicate a <code>FALSE</code> condition with empty results.
     * @throws OXException
     */
    public static SearchTerm<?> getSearchTerm(ServerSession session, ContactField[] fields, List<String> queries) throws OXException {
        if (queries.isEmpty()) {
            return null;
        }

        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (queries.size() == 1) {
            String query = queries.get(0);
            checkPatternLength(query, minimumSearchCharacters);
            return getTermForFields(fields, query);
        }
        CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
        int operands = 0;
        for (String query : queries) {
            if (fulfillsLengthConstraint(query, minimumSearchCharacters)) {
                andTerm.addSearchTerm(getTermForFields(fields, query));
                ++operands;
            }
        }

        if (operands > 0) {
            return andTerm;
        }
        // Fallback to avoid aborting the search as a whole
        String query = Strings.join(queries, " ");
        checkPatternLength(query, minimumSearchCharacters);
        return getTermForFields(fields, query);
    }

    private static SearchTerm<?> getTermForFields(ContactField[] fields, String query) {
        String pattern = addWildcards(query, true, true);
        CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (ContactField field : fields) {
            orTerm.addSearchTerm(getFieldEqualsPatternTerm(field, pattern));
        }

        return orTerm;
    }

    private static SingleSearchTerm getFieldEqualsPatternTerm(ContactField field, String pattern) {
        SingleSearchTerm searchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
        searchTerm.addOperand(new ContactFieldOperand(field));
        searchTerm.addOperand(new ConstantOperand<String>(pattern));
        return searchTerm;
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

    private static boolean fulfillsLengthConstraint(String pattern, int minimumSearchCharacters) {
        if (null != pattern && 0 < minimumSearchCharacters && pattern.length() < minimumSearchCharacters) {
            return false;
        }

        return true;
    }

    private static void checkPatternLength(String pattern, int minimumSearchCharacters) throws OXException {
        if (!fulfillsLengthConstraint(pattern, minimumSearchCharacters)) {
            throw FindExceptionCode.QUERY_TOO_SHORT.create(Integer.valueOf(minimumSearchCharacters));
        }
    }

}
