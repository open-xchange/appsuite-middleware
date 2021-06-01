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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobEntry;

/**
 * {@link JSlobEntryRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class JSlobEntryRegistry {

    private final ConcurrentMap<String, Map<String, JSlobEntryWrapper>> registry;

    /**
     * Initializes a new {@link JSlobEntryRegistry}.
     */
    public JSlobEntryRegistry() {
        super();
        registry = new ConcurrentHashMap<>(16, 0.9F, 1);
    }

    /**
     * Adds the given JSlob entry
     *
     * @param jSlobEntry
     * @return <code>true</code> if successfully added; otherwise <code>false</code>
     * @throws OXException If entry's path cannot be parsed
     */
    public synchronized boolean addJSlobEntry(JSlobEntry jSlobEntry) throws OXException {
        if (null == jSlobEntry) {
            return false;
        }

        JSlobEntryWrapper wrapper = new JSlobEntryWrapper(jSlobEntry);

        ConcurrentMap<String, JSlobEntryWrapper> entries = (ConcurrentMap<String, JSlobEntryWrapper>) registry.get(jSlobEntry.getKey());
        if (null == entries) {
            ConcurrentMap<String, JSlobEntryWrapper> newMap = new ConcurrentHashMap<>(4, 0.9F, 1);
            entries = (ConcurrentMap<String, JSlobEntryWrapper>) registry.putIfAbsent(jSlobEntry.getKey(), newMap);
            if (null == entries) {
                entries = newMap;
            }
        }

        return null == entries.putIfAbsent(jSlobEntry.getPath(), wrapper);
    }

    /**
     * Removes the given JSlob entry
     *
     * @param jSlobEntry
     */
    public synchronized void removeJSlobEntry(JSlobEntry jSlobEntry) {
        if (null == jSlobEntry) {
            return;
        }

        ConcurrentMap<String, JSlobEntryWrapper> entries = (ConcurrentMap<String, JSlobEntryWrapper>) registry.get(jSlobEntry.getKey());
        if (null != entries) {
            boolean removed = null != entries.remove(jSlobEntry.getPath());
            if (removed && entries.isEmpty()) {
                registry.remove(jSlobEntry.getKey());
            }
        }
    }

    /**
     * Gets the currently available registered JSlob entries.
     *
     * @return The available JSlob entries;
     */
    public Map<String, Map<String, JSlobEntryWrapper>> getAvailableJSlobEntries() {
        return registry;
    }

}
