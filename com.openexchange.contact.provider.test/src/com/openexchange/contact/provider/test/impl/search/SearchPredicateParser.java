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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.contact.provider.test.impl.search;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchExceptionMessages;

/**
 * {@link SearchPredicateParser} parses a {@link SearchTerm} into a {@link Predicate}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 * @param <T> The Type of the object to search
 */
public class SearchPredicateParser<T> {

    /**
     * Internal method to parse a {@link SingleSearchTerm} into a {@link SingleSearchTermPredicate}
     *
     * @param term The {@link SingleSearchTerm} to parse
     * @param searchableFields A list of Fields supported by the search
     * @param fieldMapper A {@link BiFunction} that get's the actual field value for testing the predicate during the search.
     * @param searchValueModifier A {@link BiFunction} that allows to modify a field value before testing if it matches the search criteria; can be null
     * @return The parsed {@link SingleSearchTermPredicate}
     */
    //@formatter:off
    private SingleSearchTermPredicate<T> parseSingleSearchTerm(SingleSearchTerm term,
        List<ContactField> searchableFields,
        BiFunction<ContactField, Contact, Object> fieldMapper,
        @Nullable BiFunction<ContactField, Object, Object> searchValueModifier) {

        return new SingleSearchTermPredicate<T>(term,
            searchableFields,
            fieldMapper,
            searchValueModifier);
    }
    //@formatter:on

    /**
     * Internal method to parse a {@link CompositeSearchTerm} into a {@link CompositSearchTermPredicate}
     *
     * @param term The {@link CompositeSearchTerm} to parse
     * @param searchableFields A list of Fields supported by the search
     * @param fieldMapper A {@link BiFunction} that get's the actual field value for testing the predicate during the search.
     * @param searchValueModifier A {@link BiFunction} that allows to modify a field value before testing if it matches the search criteria; can be null
     * @return The parsed {@link SingleSearchTermPredicate}
     */
    @SuppressWarnings("unchecked")
    private CompositSearchTermPredicate<T> parseCompositeSearchTerm(/*@formatter:off*/
        CompositeSearchTerm term,
        List<ContactField> searchableFields,
        BiFunction<ContactField, Contact, Object> fieldMapper,
        @Nullable BiFunction<ContactField, Object, Object> searchValueModifier /*@formatter:on*/) throws OXException {

        List<Predicate<T>> predicates = new ArrayList<Predicate<T>>();
        SearchTerm<T>[] operands = (SearchTerm<T>[]) term.getOperands();
        for (SearchTerm<T> operand : operands) {
            predicates.add(parse(operand, searchableFields, fieldMapper, searchValueModifier));
        }

        CompositeOperation operation = (CompositeOperation) term.getOperation();
        if (operation == CompositeOperation.NOT && predicates.size() != 1) {
            throw SearchExceptionMessages.PARSING_FAILED_INVALID_SEARCH_TERM.create();
        }

        return new CompositSearchTermPredicate<T>(term, predicates);
    }

    /**
     * Parses the given {@link SearchTerm} in a {@link Predicate}
     *
     * @param term The {@link SearchTerm} to parse
     * @param searchableFields A list of Fields supported by the search
     * @param fieldMapper A {@link BiFunction} that get's the actual field value for testing the predicate during the search.
     * @param searchValueModifier A {@link BiFunction} that allows to modify a field value before testing if it matches the search criteria; can be null
     * @return The {@link Predicate} which represents the given {@link SearchTerm}
     * @throws OXException
     */
    //@formatter:off
    public Predicate<T> parse(SearchTerm<T> term,
        List<ContactField> searchableFields,
        BiFunction<ContactField, Contact, Object> fieldMapper,
        @Nullable BiFunction<ContactField, Object, Object> searchValueModifier) throws OXException {

        if (term instanceof SingleSearchTerm) {
            return parseSingleSearchTerm((SingleSearchTerm)term,
                searchableFields,
                fieldMapper,
                searchValueModifier);
        } else if (term instanceof CompositeSearchTerm) {
            return parseCompositeSearchTerm((CompositeSearchTerm) term,
                searchableFields,
                fieldMapper,
                searchValueModifier);
        }
        return null;
    }
     //@formatter:off
}
