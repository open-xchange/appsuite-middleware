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

package com.openexchange.java.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link NativeBuilders}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NativeBuilders {

    public static class MapBuilder<K, V> {

        private Map<K, V> map = new HashMap<K, V>();

        public MapBuilder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return map;
        }
    }

    public static class ListBuilder<T> {

        private List<T> list = new ArrayList<T>();

        public ListBuilder<T> add(T... elements) {
            list.addAll(Arrays.asList(elements));
            return this;
        }

        public List<T> build() {
            return list;
        }
    }

    public static <K,V> MapBuilder<K,V> map() {
        return new MapBuilder<K,V>();
    }

    public static <T> ListBuilder<T> list() {
        return new ListBuilder<T>();
    }
}
