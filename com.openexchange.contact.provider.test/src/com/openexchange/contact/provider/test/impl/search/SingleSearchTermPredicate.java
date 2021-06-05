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
