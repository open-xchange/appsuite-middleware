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

package com.openexchange.chronos.common;

import java.util.List;
import com.openexchange.chronos.EventField;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.search.internal.operands.ConstantOperand;

/**
 * {@link SearchUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SearchUtils {

    /**
     * Gets a single search term using the field itself as column operand and adds the supplied value as constant operand.
     *
     * @param <V> The operand's type
     * @param <E> The field type
     * @param operation The operation to use
     * @param operand The value to use as constant operand
     * @return A single search term
     */
    public static <V, E extends Enum<?>> SingleSearchTerm getSearchTerm(E field, SingleOperation operation, V operand) {
        return getSearchTerm(field, operation, new ConstantOperand<V>(operand));
    }

    /**
     * Gets a single search term using the field itself as column operand and a second operand.
     *
     * @param <V> The operand's type
     * @param <E> The field type
     * @param operation The operation to use
     * @param operand The second operand
     * @return A single search term
     */
    public static <V, E extends Enum<?>> SingleSearchTerm getSearchTerm(E field, SingleOperation operation, Operand<V> operand) {
        return getSearchTerm(field, operation).addOperand(operand);
    }

    /**
     * Gets a single search term using the field itself as single column operand.
     *
     * @param <E> The field type
     * @param operation The operation to use
     * @param operand The value to use as constant operand
     * @return A single search term
     */
    public static <E extends Enum<?>> SingleSearchTerm getSearchTerm(E field, SingleOperation operation) {
        return new SingleSearchTerm(operation).addOperand(new ColumnFieldOperand<E>(field));
    }

    /**
     * Constructs a search term for a list of calendar queries. Each query is surrounded with wildcards implicitly, and matched against a
     * certain set of event fields.
     *
     * @param queries The queries to get the search term for
     * @return The search term, or <code>null</code> if the passed queries yield no search criteria
     */
    public static SearchTerm<?> buildSearchTerm(List<String> queries) {
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND);
        if (null != queries) {
            for (String query : queries) {
                if (false == isWildcardOnly(query)) {
                    String pattern = addWildcards(query, true, true);
                    searchTerm.addSearchTerm(new CompositeSearchTerm(CompositeOperation.OR)
                        .addSearchTerm(getSearchTerm(EventField.SUMMARY, SingleOperation.EQUALS, pattern))
                        .addSearchTerm(getSearchTerm(EventField.DESCRIPTION, SingleOperation.EQUALS, pattern))
                        .addSearchTerm(getSearchTerm(EventField.CATEGORIES, SingleOperation.EQUALS, pattern))
                    );
                }
            }
        }
        return 0 == searchTerm.getOperands().length ? null : 1 == searchTerm.getOperands().length ? searchTerm.getOperands()[0] : searchTerm;
    }

    /**
     * Gets a value indicating whether the supplied query is either empty, or solely consists of the wildcard character <code>*</code> or not.
     *
     * @param query The query to check
     * @return <code>true</code> for a wildcard-only query, <code>false</code>, otherwise
     */
    public static boolean isWildcardOnly(String query) {
        return Strings.isEmpty(query) || "*".equals(query);
    }

    /**
     * Appends and/or prepends the wildcard character <code>*</code> to the supplied search pattern, if not already done.
     *
     * @param pattern The pattern to add wildcard characters to
     * @param prepend <code>true</code> to prepend a wildcard character, <code>false</code> otherwise
     * @param append <code>true</code> to append a wildcard character, <code>false</code> otherwise
     * @return The pattern, with the wildcard characters added
     */
    public static String addWildcards(String pattern, boolean prepend, boolean append) {
        //TOOD: consider escaped wildcards
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

}
