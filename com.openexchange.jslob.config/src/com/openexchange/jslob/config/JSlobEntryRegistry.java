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
 *    trademarks of the OX Software GmbH group of companies.
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
