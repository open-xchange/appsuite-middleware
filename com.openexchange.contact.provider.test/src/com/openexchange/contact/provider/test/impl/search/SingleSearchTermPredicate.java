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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.Operand;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link SingleSearchTermPredicate} represents a {@link SingleSearchTerm} as {@link nPredicate}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 * @param <T>
 */
public class SingleSearchTermPredicate<T> implements Predicate<T> {

    private final SingleSearchTerm term;
    private final List<ContactField> searchableFields;
    private final BiFunction<ContactField, Contact, Object> fieldMapper;
    private final BiFunction<ContactField, Object, Object> searchValueModifier;

    //@formatter:off
    /**
     * Initializes a new {@link SingleSearchTermPredicate}.
     *
     * @param term The {@link SingleSearchTerm} to parse
     * @param searchableFields A list of Fields supported by the search
     * @param fieldMapper A {@link BiFunction} that get's the actual field value for testing the predicate during the search.
     */
    public SingleSearchTermPredicate(SingleSearchTerm term,
        List<ContactField> searchableFields,
        BiFunction<ContactField, Contact, Object> fieldMapper) {

        this(term, searchableFields, fieldMapper, null);
    }

    /**
     * Initializes a new {@link SingleSearchTermPredicate}.
     *
     * @param term The {@link CompositeSearchTerm} to parse
     * @param searchableFields A list of Fields supported by the search
     * @param fieldMapper A {@link BiFunction} that get's the actual field value for testing the predicate during the search.
     * @param searchValueModifier A {@link BiFunction} that allows to modify a field value before testing if it matches the search criteria; can be null
     */
    public SingleSearchTermPredicate(SingleSearchTerm term,
        List<ContactField> searchableFields,
        BiFunction<ContactField, Contact, Object> fieldMapper,
        BiFunction<ContactField, Object, Object> searchValueModifier) {

        this.term = Objects.requireNonNull(term, "term must not be null");
        this.searchableFields = Objects.requireNonNull(searchableFields, "searchableFields must not be null");
        this.fieldMapper = Objects.requireNonNull(fieldMapper, "fieldMapper must not be null");
        this.searchValueModifier = searchValueModifier;
    }
    //@formatter:on

    @Override
    public boolean test(T t) {
        Optional<Operand<?>> column = Arrays.stream(term.getOperands()).filter(op -> op.getType() == Operand.Type.COLUMN).findFirst();
        Optional<Operand<?>> constant = Arrays.stream(term.getOperands()).filter(op -> op.getType() == Operand.Type.CONSTANT).findFirst();
        SingleOperation operation = term.getOperation();
        if (column.isPresent() && constant.isPresent()) {
            Object field = column.get().getValue();
            if (!searchableFields.contains(field)) {
                return false;
            }

            //Testing purpose only: no need to support other return types than string at the moment
            //@formatter:off
            String searchValue = searchValueModifier != null ?
                (String) searchValueModifier.apply((ContactField) field, constant.get().getValue()) :
                (String) constant.get().getValue();
            //@formatter:on

            String contactValue = (String) fieldMapper.apply((ContactField) field, (Contact) t);
            if (operation == SingleOperation.EQUALS) {
                if ((searchValue).startsWith("*") || (searchValue).endsWith("*")) {
                    searchValue = searchValue.startsWith("*") ? searchValue.substring(1) : searchValue;
                    searchValue = searchValue.endsWith("*") ? searchValue.substring(0, searchValue.length() - 1) : searchValue;
                    return contactValue != null && contactValue.contains(searchValue);
                }
                return searchValue.equals(contactValue);
            } else if (operation == SingleOperation.ISNULL) {
                return contactValue == null;
            } else if (operation == SingleOperation.NOT_EQUALS) {
                return !searchValue.equals(contactValue);
            }
            //Testing purpose only: no need to support other operations at the moment
        }

        return false;
    }

    @Override
    public String toString() {
        return term.toString();
    }
}
