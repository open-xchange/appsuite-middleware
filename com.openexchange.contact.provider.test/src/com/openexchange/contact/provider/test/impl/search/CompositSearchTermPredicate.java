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

import java.util.List;
import java.util.function.Predicate;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operation;

/**
 * {@link CompositSearchTermPredicate} - Wraps a {@link CompositeSearchTerm} as {@link Predicate}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 * @param <T> The type of objects to search
 */
class CompositSearchTermPredicate<T> implements Predicate<T> {

    private final CompositeSearchTerm term;
    private final List<Predicate<T>> predicates;

    /**
     * Initializes a new {@link CompositSearchTermPredicate}.
     *
     * @param term The {@link CompositeSearchTerm} to represent
     * @param predicates A list of {@link Predicate}s to wrap
     */
    public CompositSearchTermPredicate(CompositeSearchTerm term, List<Predicate<T>> predicates) {
        this.term = term;
        this.predicates = predicates;
    }

    @Override
    public boolean test(T t) {
        Operation operation = term.getOperation();
        if (operation == CompositeOperation.AND) {
            for (Predicate<T> p : predicates) {
                if (!p.test(t)) {
                    return false;
                }
            }
            return true;
        } else if (operation == CompositeOperation.OR) {
            for (Predicate<T> p : predicates) {
                if (p.test(t)) {
                    return true;
                }
            }
            return false;
        } else if (operation == CompositeOperation.NOT) {
            Predicate<T> p = predicates.get(0);
            return p.negate().test(t);
        }
        return false;
    }

    @Override
    public String toString() {
        return term.toString();
    }
}
