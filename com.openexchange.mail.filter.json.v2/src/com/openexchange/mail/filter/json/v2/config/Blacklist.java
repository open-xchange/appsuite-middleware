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

package com.openexchange.mail.filter.json.v2.config;

import java.util.Map;
import java.util.Set;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.BasicGroup;
import com.openexchange.mail.filter.json.v2.config.MailFilterBlacklistProperty.Field;

/**
 * {@link Blacklist} is a collection of blacklisted mail filter elements for a single user
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class Blacklist {

    private final Map<String, Set<String>> blackmap;

    /**
     * Initializes a new {@link Blacklist}.
     */
    public Blacklist(Map<String, Set<String>> blackmap) {
        super();
        this.blackmap = blackmap;
    }

    /**
     * Checks if the the value is blacklisted for the given {@link BasicGroup}
     *
     * @param basic The {@link BasicGroup} to check
     * @param value The value
     * @return true if the blacklist of the {@link BasicGroup} contains the given value, otherwise false.
     */
    public boolean isBlacklisted(BasicGroup basic, String value) {
        Set<String> set = blackmap.get(basic.name());
        return null == set ? false : set.contains(value);
    }

    /**
     * Checks if the the value is blacklisted for the given {@link BasicGroup}, element and {@link Field}
     *
     * @param basic The {@link BasicGroup}
     * @param element The element of the {@link BasicGroup}
     * @param field The {@link Field} of the element
     * @param value The value
     * @return true if the blacklist of the specific {@link Field} contains the given value, otherwise false.
     */
    public boolean isBlacklisted(BasicGroup basic, String element, Field field, String value) {
        Set<String> set = blackmap.get(key(basic, element, field));
        return null == set ? false : set.contains(value);
    }

    private static final char DOT = '.';

    /**
     * Creates a key which is used by this Blacklist
     *
     * @param basic The {@link BasicGroup}
     * @param element The element
     * @param field The {@link Field}
     * @return The key for this triplet
     */
    public static String key(BasicGroup basic, String element, Field field) {
        return new StringBuilder(basic.name()).append(DOT).append(element).append(DOT).append(field.name()).toString();
    }

    /**
     * Returns a single blacklist
     *
     * @param basic The {@link BasicGroup}
     * @param element The element name of the {@link BasicGroup}
     * @param field The {@link Field}
     * @return A set of blacklisted values or null
     */
    public Set<String> get(BasicGroup basic, String element, Field field) {
        return element == null ? blackmap.get(basic.name()) : blackmap.get(key(basic, element, field));
    }

}
