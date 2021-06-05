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

package com.openexchange.group.internal;

import com.openexchange.config.lean.Property;

/**
 * {@link GroupProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public enum GroupProperty implements Property {

    /**
     * Configures whether the group "All users" should be hidden to clients when listing all groups or searching for groups in a context.
     * The virtual group "All users" always contains all existing users of a context but no guests.
     */
    HIDE_ALL_USERS("hideAllUsers", Boolean.FALSE),

    /**
     * Configures whether the group "Guests" should be hidden to clients when listing all groups or searching for groups in a context. The
     * virtual group "Guests" always contains all existing guest users of a context but no users, and is mainly used as entity in
     * permissions of system folders.
     */
    HIDE_ALL_GUESTS("hideAllGuests", Boolean.TRUE),

    /**
     * Configures whether the group "Standard group" should be hidden to clients when listing all groups or searching for groups in a
     * context. Every created user will be added to this non-virtual group automatically, but can be removed again later on.
     */
    HIDE_STANDARD_GROUP("hideStandardGroup", Boolean.TRUE),

    ;

    private final Object defaultValue;
    private final String fqn;

    /**
     * Initializes a new {@link GroupProperty}.
     *
     * @param suffix The property name suffix
     * @param defaultValue The property's default value
     */
    private GroupProperty(String suffix, Object defaultValue) {
        this.defaultValue = defaultValue;
        this.fqn = "com.openexchange.group." + suffix;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
