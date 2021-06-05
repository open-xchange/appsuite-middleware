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

package com.openexchange.pns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.Iterators;


/**
 * {@link IteratorBackedHits}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class IteratorBackedHits implements Hits {

    private final List<Hits> hits;

    /**
     * Initializes a new {@link IteratorBackedHits}.
     */
    public IteratorBackedHits(List<Hits> hits) {
        super();
        this.hits = hits;
    }

    @Override
    public Iterator<Hit> iterator() {
        List<Hits> thisHits = this.hits;
        int size = thisHits.size();
        if (size == 0) {
            return Collections.emptyIterator();
        }

        if (size == 1) {
            return Iterators.unmodifiableIterator(thisHits.get(0).iterator());
        }

        List<Iterator<Hit>> iters = new ArrayList<>(size);
        for (Hits hts : thisHits) {
            iters.add(hts.iterator());
        }
        return Iterators.unmodifiableIterator(Iterators.concat(iters.iterator()));
    }

    @Override
    public boolean isEmpty() {
        return null == hits || hits.isEmpty();
    }

}
