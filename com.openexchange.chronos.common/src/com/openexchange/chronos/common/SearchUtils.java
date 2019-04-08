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
