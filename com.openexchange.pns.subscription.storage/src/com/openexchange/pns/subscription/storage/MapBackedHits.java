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

package com.openexchange.pns.subscription.storage;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.pns.Hit;
import com.openexchange.pns.Hits;
import com.openexchange.pns.PushMatch;


/**
 * {@link MapBackedHits}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MapBackedHits implements Hits {

    /** The empty instance */
    public static final MapBackedHits EMPTY = new MapBackedHits(Collections.<ClientAndTransport, List<PushMatch>> emptyMap());

    // ----------------------------------------------------------------------------------

    private final Map<ClientAndTransport, List<PushMatch>> map;

    /**
     * Initializes a new {@link MapBackedHits}.
     */
    public MapBackedHits(Map<ClientAndTransport, List<PushMatch>> map) {
        super();
        this.map = map;
    }

    /**
     * Gets the backing map.
     *
     * @return The map
     */
    public Map<ClientAndTransport, List<PushMatch>> getMap() {
        return map;
    }

    @Override
    public Iterator<Hit> iterator() {
        return new MapBackedHitsIterator(map.entrySet().iterator());
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    // --------------------------------------------------------------------------------------------

    private static final class MapBackedHitsIterator implements Iterator<Hit> {

        private final Iterator<Entry<ClientAndTransport, List<PushMatch>>> iterator;

        /**
         * Initializes a new {@link MapBackedHitsIterator}.
         */
        MapBackedHitsIterator(Iterator<Map.Entry<ClientAndTransport, List<PushMatch>>> iterator) {
            super();
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Hit next() {
            Map.Entry<ClientAndTransport, List<PushMatch>> entry = iterator.next();
            ClientAndTransport cat = entry.getKey();
            return new MapBackedHit(cat.client, cat.transportId, entry.getValue());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

}
