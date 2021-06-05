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

package com.openexchange.java;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * {@link Sets} - A utility class for <code>java.util.Set</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class Sets {

    /**
     * Initializes a new {@link Sets}.
     */
    private Sets() {
        super();
    }

    /**
     * Generates consecutive subsets of a set, each of the same size (the final set may be smaller).
     *
     * @param original The set to return consecutive subsets of
     * @param partitionSize The desired size for each subset
     * @return A list of consecutive subsets
     * @throws IllegalArgumentException if {@code partitionSize} is not positive
     */
    public static <T> List<Set<T>> partition(Set<T> original, int partitionSize) {
        checkNotNull(original, "Set must not be null");
        checkArgument(partitionSize > 0);
        int total = original.size();
        if (partitionSize >= total) {
            return Collections.singletonList(original);
        }

        // Create a list of sets to return.
        List<Set<T>> result = new LinkedList<Set<T>>();

        // Create an iterator for the original set.
        Iterator<T> it = original.iterator();

        // Create each new set.
        Set<T> s = new LinkedHashSet<T>(partitionSize);
        s.add(it.next());
        for (int i = 1; i < total; i++) {
            if ((i % partitionSize) == 0) {
                result.add(s);
                s = new LinkedHashSet<T>(partitionSize);
            }
            s.add(it.next());
        }
        result.add(s);
        return result;
    }

}
