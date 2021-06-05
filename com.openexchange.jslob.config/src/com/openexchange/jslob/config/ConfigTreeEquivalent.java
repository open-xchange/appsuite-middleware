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

package com.openexchange.jslob.config;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;


/**
 * {@link ConfigTreeEquivalent} - A bi-directional map for config tree to JSlob mappings and vice versa.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConfigTreeEquivalent {

    /** The config tree to JSlob mappings */
    public final Map<String, String> config2lob;

    /** The JSlob to config tree mappings */
    public final Map<String, String> lob2config;

    /**
     * Initializes a new {@link ConfigTreeEquivalent}.
     */
    public ConfigTreeEquivalent() {
        super();
        config2lob = new ConcurrentHashMap<String, String>(32, 0.9f, 1);
        lob2config = new ConcurrentHashMap<String, String>(32, 0.9f, 1);
    }

    /**
     * Merges specified config tree equivalent into this one.
     *
     * @param other The other one to merge
     */
    public void mergeWith(ConfigTreeEquivalent other) {
        if (null == other) {
            return;
        }

        Map<String, String> thisConfig2lob = this.config2lob;
        for (Map.Entry<String, String> e : other.config2lob.entrySet()) {
            thisConfig2lob.putIfAbsent(e.getKey(), e.getValue());
        }

        Map<String, String> thisLob2config = this.lob2config;
        for (Map.Entry<String, String> e : other.lob2config.entrySet()) {
            thisLob2config.putIfAbsent(e.getKey(), e.getValue());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(config2lob.size() << 4);

        Set<Map.Entry<String, String>> entrySet = new TreeSet<>(new Comparator<Map.Entry<String, String>>() {

            @Override
            public int compare(Map.Entry<String, String> entry1, Map.Entry<String, String> entry2) {
                return entry1.getKey().compareToIgnoreCase(entry2.getKey());
            }
        });
        entrySet.addAll(config2lob.entrySet());

        boolean first = true;
        for (Map.Entry<String, String> e : entrySet) {
            if (first) {
                first = false;
            } else {
                sb.append('\n');
            }
            sb.append(e.getKey()).append(" > ").append(e.getValue());
        }
        return sb.toString();
    }

}
