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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.groupware.ldap;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * {@link UserAttribute} - Represents a user attribute.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserAttribute {

    private final String name;
    private final Map<String, UUID> uuids;
    private final Set<String> values;

    /**
     * Initializes a new {@link UserAttribute}.
     *
     * @param name The name
     * @param values The values
     */
    public UserAttribute(final String name) {
        super();
        this.name = name;
        uuids = new HashMap<String, UUID>(4);
        values = new LinkedHashSet<String>(4);
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the values
     *
     * @return The values
     */
    public Set<String> getValues() {
        return values;
    }

    /**
     * Gets the UUID for given value.
     *
     * @param value The value
     * @return The associated UUID or <code>null</code>
     */
    public UUID getUuidFor(final String value) {
        return uuids.get(value);
    }

    /**
     * Adds specified values
     *
     * @param value The values
     */
    public void addValues(final Collection<String> values) {
        for (final String value : values) {
            addValue(value, null);
        }
    }

    /**
     * Adds specified value
     *
     * @param value The value
     * @return <code>true</code> if successfully added; otherwise <code>false</code> if already present
     */
    public boolean addValue(final String value) {
        return addValue(value, null);
    }

    /**
     * Adds specified value
     *
     * @param value The value
     * @param uuid the associated UUID
     * @return <code>true</code> if successfully added; otherwise <code>false</code> if already present
     */
    public boolean addValue(final String value, final UUID uuid) {
        if (values.add(value)) {
            if (null != uuid) {
                uuids.put(value, uuid);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the number of attribute values
     *
     * @return The number of values.
     */
    public int size() {
        return values.size();
    }

    /**
     * Adds all values of given attribute.
     *
     * @param attr The attribute
     */
    public void addAll(UserAttribute attr) {
        for (final String value : attr.values) {
            addValue(value, attr.uuids.get(value));
        }
    }

    /**
     * Removes all values from given attribute
     *
     * @param attr The attribute
     */
    public void removeAll(UserAttribute attr) {
        for (final String value : attr.values) {
            values.remove(value);
            uuids.remove(value);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserAttribute)) {
            return false;
        }
        final UserAttribute other = (UserAttribute) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append("UserAttribute [");
        if (name != null) {
            builder.append("name=").append(name).append(", ");
        }
        if (values != null) {
            builder.append("values=").append(values).append(", ");
        }
        if (uuids != null) {
            builder.append("uuids=").append(uuids.values());
        }
        builder.append("]");
        return builder.toString();
    }

}
