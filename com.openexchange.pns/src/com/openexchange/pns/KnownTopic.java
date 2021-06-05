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

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link KnownTopic} - An enumeration for well-known topics.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum KnownTopic {

    /**
     * <code>*</code>
     * <p>
     * The special topic matching all topic identifiers.
     */
    ALL("*"),

    // ------------------------------------------------ MAIL ----------------------------------------------------

    /**
     * <code>ox:mail:new</code>
     * <p>
     * The topic for a newly arrived mail.
     */
    MAIL_NEW("ox:mail:new"),

    // ------------------------------------------------ CALENDAR ------------------------------------------------

    /**
     * <code>ox:calendar:updates</code>
     * <p>
     * The topic for updates in the calendar module.
     */
    CALENDAR_UPDATES("ox:calendar:updates"),

    ;

    private final String name;

    private KnownTopic(final String name) {
        this.name = name;
    }

    /**
     * Gets the topic name
     *
     * @return The topic name
     */
    public String getName() {
        return name;
    }

    private static final Map<String, KnownTopic> STRING2NAME;
    static {
        final KnownTopic[] values = KnownTopic.values();
        final Map<String, KnownTopic> m = new HashMap<String, KnownTopic>(values.length);
        for (final KnownTopic name : values) {
            m.put(name.getName(), name);
        }
        STRING2NAME = ImmutableMap.copyOf(m);
    }

    /**
     * Gets the associated {@code KnownTopic} enum.
     *
     * @param sName The name string
     * @return The {@code KnownTopic} enum or <code>null</code>
     */
    public static KnownTopic nameFor(final String sName) {
        return null == sName ? null : STRING2NAME.get(sName);
    }
}
